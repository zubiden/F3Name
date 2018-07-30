package ua.coolboy.f3name.bungee;

import ua.coolboy.f3name.metrics.BungeeMetrics;
import com.google.common.collect.ImmutableList;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Plugin;
import ua.coolboy.f3name.bungee.messenger.BungeeMessenger;

import ua.coolboy.f3name.core.F3Group;
import ua.coolboy.f3name.core.F3Name;
import ua.coolboy.f3name.core.LoggerUtil;
import ua.coolboy.f3name.core.PacketSerializer;
import ua.coolboy.f3name.core.hooks.LuckPermsHook;
import ua.coolboy.f3name.core.updater.UpdateCallback;
import ua.coolboy.f3name.core.updater.comparator.VersionComparator;

public class F3NameBungee extends Plugin implements F3Name {

    private BungeeLoggerUtil logger;
    private BungeeConfigParser parser;

    private BungeeMessenger messenger;

    private BungeeMetrics metrics;

    private LuckPermsHook lpHook;

    private static final List<String> HOOKS = new ArrayList<>();

    private List<String> hookedServers;

    private Map<String, BungeeF3Runnable> runnables;
    private Map<ProxiedPlayer, BungeeF3Runnable> players;

    @Override
    public void onEnable() {
        logger = new BungeeLoggerUtil();

        try {
            parser = new BungeeConfigParser(this);
        } catch (IOException ex) {
            logger.error("Failed to load config file!", ex);
            return;
        }

        logger.setColoredConsole(parser.isColoredConsole());

        logger.info("Starting BungeeCord version...");

        messenger = new BungeeMessenger(this);

        new BungeeEventListener(this);

        getProxy().registerChannel(PLUGIN_CHANNEL);

        getProxy().getPluginManager().registerCommand(this, new F3NameCommand(this));

        if (getProxy().getPluginManager().getPlugin("LuckPerms") != null) {
            HOOKS.add("LP");
            lpHook = new LuckPermsHook(parser.getF3GroupList());
            logger.info("Found LuckPerms! Using it for groups.");
        }

        hookedServers = new ArrayList<>();

        runnables = new HashMap<>();
        players = new HashMap<>();
        if (!parser.isOnlyAPI()) {
            startRunnables();
        }

        setupMetrics();

        logger.info("Plugin enabled!");
        checkUpdate();
    }

    private void startRunnables() {
        for (F3Group group : parser.getF3GroupList()) {
            BungeeF3Runnable runnable = new BungeeF3Runnable(this, group);
            getProxy().getScheduler().schedule(this, runnable, 0, group.getUpdateTime() / 20, TimeUnit.SECONDS);
            runnables.put(group.getGroupName(), runnable);
        }

        getProxy().getPlayers().forEach(p -> addPlayer(p));
    }

    public BungeeMessenger getMessenger() {
        return messenger;
    }

    public String getPlayerGroup(ProxiedPlayer player) {
        if (player.getServer() != null && hookedServers.contains(player.getServer().getInfo().getName())) {
            messenger.getPlayerGroup(player, (String group, Throwable error) -> {
                if (group.equals(F3Group.DEFAULT_GROUP)) {
                    group = getPlayerGroupLocally(player);
                }
                addPlayer(player, group);
            });
            return F3Group.DEFAULT_GROUP;
        }

        return getPlayerGroupLocally(player);
    }

    public String getPlayerGroupLocally(ProxiedPlayer player) {
        if (isHooked("LP")) {
            return lpHook.getBestPlayerGroup(player.getUniqueId());
        } else {
            Collection<String> groups = player.getGroups();
            for (F3Group group : parser.getF3GroupList()) {
                if (groups.contains(group.getGroupName())) {
                    return group.getGroupName();
                }
            }
            return F3Group.DEFAULT_GROUP;
        }
    }

    public BungeeF3Runnable addPlayer(ProxiedPlayer player, String group) {
        BungeeF3Runnable current = players.get(player);
        if (current != null) {
            current.removePlayer(player);
        }
        BungeeF3Runnable toAdd = runnables.get(group);
        if (toAdd == null) {
            toAdd = runnables.get(F3Group.DEFAULT_GROUP);
        }
        toAdd.addPlayer(player);
        players.put(player, toAdd);
        return toAdd;
    }

