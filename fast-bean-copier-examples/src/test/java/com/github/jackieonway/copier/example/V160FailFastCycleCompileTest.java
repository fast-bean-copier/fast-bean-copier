package com.github.jackieonway.copier.example;

import com.github.jackieonway.copier.processor.BeanCopierProcessor;
import org.junit.Test;

import javax.tools.Diagnostic;
import javax.tools.DiagnosticCollector;
import javax.tools.JavaCompiler;
import javax.tools.JavaFileObject;
import javax.tools.SimpleJavaFileObject;
import javax.tools.StandardJavaFileManager;
import javax.tools.StandardLocation;
import javax.tools.ToolProvider;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * v1.6.0 FAIL_FAST compile-time cycle detection tests.
 */
public class V160FailFastCycleCompileTest {

    @Test
    public void shouldFailFastForSelfReference() throws Exception {
        CompilationResult result = compile("test.SelfNode", Arrays.asList(
                "package test;\n"
                        + "public class SelfNode {\n"
                        + "  private SelfNode self;\n"
                        + "  public SelfNode getSelf() { return self; }\n"
                        + "  public void setSelf(SelfNode self) { this.self = self; }\n"
                        + "}\n",
                "package test;\n"
                        + "import com.github.jackieonway.copier.annotation.CopyTarget;\n"
                        + "@CopyTarget(source = SelfNode.class)\n"
                        + "public class SelfNodeDto {\n"
                        + "  private SelfNodeDto self;\n"
                        + "  public SelfNodeDto getSelf() { return self; }\n"
                        + "  public void setSelf(SelfNodeDto self) { this.self = self; }\n"
                        + "}\n"));

        assertFalse(result.success);
        assertTrue(result.diagnostics, result.diagnostics.contains("Cycle detected under FAIL_FAST"));
    }

    @Test
    public void shouldFailFastForBidirectionalReference() throws Exception {
        CompilationResult result = compile("test.NodeA", Arrays.asList(
                sourcePojo("NodeA", "NodeB b", "NodeB", "B"),
                sourcePojo("NodeB", "NodeA a", "NodeA", "A"),
                dto("NodeADto", "NodeA", "NodeBDto b", "NodeBDto", "B", ""),
                dto("NodeBDto", "NodeB", "NodeADto a", "NodeADto", "A", "")));

        assertFalse(result.success);
        assertTrue(result.diagnostics, result.diagnostics.contains("Cycle detected under FAIL_FAST"));
        assertTrue(result.diagnostics, result.diagnostics.contains("test.NodeADto"));
        assertTrue(result.diagnostics, result.diagnostics.contains("test.NodeBDto"));
    }

    @Test
    public void shouldFailFastForCollectionAndMapReference() throws Exception {
        CompilationResult listResult = compile("test.ListNode", Arrays.asList(
                "package test;\n"
                        + "import java.util.List;\n"
                        + "public class ListNode {\n"
                        + "  private List<ListNode> children;\n"
                        + "  public List<ListNode> getChildren() { return children; }\n"
                        + "  public void setChildren(List<ListNode> children) { this.children = children; }\n"
                        + "}\n",
                "package test;\n"
                        + "import com.github.jackieonway.copier.annotation.CopyTarget;\n"
                        + "import java.util.List;\n"
                        + "@CopyTarget(source = ListNode.class)\n"
                        + "public class ListNodeDto {\n"
                        + "  private List<ListNodeDto> children;\n"
                        + "  public List<ListNodeDto> getChildren() { return children; }\n"
                        + "  public void setChildren(List<ListNodeDto> children) { this.children = children; }\n"
                        + "}\n"));

        assertFalse(listResult.success);
        assertTrue(listResult.diagnostics, listResult.diagnostics.contains("Cycle detected under FAIL_FAST"));

        CompilationResult mapResult = compile("test.MapNode", Arrays.asList(
                "package test;\n"
                        + "import java.util.Map;\n"
                        + "public class MapNode {\n"
                        + "  private Map<String, MapNode> nodes;\n"
                        + "  public Map<String, MapNode> getNodes() { return nodes; }\n"
                        + "  public void setNodes(Map<String, MapNode> nodes) { this.nodes = nodes; }\n"
                        + "}\n",
                "package test;\n"
                        + "import com.github.jackieonway.copier.annotation.CopyTarget;\n"
                        + "import java.util.Map;\n"
                        + "@CopyTarget(source = MapNode.class)\n"
                        + "public class MapNodeDto {\n"
                        + "  private Map<String, MapNodeDto> nodes;\n"
                        + "  public Map<String, MapNodeDto> getNodes() { return nodes; }\n"
                        + "  public void setNodes(Map<String, MapNodeDto> nodes) { this.nodes = nodes; }\n"
                        + "}\n"));

        assertFalse(mapResult.success);
        assertTrue(mapResult.diagnostics, mapResult.diagnostics.contains("Cycle detected under FAIL_FAST"));
    }

