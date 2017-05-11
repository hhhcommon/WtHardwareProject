package com.wotingfm.ui.common.splash;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;

import com.android.volley.VolleyError;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.wotingfm.common.application.BSApplication;
import com.wotingfm.common.config.GlobalConfig;
import com.wotingfm.common.constant.StringConstant;
import com.wotingfm.common.volley.VolleyCallback;
import com.wotingfm.common.volley.VolleyRequest;
import com.wotingfm.ui.common.model.UserInfo;
import com.wotingfm.ui.common.welcome.activity.WelcomeActivity;
import com.wotingfm.ui.main.MainActivity;
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
        JSONObject jsonObject = VolleyRequest.getJsonObject(this);
        VolleyRequest.RequestPost(GlobalConfig.splashUrl, tag, jsonObject, new VolleyCallback() {
            @Override
            protected void requestSuccess(JSONObject result) {
                if (isCancelRequest) return;
                try {
                    String ReturnType = result.getString("ReturnType");
                    if (ReturnType != null && ReturnType.equals("1001")) {
                        try {
                            String UserInfo = result.getString("UserInfo");
                            if (UserInfo != null && !UserInfo.trim().equals("")) {
                                try {
                                    UserInfo list = new Gson().fromJson(UserInfo, new TypeToken<UserInfo>() {
                                    }.getType());
                                    Editor et = BSApplication.SharedPreferences.edit();

                                    try {
                                        String userId = list.getUserId();// ID
                                        if (userId != null && !userId.equals("")) {
                                            et.putString(StringConstant.USERID, userId);
                                        } else {
                                            et.putString(StringConstant.USERID, "");
                                        }
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                        et.putString(StringConstant.USERID, "");
                                    }
                                    // 没有这个字段
//                                    try {
//                                        String userName = list.getUserName();// 用户名
//                                        if (userName != null && !userName.equals("")) {
//                                            et.putString(StringConstant.USERNAME, userName);
//                                        } else {
//                                            et.putString(StringConstant.USERNAME, "");
//                                        }
//                                    } catch (Exception e) {
//                                        e.printStackTrace();
//                                        et.putString(StringConstant.USERNAME, "");
//                                    }
                                    try {
                                        String imageUrl = list.getPortraitMini();// 用户头像
                                        if (imageUrl != null && !imageUrl.equals("")) {
                                            et.putString(StringConstant.IMAGEURL, imageUrl);
                                        } else {
                                            et.putString(StringConstant.IMAGEURL, "");
                                        }
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                        et.putString(StringConstant.IMAGEURL, "");
                                    }
                                    try {
                                        String imageUrlBig = list.getPortraitBig();// 用户大头像
                                        if (imageUrlBig != null && !imageUrlBig.equals("")) {
                                            et.putString(StringConstant.IMAGEURBIG, imageUrlBig);
                                        } else {
                                            et.putString(StringConstant.IMAGEURBIG, "");
                                        }
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                        et.putString(StringConstant.IMAGEURBIG, "");
                                    }
                                    try {
                                        String userNum = list.getUserNum();// 用户号
                                        if (userNum != null && !userNum.equals("")) {
                                            et.putString(StringConstant.USER_NUM, userNum);
                                        } else {
                                            et.putString(StringConstant.USER_NUM, "");
                                        }
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                        et.putString(StringConstant.USER_NUM, "");
                                    }
                                    try {
                                        String gender = list.getSex();// 性别
                                        if (gender != null && !gender.equals("")) {
                                            if (gender.equals("男")) {
                                                et.putString(StringConstant.GENDERUSR, "xb001");
                                            } else if (gender.equals("女")) {
                                                et.putString(StringConstant.GENDERUSR, "xb002");
                                            }
                                        } else {
                                            et.putString(StringConstant.REGION, "");
                                        }
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                        et.putString(StringConstant.REGION, "");
                                    }
                                    try {
                                        String region = list.getRegion();// 区域
                                        /**
                                         * 地区的三种格式
                                         * 1、行政区划\/**市\/市辖区\/**区
                                         * 2、行政区划\/**特别行政区  港澳台三地区
                                         * 3、行政区划\/**自治区\/通辽市  自治区地区
                                         */
                                        if (region != null && !region.equals("")) {
                                            String[] subRegion = region.split("/");
                                            if (subRegion.length > 3) {
                                                region = subRegion[1] + " " + subRegion[3];
                                            } else if (subRegion.length == 3) {
                                                region = subRegion[1] + " " + subRegion[2];
                                            } else {
                                                region = subRegion[1].substring(0, 2);
                                            }
                                            et.putString(StringConstant.REGION, region);
                                        } else {
                                            et.putString(StringConstant.REGION, "");
                                        }
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                        et.putString(StringConstant.REGION, "");
                                    }
                                    try {
                                        String birthday = list.getBirthday();// 生日
                                        if (birthday != null && !birthday.equals("")) {
                                            et.putString(StringConstant.BIRTHDAY, birthday);
                                        } else {
                                            et.putString(StringConstant.BIRTHDAY, "");
                                        }
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                        et.putString(StringConstant.BIRTHDAY, "");
                                    }
                                    try {
                                        String age = list.getAge();// 年龄
                                        if (age != null && !age.equals("")) {
                                            et.putString(StringConstant.AGE, age);
                                        } else {
                                            et.putString(StringConstant.AGE, "");
                                        }
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                        et.putString(StringConstant.AGE, "");
                                    }
                                    try {
                                        String starSign = list.getStarSign();// 星座
                                        if (starSign != null && !starSign.equals("")) {
                                            et.putString(StringConstant.STAR_SIGN, starSign);
                                        } else {
                                            et.putString(StringConstant.STAR_SIGN, "");
                                        }
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                        et.putString(StringConstant.STAR_SIGN, "");
                                    }
                                    try {
                                        String email = list.getEmail();// 邮箱
                                        if (email != null && !email.equals("")) {
                                            if (email.equals("&null")) {
                                                et.putString(StringConstant.EMAIL, "");
                                            } else {
                                                et.putString(StringConstant.EMAIL, email);
                                            }
                                        } else {
                                            et.putString(StringConstant.EMAIL, "");
                                        }
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                        et.putString(StringConstant.EMAIL, "");
                                    }
                                    try {
                                        String userSign = list.getUserSign();// 签名
                                        if (userSign != null && !userSign.equals("")) {
                                            if (userSign.equals("&null")) {
                                                et.putString(StringConstant.USER_SIGN, "");
                                            } else {
                                                et.putString(StringConstant.USER_SIGN, userSign);
                                            }
                                        } else {
                                            et.putString(StringConstant.USER_SIGN, "");
                                        }
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                        et.putString(StringConstant.USER_SIGN, "");
                                    }
                                    try {
                                        String nickName = list.getNickName();
                                        if (nickName != null && !nickName.equals("")) {
                                            if (nickName.equals("&null")) {
                                                et.putString(StringConstant.NICK_NAME, "");
                                            } else {
                                                et.putString(StringConstant.NICK_NAME, nickName);
                                            }
                                        } else {
                                            et.putString(StringConstant.NICK_NAME, "");
                                        }
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                        et.putString(StringConstant.NICK_NAME, "");
                                    }
                                    et.putString(StringConstant.ISLOGIN, "true");
                                    if (!et.commit()) {
                                        Log.v("commit", "数据 commit 失败!");
                                    }
                                } catch (Exception e) {
                                    e.printStackTrace();
                                    unRegisterLogin();
                                }
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                            unRegisterLogin();
                        }
                    } else {
                        unRegisterLogin();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    unRegisterLogin();
                }

                if (first != null && first.equals("1")) {
                    startActivity(new Intent(SplashActivity.this, MainActivity.class));       // 跳转到主页
                } else {
                    startActivity(new Intent(SplashActivity.this, WelcomeActivity.class));    // 跳转到引导页
                }
                // overridePendingTransition(R.anim.wt_fade, R.anim.wt_hold);
                // overridePendingTransition(R.anim.wt_zoom_enter, R.anim.wt_zoom_exit);
                finish();
            }

            @Override
            protected void requestError(VolleyError error) {
                if (first != null && first.equals("1")) {
                    startActivity(new Intent(SplashActivity.this, MainActivity.class));       // 跳转到主页
                } else {
                    startActivity(new Intent(SplashActivity.this, WelcomeActivity.class));    // 跳转到引导页
                }
                finish();
            }
        });
    }

    // 更改一下登录状态
    private void unRegisterLogin() {
        SharedPreferences.Editor et = BSApplication.SharedPreferences.edit();
        et.putString(StringConstant.ISLOGIN, "false");
        et.putString(StringConstant.USERID, "");
        et.putString(StringConstant.USER_NUM, "");
        et.putString(StringConstant.IMAGEURL, "");
        et.putString(StringConstant.USER_PHONE_NUMBER, "");
        et.putString(StringConstant.USER_NUM, "");
        et.putString(StringConstant.GENDERUSR, "");
        et.putString(StringConstant.EMAIL, "");
        et.putString(StringConstant.REGION, "");
        et.putString(StringConstant.BIRTHDAY, "");
        et.putString(StringConstant.USER_SIGN, "");
        et.putString(StringConstant.STAR_SIGN, "");
        et.putString(StringConstant.AGE, "");
        et.putString(StringConstant.NICK_NAME, "");
        if (!et.commit()) {
            Log.v("commit", "数据 commit 失败!");
        }
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
