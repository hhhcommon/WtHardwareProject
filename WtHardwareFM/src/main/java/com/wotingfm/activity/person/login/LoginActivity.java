package com.wotingfm.activity.person.login;

import android.app.Dialog;
import android.content.Intent;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;

import com.android.volley.VolleyError;
import com.wotingfm.R;
import com.wotingfm.activity.common.baseactivity.BaseActivity;
import com.wotingfm.activity.person.forgetpassword.ForgetPasswordActivity;
import com.wotingfm.activity.person.register.RegisterActivity;
import com.wotingfm.common.application.BSApplication;
import com.wotingfm.common.config.GlobalConfig;
import com.wotingfm.common.constant.BroadcastConstants;
import com.wotingfm.common.constant.StringConstant;
import com.wotingfm.common.volley.VolleyCallback;
import com.wotingfm.common.volley.VolleyRequest;
import com.wotingfm.helper.InterPhoneControlHelper;
import com.wotingfm.manager.SharePreferenceManager;
import com.wotingfm.util.DialogUtils;
import com.wotingfm.util.L;
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
    private String userId;          // 用户 ID
    private String imageUrl;
    private String imageUrlBig;
    private String returnUserName;
    private String tag = "LOGIN_VOLLEY_REQUEST_CANCEL_TAG";
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

        String phoneName = (String) SharePreferenceManager.getSharePreferenceValue(context, "USER_NAME", "USER_NAME", "");
        editUserName.setText(phoneName);
        editUserName.setSelection(editUserName.getText().length());
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
            ToastUtils.show_always(context, "用户名不能为空");
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
            private String ReturnType;
            private String Message;
            private String UserNum;
            private String phoneNumber;
            private String gender;// 性别
            private String region;// 区域
            private String birthday;// 生日
            private String age;// 年龄
            private String starSign;// 星座
            private String email;// 邮箱
            private String userSign;// 签名
            private String nickName;

            @Override
            protected void requestSuccess(JSONObject result) {
                if (dialog != null) dialog.dismiss();
                if (isCancelRequest) return;
                try {
                    ReturnType = result.getString("ReturnType");
                } catch (Exception e1) {
                    e1.printStackTrace();
                }
                try {
                    Message = result.getString("Message");
                } catch (JSONException e1) {
                    e1.printStackTrace();
                }
                if (ReturnType != null && ReturnType.equals("1001")) {
                    Editor et = BSApplication.SharedPreferences.edit();
                    et.putString(StringConstant.ISLOGIN, "true");
                    et.putString(StringConstant.PERSONREFRESHB, "true");
                    try {
                        JSONObject ui = (JSONObject) new JSONTokener(result.getString("UserInfo")).nextValue();
                        try {
                            imageUrl = ui.getString("PortraitMini");
                            et.putString(StringConstant.IMAGEURL, imageUrl);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        try {
                            returnUserName = ui.getString("UserName");
                            et.putString(StringConstant.USERNAME, returnUserName);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        try {
                            UserNum = ui.getString("UserNum");
                            et.putString(StringConstant.USER_NUM, UserNum);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        try {
                            imageUrlBig = ui.getString("PortraitBig");
                            et.putString(StringConstant.IMAGEURBIG, imageUrlBig);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        try {
                            userId = ui.getString("UserId");
                            et.putString(StringConstant.USERID, userId);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        try {
                            phoneNumber = ui.getString("PhoneNum");
                            et.putString(StringConstant.PHONENUMBER, phoneNumber);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        try {
                            gender = ui.getString("Sex");
                            et.putString(StringConstant.GENDERUSR, gender);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        try {
                            birthday = ui.getString("Birthday");
                            et.putString(StringConstant.BIRTHDAY, birthday);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        try {
                            region = ui.getString("Region");

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
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        try {
                            age = ui.getString("Age");
                            et.putString(StringConstant.AGE, age);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        try {
                            starSign = ui.getString("StarSign");
                            et.putString(StringConstant.STAR_SIGN, starSign);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        try {
                            email = ui.getString("Email");
                            if(email.equals("&null")) {
                                et.putString(StringConstant.EMAIL, "");
                            } else {
                                et.putString(StringConstant.EMAIL, email);
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        try {
                            userSign = ui.getString("UserSign");
                            if(userSign.equals("&null")) {
                                et.putString(StringConstant.USER_SIGN, "");
                            } else {
                                et.putString(StringConstant.USER_SIGN, userSign);
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        try {
                            nickName = ui.getString("NickName");
                            if(nickName.equals("&null")) {
                                et.putString(StringConstant.NICK_NAME, "");
                            } else {
                                et.putString(StringConstant.NICK_NAME, nickName);
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        if (!et.commit()) L.v("数据 commit 失败!");
                        context.sendBroadcast(new Intent(BroadcastConstants.PUSH_REFRESH_LINKMAN));
                        context.sendBroadcast(new Intent(BroadcastConstants.PUSH_DOWN_COMPLETED));// 刷新下载界面
                        String phoneName = editUserName.getText().toString().trim();
                        SharePreferenceManager.saveBatchSharedPreference(context, "USER_NAME", "USER_NAME", phoneName);
                        InterPhoneControlHelper.sendEntryMessage(context);// 登录后socket发送进入的请求
                        setResult(1);
                        finish();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else if (ReturnType != null && ReturnType.equals("1002")) {
                    ToastUtils.show_always(context, "您输入的用户暂未注册!");
                } else if (ReturnType != null && ReturnType.equals("1003")) {
                    ToastUtils.show_always(context, "您输入的密码错误!");
                } else {
                    if (Message != null && !Message.trim().equals("")) {
                        ToastUtils.show_always(context, Message + "");
                    }
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
                    ToastUtils.show_always(context, "账号注册成功，已进行自动登录!");
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
        userId = null;
        imageUrl = null;
        imageUrlBig = null;
        returnUserName = null;
        tag = null;
        setContentView(R.layout.activity_null);
    }
}
