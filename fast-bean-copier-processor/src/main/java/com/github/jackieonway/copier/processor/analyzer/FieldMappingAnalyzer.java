package com.github.jackieonway.copier.processor.analyzer;

import com.github.jackieonway.copier.annotation.CopyField;
import com.github.jackieonway.copier.processor.ExpressionUtils;
import com.github.jackieonway.copier.processor.FieldMapping;
import com.github.jackieonway.copier.processor.TypeUtils;
import com.github.jackieonway.copier.processor.context.ProcessorContext;
import com.github.jackieonway.copier.processor.extractor.AnnotationExtractor;
import com.github.jackieonway.copier.processor.model.CopyFieldConfig;

import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeMirror;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 字段映射分析器，负责分析源类和目标类之间的字段映射关系。
 *
 * <p>该类封装了字段映射分析的核心逻辑，包括：
 * <ul>
 *   <li>同名字段的自动映射</li>
 *   <li>@CopyField 注解定义的自定义映射</li>
 *   <li>表达式映射（EXPRESSION 类型）</li>
 *   <li>转换器映射（CONVERTER 类型）</li>
 *   <li>具名方法映射（QUALIFIED_BY_NAME 类型）</li>
 *   <li>多对一映射（MANY_TO_ONE 类型）</li>
 *   <li>字段类型兼容性验证</li>
 * </ul>
 *
 * @author jackieonway
 * @since 1.2.1
 */
public class FieldMappingAnalyzer {

    /**
     * 处理器上下文。
     */
    private final ProcessorContext context;

    /**
     * 注解提取器。
     */
    private final AnnotationExtractor extractor;

    /**
     * 构造方法。
     *
     * @param context 处理器上下文
     */
    public FieldMappingAnalyzer(ProcessorContext context) {
        this.context = context;
        this.extractor = new AnnotationExtractor(context);
    }

    /**
     * 构造方法（允许注入 AnnotationExtractor）。
     *
     * @param context   处理器上下文
     * @param extractor 注解提取器
     */
    public FieldMappingAnalyzer(ProcessorContext context, AnnotationExtractor extractor) {
        this.context = context;
        this.extractor = extractor;
    }

    /**
     * 分析源类和目标类之间的字段映射关系。
     *
     * @param sourceType   源类型
     * @param targetType   目标类型
     * @param ignoreFields 忽略的字段名集合
     * @return 字段映射列表
     */
    public List<FieldMapping> analyze(TypeElement sourceType, TypeElement targetType,
                                       Set<String> ignoreFields) {
        List<FieldMapping> mappings = new ArrayList<>();

        // 获取源类型的所有字段
        List<VariableElement> sourceFields = TypeUtils.getAllFields(sourceType);

        // 获取目标类型的所有字段
        List<VariableElement> targetFields = TypeUtils.getAllFields(targetType);

        // 创建源字段的映射表，便于快速查找
        Map<String, VariableElement> sourceFieldMap = buildFieldMap(sourceFields);

        // 处理目标类型的每个字段
        for (VariableElement targetField : targetFields) {
            String targetFieldName = targetField.getSimpleName().toString();

            // 跳过忽略的字段
            if (ignoreFields != null && ignoreFields.contains(targetFieldName)) {
                continue;
            }

            // 分析单个字段的映射
            FieldMapping mapping = analyzeField(targetField, sourceFieldMap, sourceType);
            if (mapping != null) {
                mappings.add(mapping);
            }
        }

        return mappings;
    }


    /**
     * 分析单个目标字段的映射关系。
     *
     * @param targetField    目标字段
     * @param sourceFieldMap 源字段映射表
     * @param sourceType     源类型
     * @return 字段映射，如果无法创建则返回 null
     */
    private FieldMapping analyzeField(VariableElement targetField,
                                       Map<String, VariableElement> sourceFieldMap,
                                       TypeElement sourceType) {
        String targetFieldName = targetField.getSimpleName().toString();
        TypeMirror targetFieldType = TypeUtils.getFieldType(targetField);

        // 检查是否有 @CopyField 注解
        CopyField copyFieldAnnotation = targetField.getAnnotation(CopyField.class);

        if (copyFieldAnnotation != null) {
            // 处理 @CopyField 注解的字段
            return processCopyFieldAnnotation(copyFieldAnnotation, targetField,
                    targetFieldType, sourceFieldMap, sourceType);
        } else {
            // 查找同名的源字段（简单映射）
            return createSimpleMappingByName(targetFieldName, targetField,
                    targetFieldType, sourceFieldMap);
        }
    }

