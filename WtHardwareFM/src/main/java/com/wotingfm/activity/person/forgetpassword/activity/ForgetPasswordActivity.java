package com.wotingfm.activity.person.forgetpassword.activity;

import android.app.Dialog;
import android.os.CountDownTimer;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.TextView;

import com.android.volley.VolleyError;
import com.wotingfm.R;
import com.wotingfm.activity.common.baseactivity.AppBaseActivity;
import com.wotingfm.common.config.GlobalConfig;
import com.wotingfm.common.volley.VolleyCallback;
import com.wotingfm.common.volley.VolleyRequest;
import com.wotingfm.util.DialogUtils;
import com.wotingfm.util.ToastUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 修改密码
 * @author 辛龙
 *         2016年7月19日
 */
public class ForgetPasswordActivity extends AppBaseActivity implements OnClickListener {
    private CountDownTimer mCountDownTimer; // 验证码计时器

    private Dialog dialog;                  // 加载数据对话框
    private EditText editPhoneNum;          // 输入 手机号
    private EditText mEditTextPassWord;     // 输入 密码
    private EditText editYzm;               // 输入 验证码
    private TextView textGetYzm;            // 获取验证码
    private TextView textCxFaSong;          // 重新发送验证码
    private TextView textNext;              // 不可点击的确定按钮
    private TextView mTextConfirm;          // 确定

    private String tempVerify;
    private String phoneNum;                // 手机号
    private String password;                // 密码
    private String verificationCode;        // 验证码
    private String tag = "FORGET_PASSWORD_VOLLEY_REQUEST_CANCEL_TAG";
    private int sendType = 1;               // == 1 发送验证码  == 2 重发验证码
    private int verifyCode = -1;            // == -1 点击过获取验证码按钮或未正常发验证码
    private boolean isCancelRequest;

    @Override
    public void onClick(View v) {
        // 以下验证操作都需要网络 所以没有网络则不需要继续 提示用户设置网络
        if(GlobalConfig.CURRENT_NETWORK_STATE_TYPE == -1) {
            ToastUtils.show_allways(context, "网络失败，请检查网络!");
            return ;
        }
        switch (v.getId()) {
            case R.id.tv_confirm:          // 验证数据
                checkValue();
                break;
            case R.id.tv_getyzm:            // 检查手机号是否为空，或者是否是一个正常手机号
                checkYzm();
                break;
        }
    }

    @Override
    protected int setViewId() {
        return R.layout.activity_forget_password;
    }

    @Override
    protected void init() {
        setTitle("忘记密码");

        mEditTextPassWord = (EditText) findViewById(R.id.edittext_password);    // 输入 密码
        editPhoneNum = (EditText) findViewById(R.id.edittext_userphone);        // 输入 手机号

        editYzm = (EditText) findViewById(R.id.et_yzm);                         // 输入 验证码
        editYzm.addTextChangedListener(new MyEditListener());

        textGetYzm = (TextView) findViewById(R.id.tv_getyzm);                   // 获取验证码
        textGetYzm.setOnClickListener(this);

        textCxFaSong = (TextView) findViewById(R.id.tv_cxfasong);               // 重新发送验证码
        textNext = (TextView) findViewById(R.id.tv_next);                       // 不可点击的确定按钮

        mTextConfirm = (TextView) findViewById(R.id.tv_confirm);                // 确定
        mTextConfirm.setOnClickListener(this);
    }

    // 手机号正确则发送请求获取验证码
    private void checkYzm() {
        phoneNum = editPhoneNum.getText().toString().trim();
        if (tempVerify == null) {
            tempVerify = phoneNum;
        } else {
            if (!tempVerify.equals(phoneNum)) {
                sendType = 1;
                tempVerify = phoneNum;
            }
        }
        if ("".equalsIgnoreCase(phoneNum) || !isMobile(phoneNum)) {
            ToastUtils.show_allways(context, "请输入正确的手机号码!");
            return ;
        }

        dialog = DialogUtils.Dialogph(context, "正在获取验证码...");
        sendFindPassword();
    }

