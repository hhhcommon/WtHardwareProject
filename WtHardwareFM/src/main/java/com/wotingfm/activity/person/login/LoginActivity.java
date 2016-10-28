package com.wotingfm.activity.person.login;

import android.app.Dialog;
import android.content.Intent;
import android.content.SharedPreferences.Editor;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;

import com.android.volley.VolleyError;
import com.wotingfm.R;
import com.wotingfm.activity.common.baseactivity.AppBaseActivity;
import com.wotingfm.activity.person.forgetpassword.ForgetPasswordActivity;
import com.wotingfm.activity.person.register.RegisterActivity;
import com.wotingfm.common.application.BSApplication;
import com.wotingfm.common.config.GlobalConfig;
import com.wotingfm.common.constant.BroadcastConstant;
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
 * 登录界面，暂时没有第三方登录代码
 * 作者：xinlong on 2016/8/23 21:18
 * 邮箱：645700751@qq.com
 */
public class LoginActivity extends AppBaseActivity implements OnClickListener {
    private Dialog dialog;              // 加载数据对话框
    private EditText editTextUserName;  // 输入 用户昵称或手机号
    private EditText editTextPassword;  // 输入 密码

    private String userName;            // 用户名
    private String password;            // 密码
    private String tag = "LOGIN_VOLLEY_REQUEST_CANCEL_TAG";
    private boolean isCancelRequest;

    @Override
    protected int setViewId() {
        return R.layout.activity_login;
    }

    @Override
    protected void init() {
        setTitle("登录");

        findViewById(R.id.tv_wjmm).setOnClickListener(this);                // 忘记密码
        findViewById(R.id.btn_login).setOnClickListener(this);              // 登录
        findViewById(R.id.btn_register).setOnClickListener(this);           // 注册

        editTextUserName = (EditText) findViewById(R.id.edittext_username); // 输入 用户名
        editTextPassword = (EditText) findViewById(R.id.edittext_password); // 输入 密码

        String phoneName = (String) SharePreferenceManager.getSharePreferenceValue(context, "USER_NAME", "USER_NAME", "");
        editTextUserName.setText(phoneName);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_login:    // 登录
                checkData();
                break;
            case R.id.btn_register: // 快速注册
                startActivityForResult(new Intent(context, RegisterActivity.class), 0); // 跳转到注册界面
                break;
            case R.id.tv_wjmm:      // 忘记密码
                startActivity(new Intent(context, ForgetPasswordActivity.class));       // 跳转到忘记密码界面
                break;
        }
    }

    // 验证数据正确就进行用户登录
    private void checkData() {
        // 没有网路则不需要验证直接提示用户进行网络设置
        if(GlobalConfig.CURRENT_NETWORK_STATE_TYPE == -1) {
            ToastUtils.show_always(context, "网络失败，请检查网络");
            return ;
        }

        userName = editTextUserName.getText().toString().trim();
        password = editTextPassword.getText().toString().trim();
        if (userName == null || userName.trim().equals("")) {
            ToastUtils.show_always(context, "用户名不能为空");
            return ;
        }
        if (password == null || password.trim().equals("")) {
            ToastUtils.show_always(context, "密码不能为空");
            return ;
        }

        dialog = DialogUtils.Dialogph(context, "登录中...");
        sendLoginRequest();
    }

    // 发送用户登录网络请求
    private void sendLoginRequest() {
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
                if (dialog != null) {
                    dialog.dismiss();
                }
                if (isCancelRequest) {
                    return;
                }
                try {
                    String ReturnType = result.getString("ReturnType");
                    if (ReturnType != null && ReturnType.equals("1001")) {
                        JSONObject arg1 = null;
                        String imageUrl = "";
                        String imageUrlBig = "";
                        String userId = null;
                        String returnUserName = null;
                        String phoneNumber = null;
                        try {
                            arg1 = (JSONObject) new JSONTokener(result.getString("UserInfo")).nextValue();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        if(arg1 != null) {
                            try {
                                returnUserName = arg1.getString("UserName");
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                            try {
                                userId = arg1.getString("UserId");
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                            try {
                                imageUrl = arg1.getString("PortraitMini");
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                            try {
                                imageUrlBig = arg1.getString("PortraitBig");
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                            try {
                                phoneNumber = arg1.getString("PhoneNum");
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                        // 通过 SharedPreferences 存储用户的登录信息
                        Editor et = BSApplication.SharedPreferences.edit();
                        et.putString(StringConstant.USERID, userId);            // 用户 ID
                        et.putString(StringConstant.ISLOGIN, "true");           // 用户为登录状态
                        et.putString(StringConstant.USERNAME, returnUserName);  // 用户名
                        et.putString(StringConstant.PHONENUMBER, phoneNumber);  // 用户注册时绑定的手机号码
                        et.putString(StringConstant.IMAGEURL, imageUrl);        // 用户头像 URL
                        et.putString(StringConstant.IMAGEURBIG, imageUrlBig);   // 用户大头像 URL
                        et.putString(StringConstant.PERSONREFRESHB, "true");    // 是否刷新聊天
                        if(!et.commit()) {
                            L.w("数据 commit 失败!");
                        }
                        context.sendBroadcast(new Intent(BroadcastConstant.PUSH_REFRESH_LINKMAN));//刷新通信录界面
                        context.sendBroadcast(new Intent("push_down_completed"));// 刷新下载界面
                        setResult(1);

                        SharePreferenceManager.saveBatchSharedPreference(context, "USER_NAME", "USER_NAME", userName);
                        InterPhoneControlHelper.sendEntryMessage(context);//登录后socket发送进入的请求
                        finish();
                    } else if (ReturnType != null && ReturnType.equals("1002")) {
                        ToastUtils.show_always(context, "服务器端无此用户");
                    } else if (ReturnType != null && ReturnType.equals("1003")) {
                        ToastUtils.show_always(context, "密码错误");
                    } else {
                        ToastUtils.show_always(context, "发生未知错误，请稍后重试");
                    }
                } catch (Exception e1) {
                    e1.printStackTrace();
                }
            }

            @Override
            protected void requestError(VolleyError error) {
                if (dialog != null) {
                    dialog.dismiss();
                }
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
        editTextUserName = null;
        editTextPassword = null;
        userName = null;
        password = null;
        dialog = null;
        tag = null;
    }
}
