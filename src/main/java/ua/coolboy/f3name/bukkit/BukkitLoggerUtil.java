package ua.coolboy.f3name.bukkit;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.ConsoleCommandSender;
import ua.coolboy.f3name.core.F3Name;
import ua.coolboy.f3name.core.LoggerUtil;

public class BukkitLoggerUtil implements LoggerUtil {

    private boolean coloredConsole;
    private ConsoleCommandSender console;

    public BukkitLoggerUtil() {
        this(true);
    }

    public BukkitLoggerUtil(boolean coloredConsole) {
        this.coloredConsole = coloredConsole;
        console = Bukkit.getConsoleSender();
    }

    @Override
    public void info(Object obj) {
        console.sendMessage(getMessage(obj, ChatColor.GOLD));
    }

    @Override
    public void error(Object obj) {
        console.sendMessage(getMessage(obj, ChatColor.DARK_RED));
    }

    @Override
    public void error(Object obj, Throwable t) {
        console.sendMessage(getMessage(obj + "\n" + t.getMessage(), ChatColor.DARK_RED));
    }

    private String getMessage(Object obj, ChatColor color) {
        String message = F3Name.PREFIX + color + obj + ChatColor.RESET;
        if (!coloredConsole) {
            message = ChatColor.stripColor(message);
        }
        return message;
    }

    @Override
    public void setColoredConsole(boolean colored) {
        coloredConsole = colored;
    }

}
