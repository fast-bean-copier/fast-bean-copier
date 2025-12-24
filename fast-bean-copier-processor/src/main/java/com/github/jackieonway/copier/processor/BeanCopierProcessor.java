package com.github.jackieonway.copier.processor;

import com.google.auto.service.AutoService;
import com.github.jackieonway.copier.annotation.CopyTarget;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.MirroredTypeException;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import javax.tools.Diagnostic;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Bean 拷贝代码生成的 APT 处理器。
 *
 * 该处理器会扫描所有被 {@link CopyTarget} 注解标记的类，
 * 并自动生成对应的 Copier 类，包含 toDto、fromDto 等方法。
 *
 * @author jackieonway
 * @since 1.0.0
 */
@AutoService(Processor.class)
public class BeanCopierProcessor extends AbstractProcessor {

    /**
     * 处理环境，用于访问编译时的各种信息。
     */
    private ProcessingEnvironment processingEnv;

    /**
     * 元素工具。
     */
    private Elements elementUtils;

    /**
     * 类型工具。
     */
    private Types typeUtils;

    /**
     * 消息工具，用于输出编译期错误和警告。
     */
    private Messager messager;

    /**
     * 初始化处理器。
     *
     * @param processingEnv 处理环境
     */
    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);
        this.processingEnv = processingEnv;
        this.elementUtils = processingEnv.getElementUtils();
        this.typeUtils = processingEnv.getTypeUtils();
        this.messager = processingEnv.getMessager();
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
        // 获取所有被 @CopyTarget 注解标记的元素
        Set<? extends Element> elements = roundEnv.getElementsAnnotatedWith(CopyTarget.class);
        
        for (Element element : elements) {
            // 确保是类元素
            if (!(element instanceof TypeElement)) {
                continue;
            }
            
            TypeElement targetType = (TypeElement) element;
            
            // 获取注解信息
            CopyTarget annotation = targetType.getAnnotation(CopyTarget.class);
            
            // 获取源类型
            TypeElement sourceType = getSourceType(targetType, annotation);
            if (sourceType == null) {
                messager.printMessage(Diagnostic.Kind.ERROR, 
                    "无法获取源类型，请检查 @CopyTarget 注解的 source 属性", targetType);
                continue;
            }
            
            // 获取忽略的字段列表
            Set<String> ignoreFields = getIgnoreFields(annotation);
            
            // 进行字段映射分析
            List<FieldMapping> fieldMappings = analyzeFieldMappings(sourceType, targetType, ignoreFields);
            
            if (fieldMappings.isEmpty()) {
                messager.printMessage(Diagnostic.Kind.WARNING, 
                    "未找到任何可映射的字段", targetType);
            }
            
            // 生成 Copier 类
            CodeGenerator codeGenerator = new CodeGenerator(processingEnv, sourceType, targetType);
            codeGenerator.setFieldMappings(fieldMappings);
            codeGenerator.generateCopierClass();
        }
        
        return true;
    }

    /**
     * 获取源类型。
     *
     * @param targetType 目标类型
     * @param annotation 注解
     * @return 源类型，如果无法获取则返回 null
     */
    private TypeElement getSourceType(TypeElement targetType, CopyTarget annotation) {
        try {
            // 通过反射获取 source 属性的值
            annotation.source();
        } catch (MirroredTypeException e) {
            // 获取镜像类型
            TypeMirror sourceTypeMirror = e.getTypeMirror();
            if (sourceTypeMirror != null) {
                return (TypeElement) typeUtils.asElement(sourceTypeMirror);
            }
        }
        return null;
    }

    /**
     * 获取忽略的字段列表。
     *
     * @param annotation 注解
     * @return 忽略的字段名集合
     */
    private Set<String> getIgnoreFields(CopyTarget annotation) {
        Set<String> ignoreFields = new HashSet<>();
        String[] ignoreArray = annotation.ignore();
        if (ignoreArray != null) {
            for (String fieldName : ignoreArray) {
                ignoreFields.add(fieldName);
            }
        }
        return ignoreFields;
    }

    /**
     * 分析字段映射关系。
     *
     * @param sourceType   源类型
     * @param targetType   目标类型
     * @param ignoreFields 忽略的字段名集合
     * @return 字段映射列表
     */
    private List<FieldMapping> analyzeFieldMappings(TypeElement sourceType, TypeElement targetType, 
                                                     Set<String> ignoreFields) {
        List<FieldMapping> mappings = new ArrayList<>();
        
        // 获取源类型的所有字段
        List<VariableElement> sourceFields = TypeUtils.getAllFields(sourceType);
        
        // 获取目标类型的所有字段
        List<VariableElement> targetFields = TypeUtils.getAllFields(targetType);
        
        // 创建目标字段的映射表，便于快速查找
        Map<String, VariableElement> targetFieldMap = new HashMap<>();
        for (VariableElement field : targetFields) {
            String fieldName = field.getSimpleName().toString();
            targetFieldMap.put(fieldName, field);
        }
        
        // 进行字段映射
        for (VariableElement sourceField : sourceFields) {
            String fieldName = sourceField.getSimpleName().toString();
            
            // 跳过忽略的字段
            if (ignoreFields.contains(fieldName)) {
                continue;
            }
            
            // 查找同名的目标字段
            VariableElement targetField = targetFieldMap.get(fieldName);
            if (targetField == null) {
                continue;
            }
            
            // 检查字段类型兼容性
            TypeMirror sourceFieldType = TypeUtils.getFieldType(sourceField);
            TypeMirror targetFieldType = TypeUtils.getFieldType(targetField);
            
            if (TypeUtils.isTypeCompatible(sourceFieldType, targetFieldType)) {
                mappings.add(new FieldMapping(sourceField, targetField, sourceFieldType, targetFieldType));
            } else {
                messager.printMessage(Diagnostic.Kind.WARNING, 
                    "字段 '" + fieldName + "' 的类型不兼容：" + sourceFieldType + " -> " + targetFieldType, 
                    targetType);
            }
        }
        
        return mappings;
    }
}