    @Test
    public void shouldAllowRuntimeStrategiesAndAcyclicGraph() throws Exception {
        CompilationResult runtimeResult = compile("test.RuntimeNode", Arrays.asList(
                "package test;\n"
                        + "public class RuntimeNode {\n"
                        + "  private RuntimeNode self;\n"
                        + "  public RuntimeNode getSelf() { return self; }\n"
                        + "  public void setSelf(RuntimeNode self) { this.self = self; }\n"
                        + "}\n",
                "package test;\n"
                        + "import com.github.jackieonway.copier.annotation.CopyTarget;\n"
                        + "import com.github.jackieonway.copier.annotation.CycleDetectionStrategy;\n"
                        + "@CopyTarget(source = RuntimeNode.class, cycleDetection = CycleDetectionStrategy.RETURN_NULL)\n"
                        + "public class RuntimeNodeDto {\n"
                        + "  private RuntimeNodeDto self;\n"
                        + "  public RuntimeNodeDto getSelf() { return self; }\n"
                        + "  public void setSelf(RuntimeNodeDto self) { this.self = self; }\n"
                        + "}\n"));

        assertTrue(runtimeResult.diagnostics, runtimeResult.success);

        CompilationResult acyclicResult = compile("test.AcyclicA", Arrays.asList(
                sourcePojo("AcyclicA", "AcyclicB b", "AcyclicB", "B"),
                sourcePojo("AcyclicB", "String name", "String", "Name"),
                dto("AcyclicADto", "AcyclicA", "AcyclicBDto b", "AcyclicBDto", "B", ""),
                dto("AcyclicBDto", "AcyclicB", "String name", "String", "Name", "")));

        assertTrue(acyclicResult.diagnostics, acyclicResult.success);
    }

    @Test
    public void shouldUseGlobalPropertiesCycleDetection() throws Exception {
        CompilationResult result = compile("test.GlobalRuntimeNode", Arrays.asList(
                "package test;\n"
                        + "public class GlobalRuntimeNode {\n"
                        + "  private GlobalRuntimeNode self;\n"
                        + "  public GlobalRuntimeNode getSelf() { return self; }\n"
                        + "  public void setSelf(GlobalRuntimeNode self) { this.self = self; }\n"
                        + "}\n",
                "package test;\n"
                        + "import com.github.jackieonway.copier.annotation.CopyTarget;\n"
                        + "@CopyTarget(source = GlobalRuntimeNode.class)\n"
                        + "public class GlobalRuntimeNodeDto {\n"
                        + "  private GlobalRuntimeNodeDto self;\n"
                        + "  public GlobalRuntimeNodeDto getSelf() { return self; }\n"
                        + "  public void setSelf(GlobalRuntimeNodeDto self) { this.self = self; }\n"
                        + "}\n"),
                "fast.bean.copier.cycleDetection=RETURN_NULL\n");

        assertTrue(result.diagnostics, result.success);
    }

    private static String sourcePojo(String className, String field, String fieldType, String property) {
        String fieldName = Character.toLowerCase(property.charAt(0)) + property.substring(1);
        return "package test;\n"
                + "public class " + className + " {\n"
                + "  private " + field + ";\n"
                + "  public " + fieldType + " get" + property + "() { return " + fieldName + "; }\n"
                + "  public void set" + property + "(" + fieldType + " " + fieldName + ") { this." + fieldName + " = " + fieldName + "; }\n"
                + "}\n";
    }

