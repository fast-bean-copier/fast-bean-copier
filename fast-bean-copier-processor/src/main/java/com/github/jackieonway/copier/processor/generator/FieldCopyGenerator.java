package com.github.jackieonway.copier.processor.generator;

import com.github.jackieonway.copier.processor.FieldMapping;
import com.github.jackieonway.copier.processor.TypeUtils;
import com.github.jackieonway.copier.processor.context.ProcessorContext;
import com.squareup.javapoet.MethodSpec;

import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeMirror;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

/**
 * 字段拷贝代码生成器。
 *
 * <p>负责生成单个字段的拷贝代码，包括：
 * <ul>
 *   <li>简单字段拷贝（同名同类型）</li>
 *   <li>表达式字段拷贝（使用 expression）</li>
 *   <li>转换器字段拷贝（使用 converter）</li>
 *   <li>具名方法字段拷贝（使用 qualifiedByName）</li>
 *   <li>基本类型/包装类型转换</li>
 *   <li>集合类型深拷贝（委托给 DeepCopyGenerator）</li>
 *   <li>嵌套对象深拷贝（v1.3.2 新增）</li>
 * </ul>
 *
 * <p>v1.3.2 新增功能：
 * <ul>
 *   <li>支持不同类型嵌套对象的自动深拷贝（Address → AddressDto）</li>
 *   <li>优先使用 Copier 类进行拷贝（性能最优）</li>
 *   <li>无 Copier 时自动回退到字段拷贝（智能回退机制）</li>
 *   <li>支持无限层级嵌套（A→B→C→D...）</li>
 * </ul>
 *
 * @author jackieonway
 * @since 1.2.1
 */
public class FieldCopyGenerator {

    private final ProcessorContext context;
    private final DeepCopyGenerator deepCopyGenerator;
    
    /** uses 类列表 */
    private List<TypeMirror> usesClasses;
    
    /** 是否使用静态方法 */
    private boolean useStaticMethods = true;

    /**
     * 构造方法。
     *
     * @param context 处理器上下文
     */
    public FieldCopyGenerator(ProcessorContext context) {
        this.context = context;
        this.deepCopyGenerator = new DeepCopyGenerator(context);
    }

    /**
     * 设置 uses 类列表。
     *
     * @param usesClasses uses 类列表
     */
    public void setUsesClasses(List<TypeMirror> usesClasses) {
        this.usesClasses = usesClasses;
    }

    /**
     * 设置是否使用静态方法。
     *
     * @param useStaticMethods 是否使用静态方法
     */
    public void setUseStaticMethods(boolean useStaticMethods) {
        this.useStaticMethods = useStaticMethods;
    }

    /**
     * 生成字段拷贝代码。
     *
     * @param methodBuilder 方法构建器
     * @param mapping       字段映射
     * @param reverse       是否反向拷贝（fromDto）
     */
    public void generateFieldCopyCode(MethodSpec.Builder methodBuilder, FieldMapping mapping, boolean reverse) {
        // v1.3: 处理常量映射
        if (mapping.isConstantMapping()) {
            generateConstantFieldCopyCode(methodBuilder, mapping, reverse);
            return;
        }

        // v1.3: 处理条件映射
        if (mapping.hasCondition()) {
            generateConditionalFieldCopyCode(methodBuilder, mapping, reverse);
            return;
        }

        // v1.3: 处理默认值映射
        if (mapping.hasDefaultValue()) {
            generateDefaultValueFieldCopyCode(methodBuilder, mapping, reverse);
            return;
        }

        switch (mapping.getMappingType()) {
            case EXPRESSION:
            case MANY_TO_ONE:
                generateExpressionFieldCopyCode(methodBuilder, mapping, reverse);
                return;
            case CONVERTER:
                generateConverterFieldCopyCode(methodBuilder, mapping, reverse);
                return;
            case QUALIFIED_BY_NAME:
                generateQualifiedByNameFieldCopyCode(methodBuilder, mapping, reverse);
                return;
            case SIMPLE:
            default:
                generateSimpleFieldCopyCode(methodBuilder, mapping, reverse);
                break;
        }
    }

