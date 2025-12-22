package com.github.jackieonway.copier.example;

import java.util.List;
import java.util.Map;

/**
 * 部门，包含多种嵌套集合字段用于测试。
 */
public class Department {

    /**
     * 二层嵌套 List：List<List<User>>
     */
    private List<List<User>> userGroups;

    /**
     * Map<String, List<User>>
     */
    private Map<String, List<User>> userGroupMap;

    /**
     * List<Map<String, User>>
     */
    private List<Map<String, User>> userGroupList;

    public Department() {
    }

    public Department(List<List<User>> userGroups,
                      Map<String, List<User>> userGroupMap,
                      List<Map<String, User>> userGroupList) {
        this.userGroups = userGroups;
        this.userGroupMap = userGroupMap;
        this.userGroupList = userGroupList;
    }

    public List<List<User>> getUserGroups() {
        return userGroups;
    }

    public void setUserGroups(List<List<User>> userGroups) {
        this.userGroups = userGroups;
    }

    public Map<String, List<User>> getUserGroupMap() {
        return userGroupMap;
    }

    public void setUserGroupMap(Map<String, List<User>> userGroupMap) {
        this.userGroupMap = userGroupMap;
    }

    public List<Map<String, User>> getUserGroupList() {
        return userGroupList;
    }

    public void setUserGroupList(List<Map<String, User>> userGroupList) {
        this.userGroupList = userGroupList;
    }
}


