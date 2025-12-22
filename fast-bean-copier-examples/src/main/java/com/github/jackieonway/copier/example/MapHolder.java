package com.github.jackieonway.copier.example;

import java.util.Map;

/**
 * Map 示例实体，包含基本类型 Value 和嵌套对象 Value。
 */
public class MapHolder {

    private Long id;
    private Map<String, String> attributes;
    private Map<String, User> userMap;

    public MapHolder() {
    }

    public MapHolder(Long id, Map<String, String> attributes, Map<String, User> userMap) {
        this.id = id;
        this.attributes = attributes;
        this.userMap = userMap;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Map<String, String> getAttributes() {
        return attributes;
    }

    public void setAttributes(Map<String, String> attributes) {
        this.attributes = attributes;
    }

    public Map<String, User> getUserMap() {
        return userMap;
    }

    public void setUserMap(Map<String, User> userMap) {
        this.userMap = userMap;
    }
}


