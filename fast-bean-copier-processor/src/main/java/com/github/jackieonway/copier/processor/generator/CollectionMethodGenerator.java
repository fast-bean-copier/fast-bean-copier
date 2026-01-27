package com.github.jackieonway.copier.processor.generator;

import com.github.jackieonway.copier.processor.context.ProcessorContext;
import com.squareup.javapoet.ArrayTypeName;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeVariableName;

import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import java.util.function.UnaryOperator;

/**
 * 集合方法生成器，负责生成集合转换方法。
 *
 * <p>该类负责生成：
 * <ul>
 *   <li>toDtoList / fromDtoList - List 转换方法</li>
 *   <li>toDtoSet / fromDtoSet - Set 转换方法</li>
 *   <li>toDtoMap / fromDtoMap - Map 转换方法</li>
 *   <li>toDtoArray / fromDtoArray - Array 转换方法</li>
 *   <li>带 customizer 的 List/Set 重载方法（v1.2.1）</li>
 *   <li>带 customizer 的 Map/Array 重载方法（v1.3.1 新增）</li>
 * </ul>
 *
 * <p>v1.3.1 新增功能：
 * <ul>
 *   <li>Map 批量转换方法的 UnaryOperator 重载</li>
 *   <li>Array 批量转换方法的 UnaryOperator 重载</li>
 * </ul>
 *
 * <h3>使用示例</h3>
 *
 * <p>Map UnaryOperator 重载：
 * <pre>{@code
 * // 过滤 Map 中的 null 值条目
 * Map<String, UserDto> result = UserDtoCopier.toDtoMap(sourceMap, map -> {
 *     Map<String, UserDto> filtered = new LinkedHashMap<>();
 *     for (Map.Entry<String, UserDto> entry : map.entrySet()) {
 *         if (entry.getValue() != null && entry.getValue().getId() != null) {
 *             filtered.put(entry.getKey(), entry.getValue());
 *         }
 *     }
 *     return filtered;
 * });
 *
 * // 转换为不可变 Map
 * Map<String, UserDto> immutable = UserDtoCopier.toDtoMap(
 *     sourceMap,
 *     Collections::unmodifiableMap
 * );
 * }</pre>
 *
 * <p>Array UnaryOperator 重载：
 * <pre>{@code
 * // 过滤数组中的 null 值元素
 * UserDto[] result = UserDtoCopier.toDtoArray(sourceArray, array -> {
 *     return Arrays.stream(array)
 *         .filter(dto -> dto != null && dto.getId() != null)
 *         .toArray(UserDto[]::new);
 * });
 *
 * // 排序数组
 * UserDto[] sorted = UserDtoCopier.toDtoArray(sourceArray, array -> {
 *     Arrays.sort(array, Comparator.comparing(UserDto::getId));
 *     return array;
 * });
 * }</pre>
 *
 * @author jackieonway
 * @since 1.2.1
 */
public class CollectionMethodGenerator {

    private final ProcessorContext context;
    
    /** 源类型元素 */
    private TypeElement sourceType;
    
    /** 目标类型元素 */
    private TypeElement targetType;
    
    /** 是否使用静态方法 */
    private boolean useStaticMethods = true;

    /**
     * 构造方法。
     *
     * @param context 处理器上下文
     */
    public CollectionMethodGenerator(ProcessorContext context) {
        this.context = context;
    }

    /**
     * 设置源类型。
     *
     * @param sourceType 源类型元素
     */
    public void setSourceType(TypeElement sourceType) {
        this.sourceType = sourceType;
    }

    /**
     * 设置目标类型。
     *
     * @param targetType 目标类型元素
     */
    public void setTargetType(TypeElement targetType) {
        this.targetType = targetType;
    }

    /**
     * 设置是否使用静态方法。
     *
     * @param useStaticMethods 是否使用静态方法
     */
    public void setUseStaticMethods(boolean useStaticMethods) {
        this.useStaticMethods = useStaticMethods;
    }

