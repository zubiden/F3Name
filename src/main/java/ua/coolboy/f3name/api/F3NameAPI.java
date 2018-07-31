package ua.coolboy.f3name.api;

import ua.coolboy.f3name.core.F3Name;

/**
 * API for F3Name
 * <br>
 * Use {@link #getInstance()}
 * @author Cool_boy
 */
public class F3NameAPI {
    
    private static F3Name plugin;

    public F3NameAPI(F3Name plugin) {
        if(plugin != null) throw new IllegalAccessError("Plugin already set!");
        this.plugin = plugin;
    }
    
    /**
     * Gets plugin API
     * @return Instance of plugin main class
     */
    public static F3Name getInstance() {
        return plugin;
    }

}
