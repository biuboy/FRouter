package com.htf.arouter_api;

import com.htf.arouter_annotation.bean.RouterBean;

import java.util.Map;

public interface ARouterPath {

    /**
     * 存放的内容key:/app/MainActivity value:RouterBean == App_MainActivity.class
     */
    Map<String, RouterBean> getPathMap();
}
