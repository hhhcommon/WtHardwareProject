package com.wotingfm.activity.person.register.activity;

import android.app.Dialog;
import android.content.Intent;
import android.content.SharedPreferences.Editor;
import android.os.CountDownTimer;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.VolleyError;
import com.wotingfm.R;
import com.wotingfm.activity.common.baseactivity.AppBaseActivity;
import com.wotingfm.common.application.BSApplication;
import com.wotingfm.common.config.GlobalConfig;
import com.wotingfm.common.constant.StringConstant;
import com.wotingfm.common.volley.VolleyCallback;
import com.wotingfm.common.volley.VolleyRequest;
import com.wotingfm.helper.InterPhoneControlHelper;
import com.wotingfm.util.DialogUtils;
import com.wotingfm.util.L;
import com.wotingfm.util.ToastUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 注册
 * @author 辛龙
 *         2016年8月8日
 */
public class RegisterActivity extends AppBaseActivity implements OnClickListener {
    private CountDownTimer mCountDownTimer; // 验证码计时器

    private Dialog dialog;                  // 加载数据对话框
    private EditText mEditTextUserPhone;    // 手机号
    private EditText mEditTextName;         // 用户名
    private EditText mEditTextPassWord;     // 密码
    private EditText editYzm;               // 验证码
    private TextView textGetYzm;            // 获取验证码
    private TextView textCxFaSong;          // 重新发送验证码
    private TextView textNext;              // 不可点击的注册按钮
    private TextView mTextRegister;         // 注册

    private String phoneNumVerify;          // 手机号
    private String phoneNum;                // 手机号
    private String userName;                // 用户名
    private String password;                // 密码
    private String verificationCode;        // 验证码
    private String tempVerify;
    private String tag = "REGISTER_VOLLEY_REQUEST_CANCEL_TAG";

