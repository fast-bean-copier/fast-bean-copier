package com.github.jackieonway.copier.processor.generator;

import com.github.jackieonway.copier.processor.FieldMapping;
import com.github.jackieonway.copier.processor.TypeUtils;
import com.github.jackieonway.copier.processor.context.ProcessorContext;
import com.squareup.javapoet.MethodSpec;

import javax.lang.model.type.TypeMirror;
import java.util.List;

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
     * 生成简单字段拷贝代码。
     */
    private void generateSimpleFieldCopyCode(MethodSpec.Builder methodBuilder, FieldMapping mapping, boolean reverse) {
        String sourceFieldName = reverse ? mapping.getTargetFieldName() : mapping.getSourceFieldName();
        String targetFieldName = reverse ? mapping.getSourceFieldName() : mapping.getTargetFieldName();

        if (sourceFieldName == null) {
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

        // 集合类型深拷贝
        if (TypeUtils.isList(sourceFieldType) && TypeUtils.isList(targetFieldType)) {
            deepCopyGenerator.generateListDeepCopyCode(methodBuilder, getterName, setterName, 
                    sourceFieldType, targetFieldType, mapping, reverse, useStaticMethods);
            return;
        }

        if (TypeUtils.isSet(sourceFieldType) && TypeUtils.isSet(targetFieldType)) {
            deepCopyGenerator.generateSetDeepCopyCode(methodBuilder, getterName, setterName, 
                    sourceFieldType, targetFieldType, mapping, reverse, useStaticMethods);
            return;
        }

        if (TypeUtils.isArrayType(sourceFieldType) && TypeUtils.isArrayType(targetFieldType)) {
            deepCopyGenerator.generateArrayDeepCopyCode(methodBuilder, getterName, setterName, 
                    sourceFieldType, targetFieldType, mapping, reverse, useStaticMethods);
            return;
        }

        if (TypeUtils.isMap(sourceFieldType) && TypeUtils.isMap(targetFieldType)) {
            deepCopyGenerator.generateMapDeepCopyCode(methodBuilder, getterName, setterName, 
                    sourceFieldType, targetFieldType, mapping, reverse, useStaticMethods);
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
        
        // 表达式只在正向拷贝时使用，反向拷贝时跳过（因为表达式通常是单向的）
        if (reverse) {
            // 反向拷贝时，尝试使用简单映射
            if (mapping.getSourceFieldNames() != null && mapping.getSourceFieldNames().size() == 1) {
                String sourceFieldName = mapping.getSourceFieldNames().get(0);
                methodBuilder.addStatement("target.$L(source.$L())", 
                        "set" + capitalize(sourceFieldName), 
                        "get" + capitalize(targetFieldName));
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
        // qualifiedByName 映射通常是单向的，反向拷贝时跳过
        if (reverse) {
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
}
