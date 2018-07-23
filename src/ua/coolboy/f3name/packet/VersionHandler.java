package ua.coolboy.f3name.packet;

import org.bukkit.Bukkit;

public class VersionHandler {

    private IPayloadPacket packet;
    private String version;

    public VersionHandler() {
        version = Bukkit.getServer().getClass().getPackage().getName().split("\\.")[3];
        switch (version) {
            case "v1_13_R1":
                packet =  new PayloadPacket1_13();
                break;
            default:
                throw new IllegalStateException("[F3Name] Unsupported version: "+version+"!");
        }

    }

    
    public IPayloadPacket getPacket() {
        return packet;
    }

}
