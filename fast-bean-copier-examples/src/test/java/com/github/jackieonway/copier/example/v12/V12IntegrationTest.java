package com.github.jackieonway.copier.example.v12;

import org.junit.Test;
import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.List;

/**
 * v1.2 功能集成测试。
 *
 * @author jackieonway
 * @since 1.2.0
 */
public class V12IntegrationTest {

    /**
     * 测试多对一映射：firstName + lastName -> fullName
     */
    @Test
    public void testManyToOneMapping() {
        Person person = new Person();
        person.setId(1L);
        person.setFirstName("John");
        person.setLastName("Doe");
        
        PersonDto dto = PersonDtoCopier.toDto(person);
        
        assertNotNull(dto);
        assertEquals(Long.valueOf(1L), dto.getId());
        assertEquals("John Doe", dto.getFullName());
    }

    /**
     * 测试多对一映射：city + country -> location
     */
    @Test
    public void testManyToOneMappingLocation() {
        Person person = new Person();
        person.setCity("New York");
        person.setCountry("USA");
        
        PersonDto dto = PersonDtoCopier.toDto(person);
        
        assertNotNull(dto);
        assertEquals("New York, USA", dto.getLocation());
    }

    /**
     * 测试自定义转换器的具名方法：status -> statusName
     */
    @Test
    public void testQualifiedByNameMapping() {
        Person person = new Person();
        person.setStatus(1);
        
        PersonDto dto = PersonDtoCopier.toDto(person);
        
        assertNotNull(dto);
        assertEquals("ACTIVE", dto.getStatusName());
    }

    /**
     * 测试自定义转换器的具名方法：status = 0 -> INACTIVE
     */
    @Test
    public void testQualifiedByNameMappingInactive() {
        Person person = new Person();
        person.setStatus(0);
        
        PersonDto dto = PersonDtoCopier.toDto(person);
        
        assertNotNull(dto);
        assertEquals("INACTIVE", dto.getStatusName());
    }

    /**
     * 测试简单字段映射
     */
    @Test
    public void testSimpleFieldMapping() {
        Person person = new Person();
        person.setAddress("123 Main St");
        
        PersonDto dto = PersonDtoCopier.toDto(person);
        
        assertNotNull(dto);
        assertEquals("123 Main St", dto.getAddress());
    }

    /**
     * 测试 null 值处理
     */
    @Test
    public void testNullHandling() {
        PersonDto dto = PersonDtoCopier.toDto(null);
        assertNull(dto);
    }

    /**
     * 测试函数式定制拷贝
     */
    @Test
    public void testFunctionalCustomization() {
        Person person = new Person();
        person.setId(1L);
        person.setFirstName("John");
        person.setLastName("Doe");
        
        PersonDto dto = PersonDtoCopier.toDto(person, result -> {
            result.setFullName(result.getFullName().toUpperCase());
            return result;
        });
        
        assertNotNull(dto);
        assertEquals("JOHN DOE", dto.getFullName());
    }

    /**
     * 测试集合方法的函数式定制拷贝
     */
    @Test
    public void testListFunctionalCustomization() {
        Person person1 = new Person();
        person1.setId(1L);
        person1.setFirstName("John");
        person1.setLastName("Doe");
        
        Person person2 = new Person();
        person2.setId(2L);
        person2.setFirstName("Jane");
        person2.setLastName("Smith");
        
        List<Person> persons = Arrays.asList(person1, person2);
        
        List<PersonDto> dtos = PersonDtoCopier.toDtoList(persons, result -> {
            result.setFullName(result.getFullName().toUpperCase());
            return result;
        });
        
        assertNotNull(dtos);
        assertEquals(2, dtos.size());
        assertEquals("JOHN DOE", dtos.get(0).getFullName());
        assertEquals("JANE SMITH", dtos.get(1).getFullName());
    }

    /**
     * 测试反向拷贝（fromDto）
     */
    @Test
    public void testFromDto() {
        PersonDto dto = new PersonDto();
        dto.setId(1L);
        dto.setAddress("123 Main St");
        
        Person person = PersonDtoCopier.fromDto(dto);
        
        assertNotNull(person);
        assertEquals(Long.valueOf(1L), person.getId());
        assertEquals("123 Main St", person.getAddress());
    }
}
