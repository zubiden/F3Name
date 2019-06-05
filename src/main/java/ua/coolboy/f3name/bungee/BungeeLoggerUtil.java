package ua.coolboy.f3name.bungee;

import java.io.PrintWriter;
import java.io.StringWriter;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;

import ua.coolboy.f3name.core.F3Name;
import ua.coolboy.f3name.core.LoggerUtil;

public class BungeeLoggerUtil implements LoggerUtil {

    private boolean coloredConsole;
    private CommandSender console;

    public BungeeLoggerUtil() {
        this(true);
    }

    public BungeeLoggerUtil(boolean coloredConsole) {
        this.coloredConsole = coloredConsole;
        console = ProxyServer.getInstance().getConsole();
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
    
    @Override
    public void printStacktrace(Exception ex) {
        StringWriter outError = new StringWriter();
        ex.printStackTrace(new PrintWriter(outError));
        console.sendMessage(getMessage(outError, ChatColor.GRAY));
    }
    
    @Override
    public void setColoredConsole(boolean bool) {
        coloredConsole = bool;
    }
    
    private BaseComponent[] getMessage(Object obj, ChatColor color) {
        String message = F3Name.PREFIX + color + obj + ChatColor.RESET;
        if (!coloredConsole) {
            message = ChatColor.stripColor(message);
        }
        return TextComponent.fromLegacyText(message);
    }

}
