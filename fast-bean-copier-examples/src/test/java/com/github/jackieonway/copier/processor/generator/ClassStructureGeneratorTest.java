package com.github.jackieonway.copier.processor.generator;

import com.github.jackieonway.copier.annotation.ComponentModel;
import com.github.jackieonway.copier.processor.context.ProcessorContext;
import com.squareup.javapoet.AnnotationSpec;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeSpec;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

/**
 * ClassStructureGenerator 单元测试。
 *
 * @author jackieonway
 * @since 1.2.1
 */
public class ClassStructureGeneratorTest {

    @Mock
    private ProcessingEnvironment processingEnv;

    @Mock
    private Elements elementUtils;

    @Mock
    private Types typeUtils;

    @Mock
    private Messager messager;

    @Mock
    private TypeElement targetType;

    private ProcessorContext context;
    private ClassStructureGenerator generator;

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        when(processingEnv.getElementUtils()).thenReturn(elementUtils);
        when(processingEnv.getTypeUtils()).thenReturn(typeUtils);
        when(processingEnv.getMessager()).thenReturn(messager);
        context = new ProcessorContext(processingEnv);
        generator = new ClassStructureGenerator(context);
    }

    // ========== addClassAnnotations 测试 ==========

    @Test
    public void addClassAnnotations_withDefault_shouldAddFinalModifier() {
        context.setComponentModel(ComponentModel.DEFAULT);
        TypeSpec.Builder classBuilder = TypeSpec.classBuilder("TestCopier");

        generator.addClassAnnotations(classBuilder);

        TypeSpec typeSpec = classBuilder.build();
        assertTrue(typeSpec.modifiers.contains(Modifier.FINAL));
        assertTrue(typeSpec.annotations.isEmpty());
    }

    @Test
    public void addClassAnnotations_withSpring_shouldAddComponentAnnotation() {
        context.setComponentModel(ComponentModel.SPRING);
        TypeSpec.Builder classBuilder = TypeSpec.classBuilder("TestCopier");

        generator.addClassAnnotations(classBuilder);

        TypeSpec typeSpec = classBuilder.build();
        assertFalse(typeSpec.modifiers.contains(Modifier.FINAL));
        assertEquals(1, typeSpec.annotations.size());
        AnnotationSpec annotation = typeSpec.annotations.get(0);
        assertEquals(ClassName.get("org.springframework.stereotype", "Component"), annotation.type);
    }

    @Test
    public void addClassAnnotations_withCdi_shouldAddApplicationScopedAnnotation() {
        context.setComponentModel(ComponentModel.CDI);
        TypeSpec.Builder classBuilder = TypeSpec.classBuilder("TestCopier");

        generator.addClassAnnotations(classBuilder);

        TypeSpec typeSpec = classBuilder.build();
        assertFalse(typeSpec.modifiers.contains(Modifier.FINAL));
        assertEquals(1, typeSpec.annotations.size());
        AnnotationSpec annotation = typeSpec.annotations.get(0);
        assertEquals(ClassName.get("javax.enterprise.context", "ApplicationScoped"), annotation.type);
    }

    @Test
    public void addClassAnnotations_withJsr330_shouldAddNamedAndSingletonAnnotations() {
        context.setComponentModel(ComponentModel.JSR330);
        TypeSpec.Builder classBuilder = TypeSpec.classBuilder("TestCopier");

        generator.addClassAnnotations(classBuilder);

        TypeSpec typeSpec = classBuilder.build();
        assertFalse(typeSpec.modifiers.contains(Modifier.FINAL));
        assertEquals(2, typeSpec.annotations.size());
        
        boolean hasNamed = typeSpec.annotations.stream()
                .anyMatch(a -> a.type.equals(ClassName.get("javax.inject", "Named")));
        boolean hasSingleton = typeSpec.annotations.stream()
                .anyMatch(a -> a.type.equals(ClassName.get("javax.inject", "Singleton")));
        assertTrue(hasNamed);
        assertTrue(hasSingleton);
    }

    // ========== addConverterFields 测试 ==========

    @Test
    public void addConverterFields_withDefault_shouldAddStaticFinalFields() {
        context.setComponentModel(ComponentModel.DEFAULT);
        TypeSpec.Builder classBuilder = TypeSpec.classBuilder("TestCopier");
        Set<String> converters = new LinkedHashSet<>(Arrays.asList(
                "com.example.DateConverter",
                "com.example.StringConverter"
        ));

        generator.addConverterFields(classBuilder, converters);

        TypeSpec typeSpec = classBuilder.build();
        assertEquals(2, typeSpec.fieldSpecs.size());
        
        FieldSpec dateField = typeSpec.fieldSpecs.get(0);
        assertEquals("dateConverter", dateField.name);
        assertTrue(dateField.modifiers.contains(Modifier.PRIVATE));
        assertTrue(dateField.modifiers.contains(Modifier.STATIC));
        assertTrue(dateField.modifiers.contains(Modifier.FINAL));
        assertNotNull(dateField.initializer);
        
        FieldSpec stringField = typeSpec.fieldSpecs.get(1);
        assertEquals("stringConverter", stringField.name);
    }


    @Test
    public void addConverterFields_withSpring_shouldAddInstanceFields() {
        context.setComponentModel(ComponentModel.SPRING);
        TypeSpec.Builder classBuilder = TypeSpec.classBuilder("TestCopier");
        Set<String> converters = Collections.singleton("com.example.DateConverter");

        generator.addConverterFields(classBuilder, converters);

        TypeSpec typeSpec = classBuilder.build();
        assertEquals(1, typeSpec.fieldSpecs.size());
        
        FieldSpec field = typeSpec.fieldSpecs.get(0);
        assertEquals("dateConverter", field.name);
        assertTrue(field.modifiers.contains(Modifier.PRIVATE));
        assertTrue(field.modifiers.contains(Modifier.FINAL));
        assertFalse(field.modifiers.contains(Modifier.STATIC));
        // 实例字段没有初始化器（initializer 为空的 CodeBlock）
        assertTrue(field.initializer == null || field.initializer.isEmpty());
    }

    @Test
    public void addConverterFields_withEmptySet_shouldNotAddFields() {
        context.setComponentModel(ComponentModel.DEFAULT);
        TypeSpec.Builder classBuilder = TypeSpec.classBuilder("TestCopier");

        generator.addConverterFields(classBuilder, Collections.emptySet());

        TypeSpec typeSpec = classBuilder.build();
        assertTrue(typeSpec.fieldSpecs.isEmpty());
    }

    // ========== addUsesFields 测试 ==========

    @Test
    public void addUsesFields_withDefault_shouldAddStaticFinalFields() {
        context.setComponentModel(ComponentModel.DEFAULT);
        TypeSpec.Builder classBuilder = TypeSpec.classBuilder("TestCopier");
        
        TypeMirror usesClass = mock(TypeMirror.class);
        when(usesClass.toString()).thenReturn("com.example.StringUtils");
        List<TypeMirror> usesClasses = Collections.singletonList(usesClass);

        generator.addUsesFields(classBuilder, usesClasses);

        TypeSpec typeSpec = classBuilder.build();
        assertEquals(1, typeSpec.fieldSpecs.size());
        
        FieldSpec field = typeSpec.fieldSpecs.get(0);
        assertEquals("stringUtils", field.name);
        assertTrue(field.modifiers.contains(Modifier.PRIVATE));
        assertTrue(field.modifiers.contains(Modifier.STATIC));
        assertTrue(field.modifiers.contains(Modifier.FINAL));
    }

    @Test
    public void addUsesFields_withCdi_shouldAddInstanceFields() {
        context.setComponentModel(ComponentModel.CDI);
        TypeSpec.Builder classBuilder = TypeSpec.classBuilder("TestCopier");
        
        TypeMirror usesClass = mock(TypeMirror.class);
        when(usesClass.toString()).thenReturn("com.example.StringUtils");
        List<TypeMirror> usesClasses = Collections.singletonList(usesClass);

        generator.addUsesFields(classBuilder, usesClasses);

        TypeSpec typeSpec = classBuilder.build();
        assertEquals(1, typeSpec.fieldSpecs.size());
        
        FieldSpec field = typeSpec.fieldSpecs.get(0);
        assertEquals("stringUtils", field.name);
        assertTrue(field.modifiers.contains(Modifier.PRIVATE));
        assertTrue(field.modifiers.contains(Modifier.FINAL));
        assertFalse(field.modifiers.contains(Modifier.STATIC));
    }

    @Test
    public void addUsesFields_withEmptyList_shouldNotAddFields() {
        context.setComponentModel(ComponentModel.DEFAULT);
        TypeSpec.Builder classBuilder = TypeSpec.classBuilder("TestCopier");

        generator.addUsesFields(classBuilder, Collections.emptyList());

        TypeSpec typeSpec = classBuilder.build();
        assertTrue(typeSpec.fieldSpecs.isEmpty());
    }

    // ========== addConstructors 测试 ==========

    @Test
    public void addConstructors_withDefault_shouldAddPrivateConstructor() {
        context.setComponentModel(ComponentModel.DEFAULT);
        context.setTargetType(targetType);
        // 使用 doReturn 来避免 Name 接口的 mock 问题
        javax.lang.model.element.Name mockName = mock(javax.lang.model.element.Name.class);
        doReturn("UserDto").when(mockName).toString();
        doReturn(mockName).when(targetType).getSimpleName();
        
        TypeSpec.Builder classBuilder = TypeSpec.classBuilder("UserDtoCopier");

        generator.addConstructors(classBuilder, Collections.emptySet(), Collections.emptyList());

        TypeSpec typeSpec = classBuilder.build();
        assertEquals(1, typeSpec.methodSpecs.size());
        
        MethodSpec constructor = typeSpec.methodSpecs.get(0);
        assertTrue(constructor.isConstructor());
        assertTrue(constructor.modifiers.contains(Modifier.PRIVATE));
        assertTrue(constructor.code.toString().contains("AssertionError"));
        assertTrue(constructor.code.toString().contains("UserDtoCopier"));
    }


    @Test
    public void addConstructors_withSpring_shouldAddNoArgAndAutowiredConstructors() {
        context.setComponentModel(ComponentModel.SPRING);
        TypeSpec.Builder classBuilder = TypeSpec.classBuilder("TestCopier");
        Set<String> converters = Collections.singleton("com.example.DateConverter");

        generator.addConstructors(classBuilder, converters, Collections.emptyList());

        TypeSpec typeSpec = classBuilder.build();
        assertEquals(2, typeSpec.methodSpecs.size());
        
        // 无参构造器
        MethodSpec noArgConstructor = typeSpec.methodSpecs.get(0);
        assertTrue(noArgConstructor.isConstructor());
        assertTrue(noArgConstructor.modifiers.contains(Modifier.PUBLIC));
        assertTrue(noArgConstructor.parameters.isEmpty());
        assertTrue(noArgConstructor.code.toString().contains("dateConverter"));
        
        // 带参数的构造器
        MethodSpec injectionConstructor = typeSpec.methodSpecs.get(1);
        assertTrue(injectionConstructor.isConstructor());
        assertTrue(injectionConstructor.modifiers.contains(Modifier.PUBLIC));
        assertEquals(1, injectionConstructor.parameters.size());
        
        boolean hasAutowired = injectionConstructor.annotations.stream()
                .anyMatch(a -> a.type.equals(ClassName.get("org.springframework.beans.factory.annotation", "Autowired")));
        assertTrue(hasAutowired);
    }

    @Test
    public void addConstructors_withCdi_shouldAddNoArgAndInjectConstructors() {
        context.setComponentModel(ComponentModel.CDI);
        TypeSpec.Builder classBuilder = TypeSpec.classBuilder("TestCopier");
        Set<String> converters = Collections.singleton("com.example.DateConverter");

        generator.addConstructors(classBuilder, converters, Collections.emptyList());

        TypeSpec typeSpec = classBuilder.build();
        assertEquals(2, typeSpec.methodSpecs.size());
        
        // 带参数的构造器应该有 @Inject 注解
        MethodSpec injectionConstructor = typeSpec.methodSpecs.get(1);
        boolean hasInject = injectionConstructor.annotations.stream()
                .anyMatch(a -> a.type.equals(ClassName.get("javax.inject", "Inject")));
        assertTrue(hasInject);
    }

    @Test
    public void addConstructors_withJsr330_shouldAddNoArgAndInjectConstructors() {
        context.setComponentModel(ComponentModel.JSR330);
        TypeSpec.Builder classBuilder = TypeSpec.classBuilder("TestCopier");
        
        TypeMirror usesClass = mock(TypeMirror.class);
        when(usesClass.toString()).thenReturn("com.example.StringUtils");
        List<TypeMirror> usesClasses = Collections.singletonList(usesClass);

        generator.addConstructors(classBuilder, Collections.emptySet(), usesClasses);

        TypeSpec typeSpec = classBuilder.build();
        assertEquals(2, typeSpec.methodSpecs.size());
        
        // 带参数的构造器应该有 @Inject 注解
        MethodSpec injectionConstructor = typeSpec.methodSpecs.get(1);
        boolean hasInject = injectionConstructor.annotations.stream()
                .anyMatch(a -> a.type.equals(ClassName.get("javax.inject", "Inject")));
        assertTrue(hasInject);
        assertEquals(1, injectionConstructor.parameters.size());
        assertEquals("stringUtils", injectionConstructor.parameters.get(0).name);
    }

    @Test
    public void addConstructors_withSpringNoDependencies_shouldAddOnlyNoArgConstructor() {
        context.setComponentModel(ComponentModel.SPRING);
        TypeSpec.Builder classBuilder = TypeSpec.classBuilder("TestCopier");

        generator.addConstructors(classBuilder, Collections.emptySet(), Collections.emptyList());

        TypeSpec typeSpec = classBuilder.build();
        assertEquals(1, typeSpec.methodSpecs.size());
        
        MethodSpec noArgConstructor = typeSpec.methodSpecs.get(0);
        assertTrue(noArgConstructor.isConstructor());
        assertTrue(noArgConstructor.modifiers.contains(Modifier.PUBLIC));
        assertTrue(noArgConstructor.parameters.isEmpty());
    }

    // ========== getConverterFieldName 测试 ==========

    @Test
    public void getConverterFieldName_shouldConvertToLowerCamelCase() {
        assertEquals("dateConverter", generator.getConverterFieldName("com.example.DateConverter"));
        assertEquals("stringConverter", generator.getConverterFieldName("StringConverter"));
        assertEquals("myCustomConverter", generator.getConverterFieldName("com.foo.bar.MyCustomConverter"));
    }

    // ========== getUsesFieldName 测试 ==========

    @Test
    public void getUsesFieldName_shouldConvertToLowerCamelCase() {
        assertEquals("stringUtils", generator.getUsesFieldName("com.example.StringUtils"));
        assertEquals("dateHelper", generator.getUsesFieldName("DateHelper"));
        assertEquals("myMapper", generator.getUsesFieldName("com.foo.bar.MyMapper"));
    }

    // ========== 辅助方法 ==========
}
