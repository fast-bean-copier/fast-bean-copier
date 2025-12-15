package com.github.jackieonway.copier.processor;

import org.junit.BeforeClass;
import org.junit.Test;

import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeMirror;
import javax.tools.JavaCompiler;
import javax.tools.JavaFileObject;
import javax.tools.SimpleJavaFileObject;
import javax.tools.StandardJavaFileManager;
import javax.tools.ToolProvider;
import java.io.IOException;
import java.lang.reflect.Method;
import java.net.URI;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.*;

/**
 * 集合与泛型相关的 TypeUtils 测试。
 */
public class CollectionTypeUtilsTest {

    private static Map<String, TypeMirror> fieldTypes;

    @BeforeClass
    public static void init() throws Exception {
        String source = "package test; "
                + "import java.util.*; "
                + "public class Sample { "
                + "List<String> stringList; "
                + "Set<Integer> intSet; "
                + "Map<String, Long> map; "
                + "String[] stringArray; "
                + "List rawList; "
                + "List<? extends Number> wildcardList; "
                + "String[][] multiArray; "
                + "int[] primitiveArray; "
                + "Map<String, List<Integer>> mapList; "
                + "String name; "
                + "}";
        fieldTypes = compileAndGetFieldTypes(source, "test.Sample");
    }

    @Test
    public void shouldRecognizeCollectionTypes() {
        assertTrue(TypeUtils.isList(fieldTypes.get("stringList")));
        assertTrue(TypeUtils.isSet(fieldTypes.get("intSet")));
        assertTrue(TypeUtils.isMap(fieldTypes.get("map")));
        assertTrue(TypeUtils.isArrayType(fieldTypes.get("stringArray")));
        assertFalse(TypeUtils.isCollectionType(fieldTypes.get("name")));
        assertTrue(TypeUtils.isCollectionType(fieldTypes.get("map")));
    }

    @Test
    public void shouldExtractListTypeArgument() {
        List<TypeMirror> args = TypeUtils.extractTypeArguments(fieldTypes.get("stringList"));
        assertEquals(1, args.size());
        assertEquals("java.lang.String", args.get(0).toString());
    }

    @Test
    public void shouldHandleRawType() {
        List<TypeMirror> args = TypeUtils.extractTypeArguments(fieldTypes.get("rawList"));
        assertTrue(args.isEmpty());
    }

    @Test
    public void shouldResolveWildcardBound() {
        List<TypeMirror> args = TypeUtils.extractTypeArguments(fieldTypes.get("wildcardList"));
        assertEquals(1, args.size());
        assertEquals("java.lang.Number", args.get(0).toString());
    }

    @Test
    public void shouldExtractMapKeyAndValue() {
        TypeMirror keyType = TypeUtils.extractMapKeyType(fieldTypes.get("map"));
        TypeMirror valueType = TypeUtils.extractMapValueType(fieldTypes.get("map"));
        assertEquals("java.lang.String", keyType.toString());
        assertEquals("java.lang.Long", valueType.toString());
    }

    @Test
    public void shouldExtractNestedMapValue() {
        TypeMirror valueType = TypeUtils.extractMapValueType(fieldTypes.get("mapList"));
        assertNotNull(valueType);
        assertEquals("java.util.List<java.lang.Integer>", valueType.toString());
    }

    @Test
    public void shouldGetArrayComponentType() {
        TypeMirror component = TypeUtils.getArrayComponentType(fieldTypes.get("stringArray"));
        assertEquals("java.lang.String", component.toString());

        TypeMirror multiComponent = TypeUtils.getArrayComponentType(fieldTypes.get("multiArray"));
        assertEquals("java.lang.String", multiComponent.toString());

        TypeMirror primitiveComponent = TypeUtils.getArrayComponentType(fieldTypes.get("primitiveArray"));
        assertEquals("int", primitiveComponent.toString());
    }

    private static Map<String, TypeMirror> compileAndGetFieldTypes(String source, String className) throws Exception {
        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        assertNotNull("JDK 编译器不可用", compiler);

        try (StandardJavaFileManager fileManager = compiler.getStandardFileManager(null, null, null)) {
            JavaFileObject fileObject = new InMemoryJavaFileObject(className, source);
            JavaCompiler.CompilationTask task = compiler.getTask(null, fileManager, null,
                    Arrays.asList("-proc:none"), null, Collections.singletonList(fileObject));

            invokeNoArg(task, "parse");
            invokeNoArg(task, "analyze");

            Object elements = invokeNoArg(task, "getElements");
            Method getTypeElement = elements.getClass().getMethod("getTypeElement", CharSequence.class);
            TypeElement typeElement = (TypeElement) getTypeElement.invoke(elements, className);

            Map<String, TypeMirror> result = new HashMap<>();
            for (Element element : typeElement.getEnclosedElements()) {
                if (element.getKind() == ElementKind.FIELD) {
                    VariableElement field = (VariableElement) element;
                    result.put(field.getSimpleName().toString(), field.asType());
                }
            }
            return result;
        }
    }

    private static Object invokeNoArg(Object target, String methodName) throws Exception {
        Method method = target.getClass().getMethod(methodName);
        return method.invoke(target);
    }

    /**
     * 内存中的 Java 源文件对象。
     */
    private static class InMemoryJavaFileObject extends SimpleJavaFileObject {
        private final String source;

        protected InMemoryJavaFileObject(String className, String source) {
            super(URI.create("string:///" + className.replace('.', '/') + Kind.SOURCE.extension), Kind.SOURCE);
            this.source = source;
        }

        @Override
        public CharSequence getCharContent(boolean ignoreEncodingErrors) {
            return source;
        }
    }
}

