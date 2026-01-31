package com.github.jackieonway.copier.example;

import com.github.jackieonway.copier.example.nested.multilevel.*;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * 测试多层嵌套对象深拷贝（A有注解，B无注解，C有注解）。
 * 
 * 场景：Level1Dto 有 @CopyTarget，Level2Dto 没有 @CopyTarget，Level3Dto 有 @CopyTarget
 * 预期：Level1DtoCopier 应该使用 Level3DtoCopier 来拷贝 Level3
 */
public class MultilevelNestedObjectTest {

    @Test
    public void testMultilevelNestedObjectCopy() {
        // 创建源对象
        Level3 level3 = new Level3();
        level3.setValue("Level 3 Value");

        Level2 level2 = new Level2();
        level2.setName("Level 2 Name");
        level2.setLevel3(level3);

        Level1 level1 = new Level1();
        level1.setId(1L);
        level1.setLevel2(level2);

        // 执行拷贝
        Level1Dto dto = Level1DtoCopier.toDto(level1);

        // 验证结果
        assertNotNull(dto);
        assertEquals(Long.valueOf(1L), dto.getId());
        
        assertNotNull(dto.getLevel2());
        assertEquals("Level 2 Name", dto.getLevel2().getName());
        
        assertNotNull(dto.getLevel2().getLevel3());
        assertEquals("Level 3 Value", dto.getLevel2().getLevel3().getValue());
    }

    @Test
    public void testMultilevelNestedObjectReverseCopy() {
        // 创建 DTO 对象
        Level3Dto level3Dto = new Level3Dto();
        level3Dto.setValue("Level 3 Value");

        Level2Dto level2Dto = new Level2Dto();
        level2Dto.setName("Level 2 Name");
        level2Dto.setLevel3(level3Dto);

        Level1Dto level1Dto = new Level1Dto();
        level1Dto.setId(1L);
        level1Dto.setLevel2(level2Dto);

        // 执行反向拷贝
        Level1 entity = Level1DtoCopier.fromDto(level1Dto);

        // 验证结果
        assertNotNull(entity);
        assertEquals(Long.valueOf(1L), entity.getId());
        
        assertNotNull(entity.getLevel2());
        assertEquals("Level 2 Name", entity.getLevel2().getName());
        
        assertNotNull(entity.getLevel2().getLevel3());
        assertEquals("Level 3 Value", entity.getLevel2().getLevel3().getValue());
    }

    @Test
    public void testMultilevelNestedObjectWithNull() {
        // Level2 为 null
        Level1 level1 = new Level1();
        level1.setId(1L);
        level1.setLevel2(null);

        Level1Dto dto = Level1DtoCopier.toDto(level1);

        assertNotNull(dto);
        assertEquals(Long.valueOf(1L), dto.getId());
        assertNull(dto.getLevel2());
    }

    @Test
    public void testMultilevelNestedObjectWithNullLevel3() {
        // Level3 为 null
        Level2 level2 = new Level2();
        level2.setName("Level 2 Name");
        level2.setLevel3(null);

        Level1 level1 = new Level1();
        level1.setId(1L);
        level1.setLevel2(level2);

        Level1Dto dto = Level1DtoCopier.toDto(level1);

        assertNotNull(dto);
        assertEquals(Long.valueOf(1L), dto.getId());
        assertNotNull(dto.getLevel2());
        assertEquals("Level 2 Name", dto.getLevel2().getName());
        assertNull(dto.getLevel2().getLevel3());
    }
}
