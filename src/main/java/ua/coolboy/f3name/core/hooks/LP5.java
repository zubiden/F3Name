package ua.coolboy.f3name.core.hooks;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.UUID;
import java.util.stream.Collectors;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.model.group.Group;
import net.luckperms.api.model.user.User;
import net.luckperms.api.node.NodeType;
import ua.coolboy.f3name.core.F3Group;

public class LP5 implements ILuckPermsHook{
    
    private LuckPerms api;
    private List<String> groups;

    public LP5(List<F3Group> groups) {
        this.api = LuckPermsProvider.get();
        this.groups = groups.stream().map(F3Group::getGroupName).collect(Collectors.toList());
    }
    
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
    
    @Override
    public String getBestPlayerGroup(UUID uuid) {
        User user = api.getUserManager().getUser(uuid);
        if (user == null) {
            return F3Group.DEFAULT_GROUP;
        }
        //Wrong sorting fixed, thanks to runescapejohn
        Optional<Group> group = user.getNodes().stream()
                .filter(NodeType.INHERITANCE::matches)
                .map(NodeType.INHERITANCE::cast)
                .map(node -> api.getGroupManager().getGroup(node.getGroupName()))
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
