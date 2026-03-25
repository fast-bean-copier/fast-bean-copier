package com.github.jackieonway.copier.example.v140;

import com.github.jackieonway.copier.annotation.CopyTarget;

@CopyTarget(source = BeforeMappingUser.class)
public class ProcessorOnlyUserDto {
    private String name;

    public ProcessorOnlyUserDto() {
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
