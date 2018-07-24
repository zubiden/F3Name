package ua.coolboy.f3name;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import org.bukkit.ChatColor;
import org.bukkit.configuration.ConfigurationSection;

public class ConfigParser {

    private F3Name plugin;

    private boolean coloredConsole;

    private List<GroupDS> groups;

    public ConfigParser(F3Name plugin) {
        this.plugin = plugin;

        File file = new File(plugin.getDataFolder(), "config.yml");
        if (!file.exists()) {
            plugin.saveDefaultConfig();
        }

        coloredConsole = plugin.getConfig().getBoolean("colored-console", true);

        if (!plugin.getConfig().isConfigurationSection("groups.everyone")) {
            plugin.info("Can't find group " + ChatColor.RED + "everyone" + ChatColor.GOLD + "!"
                    + " If you're not added all groups into config, errors will appear!");
        }
        
        groups = new ArrayList<>();
        for (String key : plugin.getConfig().getConfigurationSection("groups").getKeys(false)) {
            ConfigurationSection section = plugin.getConfig().getConfigurationSection("groups." + key);
            groups.add(new GroupDS(section));
        }
    }

    public boolean isColoredConsole() {
        return coloredConsole;
    }

    public GroupDS getGDSForGroup(String group) {
        for (GroupDS gds : groups) {
            if (gds.getGroupName().equals(group)) {
                return gds;
            }
        }
        return null;
    }

    public List<GroupDS> getGDSList() {
        return groups;
    }

}
