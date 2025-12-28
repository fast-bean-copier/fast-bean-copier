package com.github.jackieonway.copier.processor;

import com.google.auto.service.AutoService;
import com.github.jackieonway.copier.annotation.ComponentModel;
import com.github.jackieonway.copier.annotation.CopyField;
import com.github.jackieonway.copier.annotation.CopyTarget;
import com.github.jackieonway.copier.converter.TypeConverter;

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
import javax.lang.model.type.MirroredTypesException;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import javax.tools.Diagnostic;
import java.util.ArrayList;
import java.util.Arrays;
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

            // v1.2: 获取 uses 和 componentModel
            List<TypeMirror> usesClasses = getUsesClasses(targetType, annotation);
            ComponentModel componentModel = annotation.componentModel();
            
            // 进行字段映射分析
            List<FieldMapping> fieldMappings = analyzeFieldMappings(sourceType, targetType, ignoreFields);
            
            if (fieldMappings.isEmpty()) {
                messager.printMessage(Diagnostic.Kind.WARNING, 
                    "未找到任何可映射的字段", targetType);
            }
            
            // 生成 Copier 类
            CodeGenerator codeGenerator = new CodeGenerator(processingEnv, sourceType, targetType);
            codeGenerator.setFieldMappings(fieldMappings);
            codeGenerator.setUsesClasses(usesClasses);
            codeGenerator.setComponentModel(componentModel);
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
     * 获取 uses 属性中的类列表。
     *
     * @param targetType 目标类型
     * @param annotation 注解
     * @return uses 类的 TypeMirror 列表
     * @since 1.2.0
     */
    private List<TypeMirror> getUsesClasses(TypeElement targetType, CopyTarget annotation) {
        List<TypeMirror> usesClasses = new ArrayList<>();
        try {
            Class<?>[] uses = annotation.uses();
            // 如果能直接获取，说明类在编译路径中
            for (Class<?> clazz : uses) {
                TypeElement element = elementUtils.getTypeElement(clazz.getCanonicalName());
                if (element != null) {
                    usesClasses.add(element.asType());
                }
            }
        } catch (MirroredTypesException e) {
            // 获取镜像类型列表
            usesClasses.addAll(e.getTypeMirrors());
        }
        return usesClasses;
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
        
        // 创建源字段的映射表，便于快速查找
        Map<String, VariableElement> sourceFieldMap = new HashMap<>();
        for (VariableElement field : sourceFields) {
            String fieldName = field.getSimpleName().toString();
            sourceFieldMap.put(fieldName, field);
        }
        
        // 创建目标字段的映射表，便于快速查找
        Map<String, VariableElement> targetFieldMap = new HashMap<>();
        for (VariableElement field : targetFields) {
            String fieldName = field.getSimpleName().toString();
            targetFieldMap.put(fieldName, field);
        }
        
        // 处理目标类型的字段（包括 @CopyField 注解）
        for (VariableElement targetField : targetFields) {
            String targetFieldName = targetField.getSimpleName().toString();
            
            // 跳过忽略的字段
            if (ignoreFields.contains(targetFieldName)) {
                continue;
            }
            
            // 检查是否有 @CopyField 注解
            CopyField copyFieldAnnotation = targetField.getAnnotation(CopyField.class);
            
            if (copyFieldAnnotation != null) {
                // 处理 @CopyField 注解的字段
                FieldMapping mapping = processCopyFieldAnnotation(
                        copyFieldAnnotation, targetField, sourceFieldMap, sourceType);
                if (mapping != null) {
                    mappings.add(mapping);
                }
            } else {
                // 查找同名的源字段（简单映射）
                VariableElement sourceField = sourceFieldMap.get(targetFieldName);
                if (sourceField == null) {
                    continue;
                }
                
                // 检查字段类型兼容性
                TypeMirror sourceFieldType = TypeUtils.getFieldType(sourceField);
                TypeMirror targetFieldType = TypeUtils.getFieldType(targetField);
                
                if (TypeUtils.isTypeCompatible(sourceFieldType, targetFieldType)) {
                    mappings.add(new FieldMapping(sourceField, targetField, sourceFieldType, targetFieldType));
                } else {
                    messager.printMessage(Diagnostic.Kind.WARNING, 
                        "字段 '" + targetFieldName + "' 的类型不兼容：" + sourceFieldType + " -> " + targetFieldType, 
                        targetType);
                }
            }
        }
        
        return mappings;
    }

    /**
     * 处理 @CopyField 注解的字段。
     *
     * @param annotation     CopyField 注解
     * @param targetField    目标字段
     * @param sourceFieldMap 源字段映射表
     * @param sourceType     源类型
     * @return 字段映射，如果无法创建则返回 null
     * @since 1.2.0
     */
    private FieldMapping processCopyFieldAnnotation(CopyField annotation, 
                                                     VariableElement targetField,
                                                     Map<String, VariableElement> sourceFieldMap,
                                                     TypeElement sourceType) {
        String targetFieldName = targetField.getSimpleName().toString();
        TypeMirror targetFieldType = TypeUtils.getFieldType(targetField);
        
        // 获取源字段名数组
        String[] sourceNames = annotation.source();
        String expression = annotation.expression();
        String qualifiedByName = annotation.qualifiedByName();
        String format = annotation.format();
        
        // 获取转换器类
        String converterClassName = getConverterClassName(annotation);
        
        // 确定映射类型和源字段
        FieldMapping mapping;
        
        if (expression != null && !expression.trim().isEmpty()) {
            // 表达式映射
            mapping = createExpressionMapping(targetField, targetFieldType, sourceNames, 
                    expression, sourceFieldMap, sourceType);
        } else if (converterClassName != null && !converterClassName.endsWith("TypeConverter$None") 
                && !converterClassName.endsWith("TypeConverter.None")) {
            // 类型转换器映射
            mapping = createConverterMapping(targetField, targetFieldType, sourceNames, 
                    converterClassName, format, sourceFieldMap);
        } else if (qualifiedByName != null && !qualifiedByName.trim().isEmpty()) {
            // 具名转换方法映射
            mapping = createQualifiedByNameMapping(targetField, targetFieldType, sourceNames, 
                    qualifiedByName, sourceFieldMap);
        } else if (sourceNames != null && sourceNames.length > 0) {
            // 简单的字段名映射（可能是多对一）
            mapping = createSimpleMapping(targetField, targetFieldType, sourceNames, sourceFieldMap);
        } else {
            // 使用目标字段名作为源字段名
            VariableElement sourceField = sourceFieldMap.get(targetFieldName);
            if (sourceField == null) {
                messager.printMessage(Diagnostic.Kind.WARNING,
                        "找不到源字段 '" + targetFieldName + "'", targetField);
                return null;
            }
            TypeMirror sourceFieldType = TypeUtils.getFieldType(sourceField);
            mapping = new FieldMapping(sourceField, targetField, sourceFieldType, targetFieldType);
        }
        
        return mapping;
    }

    /**
     * 获取转换器类名。
     */
    private String getConverterClassName(CopyField annotation) {
        try {
            Class<? extends TypeConverter<?, ?>> converterClass = annotation.converter();
            return converterClass.getCanonicalName();
        } catch (MirroredTypeException e) {
            return e.getTypeMirror().toString();
        }
    }

    /**
     * 创建表达式映射。
     */
    private FieldMapping createExpressionMapping(VariableElement targetField, 
                                                  TypeMirror targetFieldType,
                                                  String[] sourceNames,
                                                  String expression,
                                                  Map<String, VariableElement> sourceFieldMap,
                                                  TypeElement sourceType) {
        // 验证表达式语法
        String syntaxError = ExpressionUtils.validateSyntax(expression);
        if (syntaxError != null) {
            messager.printMessage(Diagnostic.Kind.ERROR,
                    "表达式语法错误: " + syntaxError, targetField);
            return null;
        }
        
        // 创建映射
        FieldMapping mapping = new FieldMapping(null, targetField, sourceType.asType(), targetFieldType);
        mapping.setMappingType(FieldMapping.MappingType.EXPRESSION);
        mapping.setExpression(expression);
        
        if (sourceNames != null && sourceNames.length > 0) {
            mapping.setSourceFieldNames(Arrays.asList(sourceNames));
            if (sourceNames.length > 1) {
                mapping.setMappingType(FieldMapping.MappingType.MANY_TO_ONE);
            }
        }
        
        return mapping;
    }

    /**
     * 创建类型转换器映射。
     */
    private FieldMapping createConverterMapping(VariableElement targetField,
                                                 TypeMirror targetFieldType,
                                                 String[] sourceNames,
                                                 String converterClassName,
                                                 String format,
                                                 Map<String, VariableElement> sourceFieldMap) {
        String targetFieldName = targetField.getSimpleName().toString();
        
        // 确定源字段
        String sourceFieldName = (sourceNames != null && sourceNames.length > 0) 
                ? sourceNames[0] : targetFieldName;
        VariableElement sourceField = sourceFieldMap.get(sourceFieldName);
        
        if (sourceField == null) {
            messager.printMessage(Diagnostic.Kind.WARNING,
                    "找不到源字段 '" + sourceFieldName + "'", targetField);
            return null;
        }
        
        TypeMirror sourceFieldType = TypeUtils.getFieldType(sourceField);
        FieldMapping mapping = new FieldMapping(sourceField, targetField, sourceFieldType, targetFieldType);
        mapping.setMappingType(FieldMapping.MappingType.CONVERTER);
        mapping.setConverterClassName(converterClassName);
        mapping.setFormat(format);
        
        return mapping;
    }

    /**
     * 创建具名转换方法映射。
     */
    private FieldMapping createQualifiedByNameMapping(VariableElement targetField,
                                                       TypeMirror targetFieldType,
                                                       String[] sourceNames,
                                                       String qualifiedByName,
                                                       Map<String, VariableElement> sourceFieldMap) {
        String targetFieldName = targetField.getSimpleName().toString();
        
        // 确定源字段
        String sourceFieldName = (sourceNames != null && sourceNames.length > 0) 
                ? sourceNames[0] : targetFieldName;
        VariableElement sourceField = sourceFieldMap.get(sourceFieldName);
        
        if (sourceField == null) {
            messager.printMessage(Diagnostic.Kind.WARNING,
                    "找不到源字段 '" + sourceFieldName + "'", targetField);
            return null;
        }
        
        TypeMirror sourceFieldType = TypeUtils.getFieldType(sourceField);
        FieldMapping mapping = new FieldMapping(sourceField, targetField, sourceFieldType, targetFieldType);
        mapping.setMappingType(FieldMapping.MappingType.QUALIFIED_BY_NAME);
        mapping.setQualifiedByName(qualifiedByName);
        
        return mapping;
    }

    /**
     * 创建简单映射（可能是多对一）。
     */
    private FieldMapping createSimpleMapping(VariableElement targetField,
                                              TypeMirror targetFieldType,
                                              String[] sourceNames,
                                              Map<String, VariableElement> sourceFieldMap) {
        if (sourceNames.length == 1) {
            // 单一源字段
            VariableElement sourceField = sourceFieldMap.get(sourceNames[0]);
            if (sourceField == null) {
                messager.printMessage(Diagnostic.Kind.WARNING,
                        "找不到源字段 '" + sourceNames[0] + "'", targetField);
                return null;
            }
            TypeMirror sourceFieldType = TypeUtils.getFieldType(sourceField);
            return new FieldMapping(sourceField, targetField, sourceFieldType, targetFieldType);
        } else {
            // 多对一映射（需要表达式）
            messager.printMessage(Diagnostic.Kind.ERROR,
                    "多对一映射需要指定 expression 属性", targetField);
            return null;
        }
    }
}
