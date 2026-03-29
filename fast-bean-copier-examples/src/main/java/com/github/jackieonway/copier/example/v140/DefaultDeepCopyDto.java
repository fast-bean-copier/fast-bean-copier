package com.github.jackieonway.copier.example.v140;

import com.github.jackieonway.copier.annotation.CopyTarget;

import java.util.List;

/**
 * 测试 DTO 类 - 用于测试默认深拷贝行为（deepCopy 默认为 true）。
 *
 * @author jackieonway
 * @since 1.4.0
 */
@CopyTarget(source = DeepCopyTestEntity.class)
public class DefaultDeepCopyDto {
    
    private Long id;
    private String name;
    private DeepCopyTestEntity.NestedObject nestedObject;
    private List<String> tags;
    
    public DefaultDeepCopyDto() {
    }
    
    public DefaultDeepCopyDto(Long id, String name, DeepCopyTestEntity.NestedObject nestedObject, List<String> tags) {
        this.id = id;
        this.name = name;
        this.nestedObject = nestedObject;
        this.tags = tags;
    }
    
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public DeepCopyTestEntity.NestedObject getNestedObject() {
        return nestedObject;
    }
    
    public void setNestedObject(DeepCopyTestEntity.NestedObject nestedObject) {
        this.nestedObject = nestedObject;
    }
    
    public List<String> getTags() {
        return tags;
    }
    
    public void setTags(List<String> tags) {
        this.tags = tags;
    }
}
