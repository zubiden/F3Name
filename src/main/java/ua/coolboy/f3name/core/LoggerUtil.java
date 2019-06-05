package ua.coolboy.f3name.core;

public interface LoggerUtil {
    public void info(Object obj);
    public void error(Object obj);
    public void error(Object obj, Throwable t);
    public void printStacktrace(Exception ex);
    public void setColoredConsole(boolean colored);
}
