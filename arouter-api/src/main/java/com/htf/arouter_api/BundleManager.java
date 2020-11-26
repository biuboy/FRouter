package com.htf.arouter_api;

import android.content.Context;
import android.os.Bundle;
import android.os.Parcelable;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by htf on 2020/11/20
 * Describe:
 */
public class BundleManager {

    private Bundle mBundle = new Bundle();

    public Bundle getBundle() {
        return mBundle;
    }

    // 对外界提供，可以携带参数的方法链式调用效果 模仿开源框架
    public BundleManager withString(@NonNull String key, @Nullable String value) {
        mBundle.putString(key, value);
        return this;
    }

    public BundleManager withDouble(@NonNull String key, @Nullable double value) {
        mBundle.putDouble(key, value);
        return this;
    }

    public BundleManager withBoolean(@NonNull String key, @Nullable boolean value) {
        mBundle.putBoolean(key, value);
        return this;
    }

    public BundleManager withParcelable(@NonNull String key, @Nullable Parcelable value) {
        mBundle.putParcelable(key, value);
        return this;
    }

    public BundleManager withSerializable(@NonNull String key, @Nullable Serializable value) {
        mBundle.putSerializable(key, value);
        return this;
    }

    public BundleManager withInt(@NonNull String key, @Nullable int value) {
        mBundle.putInt(key, value);
        return this;
    }

    public BundleManager withStringArrayListExtra(@NonNull String key, @Nullable List<String> value) {
        mBundle.putStringArrayList(key, (ArrayList<String>) value);
        return this;
    }

    public BundleManager withBundle(Bundle bundle) {
        this.mBundle = bundle;
        return this;
    }

    public Object navigation(Context context) {
        return RouterManager.getInstance().navigation(context, this);
    }

    public Object navigation(Context context, int requestCode) {
        return RouterManager.getInstance().navigation(context, this, requestCode);
    }

}
