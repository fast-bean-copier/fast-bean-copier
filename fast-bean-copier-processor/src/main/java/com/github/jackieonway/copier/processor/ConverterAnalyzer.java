package com.github.jackieonway.copier.processor;

import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import java.util.ArrayList;
import java.util.List;

/**
 * 转换器分析器，用于分析自定义转换器类。
 *
 * <p>分析 {@link com.github.jackieonway.copier.annotation.CopyTarget#uses()} 中指定的转换器类，
 * 提取其中的公共方法，用于字段转换。
 *
 * @author jackieonway
 * @since 1.2.0
 */
public class ConverterAnalyzer {

    private final Elements elementUtils;
    private final Types typeUtils;

    /**
     * 转换方法信息。
     */
    public static class ConverterMethod {
        private final String methodName;
        private final TypeMirror returnType;
        private final List<TypeMirror> parameterTypes;
        private final ExecutableElement element;

        /**
         * 创建转换方法信息。
         *
         * @param methodName 方法名
         * @param returnType 返回类型
         * @param parameterTypes 参数类型列表
         * @param element 方法元素
         */
        public ConverterMethod(String methodName, TypeMirror returnType, 
                               List<TypeMirror> parameterTypes, ExecutableElement element) {
            this.methodName = methodName;
            this.returnType = returnType;
            this.parameterTypes = parameterTypes;
            this.element = element;
        }

        /**
         * 获取方法名。
         *
         * @return 方法名
         */
        public String getMethodName() {
            return methodName;
        }

        /**
         * 获取返回类型。
         *
         * @return 返回类型
         */
        public TypeMirror getReturnType() {
            return returnType;
        }

        /**
         * 获取参数类型列表。
         *
         * @return 参数类型列表
         */
        public List<TypeMirror> getParameterTypes() {
            return parameterTypes;
        }

        /**
         * 获取方法元素。
         *
         * @return 方法元素
         */
        public ExecutableElement getElement() {
            return element;
        }

        /**
         * 检查方法是否匹配指定的参数类型和返回类型。
         *
         * @param sourceType 源类型
         * @param targetType 目标类型
         * @param typeUtils 类型工具
         * @return 如果匹配返回 true
         */
        public boolean matches(TypeMirror sourceType, TypeMirror targetType, Types typeUtils) {
            // 检查返回类型
            if (!typeUtils.isAssignable(returnType, targetType)) {
                return false;
            }
            
            // 检查参数类型（至少有一个参数）
            if (parameterTypes.isEmpty()) {
                return false;
            }
            
            // 第一个参数应该与源类型兼容
            return typeUtils.isAssignable(sourceType, parameterTypes.get(0));
        }
    }

    /**
     * 创建转换器分析器。
     *
     * @param elementUtils 元素工具
     * @param typeUtils 类型工具
     */
    public ConverterAnalyzer(Elements elementUtils, Types typeUtils) {
        this.elementUtils = elementUtils;
        this.typeUtils = typeUtils;
    }

    /**
     * 分析转换器类，提取所有公共方法。
     *
     * @param converterType 转换器类型
     * @return 转换方法列表
     */
    public List<ConverterMethod> analyzeConverter(TypeMirror converterType) {
        List<ConverterMethod> methods = new ArrayList<>();
        
        TypeElement typeElement = (TypeElement) typeUtils.asElement(converterType);
        if (typeElement == null) {
            return methods;
        }
        
        for (Element enclosed : typeElement.getEnclosedElements()) {
            if (enclosed instanceof ExecutableElement) {
                ExecutableElement method = (ExecutableElement) enclosed;
                
                // 只处理公共非静态方法
                if (method.getModifiers().contains(Modifier.PUBLIC) 
                        && !method.getModifiers().contains(Modifier.STATIC)
                        && !method.getSimpleName().toString().equals("<init>")) {
                    
                    List<TypeMirror> paramTypes = new ArrayList<>();
                    for (javax.lang.model.element.VariableElement param : method.getParameters()) {
                        paramTypes.add(param.asType());
                    }
                    
                    methods.add(new ConverterMethod(
                            method.getSimpleName().toString(),
                            method.getReturnType(),
                            paramTypes,
                            method
                    ));
                }
            }
        }
        
        return methods;
    }

    /**
     * 根据方法名查找转换方法。
     *
     * @param converterType 转换器类型
     * @param methodName 方法名
     * @return 匹配的方法，如果找不到返回 null
     */
    public ConverterMethod findMethodByName(TypeMirror converterType, String methodName) {
        List<ConverterMethod> methods = analyzeConverter(converterType);
        for (ConverterMethod method : methods) {
            if (method.getMethodName().equals(methodName)) {
                return method;
            }
        }
        return null;
    }

    /**
     * 根据类型匹配查找转换方法。
     *
     * @param converterType 转换器类型
     * @param sourceType 源类型
     * @param targetType 目标类型
     * @return 匹配的方法，如果找不到返回 null
     */
    public ConverterMethod findMethodByType(TypeMirror converterType, 
                                            TypeMirror sourceType, TypeMirror targetType) {
        List<ConverterMethod> methods = analyzeConverter(converterType);
        for (ConverterMethod method : methods) {
            if (method.matches(sourceType, targetType, typeUtils)) {
                return method;
            }
        }
        return null;
    }

    /**
     * 根据方法名和类型匹配查找转换方法。
     *
     * @param converterType 转换器类型
     * @param methodName 方法名
     * @param sourceType 源类型
     * @param targetType 目标类型
     * @return 匹配的方法，如果找不到返回 null
     */
    public ConverterMethod findMethod(TypeMirror converterType, String methodName,
                                      TypeMirror sourceType, TypeMirror targetType) {
        List<ConverterMethod> methods = analyzeConverter(converterType);
        for (ConverterMethod method : methods) {
            if (method.getMethodName().equals(methodName) 
                    && method.matches(sourceType, targetType, typeUtils)) {
                return method;
            }
        }
        return null;
    }
}