    // ========== List 方法 ==========

    /**
     * 生成 toDtoList 方法。
     *
     * @return 生成的方法规范
     */
    public MethodSpec generateToDtoList() {
        TypeName sourceTypeName = ClassName.get(sourceType);
        TypeName targetTypeName = ClassName.get(targetType);
        TypeName listOfSource = ParameterizedTypeName.get(ClassName.get(java.util.List.class), sourceTypeName);
        TypeName listOfTarget = ParameterizedTypeName.get(ClassName.get(java.util.List.class), targetTypeName);

        MethodSpec.Builder methodBuilder = MethodSpec.methodBuilder("toDtoList")
                .addModifiers(Modifier.PUBLIC)
                .returns(listOfTarget)
                .addParameter(listOfSource, "sources");

        if (useStaticMethods) {
            methodBuilder.addModifiers(Modifier.STATIC);
        }

        methodBuilder.beginControlFlow("if (sources == null)")
                .addStatement("return null")
                .endControlFlow();

        methodBuilder.addStatement("$T result = new $T<>(sources.size())",
                listOfTarget, ClassName.get(java.util.ArrayList.class));

        methodBuilder.beginControlFlow("for ($T source : sources)", sourceTypeName);
        if (useStaticMethods) {
            methodBuilder.addStatement("result.add(toDto(source))");
        } else {
            methodBuilder.addStatement("result.add(this.toDto(source))");
        }
        methodBuilder.endControlFlow();

        methodBuilder.addStatement("return result");

        return methodBuilder.build();
    }

    /**
     * 生成 fromDtoList 方法。
     *
     * @return 生成的方法规范
     */
    public MethodSpec generateFromDtoList() {
        TypeName sourceTypeName = ClassName.get(sourceType);
        TypeName targetTypeName = ClassName.get(targetType);
        TypeName listOfSource = ParameterizedTypeName.get(ClassName.get(java.util.List.class), sourceTypeName);
        TypeName listOfTarget = ParameterizedTypeName.get(ClassName.get(java.util.List.class), targetTypeName);

        MethodSpec.Builder methodBuilder = MethodSpec.methodBuilder("fromDtoList")
                .addModifiers(Modifier.PUBLIC)
                .returns(listOfSource)
                .addParameter(listOfTarget, "sources");

        if (useStaticMethods) {
            methodBuilder.addModifiers(Modifier.STATIC);
        }

        methodBuilder.beginControlFlow("if (sources == null)")
                .addStatement("return null")
                .endControlFlow();

        methodBuilder.addStatement("$T result = new $T<>(sources.size())",
                listOfSource, ClassName.get(java.util.ArrayList.class));

        methodBuilder.beginControlFlow("for ($T source : sources)", targetTypeName);
        if (useStaticMethods) {
            methodBuilder.addStatement("result.add(fromDto(source))");
        } else {
            methodBuilder.addStatement("result.add(this.fromDto(source))");
        }
        methodBuilder.endControlFlow();

        methodBuilder.addStatement("return result");

        return methodBuilder.build();
    }

    /**
     * 生成带 customizer 的 toDtoList 方法。
     *
     * @return 生成的方法规范
     * @since 1.2.0
     */
    public MethodSpec generateToDtoListWithCustomizer() {
        TypeName sourceTypeName = ClassName.get(sourceType);
        TypeName targetTypeName = ClassName.get(targetType);
        TypeName listOfSource = ParameterizedTypeName.get(ClassName.get(java.util.List.class), sourceTypeName);
        TypeName listOfTarget = ParameterizedTypeName.get(ClassName.get(java.util.List.class), targetTypeName);
        TypeName customizerType = ParameterizedTypeName.get(
                ClassName.get(UnaryOperator.class), targetTypeName);

        MethodSpec.Builder methodBuilder = MethodSpec.methodBuilder("toDtoList")
                .addModifiers(Modifier.PUBLIC)
                .returns(listOfTarget)
                .addParameter(listOfSource, "sources")
                .addParameter(customizerType, "customizer");

        if (useStaticMethods) {
            methodBuilder.addModifiers(Modifier.STATIC);
        }

        methodBuilder.beginControlFlow("if (sources == null)")
                .addStatement("return null")
                .endControlFlow();

        methodBuilder.addStatement("$T result = new $T<>(sources.size())",
                listOfTarget, ClassName.get(java.util.ArrayList.class));

        methodBuilder.beginControlFlow("for ($T source : sources)", sourceTypeName);
        if (useStaticMethods) {
            methodBuilder.addStatement("result.add(toDto(source, customizer))");
        } else {
            methodBuilder.addStatement("result.add(this.toDto(source, customizer))");
        }
        methodBuilder.endControlFlow();

        methodBuilder.addStatement("return result");

        return methodBuilder.build();
    }

