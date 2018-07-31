package ua.coolboy.f3name.bukkit.packet;

import org.bukkit.Bukkit;
import ua.coolboy.f3name.bukkit.F3NameBukkit;

public class VersionHandler {

    private IPayloadPacket packet;
    private String version;

    public VersionHandler(F3NameBukkit plugin) {
        version = Bukkit.getServer().getClass().getPackage().getName().split("\\.")[3];
        switch (version) {
            case "v1_13_R1":
                packet =  new PayloadPacket1_13(version);
                break;
            default:
                packet = new ReflectionPayloadPacket(version, plugin);
        }

    }

    
    public IPayloadPacket getPacket() {
        return packet;
    }

}
