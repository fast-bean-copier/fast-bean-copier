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

import java.util.Collections;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

/**
 * BasicMethodGenerator 单元测试。
 *
 * <p>测试基础方法生成器的构造和配置。
 * 由于方法生成需要完整的 TypeElement mock（包括包信息），
 * 主要的代码生成逻辑通过集成测试验证。
 *
 * @author jackieonway
 * @since 1.2.1
 */
public class BasicMethodGeneratorTest {

    @Mock
    private ProcessingEnvironment processingEnv;

    @Mock
    private Elements elementUtils;

    @Mock
    private Types typeUtils;

    @Mock
    private Messager messager;

    private ProcessorContext context;
    private BasicMethodGenerator generator;

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        when(processingEnv.getElementUtils()).thenReturn(elementUtils);
        when(processingEnv.getTypeUtils()).thenReturn(typeUtils);
        when(processingEnv.getMessager()).thenReturn(messager);
        
        context = new ProcessorContext(processingEnv);
        generator = new BasicMethodGenerator(context);
    }

    // ========== 构造器测试 ==========

    @Test
    public void constructor_shouldAcceptProcessorContext() {
        BasicMethodGenerator gen = new BasicMethodGenerator(context);
        assertNotNull("应该成功创建 BasicMethodGenerator", gen);
    }

    // ========== 配置方法测试 ==========

    @Test
    public void setSourceType_shouldAcceptTypeElement() {
        javax.lang.model.element.TypeElement sourceType = mock(javax.lang.model.element.TypeElement.class);
        generator.setSourceType(sourceType);
        // 验证设置成功（不抛出异常）
        assertNotNull(generator);
    }

    @Test
    public void setTargetType_shouldAcceptTypeElement() {
        javax.lang.model.element.TypeElement targetType = mock(javax.lang.model.element.TypeElement.class);
        generator.setTargetType(targetType);
        assertNotNull(generator);
    }

    @Test
    public void setFieldMappings_shouldAcceptList() {
        generator.setFieldMappings(Collections.emptyList());
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

    @Test
    public void setUsesClasses_shouldAcceptList() {
        generator.setUsesClasses(Collections.emptyList());
        assertNotNull(generator);
    }

    @Test
    public void setUsesClasses_shouldAcceptNull() {
        generator.setUsesClasses(null);
        assertNotNull(generator);
    }

    // ========== 方法存在性测试 ==========

    @Test
    public void generateToDto_methodExists() {
        // 验证方法存在
        try {
            generator.getClass().getMethod("generateToDto");
        } catch (NoSuchMethodException e) {
            fail("generateToDto 方法应该存在");
        }
    }

    @Test
    public void generateFromDto_methodExists() {
        try {
            generator.getClass().getMethod("generateFromDto");
        } catch (NoSuchMethodException e) {
            fail("generateFromDto 方法应该存在");
        }
    }

    @Test
    public void generateToDtoWithProcessors_methodExists() {
        try {
            generator.getClass().getMethod("generateToDtoWithProcessors");
        } catch (NoSuchMethodException e) {
            fail("generateToDtoWithProcessors 方法应该存在");
        }
    }

    @Test
    public void generateFromDtoWithProcessors_methodExists() {
        try {
            generator.getClass().getMethod("generateFromDtoWithProcessors");
        } catch (NoSuchMethodException e) {
            fail("generateFromDtoWithProcessors 方法应该存在");
        }
    }
}
