package ua.coolboy.f3name.core.hooks;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.UUID;
import java.util.stream.Collectors;

import me.lucko.luckperms.LuckPerms;
import me.lucko.luckperms.api.Group;
import me.lucko.luckperms.api.LuckPermsApi;
import me.lucko.luckperms.api.Node;
import me.lucko.luckperms.api.User;

import ua.coolboy.f3name.core.F3Group;

public class LuckPermsHook {
    //Comparator for Groups
    public static Comparator<Group> GROUP_COMPARATOR = (Group o1, Group o2) -> {
        OptionalInt obj1 = o1 == null ? OptionalInt.empty() : o1.getWeight();
        OptionalInt obj2 = o2 == null ? OptionalInt.empty() : o2.getWeight();
        if (obj1.isPresent() && obj2.isPresent()) {
            return obj1.getAsInt() - obj2.getAsInt();
        } else if (obj1.isPresent()) {
            return -1;
        } else if (obj2.isPresent()) {
            return 1;
        } else {
            return 0;
        }
    };

    private LuckPermsApi api;
    private List<String> groups;

    public LuckPermsHook(List<F3Group> groups) {
        api = LuckPerms.getApi();
        this.groups = groups.stream().map(F3Group::getGroupName).collect(Collectors.toList());
    }

    /*
        Better than Vault, we can get best group from already existed in config with sort by weight
     */
    public String getBestPlayerGroup(UUID uuid) {
        User user = api.getUser(uuid);
        if (user == null) {
            return F3Group.DEFAULT_GROUP;
        }
        //Wrong sorting fixed, thanks to runescapejohn
        Optional<Group> group = user.getAllNodes().stream()
                .filter(Node::isGroupNode)
                .map(node -> api.getGroup(node.getGroupName()))
                //Filter non-existing groups
                .filter(Objects::nonNull)
                //getting only groups in config
                .filter(n -> groups.contains(n.getName()))
                //max by weight
                .max(GROUP_COMPARATOR);

        if (!group.isPresent()) {
            return F3Group.DEFAULT_GROUP;
        }
        return group.get().getName();
    }

}
