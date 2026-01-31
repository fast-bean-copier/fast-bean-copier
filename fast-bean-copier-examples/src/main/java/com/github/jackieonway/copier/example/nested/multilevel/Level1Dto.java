package com.github.jackieonway.copier.example.nested.multilevel;

import com.github.jackieonway.copier.annotation.CopyTarget;

@CopyTarget(source = Level1.class)
public class Level1Dto {
    private Long id;
    private Level2Dto level2;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Level2Dto getLevel2() { return level2; }
    public void setLevel2(Level2Dto level2) { this.level2 = level2; }
}
