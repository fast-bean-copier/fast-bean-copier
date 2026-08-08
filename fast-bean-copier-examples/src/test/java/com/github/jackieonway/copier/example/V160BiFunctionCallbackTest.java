package com.github.jackieonway.copier.example;

import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.BiFunction;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;

/**
 * v1.6.0 BiFunction 回调专项测试
 *
 * @since 1.6.0
 */
public class V160BiFunctionCallbackTest {

    // ========== toDto + BiFunction 测试 ==========

    @Test
    public void testToDto_withBiFunction_readSourceAndModifyTarget() {
        Account source = new Account(1L, "john", "p1", "john@test.com");

        BiFunction<Account, AccountDto, AccountDto> postProcessor = (s, t) -> {
            t.setUsername(s.getUsername() + "_modified");
            return t;
        };

        AccountDto result = AccountDtoCopier.toDto(source, null, postProcessor);

        assertNotNull(result);
        assertEquals(Long.valueOf(1L), result.getId());
        assertEquals("john_modified", result.getUsername());
    }

    @Test
    public void testToDto_withBiFunction_returnNewInstance() {
        Account source = new Account(1L, "john", "p1", "john@test.com");

        BiFunction<Account, AccountDto, AccountDto> postProcessor = (s, t) -> {
            AccountDto newDto = new AccountDto();
            newDto.setId(s.getId());
            newDto.setUsername("custom_" + s.getUsername());
            return newDto;
        };

        AccountDto result = AccountDtoCopier.toDto(source, null, postProcessor);

        assertNotNull(result);
        assertEquals("custom_john", result.getUsername());
    }

    @Test
    public void testToDto_withBiFunction_returnOriginalInstance() {
        Account source = new Account(1L, "john", "p1", "john@test.com");

        BiFunction<Account, AccountDto, AccountDto> postProcessor = (s, t) -> {
            t.setUsername("inplace_modified");
            return t;
        };

        AccountDto result = AccountDtoCopier.toDto(source, null, postProcessor);

        assertNotNull(result);
        assertEquals("inplace_modified", result.getUsername());
    }

    @Test
    public void testToDto_withNullBiFunction() {
        Account source = new Account(1L, "john", "p1", "john@test.com");

        AccountDto result = AccountDtoCopier.toDto(source, null, null);

        assertNotNull(result);
        assertEquals(Long.valueOf(1L), result.getId());
        assertEquals("john", result.getUsername());
    }

    @Test
    public void testToDto_withPreProcessorAndBiFunction() {
        Account source = new Account(1L, "john", "p1", "john@test.com");

        java.util.function.UnaryOperator<Account> preProcessor = (s) -> {
            s.setUsername("pre_" + s.getUsername());
            return s;
        };

        BiFunction<Account, AccountDto, AccountDto> postProcessor = (s, t) -> {
            t.setUsername(s.getUsername() + "_post");
            return t;
        };

        AccountDto result = AccountDtoCopier.toDto(source, preProcessor, postProcessor);

        assertNotNull(result);
        assertEquals("pre_john_post", result.getUsername());
    }

    // ========== fromDto + BiFunction 测试 ==========

    @Test
    public void testFromDto_withBiFunction_readTargetAndModifyResult() {
        AccountDto source = new AccountDto(1L, "john", "p1", "john@test.com");

        BiFunction<AccountDto, Account, Account> postProcessor = (s, t) -> {
            t.setUsername(s.getUsername() + "_converted");
            return t;
        };

        Account result = AccountDtoCopier.fromDto(source, null, postProcessor);

        assertNotNull(result);
        assertEquals(Long.valueOf(1L), result.getId());
        assertEquals("john_converted", result.getUsername());
    }

    @Test
    public void testFromDto_withNullBiFunction() {
        AccountDto source = new AccountDto(1L, "john", "p1", "john@test.com");

        Account result = AccountDtoCopier.fromDto(source, null, null);

        assertNotNull(result);
        assertEquals(Long.valueOf(1L), result.getId());
        assertEquals("john", result.getUsername());
    }

    // ========== 集合方法 + BiFunction 测试 ==========

    @Test
    public void testToDtoList_withBiFunction() {
        List<Account> sources = new ArrayList<>();
        sources.add(new Account(1L, "john", "p1", "john@test.com"));
        sources.add(new Account(2L, "jane", "p2", "jane@test.com"));

        BiFunction<List<Account>, List<AccountDto>, List<AccountDto>> postProcessor = (s, t) -> {
            t.get(0).setUsername("first_" + t.get(0).getUsername());
            return t;
        };

        List<AccountDto> result = AccountDtoCopier.toDtoList(sources, null, postProcessor);

        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("first_john", result.get(0).getUsername());
        assertEquals("jane", result.get(1).getUsername());
    }

