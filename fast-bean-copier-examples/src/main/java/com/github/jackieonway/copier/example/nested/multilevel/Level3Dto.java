package com.github.jackieonway.copier.example.nested.multilevel;

import com.github.jackieonway.copier.annotation.CopyTarget;

@CopyTarget(source = Level3.class)
public class Level3Dto {
    private String value;

    public String getValue() { return value; }
    public void setValue(String value) { this.value = value; }
}
