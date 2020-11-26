package com.htf.arouter_annotation.bean;

import javax.lang.model.element.Element;

/**
 * app、user等组件中的MainActivity等对象, 封装成对象会有更多的属性
 */
public class RouterBean {

    public enum TypeEnum {//可扩展 fragment
        ACTIVITY
    }

    private TypeEnum typeEnum;//枚举类型activity
    private Element element;//节点 JavaPoet，可以拿到很多的信息
    private Class<?> myClass;// 被注解 class对象 eg: MainActivity.class
    private String path;// 路由地址 eg: /app/MainActivity
    private String group;// 路由组  eg: app user等


    public TypeEnum getTypeEnum() {
        return typeEnum;
    }

    public void setTypeEnum(TypeEnum typeEnum) {
        this.typeEnum = typeEnum;
    }

    public Element getElement() {
        return element;
    }

    public void setElement(Element element) {
        this.element = element;
    }

    public Class<?> getMyClass() {
        return myClass;
    }

    public void setMyClass(Class<?> myClass) {
        this.myClass = myClass;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getGroup() {
        return group;
    }

    public void setGroup(String group) {
        this.group = group;
    }

    private RouterBean(TypeEnum typeEnum, Class<?> myClass, String path, String group) {
        this.typeEnum = typeEnum;
        // this.element = element;
        this.myClass = myClass;
        this.path = path;
        this.group = group;
    }

    //对外暴露 为了方便APT生成代码
    public static RouterBean create(TypeEnum typeEnum, Class<?> clazz,String path, String group) {
        return new RouterBean(typeEnum, clazz, path, group);
    }

    /**
     * 构建者模式
     */

    public static class Builder {
        // 枚举类型：Activity
        private TypeEnum type;
        // 类节点
        private Element element;
        // 注解使用的类对象
        private Class<?> clazz;
        // 路由地址
        private String path;
        // 路由组
        private String group;

        public Builder addType(TypeEnum type) {
            this.type = type;
            return this;
        }

        public Builder addElement(Element element) {
            this.element = element;
            return this;
        }

        public Builder addClazz(Class<?> clazz) {
            this.clazz = clazz;
            return this;
        }

        public Builder addPath(String path) {
            this.path = path;
            return this;
        }

        public Builder addGroup(String group) {
            this.group = group;
            return this;
        }

        public RouterBean build() {
            if (path == null || path.length() == 0) {
                throw new IllegalArgumentException("path路径为空，请检查" + clazz);
            }

            return new RouterBean(this);
        }
    }

    private RouterBean(Builder builder) {
        this.typeEnum = builder.type;
        this.element = builder.element;
        this.myClass = builder.clazz;
        this.path = builder.path;
        this.group = builder.group;
    }

    @Override
    public String toString() {
        return "RouterBean{" +
                "path='" + path + '\'' +
                ", group='" + group + '\'' +
                '}';
    }
}
