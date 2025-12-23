package com.github.jackieonway.copier.example.processor;

import com.github.jackieonway.copier.annotation.CopyTarget;
import com.github.jackieonway.copier.example.UserDto;
import java.util.List;
import java.util.Map;

@CopyTarget(source = WildcardContainer.class)
public class WildcardContainerDto {

    private List<UserDto> users;
    private Map<String, UserDto> userMap;

    public List<UserDto> getUsers() {
        return users;
    }

    public void setUsers(List<UserDto> users) {
        this.users = users;
    }

    public Map<String, UserDto> getUserMap() {
        return userMap;
    }

    public void setUserMap(Map<String, UserDto> userMap) {
        this.userMap = userMap;
    }
}