    /**
     * 生成带 customizer 的 fromDtoList 方法。
     *
     * @return 生成的方法规范
     * @since 1.2.0
     */
    public MethodSpec generateFromDtoListWithCustomizer() {
        TypeName sourceTypeName = ClassName.get(sourceType);
        TypeName targetTypeName = ClassName.get(targetType);
        TypeName listOfSource = ParameterizedTypeName.get(ClassName.get(java.util.List.class), sourceTypeName);
        TypeName listOfTarget = ParameterizedTypeName.get(ClassName.get(java.util.List.class), targetTypeName);
        TypeName customizerType = ParameterizedTypeName.get(
                ClassName.get(UnaryOperator.class), sourceTypeName);

        MethodSpec.Builder methodBuilder = MethodSpec.methodBuilder("fromDtoList")
                .addModifiers(Modifier.PUBLIC)
                .returns(listOfSource)
                .addParameter(listOfTarget, "sources")
                .addParameter(customizerType, "customizer");

        if (useStaticMethods) {
            methodBuilder.addModifiers(Modifier.STATIC);
        }

        methodBuilder.beginControlFlow("if (sources == null)")
                .addStatement("return null")
                .endControlFlow();

        methodBuilder.addStatement("$T result = new $T<>(sources.size())",
                listOfSource, ClassName.get(java.util.ArrayList.class));

        methodBuilder.beginControlFlow("for ($T source : sources)", targetTypeName);
        if (useStaticMethods) {
            methodBuilder.addStatement("result.add(fromDto(source, customizer))");
        } else {
            methodBuilder.addStatement("result.add(this.fromDto(source, customizer))");
        }
        methodBuilder.endControlFlow();

        methodBuilder.addStatement("return result");

        return methodBuilder.build();
    }


    // ========== Set 方法 ==========

    /**
     * 生成 toDtoSet 方法。
     *
     * @return 生成的方法规范
     */
    public MethodSpec generateToDtoSet() {
        TypeName sourceTypeName = ClassName.get(sourceType);
        TypeName targetTypeName = ClassName.get(targetType);
        TypeName setOfSource = ParameterizedTypeName.get(ClassName.get(java.util.Set.class), sourceTypeName);
        TypeName setOfTarget = ParameterizedTypeName.get(ClassName.get(java.util.Set.class), targetTypeName);

        MethodSpec.Builder methodBuilder = MethodSpec.methodBuilder("toDtoSet")
                .addModifiers(Modifier.PUBLIC)
                .returns(setOfTarget)
                .addParameter(setOfSource, "sources");

        if (useStaticMethods) {
            methodBuilder.addModifiers(Modifier.STATIC);
        }

        methodBuilder.beginControlFlow("if (sources == null)")
                .addStatement("return null")
                .endControlFlow();

        methodBuilder.addStatement("$T result = new $T<>(sources.size())",
                setOfTarget, ClassName.get(java.util.LinkedHashSet.class));

        methodBuilder.beginControlFlow("for ($T source : sources)", sourceTypeName);
        if (useStaticMethods) {
            methodBuilder.addStatement("result.add(toDto(source))");
        } else {
            methodBuilder.addStatement("result.add(this.toDto(source))");
        }
        methodBuilder.endControlFlow();

        methodBuilder.addStatement("return result");

        return methodBuilder.build();
    }

