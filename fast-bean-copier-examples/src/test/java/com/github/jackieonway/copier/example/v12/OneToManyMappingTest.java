package com.github.jackieonway.copier.example.v12;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 * 一对多转换集成测试。
 * 测试单个源字段拆分映射到多个目标字段的功能。
 *
 * @author jackieonway
 * @since 1.2.0
 */
public class OneToManyMappingTest {

    /**
     * 测试一对多映射：fullName -> firstName + lastName
     */
    @Test
    public void testOneToManyMapping() {
        FullNameSource source = new FullNameSource();
        source.setId(1L);
        source.setFullName("John Doe");
        source.setEmail("john.doe@example.com");
        
        FullNameDto dto = FullNameDtoCopier.toDto(source);
        
        assertNotNull(dto);
        assertEquals(Long.valueOf(1L), dto.getId());
        assertEquals("John", dto.getFirstName());
        assertEquals("Doe", dto.getLastName());
        assertEquals("john.doe@example.com", dto.getEmail());
    }

    /**
     * 测试一对多映射：address -> city + country
     */
    @Test
    public void testOneToManyMappingAddress() {
        FullNameSource source = new FullNameSource();
        source.setAddress("New York, USA");
        
        FullNameDto dto = FullNameDtoCopier.toDto(source);
        
        assertNotNull(dto);
        assertEquals("New York", dto.getCity());
        assertEquals("USA", dto.getCountry());
    }

    /**
     * 测试一对多映射的 null 值处理
     */
    @Test
    public void testOneToManyMappingNullHandling() {
        FullNameSource source = new FullNameSource();
        source.setId(1L);
        source.setFullName(null);
        source.setAddress(null);
        
        FullNameDto dto = FullNameDtoCopier.toDto(source);
        
        assertNotNull(dto);
        assertEquals(Long.valueOf(1L), dto.getId());
        assertNull(dto.getFirstName());
        assertNull(dto.getLastName());
        assertNull(dto.getCity());
        assertNull(dto.getCountry());
    }

    /**
     * 测试一对多映射的空字符串处理
     */
    @Test
    public void testOneToManyMappingEmptyString() {
        FullNameSource source = new FullNameSource();
        source.setFullName("");
        source.setAddress("");
        
        FullNameDto dto = FullNameDtoCopier.toDto(source);
        
        assertNotNull(dto);
        assertEquals("", dto.getFirstName());
        assertEquals("", dto.getLastName());
        assertEquals("", dto.getCity());
        assertEquals("", dto.getCountry());
    }

    /**
     * 测试一对多映射的单个单词处理
     */
    @Test
    public void testOneToManyMappingSingleWord() {
        FullNameSource source = new FullNameSource();
        source.setFullName("John");
        source.setAddress("USA");
        
        FullNameDto dto = FullNameDtoCopier.toDto(source);
        
        assertNotNull(dto);
        assertEquals("John", dto.getFirstName());
        assertEquals("", dto.getLastName());
        assertEquals("USA", dto.getCity());
        assertEquals("", dto.getCountry());
    }

    /**
     * 测试反向拷贝（fromDto）
     */
    @Test
    public void testFromDto() {
        FullNameDto dto = new FullNameDto();
        dto.setId(1L);
        dto.setEmail("john.doe@example.com");
        
        FullNameSource source = FullNameDtoCopier.fromDto(dto);
        
        assertNotNull(source);
        assertEquals(Long.valueOf(1L), source.getId());
        assertEquals("john.doe@example.com", source.getEmail());
    }

    /**
     * 测试 null 源对象处理
     */
    @Test
    public void testNullSource() {
        FullNameDto dto = FullNameDtoCopier.toDto(null);
        assertNull(dto);
    }

    /**
     * 测试 null DTO 处理
     */
    @Test
    public void testNullDto() {
        FullNameSource source = FullNameDtoCopier.fromDto(null);
        assertNull(source);
    }
}