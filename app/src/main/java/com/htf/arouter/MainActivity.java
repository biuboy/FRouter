package com.htf.arouter;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.htf.arouter_annotation.ARouter;
import com.htf.arouter_annotation.Parameter;
import com.htf.arouter_annotation.bean.RouterBean;
import com.htf.arouter_api.ARouterPath;
import com.htf.arouter_api.ParameterManager;
import com.htf.arouter_api.RouterManager;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@ARouter(path = "/app/MainActivity")
public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

    }
//
    public void jumpLogin(View view) {

        RouterManager.getInstance().build("/app/NextActivity")
                .navigation(this);

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && requestCode == 1001) {
            Log.e("TAG", "回传值了");
        }
    }
//
//    public void jumpZixun(View view) {
//        RouterManager.getInstance().build("/zixun/ZiXun_WebViewActivity")
//                .navigation(this);
//    }
}