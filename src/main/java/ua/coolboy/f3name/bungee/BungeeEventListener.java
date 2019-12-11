package ua.coolboy.f3name.bungee;

import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.PlayerDisconnectEvent;
import net.md_5.bungee.api.event.PluginMessageEvent;
import net.md_5.bungee.api.event.ServerConnectedEvent;
import net.md_5.bungee.api.event.ServerSwitchEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;

public class BungeeEventListener implements Listener {

    private F3NameBungee plugin;

    public BungeeEventListener(F3NameBungee plugin) {
        this.plugin = plugin;
        plugin.getProxy().getPluginManager().registerListener(plugin, this);
    }

    @EventHandler
    public void onJoin(ServerConnectedEvent e) {
        ProxiedPlayer player = e.getPlayer();
        if (plugin.getHookedServers().contains(e.getServer().getInfo().getName())) {
            plugin.getPlayerGroup(player);
            return;
        }
        if (!plugin.getConfigParser().getExcludedServers().contains(e.getServer().getInfo().getName())) {
            BungeeF3Runnable runnable = plugin.addPlayer(player);
            if (runnable != null) {
                plugin.send(player, runnable.getCurrentString(), true);
            }
        }
    }

    @EventHandler
    public void onLeave(PlayerDisconnectEvent e) {
        plugin.removePlayer(e.getPlayer());
    }

    public void onSwitch(ServerSwitchEvent e) {
        ProxiedPlayer player = e.getPlayer();
        if (plugin.getHookedServers().contains(player.getServer().getInfo().getName())) {
            plugin.getPlayerGroup(player); //ask method to get player group
        }
    }
    
    //Forward server brand through bungee
    @EventHandler
    public void onPluginMessage(PluginMessageEvent e) {
        if(e.getTag().equals(F3NameBungee.BRAND_CHANNEL) && e.getReceiver() instanceof ProxiedPlayer) {
            if(e.isCancelled()) return;
            ProxiedPlayer player = (ProxiedPlayer) e.getReceiver();
            e.setCancelled(true);
            player.sendData(F3NameBungee.BRAND_CHANNEL, e.getData());
        }
    }

}
