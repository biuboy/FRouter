package com.htf.arouter_compiler;

import com.google.auto.service.AutoService;
import com.htf.arouter_annotation.ARouter;
import com.htf.arouter_annotation.bean.RouterBean;
import com.htf.arouter_compiler.util.ProcessorConfig;
import com.htf.arouter_compiler.util.ProcessorUtils;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;
import com.squareup.javapoet.WildcardTypeName;

import java.io.IOException;
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
import javax.annotation.processing.SupportedOptions;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import javax.tools.Diagnostic;

@AutoService(Processor.class)
@SupportedAnnotationTypes(ProcessorConfig.AROUTER_PACKAGE)
@SupportedSourceVersion(SourceVersion.RELEASE_8)
@SupportedOptions({ProcessorConfig.OPTIONS, ProcessorConfig.APT_PACKAGE})
public class ARouterProcessor extends AbstractProcessor {

    private Elements elementTool;// 操作Element的工具类（类，函数，属性，其实都是Element）


    private Types typeTool;//// type(类信息)的工具类，包含用于操作TypeMirror的工具方法

    private Messager messager;

    private Filer filer;

    private String options; // 各个模块传递过来的模块名 例如：app user zixun order等

    private String aptPackage; // 各个模块传递过来的目录 用于统一存放 apt生成的文件

    // Map<"user", List<RouterBean>>   Map<"app", List<RouterBean>>
    private Map<String, List<RouterBean>> mAllPathMap = new HashMap<>();

    // Map<"user", "ARouter$$Path$$user.class">
    private Map<String, String> mAllGroupMap = new HashMap<>();

    @Override
    public synchronized void init(ProcessingEnvironment processingEnvironment) {
        super.init(processingEnvironment);
        elementTool = processingEnvironment.getElementUtils();
        messager = processingEnvironment.getMessager();
        filer = processingEnvironment.getFiler();
        typeTool = processingEnvironment.getTypeUtils();
        // 只有接受到 module 传递过来的数据
        options = processingEnvironment.getOptions().get(ProcessorConfig.OPTIONS);
        aptPackage = processingEnvironment.getOptions().get(ProcessorConfig.APT_PACKAGE);

        if (options != null && aptPackage != null) {
            messager.printMessage(Diagnostic.Kind.NOTE, "APT环境搭建完成");
        } else {
            messager.printMessage(Diagnostic.Kind.NOTE, "APT环境有问题，请检查 options 与 aptPackage 为null...");
        }
    }

    @Override
    public boolean process(Set<? extends TypeElement> set, RoundEnvironment roundEnvironment) {
        if (!set.isEmpty()) {
            // 获取所有被 @ARouter 注解的 元素集合
            Set<? extends Element> elements = roundEnvironment.getElementsAnnotatedWith(ARouter.class);
            // 通过Element工具类，获取Activity，Callback类型
            TypeElement activityType = elementTool.getTypeElement(ProcessorConfig.ACTIVITY_PACKAGE);
            TypeMirror activityMirror = activityType.asType();// 显示类信息（获取被注解的节点，类节点）这也叫自描述 Mirror
            for (Element element : elements) {
                //获取类节点，获取包节点(com.htf.xxx)
                // String packageName = elementTool.getPackageOf(element).getQualifiedName().toString();
                String className = element.getSimpleName().toString();//获取类名
                ARouter aRouter = element.getAnnotation(ARouter.class);

                // TODO  一系列的检查工作
                // 在循环里面，对 “路由对象” 进行封装
                RouterBean routerBean = new RouterBean.Builder()
                        .addGroup(aRouter.group())
                        .addPath(aRouter.path())
                        .addElement(element)
                        .build();

                // ARouter注解的类 必须继承 Activity
                TypeMirror elementMirror = element.asType();
                if (typeTool.isSubtype(elementMirror, activityMirror)) {//表明是Activity
                    routerBean.setTypeEnum(RouterBean.TypeEnum.ACTIVITY);
                } else {
                    throw new RuntimeException("@ARouter注解只能用于Activity类之上" + className + "没有继承Activity");
                }

                if (checkRouterPath(routerBean)) {//判断是否已经存在加入到map中
                    List<RouterBean> routerBeanList = mAllPathMap.get(routerBean.getGroup());
                    if (ProcessorUtils.isEmpty(routerBeanList)) {
                        routerBeanList = new ArrayList<>();
                        routerBeanList.add(routerBean);
                        mAllPathMap.put(routerBean.getGroup(), routerBeanList);
                    } else {
                        routerBeanList.add(routerBean);
                    }
                } else {
                    messager.printMessage(Diagnostic.Kind.ERROR, "@ARouter注解未按规范配置，如：/app/MainActivity");
                }
            } // TODO end for  所有Path值已存到mAllPathMap中

            // TODO 定义javapoet生成文件的接口
            TypeElement pathType = elementTool.getTypeElement(ProcessorConfig.AROUTER_API_PATH);
            TypeElement groupType = elementTool.getTypeElement(ProcessorConfig.AROUTER_API_GROUP);
            try {
                createPathFile(pathType);
            } catch (IOException e) {
                e.printStackTrace();
                messager.printMessage(Diagnostic.Kind.NOTE, "在生成PATH时，发生异常:" + e.getMessage());
            }

            try {
                createGroupFile(groupType, pathType);
            } catch (IOException e) {
                e.printStackTrace();
                messager.printMessage(Diagnostic.Kind.NOTE, "在生成group模板时发生了异常 e:" + e.getMessage());
            }
        }
        return false;
    }

