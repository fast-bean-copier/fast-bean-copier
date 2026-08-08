package com.github.jackieonway.copier.example.v160;

import com.github.jackieonway.copier.annotation.CopyTarget;
import com.github.jackieonway.copier.annotation.CycleDetectionStrategy;

import java.util.List;

@CopyTarget(source = SelfNode.class, cycleDetection = CycleDetectionStrategy.AUTOMATIC_CACHE)
public class SelfNodeDto {
    private String name;
    private SelfNodeDto self;
    private List<SelfNodeDto> children;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public SelfNodeDto getSelf() {
        return self;
    }

    public void setSelf(SelfNodeDto self) {
        this.self = self;
    }

    public List<SelfNodeDto> getChildren() {
        return children;
    }

    public void setChildren(List<SelfNodeDto> children) {
        this.children = children;
    }
}
