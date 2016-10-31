package com.wotingfm.activity.person.register;

import android.app.Dialog;
import android.content.Intent;
import android.content.SharedPreferences.Editor;
import android.os.CountDownTimer;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.VolleyError;
import com.wotingfm.R;
import com.wotingfm.activity.common.baseactivity.AppBaseActivity;
import com.wotingfm.activity.person.agreement.AgreementActivity;
import com.wotingfm.common.application.BSApplication;
import com.wotingfm.common.config.GlobalConfig;
import com.wotingfm.common.constant.BroadcastConstant;
import com.wotingfm.common.constant.StringConstant;
import com.wotingfm.common.volley.VolleyCallback;
import com.wotingfm.common.volley.VolleyRequest;
import com.wotingfm.helper.InterPhoneControlHelper;
import com.wotingfm.util.DialogUtils;
import com.wotingfm.util.L;
import com.wotingfm.util.ToastUtils;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * 注册
 * 作者：xinlong on 2016/8/23 21:18
 * 邮箱：645700751@qq.com
 */
public class RegisterActivity extends AppBaseActivity implements OnClickListener {

    private CountDownTimer mCountDownTimer; // 验证码计时器

    private Dialog dialog;                  // 加载数据对话框
    private EditText mEditTextUserPhone;    // 手机号
    private EditText mEditTextName;         // 用户名
    private EditText mEditTextPassWord;     // 密码
    private EditText mEditTextPassWordT;    // 确认密码
    private EditText editYzm;               // 验证码
    private TextView textGetYzm;            // 获取验证码
    private TextView textCxFaSong;          // 重新发送验证码
    private TextView textNext;              // 不可点击的注册按钮
    private TextView mTextRegister;         // 注册

    private String phoneNumVerify;          // 手机号
    private String phoneNum;                // 手机号
    private String userName;                // 用户名
    private String password;                // 密码
    private String passwordT;               // 确认密码
    private String verificationCode;        // 验证码
    private String tempVerify;
    private String tag = "REGISTER_VOLLEY_REQUEST_CANCEL_TAG";

    private int sendType = -1;              // == -1 发送验证码  == 其他 再次发送验证码
    private int verifyStatus = -1;          // == -1 没有为此手机号发送过验证码 == 1 表示成功
    private boolean isCancelRequest;

    @Override
    protected int setViewId() {
        return R.layout.activity_register;
    }

