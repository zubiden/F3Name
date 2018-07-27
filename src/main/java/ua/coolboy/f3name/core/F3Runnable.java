package ua.coolboy.f3name.core;

import java.util.List;

public interface F3Runnable extends Runnable{

    public F3Group getGroup();
    
    public String getCurrentString();

    public List<String> getStrings();
    
}
