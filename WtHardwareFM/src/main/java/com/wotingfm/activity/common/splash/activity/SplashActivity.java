package com.wotingfm.activity.common.splash.activity;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;

import com.android.volley.VolleyError;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.wotingfm.activity.common.main.MainActivity;
import com.wotingfm.activity.common.welcome.activity.WelcomeActivity;
import com.wotingfm.activity.im.interphone.groupmanage.model.UserInfo;
import com.wotingfm.common.application.BSApplication;
import com.wotingfm.common.config.GlobalConfig;
import com.wotingfm.common.constant.StringConstant;
import com.wotingfm.common.volley.VolleyCallback;
import com.wotingfm.common.volley.VolleyRequest;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * 启动页面，第一个activity
 * 作者：xinlong on 2016/8/23 21:18
 * 邮箱：645700751@qq.com
 */
public class SplashActivity extends Activity {
    private SharedPreferences sharedPreferences= BSApplication.SharedPreferences;
    private String first;
    private String tag = "SPLASH_VOLLEY_REQUEST_CANCEL_TAG";
    private boolean isCancelRequest;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        first = sharedPreferences.getString(StringConstant.FIRST, "0");//是否是第一次登录
        Editor et = sharedPreferences.edit();
        et.putString(StringConstant.PERSONREFRESHB, "true");
        et.commit();
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                send();
            }
        }, 1000);
    }

    // 获取请求网络公共属性
    protected void send() {
        JSONObject jsonObject = VolleyRequest.getJsonObject(SplashActivity.this);
        VolleyRequest.RequestPost(GlobalConfig.splashUrl, tag, jsonObject, new VolleyCallback() {
            @Override
            protected void requestSuccess(JSONObject result) {
                if (isCancelRequest) {
                    return;
                }
                try {
                    String ReturnType = result.getString("ReturnType");
                    if (ReturnType.equals("1001")) {

                        try {
                            String UserInfo = result.getString("UserInfo");
                            if (UserInfo == null || UserInfo.trim().equals("")) {
                                Editor et = sharedPreferences.edit();
                                et.putString(StringConstant.USERID, "userid");
                                et.putString(StringConstant.USERNAME, "username");
                                et.putString(StringConstant.IMAGEURL, "imageurl");
                                et.putString(StringConstant.IMAGEURBIG, "imageurlbig");
                                et.commit();
                            } else {
                                UserInfo list = new Gson().fromJson(UserInfo, new TypeToken<UserInfo>() {
                                }.getType());
                                String userId = list.getUserId();
                                String userName = list.getUserName();
                                String imageUrl = list.getPortraitMini();
                                String imageUrlBig = list.getPortraitBig();
                                Editor et = sharedPreferences.edit();
                                et.putString(StringConstant.USERID, userId);
                                et.putString(StringConstant.IMAGEURL, imageUrl);
                                et.putString(StringConstant.IMAGEURBIG, imageUrlBig);
                                et.putString(StringConstant.USERNAME, userName);
                                et.commit();
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                            Log.e(tag + "异常=", e.toString() + "");
                        }
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                    Log.e(tag + "异常=", e.toString() + "");
                }

                if (first != null && first.equals("1")) {
                    startActivity(new Intent(SplashActivity.this, MainActivity.class));//跳转到主页
                } else {
                    startActivity(new Intent(SplashActivity.this, WelcomeActivity.class));//跳转到引导页
                }
                finish();
            }

            @Override
            protected void requestError(VolleyError error) {
                if (first != null && first.equals("1")) {
                    startActivity(new Intent(SplashActivity.this, MainActivity.class));//跳转到主页
                } else {
                    startActivity(new Intent(SplashActivity.this, WelcomeActivity.class));//跳转到引导页
                }
                finish();
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        isCancelRequest = VolleyRequest.cancelRequest(tag);
        sharedPreferences = null;
        first = null;
        tag = null;
    }
}