    /**
     * 生成 fromDtoSet 方法。
     *
     * @return 生成的方法规范
     */
    public MethodSpec generateFromDtoSet() {
        TypeName sourceTypeName = ClassName.get(sourceType);
        TypeName targetTypeName = ClassName.get(targetType);
        TypeName setOfSource = ParameterizedTypeName.get(ClassName.get(java.util.Set.class), sourceTypeName);
        TypeName setOfTarget = ParameterizedTypeName.get(ClassName.get(java.util.Set.class), targetTypeName);

        MethodSpec.Builder methodBuilder = MethodSpec.methodBuilder("fromDtoSet")
                .addModifiers(Modifier.PUBLIC)
                .returns(setOfSource)
                .addParameter(setOfTarget, "sources");

        if (useStaticMethods) {
            methodBuilder.addModifiers(Modifier.STATIC);
        }

        methodBuilder.beginControlFlow("if (sources == null)")
                .addStatement("return null")
                .endControlFlow();

        methodBuilder.addStatement("$T result = new $T<>(sources.size())",
                setOfSource, ClassName.get(java.util.LinkedHashSet.class));

        methodBuilder.beginControlFlow("for ($T source : sources)", targetTypeName);
        if (useStaticMethods) {
            methodBuilder.addStatement("result.add(fromDto(source))");
        } else {
            methodBuilder.addStatement("result.add(this.fromDto(source))");
        }
        methodBuilder.endControlFlow();

        methodBuilder.addStatement("return result");

        return methodBuilder.build();
    }

    /**
     * 生成带 customizer 的 toDtoSet 方法。
     *
     * @return 生成的方法规范
     * @since 1.2.0
     */
    public MethodSpec generateToDtoSetWithCustomizer() {
        TypeName sourceTypeName = ClassName.get(sourceType);
        TypeName targetTypeName = ClassName.get(targetType);
        TypeName setOfSource = ParameterizedTypeName.get(ClassName.get(java.util.Set.class), sourceTypeName);
        TypeName setOfTarget = ParameterizedTypeName.get(ClassName.get(java.util.Set.class), targetTypeName);
        TypeName customizerType = ParameterizedTypeName.get(
                ClassName.get(UnaryOperator.class), targetTypeName);

        MethodSpec.Builder methodBuilder = MethodSpec.methodBuilder("toDtoSet")
                .addModifiers(Modifier.PUBLIC)
                .returns(setOfTarget)
                .addParameter(setOfSource, "sources")
                .addParameter(customizerType, "customizer");

        if (useStaticMethods) {
            methodBuilder.addModifiers(Modifier.STATIC);
        }

        methodBuilder.beginControlFlow("if (sources == null)")
                .addStatement("return null")
                .endControlFlow();

        methodBuilder.addStatement("$T result = new $T<>(sources.size())",
                setOfTarget, ClassName.get(java.util.LinkedHashSet.class));

        methodBuilder.beginControlFlow("for ($T source : sources)", sourceTypeName);
        if (useStaticMethods) {
            methodBuilder.addStatement("result.add(toDto(source, customizer))");
        } else {
            methodBuilder.addStatement("result.add(this.toDto(source, customizer))");
        }
        methodBuilder.endControlFlow();

        methodBuilder.addStatement("return result");

        return methodBuilder.build();
    }

