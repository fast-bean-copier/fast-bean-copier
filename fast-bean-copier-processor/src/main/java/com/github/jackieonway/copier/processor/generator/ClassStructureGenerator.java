package com.github.jackieonway.copier.processor.generator;

import com.github.jackieonway.copier.annotation.ComponentModel;
import com.github.jackieonway.copier.processor.context.ProcessorContext;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeSpec;

import javax.lang.model.element.Modifier;
import javax.lang.model.type.TypeMirror;
import java.util.List;
import java.util.Set;

/**
 * 类结构生成器，负责生成 Copier 类的结构部分。
 *
 * <p>该类负责生成：
 * <ul>
 *   <li>类注解（根据 componentModel 添加 @Component/@ApplicationScoped/@Named 等）</li>
 *   <li>转换器字段声明</li>
 *   <li>uses 类字段声明</li>
 *   <li>构造器（私有构造器或依赖注入构造器）</li>
 * </ul>
 *
 * @author jackieonway
 * @since 1.2.1
 */
public class ClassStructureGenerator {

    /**
     * 处理器上下文。
     */
    private final ProcessorContext context;

    /**
     * 构造方法。
     *
     * @param context 处理器上下文
     */
    public ClassStructureGenerator(ProcessorContext context) {
        this.context = context;
    }

    /**
     * 根据 ComponentModel 添加类注解和修饰符。
     *
     * @param classBuilder 类构建器
     */
    public void addClassAnnotations(TypeSpec.Builder classBuilder) {
        ComponentModel componentModel = context.getComponentModel();
        switch (componentModel) {
            case SPRING:
                classBuilder.addAnnotation(ClassName.get("org.springframework.stereotype", "Component"));
                break;
            case CDI:
                classBuilder.addAnnotation(ClassName.get("javax.enterprise.context", "ApplicationScoped"));
                break;
            case JSR330:
                classBuilder.addAnnotation(ClassName.get("javax.inject", "Named"));
                classBuilder.addAnnotation(ClassName.get("javax.inject", "Singleton"));
                break;
            case DEFAULT:
            default:
                classBuilder.addModifiers(Modifier.FINAL);
                break;
        }
    }

    /**
     * 添加转换器字段。
     *
     * @param classBuilder 类构建器
     * @param converterClassNames 转换器类名集合
     */
    public void addConverterFields(TypeSpec.Builder classBuilder, Set<String> converterClassNames) {
        ComponentModel componentModel = context.getComponentModel();
        for (String converterClassName : converterClassNames) {
            ClassName converterType = ClassName.bestGuess(converterClassName);
            String fieldName = getConverterFieldName(converterClassName);

            if (componentModel == ComponentModel.DEFAULT) {
                // 静态字段
                classBuilder.addField(FieldSpec.builder(converterType, fieldName)
                        .addModifiers(Modifier.PRIVATE, Modifier.STATIC, Modifier.FINAL)
                        .initializer("new $T()", converterType)
                        .build());
            } else {
                // 实例字段
                classBuilder.addField(FieldSpec.builder(converterType, fieldName)
                        .addModifiers(Modifier.PRIVATE, Modifier.FINAL)
                        .build());
            }
        }
    }

    /**
     * 添加 uses 类字段。
     *
     * @param classBuilder 类构建器
     * @param usesClasses uses 类的 TypeMirror 列表
     */
    public void addUsesFields(TypeSpec.Builder classBuilder, List<TypeMirror> usesClasses) {
        ComponentModel componentModel = context.getComponentModel();
        for (TypeMirror usesClass : usesClasses) {
            ClassName usesType = ClassName.bestGuess(usesClass.toString());
            String fieldName = getUsesFieldName(usesClass.toString());

            if (componentModel == ComponentModel.DEFAULT) {
                // 静态字段
                classBuilder.addField(FieldSpec.builder(usesType, fieldName)
                        .addModifiers(Modifier.PRIVATE, Modifier.STATIC, Modifier.FINAL)
                        .initializer("new $T()", usesType)
                        .build());
            } else {
                // 实例字段
                classBuilder.addField(FieldSpec.builder(usesType, fieldName)
                        .addModifiers(Modifier.PRIVATE, Modifier.FINAL)
                        .build());
            }
        }
    }

