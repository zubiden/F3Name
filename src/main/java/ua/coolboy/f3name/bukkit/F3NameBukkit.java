package ua.coolboy.f3name.bukkit;

import ua.coolboy.f3name.metrics.BukkitMetrics;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import ua.coolboy.f3name.core.F3Group;
import ua.coolboy.f3name.core.F3Name;
import ua.coolboy.f3name.core.LoggerUtil;
import ua.coolboy.f3name.core.hooks.LuckPermsHook;
import ua.coolboy.f3name.core.hooks.bukkit.PAPIHook;
import ua.coolboy.f3name.core.hooks.bukkit.VaultHook;
import ua.coolboy.f3name.bukkit.packet.IPayloadPacket;
import ua.coolboy.f3name.bukkit.packet.VersionHandler;
import ua.coolboy.f3name.core.updater.UpdateCallback;
import ua.coolboy.f3name.core.updater.comparator.VersionComparator;

public class F3NameBukkit extends JavaPlugin implements Listener, F3Name {

    private static F3NameBukkit plugin;

    private HashMap<String, BukkitF3Runnable> runnables;
    private HashMap<Player, BukkitF3Runnable> players;

    private VersionHandler handler;

    private static BukkitConfigParser parser;

    private BukkitMetrics metrics;

    private LuckPermsHook lpHook;
    private VaultHook vaultHook;
    private PAPIHook papiHook;

    private boolean bungeePlugin;

    private F3MessageListener messageListener;

    private static final List<String> HOOKS = new ArrayList<>();

    private LoggerUtil logger;

    @Override
    public void onEnable() {
        F3NameBukkit.plugin = this;
        logger = new BukkitLoggerUtil();
        try {
            this.handler = new VersionHandler(Bukkit.getServer().getClass().getPackage().getName().split("\\.")[3]);
        } catch (IllegalStateException ex) {
            Bukkit.getLogger().severe("Stopping plugin!");
            Bukkit.getPluginManager().disablePlugin(plugin);
            return;
        }

        File file = new File(getDataFolder(), "config.yml");
        if (!file.exists()) {
            saveDefaultConfig();
        }

        parser = new BukkitConfigParser((YamlConfiguration) getConfig()); //Bukkit provides us a YamlConfiguration, safe

        logger.setColoredConsole(parser.isColoredConsole());

        logger.info("Starting Bukkit version...");

        messageListener = new F3MessageListener(plugin);

        bungeePlugin = false;
        check();

        runnables = new HashMap<>();
        players = new HashMap<>();

        if (Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI")) {
            HOOKS.add("PAPI");
            papiHook = new PAPIHook(Bukkit.getPluginManager().getPlugin("PlaceholderAPI"));
            logger.info("Found PlaceholderAPI! Using it for placeholders.");
        }

        if (Bukkit.getPluginManager().isPluginEnabled("LuckPerms")) {
            HOOKS.add("LP");
            lpHook = new LuckPermsHook(parser.getF3GroupList());
            logger.info("Found LuckPerms! Using it for groups.");
        } else if (getServer().getPluginManager().isPluginEnabled("Vault")) {
            HOOKS.add("Vault");
            vaultHook = new VaultHook(parser.getF3GroupList());
            logger.info("Found Vault! Using it for groups.");
        }

        if (!parser.isBungeeCord()) {
            startRunnables();
        }
        Bukkit.getPluginManager().registerEvents(this, this);
        setupMetrics();
        logger.info("Plugin enabled!");

        checkUpdate();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (sender.hasPermission("f3name.reload") && args.length == 1 && args[0].equals("reload")) {
            for (BukkitF3Runnable runnable : runnables.values()) {
                runnable.cancel();
            }

            reload();

            sender.sendMessage(PREFIX + ChatColor.GOLD + "Reloaded configuration!");
        } else {
            sender.sendMessage(PREFIX + ChatColor.GOLD + "v" + getDescription().getVersion() + " by Cool_boy (aka prettydude)");
        }
        return true;
    }

    private void check() {
        if (Bukkit.getOnlinePlayers().size() > 0) {
            ByteArrayDataOutput out = ByteStreams.newDataOutput();
            out.writeUTF("check");
            out.writeBoolean(parser.isBungeeCord());
            Bukkit.getOnlinePlayers().iterator().next().sendPluginMessage(plugin, PLUGIN_CHANNEL, out.toByteArray());
        }
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent e) {
        BukkitF3Runnable runnable = addPlayer(e.getPlayer());
        if (runnable != null) {
            send(e.getPlayer().getUniqueId(), runnable.getCurrentString());
        }
        //check bungeecord when player connects to empty server
        if (Bukkit.getOnlinePlayers().size() == 1) {
            new BukkitRunnable() {
                @Override
                public void run() {
                    check();
                }
            }.runTaskLater(plugin, 20);
        }
    }

