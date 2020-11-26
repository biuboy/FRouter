package com.htf.user;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.htf.arouter_annotation.ARouter;
import com.htf.arouter_annotation.Parameter;

@ARouter(path = "/user/User_MainActivity")
public class User_MainActivity extends AppCompatActivity {

    @Parameter
    String numberId;

    @Parameter
    String name;

    @Parameter
    int age;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.user_main_layout);
    }
}