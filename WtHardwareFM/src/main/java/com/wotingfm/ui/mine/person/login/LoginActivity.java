package com.wotingfm.ui.mine.person.login;

import android.app.Dialog;
import android.content.Intent;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;

import com.android.volley.VolleyError;
import com.wotingfm.R;
import com.wotingfm.ui.baseactivity.BaseActivity;
import com.wotingfm.ui.mine.person.forgetpassword.ForgetPasswordActivity;
import com.wotingfm.ui.mine.person.register.RegisterActivity;
import com.wotingfm.common.application.BSApplication;
import com.wotingfm.common.config.GlobalConfig;
import com.wotingfm.common.constant.BroadcastConstants;
import com.wotingfm.common.constant.StringConstant;
import com.wotingfm.common.volley.VolleyCallback;
import com.wotingfm.common.volley.VolleyRequest;
import com.wotingfm.common.helper.InterPhoneControlHelper;
import com.wotingfm.util.DialogUtils;
import com.wotingfm.util.ToastUtils;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

/**
 * 登录界面
 * 作者：xinlong on 2016/11/6 21:18
 * 邮箱：645700751@qq.com
 */
public class LoginActivity extends BaseActivity implements OnClickListener {
    private Dialog dialog;          // 加载数据对话框
    private EditText editUserName;  // 输入 用户名
    private EditText editPassword;  // 输入密码