    /**
     * 生成带 customizer 的 fromDtoSet 方法。
     *
     * @return 生成的方法规范
     * @since 1.2.0
     */
    public MethodSpec generateFromDtoSetWithCustomizer() {
        TypeName sourceTypeName = ClassName.get(sourceType);
        TypeName targetTypeName = ClassName.get(targetType);
        TypeName setOfSource = ParameterizedTypeName.get(ClassName.get(java.util.Set.class), sourceTypeName);
        TypeName setOfTarget = ParameterizedTypeName.get(ClassName.get(java.util.Set.class), targetTypeName);
        TypeName customizerType = ParameterizedTypeName.get(
                ClassName.get(UnaryOperator.class), sourceTypeName);

        MethodSpec.Builder methodBuilder = MethodSpec.methodBuilder("fromDtoSet")
                .addModifiers(Modifier.PUBLIC)
                .returns(setOfSource)
                .addParameter(setOfTarget, "sources")
                .addParameter(customizerType, "customizer");

        if (useStaticMethods) {
            methodBuilder.addModifiers(Modifier.STATIC);
        }

        methodBuilder.beginControlFlow("if (sources == null)")
                .addStatement("return null")
                .endControlFlow();

        methodBuilder.addStatement("$T result = new $T<>(sources.size())",
                setOfSource, ClassName.get(java.util.LinkedHashSet.class));

        methodBuilder.beginControlFlow("for ($T source : sources)", targetTypeName);
        if (useStaticMethods) {
            methodBuilder.addStatement("result.add(fromDto(source, customizer))");
        } else {
            methodBuilder.addStatement("result.add(this.fromDto(source, customizer))");
        }
        methodBuilder.endControlFlow();

        methodBuilder.addStatement("return result");

        return methodBuilder.build();
    }

    // ========== Map 方法 ==========

    /**
     * 生成 toDtoMap 方法。
     *
     * @return 生成的方法规范
     */
    public MethodSpec generateToDtoMap() {
        TypeVariableName keyType = TypeVariableName.get("K");
        TypeName sourceTypeName = ClassName.get(sourceType);
        TypeName targetTypeName = ClassName.get(targetType);
        TypeName mapOfSource = ParameterizedTypeName.get(ClassName.get(java.util.Map.class), keyType, sourceTypeName);
        TypeName mapOfTarget = ParameterizedTypeName.get(ClassName.get(java.util.Map.class), keyType, targetTypeName);
        TypeName mapImpl = ParameterizedTypeName.get(ClassName.get(java.util.LinkedHashMap.class), keyType, targetTypeName);
        TypeName entryType = ParameterizedTypeName.get(ClassName.get(java.util.Map.Entry.class), keyType, sourceTypeName);

        MethodSpec.Builder methodBuilder = MethodSpec.methodBuilder("toDtoMap")
                .addModifiers(Modifier.PUBLIC)
                .addTypeVariable(keyType)
                .returns(mapOfTarget)
                .addParameter(mapOfSource, "sources");

        if (useStaticMethods) {
            methodBuilder.addModifiers(Modifier.STATIC);
        }

        methodBuilder.beginControlFlow("if (sources == null)")
                .addStatement("return null")
                .endControlFlow();

        methodBuilder.addStatement("$T result = new $T($L)", mapOfTarget, mapImpl, buildInitialCapacity("sources.size()"))
                .beginControlFlow("for ($T entry : sources.entrySet())", entryType)
                .addStatement("$T key = entry.getKey()", keyType)
                .beginControlFlow("if (entry.getValue() != null)");

        if (useStaticMethods) {
            methodBuilder.addStatement("result.put(key, toDto(entry.getValue()))");
        } else {
            methodBuilder.addStatement("result.put(key, this.toDto(entry.getValue()))");
        }

        methodBuilder.nextControlFlow("else")
                .addStatement("result.put(key, null)")
                .endControlFlow()
                .endControlFlow()
                .addStatement("return result");

        return methodBuilder.build();
    }

