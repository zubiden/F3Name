package ua.coolboy.f3name.bukkit.packet;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Set;
import org.apache.commons.lang.Validate;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.messaging.Messenger;
import ua.coolboy.f3name.bukkit.F3NameBukkit;
import ua.coolboy.f3name.core.PacketSerializer;
import ua.coolboy.f3name.bukkit.hooks.PAPIHook;

public class ReflectionPayloadPacket {

    private F3NameBukkit plugin;

    public ReflectionPayloadPacket(F3NameBukkit plugin) {
        this.plugin = plugin;
        
        Messenger messenger = Bukkit.getMessenger();
        try {
            Method method = messenger.getClass().getDeclaredMethod("addToOutgoing", Plugin.class, String.class);
            method.setAccessible(true);
            method.invoke(messenger, plugin, F3NameBukkit.BRAND_CHANNEL);
        } catch (Exception ex) {
            plugin.getLoggerUtil().error("Failed to register channel!", ex);
        }
    }

    public void send(Player player, String string) {
        sendRaw(player, PAPIHook.getPAPIString(player, string));
    }

    public void sendRaw(Player player, String brand) {
        Validate.notNull(player, "Player is null!");
        Validate.notNull(brand, "Server brand is null!");
        
        checkPlayerChannels(player);

        player.sendPluginMessage(plugin, F3NameBukkit.BRAND_CHANNEL, new PacketSerializer(brand).toArray());
    }

    public Object getHandle() {
        throw new UnsupportedOperationException("Not implemented in ReflectionPayloadPacket!");
    }

    //Less efficient than direct usе of NMS
    private void checkPlayerChannels(Player player) {
        try {
            Field playerChannels = player.getClass().getDeclaredField("channels");
            playerChannels.setAccessible(true);
            Set<String> channels = (Set<String>) playerChannels.get(player);
            if(!channels.contains(F3NameBukkit.BRAND_CHANNEL)) {
                channels.add(F3NameBukkit.BRAND_CHANNEL);
            }
        } catch (Exception ex) {
            plugin.getLoggerUtil().error("Failed to add channel to player!",ex);
        }
    }

}