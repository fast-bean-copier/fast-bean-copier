package com.github.jackieonway.copier.example;

import com.github.jackieonway.copier.annotation.CopyTarget;

import java.util.Map;

/**
 * Map 示例 DTO，包含基本类型 Value 和嵌套对象 Value。
 */
@CopyTarget(source = MapHolder.class)
public class MapHolderDto {

    private Long id;
    private Map<String, String> attributes;
    private Map<String, UserDto> userMap;

    public MapHolderDto() {
    }

    public MapHolderDto(Long id, Map<String, String> attributes, Map<String, UserDto> userMap) {
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

    public Map<String, UserDto> getUserMap() {
        return userMap;
    }

    public void setUserMap(Map<String, UserDto> userMap) {
        this.userMap = userMap;
    }
}


