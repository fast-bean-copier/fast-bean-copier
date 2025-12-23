package com.github.jackieonway.copier.processor;

import com.github.jackieonway.copier.example.User;
import com.github.jackieonway.copier.example.UserDto;
import com.github.jackieonway.copier.example.processor.RawContainer;
import com.github.jackieonway.copier.example.processor.RawContainerDto;
import com.github.jackieonway.copier.example.processor.RawContainerDtoCopier;
import com.github.jackieonway.copier.example.processor.WildcardContainer;
import com.github.jackieonway.copier.example.processor.WildcardContainerDto;
import com.github.jackieonway.copier.example.processor.WildcardContainerDtoCopier;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * 原始类型和通配符集合的处理测试。
 */
public class RawAndWildcardCopyTest {

    @Test
    public void shouldFallbackToShallowCopyForRawAndUnknownWildcards() {
        List rawList = new ArrayList();
        rawList.add("raw");
        Map rawMap = new HashMap();
        rawMap.put("k", 1);

        List<?> wildcardList = Collections.singletonList("unknown");
        Map<String, ?> wildcardMap = Collections.singletonMap("v", "value");

        RawContainer source = new RawContainer();
        source.setRawList(rawList);
        source.setRawMap(rawMap);
        source.setWildcardList(wildcardList);
        source.setWildcardMap(wildcardMap);

        RawContainerDto dto = RawContainerDtoCopier.toDto(source);

        assertSame("raw list should be assigned directly for raw type", rawList, dto.getRawList());
        assertSame("raw map should be assigned directly for raw type", rawMap, dto.getRawMap());
        assertSame("wildcard list should not be deep copied", wildcardList, dto.getWildcardList());
        assertSame("wildcard map should not be deep copied", wildcardMap, dto.getWildcardMap());
    }

    @Test
    public void shouldDeepCopyExtendsWildcardCollections() {
        User user = new User(1L, "Tom", "tom@test.com", 20);
        List<User> users = Collections.singletonList(user);
        Map<String, User> userMap = Collections.singletonMap("u1", user);

        WildcardContainer container = new WildcardContainer();
        container.setUsers(users);
        container.setUserMap(userMap);

        WildcardContainerDto dto = WildcardContainerDtoCopier.toDto(container);

        assertNotNull(dto.getUsers());
        assertEquals(1, dto.getUsers().size());
        UserDto copied = dto.getUsers().get(0);
        assertNotSame(user, copied);
        assertEquals(user.getId(), copied.getId());
        assertEquals(user.getName(), copied.getName());

        assertNotNull(dto.getUserMap());
        assertEquals(1, dto.getUserMap().size());
        UserDto copiedFromMap = dto.getUserMap().get("u1");
        assertNotSame(user, copiedFromMap);
        assertEquals(user.getEmail(), copiedFromMap.getEmail());
    }
}

