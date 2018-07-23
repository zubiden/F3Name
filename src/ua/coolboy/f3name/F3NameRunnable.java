package ua.coolboy.f3name;

import com.google.common.collect.ImmutableList;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import ua.coolboy.f3name.packet.IPayloadPacket;
import ua.coolboy.f3name.packet.VersionHandler;

public class F3NameRunnable extends BukkitRunnable {

    private List<String> names;
    private int current;
    private VersionHandler handler;
    private GroupDS group;
    
    private static final Random random = new Random();

    private List<Player> players;

    public F3NameRunnable(VersionHandler handler, GroupDS group) {
        this.handler = handler;
        this.players = new ArrayList<>();
        this.group = group;
        
        if (group.getNamesList() == null || group.getNamesList().isEmpty()) {
            throw new IllegalArgumentException("List must contain at least one string!");
        }
        
        this.names = new ArrayList<>();

        for (String string : group.getNamesList()) {
            this.names.add(ChatColor.translateAlternateColorCodes('&', string) + ChatColor.RESET);
        }

        current = -1;
    }

    public String getCurrentString() {
        return current < 0 ? names.get(0) : names.get(current);
    }

    public List<String> getStrings() {
        return names;
    }

    public List<Player> getPlayers() {
        return ImmutableList.copyOf(players); //clone list
    }
    
    public GroupDS getGroup() {
        return group;
    }

    public void addPlayer(Player player) {
        players.add(player);
    }

    public void removePlayer(Player player) {
        players.remove(player);
    }

    @Override
    public void run() {
        if (group.isShuffle()) {
            current = random.nextInt(names.size());
        } else {
            current++;
        }
        if (names.size() <= current) {
            current = 0;
        }
        IPayloadPacket packet = handler.getPacket();
        for (Player player : players) {
            if (!player.isOnline()) {
                players.remove(player);
                continue;
            }
            packet.send(player, names.get(current));
        }
    }

}
