package ua.coolboy.f3name.bukkit.packet;

public class VersionHandler {

    private IPayloadPacket packet;
    private String version;

    public VersionHandler(String version) {
        switch (version) {
            case "v1_13_R1":
                packet =  new PayloadPacket1_13(version);
                break;
            default:
                //TODO if version > 1.13, try with reflection
                throw new IllegalStateException("[F3Name] Unsupported version: "+version+"!");
        }

    }

    
    public IPayloadPacket getPacket() {
        return packet;
    }

}
