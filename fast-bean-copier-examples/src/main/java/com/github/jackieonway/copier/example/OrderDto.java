package com.github.jackieonway.copier.example;

import com.github.jackieonway.copier.annotation.CopyTarget;

import java.util.List;

/**
 * 订单 DTO，包含基本类型 List 及嵌套对象 List。
 */
@CopyTarget(source = Order.class)
public class OrderDto {
    private Long id;
    private List<String> tags;
    private List<UserDto> users;

    public OrderDto() {
    }

    public OrderDto(Long id, List<String> tags, List<UserDto> users) {
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

    public List<UserDto> getUsers() {
        return users;
    }

    public void setUsers(List<UserDto> users) {
        this.users = users;
    }
}

