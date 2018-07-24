package ua.coolboy.f3name.hooks;

import java.util.List;
import java.util.stream.Collectors;
import net.milkbowl.vault.permission.Permission;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;
import ua.coolboy.f3name.GroupDS;

public class VaultHook {

    private Permission perms;
    private List<String> groups;

    public VaultHook(List<GroupDS> groups) {
        RegisteredServiceProvider<Permission> rsp = Bukkit.getServicesManager().getRegistration(Permission.class);
        perms = rsp.getProvider();
        this.groups = groups.stream().map(GroupDS::getGroupName).collect(Collectors.toList());
    }

    public String getBestPlayerGroup(Player player) {
        String group = perms.getPrimaryGroup(player);
        return groups.contains(group) ? group : GroupDS.DEFAULT_GROUP;
    }

}
