package com.github.jackieonway.copier.processor;

import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import com.github.jackieonway.copier.annotation.CopyTarget;

import javax.lang.model.element.Element;
import javax.lang.model.type.ArrayType;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.type.WildcardType;
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

        // 同类型的集合/数组，允许泛型不同以便后续深拷贝处理
        if (isList(source) && isList(target)) {
            return true;
        }
        if (isSet(source) && isSet(target)) {
            return true;
        }
        if (isMap(source) && isMap(target)) {
            return true;
        }
        if (isArrayType(source) && isArrayType(target)) {
            return true;
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
        return isDeclaredType(type, "java.util.List", "java.util.ArrayList");
    }

    /**
     * 判断是否为Set类型。
     *
     * @param type 要检查的类型
     * @return 如果是Set类型，返回 true；否则返回 false
     */
    public static boolean isSet(TypeMirror type) {
        return isDeclaredType(type, "java.util.Set", "java.util.HashSet", "java.util.LinkedHashSet");
    }

    /**
     * 判断是否为Map类型。
     *
     * @param type 要检查的类型
     * @return 如果是Map类型，返回 true；否则返回 false
     */
    public static boolean isMap(TypeMirror type) {
        return isDeclaredType(type, "java.util.Map", "java.util.HashMap", "java.util.LinkedHashMap", "java.util.concurrent.ConcurrentHashMap");
    }

    /**
     * 判断是否为数组类型。
     *
     * @param type 要检查的类型
     * @return 如果是数组类型，返回 true；否则返回 false
     */
    public static boolean isArrayType(TypeMirror type) {
        return type != null && type.getKind() == TypeKind.ARRAY;
    }

    /**
     * 判断是否为基本类型或其包装类型。
     *
     * @param type 要检查的类型
     * @return 如果是基本类型或包装类型，返回 true；否则返回 false
     */
    public static boolean isBasicType(TypeMirror type) {
        return isPrimitive(type) || isWrapper(type);
    }

    /**
     * 判断是否为 String 类型。
     *
     * @param type 要检查的类型
     * @return 如果是 String 类型，返回 true；否则返回 false
     */
    public static boolean isStringType(TypeMirror type) {
        return isDeclaredType(type, "java.lang.String");
    }

    /**
     * 判断元素类型是否需要深拷贝。
     *
     * 基本类型、包装类型和 String 直接返回 false；
     * 被 @CopyTarget 标注的类型或用户自定义对象返回 true；
     * 其他情况默认返回 false。
     *
     * @param elementType 元素类型
     * @return 是否需要深拷贝
     */
    public static boolean needsDeepCopy(TypeMirror elementType) {
        if (elementType == null) {
            return false;
        }

        if (isBasicType(elementType) || isStringType(elementType)) {
            return false;
        }

        // 数组：查看元素类型是否需要深拷贝
        if (isArrayType(elementType)) {
            TypeMirror component = getArrayComponentType(elementType);
            return needsDeepCopy(component);
        }

        if (elementType.getKind() != TypeKind.DECLARED) {
            return false;
        }

        Element element = ((DeclaredType) elementType).asElement();
        if (element.getAnnotation(CopyTarget.class) != null) {
            return true;
        }

        // 非 JDK 的自定义对象视为需要深拷贝（嵌套对象）
        String typeName = elementType.toString();
        return !typeName.startsWith("java.");
    }

    /**
     * 判断是否为集合类型（List/Set/Map/数组）。
     *
     * @param type 要检查的类型
     * @return 如果是集合类型，返回 true；否则返回 false
     */
    public static boolean isCollectionType(TypeMirror type) {
        return isList(type) || isSet(type) || isMap(type) || isArrayType(type);
    }

    /**
     * 提取泛型参数列表。
     *
     * @param type 目标类型
     * @return 泛型参数列表，若无泛型参数则返回空列表
     */
    public static List<TypeMirror> extractTypeArguments(TypeMirror type) {
        List<TypeMirror> result = new ArrayList<>();
        if (type == null || type.getKind() != TypeKind.DECLARED) {
            return result;
        }
        DeclaredType declaredType = (DeclaredType) type;
        List<? extends TypeMirror> typeArguments = declaredType.getTypeArguments();
        if (typeArguments == null || typeArguments.isEmpty()) {
            return result;
        }
        for (TypeMirror arg : typeArguments) {
            TypeMirror resolved = resolveWildcard(arg);
            if (resolved != null) {
                result.add(resolved);
            }
        }
        return result;
    }

    /**
     * 提取 Map 的 Key 类型。
     *
     * @param mapType Map 类型
     * @return Key 的类型，如果无法获取则返回 null
     */
    public static TypeMirror extractMapKeyType(TypeMirror mapType) {
        List<TypeMirror> args = extractTypeArguments(mapType);
        return args.isEmpty() ? null : args.get(0);
    }

    /**
     * 提取 Map 的 Value 类型。
     *
     * @param mapType Map 类型
     * @return Value 的类型，如果无法获取则返回 null
     */
    public static TypeMirror extractMapValueType(TypeMirror mapType) {
        List<TypeMirror> args = extractTypeArguments(mapType);
        return args.size() < 2 ? null : args.get(1);
    }

    /**
     * 获取数组的元素类型（支持多维数组）。
     *
     * @param arrayType 数组类型
     * @return 元素类型，如果不是数组则返回 null
     */
    public static TypeMirror getArrayComponentType(TypeMirror arrayType) {
        if (!isArrayType(arrayType)) {
            return null;
        }
        TypeMirror component = arrayType;
        while (component.getKind() == TypeKind.ARRAY) {
            component = ((ArrayType) component).getComponentType();
        }
        return component;
    }

    /**
     * 判断声明类型是否匹配指定的前缀集合。
     *
     * @param type           要检查的类型
     * @param expectedPrefix 允许的类型前缀
     * @return 如果匹配返回 true，否则返回 false
     */
    private static boolean isDeclaredType(TypeMirror type, String... expectedPrefix) {
        if (type == null || type.getKind() != TypeKind.DECLARED) {
            return false;
        }
        String typeName = type.toString();
        for (String prefix : expectedPrefix) {
            if (typeName.startsWith(prefix)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 解析通配符类型，返回其上界或下界（优先上界）。
     *
     * @param type 可能的通配符类型
     * @return 非通配符或解析后的边界类型
     */
    private static TypeMirror resolveWildcard(TypeMirror type) {
        if (type instanceof WildcardType) {
            WildcardType wildcardType = (WildcardType) type;
            if (wildcardType.getExtendsBound() != null) {
                return wildcardType.getExtendsBound();
            }
            if (wildcardType.getSuperBound() != null) {
                return wildcardType.getSuperBound();
            }
            // 无界通配符无法提供具体类型，返回 null 让调用方退回默认处理
            return null;
        }
        return type;
    }

    /**
     * 判断是否为原始类型（raw type）。
     *
     * @param type 目标类型
     * @return 如果是原始类型返回 true，否则返回 false
     */
    public static boolean isRawType(TypeMirror type) {
        if (!(type instanceof DeclaredType)) {
            return false;
        }
        DeclaredType declaredType = (DeclaredType) type;
        return declaredType.getTypeArguments() == null || declaredType.getTypeArguments().isEmpty();
    }

    /**
     * 判断类型是否包含通配符。
     *
     * @param type 目标类型
     * @return 包含通配符返回 true，否则返回 false
     */
    public static boolean hasWildcard(TypeMirror type) {
        if (!(type instanceof DeclaredType)) {
            return false;
        }
        for (TypeMirror arg : ((DeclaredType) type).getTypeArguments()) {
            if (arg instanceof WildcardType) {
                return true;
            }
            if (hasWildcard(arg)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 判断是否包含不受支持的通配符（无界或 super 通配符）。
     *
     * @param type 目标类型
     * @return 如果存在无界或 super 通配符返回 true
     */
    public static boolean hasUnboundedWildcard(TypeMirror type) {
        if (!(type instanceof DeclaredType)) {
            return false;
        }
        for (TypeMirror arg : ((DeclaredType) type).getTypeArguments()) {
            if (arg instanceof WildcardType) {
                WildcardType wildcardType = (WildcardType) arg;
                // 无界或 super 通配符无法安全读取元素类型
                if (wildcardType.getExtendsBound() == null || wildcardType.getSuperBound() != null) {
                    return true;
                }
                if (hasUnboundedWildcard(wildcardType.getExtendsBound())) {
                    return true;
                }
            } else if (hasUnboundedWildcard(arg)) {
                return true;
            }
        }
        return false;
    }
}
