package com.github.jackieonway.copier.example;

/**
 * 包含多种数组字段的示例实体。
 */
public class ArrayHolder {
    private Long id;
    private String[] tags;
    private int[] scores;
    private User[] users;

    public ArrayHolder() {
    }

    public ArrayHolder(Long id, String[] tags, int[] scores, User[] users) {
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

    public User[] getUsers() {
        return users;
    }

    public void setUsers(User[] users) {
        this.users = users;
    }
}


