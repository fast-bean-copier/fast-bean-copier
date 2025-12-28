package com.github.jackieonway.copier.example.v12;

import com.github.jackieonway.copier.annotation.CopyField;
import com.github.jackieonway.copier.annotation.CopyTarget;

/**
 * v1.2 测试用目标 DTO 类 - 演示多对一、表达式、自定义转换器。
 *
 * @author jackieonway
 * @since 1.2.0
 */
@CopyTarget(source = Person.class, uses = PersonConverter.class)
public class PersonDto {
    
    private Long id;
    
    /**
     * 多对一映射：firstName + lastName -> fullName
     */
    @CopyField(source = {"firstName", "lastName"}, 
               expression = "source.getFirstName() + \" \" + source.getLastName()")
    private String fullName;
    
    /**
     * 使用自定义转换器的具名方法
     */
    @CopyField(source = "status", qualifiedByName = "statusToName")
    private String statusName;
    
    /**
     * 简单字段映射
     */
    private String address;
    
    /**
     * 多对一映射：city + country -> location
     */
    @CopyField(source = {"city", "country"}, 
               expression = "source.getCity() + \", \" + source.getCountry()")
    private String location;

    public PersonDto() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getStatusName() {
        return statusName;
    }

    public void setStatusName(String statusName) {
        this.statusName = statusName;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }
}