    //生成路由组Group文件，如：ARouter$$Group$$app
    private void createGroupFile(TypeElement groupType, TypeElement pathType) throws IOException {
        if (ProcessorUtils.isEmpty(mAllGroupMap) || ProcessorUtils.isEmpty(mAllPathMap)) return;
        //要生成的内容
        /* public class ARouter$$Group$$order implements ARouterGroup {

         *//**
         * @return key：组名 如：“user”  ---- ARouter$$Path$$user--(包含了很多的myClass)
         *//*
            @Override
            public Map<String, Class<? extends ARouterPath>> getGroupMap() {
                Map<String, Class<? extends ARouterPath>> groupMap = new HashMap<>();
                groupMap.put("user", ARouter$$Path$$user.class);       // 寻找Path
                groupMap.put("zixun", ARouter$$Path$$zixun.class); // 寻找Path
                return groupMap;
            }
        }*/

        //Map<String, Class<? extends ARouterPath>>
        TypeName methodReturn = ParameterizedTypeName.get(
                ClassName.get(Map.class),
                ClassName.get(String.class),
                //Class<? extends ARouterPath>>
                ParameterizedTypeName.get(ClassName.get(Class.class),
                        WildcardTypeName.subtypeOf(ClassName.get(pathType))) // ? extends ARouterLoadPath)
        );

        MethodSpec.Builder methodBuilder = MethodSpec.methodBuilder(ProcessorConfig.GROUP_METHOD_NAME)
                .addAnnotation(Override.class)
                .addModifiers(Modifier.PUBLIC)
                .returns(methodReturn);


        //方法里的内容
        // Map<String, Class<? extends ARouterPath>> groupMap = new HashMap<>();
        methodBuilder.addStatement("$T<$T, $T> $N = new $T<>()",
                ClassName.get(Map.class),
                ClassName.get(String.class),
                //Class<? extends ARouterPath>
                ParameterizedTypeName.get(ClassName.get(Class.class),
                        WildcardTypeName.subtypeOf(ClassName.get(pathType))), // ? extends ARouterPath
                ProcessorConfig.GROUP_VAR1,
                ClassName.get(HashMap.class));

        //  groupMap.put("app", ARouter$$Path$$app.class);
        //	groupMap.put("user", ARouter$$Path$$user.class);
        for (Map.Entry<String, String> entry : mAllGroupMap.entrySet()) {
            methodBuilder.addStatement("$N.put($S,$T.class)",
                    ProcessorConfig.GROUP_VAR1,
                    entry.getKey(),
                    ClassName.get(aptPackage, entry.getValue()));
        }
        methodBuilder.addStatement("return $N", ProcessorConfig.GROUP_VAR1);

        ///生成文件
        String fileClassName = ProcessorConfig.GROUP_FILE_NAME + options;
        JavaFile.builder(aptPackage,
                TypeSpec.classBuilder(fileClassName)
        .addSuperinterface(ClassName.get(groupType))
        .addModifiers(Modifier.PUBLIC)
        .addMethod(methodBuilder.build())
        .build())
        .build()
        .writeTo(filer);

    }

