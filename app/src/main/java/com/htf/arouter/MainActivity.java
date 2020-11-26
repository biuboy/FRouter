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
import com.htf.common.UserBean;

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

    public void jumpLogin(View view) {
        UserBean userBean = new UserBean();
        userBean.setUuid("12001");
        userBean.setName("biubiubiu");
        userBean.setPhone("180100100112");

        UserBean userBean1 = new UserBean();
        userBean1.setUuid("120012assas");
        userBean1.setName("李逵UIi");
        userBean1.setPhone("180100100112");
        
        List<String> data = new ArrayList<>();
        data.add("asalsa;");
        data.add("男男女女多所军军");

        Bundle bundle = new Bundle();
        ArrayList<UserBean> userList = new ArrayList<>();
        userList.add(userBean);
        userList.add(userBean1);
        bundle.putParcelableArrayList("userLst", userList);

        RouterManager.getInstance().build("/user/User_LoginAcitivty")
                .withBundle(bundle)
                .navigation(this, 1001);

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && requestCode == 1001) {
            Log.e("TAG", "回传值了");
        }
    }

    public void jumpZixun(View view) {
        RouterManager.getInstance().build("/zixun/ZiXun_WebViewActivity")
                .navigation(this);
    }
}