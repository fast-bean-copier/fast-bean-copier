package com.github.jackieonway.copier.processor;

import com.github.jackieonway.copier.example.ArrayHolder;
import com.github.jackieonway.copier.example.ArrayHolderDto;
import com.github.jackieonway.copier.example.ArrayHolderDtoCopier;
import com.github.jackieonway.copier.example.User;
import com.github.jackieonway.copier.example.UserDto;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * 数组深拷贝功能测试。
 */
public class ArrayDeepCopyTest {

    @Test
    public void shouldCopyPrimitiveAndStringArray() {
        String[] tags = new String[]{"a", "b"};
        int[] scores = new int[]{1, 2, 3};
        ArrayHolder holder = new ArrayHolder(1L, tags, scores, null);

        ArrayHolderDto dto = ArrayHolderDtoCopier.toDto(holder);

        assertNotNull(dto.getTags());
        assertArrayEquals(tags, dto.getTags());
        assertNotSame("String 数组应为新实例", tags, dto.getTags());

        assertNotNull(dto.getScores());
        assertArrayEquals(scores, dto.getScores());
        assertNotSame("基本类型数组应为新实例", scores, dto.getScores());
    }

    @Test
    public void shouldDeepCopyUserArray() {
        User[] users = new User[]{
                new User(1L, "Tom", "t@e.com", 20),
                new User(2L, "Jerry", "j@e.com", 22)
        };
        ArrayHolder holder = new ArrayHolder(2L, null, null, users);

        ArrayHolderDto dto = ArrayHolderDtoCopier.toDto(holder);

        assertNotNull(dto.getUsers());
        assertEquals(users.length, dto.getUsers().length);

        for (int i = 0; i < users.length; i++) {
            User source = users[i];
            UserDto copied = dto.getUsers()[i];
            assertNotSame("嵌套对象元素应深拷贝", source, copied);
            assertEquals(source.getId(), copied.getId());
            assertEquals(source.getName(), copied.getName());
            assertEquals(source.getEmail(), copied.getEmail());
            assertEquals(source.getAge(), copied.getAge());
        }
    }

    @Test
    public void shouldHandleNullAndEmptyArray() {
        ArrayHolder nullHolder = new ArrayHolder(3L, null, null, null);
        ArrayHolderDto nullDto = ArrayHolderDtoCopier.toDto(nullHolder);
        assertNull(nullDto.getTags());
        assertNull(nullDto.getScores());
        assertNull(nullDto.getUsers());

        ArrayHolder emptyHolder = new ArrayHolder(4L, new String[0], new int[0], new User[0]);
        ArrayHolderDto emptyDto = ArrayHolderDtoCopier.toDto(emptyHolder);

        assertNotNull(emptyDto.getTags());
        assertEquals(0, emptyDto.getTags().length);

        assertNotNull(emptyDto.getScores());
        assertEquals(0, emptyDto.getScores().length);

        assertNotNull(emptyDto.getUsers());
        assertEquals(0, emptyDto.getUsers().length);
    }
}


