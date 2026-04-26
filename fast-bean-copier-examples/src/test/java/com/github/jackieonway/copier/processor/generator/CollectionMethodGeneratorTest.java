package com.github.jackieonway.copier.processor.generator;

import com.github.jackieonway.copier.processor.context.ProcessorContext;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

/**
 * CollectionMethodGenerator 单元测试。
 *
 * <p>测试集合方法生成器的构造和配置。
 * 由于方法生成需要完整的 TypeElement mock（包括包信息），
 * 主要的代码生成逻辑通过集成测试验证。
 *
 * @author jackieonway
 * @since 1.2.1
 */
public class CollectionMethodGeneratorTest {

    @Mock
    private ProcessingEnvironment processingEnv;

    @Mock
    private Elements elementUtils;

    @Mock
    private Types typeUtils;

    @Mock
    private Messager messager;

    private ProcessorContext context;
    private CollectionMethodGenerator generator;

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        when(processingEnv.getElementUtils()).thenReturn(elementUtils);
        when(processingEnv.getTypeUtils()).thenReturn(typeUtils);
        when(processingEnv.getMessager()).thenReturn(messager);
        
        context = new ProcessorContext(processingEnv);
        generator = new CollectionMethodGenerator(context);
    }

    // ========== 构造器测试 ==========

    @Test
    public void constructor_shouldAcceptProcessorContext() {
        CollectionMethodGenerator gen = new CollectionMethodGenerator(context);
        assertNotNull("应该成功创建 CollectionMethodGenerator", gen);
    }

    // ========== 配置方法测试 ==========

    @Test
    public void setSourceType_shouldAcceptTypeElement() {
        javax.lang.model.element.TypeElement sourceType = mock(javax.lang.model.element.TypeElement.class);
        generator.setSourceType(sourceType);
        assertNotNull(generator);
    }

    @Test
    public void setTargetType_shouldAcceptTypeElement() {
        javax.lang.model.element.TypeElement targetType = mock(javax.lang.model.element.TypeElement.class);
        generator.setTargetType(targetType);
        assertNotNull(generator);
    }

    @Test
    public void setUseStaticMethods_true_shouldNotThrow() {
        generator.setUseStaticMethods(true);
        assertNotNull(generator);
    }

    @Test
    public void setUseStaticMethods_false_shouldNotThrow() {
        generator.setUseStaticMethods(false);
        assertNotNull(generator);
    }

    // ========== List 方法存在性测试 ==========

    @Test
    public void generateToDtoList_methodExists() {
        try {
            generator.getClass().getMethod("generateToDtoList");
        } catch (NoSuchMethodException e) {
            fail("generateToDtoList 方法应该存在");
        }
    }

    @Test
    public void generateFromDtoList_methodExists() {
        try {
            generator.getClass().getMethod("generateFromDtoList");
        } catch (NoSuchMethodException e) {
            fail("generateFromDtoList 方法应该存在");
        }
    }

    @Test
    public void generateToDtoListWithProcessors_methodExists() {
        try {
            generator.getClass().getMethod("generateToDtoListWithProcessors");
        } catch (NoSuchMethodException e) {
            fail("generateToDtoListWithProcessors 方法应该存在");
        }
    }

    @Test
    public void generateFromDtoListWithProcessors_methodExists() {
        try {
            generator.getClass().getMethod("generateFromDtoListWithProcessors");
        } catch (NoSuchMethodException e) {
            fail("generateFromDtoListWithProcessors 方法应该存在");
        }
    }

    // ========== Set 方法存在性测试 ==========

    @Test
    public void generateToDtoSet_methodExists() {
        try {
            generator.getClass().getMethod("generateToDtoSet");
        } catch (NoSuchMethodException e) {
            fail("generateToDtoSet 方法应该存在");
        }
    }

    @Test
    public void generateFromDtoSet_methodExists() {
        try {
            generator.getClass().getMethod("generateFromDtoSet");
        } catch (NoSuchMethodException e) {
            fail("generateFromDtoSet 方法应该存在");
        }
    }

    @Test
    public void generateToDtoSetWithProcessors_methodExists() {
        try {
            generator.getClass().getMethod("generateToDtoSetWithProcessors");
        } catch (NoSuchMethodException e) {
            fail("generateToDtoSetWithProcessors 方法应该存在");
        }
    }

    @Test
    public void generateFromDtoSetWithProcessors_methodExists() {
        try {
            generator.getClass().getMethod("generateFromDtoSetWithProcessors");
        } catch (NoSuchMethodException e) {
            fail("generateFromDtoSetWithProcessors 方法应该存在");
        }
    }

    // ========== Map 方法存在性测试 ==========

    @Test
    public void generateToDtoMap_methodExists() {
        try {
            generator.getClass().getMethod("generateToDtoMap");
        } catch (NoSuchMethodException e) {
            fail("generateToDtoMap 方法应该存在");
        }
    }

    @Test
    public void generateFromDtoMap_methodExists() {
        try {
            generator.getClass().getMethod("generateFromDtoMap");
        } catch (NoSuchMethodException e) {
            fail("generateFromDtoMap 方法应该存在");
        }
    }

    // ========== Array 方法存在性测试 ==========

    @Test
    public void generateToDtoArray_methodExists() {
        try {
            generator.getClass().getMethod("generateToDtoArray");
        } catch (NoSuchMethodException e) {
            fail("generateToDtoArray 方法应该存在");
        }
    }

    @Test
    public void generateFromDtoArray_methodExists() {
        try {
            generator.getClass().getMethod("generateFromDtoArray");
        } catch (NoSuchMethodException e) {
            fail("generateFromDtoArray 方法应该存在");
        }
    }
}