    /**
     * 生成条件映射代码。
     *
     * @param methodBuilder 方法构建器
     * @param mapping       字段映射
     * @param reverse       是否反向拷贝
     * @since 1.3.0
     */
    private void generateConditionalFieldCopyCode(MethodSpec.Builder methodBuilder, 
                                                   FieldMapping mapping, boolean reverse) {
        String conditionExpression = mapping.getConditionExpression();
        if (conditionExpression == null || conditionExpression.isEmpty()) {
            // 如果条件表达式为空，回退到普通映射
            generateFieldCopyCodeWithoutCondition(methodBuilder, mapping, reverse);
            return;
        }

        // 生成条件判断代码
        methodBuilder.beginControlFlow("if ($L)", conditionExpression);
        
        // 在条件块内生成实际的字段拷贝代码
        generateFieldCopyCodeWithoutCondition(methodBuilder, mapping, reverse);
        
        methodBuilder.endControlFlow();
    }

    /**
     * 生成不带条件的字段拷贝代码（用于条件映射内部）。
     *
     * @param methodBuilder 方法构建器
     * @param mapping       字段映射
     * @param reverse       是否反向拷贝
     * @since 1.3.0
     */
    private void generateFieldCopyCodeWithoutCondition(MethodSpec.Builder methodBuilder, 
                                                        FieldMapping mapping, boolean reverse) {
        // 临时清除条件，避免递归
        String originalCondition = mapping.getCondition();
        mapping.setCondition(null);

        // 处理默认值映射
        if (mapping.hasDefaultValue()) {
            generateDefaultValueFieldCopyCode(methodBuilder, mapping, reverse);
        } else {
            switch (mapping.getMappingType()) {
                case EXPRESSION:
                case MANY_TO_ONE:
                    generateExpressionFieldCopyCode(methodBuilder, mapping, reverse);
                    break;
                case CONVERTER:
                    generateConverterFieldCopyCode(methodBuilder, mapping, reverse);
                    break;
                case QUALIFIED_BY_NAME:
                    generateQualifiedByNameFieldCopyCode(methodBuilder, mapping, reverse);
                    break;
                case SIMPLE:
                default:
                    generateSimpleFieldCopyCode(methodBuilder, mapping, reverse);
                    break;
            }
        }

        // 恢复条件
        mapping.setCondition(originalCondition);
    }

    /**
     * 生成默认值字段拷贝代码。
     *
     * @param methodBuilder 方法构建器
     * @param mapping       字段映射
     * @param reverse       是否反向拷贝
     * @since 1.3.0
     */
    private void generateDefaultValueFieldCopyCode(MethodSpec.Builder methodBuilder, 
                                                    FieldMapping mapping, boolean reverse) {
        String sourceFieldName = reverse ? mapping.getTargetFieldName() : mapping.getSourceFieldName();
        String targetFieldName = reverse ? mapping.getSourceFieldName() : mapping.getTargetFieldName();

        if (sourceFieldName == null) {
            return;
        }

        String getterName = "get" + capitalize(sourceFieldName);
        String setterName = "set" + capitalize(targetFieldName);
        String defaultValue = mapping.getDefaultValue();
        TypeMirror targetFieldType = reverse ? mapping.getSourceType() : mapping.getTargetType();

        // 生成带默认值的拷贝代码
        String convertedDefaultValue = convertStringToTypeLiteral(defaultValue, targetFieldType);
        
        methodBuilder.beginControlFlow("if (source.$L() != null)", getterName);
        methodBuilder.addStatement("target.$L(source.$L())", setterName, getterName);
        methodBuilder.nextControlFlow("else");
        methodBuilder.addStatement("target.$L($L)", setterName, convertedDefaultValue);
        methodBuilder.endControlFlow();
    }

    /**
     * 生成常量字段拷贝代码。
     *
     * @param methodBuilder 方法构建器
     * @param mapping       字段映射
     * @param reverse       是否反向拷贝
     * @since 1.3.0
     */
    private void generateConstantFieldCopyCode(MethodSpec.Builder methodBuilder, 
                                                FieldMapping mapping, boolean reverse) {
        // 常量映射只在正向拷贝时使用
        if (reverse) {
            return;
        }

        String targetFieldName = mapping.getTargetFieldName();
        String setterName = "set" + capitalize(targetFieldName);
        String constant = mapping.getConstant();
        TypeMirror targetFieldType = mapping.getTargetType();

        // 转换常量值为目标类型的字面量
        String convertedConstant = convertStringToTypeLiteral(constant, targetFieldType);
        
        methodBuilder.addStatement("target.$L($L)", setterName, convertedConstant);
    }

