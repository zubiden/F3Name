package ua.coolboy.f3name;

import java.util.HashMap;
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
import org.bukkit.plugin.java.JavaPlugin;
import ua.coolboy.f3name.hooks.LuckPermsHook;
import ua.coolboy.f3name.packet.IPayloadPacket;

import ua.coolboy.f3name.packet.VersionHandler;

public class F3Name extends JavaPlugin implements Listener {

    private static F3Name plugin;

    private HashMap<String, F3NameRunnable> runnables;
    private HashMap<Player, F3NameRunnable> players;
    private VersionHandler handler;
    private String prefix;
    private ConfigParser parser;
    private LuckPermsHook lpHook;

    private static boolean papi = false;
    private static boolean lp = false;

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
            papi = true;
            info("Found PlaceholderAPI! Using it for placeholders.");
        }

        if (Bukkit.getPluginManager().isPluginEnabled("LuckPerms")) {
            lp = true;
            lpHook = new LuckPermsHook(parser.getGDSList());
            info("Found LuckPerms! Using it for groups.");
        }

        startRunnables();

        Bukkit.getPluginManager().registerEvents(this, this);
        Metrics metrics = new Metrics(plugin);
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

            if (lp) {
                lpHook = new LuckPermsHook(parser.getGDSList());
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
        if (papi) {
            return me.clip.placeholderapi.PlaceholderAPI.setPlaceholders(player, string);
        }
        return string;
    }

    public String getPlayerGroup(Player player) {
        if (lp) {
            return lpHook.getBestPlayerGroup(player.getUniqueId());
        } else {
            return "everyone";
        }
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
