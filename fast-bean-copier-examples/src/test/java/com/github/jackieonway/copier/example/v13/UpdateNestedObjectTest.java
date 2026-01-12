package com.github.jackieonway.copier.example.v13;

import com.github.jackieonway.copier.processor.FieldMapping;
import com.github.jackieonway.copier.processor.context.ProcessorContext;
import com.github.jackieonway.copier.processor.generator.DeepCopyGenerator;
import com.squareup.javapoet.MethodSpec;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

/**
 * v1.3 更新现有对象嵌套处理功能测试。
 *
 * <p>测试 DeepCopyGenerator 中新增的嵌套对象更新和集合更新方法。
 *
 * @author jackieonway
 * @since 1.3.0
 */
public class UpdateNestedObjectTest {

    @Mock
    private ProcessingEnvironment processingEnv;

    @Mock
    private Elements elementUtils;

    @Mock
    private Types typeUtils;

    @Mock
    private Messager messager;

    @Mock
    private VariableElement sourceField;

    @Mock
    private VariableElement targetField;

    private ProcessorContext context;
    private DeepCopyGenerator deepCopyGenerator;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        
        when(processingEnv.getElementUtils()).thenReturn(elementUtils);
        when(processingEnv.getTypeUtils()).thenReturn(typeUtils);
        when(processingEnv.getMessager()).thenReturn(messager);
        
