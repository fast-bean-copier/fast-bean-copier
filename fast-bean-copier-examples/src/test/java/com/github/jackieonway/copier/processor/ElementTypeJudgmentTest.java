package com.github.jackieonway.copier.processor;

import com.github.jackieonway.copier.annotation.CopyTarget;
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
import java.lang.reflect.Method;
import java.net.URI;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.*;

/**
 * 元素类型深拷贝判断相关测试。
 */
public class ElementTypeJudgmentTest {

    private static Map<String, TypeMirror> fieldTypes;

    @BeforeClass
    public static void setUp() throws Exception {
        String source = "package test; "
                + "import com.github.jackieonway.copier.annotation.CopyTarget; "
                + "public class Types { "
                + "  int primitiveField; "
                + "  Integer wrapperField; "
                + "  String stringField; "
                + "  Nested nested; "
                + "  Annotated annotated; "
                + "  Nested[] nestedArray; "
                + "  @CopyTarget(source = Nested.class) public static class Annotated { } "
                + "  public static class Nested { } "
                + "}";
        fieldTypes = compileAndGetFieldTypes(source, "test.Types");
    }

    @Test
    public void shouldDetectBasicAndStringType() {
        assertTrue(TypeUtils.isBasicType(fieldTypes.get("primitiveField")));
        assertTrue(TypeUtils.isBasicType(fieldTypes.get("wrapperField")));
        assertTrue(TypeUtils.isStringType(fieldTypes.get("stringField")));
    }

    @Test
    public void shouldDecideNeedsDeepCopy() {
        assertFalse(TypeUtils.needsDeepCopy(fieldTypes.get("primitiveField")));
        assertFalse(TypeUtils.needsDeepCopy(fieldTypes.get("wrapperField")));
        assertFalse(TypeUtils.needsDeepCopy(fieldTypes.get("stringField")));

        assertTrue(TypeUtils.needsDeepCopy(fieldTypes.get("nested")));
        assertTrue(TypeUtils.needsDeepCopy(fieldTypes.get("annotated")));
        assertTrue(TypeUtils.needsDeepCopy(fieldTypes.get("nestedArray")));
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

