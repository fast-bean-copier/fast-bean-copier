package com.github.jackieonway.copier.example.converter;

import com.github.jackieonway.copier.example.v12.Person;
import com.github.jackieonway.copier.example.v12.PersonDto;
import com.github.jackieonway.copier.example.v12.PersonDtoCopier;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * TypeConverter 集成测试。
 * 测试自定义转换器的使用，包括：
 * 1. uses 属性配合 qualifiedByName 使用转换器方法
 * 2. 表达式映射（expression）
 * 3. null 值处理
 * 4. 集合方法与转换器的配合
 *
 * @author jackieonway
 * @since 1.2.0
 */
public class TypeConverterIntegrationTest {

    /**
     * 测试 uses 属性配合 qualifiedByName 使用转换器方法
     */
    @Test
    public void testUsesAttribute_withQualifiedByName() {
        Person person = new Person();
        person.setId(1L);
        person.setFirstName("John");
        person.setLastName("Doe");
        person.setStatus(1);
        person.setAddress("123 Main St");
        person.setCity("New York");
        person.setCountry("USA");
        
        PersonDto dto = PersonDtoCopier.toDto(person);
        
        assertNotNull(dto);
        assertEquals(Long.valueOf(1L), dto.getId());
        assertEquals("John Doe", dto.getFullName());
        assertEquals("ACTIVE", dto.getStatusName());
        assertEquals("123 Main St", dto.getAddress());
        assertEquals("New York, USA", dto.getLocation());
    }

    /**
     * 测试 qualifiedByName 方法处理不同状态值
     */
    @Test
    public void testUsesAttribute_differentStatusValues() {
        Person person1 = new Person();
        person1.setStatus(0);
        person1.setFirstName("Test");
        person1.setLastName("User");
        PersonDto dto1 = PersonDtoCopier.toDto(person1);
        assertEquals("INACTIVE", dto1.getStatusName());
        
        Person person2 = new Person();
        person2.setStatus(1);
        person2.setFirstName("Test");
        person2.setLastName("User");
        PersonDto dto2 = PersonDtoCopier.toDto(person2);
        assertEquals("ACTIVE", dto2.getStatusName());
        
        Person person3 = new Person();
        person3.setStatus(2);
        person3.setFirstName("Test");
        person3.setLastName("User");
        PersonDto dto3 = PersonDtoCopier.toDto(person3);
        assertEquals("PENDING", dto3.getStatusName());
        
        Person person4 = new Person();
        person4.setStatus(99);
        person4.setFirstName("Test");
        person4.setLastName("User");
        PersonDto dto4 = PersonDtoCopier.toDto(person4);
        assertEquals("UNKNOWN", dto4.getStatusName());
    }

    /**
     * 测试 qualifiedByName 方法处理 null 值
     */
    @Test
    public void testUsesAttribute_nullStatus() {
        Person person = new Person();
        person.setId(2L);
        person.setFirstName("Jane");
        person.setLastName("Smith");
        person.setStatus(null);
        
        PersonDto dto = PersonDtoCopier.toDto(person);
        
        assertNotNull(dto);
        assertEquals("Jane Smith", dto.getFullName());
        assertEquals("UNKNOWN", dto.getStatusName());
    }

    /**
     * 测试多对一映射（expression）
     */
    @Test
    public void testExpressionMapping_multipleSourceFields() {
        Person person = new Person();
        person.setId(3L);
        person.setFirstName("Alice");
        person.setLastName("Johnson");
        person.setCity("Los Angeles");
        person.setCountry("USA");
        
        PersonDto dto = PersonDtoCopier.toDto(person);
        
        assertNotNull(dto);
        assertEquals("Alice Johnson", dto.getFullName());
        assertEquals("Los Angeles, USA", dto.getLocation());
    }

    /**
     * 测试表达式映射处理 null 值
     */
    @Test
    public void testExpressionMapping_nullHandling() {
        Person person = new Person();
        person.setId(4L);
        person.setFirstName(null);
        person.setLastName("Brown");
        person.setCity("Chicago");
        person.setCountry(null);
        
        PersonDto dto = PersonDtoCopier.toDto(person);
        
        assertNotNull(dto);
        assertEquals("null Brown", dto.getFullName());
        assertEquals("Chicago, null", dto.getLocation());
    }

    /**
     * 测试同时使用 uses、qualifiedByName、expression 的复杂场景
     */
    @Test
    public void testComplexScenario() {
        Person person = new Person();
        person.setId(200L);
        person.setFirstName("Bob");
        person.setLastName("Wilson");
        person.setStatus(1);
        person.setAddress("456 Oak Ave");
        person.setCity("Seattle");
        person.setCountry("USA");
        
        PersonDto dto = PersonDtoCopier.toDto(person);
        
        assertNotNull(dto);
        assertEquals("Bob Wilson", dto.getFullName());
        assertEquals("ACTIVE", dto.getStatusName());
        assertEquals("Seattle, USA", dto.getLocation());
    }

    /**
     * 测试集合方法与转换器的配合
     */
    @Test
    public void testCollectionMethodsWithConverter() {
        Person person1 = new Person();
        person1.setId(1L);
        person1.setFirstName("Alice");
        person1.setLastName("Smith");
        person1.setStatus(1);
        
        Person person2 = new Person();
        person2.setId(2L);
        person2.setFirstName("Bob");
        person2.setLastName("Jones");
        person2.setStatus(0);
        
        java.util.List<Person> persons = java.util.Arrays.asList(person1, person2);
        java.util.List<PersonDto> dtos = PersonDtoCopier.toDtoList(persons);
        
        assertNotNull(dtos);
        assertEquals(2, dtos.size());
        assertEquals("Alice Smith", dtos.get(0).getFullName());
        assertEquals("ACTIVE", dtos.get(0).getStatusName());
        assertEquals("Bob Jones", dtos.get(1).getFullName());
        assertEquals("INACTIVE", dtos.get(1).getStatusName());
    }

    /**
     * 测试 UnaryOperator 定制功能与转换器的配合
     */
    @Test
    public void testUnaryOperatorWithConverter() {
        Person person = new Person();
        person.setId(1L);
        person.setFirstName("John");
        person.setLastName("Doe");
        person.setStatus(1);
        person.setAddress("123 Main St");
        
        PersonDto dto = PersonDtoCopier.toDto(person, result -> {
            result.setAddress(result.getAddress().toUpperCase());
            return result;
        });
        
        assertNotNull(dto);
        assertEquals("123 MAIN ST", dto.getAddress());
        assertEquals("John Doe", dto.getFullName());
        assertEquals("ACTIVE", dto.getStatusName());
    }

    /**
     * 测试整个对象为 null
     */
    @Test
    public void testNullSource() {
        PersonDto dto = PersonDtoCopier.toDto(null);
        assertNull(dto);
    }

    /**
     * 测试反向转换（fromDto）
     */
    @Test
    public void testReverseConversion() {
        PersonDto dto = new PersonDto();
        dto.setId(1L);
        dto.setAddress("456 Oak Ave");
        
        Person person = PersonDtoCopier.fromDto(dto);
        
        assertNotNull(person);
        assertEquals(Long.valueOf(1L), person.getId());
        assertEquals("456 Oak Ave", person.getAddress());
    }
}
