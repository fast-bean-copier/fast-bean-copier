package com.github.jackieonway.copier.processor.generator;

import com.github.jackieonway.copier.processor.FieldMapping;
import com.github.jackieonway.copier.processor.context.ProcessorContext;
import com.squareup.javapoet.MethodSpec;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.Name;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

/**
 * FieldCopyGenerator 单元测试。
 *
 * <p>测试字段拷贝代码生成器的各种映射类型处理。
 *
 * @author jackieonway
 * @since 1.2.1
 */
public class FieldCopyGeneratorTest {

    @Mock
    private ProcessingEnvironment processingEnv;

    @Mock
    private Elements elementUtils;

    @Mock
    private Types typeUtils;

    @Mock
    private Messager messager;

    private ProcessorContext context;
    private FieldCopyGenerator generator;

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        when(processingEnv.getElementUtils()).thenReturn(elementUtils);
        when(processingEnv.getTypeUtils()).thenReturn(typeUtils);
        when(processingEnv.getMessager()).thenReturn(messager);
        context = new ProcessorContext(processingEnv);
        generator = new FieldCopyGenerator(context);
    }

    /**
     * 创建 mock 的 VariableElement。
     */
    private VariableElement createMockField(String fieldName) {
        VariableElement field = mock(VariableElement.class);
        Name name = mock(Name.class);
        when(name.toString()).thenReturn(fieldName);
        when(field.getSimpleName()).thenReturn(name);
        return field;
    }

    /**
     * 创建 mock 的 TypeMirror。
     */
    private TypeMirror createMockType(String typeName) {
        TypeMirror type = mock(TypeMirror.class);
        when(type.getKind()).thenReturn(TypeKind.DECLARED);
        when(type.toString()).thenReturn(typeName);
        return type;
    }

    // ========== 简单字段拷贝测试 ==========

    @Test
    public void generateFieldCopyCode_simpleMapping_shouldGenerateGetterSetterCall() {
        MethodSpec.Builder methodBuilder = MethodSpec.methodBuilder("toDto")
                .addModifiers(Modifier.PUBLIC);
        
        TypeMirror stringType = createMockType("java.lang.String");
        VariableElement sourceField = createMockField("name");
        VariableElement targetField = createMockField("name");
        
        FieldMapping mapping = new FieldMapping(sourceField, targetField, stringType, stringType);
        mapping.setMappingType(FieldMapping.MappingType.SIMPLE);
        
        generator.generateFieldCopyCode(methodBuilder, mapping, false);
        
        MethodSpec method = methodBuilder.build();
        String code = method.toString();
        
        assertTrue("应该包含 getter 调用", code.contains("getName()"));
        assertTrue("应该包含 setter 调用", code.contains("setName("));
    }

    @Test
    public void generateFieldCopyCode_simpleMapping_reverse_shouldSwapSourceAndTarget() {
        MethodSpec.Builder methodBuilder = MethodSpec.methodBuilder("fromDto")
                .addModifiers(Modifier.PUBLIC);
        
        TypeMirror stringType = createMockType("java.lang.String");
        VariableElement sourceField = createMockField("sourceName");
        VariableElement targetField = createMockField("targetName");
        
        FieldMapping mapping = new FieldMapping(sourceField, targetField, stringType, stringType);
        mapping.setMappingType(FieldMapping.MappingType.SIMPLE);
        
        generator.generateFieldCopyCode(methodBuilder, mapping, true);
        
        MethodSpec method = methodBuilder.build();
        String code = method.toString();
        
        // 反向拷贝时，source 和 target 交换
        assertTrue("应该从 targetName 获取值", code.contains("getTargetName()"));
        assertTrue("应该设置到 sourceName", code.contains("setSourceName("));
    }

    @Test
    public void generateFieldCopyCode_nullSourceField_shouldSkip() {
        MethodSpec.Builder methodBuilder = MethodSpec.methodBuilder("toDto")
                .addModifiers(Modifier.PUBLIC);
        
        TypeMirror stringType = createMockType("java.lang.String");
        VariableElement targetField = createMockField("name");
        
        // sourceField 为 null
        FieldMapping mapping = new FieldMapping(null, targetField, null, stringType);
        mapping.setMappingType(FieldMapping.MappingType.SIMPLE);
        
        generator.generateFieldCopyCode(methodBuilder, mapping, false);
        
        MethodSpec method = methodBuilder.build();
        String code = method.toString();
        
        assertFalse("不应该生成任何代码", code.contains("target."));
    }

    // ========== 表达式字段拷贝测试 ==========

    @Test
    public void generateFieldCopyCode_expressionMapping_shouldUseExpression() {
        MethodSpec.Builder methodBuilder = MethodSpec.methodBuilder("toDto")
                .addModifiers(Modifier.PUBLIC);
        
        TypeMirror stringType = createMockType("java.lang.String");
        VariableElement sourceField = createMockField("firstName");
        VariableElement targetField = createMockField("fullName");
        
        FieldMapping mapping = new FieldMapping(sourceField, targetField, stringType, stringType);
        mapping.setExpression("source.getFirstName() + \" \" + source.getLastName()");
        mapping.setMappingType(FieldMapping.MappingType.EXPRESSION);
        
        generator.generateFieldCopyCode(methodBuilder, mapping, false);
        
        MethodSpec method = methodBuilder.build();
        String code = method.toString();
        
        assertTrue("应该包含表达式", code.contains("source.getFirstName()"));
        assertTrue("应该设置到 fullName", code.contains("setFullName("));
    }

    @Test
    public void generateFieldCopyCode_expressionMapping_reverse_shouldSkip() {
        MethodSpec.Builder methodBuilder = MethodSpec.methodBuilder("fromDto")
                .addModifiers(Modifier.PUBLIC);
        
        TypeMirror stringType = createMockType("java.lang.String");
        VariableElement sourceField = createMockField("firstName");
        VariableElement targetField = createMockField("fullName");
        
        FieldMapping mapping = new FieldMapping(sourceField, targetField, stringType, stringType);
        mapping.setExpression("source.getFirstName() + \" \" + source.getLastName()");
        mapping.setMappingType(FieldMapping.MappingType.EXPRESSION);
        
        generator.generateFieldCopyCode(methodBuilder, mapping, true);
        
        MethodSpec method = methodBuilder.build();
        String code = method.toString();
        
        // 反向拷贝时，表达式映射应该被跳过（不包含表达式内容）
        assertFalse("不应该包含表达式中的 getLastName", code.contains("getLastName()"));
    }

    @Test
    public void generateFieldCopyCode_emptyExpression_shouldSkip() {
        MethodSpec.Builder methodBuilder = MethodSpec.methodBuilder("toDto")
                .addModifiers(Modifier.PUBLIC);
        
        TypeMirror stringType = createMockType("java.lang.String");
        VariableElement sourceField = createMockField("name");
        VariableElement targetField = createMockField("name");
        
        FieldMapping mapping = new FieldMapping(sourceField, targetField, stringType, stringType);
        mapping.setExpression("   ");
        mapping.setMappingType(FieldMapping.MappingType.EXPRESSION);
        
        generator.generateFieldCopyCode(methodBuilder, mapping, false);
        
        MethodSpec method = methodBuilder.build();
        String code = method.toString();
        
        assertFalse("不应该生成任何代码", code.contains("target."));
    }

    // ========== 转换器字段拷贝测试 ==========

    @Test
    public void generateFieldCopyCode_converterMapping_staticMethod_shouldCallConverter() {
        MethodSpec.Builder methodBuilder = MethodSpec.methodBuilder("toDto")
                .addModifiers(Modifier.PUBLIC);
        
        TypeMirror stringType = createMockType("java.lang.String");
        VariableElement sourceField = createMockField("date");
        VariableElement targetField = createMockField("dateStr");
        
        FieldMapping mapping = new FieldMapping(sourceField, targetField, stringType, stringType);
        mapping.setConverterClassName("com.example.DateConverter");
        mapping.setFormat("yyyy-MM-dd");
        mapping.setMappingType(FieldMapping.MappingType.CONVERTER);
        
        generator.setUseStaticMethods(true);
        generator.generateFieldCopyCode(methodBuilder, mapping, false);
        
        MethodSpec method = methodBuilder.build();
        String code = method.toString();
        
        assertTrue("应该调用转换器", code.contains("dateConverter.convert"));
        assertTrue("应该包含格式参数", code.contains("yyyy-MM-dd"));
    }

    @Test
    public void generateFieldCopyCode_converterMapping_instanceMethod_shouldUseThis() {
        MethodSpec.Builder methodBuilder = MethodSpec.methodBuilder("toDto")
                .addModifiers(Modifier.PUBLIC);
        
        TypeMirror stringType = createMockType("java.lang.String");
        VariableElement sourceField = createMockField("date");
        VariableElement targetField = createMockField("dateStr");
        
        FieldMapping mapping = new FieldMapping(sourceField, targetField, stringType, stringType);
        mapping.setConverterClassName("com.example.DateConverter");
        mapping.setMappingType(FieldMapping.MappingType.CONVERTER);
        
        generator.setUseStaticMethods(false);
        generator.generateFieldCopyCode(methodBuilder, mapping, false);
        
        MethodSpec method = methodBuilder.build();
        String code = method.toString();
        
        assertTrue("应该使用 this 调用转换器", code.contains("this.dateConverter.convert"));
    }

    // ========== qualifiedByName 字段拷贝测试 ==========

    @Test
    public void generateFieldCopyCode_qualifiedByNameMapping_reverse_shouldSkip() {
        MethodSpec.Builder methodBuilder = MethodSpec.methodBuilder("fromDto")
                .addModifiers(Modifier.PUBLIC);
        
        TypeMirror stringType = createMockType("java.lang.String");
        VariableElement sourceField = createMockField("name");
        VariableElement targetField = createMockField("formattedName");
        
        FieldMapping mapping = new FieldMapping(sourceField, targetField, stringType, stringType);
        mapping.setQualifiedByName("formatName");
        mapping.setMappingType(FieldMapping.MappingType.QUALIFIED_BY_NAME);
        
        generator.generateFieldCopyCode(methodBuilder, mapping, true);
        
        MethodSpec method = methodBuilder.build();
        String code = method.toString();
        
        // 反向拷贝时，qualifiedByName 映射应该被跳过
        assertFalse("不应该生成任何代码", code.contains("target."));
    }

    // ========== 构造器和配置测试 ==========

    @Test
    public void constructor_shouldAcceptProcessorContext() {
        FieldCopyGenerator gen = new FieldCopyGenerator(context);
        assertNotNull("应该成功创建 FieldCopyGenerator", gen);
    }

    @Test
    public void setUseStaticMethods_shouldAffectCodeGeneration() {
        generator.setUseStaticMethods(true);
        assertNotNull(generator);
    }

    @Test
    public void setUsesClasses_shouldAcceptList() {
        generator.setUsesClasses(java.util.Collections.emptyList());
        assertNotNull(generator);
    }
}
