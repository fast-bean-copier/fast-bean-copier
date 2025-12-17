package com.github.jackieonway.copier.example;

import com.github.jackieonway.copier.annotation.CopyTarget;

/**
 * 包含多种数组字段的示例 DTO。
 */
@CopyTarget(source = ArrayHolder.class)
public class ArrayHolderDto {
    private Long id;
    private String[] tags;
    private int[] scores;
    private UserDto[] users;

    public ArrayHolderDto() {
    }

    public ArrayHolderDto(Long id, String[] tags, int[] scores, UserDto[] users) {
        this.id = id;
        this.tags = tags;
        this.scores = scores;
        this.users = users;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String[] getTags() {
        return tags;
    }

    public void setTags(String[] tags) {
        this.tags = tags;
    }

    public int[] getScores() {
        return scores;
    }

    public void setScores(int[] scores) {
        this.scores = scores;
    }

    public UserDto[] getUsers() {
        return users;
    }

    public void setUsers(UserDto[] users) {
        this.users = users;
    }
}


