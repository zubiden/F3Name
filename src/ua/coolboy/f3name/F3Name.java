package ua.coolboy.f3name;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import ua.coolboy.f3name.bstats.Metrics;

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
import ua.coolboy.f3name.hooks.LuckPermsHook;
import ua.coolboy.f3name.hooks.VaultHook;
import ua.coolboy.f3name.packet.IPayloadPacket;

import ua.coolboy.f3name.packet.VersionHandler;

public class F3Name extends JavaPlugin implements Listener {

    private static F3Name plugin;

    private HashMap<String, F3NameRunnable> runnables;
    private HashMap<Player, F3NameRunnable> players;
    private VersionHandler handler;
    private String prefix;
    private ConfigParser parser;

    private Metrics metrics;

    private LuckPermsHook lpHook;
    private VaultHook vaultHook;

    private static final List<String> hooks = new ArrayList<>();

    @Override
    public void onEnable() {
        this.plugin = this;
        prefix = ChatColor.DARK_AQUA + "[F3Name] " + ChatColor.RESET;

        try {
            this.handler = new VersionHandler();
        } catch (IllegalStateException ex) {
            info(ChatColor.DARK_RED + "Stopping plugin!");
            Bukkit.getPluginManager().disablePlugin(plugin);
            return;
        }
        parser = new ConfigParser(plugin);

        runnables = new HashMap<>();
        players = new HashMap<>();

        if (Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI")) {
            hooks.add("PAPI");
            info("Found PlaceholderAPI! Using it for placeholders.");
        }

        if (Bukkit.getPluginManager().isPluginEnabled("LuckPerms")) {
            hooks.add("LP");
            lpHook = new LuckPermsHook(parser.getGDSList());
            info("Found LuckPerms! Using it for groups.");
        } else if (getServer().getPluginManager().isPluginEnabled("Vault")) {
            hooks.add("Vault");
            vaultHook = new VaultHook(parser.getGDSList());
            info("Found Vault! Using it for groups.");
        }

        startRunnables();

        Bukkit.getPluginManager().registerEvents(this, this);
        setupMetrics();
        info("Plugin enabled!");
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (sender.hasPermission("f3name.reload") && args.length == 1 && args[0].equals("reload")) {
            for (F3NameRunnable runnable : runnables.values()) {
                runnable.cancel();
            }
            runnables.clear();

            reloadConfig();
            parser = new ConfigParser(plugin);

            startRunnables();

            if (isHooked("LP")) {
                lpHook = new LuckPermsHook(parser.getGDSList());
            } else if (isHooked("Vault")) {
                vaultHook = new VaultHook(parser.getGDSList());
            }
            sender.sendMessage(prefix + ChatColor.GOLD + "Reloaded configuration!");
        } else {
            sender.sendMessage(prefix + ChatColor.GOLD + "v" + getDescription().getVersion() + " by Cool_boy (aka prettydude)");
        }
        return true;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent e) {
        F3NameRunnable runnable = addPlayer(e.getPlayer());
        IPayloadPacket packet = handler.getPacket();
        Player player = e.getPlayer();
        packet.send(player, runnable.getCurrentString());
    }

    @EventHandler
    public void onLeave(PlayerQuitEvent e) {
        removePlayer(e.getPlayer());
    }

    public HashMap<String, F3NameRunnable> getRunnables() {
        return runnables;
    }

    public static String getPAPIString(Player player, String string) {
        if (isHooked("PAPI")) {
            return me.clip.placeholderapi.PlaceholderAPI.setPlaceholders(player, string);
        }
        return string;
    }

    public String getPlayerGroup(Player player) {
        if (isHooked("LP")) {
            return lpHook.getBestPlayerGroup(player.getUniqueId());
        } else if (isHooked("Vault")) {
            return vaultHook.getBestPlayerGroup(player);
        } else {

            return GroupDS.DEFAULT_GROUP;
        }
    }

    public static boolean isHooked(String name) {
        return hooks.contains(name);
    }

    public F3NameRunnable addPlayer(Player player) {
        F3NameRunnable current = players.get(player);
        if (current != null) {
            current.removePlayer(player);
        }
        F3NameRunnable toAdd = runnables.get(getPlayerGroup(player));
        toAdd.addPlayer(player);
        players.put(player, toAdd);
        return toAdd;
    }

    public F3NameRunnable removePlayer(Player player) {
        F3NameRunnable current = players.get(player);
        if (current != null) {
            current.removePlayer(player);
            players.remove(player);
        }
        return current;
    }

    public void info(String string) {
        if (parser.isColoredConsole()) {
            Bukkit.getConsoleSender().sendMessage(prefix + ChatColor.GOLD + string);
        } else {
            Bukkit.getLogger().info(ChatColor.stripColor(prefix + string));
        }
    }

    private void setupMetrics() {
        metrics = new Metrics(plugin);
        addHookPie("placeholderapi", Bukkit.getPluginManager().getPlugin("PlaceholderAPI"));
        addHookPie("luckperms", Bukkit.getPluginManager().getPlugin("LuckPerms"));
        addHookPie("vault", Bukkit.getPluginManager().getPlugin("Vault"));
    }

    private void addHookPie(String charid, Plugin plugin) {
        metrics.addCustomChart(new Metrics.AdvancedPie(charid, () -> {
            Map<String, Integer> map = new HashMap<>();
            if (plugin != null) {
                map.put(plugin.getDescription().getVersion(), 1);
            } else {
                map.put("Not using", 1);
            }
            return map;
        }));
    }

    private void startRunnables() {
        for (GroupDS group : parser.getGDSList()) {
            F3NameRunnable runnable = new F3NameRunnable(handler, group);
            runnable.runTaskTimer(plugin, 0, group.getUpdateTime());
            runnables.put(group.getGroupName(), runnable);
        }

        Bukkit.getOnlinePlayers().forEach(p -> addPlayer(p));
    }

    public static F3Name getInstance() {
        return plugin;
    }

}
