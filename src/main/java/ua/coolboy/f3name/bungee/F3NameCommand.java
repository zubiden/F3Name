package ua.coolboy.f3name.bungee;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.plugin.Command;

public class F3NameCommand extends Command {

    private F3NameBungee plugin;

    public F3NameCommand(F3NameBungee plugin) {
        super("f3name", "f3name.reload", "fname", "debugname", "f3n");
        this.plugin = plugin;
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (args.length == 1 && args[0].equals("reload")) {

            plugin.reload();

            sender.sendMessage(TextComponent.fromLegacyText(
                    F3NameBungee.PREFIX + ChatColor.GOLD + "Reloaded configuration!"
            ));
        } else {
            sender.sendMessage(TextComponent.fromLegacyText(
                    F3NameBungee.PREFIX + ChatColor.GOLD + "v"
                    + plugin.getDescription().getVersion() + " by Cool_boy (aka prettydude)"
            ));
        }
    }
}
