package ua.coolboy.f3name.bukkit;

import ua.coolboy.f3name.spiget.SpigetUpdateBukkit;
import ua.coolboy.f3name.metrics.BukkitMetrics;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import java.io.File;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import ua.coolboy.f3name.api.F3NameAPI;
import ua.coolboy.f3name.core.F3Group;
import ua.coolboy.f3name.core.F3Name;
import ua.coolboy.f3name.core.LoggerUtil;
import ua.coolboy.f3name.bukkit.hooks.PAPIHook;
import ua.coolboy.f3name.bukkit.hooks.VaultHook;
import ua.coolboy.f3name.bukkit.packet.ReflectionPayloadPacket;
import ua.coolboy.f3name.core.F3Runnable;
import ua.coolboy.f3name.spiget.updater.UpdateCallback;
import ua.coolboy.f3name.spiget.updater.comparator.VersionComparator;
import ua.coolboy.f3name.core.hooks.ILuckPermsHook;
import ua.coolboy.f3name.core.hooks.LuckPermsHook;

public class F3NameBukkit extends JavaPlugin implements Listener, F3Name {

    private static F3NameBukkit plugin;

    private HashMap<String, BukkitF3Runnable> runnables;
    private HashMap<Player, BukkitF3Runnable> players;

    private String serverVersion = Bukkit.getServer().getClass().getPackage().getName().split("\\.")[3];

    private static BukkitConfigParser parser;

    private BukkitMetrics metrics;

    private ILuckPermsHook lpHook;
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

        new F3NameAPI(this);

        File file = new File(getDataFolder(), "config.yml");
        if (!file.exists()) {
            saveDefaultConfig();
        }

        parser = new BukkitConfigParser(getConfig());
        logger.setColoredConsole(parser.isColoredConsole());

        logger.info("Starting Bukkit(" + serverVersion + ") version...");

        messageListener = new F3MessageListener(plugin);

        bungeePlugin = false;
        check();

        //TODO rewrite to use direct plugin messages on 1.13.2+
        /*if (serverVersion.equals("v1_13_R1")) {
           logger.error("Update to 1.13.2!");
           Bukkit.getPluginManager().disablePlugin(this);
           return;
        }*/
 /*try {
                this.getServer().getMessenger().registerOutgoingPluginChannel(this, BRAND_CHANNEL);
            } catch (Exception ex) {
                logger.error("Couldn't initialize messaging channel! Plugin is not working on versions lower than 1.13!");
            }*/
        runnables = new HashMap<>();
        players = new HashMap<>();

        searchHooks();

        if (!parser.isBungeeCord()) {
            startRunnables();
        }

        Bukkit.getPluginManager().registerEvents(this, this);

        setupMetrics();
        logger.info("Plugin enabled!");

        if(parser.checkForUpdates()) {
            checkUpdate();
        }
    }

    @Override
    public void onDisable() {
        //Hacky way to send PluginMessage onDisable
        try {
            Field field = this.getClass().getField("isEnabled");
            field.setAccessible(true);
            field.set(this, true);

            if (Bukkit.getOnlinePlayers().isEmpty()) {
                return;
            }
            ByteArrayDataOutput out = ByteStreams.newDataOutput();
            out.writeUTF("off");
            Bukkit.getOnlinePlayers().iterator().next().sendPluginMessage(plugin, PLUGIN_CHANNEL, out.toByteArray());

            field.set(this, false);
        } catch (Exception ex) {
            //Seems broken, let's just silent it :D
            //logger.error("Failed to notify BungeeCord!", ex);
        }

    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (sender.hasPermission("f3name.reload") && args.length == 1 && args[0].equals("reload")) {
            runnables.values().stream().forEach(BukkitF3Runnable::cancel);

            reload();

            sender.sendMessage(PREFIX + ChatColor.GOLD + "Reloaded configuration!");
        } else {
            sender.sendMessage(PREFIX + ChatColor.GOLD + "v" + getDescription().getVersion());
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

    @Override
    public Collection<? extends F3Runnable> getRunnables() {
        return runnables.values();
    }

    public Map<String, BukkitF3Runnable> getRunnablesMap() {
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

        Bukkit.getMessenger().unregisterIncomingPluginChannel(this);
        messageListener = new F3MessageListener(plugin);

        bungeePlugin = false;
        check();

        searchHooks();
        //stopping in case if we are missing something
        for (BukkitF3Runnable runnable : runnables.values()) {
            if (runnable == null) {
                continue;
            }
            runnable.cancel();
        }

        if (!parser.isBungeeCord()) {
            startRunnables();
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
        metrics = new BukkitMetrics(plugin, 2920);
        addHookPie("placeholderapi", Bukkit.getPluginManager().getPlugin("PlaceholderAPI"));
        addHookPie("luckperms", Bukkit.getPluginManager().getPlugin("LuckPerms"));
        addHookPie("vault", Bukkit.getPluginManager().getPlugin("Vault"));
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
            //idk is it safe, let's test
            runnable.runTaskTimerAsynchronously(plugin, 0, group.getUpdateTime());
            runnables.put(group.getGroupName(), runnable);
        }

        Bukkit.getOnlinePlayers().forEach(p -> addPlayer(p));
    }

    private void searchHooks() {
        HOOKS.clear();
        if (Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI")) {
            HOOKS.add("PAPI");
            papiHook = new PAPIHook(Bukkit.getPluginManager().getPlugin("PlaceholderAPI"));
            logger.info("Found PlaceholderAPI! Using it for placeholders.");
        }

        if (Bukkit.getPluginManager().isPluginEnabled("LuckPerms")) {
            HOOKS.add("LP");
            lpHook = LuckPermsHook.get(parser.getF3GroupList());
            if (lpHook != null) {
                logger.info("Found LuckPerms! Using it for groups.");
            } else {
                logger.error("Problem with obtaining LuckPerms instance!");
                HOOKS.remove("LP");
            }
        } else if (getServer().getPluginManager().isPluginEnabled("Vault")) {
            HOOKS.add("Vault");
            vaultHook = new VaultHook(parser.getF3GroupList());
            logger.info("Found Vault! Using it for groups.");
        }
    }

    @Override
    public LoggerUtil getLoggerUtil() {
        return logger;
    }

    public static F3NameBukkit getInstance() {
        return plugin;
    }

    @Override
    public ServerType getServerType() {
        return ServerType.BUKKIT;
    }

    public void send(Player player, String brand) {
        sendRaw(player, PAPIHook.getPAPIString(player, brand));
    }

    public void sendRaw(Player player, String brand) {
        if (player == null) {
            return;
        }
        //player.sendPluginMessage(plugin, BRAND_CHANNEL, new PacketSerializer(brand).toArray());
        new ReflectionPayloadPacket(plugin).send(player, brand);
    }

    @Override
    public void send(UUID uuid, String brand) {
        Player player = Bukkit.getPlayer(uuid);
        send(player, brand);
    }

    @Override
    public void sendRaw(UUID uuid, String brand) {
        sendRaw(Bukkit.getPlayer(uuid), brand);
    }

    @Override
    public BukkitConfigParser getConfigParser() {
        return parser;
    }

}
