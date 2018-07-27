package ua.coolboy.f3name.bukkit.packet;

import org.bukkit.entity.Player;

public interface IPayloadPacket {

    public void send(Player player, String string);
    public void sendRaw(Player player, String string);
    
    public Object getHandle();
    
    public String getVersion();

}
