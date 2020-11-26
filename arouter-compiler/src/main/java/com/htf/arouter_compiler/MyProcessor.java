//package com.htf.arouter_compiler;
//
//import com.google.auto.service.AutoService;
//import com.htf.arouter_annotation.ARouter;
//import com.htf.arouter_annotation.bean.RouterBean;
//import com.htf.arouter_compiler.util.ProcessorConfig;
//import com.htf.arouter_compiler.util.ProcessorUtils;
//import com.squareup.javapoet.ClassName;
//import com.squareup.javapoet.JavaFile;
//import com.squareup.javapoet.MethodSpec;
//import com.squareup.javapoet.ParameterizedTypeName;
//import com.squareup.javapoet.TypeName;
//import com.squareup.javapoet.TypeSpec;
//import com.squareup.javapoet.WildcardTypeName;
//
//import java.io.IOException;
//import java.util.ArrayList;
//import java.util.HashMap;
//import java.util.List;
//import java.util.Map;
//import java.util.Set;
//
//import javax.annotation.processing.AbstractProcessor;
//import javax.annotation.processing.Filer;
//import javax.annotation.processing.Messager;
//import javax.annotation.processing.ProcessingEnvironment;
//import javax.annotation.processing.Processor;
//import javax.annotation.processing.RoundEnvironment;
//import javax.annotation.processing.SupportedAnnotationTypes;
//import javax.annotation.processing.SupportedOptions;
//import javax.annotation.processing.SupportedSourceVersion;
//import javax.lang.model.SourceVersion;
//import javax.lang.model.element.Element;
//import javax.lang.model.element.Modifier;
//import javax.lang.model.element.TypeElement;
//import javax.lang.model.type.TypeMirror;
//import javax.lang.model.util.Elements;
//import javax.lang.model.util.Types;
//import javax.tools.Diagnostic;
//
//@AutoService(Processor.class)
//@SupportedSourceVersion(SourceVersion.RELEASE_8)
//@SupportedAnnotationTypes(ProcessorConfig.AROUTER_PACKAGE)
//@SupportedOptions({ProcessorConfig.OPTIONS, ProcessorConfig.APT_PACKAGE})
//public class MyProcessor extends AbstractProcessor {
//
//    private Types typeTool;//// type(类信息)的工具类，包含用于操作TypeMirror的工具方法
//
//    private Elements elementTool;// 操作Element的工具类（类，函数，属性，其实都是Element）
//
//    private Messager messager;
//
//    private Filer filer;
//
//    private String options; // 各个模块传递过来的模块名 例如：app user zixun order等
//
//    private String aptPackage; // 各个模块传递过来的目录 用于统一存放 apt生成的文件
//
//    private Map<String, List<RouterBean>> mAllPathMap = new HashMap<>();
//
//    // Map<"user", "ARouter$$Path$$user.class">
//    private Map<String, String> mAllGroupMap = new HashMap<>();
//
//    @Override
//    public synchronized void init(ProcessingEnvironment processingEnvironment) {
//        super.init(processingEnvironment);
//        elementTool = processingEnvironment.getElementUtils();
//        filer = processingEnvironment.getFiler();
//        messager = processingEnvironment.getMessager();
//        typeTool = processingEnvironment.getTypeUtils();
//        options = processingEnvironment.getOptions().get(ProcessorConfig.OPTIONS);
//        aptPackage = processingEnvironment.getOptions().get(ProcessorConfig.APT_PACKAGE);
//
//    }
//
//    @Override
//    public boolean process(Set<? extends TypeElement> set, RoundEnvironment roundEnvironment) {
//        if (set.isEmpty()) {
//            messager.printMessage(Diagnostic.Kind.NOTE, "没有发现注解的类");
//            return false;
//        }
//        // // 获取所有被 @ARouter 注解的 元素集合
//        Set<? extends Element> elements = roundEnvironment.getElementsAnnotatedWith(ARouter.class);
//        // 通过Element工具类，获取android.app.Activity类型
//        TypeElement activityElement = elementTool.getTypeElement(ProcessorConfig.ACTIVITY_PACKAGE);
//        TypeMirror activityMirror = activityElement.asType();
//        for (Element element : elements) {
//            String className = element.getSimpleName().toString();
//            ARouter aRouter = element.getAnnotation(ARouter.class);
//            messager.printMessage(Diagnostic.Kind.NOTE, "aRouter.group() == " + aRouter.group());
//            RouterBean routerBean = new RouterBean.Builder()
//                    .addElement(element)
//                    .addGroup(options)
//                    .addPath(aRouter.path())
//                    .build();
//            TypeMirror myMirror = element.asType();//自己类的TypeMirror 用于判断是不是继承android.app.Activity
//            if (typeTool.isSubtype(myMirror, activityMirror)) {//是activity
//                routerBean.setTypeEnum(RouterBean.TypeEnum.ACTIVITY);
//            } else {
//                throw new RuntimeException(className + "必须是Activity");
//            }
//            //把所有的注解类封以module名为key封装到mAllPathMap中
//            if (checkRouterPath(routerBean)) {
//                List<RouterBean> routerBeanList = mAllPathMap.get(routerBean.getGroup());
//                if (ProcessorUtils.isEmpty(routerBeanList)) {
//                    routerBeanList = new ArrayList<>();
//                    routerBeanList.add(routerBean);
//                    mAllPathMap.put(routerBean.getGroup(), routerBeanList);
//                } else {
//                    routerBeanList.add(routerBean);
//                }
//            } else {
//                messager.printMessage(Diagnostic.Kind.ERROR, "@ARouter注解未按规范配置，如：/app/MainActivity");
//            }
//        }//循环结束，把所有的注解类以module名为key封装到mAllPathMap
//
//        // TODO 开始用javapoet生成文件
//        //定义文件要实现的接口
//        TypeElement pathTypeElement = elementTool.getTypeElement(ProcessorConfig.AROUTER_API_PATH);
//        try {
//            createPathFile(pathTypeElement);
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//
//        TypeElement groupType = elementTool.getTypeElement(ProcessorConfig.AROUTER_API_GROUP);
//        try {
//            createGroupFile(groupType, pathTypeElement);
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//        return true;
//    }
//
//    //生成路由组Group文件，如：ARouter$$Group$$app
//    private void createGroupFile(TypeElement groupType, TypeElement pathType) throws IOException {
//        if (ProcessorUtils.isEmpty(mAllGroupMap) || ProcessorUtils.isEmpty(mAllPathMap)) return;
//        //要生成的内容
// public class ARouter$$Group$$order implements ARouterGroup {
//
//
//*
//         * @return key：组名 如：“order”  ---- ARouter$$Path$$order--(包含了很多的myClass)
//
//            @Override
//            public Map<String, Class<? extends ARouterPath>> getGroupMap() {
//                Map<String, Class<? extends ARouterPath>> groupMap = new HashMap<>();
//                groupMap.put("order", ARouter$$Path$$app.class);       // 寻找Path
//                groupMap.put("personal", ARouter$$Path$$user.class); // 寻找Path
//                return groupMap;
//            }
//        }
//
//
//        //Map<String, Class<? extends ARouterPath>>
//        TypeName methodReturn = ParameterizedTypeName.get(
//                ClassName.get(Map.class),
//                ClassName.get(String.class),
//                //Class<? extends ARouterPath>>
//                ParameterizedTypeName.get(ClassName.get(Class.class),
//                        WildcardTypeName.subtypeOf(ClassName.get(pathType))) // ? extends ARouterLoadPath)
//        );
//
//        MethodSpec.Builder methodBuilder = MethodSpec.methodBuilder(ProcessorConfig.GROUP_METHOD_NAME)
//                .addAnnotation(Override.class)
//                .addModifiers(Modifier.PUBLIC)
//                .returns(methodReturn);
//
//        //方法里的内容
//        // Map<String, Class<? extends ARouterPath>> groupMap = new HashMap<>();
//        methodBuilder.addStatement("$T<$T, $T> $N = new $T<>()",
//                ClassName.get(Map.class),
//                ClassName.get(String.class),
//                //Class<? extends ARouterPath>
//                ParameterizedTypeName.get(ClassName.get(Class.class),
//                        WildcardTypeName.subtypeOf(ClassName.get(pathType))), // ? extends ARouterPath
//                ProcessorConfig.GROUP_VAR1,
//                ClassName.get(HashMap.class));
//
//        //  groupMap.put("app", ARouter$$Path$$app.class);
//        //	groupMap.put("user", ARouter$$Path$$user.class);
//        methodBuilder.addStatement("$N.put($S,$T.class)",
//                ProcessorConfig.GROUP_VAR1,
//                options,
//                ClassName.get(aptPackage, mAllGroupMap.get(options)));
//        methodBuilder.addStatement("return $N", ProcessorConfig.GROUP_VAR1);
//
//        ///生成文件
//        String fileClassName = ProcessorConfig.GROUP_FILE_NAME + options;
//        JavaFile.builder(aptPackage,
//                TypeSpec.classBuilder(fileClassName)
//                        .addSuperinterface(ClassName.get(groupType))
//                        .addModifiers(Modifier.PUBLIC)
//                        .addMethod(methodBuilder.build())
//                        .build())
//                .build()
//                .writeTo(filer);
//
//    }
//
//    private void createPathFile(TypeElement typeElement) throws IOException {
//        //最终要生成这种文件
//public class XXXPath implements ARouterPath {
//            @Override
//            public Map<String, RouterBean> getPathMap() {
//                Map<String, RouterBean> pathMap = new HashMap<>();
//                pathMap.put("/order/Order_MainActivity",
//                        RouterBean.create(RouterBean.TypeEnum.ACTIVITY,
//                                Order_MainActivity.class, "/order/Order_MainActivity", "order"
//                        ));
//                 pathMap.put("/order/Order_MainActivity",
//                        RouterBean.create(RouterBean.TypeEnum.ACTIVITY,
//                                Order_MainActivity.class, "/order/Order_MainActivity", "order"
//                        ));
//                return pathMap;
//            }
//        }
//
//
//        //先把方法返回类型生成   Map<String, RouterBean>
//        ParameterizedTypeName methodReturn = ParameterizedTypeName.get(
//                ClassName.get(Map.class),
//                ClassName.get(String.class),
//                ClassName.get(RouterBean.class)
//        );
//        //生成方法
//        MethodSpec.Builder methodBuilder = MethodSpec.methodBuilder(ProcessorConfig.PATH_METHOD_NAME)
//                .addModifiers(Modifier.PUBLIC)
//                .addAnnotation(Override.class)
//                .returns(methodReturn);
//
//        for (Map.Entry<String, List<RouterBean>> entry : mAllPathMap.entrySet()) {
//            ////方法里的内容 Map<String, RouterBean> pathMap = new HashMap<>();
//            methodBuilder.addStatement("$T<$T, $T> $N = new $T<>()",
//                    ClassName.get(Map.class),
//                    ClassName.get(String.class),
//                    ClassName.get(RouterBean.class),
//                    ProcessorConfig.PATH_VAR1,
//                    ClassName.get(HashMap.class));
//            List<RouterBean> routerBeanList = entry.getValue();
//
//            for (RouterBean routerBean : routerBeanList) {
//*
//                 * 循环添加
//                 * pathMap.put("/user/user_LoginActivity",
//                 * RouterBean.create(RouterBean.TypeEnum.ACTIVITY, Order_MainActivity.class, "/user/user_LoginActivity","user"));
//
//
//                methodBuilder.addStatement("$N.put($S, $T.create($T.$L, $T.class, $S, $S))",
//                        ProcessorConfig.PATH_VAR1,
//                        routerBean.getPath(),
//                        ClassName.get(RouterBean.class),
//                        ClassName.get(RouterBean.TypeEnum.class),
//                        routerBean.getTypeEnum(),
//                        ClassName.get((TypeElement) routerBean.getElement()),
//                        routerBean.getPath(),
//                        routerBean.getGroup());
//            }
//            // return pathMap;
//            methodBuilder.addStatement("return $N", ProcessorConfig.PATH_VAR1);
//        }
//
//        //生成的类文件名ARouter$$Path$$user    有implements所以方法和类要合为一体生成
//        String fileName = ProcessorConfig.PATH_FILE_NAME + options;
//        messager.printMessage(Diagnostic.Kind.NOTE, "APT生成路由Path类文件：" +
//                aptPackage + "." + fileName);
//        JavaFile.builder(aptPackage,
//                TypeSpec.classBuilder(fileName)
//                        .addSuperinterface(ClassName.get(typeElement))
//                        .addMethod(methodBuilder.build())
//                        .addModifiers(Modifier.PUBLIC)
//                        .build())
//                .build()
//                .writeTo(filer);
//        mAllGroupMap.put(options, fileName);
//    }
//
//    private boolean checkRouterPath(RouterBean bean) {
//        String group = bean.getGroup();//app、user、zixun等module
//        String path = bean.getPath();//   /app/MainActivity
//        // TODO 校验path
//        // @ARouter注解中的path值，必须要以 / 开头
//        if (ProcessorUtils.isEmpty(path) || !path.startsWith("/")) {
//            messager.printMessage(Diagnostic.Kind.ERROR, "@ARouter注解中的path值，必须要以 / 开头");
//            return false;
//        }
//        if (path.lastIndexOf("/") == 0) {//开发者必须遵循
//            messager.printMessage(Diagnostic.Kind.ERROR, "@ARouter注解未按规范配置，如：/app/MainActivity");
//            return false;
//        }
//
//        // 从第一个 / 到第二个 / 中间截取，如：/app/MainActivity 截取出 app,order,personal 作为group
//        String finalGroup = path.substring(1, path.indexOf("/", 1));
//        if (!ProcessorUtils.isEmpty(group) && !group.equals(options)) {
//            messager.printMessage(Diagnostic.Kind.ERROR, "@ARouter注解中的group值必须和子模块名一致！");
//            return false;
//        } else {
//            bean.setGroup(finalGroup);
//        }
//        return true;
//    }
//}
