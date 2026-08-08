package com.github.jackieonway.copier.example.v160;

import java.util.List;

public class SelfNode {
    private String name;
    private SelfNode self;
    private List<SelfNode> children;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public SelfNode getSelf() {
        return self;
    }

    public void setSelf(SelfNode self) {
        this.self = self;
    }

    public List<SelfNode> getChildren() {
        return children;
    }

    public void setChildren(List<SelfNode> children) {
        this.children = children;
    }
}
