package com.htf.arouter_api;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
import android.util.LruCache;

import com.htf.arouter_annotation.bean.RouterBean;

import java.util.Map;

import static com.htf.arouter_annotation.bean.RouterBean.TypeEnum.ACTIVITY;

/**
 * Created by htf on 2020/11/20
 * Describe:路由管理器
 */
public class RouterManager {

    private String group;

    private String path;

    private static RouterManager instance;

    private LruCache<String, ARouterGroup> mGroupLruCache;

    private LruCache<String, ARouterPath> mPathLruCache;

    public static RouterManager getInstance() {
        if (instance == null) {
            synchronized (RouterManager.class) {
                if (instance == null) {
                    instance = new RouterManager();
                }
            }
        }
        return instance;
    }

    private RouterManager() {
        mGroupLruCache = new LruCache<>(100);
        mPathLruCache = new LruCache<>(100);
    }

    /***
     * @param path 例如：/order/Order_MainActivity
     *      * @return
     */
    public BundleManager build(String path) {
        if (TextUtils.isEmpty(path) || !path.startsWith("/")) {
            throw new IllegalArgumentException("path错误，正确写法：如/user/User_LoginActivity");
        }

        if (path.lastIndexOf("/") == 0) {
            throw new IllegalArgumentException("path错误,正确写法：如/user/User_LoginActivity");
        }
        // 截取组名  /user/User_LoginActivity  finalGroup = user
        String finalGroup = path.substring(1, path.indexOf("/", 1));

        if (TextUtils.isEmpty(finalGroup)) {
            throw new IllegalArgumentException("path错误,正确写法：如/user/User_LoginActivity");
        }
        this.path = path;
        this.group = finalGroup;
        return new BundleManager();
    }


    private final static String FILE_GROUP_NAME = "ARouter$$Group$$";

    // 例如：寻找 ARouter$$Group$$personal  寻址   ARouter$$Group$$user   ARouter$$Group$$app
    public Object navigation(Context context, BundleManager bundleManager) {
        RouterBean routerBean = getRouterBean(context);
        if (routerBean != null) {
            switch (routerBean.getTypeEnum()) {//为了区别Fragment Activity
                case ACTIVITY:
                    Intent intent = new Intent(context, routerBean.getMyClass()); // 例如：getClazz == Order_MainActivity.class
                    intent.putExtras(bundleManager.getBundle()); // 携带参数
                    context.startActivity(intent);
                    break;
            }
        }
        return null;
    }

    public Object navigation(Context context, BundleManager bundleManager, int requestCode) {
        RouterBean routerBean = getRouterBean(context);
        if (routerBean != null) {
            switch (routerBean.getTypeEnum()) {//为了区别Fragment  Activity
                case ACTIVITY:
                    Intent intent = new Intent(context, routerBean.getMyClass()); // 例如：getClazz == Order_MainActivity.class
                    intent.putExtras(bundleManager.getBundle()); // 携带参数
                    ((Activity) context).startActivityForResult(intent, requestCode);
                    break;
            }
        }
        return null;
    }

    private RouterBean getRouterBean(Context context) {
        String groupClassName = context.getPackageName() + "." + FILE_GROUP_NAME + group;
        try {
            ARouterGroup aRouterGroup = mGroupLruCache.get(group);
            if (null == aRouterGroup) {
                Class<?> aClass = Class.forName(groupClassName);
                aRouterGroup = (ARouterGroup) aClass.newInstance();
                mGroupLruCache.put(group, aRouterGroup);
            }
            //判断group里是否有数据
            if (aRouterGroup.getGroupMap().isEmpty()) {
                throw new RuntimeException("路由表出现问题.....");
            }
            ARouterPath aRouterPath = mPathLruCache.get(path);
            if (null == aRouterPath) {
                Class<? extends ARouterPath> pathClass = aRouterGroup.getGroupMap().get(group);
                aRouterPath = pathClass.newInstance();
                mPathLruCache.put(path, aRouterPath);
            }
            RouterBean routerBean = aRouterPath.getPathMap().get(path);
            return routerBean;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
