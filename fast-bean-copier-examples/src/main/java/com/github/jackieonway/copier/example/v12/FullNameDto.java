package com.github.jackieonway.copier.example.v12;

import com.github.jackieonway.copier.annotation.CopyField;
import com.github.jackieonway.copier.annotation.CopyTarget;

/**
 * 一对多测试用目标 DTO 类 - 演示一对多字段拆分映射。
 *
 * @author jackieonway
 * @since 1.2.0
 */
@CopyTarget(source = FullNameSource.class)
public class FullNameDto {
    
    private Long id;
    
    /**
     * 一对多映射：fullName -> firstName（取第一个单词）
     */
    @CopyField(source = "fullName", 
               expression = "source.getFullName() != null && !source.getFullName().isEmpty() ? " +
                           "source.getFullName().split(\" \")[0] : source.getFullName()")
    private String firstName;
    
    /**
     * 一对多映射：fullName -> lastName（取第二个单词，如果存在）
     */
    @CopyField(source = "fullName", 
               expression = "source.getFullName() != null && !source.getFullName().isEmpty() && " +
                           "source.getFullName().split(\" \").length > 1 ? " +
                           "source.getFullName().split(\" \")[1] : " +
                           "(source.getFullName() != null ? \"\" : null)")
    private String lastName;
    
    /**
     * 一对多映射：address -> city（取逗号前的部分）
     */
    @CopyField(source = "address", 
               expression = "source.getAddress() != null && !source.getAddress().isEmpty() && " +
                           "source.getAddress().contains(\",\") ? " +
                           "source.getAddress().split(\",\")[0].trim() : " +
                           "(source.getAddress() != null ? source.getAddress() : null)")
    private String city;
    
    /**
     * 一对多映射：address -> country（取逗号后的部分）
     */
    @CopyField(source = "address", 
               expression = "source.getAddress() != null && !source.getAddress().isEmpty() && " +
                           "source.getAddress().contains(\",\") && " +
                           "source.getAddress().split(\",\").length > 1 ? " +
                           "source.getAddress().split(\",\")[1].trim() : " +
                           "(source.getAddress() != null ? \"\" : null)")
    private String country;
    
    /**
     * 简单字段映射
     */
    private String email;

    public FullNameDto() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}