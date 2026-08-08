package com.github.jackieonway.copier.example.v160;

import com.github.jackieonway.copier.annotation.CopyTarget;
import com.github.jackieonway.copier.annotation.CycleDetectionStrategy;

@CopyTarget(source = BiNodeB.class, cycleDetection = CycleDetectionStrategy.AUTOMATIC_CACHE)
public class AutoBiNodeBDto {
    private String label;
    private AutoBiNodeADto parent;

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public AutoBiNodeADto getParent() {
        return parent;
    }

    public void setParent(AutoBiNodeADto parent) {
        this.parent = parent;
    }
}
