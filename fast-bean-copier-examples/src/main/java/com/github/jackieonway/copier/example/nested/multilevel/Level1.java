package com.github.jackieonway.copier.example.nested.multilevel;

public class Level1 {
    private Long id;
    private Level2 level2;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Level2 getLevel2() { return level2; }
    public void setLevel2(Level2 level2) { this.level2 = level2; }
}
