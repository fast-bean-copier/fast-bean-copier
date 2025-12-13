package com.github.jackieonway.copier.processor;

import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeMirror;

/**
 * 字段映射关系的数据类。
 *
 * 用于表示源对象和目标对象之间的字段映射关系，
 * 包含源字段、目标字段及其类型信息。
 *
 * @author jackieonway
 * @since 1.0.0
 */
public class FieldMapping {

    /**
     * 源字段。
     */
    private final VariableElement sourceField;

    /**
     * 目标字段。
     */
    private final VariableElement targetField;

    /**
     * 源字段的类型。
     */
    private final TypeMirror sourceType;

    /**
     * 目标字段的类型。
     */
    private final TypeMirror targetType;

    /**
     * 构造方法。
     *
     * @param sourceField 源字段
     * @param targetField 目标字段
     * @param sourceType  源字段的类型
     * @param targetType  目标字段的类型
     */
    public FieldMapping(VariableElement sourceField, VariableElement targetField,
                        TypeMirror sourceType, TypeMirror targetType) {
        this.sourceField = sourceField;
        this.targetField = targetField;
        this.sourceType = sourceType;
        this.targetType = targetType;
    }

    /**
     * 获取源字段。
     *
     * @return 源字段
     */
    public VariableElement getSourceField() {
        return sourceField;
    }

    /**
     * 获取目标字段。
     *
     * @return 目标字段
     */
    public VariableElement getTargetField() {
        return targetField;
    }

    /**
     * 获取源字段的类型。
     *
     * @return 源字段的类型
     */
    public TypeMirror getSourceType() {
        return sourceType;
    }

    /**
     * 获取目标字段的类型。
     *
     * @return 目标字段的类型
     */
    public TypeMirror getTargetType() {
        return targetType;
    }

    /**
     * 获取源字段的名称。
     *
     * @return 源字段的名称
     */
    public String getSourceFieldName() {
        return sourceField.getSimpleName().toString();
    }

    /**
     * 获取目标字段的名称。
     *
     * @return 目标字段的名称
     */
    public String getTargetFieldName() {
        return targetField.getSimpleName().toString();
    }

    /**
     * 返回字段映射的字符串表示。
     *
     * @return 字符串表示
     */
    @Override
    public String toString() {
        return "FieldMapping{" +
                "sourceField='" + getSourceFieldName() + '\'' +
                ", targetField='" + getTargetFieldName() + '\'' +
                ", sourceType=" + sourceType +
                ", targetType=" + targetType +
                '}';
    }
}
