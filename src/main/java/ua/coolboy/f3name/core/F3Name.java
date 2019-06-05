package ua.coolboy.f3name.core;

import java.util.Collection;
import java.util.UUID;

public interface F3Name {

    /**
     * Gets plugin {@link LoggerUtil}
     * @return LoggerUtil for logging on behalf of the plugin
     */
    public LoggerUtil getLoggerUtil();

    /**
     * Gets {@link ServerType}
     * @return ServerType to understand on what platform plugin is loaded
     */
    public ServerType getServerType();
    
    /**
     * Gets plugin {@link ConfigParser}
     * @return ConfigParser that used to parse config
     */
    public ConfigParser getConfigParser();
    
    /**
     * Gets {@link F3Runnable} list
     * @return List of F3Runnable, each runnable serves one group
     */
    public Collection<? extends F3Runnable> getRunnables();
    
    /**
     * Replaces placeholders and sends server brand to player
     * @param uuid player UUID
     * @param brand string to send
     */
    public void send(UUID uuid, String brand);
    
    /**
     * Sends server brand to player without editing text
     * @param uuid player UUID
     * @param brand string to send
     */
    public void sendRaw(UUID uuid, String brand);
    
    /**
     * Logger prefix
     */
    public static final String PREFIX = "§3[F3Name] §r";
    /**
     * Resource ID for updationg
     */
    public static final int RESOURCE_ID = 58997;
    
    /**
     * Minecraft brand channel
     */
    public static final String BRAND_CHANNEL = "minecraft:brand";
    
    /**
     * Plugin channel
     */
    public static final String PLUGIN_CHANNEL = "bukkit:f3name";

    public enum ServerType {
        /**
         * Represents Bukkit server
         */
        BUKKIT,
        /**
         * Represents BungeeCord server
         */
        BUNGEE;
    }
}
