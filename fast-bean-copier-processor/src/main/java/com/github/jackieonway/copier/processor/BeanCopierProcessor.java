package com.github.jackieonway.copier.processor;

import com.google.auto.service.AutoService;
import com.github.jackieonway.copier.annotation.ComponentModel;
import com.github.jackieonway.copier.annotation.CopyTarget;
import com.github.jackieonway.copier.annotation.CycleDetectionStrategy;
import com.github.jackieonway.copier.annotation.NullValueStrategy;
import com.github.jackieonway.copier.processor.analyzer.FieldMappingAnalyzer;
import com.github.jackieonway.copier.processor.context.ProcessorContext;
import com.github.jackieonway.copier.processor.extractor.AnnotationExtractor;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.ArrayType;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Bean 拷贝代码生成的 APT 处理器（协调者）。
 *
 * <p>该处理器会扫描所有被 {@link CopyTarget} 注解标记的类，
 * 并自动生成对应的 Copier 类，包含 toDto、fromDto 等方法。
 *
 * <p>该类作为协调者，将具体的处理逻辑委托给各个专门的组件：
 * <ul>
 *   <li>{@link ProcessorContext} - 管理处理器状态和工具</li>
 *   <li>{@link AnnotationExtractor} - 提取注解配置信息</li>
 *   <li>{@link FieldMappingAnalyzer} - 分析字段映射关系</li>
 *   <li>{@link CodeGenerator} - 生成 Copier 类代码</li>
 * </ul>
 *
 * @author jackieonway
 * @since 1.0.0
 */
@AutoService(Processor.class)
public class BeanCopierProcessor extends AbstractProcessor {

    /** 处理器上下文 */
    private ProcessorContext context;

    /** 注解提取器 */
    private AnnotationExtractor extractor;

    /** 字段映射分析器 */
    private FieldMappingAnalyzer analyzer;

