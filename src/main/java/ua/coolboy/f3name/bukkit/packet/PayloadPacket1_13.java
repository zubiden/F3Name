package ua.coolboy.f3name.bukkit.packet;

import io.netty.buffer.Unpooled;
import net.minecraft.server.v1_13_R1.PacketDataSerializer;
import net.minecraft.server.v1_13_R1.PacketPlayOutCustomPayload;
import org.apache.commons.lang.Validate;
import org.bukkit.craftbukkit.v1_13_R1.entity.CraftPlayer;
import org.bukkit.entity.Player;
import ua.coolboy.f3name.core.hooks.bukkit.PAPIHook;

public class PayloadPacket1_13 implements IPayloadPacket, Cloneable {
    
    private String version;
    
    protected PayloadPacket1_13(String version) {
        this.version = version;
    }

    @Override
    public void send(Player player, String brand) {
        brand = PAPIHook.getPAPIString(player, brand);
        sendRaw(player, brand);
    }
    
    @Override
    public void sendRaw(Player player, String brand) {
        Validate.notNull(player, "Player is null!");
        Validate.notNull(brand, "Server brand is null!");
        CraftPlayer cp = (CraftPlayer) player;
        cp.getHandle().playerConnection.sendPacket(new PacketPlayOutCustomPayload(
                PacketPlayOutCustomPayload.b, // minecraft:brand
                new PacketDataSerializer(Unpooled.buffer()).a(brand)
        ));
    }
    
    @Override
    public Object getHandle() {
        return new PacketPlayOutCustomPayload(
                PacketPlayOutCustomPayload.b, //brand channel
                new PacketDataSerializer(Unpooled.buffer()).a(""));
    }
    
    @Override
    public String getVersion() {
        return version;
    }

}
