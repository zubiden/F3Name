package ua.coolboy.f3name.core.hooks;

import java.util.List;
import ua.coolboy.f3name.core.F3Group;

public abstract class LuckPermsHook {

    public final static ILuckPermsHook get(List<F3Group> groups) {
        try{
            Class.forName("me.lucko.luckperms.LuckPerms");
            return new LPLegacy(groups);
        } catch(ClassNotFoundException ex) {
            
        }
        
        try{
            Class.forName("net.luckperms.api.LuckPerms");
            return new LP5(groups);
        } catch(ClassNotFoundException ex) {
            
        }
        return null;
    }
}