    private static String dto(String className, String sourceClass, String field,
                              String fieldType, String property, String annotationArgs) {
        String fieldName = Character.toLowerCase(property.charAt(0)) + property.substring(1);
        String args = annotationArgs == null || annotationArgs.isEmpty()
                ? "source = " + sourceClass + ".class"
                : "source = " + sourceClass + ".class, " + annotationArgs;
        return "package test;\n"
                + "import com.github.jackieonway.copier.annotation.CopyTarget;\n"
                + "@CopyTarget(" + args + ")\n"
                + "public class " + className + " {\n"
                + "  private " + field + ";\n"
                + "  public " + fieldType + " get" + property + "() { return " + fieldName + "; }\n"
                + "  public void set" + property + "(" + fieldType + " " + fieldName + ") { this." + fieldName + " = " + fieldName + "; }\n"
                + "}\n";
    }

    private static CompilationResult compile(String firstClassName, List<String> sources) throws IOException {
        return compile(firstClassName, sources, null);
    }

    private static CompilationResult compile(String firstClassName,
                                             List<String> sources,
                                             String propertiesContent) throws IOException {
        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        assertNotNull("JDK compiler is not available", compiler);

        File outputDir = Files.createTempDirectory("fbc-cycle-compile").toFile();
        if (propertiesContent != null) {
            Files.write(new File(outputDir, "fast-bean-copier.properties").toPath(),
                    propertiesContent.getBytes(StandardCharsets.UTF_8));
        }
        DiagnosticCollector<JavaFileObject> diagnostics = new DiagnosticCollector<>();
        try (StandardJavaFileManager fileManager = compiler.getStandardFileManager(diagnostics, null, null)) {
            fileManager.setLocation(StandardLocation.CLASS_OUTPUT, Collections.singleton(outputDir));
            fileManager.setLocation(StandardLocation.SOURCE_OUTPUT, Collections.singleton(outputDir));

            List<JavaFileObject> files = new ArrayList<>();
            for (String source : sources) {
                files.add(new InMemoryJavaFileObject(extractQualifiedClassName(source), source));
            }

            JavaCompiler.CompilationTask task = compiler.getTask(null, fileManager, diagnostics,
                    Arrays.asList("-classpath", System.getProperty("java.class.path")), null, files);
            task.setProcessors(Collections.singletonList(new BeanCopierProcessor()));
            Boolean success = task.call();
            return new CompilationResult(Boolean.TRUE.equals(success), diagnosticsToString(diagnostics));
        }
    }

    private static String diagnosticsToString(DiagnosticCollector<JavaFileObject> diagnostics) {
        StringBuilder builder = new StringBuilder();
        for (Diagnostic<? extends JavaFileObject> diagnostic : diagnostics.getDiagnostics()) {
            builder.append(diagnostic.getMessage(null)).append('\n');
        }
        return builder.toString();
    }

    private static String extractQualifiedClassName(String source) {
        String packageName = "";
        int packageIndex = source.indexOf("package ");
        if (packageIndex >= 0) {
            int packageEnd = source.indexOf(';', packageIndex);
            packageName = source.substring(packageIndex + "package ".length(), packageEnd).trim();
        }
        int classIndex = source.indexOf("public class ");
        int classStart = classIndex + "public class ".length();
        int classEnd = source.indexOf(' ', classStart);
        if (classEnd < 0) {
            classEnd = source.indexOf('{', classStart);
        }
        String className = source.substring(classStart, classEnd).trim();
        return packageName.isEmpty() ? className : packageName + "." + className;
    }

    private static final class CompilationResult {
        private final boolean success;
        private final String diagnostics;

        private CompilationResult(boolean success, String diagnostics) {
            this.success = success;
            this.diagnostics = diagnostics;
        }
    }

    private static class InMemoryJavaFileObject extends SimpleJavaFileObject {
        private final String source;

        private InMemoryJavaFileObject(String className, String source) {
            super(URI.create("string:///" + className.replace('.', '/') + Kind.SOURCE.extension), Kind.SOURCE);
            this.source = source;
        }

        @Override
        public CharSequence getCharContent(boolean ignoreEncodingErrors) {
            return source;
        }
    }
}
