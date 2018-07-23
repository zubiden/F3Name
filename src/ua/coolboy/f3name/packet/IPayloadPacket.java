package ua.coolboy.f3name.packet;

import org.bukkit.entity.Player;

public interface IPayloadPacket {

    public void send(Player player, String string);
    public void sendRaw(Player player, String string);
    
    public Object getHandle();

}