    private String userName;        // 用户名
    private String password;        // 密码
    private String tag = "LOGIN_VOLLEY_REQUEST_CANCEL_TAG";
    private String viewTag = "LoginActivity";
    private boolean isCancelRequest;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        setView();
    }

    // 初始化视图
    private void setView() {
        findViewById(R.id.head_left_btn).setOnClickListener(this);      // 返回按钮
        findViewById(R.id.tv_wjmm).setOnClickListener(this);            // 忘记密码
        findViewById(R.id.btn_login).setOnClickListener(this);          // 登录按钮
        findViewById(R.id.btn_register).setOnClickListener(this);       // 注册按钮

        editUserName = (EditText) findViewById(R.id.edittext_username); // 输入用户名
        editPassword = (EditText) findViewById(R.id.edittext_password); // 输入密码按钮

//        String phoneName = (String) SharePreferenceManager.getSharePreferenceValue(context, "USER_NAME", "USER_NAME", "");
//        editUserName.setText(phoneName);
//        editUserName.setSelection(editUserName.getText().length());
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.head_left_btn:    // 返回
                finish();
                break;
            case R.id.btn_login:        // 登录
                checkData();
                break;
            case R.id.btn_register:     // 注册
                startActivityForResult(new Intent(context, RegisterActivity.class), 0);
                break;
            case R.id.tv_wjmm:          // 忘记密码
                startActivity(new Intent(context, ForgetPasswordActivity.class));
                break;
        }
    }

    // 检查数据的正确性 检查通过则进行登录
    private void checkData() {
        userName = editUserName.getText().toString().trim();
        password = editPassword.getText().toString().trim();
        if (userName == null || userName.trim().equals("")) {
            ToastUtils.show_always(context, "登录账号不能为空");
            return;
        }
        if (password == null || password.trim().equals("")) {
            ToastUtils.show_always(context, "密码不能为空");
            return;
        }
        if (GlobalConfig.CURRENT_NETWORK_STATE_TYPE != -1) {
            dialog = DialogUtils.Dialogph(context, "登录中");
            send();
        } else {
            ToastUtils.show_always(context, "网络失败，请检查网络");
        }
    }

    // 发送登录请求
    private void send() {
        JSONObject jsonObject = VolleyRequest.getJsonObject(context);
        try {
            jsonObject.put("UserName", userName);
            jsonObject.put("Password", password);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        VolleyRequest.RequestPost(GlobalConfig.loginUrl, tag, jsonObject, new VolleyCallback() {
            @Override
            protected void requestSuccess(JSONObject result) {
                if (dialog != null) dialog.dismiss();
                if (isCancelRequest) return;
                try {
                    String ReturnType = result.getString("ReturnType");
                    if (ReturnType != null && ReturnType.equals("1001")) {
                        try {
                            JSONObject ui = (JSONObject) new JSONTokener(result.getString("UserInfo")).nextValue();
                            Editor et = BSApplication.SharedPreferences.edit();
                            et.putString(StringConstant.ISLOGIN, "true");
                            et.putString(StringConstant.PERSONREFRESHB, "true");
                            try {
                                String imageUrl = ui.getString("PortraitMini");
                                et.putString(StringConstant.IMAGEURL, imageUrl);
                            } catch (Exception e) {
                                e.printStackTrace();
                                et.putString(StringConstant.IMAGEURL, "");
                            }
//                            try {
//                                String returnUserName = ui.getString("UserName");
//                                et.putString(StringConstant.USERNAME, returnUserName);
//                            } catch (Exception e) {
//                                e.printStackTrace();
//                                et.putString(StringConstant.USERNAME, "");
//                            }
                            try {
                                String UserNum = ui.getString("UserNum");
                                et.putString(StringConstant.USER_NUM, UserNum);
                            } catch (Exception e) {
                                e.printStackTrace();
                                et.putString(StringConstant.USER_NUM, "");
                            }
                            try {
                                String imageUrlBig = ui.getString("PortraitBig");
                                et.putString(StringConstant.IMAGEURBIG, imageUrlBig);
                            } catch (Exception e) {
                                e.printStackTrace();
                                et.putString(StringConstant.IMAGEURBIG, "");
                            }
                            try {
                                String userId = ui.getString("UserId");// 用户 ID
                                et.putString(StringConstant.USERID, userId);
                            } catch (Exception e) {
                                e.printStackTrace();
                                et.putString(StringConstant.USERID, "");
                            }
                            try {
                                String phoneNumber = ui.getString("PhoneNum");
                                et.putString(StringConstant.USER_PHONE_NUMBER, phoneNumber);
                            } catch (Exception e) {
                                e.printStackTrace();
                                et.putString(StringConstant.USER_PHONE_NUMBER, "");
                            }
                            try {
                                String gender = ui.getString("Sex");// 性别
                                et.putString(StringConstant.GENDERUSR, gender);
                            } catch (Exception e) {
                                e.printStackTrace();
                                et.putString(StringConstant.GENDERUSR, "");
                            }
                            try {
                                String birthday = ui.getString("Birthday");// 生日
                                et.putString(StringConstant.BIRTHDAY, birthday);
                            } catch (Exception e) {
                                e.printStackTrace();
                                et.putString(StringConstant.BIRTHDAY, "");
                            }
                            try {
                                String region = ui.getString("Region");  // 区域

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
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                                et.putString(StringConstant.REGION, "");
                            }
                            try {
                                String age = ui.getString("Age");   // 年龄
                                et.putString(StringConstant.AGE, age);
                            } catch (Exception e) {
                                e.printStackTrace();
                                et.putString(StringConstant.AGE, "");
                            }
                            try {
                                String starSign = ui.getString("StarSign");// 星座
                                et.putString(StringConstant.STAR_SIGN, starSign);
                            } catch (Exception e) {
                                e.printStackTrace();
                                et.putString(StringConstant.STAR_SIGN, "");
                            }
                            try {
                                String email = ui.getString("Email");// 邮箱
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
                                String userSign = ui.getString("UserSign");// 签名
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
                                String nickName = ui.getString("NickName");
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

                            if (!et.commit()) {
                                Log.v("commit", "数据 commit 失败!");
                            }
                            // 更新通讯录
                            context.sendBroadcast(new Intent(BroadcastConstants.PUSH_REFRESH_LINKMAN));
                            // 更改所有界面的登录状态
                            context.sendBroadcast(new Intent(BroadcastConstants.PUSH_ALLURL_CHANGE));
                            // socket重新连接
                            InterPhoneControlHelper.sendEntryMessage(context);
                            setResult(1);
                            finish();
                        } catch (Exception e) {
                            e.printStackTrace();
                            ToastUtils.show_always(context, "登录失败，请您稍后再试");
                        }

                    } else if (ReturnType != null && ReturnType.equals("1002")) {
                        Log.i(viewTag, "1002");
                        ToastUtils.show_always(context, "您输入的用户暂未注册!");
                    } else if (ReturnType != null && ReturnType.equals("1003")) {
                        Log.i(viewTag, "1003");
                        ToastUtils.show_always(context, "您输入的密码错误!");
                    } else if (ReturnType != null && ReturnType.equals("0000")) {
                        Log.i(viewTag, "0000");
                        ToastUtils.show_always(context, "登录失败，请稍后重试!");
                    } else if (ReturnType != null && ReturnType.equals("T")) {
                        Log.i(viewTag, "T");
                        ToastUtils.show_always(context, "登录失败，请稍后重试!");
                    } else {
                        Log.i(viewTag, "Message");
                        try {
                            String Message = result.getString("Message");
                            if (Message != null && !Message.trim().equals("")) {
                                ToastUtils.show_always(context, Message + "");
                            }
                        } catch (JSONException e1) {
                            e1.printStackTrace();
                            ToastUtils.show_always(context, "登录失败，请稍后重试!");
                        }
                    }
                } catch (Exception e1) {
                    e1.printStackTrace();
                }
            }

            @Override
            protected void requestError(VolleyError error) {
                if (dialog != null) dialog.dismiss();
                ToastUtils.showVolleyError(context);
            }
        });
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case 0: // 从注册界面返回数据，注册成功
                if (resultCode == 1) {
                    setResult(1);
                    finish();
                }
                break;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        isCancelRequest = VolleyRequest.cancelRequest(tag);
        editUserName = null;
        editPassword = null;
        userName = null;
        password = null;
        dialog = null;
        tag = null;
        setContentView(R.layout.activity_null);
    }
}
