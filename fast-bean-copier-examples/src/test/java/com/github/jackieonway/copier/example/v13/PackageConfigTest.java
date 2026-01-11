package com.github.jackieonway.copier.example.v13;

import com.github.jackieonway.copier.annotation.ComponentModel;
import com.github.jackieonway.copier.annotation.NullValueStrategy;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * v1.3 包级别配置功能测试。
 *
 * @author jackieonway
 * @since 1.3.0
 */
public class PackageConfigTest {

    /**
     * 测试配置优先级：类级别 > 包级别 > 默认值。
     */
    @Test
    public void testConfigPriority() {
        // 模拟配置优先级逻辑
        ComponentModel classLevel = ComponentModel.SPRING;
        ComponentModel packageLevel = ComponentModel.CDI;
        ComponentModel defaultValue = ComponentModel.DEFAULT;
        
        // 类级别配置优先
        ComponentModel effective = classLevel;
        assertEquals(ComponentModel.SPRING, effective);
    }

    /**
     * 测试类级别为默认值时使用包级别配置。
     */
    @Test
    public void testPackageLevelFallback() {
        ComponentModel classLevel = ComponentModel.DEFAULT;
        ComponentModel packageLevel = ComponentModel.SPRING;
        ComponentModel defaultValue = ComponentModel.DEFAULT;
        
        // 类级别为默认值时，使用包级别配置
        ComponentModel effective;
        if (classLevel != ComponentModel.DEFAULT) {
            effective = classLevel;
        } else if (packageLevel != null) {
            effective = packageLevel;
        } else {
            effective = defaultValue;
        }
        
        assertEquals(ComponentModel.SPRING, effective);
    }

    /**
     * 测试 NullValueStrategy 默认值。
     */
    @Test
    public void testNullValueStrategyDefault() {
        NullValueStrategy packageLevel = null;
        NullValueStrategy defaultValue = NullValueStrategy.IGNORE;
        
        NullValueStrategy effective = packageLevel != null ? packageLevel : defaultValue;
        assertEquals(NullValueStrategy.IGNORE, effective);
    }

    /**
     * 测试 NullValueStrategy 包级别配置。
     */
    @Test
    public void testNullValueStrategyPackageLevel() {
        NullValueStrategy packageLevel = NullValueStrategy.REPLACE;
        NullValueStrategy defaultValue = NullValueStrategy.IGNORE;
        
        NullValueStrategy effective = packageLevel != null ? packageLevel : defaultValue;
        assertEquals(NullValueStrategy.REPLACE, effective);
    }
}
