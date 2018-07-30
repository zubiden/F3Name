package ua.coolboy.f3name.bungee;

import com.google.common.collect.ImmutableList;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.connection.Server;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.api.scheduler.ScheduledTask;
import net.md_5.bungee.protocol.ProtocolConstants;

import ua.coolboy.f3name.core.F3Group;
import ua.coolboy.f3name.core.F3Runnable;

public class BungeeF3Runnable implements Runnable, F3Runnable {

    private F3Group group;
    private List<String> names;
    private int current;

    private static final Random random = new Random();

    private List<ProxiedPlayer> players;
    private ProxyServer server;

    private F3NameBungee plugin;
    private ScheduledTask task;

    public BungeeF3Runnable(F3NameBungee plugin, F3Group group) {
        this.plugin = plugin;
        this.group = group;
        this.players = new ArrayList<>();
        server = ProxyServer.getInstance();

        if (group.getNamesList() == null || group.getNamesList().isEmpty()) {
            throw new IllegalArgumentException("List must contain at least one string!");
        }

        this.names = new ArrayList<>();

        for (String string : group.getNamesList()) {
            this.names.add(ChatColor.translateAlternateColorCodes('&', string) + ChatColor.RESET);
        }

        current = -1;
    }

    @Override
    public void run() {
        if (group.isShuffle()) {
            current = random.nextInt(names.size());
        } else {
            current++;
        }
        if (names.size() <= current) {
            current = 0;
        }
        for (ProxiedPlayer player : players) {
            if (!player.isConnected()) {
                //players.remove(player);
                continue;
            }

            if (player.getPendingConnection().getVersion() < ProtocolConstants.MINECRAFT_1_13
                    || isExcludedServer(player.getServer())) {
                continue;
            }

            if (isHookedServer(player.getServer())) {
                plugin.getMessenger().getMessage(player.getName(), names.get(current), (String msg, Throwable ex) -> {
                    plugin.send(player, msg);
                });
            }

            plugin.send(player, names.get(current));
        }
    }

    private boolean isExcludedServer(Server server) {
        if (server == null) {
            return true;
        }
        return plugin.getConfigParser().getExcludedServers().contains(server.getInfo().getName());
    }

    private boolean isHookedServer(Server server) {
        if (server == null) {
            return false;
        }
        return plugin.getHookedServers().contains(server.getInfo().getName());
    }

    @Override
    public F3Group getGroup() {
        return group;
    }

    public List<ProxiedPlayer> getPlayers() {
        return ImmutableList.copyOf(players); //clone list
    }

    public void addPlayer(ProxiedPlayer player) {
        players.add(player);
    }

    public void removePlayer(ProxiedPlayer player) {
        players.remove(player);
    }

    @Override
    public String getCurrentString() {
        return current < 0 ? names.get(0) : names.get(current);
    }

    @Override
    public List<String> getStrings() {
        return names;
    }

    public ScheduledTask runTaskTimer(Plugin plugin, int delay, int period) {
        return task = plugin.getProxy().getScheduler().schedule(plugin, this, delay / 20, period / 20, TimeUnit.SECONDS);
    }
    
    public ScheduledTask getTask() {
        return task;
    }

}
