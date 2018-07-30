package ua.coolboy.f3name.api;

import ua.coolboy.f3name.core.F3Name;

public class F3NameAPI {
    
    private static F3Name plugin;

    public F3NameAPI(F3Name plugin) {
        this.plugin = plugin;
    }
    
    public static F3Name getInstance() {
        return plugin;
    }

}
