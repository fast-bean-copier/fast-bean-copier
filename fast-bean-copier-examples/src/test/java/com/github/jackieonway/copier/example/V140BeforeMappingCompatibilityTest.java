package com.github.jackieonway.copier.example;

import com.github.jackieonway.copier.example.v140.BeforeMappingUser;
import com.github.jackieonway.copier.example.v140.BeforeMappingUserDto;
import com.github.jackieonway.copier.example.v140.BeforeMappingUserDtoCopier;
import com.github.jackieonway.copier.example.v140.ProcessorOnlyUserDto;
import com.github.jackieonway.copier.example.v140.ProcessorOnlyUserDtoCopier;
import org.junit.Test;

import java.util.function.UnaryOperator;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * v1.4.0 preProcessor 功能测试（原 beforeMapping 已在 v1.5.0 移除）。
 *
 * @author jackieonway
 * @since 1.4.0
 */
public class V140BeforeMappingCompatibilityTest {

    @Test
    public void testPreProcessor_only() {
        BeforeMappingUser source = new BeforeMappingUser("name1");

        UnaryOperator<BeforeMappingUser> preProcessor = u -> {
            u.setName("name2");
            return u;
        };

        ProcessorOnlyUserDto result = ProcessorOnlyUserDtoCopier.toDto(source, preProcessor, null);

        assertNotNull(result);
        assertEquals("name2", result.getName());
    }

    @Test
    public void testPreProcessor_capturesName() {
        BeforeMappingUser source = new BeforeMappingUser("name1");

        // 使用 preProcessor 替代原 beforeMapping="captureName" 功能
        UnaryOperator<BeforeMappingUser> preProcessor = u -> {
            // preProcessor 可在映射前修改源对象
            return u;
        };

        BeforeMappingUserDto result = BeforeMappingUserDtoCopier.toDto(source, preProcessor, null);

        assertNotNull(result);
        assertEquals("name1", result.getName());
    }

    @Test
    public void testPreProcessor_modifiesSource() {
        BeforeMappingUser source = new BeforeMappingUser("name1");

        UnaryOperator<BeforeMappingUser> preProcessor = u -> {
            u.setName("name2");
            return u;
        };

        BeforeMappingUserDto result = BeforeMappingUserDtoCopier.toDto(source, preProcessor, null);

        assertNotNull(result);
        assertEquals("name2", result.getName());
    }
}
