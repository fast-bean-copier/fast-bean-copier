package com.github.jackieonway.copier.example.v150;

import com.github.jackieonway.copier.annotation.CopyField;
import com.github.jackieonway.copier.annotation.CopyFromMap;
import com.github.jackieonway.copier.annotation.CopyTarget;
import com.github.jackieonway.copier.annotation.CopyToMap;

/**
 * v1.5.0 Bean ↔ Map 转换测试 DTO。
 *
 * <p>同时标注 @CopyToMap 和 @CopyFromMap，生成双向转换方法。
 * 同时标注 @CopyTarget，生成独立的 BeanCopier 类。
 *
 * @author jackieonway
 * @since 1.5.0
 */
@CopyTarget(source = MapUser.class)
@CopyToMap
@CopyFromMap
public class MapUserDto {
    private Long id;
    private String name;
    private Integer age;

    @CopyField(mapKey = "userEmail")
    private String email;

    public MapUserDto() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public Integer getAge() { return age; }
    public void setAge(Integer age) { this.age = age; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
}
