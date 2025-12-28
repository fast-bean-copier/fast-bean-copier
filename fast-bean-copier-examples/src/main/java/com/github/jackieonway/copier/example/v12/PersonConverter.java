package com.github.jackieonway.copier.example.v12;

/**
 * v1.2 测试用自定义转换器类。
 *
 * @author jackieonway
 * @since 1.2.0
 */
public class PersonConverter {

    /**
     * 将名字首字母大写。
     */
    public String capitalizeFirstName(String firstName) {
        if (firstName == null || firstName.isEmpty()) {
            return firstName;
        }
        return firstName.substring(0, 1).toUpperCase() + firstName.substring(1).toLowerCase();
    }

    /**
     * 将姓氏转为大写。
     */
    public String upperCaseLastName(String lastName) {
        if (lastName == null) {
            return null;
        }
        return lastName.toUpperCase();
    }

    /**
     * 状态码转换为状态名称。
     */
    public String statusToName(Integer status) {
        if (status == null) {
            return "UNKNOWN";
        }
        switch (status) {
            case 0: return "INACTIVE";
            case 1: return "ACTIVE";
            case 2: return "PENDING";
            default: return "UNKNOWN";
        }
    }
}
