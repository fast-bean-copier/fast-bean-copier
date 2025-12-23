package com.github.jackieonway.copier.example.processor;

import com.github.jackieonway.copier.example.User;
import java.util.List;
import java.util.Map;

public class WildcardContainer {

    private List<? extends User> users;
    private Map<String, ? extends User> userMap;

    public List<? extends User> getUsers() {
        return users;
    }

    public void setUsers(List<? extends User> users) {
        this.users = users;
    }

    public Map<String, ? extends User> getUserMap() {
        return userMap;
    }

    public void setUserMap(Map<String, ? extends User> userMap) {
        this.userMap = userMap;
    }
}

