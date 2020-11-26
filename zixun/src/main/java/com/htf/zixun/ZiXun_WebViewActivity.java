package com.htf.zixun;

import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

import com.htf.arouter_annotation.ARouter;
import com.htf.arouter_api.RouterManager;

@ARouter(path = "/zixun/ZiXun_WebViewActivity")
public class ZiXun_WebViewActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.zixun_webview_layout);
    }

    public void login(View view) {
        RouterManager.getInstance().build("/user/User_LoginAcitivty")
                .navigation(this);
    }
}