    public boolean hasBungeePlugin() {
        return bungeePlugin;
    }

    protected void setBungeePlugin() {
        bungeePlugin = true;
    }

    @EventHandler
    public void onLeave(PlayerQuitEvent e) {
        removePlayer(e.getPlayer());
    }

    public HashMap<String, BukkitF3Runnable> getRunnables() {
        return runnables;
    }

    public String getPlayerGroup(Player player) {
        if (isHooked("LP")) {
            return lpHook.getBestPlayerGroup(player.getUniqueId());
        } else if (isHooked("Vault")) {
            return vaultHook.getBestPlayerGroup(player);
        } else {
            return F3Group.DEFAULT_GROUP;
        }
    }

    public static boolean isHooked(String name) {
        return HOOKS.contains(name);
    }

    private void reload() {
        File file = new File(getDataFolder(), "config.yml");
        if (!file.exists()) {
            saveDefaultConfig();
        }

        reloadConfig();
        parser = new BukkitConfigParser(getConfig());

        check();

        startRunnables();

        if (isHooked("LP")) {
            lpHook = new LuckPermsHook(parser.getF3GroupList());
        } else if (isHooked("Vault")) {
            vaultHook = new VaultHook(parser.getF3GroupList());
        }
    }

    public BukkitF3Runnable addPlayer(Player player) {
        BukkitF3Runnable current = players.get(player);
        if (current != null) {
            current.removePlayer(player);
        }
        BukkitF3Runnable toAdd = runnables.get(getPlayerGroup(player));
        if (toAdd != null) {
            toAdd.addPlayer(player);
            players.put(player, toAdd);
        }
        return toAdd;
    }

    public BukkitF3Runnable removePlayer(Player player) {
        BukkitF3Runnable current = players.get(player);
        if (current != null) {
            current.removePlayer(player);
            players.remove(player);
        }
        return current;
    }

    private void setupMetrics() {
        metrics = new BukkitMetrics(plugin);
        addHookPie("placeholderapi", Bukkit.getPluginManager().getPlugin("PlaceholderAPI"));
        addHookPie("luckperms", Bukkit.getPluginManager().getPlugin("LuckPerms"));
        addHookPie("vault", Bukkit.getPluginManager().getPlugin("Vault"));
        metrics.addCustomChart(new BukkitMetrics.SimplePie("bungeecord", () -> "no"));
    }

    private void addHookPie(String charid, Plugin plugin) {
        metrics.addCustomChart(new BukkitMetrics.AdvancedPie(charid, () -> {
            Map<String, Integer> map = new HashMap<>();
            if (plugin != null) {
                map.put(plugin.getDescription().getVersion(), 1);
            } else {
                map.put("Not using", 1);
            }
            return map;
        }));
    }

    private void checkUpdate() {
        final SpigetUpdateBukkit updater = new SpigetUpdateBukkit(this, RESOURCE_ID);
        updater.setVersionComparator(VersionComparator.SEM_VER);
        updater.checkForUpdate(new UpdateCallback() {
            @Override
            public void updateAvailable(String newVersion, String downloadUrl, boolean hasDirectDownload) {
                if (hasDirectDownload) {
                    if (parser.isAutoUpdate() && updater.downloadUpdate()) {
                        logger.info("Downloaded update! It will be applied after restart");
                    } else {
                        logger.error("Update download failed, reason is " + updater.getFailReason());
                    }
                }
            }

            @Override
            public void upToDate() {
            }
        });
    }

    private void startRunnables() {
        runnables.clear();
        players.clear();
        for (F3Group group : parser.getF3GroupList()) {
            BukkitF3Runnable runnable = new BukkitF3Runnable(plugin, group);
            runnable.runTaskTimer(plugin, 0, group.getUpdateTime());
            runnables.put(group.getGroupName(), runnable);
        }

        Bukkit.getOnlinePlayers().forEach(p -> addPlayer(p));
    }

    @Override
    public LoggerUtil getLoggerUtil() {
        return logger;
    }

    @Override
    public F3Name getInstance() {
        return this;
    }

    @Override
    public ServerType getServerType() {
        return ServerType.BUKKIT;
    }

    public IPayloadPacket getPacket() {
        return handler.getPacket();
    }

    public void send(Player player, String brand) {
        /*f (!hasBungeePlugin()) {
            handler.getPacket().send(player, brand);
        } else {*/
        ByteArrayDataOutput out = ByteStreams.newDataOutput();
        out.writeUTF("message");
        out.writeUTF(PAPIHook.getPAPIString(player, brand)); //set placeholders
        player.sendPluginMessage(plugin, PLUGIN_CHANNEL, out.toByteArray());
        //}
    }

    @Override
    public void send(UUID uuid, String brand) {
        send(Bukkit.getPlayer(uuid), brand);
    }

    @Override
    public BukkitConfigParser getConfigParser() {
        return parser;
    }

}
