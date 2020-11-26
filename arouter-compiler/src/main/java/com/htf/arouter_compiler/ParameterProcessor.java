package com.htf.arouter_compiler;

import com.google.auto.service.AutoService;
import com.htf.arouter_annotation.Parameter;
import com.htf.arouter_compiler.factory.ParameterFactory;
import com.htf.arouter_compiler.util.ProcessorConfig;
import com.htf.arouter_compiler.util.ProcessorUtils;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.Name;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.TypeParameterElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.ArrayType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import javax.tools.Diagnostic;

@AutoService(Processor.class)
@SupportedSourceVersion(SourceVersion.RELEASE_8)
@SupportedAnnotationTypes(ProcessorConfig.PARAMETER_PACKAGE)
public class ParameterProcessor extends AbstractProcessor {

    private Elements elementTool;

    private Filer filer;

    private Messager messager;

    private Types typeTool;


    private Map<TypeElement, List<Element>> mParameterMap = new HashMap<>();

    @Override
    public synchronized void init(ProcessingEnvironment processingEnvironment) {
        super.init(processingEnvironment);
        elementTool = processingEnvironment.getElementUtils();
        filer = processingEnvironment.getFiler();
        messager = processingEnvironment.getMessager();
        typeTool = processingEnvironment.getTypeUtils();
    }

    @Override
    public boolean process(Set<? extends TypeElement> set, RoundEnvironment roundEnvironment) {
        if (!ProcessorUtils.isEmpty(set)) {
            Set<? extends Element> elements = roundEnvironment.getElementsAnnotatedWith(Parameter.class);
            if (!elements.isEmpty()) {//注解式作用在变量上，所以集合里存放的是声明的变量
                for (Element element : elements) {
                    TypeElement enclosingElement = (TypeElement) element.getEnclosingElement();//获取父节点 MainActivity
                    if (mParameterMap.containsKey(enclosingElement)) {
                        mParameterMap.get(enclosingElement).add(element);
                    } else {
                        List<Element> fieldsList = new ArrayList<>();
                        fieldsList.add(element);
                        mParameterMap.put(enclosingElement, fieldsList);
                    }
                }
                if (mParameterMap.isEmpty()) return true;
                TypeElement activityType = elementTool.getTypeElement(ProcessorConfig.ACTIVITY_PACKAGE);
                TypeElement parameterType = elementTool.getTypeElement(ProcessorConfig.AROUTER_AIP_PARAMETER_DATA);//com.htf.arouter_api.ParameterData
                /**
                 * 生成方法
                 * @Override
                 * public void getParameter(Object targetParameter) {
                 */
                ParameterSpec parameterSpec = ParameterSpec.builder(TypeName.OBJECT, ProcessorConfig.PARAMETER_NAME).build();
                /**
                 * 方法里的内容
                 */
                for (Map.Entry<TypeElement, List<Element>> entry : mParameterMap.entrySet()) {
                    TypeElement typeElement = entry.getKey();
                    //先判断是不是在Activity
                    if (!typeTool.isSubtype(typeElement.asType(), activityType.asType())) {
                        throw new RuntimeException("@Parameter注解目前仅限用于Activity类之上");
                    }

                    ClassName className = ClassName.get(typeElement);
                    MethodSpec.Builder methodBuilder = MethodSpec.methodBuilder(ProcessorConfig.PARAMETER_METHOD_NAME)
                            .addModifiers(Modifier.PUBLIC)
                            .addAnnotation(Override.class)
                            .addParameter(parameterSpec);
                    ParameterFactory parameterFactory = new ParameterFactory.Builder(methodBuilder)
                            .setClassName(className)
                            .setMessager(messager)
                            .setElementTool(elementTool)
                            .setTypeTool(typeTool)
                            .build();

                    parameterFactory.addFirstStatement();

                    for (Element element : entry.getValue()) {
                        parameterFactory.buildStatement(element);
                    }

                    /**
                     * 生成文件
                     */
                    String finalClassName = typeElement.getSimpleName().toString() + ProcessorConfig.PARAMETER_FILE_NAME;
                    try {
                        JavaFile.builder(className.packageName(),
                                TypeSpec.classBuilder(finalClassName)
                                        .addSuperinterface(ClassName.get(parameterType))
                                        .addModifiers(Modifier.PUBLIC)
                                        .addMethod(parameterFactory.build())
                                        .build())
                                .build()
                                .writeTo(filer);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        return false;
    }
}
