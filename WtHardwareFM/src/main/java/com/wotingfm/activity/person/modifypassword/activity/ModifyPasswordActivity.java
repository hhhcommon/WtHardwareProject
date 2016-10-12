package com.wotingfm.activity.person.modifypassword.activity;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Intent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.VolleyError;
import com.wotingfm.R;
import com.wotingfm.activity.common.baseactivity.AppBaseActivity;
import com.wotingfm.activity.person.login.activity.LoginActivity;
import com.wotingfm.common.config.GlobalConfig;
import com.wotingfm.common.volley.VolleyCallback;
import com.wotingfm.common.volley.VolleyRequest;
import com.wotingfm.util.DialogUtils;
import com.wotingfm.util.ToastUtils;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * 修改密码
 *
 * @author 辛龙
 *         2016年7月19日
 */
public class ModifyPasswordActivity extends AppBaseActivity implements OnClickListener {
    private Dialog dialog;                  // 加载数据对话框
    private EditText editOldPassword;       // 输入 旧密码
    private EditText editNewPassword;       // 输入 新密码
    private EditText editNewPasswordConfirm;// 输入 确定新密码
    private EditText editYzm;               // 输入 验证码
    private TextView textGetYzm;            // 获取验证码
    private TextView textCxFaSong;          // 重新发送验证码

    private String oldPassword;             // 旧密码
    private String newPassword;             // 新密码
    private String userId;                  // 用户 ID
    private String phoneNum;                // 用户手机号
    private String tag = "MODIFY_PASSWORD_VOLLEY_REQUEST_CANCEL_TAG";

    private int viewType;                   // 为0则来自通过验证过手机号的请求 userId由上一个界面传入
    private boolean isCancelRequest;

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_modifypassword:   // 修改密码确认按钮
                if (!checkData()) {         // 检查数据的正确性
                    return;
                }
                if (GlobalConfig.CURRENT_NETWORK_STATE_TYPE == -1) {// 检查是否连接网络
                    ToastUtils.show_allways(ModifyPasswordActivity.this, "网络连接失败，请稍后重试");
                }
                if (viewType != 0) {
                    send();
                } else {
                    sendModifyPassword();
                }
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
        handleIntent();

        findViewById(R.id.btn_modifypassword).setOnClickListener(this);             // 确定修改密码

        editOldPassword = (EditText) findViewById(R.id.edit_oldpassword);           // 旧密码
        editNewPassword = (EditText) findViewById(R.id.edit_newpassword);           // 新密码
        editNewPasswordConfirm = (EditText) findViewById(R.id.edit_confirmpassword);// 确定新密码
        editYzm = (EditText) findViewById(R.id.edit_yzm);                           // 验证码
        textGetYzm = (TextView) findViewById(R.id.tv_getyzm);                       // 获取验证码
        textCxFaSong = (TextView) findViewById(R.id.tv_cxfasong);                   // 重新发送验证码
    }

    // 处理上一个界面传递的数据
    private void handleIntent() {
        Intent intent = getIntent();
        if(intent != null) {// 判断是否有数据传入
            viewType = getIntent().getIntExtra("origin", 1);
            userId = getIntent().getStringExtra("userid");
            phoneNum = getIntent().getStringExtra("phonenum");
            if (viewType == 0) {
                LinearLayout linearOldPassword = (LinearLayout) findViewById(R.id.lin_oldpassword);
                linearOldPassword.setVisibility(View.GONE);
            }
        }
    }

    // 检查数据的正确性
    protected boolean checkData() {
        oldPassword = editOldPassword.getText().toString().trim();
        newPassword = editNewPassword.getText().toString().trim();
        String passwordConfirm = editNewPasswordConfirm.getText().toString().trim();
        if (viewType != 0) {
            if ("".equalsIgnoreCase(oldPassword)) {
                Toast.makeText(context, "请输入您的旧密码", Toast.LENGTH_LONG).show();
                return false;
            }
            if ("".equalsIgnoreCase(newPassword)) {
                Toast.makeText(context, "请输入您的新密码", Toast.LENGTH_LONG).show();
                return false;
            }
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
        return true;
    }


    // viewType == 0 通过手机验证修改密码
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
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                if (ReturnType != null && ReturnType.equals("1001")) {
                    ToastUtils.show_allways(context, "密码修改成功");
                    Intent intent = new Intent(context, LoginActivity.class);
                    intent.putExtra("phonenum", phoneNum);
                    startActivity(intent);
                    finish();
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

    // viewType != 0 通过旧密码修改新密码
    protected void send() {
        dialog = DialogUtils.Dialogph(context, "正在提交请求...");
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
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                if (ReturnType != null && ReturnType.equals("1001")) {
                    ToastUtils.show_allways(context, "密码修改成功");
                    finish();
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

    @Override
    protected void onDestroy() {
        super.onDestroy();
        isCancelRequest = VolleyRequest.cancelRequest(tag);
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
