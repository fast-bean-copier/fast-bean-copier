package com.github.jackieonway.copier.example.nested;

public class Department {
    private Long id;
    private String name;
    private Team team;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public Team getTeam() { return team; }
    public void setTeam(Team team) { this.team = team; }
}
