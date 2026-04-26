package com.github.jackieonway.copier.example.v140;

import com.github.jackieonway.copier.annotation.CopyTarget;

@CopyTarget(source = BeforeMappingUser.class)
public class BeforeMappingUserDto {
    private String name;
    private String capturedName;

    public BeforeMappingUserDto() {
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCapturedName() {
        return capturedName;
    }

    public void setCapturedName(String capturedName) {
        this.capturedName = capturedName;
    }

    public void captureName(BeforeMappingUser source) {
        if (source != null) {
            this.capturedName = source.getName();
        }
    }
}
