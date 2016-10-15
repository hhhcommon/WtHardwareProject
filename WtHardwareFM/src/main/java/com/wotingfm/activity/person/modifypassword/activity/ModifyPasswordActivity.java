package com.wotingfm.activity.person.modifypassword.activity;

import android.app.AlertDialog;
import android.app.Dialog;
import android.os.CountDownTimer;
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
import com.wotingfm.util.DialogUtils;
import com.wotingfm.util.L;
import com.wotingfm.util.ToastUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 修改密码
 *
 * @author 辛龙
 *         2016年7月19日
 */
public class ModifyPasswordActivity extends AppBaseActivity implements OnClickListener {
    private CountDownTimer mCountDownTimer; // 计时器

    private Dialog dialog;                  // 加载数据对话框
    private EditText editOldPassword;       // 输入 旧密码
    private EditText editNewPassword;       // 输入 新密码
    private EditText editNewPasswordConfirm;// 输入 确定新密码
    private EditText editYzm;               // 输入 验证码
    private TextView textGetYzm;            // 获取验证码
    private TextView textCxFaSong;          // 重新发送验证码

    private String oldPassword;             // 旧密码
    private String newPassword;             // 新密码
    private String verificationCode;        // 验证码
    private String userId;                  // 用户 ID
    private String phoneNum;                // 用户手机号
    private String tag = "MODIFY_PASSWORD_VOLLEY_REQUEST_CANCEL_TAG";

    private int modifyType;                 // 修改密码方式  == 1 为通过旧密码修改  == 2 为通过验证码修改
    private int sendType = 1;
    private boolean isCancelRequest;

    @Override
    public void onClick(View v) {
        // 以下操作需要网络支持 所以没有网络就不需要继续验证先提醒用户进行网络设置
        if (GlobalConfig.CURRENT_NETWORK_STATE_TYPE == -1) {
            ToastUtils.show_allways(context, "网络连接失败，请稍后重试!");
            return ;
        }
        switch (v.getId()) {
            case R.id.btn_modifypassword:   // 修改密码确认按钮
                if (!checkData()) {         // 检查数据的正确性
                    return;
                }
                dialog = DialogUtils.Dialogph(context, "正在提交请求...");
                if(modifyType == 1) {
                    sendAdoptOldPasswordMotify();
                } else {
                    sendAdoptCodeModify();
                }
                break;
            case R.id.tv_getyzm:
                checkVerificationCode();
                break;
        }
    }

    @Override
    protected int setViewId() {
        return R.layout.activity_modify_password;
    }

    @Override
    protected void init() {
        setTitle("修改密码");

        findViewById(R.id.btn_modifypassword).setOnClickListener(this);             // 确定修改密码

        editOldPassword = (EditText) findViewById(R.id.edit_oldpassword);           // 旧密码
        editNewPassword = (EditText) findViewById(R.id.edit_newpassword);           // 新密码
        editNewPasswordConfirm = (EditText) findViewById(R.id.edit_confirmpassword);// 确定新密码
        editYzm = (EditText) findViewById(R.id.edit_yzm);                           // 验证码

        textGetYzm = (TextView) findViewById(R.id.tv_getyzm);                       // 获取验证码
        textGetYzm.setOnClickListener(this);

        textCxFaSong = (TextView) findViewById(R.id.tv_cxfasong);                   // 重新发送验证码
    }

    // 检查数据的正确性
    protected boolean checkData() {
        verificationCode = editYzm.getText().toString().trim();
        oldPassword = editOldPassword.getText().toString().trim();
        newPassword = editNewPassword.getText().toString().trim();
        String passwordConfirm = editNewPasswordConfirm.getText().toString().trim();
        if ("".equalsIgnoreCase(newPassword)) {
            Toast.makeText(context, "请输入您的新密码", Toast.LENGTH_LONG).show();
            return false;
        }
        if (newPassword.length() < 6) {
            Toast.makeText(context, "密码请输入六位以上", Toast.LENGTH_LONG).show();
            return false;
        }
        if ("".equalsIgnoreCase(newPassword)) {
            Toast.makeText(context, "请再次输入密码", Toast.LENGTH_LONG).show();
            return false;
        }
        if (!newPassword.equals(passwordConfirm)) {
            new AlertDialog.Builder(this).setMessage("两次输入的密码不一致").setPositiveButton("确定", null).show();
            return false;
        }
        if (passwordConfirm.length() < 6) {
            Toast.makeText(context, "密码请输入六位以上", Toast.LENGTH_LONG).show();
            return false;
        }
        if ("".equalsIgnoreCase(oldPassword) && "".equalsIgnoreCase(verificationCode)) {
            Toast.makeText(context, "请选择一种修改密码的方式!", Toast.LENGTH_LONG).show();
            return false;
        } else {// 如果两者都填了则程序走后者 即验证码方式修改
            if (!"".equalsIgnoreCase(oldPassword)) {        // 旧密码
                modifyType = 1;
            }
            if(!"".equalsIgnoreCase(verificationCode)) {    // 验证码
                modifyType = 2;
            }
        }
        return true;
    }

