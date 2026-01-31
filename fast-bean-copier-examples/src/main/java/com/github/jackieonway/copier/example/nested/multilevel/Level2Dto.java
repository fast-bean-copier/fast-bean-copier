package com.github.jackieonway.copier.example.nested.multilevel;

// 注意：Level2Dto 没有 @CopyTarget 注解
public class Level2Dto {
    private String name;
    private Level3Dto level3;

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public Level3Dto getLevel3() { return level3; }
    public void setLevel3(Level3Dto level3) { this.level3 = level3; }
}