    /**
     * 将字符串值转换为目标类型的字面量表示。
     *
     * @param value      字符串值
     * @param targetType 目标类型
     * @return 类型字面量
     * @since 1.3.0
     */
    private String convertStringToTypeLiteral(String value, TypeMirror targetType) {
        if (targetType == null || value == null) {
            return "\"" + value + "\"";
        }

        String typeName = targetType.toString();

        // String 类型
        if (typeName.equals("java.lang.String")) {
            return "\"" + escapeString(value) + "\"";
        }

        // Integer/int 类型
        if (typeName.equals("java.lang.Integer") || typeName.equals("int")) {
            return value;
        }

        // Long/long 类型
        if (typeName.equals("java.lang.Long") || typeName.equals("long")) {
            return value + "L";
        }

        // Double/double 类型
        if (typeName.equals("java.lang.Double") || typeName.equals("double")) {
            return value + "D";
        }

        // Float/float 类型
        if (typeName.equals("java.lang.Float") || typeName.equals("float")) {
            return value + "F";
        }

        // Short/short 类型
        if (typeName.equals("java.lang.Short") || typeName.equals("short")) {
            return "(short) " + value;
        }

        // Byte/byte 类型
        if (typeName.equals("java.lang.Byte") || typeName.equals("byte")) {
            return "(byte) " + value;
        }

        // Boolean/boolean 类型
        if (typeName.equals("java.lang.Boolean") || typeName.equals("boolean")) {
            return value.toLowerCase();
        }

        // BigDecimal 类型
        if (typeName.equals("java.math.BigDecimal")) {
            return "new java.math.BigDecimal(\"" + value + "\")";
        }

        // BigInteger 类型
        if (typeName.equals("java.math.BigInteger")) {
            return "new java.math.BigInteger(\"" + value + "\")";
        }

        // 默认作为字符串处理
        return "\"" + escapeString(value) + "\"";
    }

    /**
     * 转义字符串中的特殊字符。
     *
     * @param str 原始字符串
     * @return 转义后的字符串
     * @since 1.3.0
     */
    private String escapeString(String str) {
        if (str == null) {
            return "";
        }
        return str.replace("\\", "\\\\")
                  .replace("\"", "\\\"")
                  .replace("\n", "\\n")
                  .replace("\r", "\\r")
                  .replace("\t", "\\t");
    }

