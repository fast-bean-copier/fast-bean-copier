package com.github.jackieonway.copier.processor.generator;

import com.github.jackieonway.copier.processor.FieldMapping;
import com.github.jackieonway.copier.processor.context.ProcessorContext;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import java.util.Collections;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

/**
 * DeepCopyGenerator 单元测试。
 *
 * <p>测试深拷贝代码生成器的辅助方法和警告输出。
 * 由于深拷贝代码生成需要完整的类型系统支持，
 * 主要的代码生成逻辑通过集成测试验证。
 *
 * @author jackieonway
 * @since 1.2.1
 */
public class DeepCopyGeneratorTest {

    @Mock
    private ProcessingEnvironment processingEnv;

    @Mock
    private Elements elementUtils;

    @Mock
    private Types typeUtils;

    @Mock
    private Messager messager;

    private ProcessorContext context;
    private DeepCopyGenerator generator;

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        when(processingEnv.getElementUtils()).thenReturn(elementUtils);
        when(processingEnv.getTypeUtils()).thenReturn(typeUtils);
        when(processingEnv.getMessager()).thenReturn(messager);
        context = new ProcessorContext(processingEnv);
        generator = new DeepCopyGenerator(context);
    }

    // ========== hasUnsupportedGenerics 测试 ==========

    @Test
    public void hasUnsupportedGenerics_withRawType_shouldReturnTrue() {
        DeclaredType rawType = mock(DeclaredType.class);
        when(rawType.getKind()).thenReturn(TypeKind.DECLARED);
        doReturn(Collections.emptyList()).when(rawType).getTypeArguments();
        when(rawType.toString()).thenReturn("java.util.List");

        assertTrue("原始类型应该返回 true", generator.hasUnsupportedGenerics(rawType));
    }

    @Test
    public void hasUnsupportedGenerics_withParameterizedType_shouldReturnFalse() {
        DeclaredType parameterizedType = mock(DeclaredType.class);
        when(parameterizedType.getKind()).thenReturn(TypeKind.DECLARED);
        
        TypeMirror elementType = mock(TypeMirror.class);
        when(elementType.getKind()).thenReturn(TypeKind.DECLARED);
        when(elementType.toString()).thenReturn("java.lang.String");
        
        doReturn(Collections.singletonList(elementType)).when(parameterizedType).getTypeArguments();
        when(parameterizedType.toString()).thenReturn("java.util.List<java.lang.String>");

        assertFalse("参数化类型应该返回 false", generator.hasUnsupportedGenerics(parameterizedType));
    }

    @Test
    public void hasUnsupportedGenerics_withNull_shouldReturnFalse() {
        // null 类型不是原始类型也不是无界通配符
        assertFalse("null 应该返回 false", generator.hasUnsupportedGenerics(null));
    }

    // ========== warnUnsupportedGenerics 测试 ==========

    @Test
    public void warnUnsupportedGenerics_shouldOutputWarning() {
        TypeMirror sourceFieldType = mock(TypeMirror.class);
        when(sourceFieldType.toString()).thenReturn("java.util.List");
        
        TypeMirror targetFieldType = mock(TypeMirror.class);
        when(targetFieldType.toString()).thenReturn("java.util.List");
        
        VariableElement targetField = mock(VariableElement.class);
        FieldMapping mapping = mock(FieldMapping.class);
        when(mapping.getTargetField()).thenReturn(targetField);

        generator.warnUnsupportedGenerics(sourceFieldType, targetFieldType, mapping);

        verify(messager).printMessage(
                eq(javax.tools.Diagnostic.Kind.WARNING),
                contains("原始类型或不受支持的通配符"),
                eq(targetField)
        );
    }

    @Test
    public void warnUnsupportedGenerics_shouldIncludeTypeInfo() {
        TypeMirror sourceFieldType = mock(TypeMirror.class);
        when(sourceFieldType.toString()).thenReturn("java.util.List<?>"); 
        
        TypeMirror targetFieldType = mock(TypeMirror.class);
        when(targetFieldType.toString()).thenReturn("java.util.List<String>");
        
        VariableElement targetField = mock(VariableElement.class);
        FieldMapping mapping = mock(FieldMapping.class);
        when(mapping.getTargetField()).thenReturn(targetField);

        generator.warnUnsupportedGenerics(sourceFieldType, targetFieldType, mapping);

        verify(messager).printMessage(
                eq(javax.tools.Diagnostic.Kind.WARNING),
                argThat(msg -> msg.toString().contains("source=") && msg.toString().contains("target=")),
                eq(targetField)
        );
    }

    // ========== 构造器测试 ==========

    @Test
    public void constructor_shouldAcceptProcessorContext() {
        DeepCopyGenerator gen = new DeepCopyGenerator(context);
        assertNotNull("应该成功创建 DeepCopyGenerator", gen);
    }
}
