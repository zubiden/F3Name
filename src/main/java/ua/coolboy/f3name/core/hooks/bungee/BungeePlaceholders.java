package ua.coolboy.f3name.core.hooks.bungee;

import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.connection.Server;

//meh... i don't think that this is a hook, but this package is better for it
public class BungeePlaceholders {

    public static String setPlaceholders(ProxiedPlayer player, String string) {
        string = string.replace("%player_name%", catchNull(player.getName()));
        string = string.replace("%player_displayname%", catchNull(player.getDisplayName()));
        string = string.replace("%player_uuid%", catchNull(player.getUniqueId().toString()));

        string = string.replace("%player_ping%", catchNull(Integer.toString(player.getPing())));
        Server server = player.getServer();
        if (server != null) {
            string = string.replace("%server_name%", catchNull(server.getInfo().getName()));
            string = string.replace("%server_motd%", catchNull(server.getInfo().getMotd()));
        }
        return string;
    }

    private static String catchNull(String string) {
        return string == null ? "null" : string;
    }

}
