package com.github.jackieonway.copier.example;

import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.UnaryOperator;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

public class V140ProcessorIntegrationTest {

    @Test
    public void testToDto_withProcessors_preAndPostApplied() {
        Account source = new Account(1L, "u1", "p1", "u1@test.com");

        UnaryOperator<Account> preProcessor = account -> {
            account.setUsername("u1_pre");
            account.setEmail("u1_pre@test.com");
            return account;
        };

        BiFunction<Account, AccountDto, AccountDto> postProcessor = (s, t) -> {
            t.setUsername(t.getUsername() + "_post");
            return t;
        };

        AccountDto result = AccountDtoCopier.toDto(source, preProcessor, postProcessor);

        assertNotNull(result);
        assertEquals(Long.valueOf(1L), result.getId());
        assertEquals("u1_pre_post", result.getUsername());
        assertEquals("u1_pre@test.com", result.getEmail());
    }

    @Test
    public void testFromDto_withProcessors_preAndPostApplied() {
        AccountDto source = new AccountDto(1L, "u1", "p1", "u1@test.com");

        UnaryOperator<AccountDto> preProcessor = dto -> {
            dto.setUsername("u1_pre");
            dto.setEmail("u1_pre@test.com");
            return dto;
        };

        BiFunction<AccountDto, Account, Account> postProcessor = (s, t) -> {
            t.setUsername(t.getUsername() + "_post");
            return t;
        };

        Account result = AccountDtoCopier.fromDto(source, preProcessor, postProcessor);

        assertNotNull(result);
        assertEquals(Long.valueOf(1L), result.getId());
        assertEquals("u1_pre_post", result.getUsername());
        assertEquals("u1_pre@test.com", result.getEmail());
    }

    @Test
    public void testToDtoList_withProcessors_preAffectsOrder_postFiltersResult() {
        List<Account> sources = new ArrayList<>();
        sources.add(new Account(1L, "u1", "p1", "u1@test.com"));
        sources.add(new Account(2L, "u2", "p2", "u2@test.com"));

        UnaryOperator<List<Account>> preProcessor = list -> {
            List<Account> reversed = new ArrayList<>(list);
            Collections.reverse(reversed);
            return reversed;
        };

        BiFunction<List<Account>, List<AccountDto>, List<AccountDto>> postProcessor = (s, t) -> t.subList(0, 1);

        List<AccountDto> result = AccountDtoCopier.toDtoList(sources, preProcessor, postProcessor);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(Long.valueOf(2L), result.get(0).getId());
        assertEquals("u2", result.get(0).getUsername());
    }

    @Test
    public void testToDtoSet_withProcessors_preFilters_postNoop() {
        Set<Account> sources = new LinkedHashSet<>();
        sources.add(new Account(1L, "u1", "p1", "u1@test.com"));
        sources.add(new Account(2L, "u2", "p2", "u2@test.com"));

        UnaryOperator<Set<Account>> preProcessor = set -> {
            Set<Account> filtered = new LinkedHashSet<>();
            for (Account a : set) {
                if (a != null && Long.valueOf(2L).equals(a.getId())) {
                    filtered.add(a);
                }
            }
            return filtered;
        };

        BiFunction<Set<Account>, Set<AccountDto>, Set<AccountDto>> postProcessor = (s, t) -> t;

        Set<AccountDto> result = AccountDtoCopier.toDtoSet(sources, preProcessor, postProcessor);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(Long.valueOf(2L), result.iterator().next().getId());
    }

    @Test
    public void testToDtoMap_withProcessors_preFilters_postNoop() {
        Map<String, Account> sources = new LinkedHashMap<>();
        sources.put("a", new Account(1L, "u1", "p1", "u1@test.com"));
        sources.put("b", new Account(2L, "u2", "p2", "u2@test.com"));

        UnaryOperator<Map<String, Account>> preProcessor = map -> {
            Map<String, Account> filtered = new LinkedHashMap<>();
            filtered.put("b", map.get("b"));
            return filtered;
        };

        BiFunction<Map<String, Account>, Map<String, AccountDto>, Map<String, AccountDto>> postProcessor = (s, t) -> t;

        Map<String, AccountDto> result = AccountDtoCopier.toDtoMap(sources, preProcessor, postProcessor);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(Long.valueOf(2L), result.get("b").getId());
    }

    @Test
    public void testToDtoArray_withProcessors_preTruncates_postNoop() {
        Account[] sources = new Account[] {
                new Account(1L, "u1", "p1", "u1@test.com"),
                new Account(2L, "u2", "p2", "u2@test.com")
        };

        UnaryOperator<Account[]> preProcessor = array -> Arrays.copyOf(array, 1);

        BiFunction<Account[], AccountDto[], AccountDto[]> postProcessor = (s, t) -> t;

        AccountDto[] result = AccountDtoCopier.toDtoArray(sources, preProcessor, postProcessor);

        assertNotNull(result);
        assertEquals(1, result.length);
        assertEquals(Long.valueOf(1L), result[0].getId());
    }

    @Test
    public void testToDtoList_withProcessors_preReturnsNull_thenReturnsNull() {
        List<Account> sources = new ArrayList<>();
        sources.add(new Account(1L, "u1", "p1", "u1@test.com"));

        UnaryOperator<List<Account>> preProcessor = list -> null;

        List<AccountDto> result = AccountDtoCopier.toDtoList(sources, preProcessor, null);

        assertNull(result);
    }

    @Test
    public void testToDtoList_withPostProcessor_stillWorks() {
        List<Account> sources = new ArrayList<>();
        sources.add(new Account(1L, "u1", "p1", "u1@test.com"));

        BiFunction<List<Account>, List<AccountDto>, List<AccountDto>> postProcessor = (s, t) -> {
            t.get(0).setUsername("u1_custom");
            return t;
        };

        List<AccountDto> result = AccountDtoCopier.toDtoList(sources, null, postProcessor);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("u1_custom", result.get(0).getUsername());
    }
}
