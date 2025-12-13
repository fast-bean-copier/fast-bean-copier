package com.github.jackieonway.copier.processor;

import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeMirror;
import java.util.List;

/**
 * 类型检查和处理的工具类。
 *
 * 提供了一系列静态方法用于检查和处理 Java 类型，
 * 包括基本类型、包装类型、嵌套对象等。
 *
 * @author jackieonway
 * @since 1.0.0
 */
public final class TypeUtils {

    /**
     * 私有构造方法，防止实例化。
     */
    private TypeUtils() {
        throw new AssertionError("No instances of TypeUtils");
    }

    /**
     * 判断给定的类型是否为基本类型。
     *
     * 基本类型包括：int、long、short、byte、float、double、char、boolean
     *
     * @param type 要检查的类型
     * @return 如果是基本类型，返回 true；否则返回 false
     */
    public static boolean isPrimitive(TypeMirror type) {
        // 暂时为空实现，后续补充具体逻辑
        return false;
    }

    /**
     * 判断给定的类型是否为包装类型。
     *
     * 包装类型包括：Integer、Long、Short、Byte、Float、Double、Character、Boolean
     *
     * @param type 要检查的类型
     * @return 如果是包装类型，返回 true；否则返回 false
     */
    public static boolean isWrapper(TypeMirror type) {
        // 暂时为空实现，后续补充具体逻辑
        return false;
    }

    /**
     * 判断两个类型是否兼容。
     *
     * 兼容的情况包括：
     * - 完全相同的类型
     * - 基本类型与对应的包装类型
     *
     * @param source 源类型
     * @param target 目标类型
     * @return 如果两个类型兼容，返回 true；否则返回 false
     */
    public static boolean isTypeCompatible(TypeMirror source, TypeMirror target) {
        // 暂时为空实现，后续补充具体逻辑
        return false;
    }

    /**
     * 获取类的所有字段。
     *
     * 包括继承自父类的字段，但不包括 static 和 transient 字段。
     *
     * @param element 要获取字段的类元素
     * @return 字段列表
     */
    public static List<VariableElement> getAllFields(TypeElement element) {
        // 暂时为空实现，后续补充具体逻辑
        return null;
    }

    /**
     * 获取字段的类型。
     *
     * @param field 要获取类型的字段
     * @return 字段的类型
     */
    public static TypeMirror getFieldType(VariableElement field) {
        // 暂时为空实现，后续补充具体逻辑
        return null;
    }
}