    /**
     * 生成 fromDtoMap 方法。
     *
     * @return 生成的方法规范
     */
    public MethodSpec generateFromDtoMap() {
        TypeVariableName keyType = TypeVariableName.get("K");
        TypeName sourceTypeName = ClassName.get(sourceType);
        TypeName targetTypeName = ClassName.get(targetType);
        TypeName mapOfSource = ParameterizedTypeName.get(ClassName.get(java.util.Map.class), keyType, sourceTypeName);
        TypeName mapOfTarget = ParameterizedTypeName.get(ClassName.get(java.util.Map.class), keyType, targetTypeName);
        TypeName mapImpl = ParameterizedTypeName.get(ClassName.get(java.util.LinkedHashMap.class), keyType, sourceTypeName);
        TypeName entryType = ParameterizedTypeName.get(ClassName.get(java.util.Map.Entry.class), keyType, targetTypeName);

        MethodSpec.Builder methodBuilder = MethodSpec.methodBuilder("fromDtoMap")
                .addModifiers(Modifier.PUBLIC)
                .addTypeVariable(keyType)
                .returns(mapOfSource)
                .addParameter(mapOfTarget, "sources");

        if (useStaticMethods) {
            methodBuilder.addModifiers(Modifier.STATIC);
        }

        methodBuilder.beginControlFlow("if (sources == null)")
                .addStatement("return null")
                .endControlFlow();

        methodBuilder.addStatement("$T result = new $T($L)", mapOfSource, mapImpl, buildInitialCapacity("sources.size()"))
                .beginControlFlow("for ($T entry : sources.entrySet())", entryType)
                .addStatement("$T key = entry.getKey()", keyType)
                .beginControlFlow("if (entry.getValue() != null)");

        if (useStaticMethods) {
            methodBuilder.addStatement("result.put(key, fromDto(entry.getValue()))");
        } else {
            methodBuilder.addStatement("result.put(key, this.fromDto(entry.getValue()))");
        }

        methodBuilder.nextControlFlow("else")
                .addStatement("result.put(key, null)")
                .endControlFlow()
                .endControlFlow()
                .addStatement("return result");

        return methodBuilder.build();
    }

    // ========== Array 方法 ==========

    /**
     * 生成 toDtoArray 方法。
     *
     * @return 生成的方法规范
     */
    public MethodSpec generateToDtoArray() {
        TypeName sourceArrayType = ArrayTypeName.of(ClassName.get(sourceType));
        TypeName targetArrayType = ArrayTypeName.of(ClassName.get(targetType));
        TypeName sourceElementType = ClassName.get(sourceType);

        MethodSpec.Builder methodBuilder = MethodSpec.methodBuilder("toDtoArray")
                .addModifiers(Modifier.PUBLIC)
                .returns(targetArrayType)
                .addParameter(sourceArrayType, "sources");

        if (useStaticMethods) {
            methodBuilder.addModifiers(Modifier.STATIC);
        }

        methodBuilder.beginControlFlow("if (sources == null)")
                .addStatement("return null")
                .endControlFlow();

        methodBuilder.addStatement("$T result = new $T[sources.length]", targetArrayType, ClassName.get(targetType))
                .beginControlFlow("for (int i = 0; i < sources.length; i++)")
                .addStatement("$T element = sources[i]", sourceElementType)
                .beginControlFlow("if (element != null)");

        if (useStaticMethods) {
            methodBuilder.addStatement("result[i] = toDto(element)");
        } else {
            methodBuilder.addStatement("result[i] = this.toDto(element)");
        }

        methodBuilder.nextControlFlow("else")
                .addStatement("result[i] = null")
                .endControlFlow()
                .endControlFlow()
                .addStatement("return result");

        return methodBuilder.build();
    }

