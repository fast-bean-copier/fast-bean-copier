package com.github.jackieonway.copier.processor;

import com.github.jackieonway.copier.example.MapHolder;
import com.github.jackieonway.copier.example.MapHolderDto;
import com.github.jackieonway.copier.example.MapHolderDtoCopier;
import com.github.jackieonway.copier.example.User;
import com.github.jackieonway.copier.example.UserDto;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.*;

/**
 * Map 深拷贝功能测试。
 */
public class MapDeepCopyTest {

    @Test
    public void shouldCopyPrimitiveValueMap() {
        Map<String, String> attributes = new HashMap<>();
        attributes.put("k1", "v1");
        attributes.put("k2", "v2");

        MapHolder holder = new MapHolder(1L, attributes, null);

        MapHolderDto dto = MapHolderDtoCopier.toDto(holder);

        assertNotNull(dto.getAttributes());
        assertEquals(attributes.size(), dto.getAttributes().size());
        assertEquals(attributes, dto.getAttributes());
        assertNotSame("目标 Map 应为新实例", attributes, dto.getAttributes());
    }

    @Test
    public void shouldDeepCopyNestedObjectValueMap() {
        User user = new User(1L, "Tom", "t@e.com", 20);
        Map<String, User> userMap = new HashMap<>();
        userMap.put("u1", user);

        MapHolder holder = new MapHolder(2L, null, userMap);

        MapHolderDto dto = MapHolderDtoCopier.toDto(holder);

        assertNotNull(dto.getUserMap());
        assertEquals(1, dto.getUserMap().size());

        UserDto copied = dto.getUserMap().get("u1");
        assertNotSame("嵌套对象应深拷贝", user, copied);
        assertEquals(user.getId(), copied.getId());
        assertEquals(user.getName(), copied.getName());
        assertEquals(user.getEmail(), copied.getEmail());
        assertEquals(user.getAge(), copied.getAge());
    }

    @Test
    public void shouldHandleNullAndEmptyMapAndNullValues() {
        MapHolder nullHolder = new MapHolder(3L, null, null);
        MapHolderDto nullDto = MapHolderDtoCopier.toDto(nullHolder);
        assertNull(nullDto.getAttributes());
        assertNull(nullDto.getUserMap());

        Map<String, String> emptyAttributes = new HashMap<>();
        Map<String, User> emptyUserMap = new HashMap<>();
        MapHolder emptyHolder = new MapHolder(4L, emptyAttributes, emptyUserMap);
        MapHolderDto emptyDto = MapHolderDtoCopier.toDto(emptyHolder);

        assertNotNull(emptyDto.getAttributes());
        assertTrue(emptyDto.getAttributes().isEmpty());

        assertNotNull(emptyDto.getUserMap());
        assertTrue(emptyDto.getUserMap().isEmpty());

        Map<String, User> mapWithNull = new HashMap<>();
        mapWithNull.put("nullUser", null);
        MapHolder nullValueHolder = new MapHolder(5L, null, mapWithNull);
        MapHolderDto nullValueDto = MapHolderDtoCopier.toDto(nullValueHolder);
        assertNotNull(nullValueDto.getUserMap());
        assertTrue(nullValueDto.getUserMap().containsKey("nullUser"));
        assertNull(nullValueDto.getUserMap().get("nullUser"));
    }
}


