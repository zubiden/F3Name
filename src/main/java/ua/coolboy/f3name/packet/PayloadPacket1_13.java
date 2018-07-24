package ua.coolboy.f3name.packet;

import io.netty.buffer.Unpooled;
import net.minecraft.server.v1_13_R1.PacketDataSerializer;
import net.minecraft.server.v1_13_R1.PacketPlayOutCustomPayload;
import org.bukkit.craftbukkit.v1_13_R1.entity.CraftPlayer;
import org.bukkit.entity.Player;
import ua.coolboy.f3name.hooks.PAPIHook;

public class PayloadPacket1_13 implements IPayloadPacket, Cloneable {
    
    private String version;
    
    protected PayloadPacket1_13(String version) {
        this.version = version;
    }

    @Override
    public void send(Player player, String string) {
        string = PAPIHook.getPAPIString(player, string);
        CraftPlayer cp = (CraftPlayer) player;
        cp.getHandle().playerConnection.sendPacket(new PacketPlayOutCustomPayload(
                PacketPlayOutCustomPayload.b, // minecraft:brand
                new PacketDataSerializer(Unpooled.buffer()).a(string)
        ));
    }
    
    @Override
    public void sendRaw(Player player, String string) {
        CraftPlayer cp = (CraftPlayer) player;
        cp.getHandle().playerConnection.sendPacket(new PacketPlayOutCustomPayload(
                PacketPlayOutCustomPayload.b, // minecraft:brand
                new PacketDataSerializer(Unpooled.buffer()).a(string)
        ));
    }
    
    @Override
    public Object getHandle() {
        return new PacketPlayOutCustomPayload(
                PacketPlayOutCustomPayload.b, //brand channel
                new PacketDataSerializer(Unpooled.buffer()).a(""));
    }
    
    public String getVersion() {
        return version;
    }

}
