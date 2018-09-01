package ua.coolboy.f3name.core;

import java.util.List;

public interface ConfigParser {

    public boolean isColoredConsole();

    public F3Group getF3Group(String name);

    public List<F3Group> getF3GroupList();
    
}