    //生成文件实现ARouterPath
    private void createPathFile(TypeElement pathType) throws IOException {
        if (ProcessorUtils.isEmpty(mAllPathMap)) {
            return;
        }
        // 生成Map<String, RouterBean>
        TypeName methodReturn = ParameterizedTypeName.get(
                ClassName.get(Map.class),
                ClassName.get(String.class),
                ClassName.get(RouterBean.class)
        );

        //遍历APP、zixun、user、module组件中带注解的文件
        for (Map.Entry<String, List<RouterBean>> entry : mAllPathMap.entrySet()) {
            //方法
            MethodSpec.Builder methodBuilder = MethodSpec.methodBuilder(ProcessorConfig.PATH_METHOD_NAME)
                    .addAnnotation(Override.class)
                    .addModifiers(Modifier.PUBLIC)
                    .returns(methodReturn);//把Map<String, RouterBean> 返回类型

            //方法里的内容 Map<String, RouterBean> pathMap = new HashMap<>();
            methodBuilder.addStatement("$T<$T, $T> $N = new $T<>()",
                    ClassName.get(Map.class),
                    ClassName.get(String.class),
                    ClassName.get(RouterBean.class),
                    ProcessorConfig.PATH_VAR1,
                    ClassName.get(HashMap.class));

            /**
             * pathMap.put("/user/user_LoginActivity",
             * RouterBean.create(RouterBean.TypeEnum.ACTIVITY, Order_MainActivity.class, "/user/user_LoginActivity","user"));
             */
            List<RouterBean> pathList = entry.getValue();
            for (RouterBean routerBean : pathList) {
                methodBuilder.addStatement("$N.put($S, $T.create($T.$L,$T.class,$S,$S))",
                        ProcessorConfig.PATH_VAR1,
                        routerBean.getPath(),
                        ClassName.get(RouterBean.class),
                        ClassName.get(RouterBean.TypeEnum.class), // RouterBean.Type
                        routerBean.getTypeEnum(),// 枚举类型：ACTIVITY
                        ClassName.get((TypeElement) routerBean.getElement()), // user_LoginActivity.class
                        routerBean.getPath(), // 路径名
                        routerBean.getGroup() // 组名
                );
            }
            // return pathMap;
            methodBuilder.addStatement("return $N", ProcessorConfig.PATH_VAR1);

            //生成的类文件名ARouter$$Path$$user    有implements所以方法和类要合为一体生成
            String fileName = ProcessorConfig.PATH_FILE_NAME + entry.getKey();
            messager.printMessage(Diagnostic.Kind.NOTE, "APT生成路由Path类文件：" +
                    aptPackage + "." + fileName);

            JavaFile.builder(aptPackage,
                    TypeSpec.classBuilder(fileName)
                            .addSuperinterface(ClassName.get(pathType))
                            .addModifiers(Modifier.PUBLIC)
                            .addMethod(methodBuilder.build())
                            .build())
                    .build()
                    .writeTo(filer);
            mAllGroupMap.put(entry.getKey(), fileName);
        }
    }

    private boolean checkRouterPath(RouterBean bean) {
        String group = bean.getGroup();//app、user、zixun等module
        String path = bean.getPath();//   /app/MainActivity

        // TODO 校验path
        // @ARouter注解中的path值，必须要以 / 开头
        if (ProcessorUtils.isEmpty(path) || !path.startsWith("/")) {
            messager.printMessage(Diagnostic.Kind.ERROR, "@ARouter注解中的path值，必须要以 / 开头");
            return false;
        }
        if (path.lastIndexOf("/") == 0) {//开发者必须遵循
            messager.printMessage(Diagnostic.Kind.ERROR, "@ARouter注解未按规范配置，如：/app/MainActivity");
            return false;
        }

        // 从第一个 / 到第二个 / 中间截取，如：/app/MainActivity 截取出 app,order,personal 作为group
        String finalGroup = path.substring(1, path.indexOf("/", 1));
        if (!ProcessorUtils.isEmpty(group) && !group.equals(options)) {
            messager.printMessage(Diagnostic.Kind.ERROR, "@ARouter注解中的group值必须和子模块名一致！");
            return false;
        } else {
            bean.setGroup(finalGroup);
        }
        return true;
    }
}