    /**
     * 处理带有 @CopyField 注解的字段。
     *
     * @param annotation     CopyField 注解
     * @param targetField    目标字段
     * @param targetFieldType 目标字段类型
     * @param sourceFieldMap 源字段映射表
     * @param sourceType     源类型
     * @return 字段映射，如果无法创建则返回 null
     */
    private FieldMapping processCopyFieldAnnotation(CopyField annotation,
                                                     VariableElement targetField,
                                                     TypeMirror targetFieldType,
                                                     Map<String, VariableElement> sourceFieldMap,
                                                     TypeElement sourceType) {
        // 提取注解配置
        CopyFieldConfig config = extractor.extractCopyFieldConfig(annotation);
        if (config == null) {
            return null;
        }

        String targetFieldName = targetField.getSimpleName().toString();
        String[] sourceNames = config.getSourceNames();

        // 提取 v1.3 新增属性
        String condition = extractor.extractCondition(annotation);
        String defaultValue = extractor.extractDefaultValue(annotation);
        String constant = extractor.extractConstant(annotation);

        // 验证互斥关系
        if (!validateMutualExclusion(defaultValue, constant, targetField)) {
            return null;
        }

        FieldMapping mapping = null;

        // 根据配置确定映射类型
        if (constant != null && !constant.isEmpty()) {
            // 常量映射（v1.3 新增）
            mapping = createConstantMapping(targetField, targetFieldType, constant);
        } else if (config.hasExpression()) {
            // 表达式映射
            mapping = createExpressionMapping(targetField, targetFieldType, sourceNames,
                    config.getExpression(), sourceFieldMap, sourceType);
        } else if (config.hasConverter()) {
            // 类型转换器映射
            mapping = createConverterMapping(targetField, targetFieldType, sourceNames,
                    config.getConverterClassName(), config.getFormat(), sourceFieldMap);
        } else if (config.hasQualifiedByName()) {
            // 具名转换方法映射
            mapping = createQualifiedByNameMapping(targetField, targetFieldType, sourceNames,
                    config.getQualifiedByName(), sourceFieldMap);
        } else if (config.hasSourceNames()) {
            // 简单的字段名映射（可能是多对一）
            mapping = createSimpleMapping(targetField, targetFieldType, sourceNames, sourceFieldMap);
        } else {
            // 使用目标字段名作为源字段名
            mapping = createSimpleMappingByName(targetFieldName, targetField,
                    targetFieldType, sourceFieldMap);
        }

        // 设置 v1.3 新增属性
        if (mapping != null) {
            if (condition != null && !condition.isEmpty()) {
                mapping.setCondition(condition);
            }
            if (defaultValue != null && !defaultValue.isEmpty()) {
                mapping.setDefaultValue(defaultValue);
            }
        }

        return mapping;
    }

    /**
     * 验证 defaultValue 和 constant 的互斥关系。
     *
     * @param defaultValue 默认值
     * @param constant     常量值
     * @param targetField  目标字段
     * @return 如果验证通过返回 true
     * @since 1.3.0
     */
    private boolean validateMutualExclusion(String defaultValue, String constant,
                                            VariableElement targetField) {
        boolean hasDefaultValue = defaultValue != null && !defaultValue.isEmpty();
        boolean hasConstant = constant != null && !constant.isEmpty();

        if (hasDefaultValue && hasConstant) {
            context.error("@CopyField 的 defaultValue 和 constant 属性不能同时使用", targetField);
            return false;
        }

        return true;
    }

