package com.github.jackieonway.copier.example.nested;

public class Team {
    private Long id;
    private String name;
    private Manager leader;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public Manager getLeader() { return leader; }
    public void setLeader(Manager leader) { this.leader = leader; }
}
