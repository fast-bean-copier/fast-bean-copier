package com.github.jackieonway.copier.example;

import com.github.jackieonway.copier.example.v150.MapUser;
import com.github.jackieonway.copier.example.v150.MapUserDto;
import com.github.jackieonway.copier.example.v150.MapUserDtoCopier;
import com.github.jackieonway.copier.example.v150.MapUserDtoMapCopier;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * v1.5.0 双向转换与 @CopyTarget 共存测试。
 *
 * <p>验证同时使用 @CopyToMap/@CopyFromMap 以及与 @CopyTarget 共存时，
 * 生成两个独立的 Copier 类，互不影响。
 *
 * @author jackieonway
 * @since 1.5.0
 */
public class V150MapAndBeanCopierCoexistenceTest {

    // ========== @CopyTarget 生成的 BeanCopier 正常工作 ==========

    @Test
    public void testBeanCopier_toDto_works() {
        MapUser source = new MapUser(1L, "Alice", 30, "alice@example.com");

        MapUserDto result = MapUserDtoCopier.toDto(source);

        assertNotNull(result);
        assertEquals(Long.valueOf(1L), result.getId());
        assertEquals("Alice", result.getName());
        assertEquals(Integer.valueOf(30), result.getAge());
        assertEquals("alice@example.com", result.getEmail());
    }

    @Test
    public void testBeanCopier_fromDto_works() {
        MapUserDto dto = new MapUserDto();
        dto.setId(2L);
        dto.setName("Bob");
        dto.setAge(25);
        dto.setEmail("bob@example.com");

        MapUser result = MapUserDtoCopier.fromDto(dto);

        assertNotNull(result);
        assertEquals(Long.valueOf(2L), result.getId());
        assertEquals("Bob", result.getName());
    }

    // ========== MapCopier 独立生成，互不影响 ==========

    @Test
    public void testMapCopier_toMap_works() {
        MapUserDto dto = new MapUserDto();
        dto.setId(1L);
        dto.setName("Alice");
        dto.setEmail("alice@example.com");

        Map<String, Object> map = MapUserDtoMapCopier.toMap(dto);

        assertNotNull(map);
        assertEquals(1L, map.get("id"));
        assertEquals("Alice", map.get("name"));
        // mapKey = "userEmail"
        assertEquals("alice@example.com", map.get("userEmail"));
        assertFalse(map.containsKey("email"));
    }

    @Test
    public void testMapCopier_fromMap_works() {
        Map<String, Object> map = new HashMap<>();
        map.put("id", 1L);
        map.put("name", "Alice");
        map.put("userEmail", "alice@example.com");

        MapUserDto result = MapUserDtoMapCopier.fromMap(map);

        assertNotNull(result);
        assertEquals(Long.valueOf(1L), result.getId());
        assertEquals("Alice", result.getName());
        assertEquals("alice@example.com", result.getEmail());
    }

    // ========== @CopyField(mapKey) 仅在 Map 体系中生效 ==========

    @Test
    public void testMapKey_onlyAffectsMapCopier_notBeanCopier() {
        // BeanCopier 中 email 字段正常映射（不受 mapKey 影响）
        MapUser source = new MapUser(1L, "Alice", 30, "alice@example.com");
        MapUserDto beanResult = MapUserDtoCopier.toDto(source);
        assertEquals("alice@example.com", beanResult.getEmail());

        // MapCopier 中 email 字段使用 mapKey = "userEmail"
        Map<String, Object> mapResult = MapUserDtoMapCopier.toMap(beanResult);
        assertTrue("MapCopier 应使用 mapKey 'userEmail'", mapResult.containsKey("userEmail"));
        assertFalse("MapCopier 不应使用原字段名 'email'", mapResult.containsKey("email"));
    }

    // ========== 双向转换一致性 ==========

    @Test
    public void testRoundTrip_toMapThenFromMap_preservesData() {
        MapUserDto original = new MapUserDto();
        original.setId(1L);
        original.setName("Alice");
        original.setAge(30);
        original.setEmail("alice@example.com");

        Map<String, Object> map = MapUserDtoMapCopier.toMap(original);
        MapUserDto restored = MapUserDtoMapCopier.fromMap(map);

        assertNotNull(restored);
        assertEquals(original.getId(), restored.getId());
        assertEquals(original.getName(), restored.getName());
        assertEquals(original.getAge(), restored.getAge());
        assertEquals(original.getEmail(), restored.getEmail());
    }

    @Test
    public void testRoundTrip_beanToDtoToMapAndBack() {
        MapUser user = new MapUser(1L, "Alice", 30, "alice@example.com");

        // Bean -> DTO (BeanCopier)
        MapUserDto dto = MapUserDtoCopier.toDto(user);
        // DTO -> Map (MapCopier)
        Map<String, Object> map = MapUserDtoMapCopier.toMap(dto);
        // Map -> DTO (MapCopier)
        MapUserDto restored = MapUserDtoMapCopier.fromMap(map);
        // DTO -> Bean (BeanCopier)
        MapUser restoredUser = MapUserDtoCopier.fromDto(restored);

        assertNotNull(restoredUser);
        assertEquals(user.getId(), restoredUser.getId());
        assertEquals(user.getName(), restoredUser.getName());
        assertEquals(user.getAge(), restoredUser.getAge());
        assertEquals(user.getEmail(), restoredUser.getEmail());
    }
}
