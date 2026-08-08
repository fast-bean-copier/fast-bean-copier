package com.github.jackieonway.copier.example.v160;

import com.github.jackieonway.copier.annotation.CopyTarget;
import com.github.jackieonway.copier.annotation.CycleDetectionStrategy;

@CopyTarget(source = BiNodeB.class, cycleDetection = CycleDetectionStrategy.RETURN_NULL)
public class NullBiNodeBDto {
    private String label;
    private NullBiNodeADto parent;

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public NullBiNodeADto getParent() {
        return parent;
    }

    public void setParent(NullBiNodeADto parent) {
        this.parent = parent;
    }
}
