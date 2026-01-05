package com.github.jackieonway.copier.processor.context;

import com.github.jackieonway.copier.annotation.ComponentModel;
import com.github.jackieonway.copier.processor.FieldMapping;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.Name;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import javax.tools.Diagnostic;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

/**
 * ProcessorContext 单元测试。
 *
 * @author jackieonway
 * @since 2.0.0
 */
public class ProcessorContextTest {

    @Mock
    private ProcessingEnvironment processingEnv;

    @Mock
    private Elements elementUtils;

    @Mock
    private Types typeUtils;

    @Mock
    private Messager messager;

    @Mock
    private TypeElement sourceType;

    @Mock
    private TypeElement targetType;

    @Mock
    private TypeMirror typeMirror;

    @Mock
    private Element element;

    @Mock
    private FieldMapping fieldMapping;

    private ProcessorContext context;

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        when(processingEnv.getElementUtils()).thenReturn(elementUtils);
        when(processingEnv.getTypeUtils()).thenReturn(typeUtils);
        when(processingEnv.getMessager()).thenReturn(messager);
        context = new ProcessorContext(processingEnv);
    }

    // ========== 构造器和工具类访问测试 ==========

    @Test
    public void constructor_shouldInitializeAllTools() {
        assertNotNull(context.getProcessingEnv());
        assertNotNull(context.getElementUtils());
        assertNotNull(context.getTypeUtils());
        assertNotNull(context.getMessager());
        assertEquals(processingEnv, context.getProcessingEnv());
        assertEquals(elementUtils, context.getElementUtils());
        assertEquals(typeUtils, context.getTypeUtils());
        assertEquals(messager, context.getMessager());
    }

    // ========== 类型信息存储和获取测试 ==========

    @Test
    public void setAndGetSourceType_shouldWorkCorrectly() {
        assertNull(context.getSourceType());
        context.setSourceType(sourceType);
        assertEquals(sourceType, context.getSourceType());
    }

    @Test
    public void setAndGetTargetType_shouldWorkCorrectly() {
        assertNull(context.getTargetType());
        context.setTargetType(targetType);
        assertEquals(targetType, context.getTargetType());
    }

    @Test
    public void getSourceTypeName_shouldReturnSimpleName() {
        Name name = mock(Name.class);
        when(name.toString()).thenReturn("User");
        when(sourceType.getSimpleName()).thenReturn(name);
        
        context.setSourceType(sourceType);
        assertEquals("User", context.getSourceTypeName());
    }

    @Test
    public void getSourceTypeName_shouldReturnNullWhenNotSet() {
        assertNull(context.getSourceTypeName());
    }

    @Test
    public void getTargetTypeName_shouldReturnSimpleName() {
        Name name = mock(Name.class);
        when(name.toString()).thenReturn("UserDto");
        when(targetType.getSimpleName()).thenReturn(name);
        
        context.setTargetType(targetType);
        assertEquals("UserDto", context.getTargetTypeName());
    }

    @Test
    public void getSourceTypeQualifiedName_shouldReturnFullName() {
        Name name = mock(Name.class);
        when(name.toString()).thenReturn("com.example.User");
        when(sourceType.getQualifiedName()).thenReturn(name);
        
        context.setSourceType(sourceType);
        assertEquals("com.example.User", context.getSourceTypeQualifiedName());
    }

    @Test
    public void getTargetTypeQualifiedName_shouldReturnFullName() {
        Name name = mock(Name.class);
        when(name.toString()).thenReturn("com.example.UserDto");
        when(targetType.getQualifiedName()).thenReturn(name);
        
        context.setTargetType(targetType);
        assertEquals("com.example.UserDto", context.getTargetTypeQualifiedName());
    }

    // ========== 配置信息存储和获取测试 ==========

    @Test
    public void setAndGetIgnoreFields_shouldWorkCorrectly() {
        Set<String> ignoreFields = new HashSet<>(Arrays.asList("field1", "field2"));
        context.setIgnoreFields(ignoreFields);
        
        Set<String> result = context.getIgnoreFields();
        assertEquals(2, result.size());
        assertTrue(result.contains("field1"));
        assertTrue(result.contains("field2"));
    }

    @Test
    public void getIgnoreFields_shouldReturnUnmodifiableSet() {
        Set<String> ignoreFields = new HashSet<>(Collections.singletonList("field1"));
        context.setIgnoreFields(ignoreFields);
        
        Set<String> result = context.getIgnoreFields();
        try {
            result.add("field2");
            fail("Should throw UnsupportedOperationException");
        } catch (UnsupportedOperationException e) {
            // Expected
        }
    }

    @Test
    public void setIgnoreFields_withNull_shouldSetEmptySet() {
        context.setIgnoreFields(null);
        assertNotNull(context.getIgnoreFields());
        assertTrue(context.getIgnoreFields().isEmpty());
    }

    @Test
    public void setAndGetUsesClasses_shouldWorkCorrectly() {
        List<TypeMirror> usesClasses = Arrays.asList(typeMirror);
        context.setUsesClasses(usesClasses);
        
        List<TypeMirror> result = context.getUsesClasses();
        assertEquals(1, result.size());
        assertEquals(typeMirror, result.get(0));
    }

    @Test
    public void getUsesClasses_shouldReturnUnmodifiableList() {
        context.setUsesClasses(Arrays.asList(typeMirror));
        
        List<TypeMirror> result = context.getUsesClasses();
        try {
            result.add(typeMirror);
            fail("Should throw UnsupportedOperationException");
        } catch (UnsupportedOperationException e) {
            // Expected
        }
    }

    @Test
    public void setUsesClasses_withNull_shouldSetEmptyList() {
        context.setUsesClasses(null);
        assertNotNull(context.getUsesClasses());
        assertTrue(context.getUsesClasses().isEmpty());
    }

    @Test
    public void setAndGetComponentModel_shouldWorkCorrectly() {
        assertEquals(ComponentModel.DEFAULT, context.getComponentModel());
        
        context.setComponentModel(ComponentModel.SPRING);
        assertEquals(ComponentModel.SPRING, context.getComponentModel());
        
        context.setComponentModel(ComponentModel.CDI);
        assertEquals(ComponentModel.CDI, context.getComponentModel());
        
        context.setComponentModel(ComponentModel.JSR330);
        assertEquals(ComponentModel.JSR330, context.getComponentModel());
    }

    @Test
    public void setComponentModel_withNull_shouldSetDefault() {
        context.setComponentModel(ComponentModel.SPRING);
        context.setComponentModel(null);
        assertEquals(ComponentModel.DEFAULT, context.getComponentModel());
    }

    // ========== 字段映射存储和获取测试 ==========

    @Test
    public void setAndGetFieldMappings_shouldWorkCorrectly() {
        List<FieldMapping> mappings = Arrays.asList(fieldMapping);
        context.setFieldMappings(mappings);
        
        List<FieldMapping> result = context.getFieldMappings();
        assertEquals(1, result.size());
        assertEquals(fieldMapping, result.get(0));
    }

    @Test
    public void getFieldMappings_shouldReturnUnmodifiableList() {
        context.setFieldMappings(Arrays.asList(fieldMapping));
        
        List<FieldMapping> result = context.getFieldMappings();
        try {
            result.add(fieldMapping);
            fail("Should throw UnsupportedOperationException");
        } catch (UnsupportedOperationException e) {
            // Expected
        }
    }

    @Test
    public void setFieldMappings_withNull_shouldSetEmptyList() {
        context.setFieldMappings(null);
        assertNotNull(context.getFieldMappings());
        assertTrue(context.getFieldMappings().isEmpty());
    }

    // ========== 消息输出测试 ==========

    @Test
    public void error_withElement_shouldCallMessager() {
        context.error("Error message", element);
        verify(messager).printMessage(Diagnostic.Kind.ERROR, "Error message", element);
    }

    @Test
    public void error_withoutElement_shouldCallMessager() {
        context.error("Error message");
        verify(messager).printMessage(Diagnostic.Kind.ERROR, "Error message");
    }

    @Test
    public void warning_withElement_shouldCallMessager() {
        context.warning("Warning message", element);
        verify(messager).printMessage(Diagnostic.Kind.WARNING, "Warning message", element);
    }

    @Test
    public void warning_withoutElement_shouldCallMessager() {
        context.warning("Warning message");
        verify(messager).printMessage(Diagnostic.Kind.WARNING, "Warning message");
    }

    @Test
    public void note_shouldCallMessager() {
        context.note("Note message");
        verify(messager).printMessage(Diagnostic.Kind.NOTE, "Note message");
    }

    @Test
    public void note_withElement_shouldCallMessager() {
        context.note("Note message", element);
        verify(messager).printMessage(Diagnostic.Kind.NOTE, "Note message", element);
    }

    // ========== 辅助方法测试 ==========

    @Test
    public void isStaticMode_shouldReturnTrueForDefault() {
        context.setComponentModel(ComponentModel.DEFAULT);
        assertTrue(context.isStaticMode());
        assertFalse(context.isSpringMode());
        assertFalse(context.isCdiMode());
        assertFalse(context.isJsr330Mode());
    }

    @Test
    public void isSpringMode_shouldReturnTrueForSpring() {
        context.setComponentModel(ComponentModel.SPRING);
        assertFalse(context.isStaticMode());
        assertTrue(context.isSpringMode());
        assertFalse(context.isCdiMode());
        assertFalse(context.isJsr330Mode());
    }

    @Test
    public void isCdiMode_shouldReturnTrueForCdi() {
        context.setComponentModel(ComponentModel.CDI);
        assertFalse(context.isStaticMode());
        assertFalse(context.isSpringMode());
        assertTrue(context.isCdiMode());
        assertFalse(context.isJsr330Mode());
    }

    @Test
    public void isJsr330Mode_shouldReturnTrueForJsr330() {
        context.setComponentModel(ComponentModel.JSR330);
        assertFalse(context.isStaticMode());
        assertFalse(context.isSpringMode());
        assertFalse(context.isCdiMode());
        assertTrue(context.isJsr330Mode());
    }

    // ========== reset 方法测试 ==========

    @Test
    public void reset_shouldClearAllState() {
        // 设置所有状态
        context.setSourceType(sourceType);
        context.setTargetType(targetType);
        context.setIgnoreFields(new HashSet<>(Arrays.asList("field1")));
        context.setUsesClasses(Arrays.asList(typeMirror));
        context.setComponentModel(ComponentModel.SPRING);
        context.setFieldMappings(Arrays.asList(fieldMapping));
        
        // 重置
        context.reset();
        
        // 验证所有状态已清除
        assertNull(context.getSourceType());
        assertNull(context.getTargetType());
        assertTrue(context.getIgnoreFields().isEmpty());
        assertTrue(context.getUsesClasses().isEmpty());
        assertEquals(ComponentModel.DEFAULT, context.getComponentModel());
        assertTrue(context.getFieldMappings().isEmpty());
    }

    // ========== 数据隔离测试 ==========

    @Test
    public void setIgnoreFields_shouldNotAffectOriginalSet() {
        Set<String> original = new HashSet<>(Arrays.asList("field1"));
        context.setIgnoreFields(original);
        
        // 修改原始集合
        original.add("field2");
        
        // 验证 context 中的集合不受影响
        assertEquals(1, context.getIgnoreFields().size());
        assertTrue(context.getIgnoreFields().contains("field1"));
        assertFalse(context.getIgnoreFields().contains("field2"));
    }

    @Test
    public void setUsesClasses_shouldNotAffectOriginalList() {
        TypeMirror anotherTypeMirror = mock(TypeMirror.class);
        List<TypeMirror> original = new java.util.ArrayList<>(Arrays.asList(typeMirror));
        context.setUsesClasses(original);
        
        // 修改原始列表
        original.add(anotherTypeMirror);
        
        // 验证 context 中的列表不受影响
        assertEquals(1, context.getUsesClasses().size());
    }

    @Test
    public void setFieldMappings_shouldNotAffectOriginalList() {
        FieldMapping anotherMapping = mock(FieldMapping.class);
        List<FieldMapping> original = new java.util.ArrayList<>(Arrays.asList(fieldMapping));
        context.setFieldMappings(original);
        
        // 修改原始列表
        original.add(anotherMapping);
        
        // 验证 context 中的列表不受影响
        assertEquals(1, context.getFieldMappings().size());
    }
}
