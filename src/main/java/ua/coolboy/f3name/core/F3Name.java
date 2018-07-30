package ua.coolboy.f3name.core;

import java.util.UUID;

public interface F3Name {

    public LoggerUtil getLoggerUtil();

    public ServerType getServerType();

    public F3Name getInstance();
    
    public ConfigParser getConfigParser();
    
    public void send(UUID uuid, String brand);
    
    public static final String PREFIX = "§3[F3Name] §r";
    public static final int RESOURCE_ID = 58997;
    
    public static final String BRAND_CHANNEL = "minecraft:brand";
    public static final String PLUGIN_CHANNEL = "bukkit:f3name";

    public enum ServerType {
        BUKKIT, BUNGEE;
    }
}
