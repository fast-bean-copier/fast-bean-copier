package com.github.jackieonway.copier.processor;

import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

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
        if (type == null) {
            return false;
        }
        TypeKind kind = type.getKind();
        return kind == TypeKind.INT || kind == TypeKind.LONG || kind == TypeKind.SHORT ||
                kind == TypeKind.BYTE || kind == TypeKind.FLOAT || kind == TypeKind.DOUBLE ||
                kind == TypeKind.CHAR || kind == TypeKind.BOOLEAN;
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
        if (type == null || type.getKind() != TypeKind.DECLARED) {
            return false;
        }
        String typeName = type.toString();
        return typeName.equals("java.lang.Integer") || typeName.equals("java.lang.Long") ||
                typeName.equals("java.lang.Short") || typeName.equals("java.lang.Byte") ||
                typeName.equals("java.lang.Float") || typeName.equals("java.lang.Double") ||
                typeName.equals("java.lang.Character") || typeName.equals("java.lang.Boolean");
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
        if (source == null || target == null) {
            return false;
        }

        // 完全相同的类型
        if (source.toString().equals(target.toString())) {
            return true;
        }

        // 基本类型与包装类型的兼容性
        if (isPrimitive(source) && isWrapper(target)) {
            return isPrimitiveWrapperMatch(source, target);
        }

        if (isWrapper(source) && isPrimitive(target)) {
            return isPrimitiveWrapperMatch(target, source);
        }

        return false;
    }

    /**
     * 判断基本类型和包装类型是否匹配。
     *
     * @param primitiveType 基本类型
     * @param wrapperType   包装类型
     * @return 如果匹配，返回 true；否则返回 false
     */
    private static boolean isPrimitiveWrapperMatch(TypeMirror primitiveType, TypeMirror wrapperType) {
        TypeKind kind = primitiveType.getKind();
        String wrapperName = wrapperType.toString();

        switch (kind) {
            case INT:
                return wrapperName.equals("java.lang.Integer");
            case LONG:
                return wrapperName.equals("java.lang.Long");
            case SHORT:
                return wrapperName.equals("java.lang.Short");
            case BYTE:
                return wrapperName.equals("java.lang.Byte");
            case FLOAT:
                return wrapperName.equals("java.lang.Float");
            case DOUBLE:
                return wrapperName.equals("java.lang.Double");
            case CHAR:
                return wrapperName.equals("java.lang.Character");
            case BOOLEAN:
                return wrapperName.equals("java.lang.Boolean");
            default:
                return false;
        }
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
        List<VariableElement> fields = new ArrayList<>();
        if (element == null) {
            return fields;
        }

        // 获取当前类的所有字段
        for (Object enclosedElement : element.getEnclosedElements()) {
            if (enclosedElement instanceof VariableElement) {
                VariableElement field = (VariableElement) enclosedElement;
                Set<Modifier> modifiers = field.getModifiers();
                // 过滤掉 static 和 transient 字段
                if (!modifiers.contains(Modifier.STATIC) && !modifiers.contains(Modifier.TRANSIENT)) {
                    fields.add(field);
                }
            }
        }

        return fields;
    }

    /**
     * 获取字段的类型。
     *
     * @param field 要获取类型的字段
     * @return 字段的类型
     */
    public static TypeMirror getFieldType(VariableElement field) {
        if (field == null) {
            return null;
        }
        return field.asType();
    }

    /**
     * 获取基本类型对应的包装类型名称。
     *
     * @param primitiveType 基本类型
     * @return 包装类型的全限定名
     */
    public static String getWrapperTypeName(TypeMirror primitiveType) {
        if (primitiveType == null) {
            return null;
        }
        TypeKind kind = primitiveType.getKind();
        switch (kind) {
            case INT:
                return "java.lang.Integer";
            case LONG:
                return "java.lang.Long";
            case SHORT:
                return "java.lang.Short";
            case BYTE:
                return "java.lang.Byte";
            case FLOAT:
                return "java.lang.Float";
            case DOUBLE:
                return "java.lang.Double";
            case CHAR:
                return "java.lang.Character";
            case BOOLEAN:
                return "java.lang.Boolean";
            default:
                return null;
        }
    }

    /**
     * 获取包装类型对应的基本类型名称。
     *
     * @param wrapperType 包装类型
     * @return 基本类型的名称
     */
    public static String getPrimitiveTypeName(TypeMirror wrapperType) {
        if (wrapperType == null || wrapperType.getKind() != TypeKind.DECLARED) {
            return null;
        }
        String typeName = wrapperType.toString();
        switch (typeName) {
            case "java.lang.Integer":
                return "int";
            case "java.lang.Long":
                return "long";
            case "java.lang.Short":
                return "short";
            case "java.lang.Byte":
                return "byte";
            case "java.lang.Float":
                return "float";
            case "java.lang.Double":
                return "double";
            case "java.lang.Character":
                return "char";
            case "java.lang.Boolean":
                return "boolean";
            default:
                return null;
        }
    }

    /**
     * 获取基本类型的默认值。
     *
     * @param primitiveType 基本类型
     * @return 默认值的字符串表示
     */
    public static String getDefaultValue(TypeMirror primitiveType) {
        if (primitiveType == null) {
            return null;
        }
        TypeKind kind = primitiveType.getKind();
        switch (kind) {
            case INT:
            case LONG:
            case SHORT:
            case BYTE:
                return "0";
            case FLOAT:
            case DOUBLE:
                return "0.0";
            case CHAR:
                return "'\\u0000'";
            case BOOLEAN:
                return "false";
            default:
                return null;
        }
    }

    /**
     * 判断是否为List类型。
     *
     * @param type 要检查的类型
     * @return 如果是List类型，返回 true；否则返回 false
     */
    public static boolean isList(TypeMirror type) {
        if (type == null || type.getKind() != TypeKind.DECLARED) {
            return false;
        }
        String typeName = type.toString();
        return typeName.startsWith("java.util.List") || typeName.startsWith("java.util.ArrayList");
    }

    /**
     * 判断是否为Set类型。
     *
     * @param type 要检查的类型
     * @return 如果是Set类型，返回 true；否则返回 false
     */
    public static boolean isSet(TypeMirror type) {
        if (type == null || type.getKind() != TypeKind.DECLARED) {
            return false;
        }
        String typeName = type.toString();
        return typeName.startsWith("java.util.Set") || typeName.startsWith("java.util.HashSet");
    }
}