    /**
     * 生成简单字段拷贝代码。
     *
     * <p>处理以下类型的字段拷贝：
     * <ul>
     *   <li>集合类型（List、Set、Map、数组）：委托给 DeepCopyGenerator</li>
     *   <li>嵌套对象（v1.3.2）：调用 {@link #generateNestedObjectCopyCode}</li>
     *   <li>基本类型/包装类型：自动装箱/拆箱</li>
     *   <li>简单字段：直接赋值</li>
     * </ul>
     *
     * <p>v1.3.2 新增：支持不同类型嵌套对象的深拷贝。
     * 如果源字段和目标字段都是自定义对象且类型不同，
     * 会尝试使用 Copier 或字段拷贝实现转换。
     *
     * @param methodBuilder 方法构建器
     * @param mapping       字段映射
     * @param reverse       是否反向拷贝
     * @since 1.2.1
     */
    private void generateSimpleFieldCopyCode(MethodSpec.Builder methodBuilder, FieldMapping mapping, boolean reverse) {
        String sourceFieldName = reverse ? mapping.getTargetFieldName() : mapping.getSourceFieldName();
        String targetFieldName = reverse ? mapping.getSourceFieldName() : mapping.getTargetFieldName();

        if (sourceFieldName == null) {
            return;
        }

        // v1.3.1: 检查是否在逆向转换中跳过
        if (reverse && mapping.isSkipInReverseMapping()) {
            String skipReason = mapping.getReverseSkipReason();
            methodBuilder.addComment(skipReason + " '" + targetFieldName + "' 不可逆，在 fromDto()/updateEntity() 中跳过");
            return;
        }

        String getterName = "get" + capitalize(sourceFieldName);
        String setterName = "set" + capitalize(targetFieldName);

        TypeMirror sourceFieldType = reverse ? mapping.getTargetType() : mapping.getSourceType();
        TypeMirror targetFieldType = reverse ? mapping.getSourceType() : mapping.getTargetType();

        // 检查是否有不支持的泛型
        if (deepCopyGenerator.hasUnsupportedGenerics(sourceFieldType) 
                || deepCopyGenerator.hasUnsupportedGenerics(targetFieldType)) {
            deepCopyGenerator.warnUnsupportedGenerics(sourceFieldType, targetFieldType, mapping);
            methodBuilder.addStatement("target.$L(source.$L())", setterName, getterName);
            return;
        }

        // v1.4: 检查深拷贝控制标记
        boolean enableDeepCopy = mapping.isDeepCopy();

        // 集合类型深拷贝
        if (TypeUtils.isList(sourceFieldType) && TypeUtils.isList(targetFieldType)) {
            if (enableDeepCopy) {
                deepCopyGenerator.generateListDeepCopyCode(methodBuilder, getterName, setterName, 
                        sourceFieldType, targetFieldType, mapping, reverse, useStaticMethods);
            } else {
                // 浅拷贝：直接赋值引用
                methodBuilder.addStatement("target.$L(source.$L())", setterName, getterName);
            }
            return;
        }

        if (TypeUtils.isSet(sourceFieldType) && TypeUtils.isSet(targetFieldType)) {
            if (enableDeepCopy) {
                deepCopyGenerator.generateSetDeepCopyCode(methodBuilder, getterName, setterName, 
                        sourceFieldType, targetFieldType, mapping, reverse, useStaticMethods);
            } else {
                // 浅拷贝：直接赋值引用
                methodBuilder.addStatement("target.$L(source.$L())", setterName, getterName);
            }
            return;
        }

        if (TypeUtils.isArrayType(sourceFieldType) && TypeUtils.isArrayType(targetFieldType)) {
            if (enableDeepCopy) {
                deepCopyGenerator.generateArrayDeepCopyCode(methodBuilder, getterName, setterName, 
                        sourceFieldType, targetFieldType, mapping, reverse, useStaticMethods);
            } else {
                // 浅拷贝：直接赋值引用
                methodBuilder.addStatement("target.$L(source.$L())", setterName, getterName);
            }
            return;
        }

        if (TypeUtils.isMap(sourceFieldType) && TypeUtils.isMap(targetFieldType)) {
            if (enableDeepCopy) {
                deepCopyGenerator.generateMapDeepCopyCode(methodBuilder, getterName, setterName, 
                        sourceFieldType, targetFieldType, mapping, reverse, useStaticMethods);
            } else {
                // 浅拷贝：直接赋值引用
                methodBuilder.addStatement("target.$L(source.$L())", setterName, getterName);
            }
            return;
        }

        // ⭐ 新增：嵌套对象深拷贝
        if (TypeUtils.needsDeepCopy(sourceFieldType) && TypeUtils.needsDeepCopy(targetFieldType)) {
            // 检查是否为同类型（同类型使用简单赋值）
            if (sourceFieldType.toString().equals(targetFieldType.toString())) {
                methodBuilder.addStatement("target.$L(source.$L())", setterName, getterName);
                return;
            }
            
            // v1.4: 检查深拷贝控制标记
            if (enableDeepCopy) {
                // 不同类型：尝试使用 Copier，如果没有则使用字段拷贝
                generateNestedObjectCopyCode(methodBuilder, mapping, reverse, 
                        sourceFieldType, targetFieldType, getterName, setterName);
            } else {
                // 浅拷贝：直接赋值引用
                methodBuilder.addStatement("target.$L(source.$L())", setterName, getterName);
            }
            return;
        }

        // 基本类型/包装类型转换
        if (needsTypeConversion(sourceFieldType, targetFieldType)) {
            String conversionCode = generateConversionCode(sourceFieldType, targetFieldType, 
                    "source." + getterName + "()");
            methodBuilder.addStatement("target.$L($L)", setterName, conversionCode);
            return;
        }

        // 简单赋值
        methodBuilder.addStatement("target.$L(source.$L())", setterName, getterName);
    }


    /**
     * 生成表达式字段拷贝代码。
     */
    private void generateExpressionFieldCopyCode(MethodSpec.Builder methodBuilder, 
                                                  FieldMapping mapping, boolean reverse) {
        String targetFieldName = mapping.getTargetFieldName();
        String setterName = "set" + capitalize(targetFieldName);
        String expression = mapping.getExpression();
        
        if (expression == null || expression.trim().isEmpty()) {
            return;
        }
        
        // v1.3.1: 表达式只在正向拷贝时使用，反向拷贝时跳过
        if (reverse) {
            if (mapping.isSkipInReverseMapping()) {
                String skipReason = mapping.getReverseSkipReason();
                String sourceFieldName = mapping.getSourceFieldNames() != null && !mapping.getSourceFieldNames().isEmpty() 
                        ? mapping.getSourceFieldNames().get(0) : targetFieldName;
                methodBuilder.addComment(skipReason + " '" + sourceFieldName + "' 不可逆，在 fromDto()/updateEntity() 中跳过");
            }
            return;
        }
        
        // 生成表达式代码
        methodBuilder.addStatement("target.$L($L)", setterName, expression);
    }

