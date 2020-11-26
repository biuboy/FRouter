package com.htf.common;

import android.annotation.SuppressLint;
import android.os.Parcel;
import android.os.Parcelable;

import java.util.List;

/**
 * Created by htf on 2020/11/20
 * Describe:
 */
public class UserBean implements Parcelable {

    private String name;

    private String phone;

    private String uuid;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int arg1) {
        dest.writeString(name);
        dest.writeString(uuid);
        dest.writeString(phone);
    }
    public static final Creator<UserBean> CREATOR = new Creator<UserBean>() {

        @Override
        public UserBean createFromParcel(Parcel source) {

            UserBean bean = new UserBean();
            bean.name =  source.readString();
            bean.uuid =  source.readString();
            bean.phone =  source.readString();
            return bean;
        }

        @Override
        public UserBean[] newArray(int size) {
            return new UserBean[size];
        }
    };

    @Override
    public String toString() {
        return "UserBean{" +
                "name='" + name + '\'' +
                ", phone='" + phone + '\'' +
                ", uuid='" + uuid + '\'' +
                '}';
    }
}
