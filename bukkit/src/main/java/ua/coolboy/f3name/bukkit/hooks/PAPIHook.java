package ua.coolboy.f3name.bukkit.hooks;

import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

public class PAPIHook {
    private static boolean isHooked;
    
    public PAPIHook(Plugin papi) {
        isHooked = papi != null;
    }

    public static String getPAPIString(Player player, String string) {
        if(!isHooked) return string;
        return me.clip.placeholderapi.PlaceholderAPI.setPlaceholders((OfflinePlayer) player, string);
    }

}
