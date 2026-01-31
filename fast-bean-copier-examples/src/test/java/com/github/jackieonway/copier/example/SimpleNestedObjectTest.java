package com.github.jackieonway.copier.example;

import com.github.jackieonway.copier.example.nested.*;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * 简化的嵌套对象测试。
 *
 * @author jackieonway
 * @since 1.3.2
 */
public class SimpleNestedObjectTest {

    @Test
    public void testEmployeeWithAddress() {
        // 创建测试数据
        Address address = new Address();
        address.setProvince("广东省");
        address.setCity("深圳市");
        address.setStreet("科技园");

        Employee employee = new Employee();
        employee.setId(1L);
        employee.setName("张三");
        employee.setAddress(address);

        // 执行转换
        EmployeeDto dto = EmployeeDtoCopier.toDto(employee);

        // 验证结果
        assertNotNull("DTO should not be null", dto);
        assertEquals("ID should match", Long.valueOf(1L), dto.getId());
        assertEquals("Name should match", "张三", dto.getName());
        
        assertNotNull("Address DTO should not be null", dto.getAddress());
        assertEquals("Province should match", "广东省", dto.getAddress().getProvince());
        assertEquals("City should match", "深圳市", dto.getAddress().getCity());
        assertEquals("Street should match", "科技园", dto.getAddress().getStreet());
    }

    @Test
    public void testEmployeeWithNullAddress() {
        Employee employee = new Employee();
        employee.setId(2L);
        employee.setName("李四");
        employee.setAddress(null);

        EmployeeDto dto = EmployeeDtoCopier.toDto(employee);

        assertNotNull("DTO should not be null", dto);
        assertEquals("ID should match", Long.valueOf(2L), dto.getId());
        assertNull("Address DTO should be null", dto.getAddress());
    }

    @Test
    public void testPersonWithSimpleAddress() {
        // 测试无 Copier 的字段拷贝
        SimpleAddress simpleAddress = new SimpleAddress();
        simpleAddress.setCity("杭州市");
        simpleAddress.setStreet("西湖区");

        PersonWithSimpleAddress person = new PersonWithSimpleAddress();
        person.setId(1L);
        person.setName("测试用户");
        person.setAddress(simpleAddress);

        PersonWithSimpleAddressDto dto = PersonWithSimpleAddressDtoCopier.toDto(person);

        assertNotNull("DTO should not be null", dto);
        assertEquals("ID should match", Long.valueOf(1L), dto.getId());
        assertEquals("Name should match", "测试用户", dto.getName());
        
        assertNotNull("Address DTO should not be null", dto.getAddress());
        assertEquals("City should match", "杭州市", dto.getAddress().getCity());
        assertEquals("Street should match", "西湖区", dto.getAddress().getStreet());
    }

    @Test
    public void testNullSourceReturnsNull() {
        EmployeeDto dto = EmployeeDtoCopier.toDto(null);
        assertNull("DTO should be null when source is null", dto);
    }
}
