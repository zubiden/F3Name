package ua.coolboy.f3name;

import java.util.List;
import org.bukkit.configuration.ConfigurationSection;

public class GroupDS {

    private String group;
    private List<String> names;
    private boolean shuffle;
    private int updateTime;
    
    public static final String DEFAULT_GROUP = "everyone";
    
    //GroupDebugScreen
    public GroupDS(ConfigurationSection section) {
        group = section.getName();
        names = section.getStringList("f3names");
        shuffle = section.getBoolean("shuffle", false);
        updateTime = section.getInt("update-time", 100);
    }

    public String getGroupName() {
        return group;
    }

    public List<String> getNamesList() {
        return names;
    }
    
    public boolean isShuffle() {
        return shuffle;
    }
    
    public int getUpdateTime() {
        return updateTime;
    }
}
