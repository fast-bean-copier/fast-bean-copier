package com.github.jackieonway.copier.example;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * 嵌套对象拷贝的集成测试。
 *
 * 注意：当前实现支持同名字段的直接拷贝。
 * 对于不同类型的嵌套对象（如Address -> AddressDto），
 * 需要在应用层手动处理或使用自定义转换器。
 *
 * @author jackieonway
 * @since 1.0.0
 */
public class NestedObjectCopyTest {

    /**
     * 测试基本字段拷贝（不涉及嵌套对象类型转换）。
     */
    @Test
    public void testBasicFieldCopy() {
        // 创建源对象
        Employee employee = new Employee(1L, "张三", null);
        
        // 调用生成的 toDto 方法
        EmployeeDto employeeDto = EmployeeDtoCopier.toDto(employee);
        
        // 验证基本字段拷贝
        assertNotNull(employeeDto);
        assertEquals(Long.valueOf(1L), employeeDto.getId());
        assertEquals("张三", employeeDto.getName());
    }

    /**
     * 测试 null 对象处理。
     */
    @Test
    public void testNullObjectHandling() {
        // 调用生成的 toDto 方法，传入 null
        EmployeeDto employeeDto = EmployeeDtoCopier.toDto(null);
        
        // 验证返回 null
        assertNull(employeeDto);
    }

    /**
     * 测试嵌套对象为 null 的情况。
     */
    @Test
    public void testNestedObjectWithNull() {
        // 创建源对象，地址为 null
        Employee employee = new Employee(2L, "李四", null);
        
        // 调用生成的 toDto 方法
        EmployeeDto employeeDto = EmployeeDtoCopier.toDto(employee);
        
        // 验证结果
        assertNotNull(employeeDto);
        assertEquals(Long.valueOf(2L), employeeDto.getId());
        assertEquals("李四", employeeDto.getName());
        
        // 验证嵌套对象为 null
        assertNull(employeeDto.getAddress());
    }

    /**
     * 测试反向拷贝基本字段。
     */
    @Test
    public void testReverseBasicFieldCopy() {
        // 创建目标对象
        EmployeeDto employeeDto = new EmployeeDto(3L, "王五", null);
        
        // 调用生成的 fromDto 方法
        Employee employee = EmployeeDtoCopier.fromDto(employeeDto);
        
        // 验证结果
        assertNotNull(employee);
        assertEquals(Long.valueOf(3L), employee.getId());
        assertEquals("王五", employee.getName());
        assertNull(employee.getAddress());
    }
}
