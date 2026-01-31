package com.github.jackieonway.copier.example.nested;

import com.github.jackieonway.copier.annotation.CopyTarget;

@CopyTarget(source = PartialMatchSource.class)
public class PartialMatchTarget {
    private Long id;
    private String name;
    // 注意：没有 extraField，测试字段不完全匹配的情况

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
}