        context = new ProcessorContext(processingEnv);
        deepCopyGenerator = new DeepCopyGenerator(context);
    }

    /**
     * 测试集合字段更新代码生成（List）。
     */
    @Test
    public void testGenerateCollectionUpdateForList() {
        // 设置 mock 为 List 类型
        DeclaredType listType = mock(DeclaredType.class);
        when(listType.toString()).thenReturn("java.util.List<java.lang.String>");
        when(listType.getKind()).thenReturn(TypeKind.DECLARED);
        
        when(sourceField.getSimpleName()).thenReturn(new TestName("items"));
        when(targetField.getSimpleName()).thenReturn(new TestName("items"));
        
        FieldMapping mapping = new FieldMapping(sourceField, targetField, listType, listType);
        
        MethodSpec.Builder methodBuilder = MethodSpec.methodBuilder("testMethod");
        
        // 调用方法
        deepCopyGenerator.generateCollectionUpdate(
                methodBuilder,
                "getItems",
                "setItems",
                listType,
                listType,
                mapping,
                false
        );
        
        // 验证生成的代码
        String code = methodBuilder.build().toString();
        assertTrue("应包含源字段 null 检查", code.contains("source.getItems() != null"));
        assertTrue("应包含 ArrayList 创建", code.contains("ArrayList"));
    }

    /**
     * 测试集合字段更新代码生成（Set）。
     */
    @Test
    public void testGenerateCollectionUpdateForSet() {
        // 设置 mock 为 Set 类型
        DeclaredType setType = mock(DeclaredType.class);
        when(setType.toString()).thenReturn("java.util.Set<java.lang.String>");
        when(setType.getKind()).thenReturn(TypeKind.DECLARED);
        
        when(sourceField.getSimpleName()).thenReturn(new TestName("tags"));
        when(targetField.getSimpleName()).thenReturn(new TestName("tags"));
        
        FieldMapping mapping = new FieldMapping(sourceField, targetField, setType, setType);
        
        MethodSpec.Builder methodBuilder = MethodSpec.methodBuilder("testMethod");
        
        // 调用方法
        deepCopyGenerator.generateCollectionUpdate(
                methodBuilder,
                "getTags",
                "setTags",
                setType,
                setType,
                mapping,
                false
        );
        
        // 验证生成的代码
        String code = methodBuilder.build().toString();
        assertTrue("应包含源字段 null 检查", code.contains("source.getTags() != null"));
        assertTrue("应包含 LinkedHashSet 创建", code.contains("LinkedHashSet"));
    }

    /**
     * 测试集合字段更新代码生成（Map）。
     */
    @Test
    public void testGenerateCollectionUpdateForMap() {
        // 设置 mock 为 Map 类型
        DeclaredType mapType = mock(DeclaredType.class);
        when(mapType.toString()).thenReturn("java.util.Map<java.lang.String, java.lang.Object>");
        when(mapType.getKind()).thenReturn(TypeKind.DECLARED);
        
        when(sourceField.getSimpleName()).thenReturn(new TestName("properties"));
        when(targetField.getSimpleName()).thenReturn(new TestName("properties"));
        
        FieldMapping mapping = new FieldMapping(sourceField, targetField, mapType, mapType);
        
        MethodSpec.Builder methodBuilder = MethodSpec.methodBuilder("testMethod");
        
        // 调用方法
        deepCopyGenerator.generateCollectionUpdate(
                methodBuilder,
                "getProperties",
                "setProperties",
                mapType,
                mapType,
                mapping,
                false
        );
        
        // 验证生成的代码
        String code = methodBuilder.build().toString();
        assertTrue("应包含源字段 null 检查", code.contains("source.getProperties() != null"));
        assertTrue("应包含 HashMap 创建", code.contains("HashMap"));
    }

    /**
     * 测试嵌套对象更新代码生成的基本结构。
     * 
     * <p>由于 TypeUtils.needsDeepCopy 需要真实的 TypeMirror，
     * 这里只测试方法生成的基本代码结构。
     */
    @Test
    public void testGenerateNestedObjectUpdateBasicStructure() {
        // 设置 mock - 使用简单类型（不需要深拷贝）
        DeclaredType simpleType = mock(DeclaredType.class);
        when(simpleType.toString()).thenReturn("java.lang.String");
        when(simpleType.getKind()).thenReturn(TypeKind.DECLARED);
        
        when(sourceField.getSimpleName()).thenReturn(new TestName("name"));
        when(targetField.getSimpleName()).thenReturn(new TestName("name"));
        
        FieldMapping mapping = new FieldMapping(sourceField, targetField, simpleType, simpleType);
        
        MethodSpec.Builder methodBuilder = MethodSpec.methodBuilder("testMethod");
        
        // 调用方法
        deepCopyGenerator.generateNestedObjectUpdate(
                methodBuilder,
                "getName",
                "setName",
                simpleType,
                simpleType,
                mapping,
                false
        );
        
        // 验证生成的代码包含必要的逻辑
        String code = methodBuilder.build().toString();
        assertTrue("应包含源字段 null 检查", code.contains("source.getName() != null"));
        assertTrue("应包含目标字段 null 检查", code.contains("target.getName() == null"));
    }

    /**
     * 测试数组字段更新代码生成。
     */
    @Test
    public void testGenerateCollectionUpdateForArray() {
        // 设置 mock 为数组类型
        DeclaredType arrayType = mock(DeclaredType.class);
        when(arrayType.toString()).thenReturn("java.lang.String[]");
        when(arrayType.getKind()).thenReturn(TypeKind.ARRAY);
        
        when(sourceField.getSimpleName()).thenReturn(new TestName("values"));
        when(targetField.getSimpleName()).thenReturn(new TestName("values"));
        
        FieldMapping mapping = new FieldMapping(sourceField, targetField, arrayType, arrayType);
        
        MethodSpec.Builder methodBuilder = MethodSpec.methodBuilder("testMethod");
        
        // 调用方法
        deepCopyGenerator.generateCollectionUpdate(
                methodBuilder,
                "getValues",
                "setValues",
                arrayType,
                arrayType,
                mapping,
                false
        );
        
        // 验证生成的代码
        String code = methodBuilder.build().toString();
        assertTrue("应包含源字段 null 检查", code.contains("source.getValues() != null"));
        assertTrue("应包含 clone 调用", code.contains("clone()"));
    }

    /**
     * 辅助类：模拟 Name 接口。
     */
    private static class TestName implements javax.lang.model.element.Name {
        private final String name;

        TestName(String name) {
            this.name = name;
        }

        @Override
        public boolean contentEquals(CharSequence cs) {
            return name.equals(cs.toString());
        }

        @Override
        public int length() {
            return name.length();
        }

        @Override
        public char charAt(int index) {
            return name.charAt(index);
        }

        @Override
        public CharSequence subSequence(int start, int end) {
            return name.subSequence(start, end);
        }

        @Override
        public String toString() {
            return name;
        }
    }
}