    public BungeeF3Runnable addPlayer(ProxiedPlayer player) {
        return addPlayer(player, getPlayerGroup(player));
    }

    public BungeeF3Runnable removePlayer(ProxiedPlayer player) {
        BungeeF3Runnable current = players.get(player);
        if (current != null) {
            current.removePlayer(player);
            players.remove(player);
        }
        return current;
    }

    public boolean isHooked(String string) {
        return HOOKS.contains(string);
    }

    public Map<String, BungeeF3Runnable> getRunnables() {
        return runnables;
    }

    public List<String> getHookedServers() {
        return ImmutableList.copyOf(hookedServers);
    }

    public void addHookedServer(String name) {
        hookedServers.add(name);
    }

    protected void reload() {
        getProxy().getScheduler().cancel(this);

        runnables.clear();
        players.clear();
        hookedServers.clear();
        checkServers();

        try {
            parser = new BungeeConfigParser(this);
        } catch (IOException ex) {
            logger.error("Failed to load config file!", ex);
            return;
        }

        startRunnables();

        if (isHooked("LP")) {
            lpHook = new LuckPermsHook(parser.getF3GroupList());
        }
    }

    private void checkServers() {
        logger.info("Checking servers for F3Name plugin...");
        for (ServerInfo info : getProxy().getServers().values()) {
            checkServer(info);
        }
    }

    public void checkServer(ServerInfo info) {
        if (!info.getPlayers().isEmpty()) {
            //clear known data about server
            hookedServers.remove(info.getName());
            getConfigParser().removeExcludedServer(info.getName());

            messenger.checkServer(info, (Boolean result, Throwable error) -> {
                if (result) {
                    addHookedServer(info.getName());
                } else {
                    getConfigParser().excludeServer(info.getName());
                }
            });
        }
    }

    private void setupMetrics() {
        metrics = new BungeeMetrics(this);
        metrics.addCustomChart(new BungeeMetrics.SimplePie("bungeecord", () -> "yes"));
        addHookPie("luckperms", getProxy().getPluginManager().getPlugin("LuckPerms"));
    }
    
    private void addHookPie(String charid, Plugin plugin) {
        metrics.addCustomChart(new BungeeMetrics.AdvancedPie(charid, () -> {
            Map<String, Integer> map = new HashMap<>();
            if (plugin != null) {
                map.put("BungeeCord|"+plugin.getDescription().getVersion(), 1);
            } else {
                map.put("Not using", 1);
            }
            return map;
        }));
    }
    
    private void checkUpdate() {
        final SpigetUpdateBungee updater = new SpigetUpdateBungee(this, RESOURCE_ID);
        updater.setVersionComparator(VersionComparator.SEM_VER);
        updater.checkForUpdate(new UpdateCallback() {
            @Override
            public void updateAvailable(String newVersion, String downloadUrl, boolean hasDirectDownload) {
                logger.info("Update available: "+newVersion+"! Download link: "+downloadUrl);
            }

            @Override
            public void upToDate() {
            }
        });
    }
    
    @Override
    public LoggerUtil getLoggerUtil() {
        return logger;
    }

    @Override
    public ServerType getServerType() {
        return ServerType.BUNGEE;
    }

    @Override
    public BungeeConfigParser getConfigParser() {
        return parser;
    }

    public void send(ProxiedPlayer player, String brand) {
        messenger.getMessage(player.getName(), brand, (String message, Throwable error) -> {
            if (brand.equals(message)) {
                message = BungeePlaceholders.setPlaceholders(player, brand);
            }
            player.sendData(BRAND_CHANNEL, new PacketSerializer(message + ChatColor.RESET).toArray());
        });
    }

    public void sendLocally(ProxiedPlayer player, String brand) {
        player.sendData(BRAND_CHANNEL, new PacketSerializer(brand + ChatColor.RESET).toArray());
    }

    @Override
    public void send(UUID uuid, String brand) {
        ProxiedPlayer player = getProxy().getPlayer(uuid);
        if (player == null) {
            throw new IllegalArgumentException("Can't find player with UUID " + uuid);
        }
        send(player, brand);
    }

    @Override
    public F3Name getInstance() {
        return this;
    }

}
