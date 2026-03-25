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

public class V140BeforeMappingCompatibilityTest {

    @Test
    public void testBeforeMapping_only() {
        BeforeMappingUser source = new BeforeMappingUser("name1");

        BeforeMappingUserDto result = BeforeMappingUserDtoCopier.toDto(source);

        assertNotNull(result);
        assertEquals("name1", result.getCapturedName());
        assertEquals("name1", result.getName());
    }

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
    public void testPreProcessor_thenBeforeMapping() {
        BeforeMappingUser source = new BeforeMappingUser("name1");

        UnaryOperator<BeforeMappingUser> preProcessor = u -> {
            u.setName("name2");
            return u;
        };

        BeforeMappingUserDto result = BeforeMappingUserDtoCopier.toDto(source, preProcessor, null);

        assertNotNull(result);
        assertEquals("name2", result.getCapturedName());
        assertEquals("name2", result.getName());
    }
}
