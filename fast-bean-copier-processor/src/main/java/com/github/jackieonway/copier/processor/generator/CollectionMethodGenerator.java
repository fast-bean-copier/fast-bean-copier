package com.github.jackieonway.copier.processor.generator;

import com.github.jackieonway.copier.annotation.CycleDetectionStrategy;
import com.github.jackieonway.copier.processor.context.ProcessorContext;
import com.squareup.javapoet.ArrayTypeName;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeVariableName;

import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import java.util.IdentityHashMap;
import java.util.function.BiFunction;
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
 *   <li>带 customizer 的 List/Set/Map/Array 重载方法（v1.2.1 新增 List/Set，v1.3.1 新增 Map/Array 并统一 List/Set 行为）</li>
 * </ul>
 *
 * <p>v1.3.1 重要变更：
 * <ul>
 *   <li>统一所有集合方法的 customizer 行为：对整个集合应用而非单个元素</li>
 *   <li>List/Set customizer 从 {@code UnaryOperator<Element>} 改为 {@code UnaryOperator<Collection>}</li>
 *   <li>新增 Map 批量转换方法的 {@code UnaryOperator<Map>} 重载</li>
 *   <li>新增 Array 批量转换方法的 {@code UnaryOperator<Array>} 重载</li>
 * </ul>
 *
 * <h3>使用示例</h3>
 *
 * <p>List UnaryOperator 重载（v1.3.1 统一行为）：
 * <pre>{@code
 * // 过滤列表
 * List<UserDto> result = UserDtoCopier.toDtoList(sourceList, list -> 
 *     list.stream()
 *         .filter(dto -> dto.getAge() > 18)
 *         .collect(Collectors.toList())
 * );
 *
 * // 排序列表
 * List<UserDto> sorted = UserDtoCopier.toDtoList(sourceList, list -> {
 *     list.sort(Comparator.comparing(UserDto::getName));
 *     return list;
 * });
 * }</pre>
 *
 * <p>Set UnaryOperator 重载（v1.3.1 统一行为）：
 * <pre>{@code
 * // 去重并转换为不可变 Set
 * Set<UserDto> immutable = UserDtoCopier.toDtoSet(
 *     sourceSet,
 *     Collections::unmodifiableSet
 * );
 * }</pre>
 *
 * <p>Map UnaryOperator 重载（v1.3.1 新增）：
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
 * }</pre>
 *
 * <p>Array UnaryOperator 重载（v1.3.1 新增）：
 * <pre>{@code
 * // 过滤数组中的 null 值元素
 * UserDto[] result = UserDtoCopier.toDtoArray(sourceArray, array -> {
 *     return Arrays.stream(array)
 *         .filter(dto -> dto != null && dto.getId() != null)
 *         .toArray(UserDto[]::new);
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
        addBatchCycleContext(methodBuilder);

        methodBuilder.beginControlFlow("for ($T source : sources)", sourceTypeName);
        if (isRuntimeCycleStrategy()) {
            methodBuilder.addStatement("result.add(toDto(source, __cache))");
        } else if (useStaticMethods) {
            methodBuilder.addStatement("result.add(toDto(source))");
        } else {
            methodBuilder.addStatement("result.add(this.toDto(source))");
        }
        methodBuilder.endControlFlow();

        methodBuilder.addStatement("return result");

        return methodBuilder.build();
    }

    // ========== v1.4.0 新增：Set/Map/Array processors 重载方法 ==========

    public MethodSpec generateToDtoSetWithProcessors() {
        TypeName sourceTypeName = ClassName.get(sourceType);
        TypeName targetTypeName = ClassName.get(targetType);
        TypeName setOfSource = ParameterizedTypeName.get(ClassName.get(java.util.Set.class), sourceTypeName);
        TypeName setOfTarget = ParameterizedTypeName.get(ClassName.get(java.util.Set.class), targetTypeName);
        TypeName preProcessorType = ParameterizedTypeName.get(
                ClassName.get(UnaryOperator.class), setOfSource);
        TypeName postProcessorType = ParameterizedTypeName.get(
                ClassName.get(BiFunction.class), setOfSource, setOfTarget, setOfTarget);

        MethodSpec.Builder methodBuilder = MethodSpec.methodBuilder("toDtoSet")
                .addModifiers(Modifier.PUBLIC)
                .returns(setOfTarget)
                .addParameter(setOfSource, "sources")
                .addParameter(preProcessorType, "preProcessor")
                .addParameter(postProcessorType, "postProcessor");

        if (useStaticMethods) {
            methodBuilder.addModifiers(Modifier.STATIC);
        }

        methodBuilder.beginControlFlow("if (sources == null)")
                .addStatement("return null")
                .endControlFlow();

        methodBuilder.beginControlFlow("if (preProcessor != null)")
                .addStatement("sources = preProcessor.apply(sources)")
                .endControlFlow();

        methodBuilder.beginControlFlow("if (sources == null)")
                .addStatement("return null")
                .endControlFlow();

        if (useStaticMethods) {
            methodBuilder.addStatement("$T result = toDtoSet(sources)", setOfTarget);
        } else {
            methodBuilder.addStatement("$T result = this.toDtoSet(sources)", setOfTarget);
        }

        methodBuilder.beginControlFlow("if (result != null && postProcessor != null)")
                .addStatement("result = postProcessor.apply(sources, result)")
                .endControlFlow()
                .addStatement("return result");

        return methodBuilder.build();
    }

    public MethodSpec generateFromDtoSetWithProcessors() {
        TypeName sourceTypeName = ClassName.get(sourceType);
        TypeName targetTypeName = ClassName.get(targetType);
        TypeName setOfSource = ParameterizedTypeName.get(ClassName.get(java.util.Set.class), sourceTypeName);
        TypeName setOfTarget = ParameterizedTypeName.get(ClassName.get(java.util.Set.class), targetTypeName);
        TypeName preProcessorType = ParameterizedTypeName.get(
                ClassName.get(UnaryOperator.class), setOfTarget);
        TypeName postProcessorType = ParameterizedTypeName.get(
                ClassName.get(BiFunction.class), setOfTarget, setOfSource, setOfSource);

        MethodSpec.Builder methodBuilder = MethodSpec.methodBuilder("fromDtoSet")
                .addModifiers(Modifier.PUBLIC)
                .returns(setOfSource)
                .addParameter(setOfTarget, "sources")
                .addParameter(preProcessorType, "preProcessor")
                .addParameter(postProcessorType, "postProcessor");

        if (useStaticMethods) {
            methodBuilder.addModifiers(Modifier.STATIC);
        }

        methodBuilder.beginControlFlow("if (sources == null)")
                .addStatement("return null")
                .endControlFlow();

        methodBuilder.beginControlFlow("if (preProcessor != null)")
                .addStatement("sources = preProcessor.apply(sources)")
                .endControlFlow();

        methodBuilder.beginControlFlow("if (sources == null)")
                .addStatement("return null")
                .endControlFlow();

        if (useStaticMethods) {
            methodBuilder.addStatement("$T result = fromDtoSet(sources)", setOfSource);
        } else {
            methodBuilder.addStatement("$T result = this.fromDtoSet(sources)", setOfSource);
        }

        methodBuilder.beginControlFlow("if (result != null && postProcessor != null)")
                .addStatement("result = postProcessor.apply(sources, result)")
                .endControlFlow()
                .addStatement("return result");

        return methodBuilder.build();
    }

    public MethodSpec generateToDtoMapWithProcessors() {
        TypeVariableName keyType = TypeVariableName.get("K");
        TypeName sourceTypeName = ClassName.get(sourceType);
        TypeName targetTypeName = ClassName.get(targetType);
        TypeName mapOfSource = ParameterizedTypeName.get(ClassName.get(java.util.Map.class), keyType, sourceTypeName);
        TypeName mapOfTarget = ParameterizedTypeName.get(ClassName.get(java.util.Map.class), keyType, targetTypeName);
        TypeName preProcessorType = ParameterizedTypeName.get(
                ClassName.get(UnaryOperator.class), mapOfSource);
        TypeName postProcessorType = ParameterizedTypeName.get(
                ClassName.get(BiFunction.class), mapOfSource, mapOfTarget, mapOfTarget);

        MethodSpec.Builder methodBuilder = MethodSpec.methodBuilder("toDtoMap")
                .addModifiers(Modifier.PUBLIC)
                .addTypeVariable(keyType)
                .returns(mapOfTarget)
                .addParameter(mapOfSource, "sources")
                .addParameter(preProcessorType, "preProcessor")
                .addParameter(postProcessorType, "postProcessor");

        if (useStaticMethods) {
            methodBuilder.addModifiers(Modifier.STATIC);
        }

        methodBuilder.beginControlFlow("if (sources == null)")
                .addStatement("return null")
                .endControlFlow();

        methodBuilder.beginControlFlow("if (preProcessor != null)")
                .addStatement("sources = preProcessor.apply(sources)")
                .endControlFlow();

        methodBuilder.beginControlFlow("if (sources == null)")
                .addStatement("return null")
                .endControlFlow();

        if (useStaticMethods) {
            methodBuilder.addStatement("$T result = toDtoMap(sources)", mapOfTarget);
        } else {
            methodBuilder.addStatement("$T result = this.toDtoMap(sources)", mapOfTarget);
        }

        methodBuilder.beginControlFlow("if (result != null && postProcessor != null)")
                .addStatement("result = postProcessor.apply(sources, result)")
                .endControlFlow()
                .addStatement("return result");

        return methodBuilder.build();
    }

    public MethodSpec generateFromDtoMapWithProcessors() {
        TypeVariableName keyType = TypeVariableName.get("K");
        TypeName sourceTypeName = ClassName.get(sourceType);
        TypeName targetTypeName = ClassName.get(targetType);
        TypeName mapOfSource = ParameterizedTypeName.get(ClassName.get(java.util.Map.class), keyType, sourceTypeName);
        TypeName mapOfTarget = ParameterizedTypeName.get(ClassName.get(java.util.Map.class), keyType, targetTypeName);
        TypeName preProcessorType = ParameterizedTypeName.get(
                ClassName.get(UnaryOperator.class), mapOfTarget);
        TypeName postProcessorType = ParameterizedTypeName.get(
                ClassName.get(BiFunction.class), mapOfTarget, mapOfSource, mapOfSource);

        MethodSpec.Builder methodBuilder = MethodSpec.methodBuilder("fromDtoMap")
                .addModifiers(Modifier.PUBLIC)
                .addTypeVariable(keyType)
                .returns(mapOfSource)
                .addParameter(mapOfTarget, "sources")
                .addParameter(preProcessorType, "preProcessor")
                .addParameter(postProcessorType, "postProcessor");

        if (useStaticMethods) {
            methodBuilder.addModifiers(Modifier.STATIC);
        }

        methodBuilder.beginControlFlow("if (sources == null)")
                .addStatement("return null")
                .endControlFlow();

        methodBuilder.beginControlFlow("if (preProcessor != null)")
                .addStatement("sources = preProcessor.apply(sources)")
                .endControlFlow();

        methodBuilder.beginControlFlow("if (sources == null)")
                .addStatement("return null")
                .endControlFlow();

        if (useStaticMethods) {
            methodBuilder.addStatement("$T result = fromDtoMap(sources)", mapOfSource);
        } else {
            methodBuilder.addStatement("$T result = this.fromDtoMap(sources)", mapOfSource);
        }

        methodBuilder.beginControlFlow("if (result != null && postProcessor != null)")
                .addStatement("result = postProcessor.apply(sources, result)")
                .endControlFlow()
                .addStatement("return result");

        return methodBuilder.build();
    }

    public MethodSpec generateToDtoArrayWithProcessors() {
        TypeName sourceArrayType = ArrayTypeName.of(ClassName.get(sourceType));
        TypeName targetArrayType = ArrayTypeName.of(ClassName.get(targetType));
        TypeName preProcessorType = ParameterizedTypeName.get(
                ClassName.get(UnaryOperator.class), sourceArrayType);
        TypeName postProcessorType = ParameterizedTypeName.get(
                ClassName.get(BiFunction.class), sourceArrayType, targetArrayType, targetArrayType);

        MethodSpec.Builder methodBuilder = MethodSpec.methodBuilder("toDtoArray")
                .addModifiers(Modifier.PUBLIC)
                .returns(targetArrayType)
                .addParameter(sourceArrayType, "sources")
                .addParameter(preProcessorType, "preProcessor")
                .addParameter(postProcessorType, "postProcessor");

        if (useStaticMethods) {
            methodBuilder.addModifiers(Modifier.STATIC);
        }

        methodBuilder.beginControlFlow("if (sources == null)")
                .addStatement("return null")
                .endControlFlow();

        methodBuilder.beginControlFlow("if (preProcessor != null)")
                .addStatement("sources = preProcessor.apply(sources)")
                .endControlFlow();

        methodBuilder.beginControlFlow("if (sources == null)")
                .addStatement("return null")
                .endControlFlow();

        if (useStaticMethods) {
            methodBuilder.addStatement("$T result = toDtoArray(sources)", targetArrayType);
        } else {
            methodBuilder.addStatement("$T result = this.toDtoArray(sources)", targetArrayType);
        }

        methodBuilder.beginControlFlow("if (result != null && postProcessor != null)")
                .addStatement("result = postProcessor.apply(sources, result)")
                .endControlFlow()
                .addStatement("return result");

        return methodBuilder.build();
    }

    public MethodSpec generateFromDtoArrayWithProcessors() {
        TypeName sourceArrayType = ArrayTypeName.of(ClassName.get(sourceType));
        TypeName targetArrayType = ArrayTypeName.of(ClassName.get(targetType));
        TypeName preProcessorType = ParameterizedTypeName.get(
                ClassName.get(UnaryOperator.class), targetArrayType);
        TypeName postProcessorType = ParameterizedTypeName.get(
                ClassName.get(BiFunction.class), targetArrayType, sourceArrayType, sourceArrayType);

        MethodSpec.Builder methodBuilder = MethodSpec.methodBuilder("fromDtoArray")
                .addModifiers(Modifier.PUBLIC)
                .returns(sourceArrayType)
                .addParameter(targetArrayType, "sources")
                .addParameter(preProcessorType, "preProcessor")
                .addParameter(postProcessorType, "postProcessor");

        if (useStaticMethods) {
            methodBuilder.addModifiers(Modifier.STATIC);
        }

        methodBuilder.beginControlFlow("if (sources == null)")
                .addStatement("return null")
                .endControlFlow();

        methodBuilder.beginControlFlow("if (preProcessor != null)")
                .addStatement("sources = preProcessor.apply(sources)")
                .endControlFlow();

        methodBuilder.beginControlFlow("if (sources == null)")
                .addStatement("return null")
                .endControlFlow();

        if (useStaticMethods) {
            methodBuilder.addStatement("$T result = fromDtoArray(sources)", sourceArrayType);
        } else {
            methodBuilder.addStatement("$T result = this.fromDtoArray(sources)", sourceArrayType);
        }

        methodBuilder.beginControlFlow("if (result != null && postProcessor != null)")
                .addStatement("result = postProcessor.apply(sources, result)")
                .endControlFlow()
                .addStatement("return result");

        return methodBuilder.build();
    }

    public MethodSpec generateFromDtoListWithProcessors() {
        TypeName sourceTypeName = ClassName.get(sourceType);
        TypeName targetTypeName = ClassName.get(targetType);
        TypeName listOfSource = ParameterizedTypeName.get(ClassName.get(java.util.List.class), sourceTypeName);
        TypeName listOfTarget = ParameterizedTypeName.get(ClassName.get(java.util.List.class), targetTypeName);
        TypeName preProcessorType = ParameterizedTypeName.get(
                ClassName.get(UnaryOperator.class), listOfTarget);
        TypeName postProcessorType = ParameterizedTypeName.get(
                ClassName.get(BiFunction.class), listOfTarget, listOfSource, listOfSource);

        MethodSpec.Builder methodBuilder = MethodSpec.methodBuilder("fromDtoList")
                .addModifiers(Modifier.PUBLIC)
                .returns(listOfSource)
                .addParameter(listOfTarget, "sources")
                .addParameter(preProcessorType, "preProcessor")
                .addParameter(postProcessorType, "postProcessor");

        if (useStaticMethods) {
            methodBuilder.addModifiers(Modifier.STATIC);
        }

        methodBuilder.beginControlFlow("if (sources == null)")
                .addStatement("return null")
                .endControlFlow();

        methodBuilder.beginControlFlow("if (preProcessor != null)")
                .addStatement("sources = preProcessor.apply(sources)")
                .endControlFlow();

        methodBuilder.beginControlFlow("if (sources == null)")
                .addStatement("return null")
                .endControlFlow();

        if (useStaticMethods) {
            methodBuilder.addStatement("$T result = fromDtoList(sources)", listOfSource);
        } else {
            methodBuilder.addStatement("$T result = this.fromDtoList(sources)", listOfSource);
        }

        methodBuilder.beginControlFlow("if (result != null && postProcessor != null)")
                .addStatement("result = postProcessor.apply(sources, result)")
                .endControlFlow()
                .addStatement("return result");

        return methodBuilder.build();
    }

    public MethodSpec generateToDtoListWithProcessors() {
        TypeName sourceTypeName = ClassName.get(sourceType);
        TypeName targetTypeName = ClassName.get(targetType);
        TypeName listOfSource = ParameterizedTypeName.get(ClassName.get(java.util.List.class), sourceTypeName);
        TypeName listOfTarget = ParameterizedTypeName.get(ClassName.get(java.util.List.class), targetTypeName);
        TypeName preProcessorType = ParameterizedTypeName.get(
                ClassName.get(UnaryOperator.class), listOfSource);
        TypeName postProcessorType = ParameterizedTypeName.get(
                ClassName.get(BiFunction.class), listOfSource, listOfTarget, listOfTarget);

        MethodSpec.Builder methodBuilder = MethodSpec.methodBuilder("toDtoList")
                .addModifiers(Modifier.PUBLIC)
                .returns(listOfTarget)
                .addParameter(listOfSource, "sources")
                .addParameter(preProcessorType, "preProcessor")
                .addParameter(postProcessorType, "postProcessor");

        if (useStaticMethods) {
            methodBuilder.addModifiers(Modifier.STATIC);
        }

        methodBuilder.beginControlFlow("if (sources == null)")
                .addStatement("return null")
                .endControlFlow();

        methodBuilder.beginControlFlow("if (preProcessor != null)")
                .addStatement("sources = preProcessor.apply(sources)")
                .endControlFlow();

        methodBuilder.beginControlFlow("if (sources == null)")
                .addStatement("return null")
                .endControlFlow();

        if (useStaticMethods) {
            methodBuilder.addStatement("$T result = toDtoList(sources)", listOfTarget);
        } else {
            methodBuilder.addStatement("$T result = this.toDtoList(sources)", listOfTarget);
        }

        methodBuilder.beginControlFlow("if (result != null && postProcessor != null)")
                .addStatement("result = postProcessor.apply(sources, result)")
                .endControlFlow()
                .addStatement("return result");

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
        addBatchCycleContext(methodBuilder);

        methodBuilder.beginControlFlow("for ($T source : sources)", targetTypeName);
        if (isRuntimeCycleStrategy()) {
            methodBuilder.addStatement("result.add(fromDto(source, __cache))");
        } else if (useStaticMethods) {
            methodBuilder.addStatement("result.add(fromDto(source))");
        } else {
            methodBuilder.addStatement("result.add(this.fromDto(source))");
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
        addBatchCycleContext(methodBuilder);

        methodBuilder.beginControlFlow("for ($T source : sources)", sourceTypeName);
        if (isRuntimeCycleStrategy()) {
            methodBuilder.addStatement("result.add(toDto(source, __cache))");
        } else if (useStaticMethods) {
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
        addBatchCycleContext(methodBuilder);

        methodBuilder.beginControlFlow("for ($T source : sources)", targetTypeName);
        if (isRuntimeCycleStrategy()) {
            methodBuilder.addStatement("result.add(fromDto(source, __cache))");
        } else if (useStaticMethods) {
            methodBuilder.addStatement("result.add(fromDto(source))");
        } else {
            methodBuilder.addStatement("result.add(this.fromDto(source))");
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

        methodBuilder.addStatement("$T result = new $T($L)", mapOfTarget, mapImpl, buildInitialCapacity("sources.size()"));
        addBatchCycleContext(methodBuilder);
        methodBuilder
                .beginControlFlow("for ($T entry : sources.entrySet())", entryType)
                .addStatement("$T key = entry.getKey()", keyType)
                .beginControlFlow("if (entry.getValue() != null)");

        if (isRuntimeCycleStrategy()) {
            methodBuilder.addStatement("result.put(key, toDto(entry.getValue(), __cache))");
        } else if (useStaticMethods) {
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

        methodBuilder.addStatement("$T result = new $T($L)", mapOfSource, mapImpl, buildInitialCapacity("sources.size()"));
        addBatchCycleContext(methodBuilder);
        methodBuilder
                .beginControlFlow("for ($T entry : sources.entrySet())", entryType)
                .addStatement("$T key = entry.getKey()", keyType)
                .beginControlFlow("if (entry.getValue() != null)");

        if (isRuntimeCycleStrategy()) {
            methodBuilder.addStatement("result.put(key, fromDto(entry.getValue(), __cache))");
        } else if (useStaticMethods) {
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

        methodBuilder.addStatement("$T result = new $T[sources.length]", targetArrayType, ClassName.get(targetType));
        addBatchCycleContext(methodBuilder);
        methodBuilder
                .beginControlFlow("for (int i = 0; i < sources.length; i++)")
                .addStatement("$T element = sources[i]", sourceElementType)
                .beginControlFlow("if (element != null)");

        if (isRuntimeCycleStrategy()) {
            methodBuilder.addStatement("result[i] = toDto(element, __cache)");
        } else if (useStaticMethods) {
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

        methodBuilder.addStatement("$T result = new $T[sources.length]", sourceArrayType, ClassName.get(sourceType));
        addBatchCycleContext(methodBuilder);
        methodBuilder
                .beginControlFlow("for (int i = 0; i < sources.length; i++)")
                .addStatement("$T element = sources[i]", targetElementType)
                .beginControlFlow("if (element != null)");

        if (isRuntimeCycleStrategy()) {
            methodBuilder.addStatement("result[i] = fromDto(element, __cache)");
        } else if (useStaticMethods) {
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

    private boolean isRuntimeCycleStrategy() {
        CycleDetectionStrategy strategy = context.getCycleDetectionStrategy();
        return strategy == CycleDetectionStrategy.RETURN_NULL
                || strategy == CycleDetectionStrategy.AUTOMATIC_CACHE;
    }

    private void addBatchCycleContext(MethodSpec.Builder methodBuilder) {
        if (isRuntimeCycleStrategy()) {
            methodBuilder.addStatement("$T<$T, $T> __cache = new $T<>()",
                    IdentityHashMap.class, Object.class, Object.class, IdentityHashMap.class);
        }
    }
}
