package com.htf.arouter;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.htf.arouter_annotation.ARouter;
import com.htf.arouter_annotation.Parameter;
import com.htf.arouter_api.ParameterManager;

@ARouter(path = "/app/NextActivity")
public class NextActivity extends AppCompatActivity {

    @Parameter
    String numberId;

    @Parameter
    String name;

    @Parameter
    int age;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ParameterManager.getInstance().loadParameter(this);
    }
}
