package com.wotingfm.activity.person.phonecheck;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.TextView;

import com.android.volley.VolleyError;
import com.wotingfm.R;
import com.wotingfm.activity.common.baseactivity.BaseActivity;
import com.wotingfm.activity.person.modifyphonenumber.ModifyPhoneNumberActivity;
import com.wotingfm.common.config.GlobalConfig;
import com.wotingfm.common.volley.VolleyCallback;
import com.wotingfm.common.volley.VolleyRequest;
import com.wotingfm.util.DialogUtils;
import com.wotingfm.util.ToastUtils;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * 账号绑定==找回密码--变更手机号
 * @author 辛龙
 *         2016年7月19日
 */
public class PhoneCheckActivity extends BaseActivity implements OnClickListener, TextWatcher {
    private CountDownTimer mCountDownTimer;     // 获取验证码时间

    private Dialog dialog;
    private EditText editYzm;                   // 输入 验证码
    private TextView textGetYzm;                // 获取验证码
    private TextView textCxFaSong;              // 重新发送验证码
    private TextView textNextDefault;           // 不可点击的"下一步"
    private TextView textNext;                  // 下一步

    private String phoneNum;                    // 用户绑定的手机号码  不需要用户输入 此绑定的手机号码与登录账户相关联
    private String verificationCode;            // 验证码
    private String tag = "PHONE_CHECK_VOLLEY_REQUEST_CANCEL_TAG";
    private boolean isCancelRequest;
    private boolean isGetCode;                  // 是否获取验证码
    private int sendType = 1;                   // sendType == 1 发送验证码 sendType == 2 重发验证码

    @Override
    public void onClick(View v) {
        if(v.getId() == R.id.head_left_btn) {   // 返回
            finish();
            return ;
        }
        // 以下操作需要网络支持 没有网络则不需要继续验证先提示用户设置网络
        if(GlobalConfig.CURRENT_NETWORK_STATE_TYPE == -1) {
            ToastUtils.show_always(context, "网络连接失败，请检查网络!");
            return ;
        }
        switch (v.getId()) {
            case R.id.tv_getyzm:                // 获取验证码
                dialog = DialogUtils.Dialogph(context, "正在获取验证码");
                sendVerificationCode();
                break;
            case R.id.tv_next:                  // 下一步
                verificationCode = editYzm.getText().toString().trim();
                dialog = DialogUtils.Dialogph(context, "正在验证");
                sendRequest();
                break;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_phonecheck);
        setView();
    }

    // 初始化视图
    private void setView() {
        findViewById(R.id.head_left_btn).setOnClickListener(this);          // 返回

        editYzm = (EditText) findViewById(R.id.et_yzm);                     // 输入 验证码
        editYzm.addTextChangedListener(this);

        textGetYzm = (TextView) findViewById(R.id.tv_getyzm);               // 获取验证码
        textGetYzm.setOnClickListener(this);
        textCxFaSong = (TextView) findViewById(R.id.tv_cxfasong);           // 重新发送验证码

        textNextDefault = (TextView) findViewById(R.id.tv_next_default);    // 不可点击的"下一步"
        textNext = (TextView) findViewById(R.id.tv_next);                   // 下一步
        textNext.setOnClickListener(this);

        // 获取用户绑定的手机号
        if(getIntent() != null) {
            phoneNum = getIntent().getStringExtra("phoneNumber");
        } else {
            ToastUtils.show_always(context, "获取用户绑定手机号码失败，请返回重试!");
        }
    }

    // 获取验证码
    private void sendVerificationCode() {
        String url;
        if(sendType == 2) {
            url = GlobalConfig.reSendPhoneCheckCodeNumUrl;  // 再次获取验证码
        } else {
            url = GlobalConfig.retrieveByPhoneNumUrl;       // 第一次获取验证码
        }
        JSONObject jsonObject = VolleyRequest.getJsonObject(context);
        try {
            jsonObject.put("PhoneNum", phoneNum);
            if(sendType == 2) {
                jsonObject.put("OperType", "1");
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        VolleyRequest.RequestPost(url, tag, jsonObject, new VolleyCallback() {
            private String ReturnType;
            private String Message;

            @Override
            protected void requestSuccess(JSONObject result) {
                if (dialog != null) dialog.dismiss();
                if (isCancelRequest) return ;
                try {
                    ReturnType = result.getString("ReturnType");
                    Message = result.getString("Message");
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                if (ReturnType != null && ReturnType.equals("1001")) {
                    ToastUtils.show_always(context, "验证码已经发送");
                    isGetCode = true;
                    sendType = 2;
                    timerDown();
                    textGetYzm.setVisibility(View.GONE);
                    textCxFaSong.setVisibility(View.VISIBLE);
                } else if (ReturnType != null && ReturnType.equals("T")) {
                    ToastUtils.show_always(context, "异常返回值");
                } else if (ReturnType != null && ReturnType.equals("1002")) {
                    ToastUtils.show_always(context, "此手机号在系统内没有注册");
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

            @Override
            protected void requestSuccess(JSONObject result) {
                if (dialog != null) dialog.dismiss();
                if (isCancelRequest) return;
                try {
                    ReturnType = result.getString("ReturnType");
                    Message = result.getString("Message");
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                if (ReturnType != null && ReturnType.equals("1001")) {
                    ToastUtils.show_always(context, "验证成功，跳转至绑定新的手机号码界面!");
                    Intent intent = new Intent(context, ModifyPhoneNumberActivity.class);
                    startActivityForResult(intent, 1);
                } else if (ReturnType != null && ReturnType.equals("T")) {
                    ToastUtils.show_always(context, "异常返回值");
                } else if (ReturnType != null && ReturnType.equals("1002")) {
                    ToastUtils.show_always(context, "验证码不匹配");
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

    // 再次获取验证码倒计时
    private void timerDown() {
        mCountDownTimer = new CountDownTimer(60000, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                String timeTip = millisUntilFinished / 1000 + "s后重新发送";
                textCxFaSong.setText(timeTip);
            }

            @Override
            public void onFinish() {
                isGetCode = false;
                textCxFaSong.setVisibility(View.GONE);
                textGetYzm.setVisibility(View.VISIBLE);
            }
        }.start();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case 1:
                if (resultCode == 1) {
                    finish();
                }
                break;
        }
    }

    @Override
    public void afterTextChanged(Editable s) {
        if(!isGetCode) {// 获取验证码失败或没有获取验证码
            return ;
        }
        if (s.length() == 6 && phoneNum != null && !phoneNum.equals("") && phoneNum.length() == 11) {
            textNextDefault.setVisibility(View.GONE);
            textNext.setVisibility(View.VISIBLE);
        } else {
            textNext.setVisibility(View.GONE);
            textNextDefault.setVisibility(View.VISIBLE);
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
        editYzm = null;
        textGetYzm = null;
        textNext = null;
        phoneNum = null;
        dialog = null;
        textCxFaSong = null;
        verificationCode = null;
        textNextDefault = null;
        tag = null;
        setContentView(R.layout.activity_null);
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
    }
}
