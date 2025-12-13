package com.github.jackieonway.copier.example;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * 字段忽略功能的集成测试。
 *
 * @author jackieonway
 * @since 1.0.0
 */
public class FieldIgnoreTest {

    /**
     * 测试忽略password字段。
     */
    @Test
    public void testIgnorePasswordField() {
        // 创建源对象
        Account account = new Account(1L, "admin", "secret123", "admin@example.com");
        
        // 调用生成的 toDto 方法
        AccountDto accountDto = AccountDtoCopier.toDto(account);
        
        // 验证结果
        assertNotNull(accountDto);
        assertEquals(account.getId(), accountDto.getId());
        assertEquals(account.getUsername(), accountDto.getUsername());
        assertEquals(account.getEmail(), accountDto.getEmail());
        
        // password 字段应该为 null（未被拷贝）
        assertNull(accountDto.getPassword());
    }

    /**
     * 测试反向拷贝时忽略password字段。
     */
    @Test
    public void testIgnorePasswordFieldInReverse() {
        // 创建目标对象
        AccountDto accountDto = new AccountDto(2L, "user", "encrypted_password", "user@example.com");
        
        // 调用生成的 fromDto 方法
        Account account = AccountDtoCopier.fromDto(accountDto);
        
        // 验证结果
        assertNotNull(account);
        assertEquals(accountDto.getId(), account.getId());
        assertEquals(accountDto.getUsername(), account.getUsername());
        assertEquals(accountDto.getEmail(), account.getEmail());
        
        // password 字段应该为 null（未被拷贝）
        assertNull(account.getPassword());
    }

    /**
     * 测试忽略字段不影响其他字段的拷贝。
     */
    @Test
    public void testIgnoredFieldDoesNotAffectOtherFields() {
        // 创建源对象
        Account account = new Account(3L, "test", "password123", "test@example.com");
        
        // 调用生成的 toDto 方法
        AccountDto accountDto = AccountDtoCopier.toDto(account);
        
        // 验证所有其他字段都被正确拷贝
        assertNotNull(accountDto);
        assertEquals(Long.valueOf(3L), accountDto.getId());
        assertEquals("test", accountDto.getUsername());
        assertEquals("test@example.com", accountDto.getEmail());
        
        // password 字段被忽略
        assertNull(accountDto.getPassword());
    }
}