    /**
     * 创建常量映射。
     *
     * @param targetField     目标字段
     * @param targetFieldType 目标字段类型
     * @param constant        常量值
     * @return 字段映射
     * @since 1.3.0
     */
    FieldMapping createConstantMapping(VariableElement targetField,
                                        TypeMirror targetFieldType,
                                        String constant) {
        FieldMapping mapping = new FieldMapping(null, targetField, null, targetFieldType);
        mapping.setConstant(constant);
        mapping.setMappingType(FieldMapping.MappingType.SIMPLE);
        // v1.3.1: 常量映射在逆向转换中跳过
        mapping.setSkipInReverseMapping(true);
        mapping.setReverseSkipReason("常量映射");
        return mapping;
    }

    /**
     * 根据目标字段名创建简单映射。
     *
     * @param targetFieldName 目标字段名
     * @param targetField     目标字段
     * @param targetFieldType 目标字段类型
     * @param sourceFieldMap  源字段映射表
     * @return 字段映射，如果无法创建则返回 null
     */
    private FieldMapping createSimpleMappingByName(String targetFieldName,
                                                    VariableElement targetField,
                                                    TypeMirror targetFieldType,
                                                    Map<String, VariableElement> sourceFieldMap) {
        VariableElement sourceField = sourceFieldMap.get(targetFieldName);
        if (sourceField == null) {
            // 找不到同名源字段，跳过
            return null;
        }

        TypeMirror sourceFieldType = TypeUtils.getFieldType(sourceField);

        // 检查字段类型兼容性
        if (!TypeUtils.isTypeCompatible(sourceFieldType, targetFieldType)) {
            context.warning("字段 '" + targetFieldName + "' 的类型不兼容：" +
                    sourceFieldType + " -> " + targetFieldType, targetField);
            return null;
        }

        return new FieldMapping(sourceField, targetField, sourceFieldType, targetFieldType);
    }

    /**
     * 创建简单映射（可能是多对一）。
     *
     * @param targetField     目标字段
     * @param targetFieldType 目标字段类型
     * @param sourceNames     源字段名数组
     * @param sourceFieldMap  源字段映射表
     * @return 字段映射，如果无法创建则返回 null
     */
    FieldMapping createSimpleMapping(VariableElement targetField,
                                      TypeMirror targetFieldType,
                                      String[] sourceNames,
                                      Map<String, VariableElement> sourceFieldMap) {
        if (sourceNames == null || sourceNames.length == 0) {
            return null;
        }

        if (sourceNames.length == 1) {
            // 单一源字段
            VariableElement sourceField = sourceFieldMap.get(sourceNames[0]);
            if (sourceField == null) {
                context.warning("找不到源字段 '" + sourceNames[0] + "'", targetField);
                return null;
            }
            TypeMirror sourceFieldType = TypeUtils.getFieldType(sourceField);

            // 检查类型兼容性
            if (!TypeUtils.isTypeCompatible(sourceFieldType, targetFieldType)) {
                context.warning("字段类型不兼容：" + sourceFieldType + " -> " + targetFieldType, targetField);
                return null;
            }

            return new FieldMapping(sourceField, targetField, sourceFieldType, targetFieldType);
        } else {
            // 多对一映射（需要表达式）
            context.error("多对一映射需要指定 expression 属性", targetField);
            return null;
        }
    }