    private int sendType = -1;              // == -1 发送验证码  == 其他 再次发送验证码
    private int verifyStatus = -1;          // == -1 没有为此手机号发送过验证码 == 1 表示成功
    private boolean isCancelRequest;

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.tv_register:          // 验证数据
                checkValue();
                break;
            case R.id.tv_getyzm:            // 检查手机号是否正确
                checkYzm();
                break;
        }
    }

    @Override
    protected int setViewId() {
        return R.layout.activity_register;
    }

    @Override
    protected void init() {
        setTitle("注册");

        mEditTextUserPhone = (EditText) findViewById(R.id.edittext_userphone);  // 输入 手机号
        mEditTextName = (EditText) findViewById(R.id.edittext_username);        // 输入 用户名
        mEditTextPassWord = (EditText) findViewById(R.id.edittext_password);    // 输入 密码

        editYzm = (EditText) findViewById(R.id.et_yzm);                         // 输入 验证码
        editYzm.addTextChangedListener(new MyEditListener());

        textGetYzm = (TextView) findViewById(R.id.tv_getyzm);                   // 获取验证码
        textGetYzm.setOnClickListener(this);

        textCxFaSong = (TextView) findViewById(R.id.tv_cxfasong);               // 重新发送验证码
        textNext = (TextView) findViewById(R.id.tv_next);                       // 不可点击注册按钮

        mTextRegister = (TextView) findViewById(R.id.tv_register);              // 注册
        mTextRegister.setOnClickListener(this);
    }

    // 检查数据的正确性
    private void checkValue() {
        phoneNum = mEditTextUserPhone.getText().toString().trim();              // 手机号
        userName = mEditTextName.getText().toString().trim();                   // 用户名
        password = mEditTextPassWord.getText().toString().trim();               // 密码
        verificationCode = editYzm.getText().toString().trim();                 // 验证码
        if ("".equalsIgnoreCase(phoneNum) || !isMobile(phoneNum)) {
            ToastUtils.show_allways(context, "请输入正确的手机号码!");
            return;
        }
        if (!phoneNum.equals(phoneNumVerify)) {
            ToastUtils.show_allways(context, "请输入您之前获取验证的手机号码");
            return;
        }
        if ("".equalsIgnoreCase(verificationCode) || verificationCode.length() != 6) {
            ToastUtils.show_allways(context, "验证码不正确!");
            return;
        }
        if (userName == null || userName.trim().equals("")) {
            Toast.makeText(context, "用户名不能为空", Toast.LENGTH_SHORT).show();
            return;
        }
        if (userName.length() < 3) {
            Toast.makeText(context, "请输入三位以上用户名", Toast.LENGTH_SHORT).show();
            return;
        }
        if (password == null || password.trim().equals("")) {
            Toast.makeText(context, "密码不能为空", Toast.LENGTH_SHORT).show();
            return;
        }
        if (password.length() < 6) {
            Toast.makeText(context, "请输入六位以上密码", Toast.LENGTH_SHORT).show();
            return;
        }
        if (GlobalConfig.CURRENT_NETWORK_STATE_TYPE != -1) {
            dialog = DialogUtils.Dialogph(context, "正在验证手机号");
            sendRequest();
        } else {
            ToastUtils.show_allways(context, "网络失败，请检查网络");
        }
    }

    // 发送网络请求
    private void sendRequest() {
        JSONObject jsonObject = VolleyRequest.getJsonObject(context);
        try {
            jsonObject.put("PhoneNum", phoneNum);
            jsonObject.put("CheckCode", verificationCode);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        VolleyRequest.RequestPost(GlobalConfig.checkPhoneCheckCodeUrl, tag, jsonObject, new VolleyCallback() {
            private String ReturnType;
            private String Message;
//            private String UserId;

            @Override
            protected void requestSuccess(JSONObject result) {
                if (dialog != null) {
                    dialog.dismiss();
                }
                if (isCancelRequest) {
                    return;
                }
                try {
                    ReturnType = result.getString("ReturnType");
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                try {
                    Message = result.getString("Message");
                } catch (JSONException e) {
                    e.printStackTrace();
                }
//                try {
//                    UserId = result.getString("UserId");
//                } catch (JSONException e) {
//                    e.printStackTrace();
//                }
                if (ReturnType != null && ReturnType.equals("1001")) {
                    if (GlobalConfig.CURRENT_NETWORK_STATE_TYPE != -1) {
                        dialog = DialogUtils.Dialogph(context, "注册中...");
                        send();
                    } else {
                        ToastUtils.show_allways(context, "网络失败，请检查网络");
                    }
                } else if (ReturnType != null && ReturnType.equals("T")) {
                    ToastUtils.show_allways(context, "异常返回值");
                } else if (ReturnType != null && ReturnType.equals("1002")) {
                    ToastUtils.show_allways(context, "验证码不匹配");
                } else {
                    if (Message != null && !Message.trim().equals("")) {
                        ToastUtils.show_allways(context, Message + "");
                    }
                }
            }

            @Override
            protected void requestError(VolleyError error) {
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });
    }

    // 验证手机号码获取验证码
    private void checkYzm() {
        phoneNumVerify = mEditTextUserPhone.getText().toString().trim();
        if (tempVerify == null) {
            tempVerify = phoneNumVerify;
        } else {
            if (!tempVerify.equals(phoneNumVerify)) {
                sendType = -1;
                tempVerify = phoneNumVerify;
            }
        }
        if ("".equalsIgnoreCase(phoneNumVerify)) {
            ToastUtils.show_allways(this, "手机号码不能为空");
            return;
        }
        if (!isMobile(phoneNumVerify)) {
            ToastUtils.show_allways(this, "请您输入正确的手机号");
            return;
        }
        if (GlobalConfig.CURRENT_NETWORK_STATE_TYPE != -1) {
            dialog = DialogUtils.Dialogph(context, "正在验证手机号");
            getVerifyCode();
        } else {
            ToastUtils.show_allways(context, "网络失败，请检查网络");
        }
    }

    // 获取验证码
    private void getVerifyCode() {
        String url;
        if (sendType == -1) {// 第一次获取验证码
            url = GlobalConfig.registerByPhoneNumUrl;
        } else {// 再次获取验证码
            url = GlobalConfig.reSendPhoneCheckCodeNumUrl;
        }
        JSONObject jsonObject = VolleyRequest.getJsonObject(context);
        try {
            jsonObject.put("PhoneNum", phoneNumVerify);
            if (sendType == -1) {
                jsonObject.put("OperType", 1);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        VolleyRequest.RequestPost(url, tag, jsonObject, new VolleyCallback() {
            private String ReturnType;
            private String Message;

            @Override
            protected void requestSuccess(JSONObject result) {
                if (dialog != null) {
                    dialog.dismiss();
                }
                if (isCancelRequest) {
                    return;
                }
                try {
                    ReturnType = result.getString("ReturnType");
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                try {
                    Message = result.getString("Message");
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                if (ReturnType != null && ReturnType.equals("1001")) {
                    ToastUtils.show_allways(context, "验证码已经发送");
                    timerDown();// 每秒减1
                    sendType = 2;
                    verifyStatus = 1;
                    textGetYzm.setVisibility(View.GONE);
                    textCxFaSong.setVisibility(View.VISIBLE);
                } else if (ReturnType != null && ReturnType.equals("T")) {
                    ToastUtils.show_allways(context, "获取验证码发生异常，请稍后重试!");
                } else if (ReturnType != null && ReturnType.equals("1002")) {
                    ToastUtils.show_allways(context, "此号码已经注册!");
                } else {
                    if (Message != null && !Message.trim().equals("")) {
                        ToastUtils.show_allways(context, Message + "");
                    }
                }
            }

            @Override
            protected void requestError(VolleyError error) {
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });
    }

    // 发送注册请求
    private void send() {
        JSONObject jsonObject = VolleyRequest.getJsonObject(context);
        try {
            jsonObject.put("UserName", userName);
            jsonObject.put("Password", password);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        VolleyRequest.RequestPost(GlobalConfig.registerUrl, tag, jsonObject, new VolleyCallback() {
            private String ReturnType;
            private String Message;
            private String userId;

            @Override
            protected void requestSuccess(JSONObject result) {
                if (dialog != null) {
                    dialog.dismiss();
                }
                if (isCancelRequest) {
                    return;
                }
                try {
                    ReturnType = result.getString("ReturnType");
                } catch (Exception e2) {
                    e2.printStackTrace();
                }
                try {
                    Message = result.getString("Message");
                    L.v("Message -- > > " + Message);
                } catch (JSONException e1) {
                    e1.printStackTrace();
                }
                try {
                    userId = result.getString("UserId");
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                if (ReturnType != null && ReturnType.equals("1001")) {
                    // 通过sharedPreference存储用户的登录信息
                    Editor et = BSApplication.SharedPreferences.edit();
                    et.putString(StringConstant.USERID, userId);
                    et.putString(StringConstant.ISLOGIN, "true");
                    et.putString(StringConstant.USERNAME, userName);
                    et.putString(StringConstant.PHONENUMBER, phoneNum);
                    et.putString(StringConstant.PERSONREFRESHB, "true");
                    et.commit();
                    Intent pushIntent = new Intent("push_refreshlinkman");
                    sendBroadcast(pushIntent);
                    InterPhoneControlHelper.sendEntryMessage(context);
                    setResult(1);
                    finish();
                } else if (ReturnType != null && ReturnType.equals("1002")) {
                    ToastUtils.show_allways(context, "服务器端无此用户!");
                } else if (ReturnType != null && ReturnType.equals("1003")) {
                    ToastUtils.show_allways(context, "用户名重复!");
                } else {
                    ToastUtils.show_allways(context, "发生未知错误，请稍后重试");
                }
            }

            @Override
            protected void requestError(VolleyError error) {
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });
    }

    // 再次获取验证码时间
    private void timerDown() {
        mCountDownTimer = new CountDownTimer(60000, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                textCxFaSong.setText(millisUntilFinished / 1000 + "s后重新发送");
            }

            @Override
            public void onFinish() {
                textCxFaSong.setVisibility(View.GONE);
                textGetYzm.setVisibility(View.VISIBLE);
            }
        }.start();
    }

    // 验证手机号的方法
    private boolean isMobile(String str) {
        Pattern pattern = Pattern.compile("^[1][3,4,5,7,8][0-9]{9}$"); // 验证手机号格式
        Matcher matcher = pattern.matcher(str);
        return matcher.matches();
    }

    // 输入框监听
    class MyEditListener implements TextWatcher {

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
        }

        @Override
        public void afterTextChanged(Editable s) {
            if (s.length() == 6 && phoneNumVerify != null && !phoneNumVerify.equals("")) {
                if (verifyStatus == 1) {
                    textNext.setVisibility(View.GONE);
                    mTextRegister.setVisibility(View.VISIBLE);
                } else {
                    ToastUtils.show_allways(context, "请点击获取验证码，获取验证码信息");
                }
            } else {
                mTextRegister.setVisibility(View.GONE);
                textNext.setVisibility(View.VISIBLE);
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        isCancelRequest = VolleyRequest.cancelRequest(tag);
        if (mCountDownTimer != null) {
            mCountDownTimer.cancel();
            mCountDownTimer = null;
        }
        mEditTextName = null;
        mEditTextPassWord = null;
        password = null;
        userName = null;
        context = null;
        dialog = null;
        mTextRegister = null;
        phoneNum = null;
        mEditTextUserPhone = null;
        editYzm = null;
        textGetYzm = null;
        verificationCode = null;
        textCxFaSong = null;
        textNext = null;
        phoneNumVerify = null;
        tempVerify = null;
        tag = null;
    }
}
