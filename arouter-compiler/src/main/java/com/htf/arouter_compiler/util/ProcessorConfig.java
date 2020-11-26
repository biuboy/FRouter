package com.htf.arouter_compiler.util;

public interface ProcessorConfig {

    // @ARouter注解 的 包名 + 类名
    String AROUTER_PACKAGE =  "com.htf.arouter_annotation.ARouter";

    //
    String PARAMETER_PACKAGE =  "com.htf.arouter_annotation.Parameter";

    // 接收module参数
    String OPTIONS = "moduleName";

    // 目的是接收 包名
    String APT_PACKAGE = "packageNameForAPT";

    // 为了匹配加注解的类是否为Activity
    public static final String ACTIVITY_PACKAGE = "android.app.Activity";

    // ARouter api 包名
    String AROUTER_API_PACKAGE = "com.htf.arouter_api";

    // ARouter api 的 ARouterPath 高层标准
    String AROUTER_API_PATH = AROUTER_API_PACKAGE + ".ARouterPath";

    // ARouter api 的 ARouterGroup 高层标准
    String AROUTER_API_GROUP = AROUTER_API_PACKAGE + ".ARouterGroup";

    // 路由组，中的 Path 里面的 方法名
    String PATH_METHOD_NAME = "getPathMap";

    // 路由组，中的 Path 里面 的 变量名
    String PATH_VAR1 = "pathMap";

    // 路由组，PATH 最终要生成的 文件名
    String PATH_FILE_NAME = "ARouter$$Path$$";
    // 路由组，中的 Group 里面的 方法名
    String GROUP_METHOD_NAME = "getGroupMap";

    // 路由组，中的 Group 里面 的 变量名 1
    String GROUP_VAR1 = "groupMap";

    // 路由组，GROUP 最终要生成的 文件名
    String GROUP_FILE_NAME = "ARouter$$Group$$";

    // ARouter api 的 ParameterGet 高层标准
    String AROUTER_AIP_PARAMETER_DATA = AROUTER_API_PACKAGE + ".ParameterData";

    // ARouter api 的 ParmeterGet 方法的名字
    String PARAMETER_METHOD_NAME = "getParameter";

    // ARouter api 的 ParameterGet 方法参数的名字
    String PARAMETER_NAME = "targetParameter";

    // String全类名
    String STRING = "java.lang.String";

    //用户判断是否是自定义对象类型 要实现Parcelable
    String PARCELABLE = "android.os.Parcelable";

    // ARouter aip 的 ParmeterGet 的 生成文件名称 $$Parameter
    String PARAMETER_FILE_NAME = "$$Parameter";

    //用户判断是否是List类型
    String LIST_STRING = "java.util.List<java.lang.String>";

    String LIST_PARCELABLE = "java.util.List<&T>";
}