    @Test
    public void testFromDtoList_withBiFunction() {
        List<AccountDto> sources = new ArrayList<>();
        sources.add(new AccountDto(1L, "john", "p1", "john@test.com"));
        sources.add(new AccountDto(2L, "jane", "p2", "jane@test.com"));

        BiFunction<List<AccountDto>, List<Account>, List<Account>> postProcessor = (s, t) -> {
            t.get(0).setUsername("converted_" + t.get(0).getUsername());
            return t;
        };

        List<Account> result = AccountDtoCopier.fromDtoList(sources, null, postProcessor);

        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("converted_john", result.get(0).getUsername());
    }

    @Test
    public void testToDtoSet_withBiFunction() {
        Set<Account> sources = new LinkedHashSet<>();
        sources.add(new Account(1L, "john", "p1", "john@test.com"));
        sources.add(new Account(2L, "jane", "p2", "jane@test.com"));

        BiFunction<Set<Account>, Set<AccountDto>, Set<AccountDto>> postProcessor = (s, t) -> {
            return t;
        };

        Set<AccountDto> result = AccountDtoCopier.toDtoSet(sources, null, postProcessor);

        assertNotNull(result);
        assertEquals(2, result.size());
    }

    @Test
    public void testFromDtoSet_withBiFunction() {
        Set<AccountDto> sources = new LinkedHashSet<>();
        sources.add(new AccountDto(1L, "john", "p1", "john@test.com"));
        sources.add(new AccountDto(2L, "jane", "p2", "jane@test.com"));

        BiFunction<Set<AccountDto>, Set<Account>, Set<Account>> postProcessor = (s, t) -> {
            return t;
        };

        Set<Account> result = AccountDtoCopier.fromDtoSet(sources, null, postProcessor);

        assertNotNull(result);
        assertEquals(2, result.size());
    }

    @Test
    public void testToDtoMap_withBiFunction() {
        Map<String, Account> sources = new LinkedHashMap<>();
        sources.put("a", new Account(1L, "john", "p1", "john@test.com"));
        sources.put("b", new Account(2L, "jane", "p2", "jane@test.com"));

        BiFunction<Map<String, Account>, Map<String, AccountDto>, Map<String, AccountDto>> postProcessor = (s, t) -> {
            return t;
        };

        Map<String, AccountDto> result = AccountDtoCopier.toDtoMap(sources, null, postProcessor);

        assertNotNull(result);
        assertEquals(2, result.size());
    }

    @Test
    public void testFromDtoMap_withBiFunction() {
        Map<String, AccountDto> sources = new LinkedHashMap<>();
        sources.put("a", new AccountDto(1L, "john", "p1", "john@test.com"));
        sources.put("b", new AccountDto(2L, "jane", "p2", "jane@test.com"));

        BiFunction<Map<String, AccountDto>, Map<String, Account>, Map<String, Account>> postProcessor = (s, t) -> {
            return t;
        };

        Map<String, Account> result = AccountDtoCopier.fromDtoMap(sources, null, postProcessor);

        assertNotNull(result);
        assertEquals(2, result.size());
    }

    @Test
    public void testToDtoArray_withBiFunction() {
        Account[] sources = new Account[] {
                new Account(1L, "john", "p1", "john@test.com"),
                new Account(2L, "jane", "p2", "jane@test.com")
        };

        BiFunction<Account[], AccountDto[], AccountDto[]> postProcessor = (s, t) -> {
            return t;
        };

        AccountDto[] result = AccountDtoCopier.toDtoArray(sources, null, postProcessor);

        assertNotNull(result);
        assertEquals(2, result.length);
    }

    @Test
    public void testFromArray_withBiFunction() {
        AccountDto[] sources = new AccountDto[] {
                new AccountDto(1L, "john", "p1", "john@test.com"),
                new AccountDto(2L, "jane", "p2", "jane@test.com")
        };

        BiFunction<AccountDto[], Account[], Account[]> postProcessor = (s, t) -> {
            return t;
        };

        Account[] result = AccountDtoCopier.fromDtoArray(sources, null, postProcessor);

        assertNotNull(result);
        assertEquals(2, result.length);
    }
}