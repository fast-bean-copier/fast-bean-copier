package com.github.jackieonway.copier.example.nested;

public class SameTypeNested {
    private Long id;
    private String name;
    private SameTypeNested child;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public SameTypeNested getChild() { return child; }
    public void setChild(SameTypeNested child) { this.child = child; }
}
