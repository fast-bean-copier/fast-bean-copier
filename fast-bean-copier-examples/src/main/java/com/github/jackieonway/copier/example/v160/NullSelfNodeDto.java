package com.github.jackieonway.copier.example.v160;

import com.github.jackieonway.copier.annotation.CopyTarget;
import com.github.jackieonway.copier.annotation.CycleDetectionStrategy;

import java.util.List;

@CopyTarget(source = SelfNode.class, cycleDetection = CycleDetectionStrategy.RETURN_NULL)
public class NullSelfNodeDto {
    private String name;
    private NullSelfNodeDto self;
    private List<NullSelfNodeDto> children;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public NullSelfNodeDto getSelf() {
        return self;
    }

    public void setSelf(NullSelfNodeDto self) {
        this.self = self;
    }

    public List<NullSelfNodeDto> getChildren() {
        return children;
    }

    public void setChildren(List<NullSelfNodeDto> children) {
        this.children = children;
    }
}
