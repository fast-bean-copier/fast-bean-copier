package com.github.jackieonway.copier.example.v140;

import java.util.List;

/**
 * 测试实体类 - 用于测试深拷贝控制。
 *
 * @author jackieonway
 * @since 1.4.0
 */
public class DeepCopyTestEntity {
    
    private Long id;
    private String name;
    private NestedObject nestedObject;
    private List<String> tags;
    
    public DeepCopyTestEntity() {
    }
    
    public DeepCopyTestEntity(Long id, String name, NestedObject nestedObject, List<String> tags) {
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
    
    public NestedObject getNestedObject() {
        return nestedObject;
    }
    
    public void setNestedObject(NestedObject nestedObject) {
        this.nestedObject = nestedObject;
    }
    
    public List<String> getTags() {
        return tags;
    }
    
    public void setTags(List<String> tags) {
        this.tags = tags;
    }
    
    /**
     * 嵌套对象类。
     */
    public static class NestedObject {
        private String value;
        
        public NestedObject() {
        }
        
        public NestedObject(String value) {
            this.value = value;
        }
        
        public String getValue() {
            return value;
        }
        
        public void setValue(String value) {
            this.value = value;
        }
    }
}