    /**
     * 创建表达式映射。
     *
     * @param targetField     目标字段
     * @param targetFieldType 目标字段类型
     * @param sourceNames     源字段名数组
     * @param expression      表达式字符串
     * @param sourceFieldMap  源字段映射表
     * @param sourceType      源类型
     * @return 字段映射，如果无法创建则返回 null
     */
    FieldMapping createExpressionMapping(VariableElement targetField,
                                          TypeMirror targetFieldType,
                                          String[] sourceNames,
                                          String expression,
                                          Map<String, VariableElement> sourceFieldMap,
                                          TypeElement sourceType) {
        // 验证表达式语法
        String syntaxError = ExpressionUtils.validateSyntax(expression);
        if (syntaxError != null) {
            context.error("表达式语法错误: " + syntaxError, targetField);
            return null;
        }

        // 创建映射
        FieldMapping mapping = new FieldMapping(null, targetField, sourceType.asType(), targetFieldType);
        mapping.setExpression(expression);

        if (sourceNames != null && sourceNames.length > 0) {
            mapping.setSourceFieldNames(Arrays.asList(sourceNames));
            if (sourceNames.length > 1) {
                mapping.setMappingType(FieldMapping.MappingType.MANY_TO_ONE);
            } else {
                mapping.setMappingType(FieldMapping.MappingType.EXPRESSION);
            }
        } else {
            mapping.setMappingType(FieldMapping.MappingType.EXPRESSION);
        }

        // v1.3.1: 表达式映射在逆向转换中跳过
        mapping.setSkipInReverseMapping(true);
        mapping.setReverseSkipReason("表达式映射");

        return mapping;
    }

    /**
     * 创建类型转换器映射。
     *
     * @param targetField        目标字段
     * @param targetFieldType    目标字段类型
     * @param sourceNames        源字段名数组
     * @param converterClassName 转换器类名
     * @param format             格式字符串
     * @param sourceFieldMap     源字段映射表
     * @return 字段映射，如果无法创建则返回 null
     */
    FieldMapping createConverterMapping(VariableElement targetField,
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
            context.warning("找不到源字段 '" + sourceFieldName + "'", targetField);
            return null;
        }

        TypeMirror sourceFieldType = TypeUtils.getFieldType(sourceField);
        FieldMapping mapping = new FieldMapping(sourceField, targetField, sourceFieldType, targetFieldType);
        mapping.setMappingType(FieldMapping.MappingType.CONVERTER);
        mapping.setConverterClassName(converterClassName);
        mapping.setFormat(format);

        // v1.3.1: 类型转换器映射在逆向转换中跳过
        mapping.setSkipInReverseMapping(true);
        mapping.setReverseSkipReason("类型转换器映射");

        return mapping;
    }

    /**
     * 创建具名转换方法映射。
     *
     * @param targetField     目标字段
     * @param targetFieldType 目标字段类型
     * @param sourceNames     源字段名数组
     * @param qualifiedByName 具名转换方法名
     * @param sourceFieldMap  源字段映射表
     * @return 字段映射，如果无法创建则返回 null
     */
    FieldMapping createQualifiedByNameMapping(VariableElement targetField,
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
            context.warning("找不到源字段 '" + sourceFieldName + "'", targetField);
            return null;
        }

        TypeMirror sourceFieldType = TypeUtils.getFieldType(sourceField);
        FieldMapping mapping = new FieldMapping(sourceField, targetField, sourceFieldType, targetFieldType);
        mapping.setMappingType(FieldMapping.MappingType.QUALIFIED_BY_NAME);
        mapping.setQualifiedByName(qualifiedByName);

        // v1.3.1: 具名方法映射在逆向转换中跳过
        mapping.setSkipInReverseMapping(true);
        mapping.setReverseSkipReason("具名方法映射");

        return mapping;
    }

    /**
     * 验证字段类型兼容性。
     *
     * @param sourceType 源类型
     * @param targetType 目标类型
     * @return 如果类型兼容返回 true
     */
    public boolean isTypeCompatible(TypeMirror sourceType, TypeMirror targetType) {
        return TypeUtils.isTypeCompatible(sourceType, targetType);
    }

    /**
     * 构建字段名到字段元素的映射表。
     *
     * @param fields 字段列表
     * @return 字段映射表
     */
    private Map<String, VariableElement> buildFieldMap(List<VariableElement> fields) {
        Map<String, VariableElement> fieldMap = new HashMap<>();
        for (VariableElement field : fields) {
            String fieldName = field.getSimpleName().toString();
            fieldMap.put(fieldName, field);
        }
        return fieldMap;
    }

    /**
     * 获取注解提取器。
     *
     * @return 注解提取器
     */
    public AnnotationExtractor getExtractor() {
        return extractor;
    }
}
