package com.github.jackieonway.copier.example;

import java.util.Set;

/**
 * 用户分组实体，包含基本类型 Set 及嵌套对象 Set。
 */
public class Group {
    private Long id;
    private Set<String> tags;
    private Set<User> members;

    public Group() {
    }

    public Group(Long id, Set<String> tags, Set<User> members) {
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

    public Set<User> getMembers() {
        return members;
    }

    public void setMembers(Set<User> members) {
        this.members = members;
    }
}


