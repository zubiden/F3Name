package ua.coolboy.f3name.bungee.messenger;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import java.util.HashMap;
import java.util.Map;
import net.md_5.bungee.api.Callback;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.connection.Server;
import net.md_5.bungee.api.event.PluginMessageEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;
import ua.coolboy.f3name.bungee.F3NameBungee;
import ua.coolboy.f3name.core.F3Group;
import ua.coolboy.f3name.core.F3Name;

public class BungeeMessenger implements Listener {

    private F3NameBungee plugin;
    private Map<String, Callback<String>> groupCallback;
    private Map<String, Callback<String>> messageCallback;
    private Map<String, Callback<Boolean>> checkCallback;

    public BungeeMessenger(F3NameBungee plugin) {
        this.plugin = plugin;
        plugin.getProxy().getPluginManager().registerListener(plugin, this);

        groupCallback = new HashMap<>();
        messageCallback = new HashMap<>();
        checkCallback = new HashMap<>();
    }

    public void getPlayerGroup(String playername, Callback<String> callback) {
        ProxiedPlayer player = plugin.getProxy().getPlayer(playername);
        if (player == null) {
            callback.done(F3Group.DEFAULT_GROUP, new IllegalArgumentException("Player not found!"));
            return;
        }
        getPlayerGroup(player, callback);
    }

    public void getPlayerGroup(ProxiedPlayer player, Callback<String> callback) {
        Server server = player.getServer();
        if (server == null || !plugin.getHookedServers().contains(server.getInfo().getName())) {
            callback.done(F3Group.DEFAULT_GROUP, null);
            return;
        }
        ByteArrayDataOutput out = getNewOutput();
        out.writeUTF(Actions.GROUP.getSubchannel());
        out.writeUTF(player.getName());
        server.sendData(F3Name.PLUGIN_CHANNEL, out.toByteArray());
        groupCallback.put(player.getName(), callback);
    }

    //Callback never fires if server don't have plugin
    public void checkServer(String server, Callback<Boolean> callback) {
        ServerInfo info = plugin.getProxy().getServerInfo(server);
        if (info == null) {
            callback.done(false, new IllegalArgumentException("Server not found!"));
            return;
        }
        checkServer(info, callback);
    }

    //Callback never fires if server don't have plugin
    public void checkServer(ServerInfo server, Callback<Boolean> callback) {
        ByteArrayDataOutput out = getNewOutput();
        out.writeUTF(Actions.CHECK.getSubchannel());
        server.sendData(F3Name.PLUGIN_CHANNEL, out.toByteArray());
        checkCallback.put(server.getName().toLowerCase(), callback);
    }

    public void getMessage(String playername, String message, Callback<String> callback) {
        ProxiedPlayer player = plugin.getProxy().getPlayer(playername);
        if (player == null || player.getServer() == null) {
            callback.done(message, new IllegalArgumentException("Player not found!"));
            return;
        }

        if (!plugin.getHookedServers().contains(player.getServer().getInfo().getName())) {
            callback.done(message, new IllegalAccessError("Server don't have plugin")); //IllegalAccess? Why not?
            return;
        }

        ByteArrayDataOutput out = getNewOutput();
        out.writeUTF(Actions.MESSAGE.getSubchannel());
        out.writeUTF(player.getName());
        out.writeUTF(message);

        player.getServer().sendData(F3Name.PLUGIN_CHANNEL, out.toByteArray());

        messageCallback.put(player.getName(), callback);
    }

    @EventHandler
    public void onPluginMessage(PluginMessageEvent e) {
        if (e.getTag().equals(F3NameBungee.PLUGIN_CHANNEL)) {
            if (!(e.getReceiver() instanceof ProxiedPlayer)) {
                return;
            }
            ProxiedPlayer player = (ProxiedPlayer) e.getReceiver();
            ByteArrayDataInput in = ByteStreams.newDataInput(e.getData());
            String code = in.readUTF();
            switch (code) {
                case "check":
                    boolean bungee = in.readBoolean();
                    String servername = player.getServer().getInfo().getName();
                    
                    plugin.getLoggerUtil().info("Found plugin on " + servername);

                    Callback<Boolean> b = checkCallback.get(servername);
                    if (b != null) {
                        b.done(bungee, null);
                        checkCallback.remove(servername);
                    } else if (bungee) {
                        plugin.addHookedServer(servername);
                    } else {
                        plugin.getConfigParser().excludeServer(servername);
                    }

                    //auto response
                    ByteArrayDataOutput out = ByteStreams.newDataOutput();
                    out.writeUTF("ok");
                    player.getServer().sendData(F3NameBungee.PLUGIN_CHANNEL, out.toByteArray());
                    break;
                case "group":
                    String group = in.readUTF();
                    Callback<String> s = groupCallback.get(player.getName());
                    if (s != null) {
                        s.done(group, null);
                    }
                    groupCallback.remove(player.getName());
                    break;
                case "message":
                    String text = in.readUTF();
                    Callback<String> msg = messageCallback.get(player.getName());

                    if (msg != null) {
                        msg.done(text, null);
                    } else {
                        plugin.sendLocally(player, text);
                    }
                    messageCallback.remove(player.getName());
                    break;
            }
        }
    }

    private ByteArrayDataOutput getNewOutput() {
        return ByteStreams.newDataOutput();
    }

    private ByteArrayDataInput getInput(byte[] bytes) {
        return ByteStreams.newDataInput(bytes);
    }

    public enum Actions {
        CHECK, GROUP, MESSAGE;

        public String getSubchannel() {
            return this.toString().toLowerCase();
        }
    }
}
