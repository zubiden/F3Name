package ua.coolboy.f3name.bukkit;

import java.util.ArrayList;
import java.util.List;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import ua.coolboy.f3name.core.ConfigParser;
import ua.coolboy.f3name.core.F3Group;

public class BukkitConfigParser implements ConfigParser {

    private FileConfiguration config;

    private boolean coloredConsole, bungeecord, autoupdate;

    private List<F3Group> groups;

    public BukkitConfigParser(FileConfiguration config) {
        this.config = config;

        coloredConsole = config.getBoolean("colored-console", true);
        bungeecord = config.getBoolean("bungeecord-as-primary", false);
        autoupdate = config.getBoolean("auto-update", true);
        
        groups = new ArrayList<>();
        for (String key : config.getConfigurationSection("groups").getKeys(false)) {
            ConfigurationSection section = config.getConfigurationSection("groups." + key);
            List<String> messages = section.getStringList("f3names");
            int updateTime = section.getInt("update-time", 200);
            boolean shuffle = section.getBoolean("shuffle", false);
            groups.add(new F3Group(key, messages, updateTime, shuffle));
        }
    }

    @Override
    public boolean isColoredConsole() {
        return coloredConsole;
    }
    
    public boolean isAutoUpdate() {
        return autoupdate;
    }
    
    public boolean isBungeeCord() {
        return bungeecord;
    }

    @Override
    public F3Group getF3Group(String name) {
        for (F3Group gds : groups) {
            if (gds.getGroupName().equals(name)) {
                return gds;
            }
        }
        return null;
    }

    @Override
    public List<F3Group> getF3GroupList() {
        return groups;
    }

}
