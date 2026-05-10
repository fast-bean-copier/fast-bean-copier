package com.github.jackieonway.copier.example;

import com.github.jackieonway.copier.example.v150.MapUserDto;
import com.github.jackieonway.copier.example.v150.MapUserDtoMapCopier;
import org.junit.Test;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * v1.5.0 MapCopier componentModel 测试。
 *
 * <p>验证 DEFAULT componentModel 生成静态方法。
 *
 * @author jackieonway
 * @since 1.5.0
 */
public class V150MapComponentModelTest {

    // ========== DEFAULT：生成静态方法 ==========

    @Test
    public void testDefault_toMap_isStaticMethod() throws NoSuchMethodException {
        Method toMap = MapUserDtoMapCopier.class.getMethod("toMap", MapUserDto.class);
        assertTrue("toMap 应为静态方法", Modifier.isStatic(toMap.getModifiers()));
    }

    @Test
    public void testDefault_fromMap_isStaticMethod() throws NoSuchMethodException {
        Method fromMap = MapUserDtoMapCopier.class.getMethod("fromMap", Map.class);
        assertTrue("fromMap 应为静态方法", Modifier.isStatic(fromMap.getModifiers()));
    }

    @Test
    public void testDefault_toMapList_isStaticMethod() throws NoSuchMethodException {
        Method toMapList = MapUserDtoMapCopier.class.getMethod("toMapList", java.util.List.class);
        assertTrue("toMapList 应为静态方法", Modifier.isStatic(toMapList.getModifiers()));
    }

    @Test
    public void testDefault_fromMapList_isStaticMethod() throws NoSuchMethodException {
        Method fromMapList = MapUserDtoMapCopier.class.getMethod("fromMapList", java.util.List.class);
        assertTrue("fromMapList 应为静态方法", Modifier.isStatic(fromMapList.getModifiers()));
    }

    // ========== 静态方法可直接调用 ==========

    @Test
    public void testDefault_staticCall_works() {
        MapUserDto dto = new MapUserDto();
        dto.setId(1L);
        dto.setName("Alice");

        // 直接通过类名调用静态方法
        Map<String, Object> map = MapUserDtoMapCopier.toMap(dto);
        assertNotNull(map);
        assertEquals(1L, map.get("id"));
    }

    @Test
    public void testDefault_fromMap_staticCall_works() {
        Map<String, Object> map = new HashMap<>();
        map.put("id", 1L);
        map.put("name", "Alice");

        MapUserDto result = MapUserDtoMapCopier.fromMap(map);
        assertNotNull(result);
        assertEquals(Long.valueOf(1L), result.getId());
    }
}
