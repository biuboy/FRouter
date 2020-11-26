package com.htf.user;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

import com.htf.arouter_annotation.ARouter;
import com.htf.arouter_annotation.Parameter;
import com.htf.arouter_api.ParameterManager;
import com.htf.common.UserBean;

import java.util.List;

@ARouter(path = "/user/User_LoginAcitivty")
public class User_LoginAcitivty extends AppCompatActivity {

    @Parameter
    String numberId;

    @Parameter
    int age;

    @Parameter
    UserBean userBean;

    @Parameter
    List<String> data;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.user_login_layout);
        ParameterManager.getInstance().loadParameter(this);

        Bundle bundle = getIntent().getExtras();
        List<UserBean> list = bundle.getParcelableArrayList("userLst");
       if (list !=null) {
            for (UserBean bean : list) {
                Log.e("TAG", "bean == " + bean.toString());
            }
        }
    }

    public void backData(View view) {
        Intent intent = new Intent();
        setResult(RESULT_OK, intent);
        finish();
    }
}