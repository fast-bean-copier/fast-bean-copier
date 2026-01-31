package com.github.jackieonway.copier.example.nested;

import com.github.jackieonway.copier.annotation.CopyTarget;

@CopyTarget(source = Team.class)
public class TeamDto {
    private Long id;
    private String name;
    private ManagerDto leader;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public ManagerDto getLeader() { return leader; }
    public void setLeader(ManagerDto leader) { this.leader = leader; }
}
