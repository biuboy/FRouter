package com.htf.zixun;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.htf.arouter_annotation.ARouter;

@ARouter(path = "/zixun/ZiXun_ZiXunActivity")
public class ZiXun_ZiXunActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.zixun_activity_main);
    }
}