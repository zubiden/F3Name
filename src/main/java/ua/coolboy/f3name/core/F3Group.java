package ua.coolboy.f3name.core;

import java.util.List;

public class F3Group {

    private String group;
    private List<String> messages;
    private boolean shuffle;
    private int updateTime;
    
    public static final String DEFAULT_GROUP = "everyone";
    
    //GroupDebugScreen
    public F3Group(String group, List<String> messages, int updateTime, boolean shuffle) {
        this.group = group;
        this.messages = messages;
        this.updateTime = updateTime;
        this.shuffle = shuffle;
    }

    public String getGroupName() {
        return group;
    }

    public List<String> getNamesList() {
        return messages;
    }
    
    public boolean isShuffle() {
        return shuffle;
    }
    
    public int getUpdateTime() {
        return updateTime;
    }
}
