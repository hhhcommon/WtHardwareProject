package com.wotingfm.activity.person.modifyphonenumber;

import android.app.Dialog;
import android.content.SharedPreferences;
import android.os.CountDownTimer;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.android.volley.VolleyError;
import com.wotingfm.R;
import com.wotingfm.activity.common.baseactivity.AppBaseActivity;
import com.wotingfm.common.application.BSApplication;
import com.wotingfm.common.config.GlobalConfig;
import com.wotingfm.common.constant.StringConstant;
import com.wotingfm.common.volley.VolleyCallback;
import com.wotingfm.common.volley.VolleyRequest;
import com.wotingfm.util.DialogUtils;
import com.wotingfm.util.L;
import com.wotingfm.util.ToastUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 修改手机号
 */
public class ModifyPhoneNumberActivity extends AppBaseActivity implements View.OnClickListener {
    private CountDownTimer mCountDownTimer;         // 计时器

    private Dialog dialog;                          // 加载数据对话框
    private EditText editPhoneNumber;               // 输入 手机号码
    private EditText editVerificationCode;          // 输入 验证码
    private TextView textGetVerificationCode;       // 获取验证码
    private TextView textResend;                    // 重新发送验证码

    private String tag = "MODIFY_PHONE_NUMBER_VOLLEY_REQUEST_CANCEL_TAG";
    private String phoneNumber;                     // 手机号
    private String verificationCode;                // 验证码
    private int sendType = 1;                       // == 1 为第一次发送验证码  == 2 为重新发送验证码
    private boolean isCancelRequest;

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_confirm:                  // 确定修改
                checkValue();
                break;
            case R.id.text_get_verification_code:   // 获取验证码
                checkVerificationCode();
                break;
        }
    }

    @Override
    protected int setViewId() {
        return R.layout.activity_modify_phone_number;
    }

    @Override
    protected void init() {
        setTitle("修改手机号");

        editPhoneNumber = (EditText) findViewById(R.id.edit_phone_number);                  // 手机号码
        editVerificationCode = (EditText) findViewById(R.id.edit_verification_code);        // 验证码

        textGetVerificationCode = (TextView) findViewById(R.id.text_get_verification_code); // 获取验证码
        textGetVerificationCode.setOnClickListener(this);

        textResend = (TextView) findViewById(R.id.text_resend);                             // 重新发送验证码
        textResend.setOnClickListener(this);

        findViewById(R.id.btn_confirm).setOnClickListener(this);                            // 确定修改
    }

    // 验证码手机号正确就获取验证码
    private void checkVerificationCode() {
        phoneNumber = editPhoneNumber.getText().toString().trim();
        if ("".equalsIgnoreCase(phoneNumber) || !isMobile(phoneNumber)) {// 检查输入数字是否为手机号
            ToastUtils.show_allways(context, "请输入正确的手机号码!");
            return ;
        }
        if (GlobalConfig.CURRENT_NETWORK_STATE_TYPE != -1) {
            dialog = DialogUtils.Dialogph(context, "正在获取验证码...");
            sendVerificationCode();                                     // 发送网络请求 获取验证码
        } else {
            ToastUtils.show_allways(context, "网络失败，请检查网络!");
        }
    }

    // 检查数据的正确性
    private void checkValue() {
        verificationCode = editVerificationCode.getText().toString().trim();
        if ("".equalsIgnoreCase(phoneNumber) || !isMobile(phoneNumber)) {// 检查输入数字是否为手机号
            ToastUtils.show_allways(context, "请输入正确的手机号码!");
            return ;
        }
        if ("".equalsIgnoreCase(verificationCode) || verificationCode.length() != 6) {
            ToastUtils.show_allways(context, "验证码不正确!");
            return ;
        }
        if (GlobalConfig.CURRENT_NETWORK_STATE_TYPE != -1) {
            dialog = DialogUtils.Dialogph(context, "正在验证手机号...");
            sendRequest();
        } else {
            ToastUtils.show_short(context, "网络失败，请检查网络!");
        }
    }

    // 请求网络获取验证码
    private void sendVerificationCode() {
        String url;
        if(sendType == 1) {
            url = GlobalConfig.registerByPhoneNumUrl;       // 第一次发送验证码接口
        } else {
            url = GlobalConfig.reSendPhoneCheckCodeNumUrl;  // 重新发送验证码接口
        }
        JSONObject jsonObject = VolleyRequest.getJsonObject(context);
        try {
            jsonObject.put("PhoneNum", phoneNumber);
            if(sendType == 2) {
                jsonObject.put("OperType", "1");
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        VolleyRequest.RequestPost(url, tag, jsonObject, new VolleyCallback() {
            private String returnType;
            private String message;

            @Override
            protected void requestSuccess(JSONObject result) {
                if (dialog != null) {
                    dialog.dismiss();
                }
                if(isCancelRequest){
                    return ;
                }
                try {
                    returnType = result.getString("ReturnType");
                    message = result.getString("Message");
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                if (returnType != null && returnType.equals("1001")) {
                    sendType = 2;// 再次发送验证码

                    ToastUtils.show_allways(context, "验证码已经发送");
                    timerDown();
                    textGetVerificationCode.setVisibility(View.GONE);
                    textResend.setVisibility(View.VISIBLE);
                } else if (returnType != null && returnType.equals("T")) {
                    ToastUtils.show_allways(context, "异常返回值");
                } else if (returnType != null && returnType.equals("1002")) {
                    ToastUtils.show_allways(context, "此号码已经注册");
                } else {
                    if (message != null && !message.trim().equals("")) {
                        ToastUtils.show_allways(context, message + "");
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

    // 提交数据到服务器进行验证
    private void sendRequest() {
        JSONObject jsonObject = VolleyRequest.getJsonObject(context);
        try {
            jsonObject.put("PhoneNum", phoneNumber);
            jsonObject.put("CheckCode", verificationCode);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        VolleyRequest.RequestPost(GlobalConfig.checkPhoneCheckCodeUrl, tag, jsonObject, new VolleyCallback() {
            private String returnType;
            private String message;

            @Override
            protected void requestSuccess(JSONObject result) {
                if (dialog != null) {
                    dialog.dismiss();
                }
                if(isCancelRequest){
                    return ;
                }
                try {
                    returnType = result.getString("ReturnType");
                    message = result.getString("Message");
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                if (returnType != null && returnType.equals("1001")) {
                    dialog = DialogUtils.Dialogph(context, "正在修改绑定手机号...");
                    sendBinding();
                } else if (returnType != null && returnType.equals("T")) {
                    ToastUtils.show_allways(context, "异常返回值!");
                } else if (returnType != null && returnType.equals("1002")) {
                    ToastUtils.show_allways(context, "验证码错误!");
                }else {
                    if (message != null && !message.trim().equals("")) {
                        ToastUtils.show_allways(context, message + "");
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

    // 修改手机号方法 利用目前的修改手机号接口
    private void sendBinding() {
        JSONObject jsonObject = VolleyRequest.getJsonObject(context);
        try {
            jsonObject.put("PhoneNum", phoneNumber);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        VolleyRequest.RequestPost(GlobalConfig.bindExtUserUrl, tag, jsonObject, new VolleyCallback() {
            private String returnType;
            private String message;

            @Override
            protected void requestSuccess(JSONObject result) {
                if (dialog != null) {
                    dialog.dismiss();
                }
                if(isCancelRequest){
                    return ;
                }
                try {
                    returnType = result.getString("ReturnType");
                    message = result.getString("Message");
                    L.v("message", "sendBinding -- > > " + message);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                if (returnType != null && returnType.equals("1001")) {
                    ToastUtils.show_allways(context, "手机号修改成功!");
                    SharedPreferences.Editor et = BSApplication.SharedPreferences.edit();
                    et.putString(StringConstant.PHONENUMBER, phoneNumber);
                    et.commit();
                    finish();
                } else {
                    ToastUtils.show_allways(context, "手机号修改失败!");
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

    // 验证码时间
    private void timerDown() {
        mCountDownTimer = new CountDownTimer(60000, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                textResend.setText(millisUntilFinished / 1000 + "s后重新发送");
            }

            @Override
            public void onFinish() {
                textResend.setVisibility(View.GONE);
                textGetVerificationCode.setVisibility(View.VISIBLE);
            }
        }.start();
    }

    // 用正则验证手机号
    private boolean isMobile(String str) {
        Pattern pattern = Pattern.compile("^[1][3,4,5,7,8][0-9]{9}$");
        Matcher matcher = pattern.matcher(str);
        return matcher.matches();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        isCancelRequest = VolleyRequest.cancelRequest(tag);// 根据 TAG 取消网络请求
        if(mCountDownTimer != null) {
            mCountDownTimer.cancel();
            mCountDownTimer = null;
        }
    }
}
