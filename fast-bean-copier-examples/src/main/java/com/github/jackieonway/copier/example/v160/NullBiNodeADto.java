package com.github.jackieonway.copier.example.v160;

import com.github.jackieonway.copier.annotation.CopyTarget;
import com.github.jackieonway.copier.annotation.CycleDetectionStrategy;

@CopyTarget(source = BiNodeA.class, cycleDetection = CycleDetectionStrategy.RETURN_NULL)
public class NullBiNodeADto {
    private String name;
    private NullBiNodeBDto child;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public NullBiNodeBDto getChild() {
        return child;
    }

    public void setChild(NullBiNodeBDto child) {
        this.child = child;
    }
}