    /**
     * 生成 fromDtoArray 方法。
     *
     * @return 生成的方法规范
     */
    public MethodSpec generateFromDtoArray() {
        TypeName sourceArrayType = ArrayTypeName.of(ClassName.get(sourceType));
        TypeName targetArrayType = ArrayTypeName.of(ClassName.get(targetType));
        TypeName targetElementType = ClassName.get(targetType);

        MethodSpec.Builder methodBuilder = MethodSpec.methodBuilder("fromDtoArray")
                .addModifiers(Modifier.PUBLIC)
                .returns(sourceArrayType)
                .addParameter(targetArrayType, "sources");

        if (useStaticMethods) {
            methodBuilder.addModifiers(Modifier.STATIC);
        }

        methodBuilder.beginControlFlow("if (sources == null)")
                .addStatement("return null")
                .endControlFlow();

        methodBuilder.addStatement("$T result = new $T[sources.length]", sourceArrayType, ClassName.get(sourceType))
                .beginControlFlow("for (int i = 0; i < sources.length; i++)")
                .addStatement("$T element = sources[i]", targetElementType)
                .beginControlFlow("if (element != null)");

        if (useStaticMethods) {
            methodBuilder.addStatement("result[i] = fromDto(element)");
        } else {
            methodBuilder.addStatement("result[i] = this.fromDto(element)");
        }

        methodBuilder.nextControlFlow("else")
                .addStatement("result[i] = null")
                .endControlFlow()
                .endControlFlow()
                .addStatement("return result");

        return methodBuilder.build();
    }

    // ========== v1.3.1 新增：Map UnaryOperator 重载方法 ==========

    /**
     * 生成带 customizer 的 toDtoMap 方法。
     *
     * @return 生成的方法规范
     * @since 1.3.1
     */
    public MethodSpec generateToDtoMapWithCustomizer() {
        TypeVariableName keyType = TypeVariableName.get("K");
        TypeName sourceTypeName = ClassName.get(sourceType);
        TypeName targetTypeName = ClassName.get(targetType);
        TypeName mapOfSource = ParameterizedTypeName.get(ClassName.get(java.util.Map.class), keyType, sourceTypeName);
        TypeName mapOfTarget = ParameterizedTypeName.get(ClassName.get(java.util.Map.class), keyType, targetTypeName);
        TypeName customizerType = ParameterizedTypeName.get(
                ClassName.get(UnaryOperator.class), mapOfTarget);

        MethodSpec.Builder methodBuilder = MethodSpec.methodBuilder("toDtoMap")
                .addModifiers(Modifier.PUBLIC)
                .addTypeVariable(keyType)
                .returns(mapOfTarget)
                .addParameter(mapOfSource, "sources")
                .addParameter(customizerType, "customizer");

        if (useStaticMethods) {
            methodBuilder.addModifiers(Modifier.STATIC);
        }

        methodBuilder.beginControlFlow("if (sources == null)")
                .addStatement("return null")
                .endControlFlow();

        if (useStaticMethods) {
            methodBuilder.addStatement("$T result = toDtoMap(sources)", mapOfTarget);
        } else {
            methodBuilder.addStatement("$T result = this.toDtoMap(sources)", mapOfTarget);
        }

        methodBuilder.beginControlFlow("if (result != null && customizer != null)")
                .addStatement("result = customizer.apply(result)")
                .endControlFlow()
                .addStatement("return result");

        return methodBuilder.build();
    }

    /**
     * 生成带 customizer 的 fromDtoMap 方法。
     *
     * @return 生成的方法规范
     * @since 1.3.1
     */
    public MethodSpec generateFromDtoMapWithCustomizer() {
        TypeVariableName keyType = TypeVariableName.get("K");
        TypeName sourceTypeName = ClassName.get(sourceType);
        TypeName targetTypeName = ClassName.get(targetType);
        TypeName mapOfSource = ParameterizedTypeName.get(ClassName.get(java.util.Map.class), keyType, sourceTypeName);
        TypeName mapOfTarget = ParameterizedTypeName.get(ClassName.get(java.util.Map.class), keyType, targetTypeName);
        TypeName customizerType = ParameterizedTypeName.get(
                ClassName.get(UnaryOperator.class), mapOfSource);

        MethodSpec.Builder methodBuilder = MethodSpec.methodBuilder("fromDtoMap")
                .addModifiers(Modifier.PUBLIC)
                .addTypeVariable(keyType)
                .returns(mapOfSource)
                .addParameter(mapOfTarget, "sources")
                .addParameter(customizerType, "customizer");

        if (useStaticMethods) {
            methodBuilder.addModifiers(Modifier.STATIC);
        }

        methodBuilder.beginControlFlow("if (sources == null)")
                .addStatement("return null")
                .endControlFlow();

        if (useStaticMethods) {
            methodBuilder.addStatement("$T result = fromDtoMap(sources)", mapOfSource);
        } else {
            methodBuilder.addStatement("$T result = this.fromDtoMap(sources)", mapOfSource);
        }

        methodBuilder.beginControlFlow("if (result != null && customizer != null)")
                .addStatement("result = customizer.apply(result)")
                .endControlFlow()
                .addStatement("return result");

        return methodBuilder.build();
    }