    /**
     * 添加构造器。
     *
     * @param classBuilder 类构建器
     * @param converterClassNames 转换器类名集合
     * @param usesClasses uses 类的 TypeMirror 列表
     */
    public void addConstructors(TypeSpec.Builder classBuilder, Set<String> converterClassNames,
                                 List<TypeMirror> usesClasses) {
        ComponentModel componentModel = context.getComponentModel();
        if (componentModel == ComponentModel.DEFAULT) {
            // 私有构造器，防止实例化
            classBuilder.addMethod(createPrivateConstructor());
        } else {
            // 无参构造器（使用默认实例）
            MethodSpec.Builder noArgConstructor = MethodSpec.constructorBuilder()
                    .addModifiers(Modifier.PUBLIC);

            // 初始化转换器字段
            for (String converterClassName : converterClassNames) {
                ClassName converterType = ClassName.bestGuess(converterClassName);
                String fieldName = getConverterFieldName(converterClassName);
                noArgConstructor.addStatement("this.$L = new $T()", fieldName, converterType);
            }

            // 初始化 uses 字段
            for (TypeMirror usesClass : usesClasses) {
                ClassName usesType = ClassName.bestGuess(usesClass.toString());
                String fieldName = getUsesFieldName(usesClass.toString());
                noArgConstructor.addStatement("this.$L = new $T()", fieldName, usesType);
            }

            classBuilder.addMethod(noArgConstructor.build());

            // 如果有需要注入的依赖，添加带参数的构造器
            if (!converterClassNames.isEmpty() || !usesClasses.isEmpty()) {
                addInjectionConstructor(classBuilder, converterClassNames, usesClasses);
            }
        }
    }


    /**
     * 添加依赖注入构造器。
     *
     * @param classBuilder 类构建器
     * @param converterClassNames 转换器类名集合
     * @param usesClasses uses 类的 TypeMirror 列表
     */
    private void addInjectionConstructor(TypeSpec.Builder classBuilder, Set<String> converterClassNames,
                                          List<TypeMirror> usesClasses) {
        ComponentModel componentModel = context.getComponentModel();
        MethodSpec.Builder injectionConstructor = MethodSpec.constructorBuilder()
                .addModifiers(Modifier.PUBLIC);

        // 添加注入注解
        if (componentModel == ComponentModel.SPRING) {
            injectionConstructor.addAnnotation(
                    ClassName.get("org.springframework.beans.factory.annotation", "Autowired"));
        } else {
            injectionConstructor.addAnnotation(ClassName.get("javax.inject", "Inject"));
        }

        // 添加转换器参数
        for (String converterClassName : converterClassNames) {
            ClassName converterType = ClassName.bestGuess(converterClassName);
            String fieldName = getConverterFieldName(converterClassName);
            injectionConstructor.addParameter(converterType, fieldName);
            injectionConstructor.addStatement("this.$L = $L", fieldName, fieldName);
        }

        // 添加 uses 参数
        for (TypeMirror usesClass : usesClasses) {
            ClassName usesType = ClassName.bestGuess(usesClass.toString());
            String fieldName = getUsesFieldName(usesClass.toString());
            injectionConstructor.addParameter(usesType, fieldName);
            injectionConstructor.addStatement("this.$L = $L", fieldName, fieldName);
        }

        classBuilder.addMethod(injectionConstructor.build());
    }

    /**
     * 创建私有构造方法，防止实例化。
     *
     * @return 私有构造方法
     */
    private MethodSpec createPrivateConstructor() {
        String copierClassName = context.getTargetTypeName() + "Copier";
        return MethodSpec.constructorBuilder()
                .addModifiers(Modifier.PRIVATE)
                .addStatement("throw new $T($S)", AssertionError.class, "No instances of " + copierClassName)
                .build();
    }

    /**
     * 获取转换器字段名。
     *
     * <p>将类名转换为字段名，首字母小写。
     * 例如：DateConverter -> dateConverter
     *
     * @param converterClassName 转换器类的完全限定名
     * @return 字段名
     */
    public String getConverterFieldName(String converterClassName) {
        String simpleName = converterClassName.substring(converterClassName.lastIndexOf('.') + 1);
        return Character.toLowerCase(simpleName.charAt(0)) + simpleName.substring(1);
    }

    /**
     * 获取 uses 字段名。
     *
     * <p>将类名转换为字段名，首字母小写。
     * 例如：StringUtils -> stringUtils
     *
     * @param usesClassName uses 类的完全限定名
     * @return 字段名
     */
    public String getUsesFieldName(String usesClassName) {
        String simpleName = usesClassName.substring(usesClassName.lastIndexOf('.') + 1);
        return Character.toLowerCase(simpleName.charAt(0)) + simpleName.substring(1);
    }
}