    /**
     * 初始化处理器。
     *
     * @param processingEnv 处理环境
     */
    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);
        
        // 初始化组件
        this.context = new ProcessorContext(processingEnv);
        this.extractor = new AnnotationExtractor(context);
        this.analyzer = new FieldMappingAnalyzer(context, extractor);
    }

    /**
     * 返回该处理器支持的注解类型。
     *
     * @return 支持的注解类型集合
     */
    @Override
    public Set<String> getSupportedAnnotationTypes() {
        return Collections.singleton(CopyTarget.class.getCanonicalName());
    }

    /**
     * 返回该处理器支持的源代码版本。
     *
     * @return 支持的源代码版本
     */
    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.RELEASE_8;
    }

    /**
     * 处理注解。
     *
     * @param annotations 在此轮中要处理的注解类型
     * @param roundEnv    有关当前和上一轮的信息
     * @return 如果此处理器处理了这些注解，则返回 true；否则返回 false
     */
    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        Set<? extends Element> elements = roundEnv.getElementsAnnotatedWith(CopyTarget.class);
        List<TypeElement> targetTypes = new ArrayList<>();
        for (Element element : elements) {
            if (!(element instanceof TypeElement)) {
                continue;
            }
            targetTypes.add((TypeElement) element);
        }

        if (!validateFailFastCycleGraph(targetTypes)) {
            return true;
        }

        for (TypeElement targetType : targetTypes) {
            processTargetType(targetType);
        }
        
        return true;
    }

    private boolean validateFailFastCycleGraph(List<TypeElement> targetTypes) {
        Map<String, CycleNode> nodes = new HashMap<>();
        for (TypeElement targetType : targetTypes) {
            String typeName = targetType.getQualifiedName().toString();
            CopyTarget annotation = targetType.getAnnotation(CopyTarget.class);
            PackageElement packageElement = context.getElementUtils().getPackageOf(targetType);
            AnnotationExtractor.PackageConfig packageConfig = extractor.extractPackageConfig(packageElement);
            nodes.put(typeName, new CycleNode(targetType,
                    extractor.getEffectiveCycleDetection(targetType, annotation, packageConfig)));
        }

        for (TypeElement targetType : targetTypes) {
            CopyTarget annotation = targetType.getAnnotation(CopyTarget.class);
            TypeElement sourceType = extractor.extractSourceType(targetType, annotation);
            if (sourceType == null) {
                continue;
            }
            Set<String> ignoreFields = extractor.extractIgnoreFields(annotation);
            List<FieldMapping> fieldMappings = analyzer.analyze(sourceType, targetType, ignoreFields);
            CycleNode node = nodes.get(targetType.getQualifiedName().toString());
            for (FieldMapping mapping : fieldMappings) {
                collectCycleEdges(mapping.getTargetType(), node.edges);
            }
        }

        for (CycleNode node : nodes.values()) {
            if (node.strategy == CycleDetectionStrategy.FAIL_FAST
                    && hasFailFastCycle(node, nodes, new LinkedHashSet<String>(), new HashSet<String>())) {
                return false;
            }
        }
        return true;
    }

    private boolean hasFailFastCycle(CycleNode node,
                                      Map<String, CycleNode> nodes,
                                      LinkedHashSet<String> path,
                                      Set<String> checked) {
        String nodeName = node.name();
        if (path.contains(nodeName)) {
            reportFailFastCycle(node, path, nodeName);
            return true;
        }
        if (checked.contains(nodeName)) {
            return false;
        }

        path.add(nodeName);
        for (String edge : node.edges) {
            CycleNode next = nodes.get(edge);
            if (next == null || next.strategy != CycleDetectionStrategy.FAIL_FAST) {
                continue;
            }
            if (hasFailFastCycle(next, nodes, path, checked)) {
                return true;
            }
        }
        path.remove(nodeName);
        checked.add(nodeName);
        return false;
    }

    private void reportFailFastCycle(CycleNode node, LinkedHashSet<String> path, String repeatedType) {
        List<String> cycle = new ArrayList<>();
        boolean inCycle = false;
        for (String item : path) {
            if (item.equals(repeatedType)) {
                inCycle = true;
            }
            if (inCycle) {
                cycle.add(item);
            }
        }
        cycle.add(repeatedType);
        context.error("Cycle detected under FAIL_FAST: " + joinCycle(cycle)
                        + ". Set cycleDetection to RETURN_NULL or AUTOMATIC_CACHE, or ignore the cyclic field.",
                node.targetType);
    }

    private String joinCycle(List<String> cycle) {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < cycle.size(); i++) {
            if (i > 0) {
                builder.append(" -> ");
            }
            builder.append(cycle.get(i));
        }
        return builder.toString();
    }

    private void collectCycleEdges(TypeMirror typeMirror, Set<String> edges) {
        if (typeMirror == null) {
            return;
        }
        if (typeMirror.getKind() == TypeKind.ARRAY) {
            collectCycleEdges(((ArrayType) typeMirror).getComponentType(), edges);
            return;
        }
        if (typeMirror.getKind() != TypeKind.DECLARED) {
            return;
        }

        DeclaredType declaredType = (DeclaredType) typeMirror;
        Element element = declaredType.asElement();
        if (element instanceof TypeElement && element.getAnnotation(CopyTarget.class) != null) {
            edges.add(((TypeElement) element).getQualifiedName().toString());
        }
        for (TypeMirror argument : TypeUtils.extractTypeArguments(typeMirror)) {
            collectCycleEdges(argument, edges);
        }
    }

    private static final class CycleNode {
        private final TypeElement targetType;
        private final CycleDetectionStrategy strategy;
        private final Set<String> edges = new LinkedHashSet<>();

        private CycleNode(TypeElement targetType, CycleDetectionStrategy strategy) {
            this.targetType = targetType;
            this.strategy = strategy;
        }

        private String name() {
            return targetType.getQualifiedName().toString();
        }
    }

    /**
     * 处理单个目标类型。
     *
     * @param targetType 目标类型元素
     */
    private void processTargetType(TypeElement targetType) {
        // 获取注解
        CopyTarget annotation = targetType.getAnnotation(CopyTarget.class);

        // 提取源类型
        TypeElement sourceType = extractor.extractSourceType(targetType, annotation);
        if (sourceType == null) {
            context.error("无法获取源类型，请检查 @CopyTarget 注解的 source 属性", targetType);
            return;
        }

        // 提取配置信息
        Set<String> ignoreFields = extractor.extractIgnoreFields(annotation);
        List<TypeMirror> usesClasses = extractor.extractUsesClasses(annotation);
        ComponentModel componentModel = extractor.extractComponentModel(annotation);
        // 提取包级别配置（v1.3 新增）
        PackageElement packageElement = context.getElementUtils().getPackageOf(targetType);
        AnnotationExtractor.PackageConfig packageConfig = extractor.extractPackageConfig(packageElement);

        // 合并配置（类级别 > 包级别 > 默认值）
        ComponentModel effectiveComponentModel = extractor.mergeComponentModel(componentModel, packageConfig);
        NullValueStrategy effectiveNullValueStrategy = extractor.getEffectiveNullValueStrategy(packageConfig);

        // 提取循环检测策略（v1.6 新增）
        CycleDetectionStrategy cycleStrategy = extractor.getEffectiveCycleDetection(targetType, annotation, packageConfig);
        context.setCycleDetectionStrategy(cycleStrategy);

        // 进入类型处理链
        String targetTypeQualifiedName = targetType.getQualifiedName().toString();
        context.enterType(targetTypeQualifiedName);

        try {
            // 分析字段映射
            List<FieldMapping> fieldMappings = analyzer.analyze(sourceType, targetType, ignoreFields);

            if (fieldMappings.isEmpty()) {
                context.warning("未找到任何可映射的字段", targetType);
            }

            // 编译期循环检测：检查嵌套字段的目标类型是否在处理链中
            checkNestedTypeCycles(fieldMappings, targetTypeQualifiedName, cycleStrategy);

            // 生成 Copier 类
            generateCopierClass(sourceType, targetType, fieldMappings, usesClasses,
                    effectiveComponentModel, effectiveNullValueStrategy);
        } finally {
            // 离开类型处理链
            context.exitType(targetTypeQualifiedName);
        }
    }

    /**
     * 检查嵌套字段是否存在循环引用。
     *
     * <p>遍历字段映射，对目标类型为复杂类型（带 @CopyTarget 注解）的字段，
     * 检查其目标类型是否已在当前处理链中，若在且策略为 FAIL_FAST 则编译报错。</p>
     *
     * @param fieldMappings          字段映射列表
     * @param currentTargetTypeName  当前目标类型全限定名
     * @param strategy               循环检测策略
     */
    private void checkNestedTypeCycles(List<FieldMapping> fieldMappings,
                                        String currentTargetTypeName,
                                        CycleDetectionStrategy strategy) {
        if (strategy == CycleDetectionStrategy.RETURN_NULL || strategy == CycleDetectionStrategy.AUTOMATIC_CACHE) {
            // 运行期策略不在编译期报错，由 FieldCopyGenerator 处理
            return;
        }

        for (FieldMapping mapping : fieldMappings) {
            TypeMirror targetTypeMirror = mapping.getTargetType();

            // 只检查声明类型（类/接口），跳过基本类型、集合泛型参数等
            if (targetTypeMirror.getKind() != TypeKind.DECLARED) {
                continue;
            }

            DeclaredType declaredType = (DeclaredType) targetTypeMirror;
            Element element = declaredType.asElement();

            if (!(element instanceof TypeElement)) {
                continue;
            }

            TypeElement nestedTargetType = (TypeElement) element;

            // 检查嵌套目标类型是否有 @CopyTarget 注解（只有带注解的类型才会被处理为 copier）
            CopyTarget nestedAnnotation = nestedTargetType.getAnnotation(CopyTarget.class);
            if (nestedAnnotation == null) {
                continue;
            }

            // 检查该嵌套类型是否已在处理链中（即构成循环）
            String nestedTypeName = nestedTargetType.getQualifiedName().toString();
            if (context.isInCycle(nestedTypeName)) {
                context.error(
                        "检测到循环引用: " + currentTargetTypeName + " → " + nestedTypeName
                                + "，且 " + nestedTypeName + " 正在处理中。"
                                + " 请在 @CopyTarget 中设置 cycleDetection = CycleDetectionStrategy.RETURN_NULL"
                                + " 或 CycleDetectionStrategy.AUTOMATIC_CACHE 以打破循环，"
                                + " 或使用 ignore 属性排除循环字段。",
                        mapping.getTargetField());
                return;
            }
        }
    }

    /**
     * 生成 Copier 类。
     *
     * @param sourceType           源类型
     * @param targetType           目标类型
     * @param fieldMappings        字段映射列表
     * @param usesClasses          uses 类列表
     * @param componentModel       组件模型
     * @param nullValueStrategy    null 值处理策略
     */
    private void generateCopierClass(TypeElement sourceType, TypeElement targetType,
                                      List<FieldMapping> fieldMappings,
                                      List<TypeMirror> usesClasses,
                                      ComponentModel componentModel,
                                      NullValueStrategy nullValueStrategy) {
        CodeGenerator codeGenerator = new CodeGenerator(
                context.getProcessingEnv(), sourceType, targetType);
        codeGenerator.setFieldMappings(fieldMappings);
        codeGenerator.setUsesClasses(usesClasses);
        codeGenerator.setComponentModel(componentModel);
        codeGenerator.setNullValueStrategy(nullValueStrategy);
        codeGenerator.setCycleDetectionStrategy(context.getCycleDetectionStrategy());
        codeGenerator.generateCopierClass();
    }
}
