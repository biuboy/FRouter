package com.htf.arouter_compiler.factory;

import com.htf.arouter_annotation.Parameter;
import com.htf.arouter_compiler.util.ProcessorConfig;
import com.htf.arouter_compiler.util.ProcessorUtils;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.TypeSpec;

import java.io.IOException;

import javax.annotation.processing.Messager;
import javax.lang.model.element.Element;
import javax.lang.model.element.Modifier;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import javax.tools.Diagnostic;

public class ParameterFactory {

    // 方法的构建
    private MethodSpec.Builder methodBuilder;

    private Messager messager;

    private ClassName className;// 类名，如：MainActivity

    private Types typeTool;

    private Elements elementTool; // 类信息

    private ParameterFactory(Builder builder) {
        this.messager = builder.messager;
        this.className = builder.className;
        this.typeTool = builder.typeTool;
        this.elementTool = builder.elementTool;
        this.methodBuilder = builder.methodBuilder;
    }

    public void addFirstStatement() {
        methodBuilder.addStatement("$T t = ($T)" + ProcessorConfig.PARAMETER_NAME, className, className);
    }

    public void buildStatement(Element element) {
        TypeMirror parcelableType = elementTool.getTypeElement(ProcessorConfig.PARCELABLE).asType();//  //判断是不是对象bean类型
        TypeMirror typeMirror = element.asType();//遍历注解的属性节点， 生成函数体
        int type = typeMirror.getKind().ordinal();//获取TypeKind枚举的序列号
        String fieldName = element.getSimpleName().toString();//获取属性名字 numberId 、name等
        String annotationValue = element.getAnnotation(Parameter.class).name();//获取注解值
        annotationValue = ProcessorUtils.isEmpty(annotationValue) ? fieldName : annotationValue;
        String finalValue = "t." + fieldName;
        String methodContent = finalValue + " = t.getIntent().";
        // TypeKind 枚举类型不包含String
        if (type == TypeKind.INT.ordinal()) {
            methodContent += "getIntExtra($S, " + finalValue + ")";  // Int有默认值
        } else if (type == TypeKind.DOUBLE.ordinal()) {
            methodContent += "getDoubleExtra($S, " + finalValue + ")";  // Double有默认值
        } else if (type == TypeKind.BOOLEAN.ordinal()) {
            methodContent += "getBooleanExtra($S, " + finalValue + ")";  // Boolean有默认值
        } else if (typeTool.isSubtype(typeMirror, parcelableType)) {
            methodContent += "getParcelableExtra($S)"; // 没有默认值
        } else if (typeMirror.toString().equals(ProcessorConfig.STRING)) {
            methodContent += "getStringExtra($S)"; // 没有默认值
        } else if (typeMirror.toString().equals(ProcessorConfig.LIST_STRING)) {
            methodContent += "getStringArrayListExtra($S)"; // 没有默认值
        } else {
            messager.printMessage(Diagnostic.Kind.ERROR, "目前暂支持String、int、boolean、List<String>、对象bean传参");
        }
        methodBuilder.addStatement(methodContent, annotationValue);
    }

    public MethodSpec build() {
        return methodBuilder.build();
    }

    /**
     * Builder构建者设计模式
     */
    public static class Builder {

        private Messager messager;

        private MethodSpec.Builder methodBuilder;

        private ClassName className;

        private Types typeTool;

        private Elements elementTool;

        public Builder(MethodSpec.Builder methodBuilder) {
            this.methodBuilder = methodBuilder;
        }

        public Builder setElementTool(Elements elementTool) {
            this.elementTool = elementTool;
            return this;
        }

        public Builder setTypeTool(Types typeTool) {
            this.typeTool = typeTool;
            return this;
        }

        public Builder setMessager(Messager messager) {
            this.messager = messager;
            return this;
        }

        public Builder setClassName(ClassName className) {
            this.className = className;
            return this;
        }

        public ParameterFactory build() {
            if (methodBuilder == null) {
                throw new IllegalArgumentException("方法为空");
            }
            if (messager == null) {
                throw new IllegalArgumentException("messager为空，Messager用来报告错误、警告和其他提示信息");
            }
            return new ParameterFactory(this);
        }
    }
}
