package com.github.jackieonway.copier.processor;

import com.google.auto.service.AutoService;
import com.github.jackieonway.copier.annotation.ComponentModel;
import com.github.jackieonway.copier.annotation.CopyTarget;
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
import javax.lang.model.type.TypeMirror;
import java.util.Collections;
import java.util.List;
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
        
        for (Element element : elements) {
            if (!(element instanceof TypeElement)) {
                continue;
            }
            
            processTargetType((TypeElement) element);
        }
        
        return true;
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
        
        // 分析字段映射
        List<FieldMapping> fieldMappings = analyzer.analyze(sourceType, targetType, ignoreFields);
        
        if (fieldMappings.isEmpty()) {
            context.warning("未找到任何可映射的字段", targetType);
        }
        
        // 生成 Copier 类
        generateCopierClass(sourceType, targetType, fieldMappings, usesClasses, 
                effectiveComponentModel, effectiveNullValueStrategy);
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
        codeGenerator.generateCopierClass();
    }
}