    @Override
    protected void init() {
        setTitle("注册");

        mEditTextUserPhone = (EditText) findViewById(R.id.edittext_userphone);  // 输入 手机号
        mEditTextUserPhone.addTextChangedListener(new MyEditListener());

        mEditTextName = (EditText) findViewById(R.id.edittext_username);        // 输入 用户名
        mEditTextName.addTextChangedListener(new MyEditListener());

        mEditTextPassWord = (EditText) findViewById(R.id.edittext_password);    // 输入 密码
        mEditTextPassWord.addTextChangedListener(new MyEditListener());

        mEditTextPassWordT = (EditText) findViewById(R.id.edittext_passwordT);  // 输入 确认密码
        mEditTextPassWordT.addTextChangedListener(new MyEditListener());

        editYzm = (EditText) findViewById(R.id.et_yzm);                         // 输入 验证码
        editYzm.addTextChangedListener(new MyEditListener());

        textGetYzm = (TextView) findViewById(R.id.tv_getyzm);                   // 获取验证码
        textGetYzm.setOnClickListener(this);

        textCxFaSong = (TextView) findViewById(R.id.tv_cxfasong);               // 重新发送验证码
        textNext = (TextView) findViewById(R.id.tv_next);                       // 不可点击注册按钮

        mTextRegister = (TextView) findViewById(R.id.tv_register);              // 注册
        mTextRegister.setOnClickListener(this);

        LinearLayout agreement = (LinearLayout) findViewById(R.id.lin_agreement);// 注册协议
        agreement.setOnClickListener(this);

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.tv_register:          // 验证数据
                // 以下操作都需要网络 所以没有网络就不需要继续验证直接提示用户设置网络
                if (GlobalConfig.CURRENT_NETWORK_STATE_TYPE == -1) {
                    ToastUtils.show_always(context, "网络失败，请检查网络");
                    return;
                }
                checkValue();
                break;
            case R.id.tv_getyzm:            // 检查手机号是否正确
                // 以下操作都需要网络 所以没有网络就不需要继续验证直接提示用户设置网络
                if (GlobalConfig.CURRENT_NETWORK_STATE_TYPE == -1) {
                    ToastUtils.show_always(context, "网络失败，请检查网络");
                    return;
                }
                checkYzm();
                break;
            case R.id.lin_agreement:        // 注册协议
                startActivity(new Intent(context, AgreementActivity.class));
                break;
        }
    }

    // 判断数据是否填写完整
    private boolean isComplete(int type) {
        phoneNum = mEditTextUserPhone.getText().toString().trim();              // 手机号
        userName = mEditTextName.getText().toString().trim();                   // 用户名
        password = mEditTextPassWord.getText().toString().trim();               // 密码
        passwordT = mEditTextPassWordT.getText().toString().trim();             // 确认密码
        verificationCode = editYzm.getText().toString().trim();                 // 验证码

        if ("".equalsIgnoreCase(phoneNum) || phoneNum.length() != 11) {
            if (type == 1) ToastUtils.show_always(context, "请输入正确的手机号码!");
            return false;
        } else if (!phoneNum.equals(phoneNumVerify)) {
            if (type == 1) ToastUtils.show_always(context, "请输入您之前获取验证的手机号码");
            return false;
        } else if (userName == null || userName.trim().equals("")) {
            if (type == 1) Toast.makeText(context, "用户名不能为空", Toast.LENGTH_SHORT).show();
            return false;
        } else if (userName.length() < 3) {
            if (type == 1) Toast.makeText(context, "请输入三位以上用户名", Toast.LENGTH_SHORT).show();
            return false;
        } else if (password == null || password.trim().equals("")) {
            if (type == 1) Toast.makeText(context, "密码不能为空", Toast.LENGTH_SHORT).show();
            return false;
        } else if (password.length() < 6) {
            if (type == 1) Toast.makeText(context, "请输入六位以上密码", Toast.LENGTH_SHORT).show();
            return false;
        } else if (passwordT == null || passwordT.trim().equals("")) {
            if (type == 1) Toast.makeText(context, "密码不能为空", Toast.LENGTH_SHORT).show();
            return false;
        } else if (!passwordT.equals(password)) {
            if (type == 1) Toast.makeText(context, "密码跟确认密码不匹配", Toast.LENGTH_SHORT).show();
            return false;
        } else if ("".equalsIgnoreCase(verificationCode) || verificationCode.length() != 6) {
            if (type == 1) ToastUtils.show_always(context, "验证码不正确!");
            return false;
        } else {
            return true;
        }
    }

    // 检查数据的正确性
    private void checkValue() {
        if (isComplete(1)) {
            dialog = DialogUtils.Dialogph(context, "正在验证手机号");
            sendRequest();// 验证验证码，成功后会执行注册程序
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
                    if (ReturnType != null && ReturnType.equals("1001")) {
                        if (GlobalConfig.CURRENT_NETWORK_STATE_TYPE != -1) {
                            dialog = DialogUtils.Dialogph(context, "注册中...");
                            send();
                        } else {
                            ToastUtils.show_always(context, "网络失败，请检查网络");
                        }
                    } else if (ReturnType != null && ReturnType.equals("T")) {
                        ToastUtils.show_always(context, "数据出错了,请稍后再试");
                    } else if (ReturnType != null && ReturnType.equals("1002")) {
                        ToastUtils.show_always(context, "验证码不匹配");
                    } else {
                        try {
                            Message = result.getString("Message");
                            if (Message != null && !Message.trim().equals("")) {
                                ToastUtils.show_always(context, Message + "");
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
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

    // 验证手机号码获取验证码
    private void checkYzm() {
        phoneNumVerify = mEditTextUserPhone.getText().toString().trim();
        // 判断获取到的验证码是否是跟注册账号为同一个账号
        if (tempVerify == null) {
            tempVerify = phoneNumVerify;
        } else {
            if (!tempVerify.equals(phoneNumVerify)) {
                sendType = -1;
                tempVerify = phoneNumVerify;
            }
        }
        if ("".equalsIgnoreCase(phoneNumVerify)) {
            ToastUtils.show_always(context, "手机号码不能为空");
            return;
        }
//        if (!isMobile(phoneNumVerify)) {
//            ToastUtils.show_always(context, "请您输入正确的手机号");
//            return;
//        }
        if (phoneNumVerify.length() != 11) {
            ToastUtils.show_always(context, "请您输入正确的手机号");
            return;
        }
        dialog = DialogUtils.Dialogph(context, "正在验证手机号");
        getVerifyCode();
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
            if (sendType != -1) {
                jsonObject.put("OperType", 1);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        VolleyRequest.RequestPost(url, tag, jsonObject, new VolleyCallback() {

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
                        ToastUtils.show_always(context, "验证码已经发送");
                        if (mCountDownTimer != null) {
                            mCountDownTimer.cancel();
                            mCountDownTimer = null;
                        }
                        timerDown();// 每秒减1
                        sendType = 2;
                        verifyStatus = 1;
                        textGetYzm.setVisibility(View.GONE);
                        textCxFaSong.setVisibility(View.VISIBLE);
                    } else if (ReturnType != null && ReturnType.equals("T")) {
                        ToastUtils.show_always(context, "数据出错了,请稍后再试");
                    } else if (ReturnType != null && ReturnType.equals("1002")) {
                        ToastUtils.show_always(context, "此号码已经注册!");
                    } else {
                        try {
                            String Message = result.getString("Message");
                            if (Message != null && !Message.trim().equals("")) {
                                ToastUtils.show_always(context, Message + "");
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
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

    // 发送注册请求
    private void send() {
        JSONObject jsonObject = VolleyRequest.getJsonObject(context);
        try {
            jsonObject.put("UserName", userName);
            jsonObject.put("UsePhone", "1");
            jsonObject.put("Password", password);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        VolleyRequest.RequestPost(GlobalConfig.registerUrl, tag, jsonObject, new VolleyCallback() {
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
                        // 通过 sharedPreference 存储用户的登录信息
                        try {
                            String userId = result.getString("UserId");
                            Editor et = BSApplication.SharedPreferences.edit();
                            et.putString(StringConstant.USERID, userId);
                            et.putString(StringConstant.ISLOGIN, "true");
                            et.putString(StringConstant.USERNAME, userName);
                            et.putString(StringConstant.PHONENUMBER, phoneNum);
                            et.putString(StringConstant.PERSONREFRESHB, "true");// 刷新通信录
                            if (!et.commit()) {
                                L.w("数据 commit 失败!");
                            }
                            sendBroadcast(new Intent(BroadcastConstant.PUSH_REFRESH_LINKMAN));// 刷新通信录界面
                            InterPhoneControlHelper.sendEntryMessage(context);// 登录后socket发送进入的请求
                            setResult(1);// 返回结果数据到上一个界面
                            finish();
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    } else if (ReturnType != null && ReturnType.equals("1002")) {
                        ToastUtils.show_always(context, "用户名第一个字符不能是数字,无法注册!");
                    } else if (ReturnType != null && ReturnType.equals("1003")) {
                        ToastUtils.show_always(context, "您输入的用户名已存在");
                    } else {
                        ToastUtils.show_always(context, "数据出错了,请稍后再试");
                    }
                } catch (Exception e) {
                    e.printStackTrace();
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

    // 再次获取验证码时间
    private void timerDown() {
        mCountDownTimer = new CountDownTimer(60000, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                textCxFaSong.setText(millisUntilFinished / 1000 + "s后重新发送");
            }

            @Override
            public void onFinish() {
                textCxFaSong.setVisibility(View.INVISIBLE);
                textGetYzm.setVisibility(View.VISIBLE);
            }
        }.start();
    }

    // 验证手机号的方法，为了避免手机格式的匹配不完全，取消前端的正则匹配，交由后端处理，前端只处理字节是否为11位
//    private boolean isMobile(String str) {
//        Pattern pattern = Pattern.compile("^[1][3,4,5,7,8][0-9]{9}$"); // 验证手机号格式
//        Matcher matcher = pattern.matcher(str);
//        return matcher.matches();
//    }

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
            if (isComplete(2)) {
                if (verifyStatus == 1) {
                    textNext.setVisibility(View.GONE);
                    mTextRegister.setVisibility(View.VISIBLE);
                } else {
                    ToastUtils.show_always(context, "请点击获取验证码，获取验证码信息");
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
        mEditTextPassWordT = null;
        passwordT = null;
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