    /**
     * 生成转换器字段拷贝代码。
     */
    private void generateConverterFieldCopyCode(MethodSpec.Builder methodBuilder, 
                                                 FieldMapping mapping, boolean reverse) {
        String sourceFieldName = reverse ? mapping.getTargetFieldName() : mapping.getSourceFieldName();
        String targetFieldName = reverse ? mapping.getSourceFieldName() : mapping.getTargetFieldName();
        
        if (sourceFieldName == null) {
            return;
        }
        
        // v1.3.1: 转换器映射在逆向转换中跳过
        if (reverse && mapping.isSkipInReverseMapping()) {
            String skipReason = mapping.getReverseSkipReason();
            methodBuilder.addComment(skipReason + " '" + targetFieldName + "' 不可逆，在 fromDto()/updateEntity() 中跳过");
            return;
        }
        
        String getterName = "get" + capitalize(sourceFieldName);
        String setterName = "set" + capitalize(targetFieldName);
        String converterFieldName = getConverterFieldName(mapping.getConverterClassName());
        String format = mapping.getFormat() != null ? mapping.getFormat() : "";
        
        // 生成转换器调用代码
        if (useStaticMethods) {
            methodBuilder.addStatement("target.$L($L.convert(source.$L(), $S))", 
                    setterName, converterFieldName, getterName, format);
        } else {
            methodBuilder.addStatement("target.$L(this.$L.convert(source.$L(), $S))", 
                    setterName, converterFieldName, getterName, format);
        }
    }

    /**
     * 生成具名转换方法字段拷贝代码。
     */
    private void generateQualifiedByNameFieldCopyCode(MethodSpec.Builder methodBuilder, 
                                                       FieldMapping mapping, boolean reverse) {
        // v1.3.1: qualifiedByName 映射在逆向转换中跳过
        if (reverse) {
            if (mapping.isSkipInReverseMapping()) {
                String skipReason = mapping.getReverseSkipReason();
                String targetFieldName = mapping.getSourceFieldName();
                methodBuilder.addComment(skipReason + " '" + targetFieldName + "' 不可逆，在 fromDto() 中跳过");
            }
            return;
        }
        
        String sourceFieldName = mapping.getSourceFieldName();
        String targetFieldName = mapping.getTargetFieldName();
        
        if (sourceFieldName == null) {
            return;
        }
        
        String getterName = "get" + capitalize(sourceFieldName);
        String setterName = "set" + capitalize(targetFieldName);
        String methodName = mapping.getQualifiedByName();
        
        // 查找包含该方法的 uses 类
        String usesFieldName = findUsesFieldForMethod(methodName);
        
        if (usesFieldName != null) {
            if (useStaticMethods) {
                methodBuilder.addStatement("target.$L($L.$L(source.$L()))", 
                        setterName, usesFieldName, methodName, getterName);
            } else {
                methodBuilder.addStatement("target.$L(this.$L.$L(source.$L()))", 
                        setterName, usesFieldName, methodName, getterName);
            }
        } else {
            // 如果找不到 uses 类，生成警告并使用简单赋值
            context.warning("找不到包含方法 '" + methodName + "' 的 uses 类", null);
            methodBuilder.addStatement("target.$L(source.$L())", setterName, getterName);
        }
    }

    /**
     * 查找包含指定方法的 uses 字段名。
     */
    private String findUsesFieldForMethod(String methodName) {
        // 简化实现：返回第一个 uses 类的字段名
        // 实际应该检查方法是否存在于该类中
        if (usesClasses != null && !usesClasses.isEmpty()) {
            return getUsesFieldName(usesClasses.get(0).toString());
        }
        return null;
    }

    /**
     * 判断是否需要类型转换。
     */
    private boolean needsTypeConversion(TypeMirror sourceType, TypeMirror targetType) {
        if (sourceType.toString().equals(targetType.toString())) {
            return false;
        }
        return (TypeUtils.isPrimitive(sourceType) && TypeUtils.isWrapper(targetType)) ||
               (TypeUtils.isWrapper(sourceType) && TypeUtils.isPrimitive(targetType));
    }

