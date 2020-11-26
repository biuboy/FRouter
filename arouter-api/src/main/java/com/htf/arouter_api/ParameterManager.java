package com.htf.arouter_api;

import android.app.Activity;
import android.util.LruCache;

/**
 * Created by htf on 2020/11/20
 * Describe: 传递参数管理器
 */
public class ParameterManager {

    private static ParameterManager instance;

    public static ParameterManager getInstance() {
        if (instance == null) {
            synchronized (ParameterManager.class) {
                if (instance == null) {
                    instance = new ParameterManager();
                }
            }
        }
        return instance;
    }

    // 为了寻找javapoet生成的User_LoginActivity + $$Parameter
    static final String FILE_SUFFIX_NAME = "$$Parameter";

    private LruCache<String, ParameterData> cache;

    private ParameterManager() {
        cache = new LruCache<>(100);//为了提高性能
    }

    public void loadParameter(Activity activity) {
        String activityName = activity.getClass().getName();
        ParameterData parameterData = cache.get(activityName);
        if (parameterData == null) {
            try {
                Class<?> aClass = Class.forName(activityName + FILE_SUFFIX_NAME);
                parameterData = (ParameterData) aClass.newInstance();
                cache.put(activityName, parameterData);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        parameterData.getParameter(activity);
    }

}
