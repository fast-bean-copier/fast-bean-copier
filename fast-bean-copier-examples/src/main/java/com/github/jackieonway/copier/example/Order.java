package com.github.jackieonway.copier.example;

import java.util.List;

/**
 * 订单实体，包含基本类型 List 及嵌套对象 List。
 */
public class Order {
    private Long id;
    private List<String> tags;
    private List<User> users;

    public Order() {
    }

    public Order(Long id, List<String> tags, List<User> users) {
        this.id = id;
        this.tags = tags;
        this.users = users;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public List<String> getTags() {
        return tags;
    }

    public void setTags(List<String> tags) {
        this.tags = tags;
    }

    public List<User> getUsers() {
        return users;
    }

    public void setUsers(List<User> users) {
        this.users = users;
    }
}

