package com.github.jackieonway.copier.example.v160;

import com.github.jackieonway.copier.annotation.CopyTarget;
import com.github.jackieonway.copier.annotation.CycleDetectionStrategy;

@CopyTarget(source = BiNodeA.class, cycleDetection = CycleDetectionStrategy.AUTOMATIC_CACHE)
public class AutoBiNodeADto {
    private String name;
    private AutoBiNodeBDto child;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public AutoBiNodeBDto getChild() {
        return child;
    }

    public void setChild(AutoBiNodeBDto child) {
        this.child = child;
    }
}
