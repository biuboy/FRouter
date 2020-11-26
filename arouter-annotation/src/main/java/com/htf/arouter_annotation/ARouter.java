package com.htf.arouter_annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)//作用在类上
@Retention(RetentionPolicy.CLASS)//CLASS编译时   RetentionPolicy.RUNTIME运行时
public @interface ARouter {
    String path();
    String group() default "";
}
