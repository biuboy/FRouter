package com.htf.arouter_api;

import java.util.Map;

public interface ARouterGroup {

    /**
     *
     * 存放的内容key:app  value:app所有的path类
     */
    Map<String, Class<? extends ARouterPath>> getGroupMap();
}
