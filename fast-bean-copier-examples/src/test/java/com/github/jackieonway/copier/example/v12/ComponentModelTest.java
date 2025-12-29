package com.github.jackieonway.copier.example.v12;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 * 依赖注入支持集成测试。
 * 测试 DEFAULT ComponentModel 下的代码生成。
 *
 * @author jackieonway
 * @since 1.2.0
 */
public class ComponentModelTest {

    /**
     * 测试 DEFAULT 模式代码生成
     * DEFAULT 模式应该生成静态方法
     */
    @Test
    public void testDefaultModeCodeGeneration() {
        // PersonDtoCopier 使用 DEFAULT 模式（默认）
        Person person = new Person();
        person.setId(1L);
        person.setFirstName("John");
        person.setLastName("Doe");
        
        // 验证可以通过静态方法调用
        PersonDto dto = PersonDtoCopier.toDto(person);
        
        assertNotNull(dto);
        assertEquals(Long.valueOf(1L), dto.getId());
        assertEquals("John Doe", dto.getFullName());
    }

    /**
     * 测试静态方法签名
     */
    @Test
    public void testStaticMethodSignature() {
        // 验证 DEFAULT 模式生成的是静态方法
        Person person = new Person();
        person.setId(1L);
        person.setFirstName("Test");
        person.setLastName("User");
        
        // 这些调用证明方法是静态的
        PersonDto dto1 = PersonDtoCopier.toDto(person);
        PersonDto dto2 = PersonDtoCopier.toDto(person, result -> {
            result.setFullName(result.getFullName().toUpperCase());
            return result;
        });
        
        assertNotNull(dto1);
        assertNotNull(dto2);
        assertEquals("Test User", dto1.getFullName());
        assertEquals("TEST USER", dto2.getFullName());
    }

    /**
     * 测试集合方法的静态签名
     */
    @Test
    public void testStaticCollectionMethods() {
        Person person = new Person();
        person.setId(1L);
        person.setFirstName("John");
        person.setLastName("Doe");
        
        java.util.List<Person> persons = java.util.Arrays.asList(person);
        
        // 验证集合方法也是静态的
        java.util.List<PersonDto> dtos = PersonDtoCopier.toDtoList(persons);
        
        assertNotNull(dtos);
        assertEquals(1, dtos.size());
        assertEquals("John Doe", dtos.get(0).getFullName());
    }

    /**
     * 测试反向拷贝的静态方法
     */
    @Test
    public void testStaticFromDtoMethod() {
        PersonDto dto = new PersonDto();
        dto.setId(1L);
        dto.setAddress("123 Main St");
        
        // 验证 fromDto 也是静态方法
        Person person = PersonDtoCopier.fromDto(dto);
        
        assertNotNull(person);
        assertEquals(Long.valueOf(1L), person.getId());
        assertEquals("123 Main St", person.getAddress());
    }

    /**
     * 测试 null 值处理
     */
    @Test
    public void testNullHandling() {
        // 测试 null 源对象
        PersonDto dto = PersonDtoCopier.toDto(null);
        assertNull(dto);
        
        // 测试 null DTO
        Person person = PersonDtoCopier.fromDto(null);
        assertNull(person);
    }

    /**
     * 测试代码生成的正确性
     * 通过编译即表示代码生成正确
     */
    @Test
    public void testCodeGenerationCorrectness() {
        // 这个测试主要验证生成的代码能够编译通过
        // 并且功能正确
        
        Person person = new Person();
        person.setId(1L);
        person.setFirstName("Code");
        person.setLastName("Generation");
        person.setAddress("Test Address");
        
        PersonDto dto = PersonDtoCopier.toDto(person);
        
        assertNotNull(dto);
        assertEquals(Long.valueOf(1L), dto.getId());
        assertEquals("Code Generation", dto.getFullName());
        assertEquals("Test Address", dto.getAddress());
        
        // 测试反向转换
        Person converted = PersonDtoCopier.fromDto(dto);
        assertNotNull(converted);
        assertEquals(Long.valueOf(1L), converted.getId());
        assertEquals("Test Address", converted.getAddress());
    }
}