    // ========== v1.3.1 新增：Array UnaryOperator 重载方法 ==========

    /**
     * 生成带 customizer 的 toDtoArray 方法。
     *
     * @return 生成的方法规范
     * @since 1.3.1
     */
    public MethodSpec generateToDtoArrayWithCustomizer() {
        TypeName sourceArrayType = ArrayTypeName.of(ClassName.get(sourceType));
        TypeName targetArrayType = ArrayTypeName.of(ClassName.get(targetType));
        TypeName customizerType = ParameterizedTypeName.get(
                ClassName.get(UnaryOperator.class), targetArrayType);

        MethodSpec.Builder methodBuilder = MethodSpec.methodBuilder("toDtoArray")
                .addModifiers(Modifier.PUBLIC)
                .returns(targetArrayType)
                .addParameter(sourceArrayType, "sources")
                .addParameter(customizerType, "customizer");

        if (useStaticMethods) {
            methodBuilder.addModifiers(Modifier.STATIC);
        }

        methodBuilder.beginControlFlow("if (sources == null)")
                .addStatement("return null")
                .endControlFlow();

        if (useStaticMethods) {
            methodBuilder.addStatement("$T result = toDtoArray(sources)", targetArrayType);
        } else {
            methodBuilder.addStatement("$T result = this.toDtoArray(sources)", targetArrayType);
        }

        methodBuilder.beginControlFlow("if (result != null && customizer != null)")
                .addStatement("result = customizer.apply(result)")
                .endControlFlow()
                .addStatement("return result");

        return methodBuilder.build();
    }

    /**
     * 生成带 customizer 的 fromDtoArray 方法。
     *
     * @return 生成的方法规范
     * @since 1.3.1
     */
    public MethodSpec generateFromDtoArrayWithCustomizer() {
        TypeName sourceArrayType = ArrayTypeName.of(ClassName.get(sourceType));
        TypeName targetArrayType = ArrayTypeName.of(ClassName.get(targetType));
        TypeName customizerType = ParameterizedTypeName.get(
                ClassName.get(UnaryOperator.class), sourceArrayType);

        MethodSpec.Builder methodBuilder = MethodSpec.methodBuilder("fromDtoArray")
                .addModifiers(Modifier.PUBLIC)
                .returns(sourceArrayType)
                .addParameter(targetArrayType, "sources")
                .addParameter(customizerType, "customizer");

        if (useStaticMethods) {
            methodBuilder.addModifiers(Modifier.STATIC);
        }

        methodBuilder.beginControlFlow("if (sources == null)")
                .addStatement("return null")
                .endControlFlow();

        if (useStaticMethods) {
            methodBuilder.addStatement("$T result = fromDtoArray(sources)", sourceArrayType);
        } else {
            methodBuilder.addStatement("$T result = this.fromDtoArray(sources)", sourceArrayType);
        }

        methodBuilder.beginControlFlow("if (result != null && customizer != null)")
                .addStatement("result = customizer.apply(result)")
                .endControlFlow()
                .addStatement("return result");

        return methodBuilder.build();
    }

    // ========== 辅助方法 ==========

    /**
     * 计算集合或 Map 的初始容量。
     *
     * @param sizeExpression 大小表达式
     * @return 初始容量表达式
     */
    private String buildInitialCapacity(String sizeExpression) {
        return "Math.max((int)(" + sizeExpression + " / 0.75f) + 1, 16)";
    }
}