    /**
     * 生成类型转换代码。
     */
    private String generateConversionCode(TypeMirror sourceType, TypeMirror targetType, String valueCode) {
        // 基本类型 -> 包装类型（自动装箱）
        if (TypeUtils.isPrimitive(sourceType) && TypeUtils.isWrapper(targetType)) {
            return valueCode;
        }
        
        // 包装类型 -> 基本类型（自动拆箱 + null 处理）
        if (TypeUtils.isWrapper(sourceType) && TypeUtils.isPrimitive(targetType)) {
            String defaultValue = TypeUtils.getDefaultValue(targetType);
            return valueCode + " != null ? " + valueCode + " : " + defaultValue;
        }
        
        return valueCode;
    }

    /**
     * 获取转换器字段名。
     */
    private String getConverterFieldName(String converterClassName) {
        String simpleName = converterClassName.substring(converterClassName.lastIndexOf('.') + 1);
        return Character.toLowerCase(simpleName.charAt(0)) + simpleName.substring(1);
    }

    /**
     * 获取 uses 字段名。
     */
    private String getUsesFieldName(String usesClassName) {
        String simpleName = usesClassName.substring(usesClassName.lastIndexOf('.') + 1);
        return Character.toLowerCase(simpleName.charAt(0)) + simpleName.substring(1);
    }

    /**
     * 首字母大写。
     */
    private String capitalize(String str) {
        if (str == null || str.isEmpty()) {
            return str;
        }
        return str.substring(0, 1).toUpperCase() + str.substring(1);
    }

    /**
     * 检查目标类型是否有对应的 Copier 类。
     *
     * <p>通过检查目标类型是否有 @CopyTarget 注解来判断是否会生成 Copier。
     * <p><strong>重要：</strong>不能直接检查 Copier 类是否存在，因为在 APT 编译期该类可能还未生成。
     * 正确的做法是检查目标类型是否有 @CopyTarget 注解，如果有注解则会生成对应的 Copier 类。
     *
     * <p>示例：
     * <pre>{@code
     * @CopyTarget(source = Address.class)
     * public class AddressDto {
     *     // 会生成 AddressDtoCopier 类
     * }
     * }</pre>
     *
     * @param targetType 目标类型
     * @return 如果目标类型有 @CopyTarget 注解返回 true，否则返回 false
     * @since 1.3.2
     */
    private boolean checkCopierExists(TypeMirror targetType) {
        if (targetType == null) {
            return false;
        }
        
        // 获取目标类型的 TypeElement
        Element element = context.getTypeUtils().asElement(targetType);
        if (!(element instanceof TypeElement)) {
            return false;
        }
        
        // 检查是否有 @CopyTarget 注解
        TypeElement typeElement = (TypeElement) element;
        return typeElement.getAnnotation(com.github.jackieonway.copier.annotation.CopyTarget.class) != null;
    }

