package com.github.jackieonway.copier.example;

import com.github.jackieonway.copier.annotation.CopyTarget;

import java.util.List;
import java.util.Map;

/**
 * Department 对应的 DTO，使用嵌套集合字段。
 */
@CopyTarget(source = Department.class)
public class DepartmentDto {

    /**
     * 二层嵌套 List：List<List<UserDto>>
     */
    private List<List<UserDto>> userGroups;

    /**
     * Map<String, List<UserDto>>
     */
    private Map<String, List<UserDto>> userGroupMap;

    /**
     * List<Map<String, UserDto>>
     */
    private List<Map<String, UserDto>> userGroupList;

    public DepartmentDto() {
    }

    public DepartmentDto(List<List<UserDto>> userGroups,
                         Map<String, List<UserDto>> userGroupMap,
                         List<Map<String, UserDto>> userGroupList) {
        this.userGroups = userGroups;
        this.userGroupMap = userGroupMap;
        this.userGroupList = userGroupList;
    }

    public List<List<UserDto>> getUserGroups() {
        return userGroups;
    }

    public void setUserGroups(List<List<UserDto>> userGroups) {
        this.userGroups = userGroups;
    }

    public Map<String, List<UserDto>> getUserGroupMap() {
        return userGroupMap;
    }

    public void setUserGroupMap(Map<String, List<UserDto>> userGroupMap) {
        this.userGroupMap = userGroupMap;
    }

    public List<Map<String, UserDto>> getUserGroupList() {
        return userGroupList;
    }

    public void setUserGroupList(List<Map<String, UserDto>> userGroupList) {
        this.userGroupList = userGroupList;
    }
}


