package com.wotingfm.activity.common.splash.activity;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences.Editor;
import android.content.res.Configuration;
import android.content.res.Resources;
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
import com.wotingfm.util.L;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * 启动页面，第一个activity
 * 作者：xinlong on 2016/8/23 21:18
 * 邮箱：645700751@qq.com
 */
public class SplashActivity extends Activity {
    private String first;
    private String tag = "SPLASH_VOLLEY_REQUEST_CANCEL_TAG";
    private boolean isCancelRequest;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        first = BSApplication.SharedPreferences.getString(StringConstant.FIRST, "0");// 是否是第一次登录
        Editor et = BSApplication.SharedPreferences.edit();
        et.putString(StringConstant.PERSONREFRESHB, "true");// 默认每次都是刷新通讯录界面
        if(!et.commit()) L.w("数据 commit 失败!");
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                send();
            }
        }, 1000);// 延时一秒钟后执行 send 方法
    }

    // 获取请求网络公共属性
    protected void send() {
        JSONObject jsonObject = VolleyRequest.getJsonObject(SplashActivity.this);
        VolleyRequest.RequestPost(GlobalConfig.splashUrl, tag, jsonObject, new VolleyCallback() {
            @Override
            protected void requestSuccess(JSONObject result) {
                if (isCancelRequest) return;
                try {
                    String ReturnType = result.getString("ReturnType");
                    if (ReturnType != null && ReturnType.equals("1001")) {
                        Editor et = BSApplication.SharedPreferences.edit();
                        String UserInfo = result.getString("UserInfo");
                        if (UserInfo != null && !UserInfo.trim().equals("")) {
                            UserInfo list = new Gson().fromJson(UserInfo, new TypeToken<UserInfo>() {}.getType());
                            String userId = list.getUserId();// ID
                            String userName = list.getUserName();// 用户名
                            String userNum = list.getUserNum();// 用户号
                            String imageUrl = list.getPortraitMini();// 用户头像
                            String imageUrlBig = list.getPortraitBig();// 用户大头像
                            String gender = list.getSex();// 性别
                            String region = list.getRegion();// 区域
                            String birthday = list.getBirthday();// 生日
                            String age = list.getAge();// 年龄
                            String starSign = list.getStarSign();// 星座
                            String email = list.getEmail();// 邮箱
                            String userSign = list.getUserSign();// 签名
                            String nickName=list.getNickName();

                            if (userId != null && !userId.equals("")) {
                                et.putString(StringConstant.USERID, userId);
                            }
                            if (userName != null && !userName.equals("")) {
                                et.putString(StringConstant.USERNAME, userName);
                            }
                            if (imageUrl != null && !imageUrl.equals("")) {
                                et.putString(StringConstant.IMAGEURL, imageUrl);
                            }
                            if (imageUrlBig != null && !imageUrlBig.equals("")) {
                                et.putString(StringConstant.IMAGEURBIG, imageUrlBig);
                            }
                            if (userNum != null && !userNum.equals("")) {
                                et.putString(StringConstant.USER_NUM, userNum);
                            }
                            if (gender != null && !gender.equals("")) {
                                if(gender.equals("男")) {
                                    et.putString(StringConstant.GENDERUSR, "xb001");
                                } else if(gender.equals("女")) {
                                    et.putString(StringConstant.GENDERUSR, "xb002");
                                }
                            }

                            /**
                             * 地区的三种格式
                             * 1、行政区划\/**市\/市辖区\/**区
                             * 2、行政区划\/**特别行政区  港澳台三地区
                             * 3、行政区划\/**自治区\/通辽市  自治区地区
                             */
                            if (region != null && !region.equals("")) {
                                String[] subRegion = region.split("/");
                                if(subRegion.length > 3) {
                                    region = subRegion[1] + " " + subRegion[3];
                                } else if(subRegion.length == 3) {
                                    region = subRegion[1] + " " + subRegion[2];
                                } else {
                                    region = subRegion[1].substring(0, 2);
                                }
                                et.putString(StringConstant.REGION, region);
                            }
                            if (birthday != null && !birthday.equals("")) {
                                et.putString(StringConstant.BIRTHDAY, birthday);
                            }
                            if (age != null && !age.equals("")) {
                                et.putString(StringConstant.AGE, age);
                            }
                            if (starSign != null && !starSign.equals("")) {
                                et.putString(StringConstant.STAR_SIGN, starSign);
                            }
                            if (email != null && !email.equals("")) {
                                if(email.equals("&null")) {
                                    et.putString(StringConstant.EMAIL, "");
                                } else {
                                    et.putString(StringConstant.EMAIL, email);
                                }
                            }
                            if (userSign != null && !userSign.equals("")) {
                                if(userSign.equals("&null")) {
                                    et.putString(StringConstant.USER_SIGN, "");
                                } else {
                                    et.putString(StringConstant.USER_SIGN, userSign);
                                }
                            }
                            if (nickName != null && !nickName.equals("")) {
                                if(nickName.equals("&null")) {
                                    et.putString(StringConstant.NICK_NAME, "");
                                } else {
                                    et.putString(StringConstant.NICK_NAME, nickName);
                                }
                            }
                            if (!et.commit()) {
                                Log.v("commit", "数据 commit 失败!");
                            }
                        }
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
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

    // 设置 android app 的字体大小不受系统字体大小改变的影响
    @Override
    public Resources getResources() {
        Resources res = super.getResources();
        Configuration config = new Configuration();
        config.setToDefaults();
        res.updateConfiguration(config, res.getDisplayMetrics());
        return res;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        isCancelRequest = VolleyRequest.cancelRequest(tag);
        first = null;
        tag = null;
    }
}
