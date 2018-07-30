package ua.coolboy.f3name.bukkit.packet;

import ua.coolboy.f3name.bukkit.F3NameBukkit;

public class VersionHandler {

    private IPayloadPacket packet;
    private String version;

    public VersionHandler(String version, F3NameBukkit plugin) {
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