    // 验证数据的正确性 然后将数据发送到服务器进行验证
    private void checkValue() {
        if (verifyCode == -1) {
            ToastUtils.show_allways(context, "获取验证码异常!");
            return ;
        }

        verificationCode = editYzm.getText().toString().trim();
        password = mEditTextPassWord.getText().toString().trim();
        if ("".equalsIgnoreCase(phoneNum) || !isMobile(phoneNum)) {
            ToastUtils.show_allways(context, "请输入正确的手机号码!");
            return ;
        }
        if ("".equalsIgnoreCase(verificationCode) || verificationCode.length() != 6) {
            ToastUtils.show_allways(context, "验证码不正确!");
            return ;
        }
        if (password.length() < 6 || "".equalsIgnoreCase(password)) {
            ToastUtils.show_allways(context, "密码格式错误!");
            return ;
        }

        dialog = DialogUtils.Dialogph(context, "正在提交数据...");
        sendRequest();
    }

    // 提交数据到服务器进行验证
    private void sendRequest() {
        JSONObject jsonObject = VolleyRequest.getJsonObject(context);
        try {
            jsonObject.put("PhoneNum", phoneNum);
            jsonObject.put("CheckCode", verificationCode);
            jsonObject.put("NeedUserId", "true");
        } catch (JSONException e) {
            e.printStackTrace();
        }

        VolleyRequest.RequestPost(GlobalConfig.checkPhoneCheckCodeUrl, tag, jsonObject, new VolleyCallback() {
            private String ReturnType;
            private String Message;
            private String UserId;

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
                    Message = result.getString("Message");
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                if (ReturnType != null && ReturnType.equals("1001")) {
                    try {
                        UserId = result.getString("UserId");
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    if (UserId != null && !UserId.equals("")) {
                        sendModifyPassword(UserId);
                    } else {
                        ToastUtils.show_allways(context, "获取UserId异常");
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
                ToastUtils.showVolleyError(context);
            }
        });
    }

    // 获取验证码
    private void sendFindPassword() {
        String url;
        if(sendType == 1) {
            url = GlobalConfig.retrieveByPhoneNumUrl;
        } else {
            url = GlobalConfig.reSendPhoneCheckCodeNumUrl;
        }
        JSONObject jsonObject = VolleyRequest.getJsonObject(context);
        try {
            jsonObject.put("PhoneNum", phoneNum);
            if(sendType == 2) {
                jsonObject.put("OperType", "2");
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
                    Message = result.getString("Message");
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                if (ReturnType != null && ReturnType.equals("1001")) {
                    ToastUtils.show_allways(context, "验证码已经发送!");
                    verifyCode = 1;
                    sendType = 2;
                    timerDown();
                    textGetYzm.setVisibility(View.GONE);
                    textCxFaSong.setVisibility(View.VISIBLE);
                } else if (ReturnType != null && ReturnType.equals("T")) {
                    ToastUtils.show_allways(context, "获取验证码异常，请确认后重试!");
                } else if (ReturnType != null && ReturnType.equals("1002")) {
                    ToastUtils.show_allways(context, "此手机号在系统内没有注册");
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
                ToastUtils.showVolleyError(context);
            }
        });
    }

    // 发送修改密码的网络请求
    protected void sendModifyPassword(String userId) {
        dialog = DialogUtils.Dialogph(context, "正在提交请求...");
        JSONObject jsonObject = VolleyRequest.getJsonObject(context);
        try {
            jsonObject.put("RetrieveUserId", userId);
            jsonObject.put("NewPassword", password);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        VolleyRequest.RequestPost(GlobalConfig.updatePwd_AfterCheckPhoneOKUrl, userId, jsonObject, new VolleyCallback() {
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
                    Message = result.getString("Message");
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                if (ReturnType != null && ReturnType.equals("1001")) {
                    ToastUtils.show_allways(context, "密码修改成功!");
                    finish();
                } else {
                    if (Message != null && !Message.trim().equals("")) {
                        ToastUtils.show_allways(context, Message);
                    }
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
            if (s != null && s.length() == 6) {
                textNext.setVisibility(View.GONE);
                mTextConfirm.setVisibility(View.VISIBLE);
            } else {
                mTextConfirm.setVisibility(View.GONE);
                textNext.setVisibility(View.VISIBLE);
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        isCancelRequest = VolleyRequest.cancelRequest(tag);
        if(mCountDownTimer != null) {
            mCountDownTimer.cancel();
            mCountDownTimer = null;
        }
        mEditTextPassWord = null;
        editYzm = null;
        textGetYzm = null;
        textCxFaSong = null;
        textNext = null;
        mTextConfirm = null;
        editPhoneNum = null;
        phoneNum = null;
        dialog = null;
        verificationCode = null;
        password = null;
        tag = null;
    }
}