    // 验证手机号正确就获取验证码
    private void checkVerificationCode() {
        phoneNum = BSApplication.SharedPreferences.getString(StringConstant.PHONENUMBER, "");  // 用户手机号
        L.v("phoneNum", phoneNum);
        if ("".equalsIgnoreCase(phoneNum) || !isMobile(phoneNum)) { // 检查输入数字是否为手机号
            ToastUtils.show_allways(context, "请输入正确的手机号码!");
            return ;
        }

        dialog = DialogUtils.Dialogph(context, "正在获取验证码...");
        sendVerificationCode();                                     // 发送网络请求 获取验证码
    }

    // 获取验证码
    private void sendVerificationCode() {
        String url;
        if(sendType == 1) {
            url = GlobalConfig.retrieveByPhoneNumUrl;               // 第一次发送验证码接口
        } else {
            url = GlobalConfig.reSendPhoneCheckCodeNumUrl;          // 重新发送验证码接口
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

                    ToastUtils.show_allways(context, "验证码已经发送!");
                    timerDown();
                    textGetYzm.setVisibility(View.GONE);
                    textCxFaSong.setVisibility(View.VISIBLE);
                } else if (returnType != null && returnType.equals("T")) {
                    ToastUtils.show_allways(context, "获取异常，请确认后重试!");
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
                ToastUtils.showVolleyError(context);
            }
        });
    }

    // 通过旧密码修改新密码
    protected void sendAdoptOldPasswordMotify() {
        JSONObject jsonObject = VolleyRequest.getJsonObject(context);
        try {
            jsonObject.put("OldPassword", oldPassword);// 待改
            jsonObject.put("NewPassword", newPassword);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        VolleyRequest.RequestPost(GlobalConfig.modifyPasswordUrl, tag, jsonObject, new VolleyCallback() {
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
                    L.v("Message", Message);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                if (ReturnType != null && ReturnType.equals("1001")) {
                    ToastUtils.show_allways(context, "密码修改成功!");
                    finish();
                } else {
                    ToastUtils.show_allways(context, "修改密码失败，请稍后重试!");
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

    // 通过验证码的方式修改密码
    private void sendAdoptCodeModify() {
        JSONObject jsonObject = VolleyRequest.getJsonObject(context);
        try {
            jsonObject.put("PhoneNum", phoneNum);
            jsonObject.put("CheckCode", verificationCode);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        VolleyRequest.RequestPost(GlobalConfig.checkPhoneCheckCodeUrl, tag, jsonObject, new VolleyCallback() {
            private String returnType;
            private String message;

            @Override
            protected void requestSuccess(JSONObject result) {
                if(isCancelRequest){
                    return ;
                }
                try {
                    returnType = result.getString("ReturnType");
                    message = result.getString("Message");
                    L.v("message", "message -- > > " + message);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                if (returnType != null && returnType.equals("1001")) {
                    sendModifyPassword();
                } else {
                    ToastUtils.show_allways(context, "网络异常或验证码错误，请稍后重试!");
                    if (dialog != null) {
                        dialog.dismiss();
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

    protected void sendModifyPassword() {
        JSONObject jsonObject = VolleyRequest.getJsonObject(context);
        try {
            jsonObject.put("RetrieveUserId", userId);
            jsonObject.put("NewPassword", newPassword);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        VolleyRequest.RequestPost(GlobalConfig.updatePwd_AfterCheckPhoneOKUrl, tag, jsonObject, new VolleyCallback() {
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
                    L.v("Message", Message);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                if (ReturnType != null && ReturnType.equals("1001")) {
                    ToastUtils.show_allways(context, "密码修改成功");
                    finish();
                } else {
                    ToastUtils.show_allways(context, "密码修改失败,请稍后重试!");
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

    // 验证码时间
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

    // 用正则验证手机号
    private boolean isMobile(String str) {
        Pattern pattern = Pattern.compile("^[1][3,4,5,7,8][0-9]{9}$");
        Matcher matcher = pattern.matcher(str);
        return matcher.matches();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        isCancelRequest = VolleyRequest.cancelRequest(tag);
        if(mCountDownTimer != null) {
            mCountDownTimer.cancel();
            mCountDownTimer = null;
        }
        editOldPassword = null;
        editNewPassword = null;
        editNewPasswordConfirm = null;
        dialog = null;
        oldPassword = null;
        newPassword = null;
        userId = null;
        phoneNum = null;
        tag = null;
    }
}
