package com.github.jackieonway.copier.example;

import com.github.jackieonway.copier.annotation.CopyTarget;

import java.util.Set;

/**
 * 用户分组 DTO，包含基本类型 Set 及嵌套对象 Set。
 */
@CopyTarget(source = Group.class)
public class GroupDto {
    private Long id;
    private Set<String> tags;
    private Set<UserDto> members;

    public GroupDto() {
    }

    public GroupDto(Long id, Set<String> tags, Set<UserDto> members) {
        this.id = id;
        this.tags = tags;
        this.members = members;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Set<String> getTags() {
        return tags;
    }

    public void setTags(Set<String> tags) {
        this.tags = tags;
    }

    public Set<UserDto> getMembers() {
        return members;
    }

    public void setMembers(Set<UserDto> members) {
        this.members = members;
    }
}


