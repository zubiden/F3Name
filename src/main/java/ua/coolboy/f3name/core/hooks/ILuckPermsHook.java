package ua.coolboy.f3name.core.hooks;

import java.util.UUID;

public interface ILuckPermsHook {
    /*
        Better than Vault, we can get best group from already existed in config with sort by weight
     */
    public String getBestPlayerGroup(UUID uuid);

}
