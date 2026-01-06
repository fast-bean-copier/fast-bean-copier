package com.github.jackieonway.copier.processor.analyzer;

import com.github.jackieonway.copier.annotation.CopyField;
import com.github.jackieonway.copier.processor.FieldMapping;
import com.github.jackieonway.copier.processor.context.ProcessorContext;
import com.github.jackieonway.copier.processor.extractor.AnnotationExtractor;
import com.github.jackieonway.copier.processor.model.CopyFieldConfig;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.Name;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

/**
 * FieldMappingAnalyzer 单元测试。
 *
 * <p>验证字段映射分析的正确性：
 * <ul>
 *   <li>同名且类型兼容的字段被正确映射</li>
 *   <li>带 @CopyField 注解的字段按注解配置映射</li>
 *   <li>映射类型（SIMPLE、EXPRESSION、CONVERTER 等）正确识别</li>
 * </ul>
 *
 * <p><b>Property 3: 字段映射分析正确性</b>
 * <p><b>Validates: Requirements 2.1, 2.2, 2.3, 2.4, 2.5, 2.6, 2.7</b>
 *
 * @author jackieonway
 * @since 1.2.1
 */
public class FieldMappingAnalyzerTest {

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
    private AnnotationExtractor extractor;

    private ProcessorContext context;
    private FieldMappingAnalyzer analyzer;

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        when(processingEnv.getElementUtils()).thenReturn(elementUtils);
        when(processingEnv.getTypeUtils()).thenReturn(typeUtils);
        when(processingEnv.getMessager()).thenReturn(messager);
        context = new ProcessorContext(processingEnv);
        analyzer = new FieldMappingAnalyzer(context, extractor);
    }

    // ========== 简单映射测试 (Requirements 2.1) ==========

    /**
     * 测试同名字段的简单映射。
     * Validates: Requirements 2.1
     */
    @Test
    public void createSimpleMapping_withSingleSourceField_shouldCreateMapping() {
        // 准备
        VariableElement targetField = createMockField("name", "java.lang.String");
        VariableElement sourceField = createMockField("name", "java.lang.String");
        Map<String, VariableElement> sourceFieldMap = new HashMap<>();
        sourceFieldMap.put("name", sourceField);

        // 执行
        FieldMapping mapping = analyzer.createSimpleMapping(
                targetField, targetField.asType(), new String[]{"name"}, sourceFieldMap);

        // 验证
        assertNotNull(mapping);
        assertEquals(sourceField, mapping.getSourceField());
        assertEquals(targetField, mapping.getTargetField());
        assertEquals(FieldMapping.MappingType.SIMPLE, mapping.getMappingType());
    }

    /**
     * 测试找不到源字段时返回 null。
     * Validates: Requirements 2.1
     */
    @Test
    public void createSimpleMapping_withMissingSourceField_shouldReturnNull() {
        // 准备
        VariableElement targetField = createMockField("name", "java.lang.String");
        Map<String, VariableElement> sourceFieldMap = new HashMap<>();

        // 执行
        FieldMapping mapping = analyzer.createSimpleMapping(
                targetField, targetField.asType(), new String[]{"nonExistent"}, sourceFieldMap);

        // 验证
        assertNull(mapping);
    }


    /**
     * 测试多对一映射没有表达式时报错。
     * Validates: Requirements 2.6
     */
    @Test
    public void createSimpleMapping_withMultipleSourceFieldsWithoutExpression_shouldReturnNull() {
        // 准备
        VariableElement targetField = createMockField("fullName", "java.lang.String");
        VariableElement sourceField1 = createMockField("firstName", "java.lang.String");
        VariableElement sourceField2 = createMockField("lastName", "java.lang.String");
        Map<String, VariableElement> sourceFieldMap = new HashMap<>();
        sourceFieldMap.put("firstName", sourceField1);
        sourceFieldMap.put("lastName", sourceField2);

        // 执行
        FieldMapping mapping = analyzer.createSimpleMapping(
                targetField, targetField.asType(), new String[]{"firstName", "lastName"}, sourceFieldMap);

        // 验证
        assertNull(mapping);
    }

    // ========== 表达式映射测试 (Requirements 2.3) ==========

    /**
     * 测试表达式映射的创建。
     * Validates: Requirements 2.3
     */
    @Test
    public void createExpressionMapping_withValidExpression_shouldCreateMapping() {
        // 准备
        VariableElement targetField = createMockField("fullName", "java.lang.String");
        Map<String, VariableElement> sourceFieldMap = new HashMap<>();
        TypeMirror sourceTypeMirror = mock(TypeMirror.class);
        when(sourceType.asType()).thenReturn(sourceTypeMirror);

        // 执行
        FieldMapping mapping = analyzer.createExpressionMapping(
                targetField, targetField.asType(),
                new String[]{"firstName", "lastName"},
                "source.getFirstName() + \" \" + source.getLastName()",
                sourceFieldMap, sourceType);

        // 验证
        assertNotNull(mapping);
        assertEquals(FieldMapping.MappingType.MANY_TO_ONE, mapping.getMappingType());
        assertEquals("source.getFirstName() + \" \" + source.getLastName()", mapping.getExpression());
        assertEquals(2, mapping.getSourceFieldNames().size());
    }

    /**
     * 测试单源字段的表达式映射。
     * Validates: Requirements 2.3
     */
    @Test
    public void createExpressionMapping_withSingleSourceField_shouldCreateExpressionType() {
        // 准备
        VariableElement targetField = createMockField("upperName", "java.lang.String");
        Map<String, VariableElement> sourceFieldMap = new HashMap<>();
        TypeMirror sourceTypeMirror = mock(TypeMirror.class);
        when(sourceType.asType()).thenReturn(sourceTypeMirror);

        // 执行
        FieldMapping mapping = analyzer.createExpressionMapping(
                targetField, targetField.asType(),
                new String[]{"name"},
                "source.getName().toUpperCase()",
                sourceFieldMap, sourceType);

        // 验证
        assertNotNull(mapping);
        assertEquals(FieldMapping.MappingType.EXPRESSION, mapping.getMappingType());
        assertEquals("source.getName().toUpperCase()", mapping.getExpression());
    }

    /**
     * 测试无效表达式语法时返回 null。
     * Validates: Requirements 2.3
     */
    @Test
    public void createExpressionMapping_withInvalidSyntax_shouldReturnNull() {
        // 准备
        VariableElement targetField = createMockField("value", "java.lang.String");
        Map<String, VariableElement> sourceFieldMap = new HashMap<>();
        TypeMirror sourceTypeMirror = mock(TypeMirror.class);
        when(sourceType.asType()).thenReturn(sourceTypeMirror);

        // 执行 - 括号不匹配
        FieldMapping mapping = analyzer.createExpressionMapping(
                targetField, targetField.asType(),
                null,
                "source.getValue((",
                sourceFieldMap, sourceType);

        // 验证
        assertNull(mapping);
    }

    // ========== 转换器映射测试 (Requirements 2.4) ==========

    /**
     * 测试转换器映射的创建。
     * Validates: Requirements 2.4
     */
    @Test
    public void createConverterMapping_withValidConverter_shouldCreateMapping() {
        // 准备
        VariableElement targetField = createMockField("dateStr", "java.lang.String");
        VariableElement sourceField = createMockField("date", "java.util.Date");
        Map<String, VariableElement> sourceFieldMap = new HashMap<>();
        sourceFieldMap.put("date", sourceField);

        // 执行
        FieldMapping mapping = analyzer.createConverterMapping(
                targetField, targetField.asType(),
                new String[]{"date"},
                "com.example.DateConverter",
                "yyyy-MM-dd",
                sourceFieldMap);

        // 验证
        assertNotNull(mapping);
        assertEquals(FieldMapping.MappingType.CONVERTER, mapping.getMappingType());
        assertEquals("com.example.DateConverter", mapping.getConverterClassName());
        assertEquals("yyyy-MM-dd", mapping.getFormat());
    }

    /**
     * 测试转换器映射使用目标字段名作为默认源字段名。
     * Validates: Requirements 2.4
     */
    @Test
    public void createConverterMapping_withoutSourceName_shouldUseTargetFieldName() {
        // 准备
        VariableElement targetField = createMockField("value", "java.lang.String");
        VariableElement sourceField = createMockField("value", "java.lang.Integer");
        Map<String, VariableElement> sourceFieldMap = new HashMap<>();
        sourceFieldMap.put("value", sourceField);

        // 执行
        FieldMapping mapping = analyzer.createConverterMapping(
                targetField, targetField.asType(),
                null, // 没有指定源字段名
                "com.example.IntToStringConverter",
                null,
                sourceFieldMap);

        // 验证
        assertNotNull(mapping);
        assertEquals(sourceField, mapping.getSourceField());
    }


    /**
     * 测试转换器映射找不到源字段时返回 null。
     * Validates: Requirements 2.4
     */
    @Test
    public void createConverterMapping_withMissingSourceField_shouldReturnNull() {
        // 准备
        VariableElement targetField = createMockField("value", "java.lang.String");
        Map<String, VariableElement> sourceFieldMap = new HashMap<>();

        // 执行
        FieldMapping mapping = analyzer.createConverterMapping(
                targetField, targetField.asType(),
                new String[]{"nonExistent"},
                "com.example.Converter",
                null,
                sourceFieldMap);

        // 验证
        assertNull(mapping);
    }

    // ========== 具名方法映射测试 (Requirements 2.5) ==========

    /**
     * 测试具名方法映射的创建。
     * Validates: Requirements 2.5
     */
    @Test
    public void createQualifiedByNameMapping_withValidMethod_shouldCreateMapping() {
        // 准备
        VariableElement targetField = createMockField("formattedValue", "java.lang.String");
        VariableElement sourceField = createMockField("value", "java.lang.Integer");
        Map<String, VariableElement> sourceFieldMap = new HashMap<>();
        sourceFieldMap.put("value", sourceField);

        // 执行
        FieldMapping mapping = analyzer.createQualifiedByNameMapping(
                targetField, targetField.asType(),
                new String[]{"value"},
                "formatValue",
                sourceFieldMap);

        // 验证
        assertNotNull(mapping);
        assertEquals(FieldMapping.MappingType.QUALIFIED_BY_NAME, mapping.getMappingType());
        assertEquals("formatValue", mapping.getQualifiedByName());
    }

    /**
     * 测试具名方法映射使用目标字段名作为默认源字段名。
     * Validates: Requirements 2.5
     */
    @Test
    public void createQualifiedByNameMapping_withoutSourceName_shouldUseTargetFieldName() {
        // 准备
        VariableElement targetField = createMockField("value", "java.lang.String");
        VariableElement sourceField = createMockField("value", "java.lang.Integer");
        Map<String, VariableElement> sourceFieldMap = new HashMap<>();
        sourceFieldMap.put("value", sourceField);

        // 执行
        FieldMapping mapping = analyzer.createQualifiedByNameMapping(
                targetField, targetField.asType(),
                null, // 没有指定源字段名
                "convertValue",
                sourceFieldMap);

        // 验证
        assertNotNull(mapping);
        assertEquals(sourceField, mapping.getSourceField());
    }

    // ========== 类型兼容性测试 (Requirements 2.7) ==========

    /**
     * 测试相同类型的兼容性。
     * Validates: Requirements 2.7
     */
    @Test
    public void isTypeCompatible_withSameType_shouldReturnTrue() {
        TypeMirror stringType = createMockTypeMirror("java.lang.String");

        boolean result = analyzer.isTypeCompatible(stringType, stringType);

        assertTrue(result);
    }

    /**
     * 测试基本类型和包装类型的兼容性。
     * Validates: Requirements 2.7
     */
    @Test
    public void isTypeCompatible_withPrimitiveAndWrapper_shouldReturnTrue() {
        TypeMirror intType = createMockPrimitiveType(TypeKind.INT);
        TypeMirror integerType = createMockTypeMirror("java.lang.Integer");

        boolean result = analyzer.isTypeCompatible(intType, integerType);

        assertTrue(result);
    }

    // ========== 辅助方法 ==========

    /**
     * 创建模拟的字段元素。
     */
    private VariableElement createMockField(String fieldName, String typeName) {
        VariableElement field = mock(VariableElement.class);
        Name name = mock(Name.class);
        when(name.toString()).thenReturn(fieldName);
        when(field.getSimpleName()).thenReturn(name);

        TypeMirror type = createMockTypeMirror(typeName);
        when(field.asType()).thenReturn(type);

        return field;
    }

    /**
     * 创建模拟的类型镜像。
     */
    private TypeMirror createMockTypeMirror(String typeName) {
        TypeMirror type = mock(TypeMirror.class);
        when(type.toString()).thenReturn(typeName);
        when(type.getKind()).thenReturn(TypeKind.DECLARED);
        return type;
    }

    /**
     * 创建模拟的基本类型镜像。
     */
    private TypeMirror createMockPrimitiveType(TypeKind kind) {
        TypeMirror type = mock(TypeMirror.class);
        when(type.getKind()).thenReturn(kind);
        when(type.toString()).thenReturn(kind.name().toLowerCase());
        return type;
    }
}
