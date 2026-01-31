package com.github.jackieonway.copier.example.nested;

import com.github.jackieonway.copier.annotation.CopyTarget;

@CopyTarget(source = Department.class)
public class DepartmentDto {
    private Long id;
    private String name;
    private TeamDto team;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public TeamDto getTeam() { return team; }
    public void setTeam(TeamDto team) { this.team = team; }
}