    /**
     * 生成嵌套对象拷贝代码。
     *
     * <p>优先使用 Copier 类进行拷贝，如果没有 Copier 则使用字段拷贝。
     *
     * <p>实现策略：
     * <ol>
     *   <li>检查 DTO 类型是否有 @CopyTarget 注解（通过 {@link #checkCopierExists}）</li>
     *   <li>如果有注解：生成调用 Copier 的代码（toDto/fromDto）</li>
     *   <li>如果没有注解：生成字段拷贝代码（通过 {@link #generateFieldBasedCopy}）</li>
     * </ol>
     *
     * <p>生成的代码示例（有 Copier）：
     * <pre>{@code
     * if (source.getAddress() != null) {
     *     target.setAddress(AddressDtoCopier.toDto(source.getAddress()));
     * } else {
     *     target.setAddress(null);
     * }
     * }</pre>
     *
     * <p>生成的代码示例（无 Copier）：
     * <pre>{@code
     * if (source.getAddress() != null) {
     *     AddressDto nestedAddress = new AddressDto();
     *     nestedAddress.setProvince(source.getAddress().getProvince());
     *     nestedAddress.setCity(source.getAddress().getCity());
     *     target.setAddress(nestedAddress);
     * } else {
     *     target.setAddress(null);
     * }
     * }</pre>
     *
     * @param methodBuilder    方法构建器
     * @param mapping          字段映射
     * @param reverse          是否反向拷贝（true: fromDto, false: toDto）
     * @param sourceFieldType  源字段类型
     * @param targetFieldType  目标字段类型
     * @param getterName       getter 方法名（不含括号）
     * @param setterName       setter 方法名（不含括号）
     * @since 1.3.2
     */
    private void generateNestedObjectCopyCode(MethodSpec.Builder methodBuilder,
                                               FieldMapping mapping,
                                               boolean reverse,
                                               TypeMirror sourceFieldType,
                                               TypeMirror targetFieldType,
                                               String getterName,
                                               String setterName) {
        // 在正向转换中，检查目标类型（DTO）是否有 @CopyTarget
        // 在反向转换中，检查源类型（DTO）是否有 @CopyTarget
        TypeMirror dtoType = reverse ? sourceFieldType : targetFieldType;
        boolean hasCopier = checkCopierExists(dtoType);
        
        if (hasCopier) {
            // 使用 Copier 进行拷贝（使用三元表达式简化）
            String copierClassName = dtoType.toString() + "Copier";
            String copierSimpleName = copierClassName.substring(copierClassName.lastIndexOf('.') + 1);
            
            // 根据转换方向选择方法名
            String copierMethod = reverse ? "fromDto" : "toDto";
            
            // 生成三元表达式形式的 Copier 调用
            methodBuilder.addStatement("target.$L(source.$L() != null ? $L.$L(source.$L()) : null)", 
                    setterName, getterName, copierSimpleName, copierMethod, getterName);
        } else {
            // 没有 Copier，使用字段拷贝
            generateFieldBasedCopy(methodBuilder, sourceFieldType, targetFieldType, 
                    "source." + getterName + "()", "target." + setterName, reverse);
        }
    }

    /**
     * 生成基于字段的拷贝代码（无 Copier 时使用）。
     *
     * <p>当嵌套对象没有 @CopyTarget 注解时，使用此方法生成字段拷贝代码。
     * 通过 getter/setter 递归拷贝所有同名且类型兼容的字段。
     *
     * <p>拷贝规则：
     * <ul>
     *   <li>只拷贝同名字段</li>
     *   <li>只拷贝类型兼容的字段（通过 {@link TypeUtils#isTypeCompatible} 判断）</li>
     *   <li>递归处理嵌套对象的嵌套对象</li>
     *   <li>对于嵌套对象，优先检查是否有 Copier，有则使用 Copier</li>
     *   <li>跳过 static 和 transient 字段</li>
     * </ul>
     *
     * <p>生成的代码示例：
     * <pre>{@code
     * if (source.getAddress() != null) {
     *     Address sourceAddress = source.getAddress();
     *     AddressDto nestedAddress = new AddressDto();
     *     nestedAddress.setProvince(sourceAddress.getProvince());
     *     nestedAddress.setCity(sourceAddress.getCity());
     *     // 如果 Address 有嵌套对象 Country，且 CountryDto 有 @CopyTarget
     *     nestedAddress.setCountry(sourceAddress.getCountry() != null ? CountryDtoCopier.toDto(sourceAddress.getCountry()) : null);
     *     target.setAddress(nestedAddress);
     * } else {
     *     target.setAddress(null);
     * }
     * }</pre>
     *
     * @param methodBuilder  方法构建器
     * @param sourceType     源类型
     * @param targetType     目标类型
     * @param sourceGetter   源对象的 getter 表达式（如 "source.getAddress()"）
     * @param targetSetter   目标对象的 setter 表达式，不含括号（如 "target.setAddress"）
     * @param reverse        是否反向拷贝（true: fromDto, false: toDto）
     * @since 1.3.2
     */
    private void generateFieldBasedCopy(MethodSpec.Builder methodBuilder,
                                        TypeMirror sourceType,
                                        TypeMirror targetType,
                                        String sourceGetter,
                                        String targetSetter,
                                        boolean reverse) {
        // 获取源类型和目标类型的 TypeElement
        Element sourceElement = context.getTypeUtils().asElement(sourceType);
        Element targetElement = context.getTypeUtils().asElement(targetType);
        
        if (!(sourceElement instanceof TypeElement) ||
            !(targetElement instanceof TypeElement)) {
            // 无法获取字段信息，使用简单赋值
            methodBuilder.addStatement("$L($L)", targetSetter, sourceGetter);
            return;
        }
        
        TypeElement sourceTypeElement = (TypeElement) sourceElement;
        TypeElement targetTypeElement = (TypeElement) targetElement;
        
        // 获取所有字段
        List<VariableElement> sourceFields = TypeUtils.getAllFields(sourceTypeElement);
        List<VariableElement> targetFields = TypeUtils.getAllFields(targetTypeElement);
        
        // 构建目标字段映射表
        Map<String, VariableElement> targetFieldMap = buildFieldMap(targetFields);
        
        // 生成临时变量和拷贝代码
        String targetTempVarName = "nested" + capitalize(targetTypeElement.getSimpleName().toString());
        String sourceTempVarName = "source" + capitalize(sourceTypeElement.getSimpleName().toString());
        
        methodBuilder.beginControlFlow("if ($L != null)", sourceGetter);
        // 引入源对象临时变量，避免重复调用 getter
        methodBuilder.addStatement("$T $L = $L", 
                sourceType, sourceTempVarName, sourceGetter);
        methodBuilder.addStatement("$T $L = new $T()", 
                targetType, targetTempVarName, targetType);
        
        // 为每个同名字段生成拷贝代码
        for (VariableElement sourceField : sourceFields) {
            String fieldName = sourceField.getSimpleName().toString();
            VariableElement targetField = targetFieldMap.get(fieldName);
            
            if (targetField == null) {
                continue; // 目标类型没有同名字段，跳过
            }
            
            TypeMirror sourceFieldType = sourceField.asType();
            TypeMirror targetFieldType = targetField.asType();
            
            // 检查类型兼容性
            if (!TypeUtils.isTypeCompatible(sourceFieldType, targetFieldType)) {
                continue; // 类型不兼容，跳过
            }
            
            String fieldGetterName = "get" + capitalize(fieldName);
            String fieldSetterName = "set" + capitalize(fieldName);
            
            // 递归处理嵌套对象
            if (TypeUtils.needsDeepCopy(sourceFieldType) && TypeUtils.needsDeepCopy(targetFieldType)) {
                if (!sourceFieldType.toString().equals(targetFieldType.toString())) {
                    // 不同类型的嵌套对象，检查是否有 Copier
                    TypeMirror nestedDtoType = reverse ? sourceFieldType : targetFieldType;
                    boolean hasNestedCopier = checkCopierExists(nestedDtoType);
                    
                    if (hasNestedCopier) {
                        // 有 Copier，使用 Copier 进行拷贝（使用三元表达式简化）
                        String copierClassName = nestedDtoType.toString() + "Copier";
                        String copierSimpleName = copierClassName.substring(copierClassName.lastIndexOf('.') + 1);
                        String copierMethod = reverse ? "fromDto" : "toDto";
                        
                        methodBuilder.addStatement("$L.$L($L.$L() != null ? $L.$L($L.$L()) : null)", 
                                targetTempVarName, fieldSetterName, sourceTempVarName, fieldGetterName,
                                copierSimpleName, copierMethod, sourceTempVarName, fieldGetterName);
                    } else {
                        // 没有 Copier，递归生成字段拷贝
                        String nestedSourceGetter = sourceTempVarName + "." + fieldGetterName + "()";
                        String nestedTargetSetter = targetTempVarName + "." + fieldSetterName;
                        generateFieldBasedCopy(methodBuilder, sourceFieldType, targetFieldType,
                                nestedSourceGetter, nestedTargetSetter, reverse);
                    }
                    continue;
                }
            }
            
            // 简单字段拷贝
            methodBuilder.addStatement("$L.$L($L.$L())", 
                    targetTempVarName, fieldSetterName, sourceTempVarName, fieldGetterName);
        }
        
        methodBuilder.addStatement("$L($L)", targetSetter, targetTempVarName);
        methodBuilder.nextControlFlow("else");
        methodBuilder.addStatement("$L(null)", targetSetter);
        methodBuilder.endControlFlow();
    }

    /**
     * 构建字段映射表。
     *
     * <p>将字段列表转换为 Map，key 为字段名，value 为字段元素。
     * 用于快速查找目标类型中是否存在同名字段。
     *
     * @param fields 字段列表
     * @return 字段名到字段元素的映射
     * @since 1.3.2
     */
    private Map<String, VariableElement> buildFieldMap(List<VariableElement> fields) {
        Map<String, VariableElement> fieldMap = new HashMap<>();
        for (VariableElement field : fields) {
            fieldMap.put(field.getSimpleName().toString(), field);
        }
        return fieldMap;
    }
}
