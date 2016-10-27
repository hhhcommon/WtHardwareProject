package com.wotingfm.activity.im.interphone.groupmanage.modifygrouppassword;

import android.app.Dialog;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.android.volley.VolleyError;
import com.wotingfm.R;
import com.wotingfm.activity.common.baseactivity.BaseActivity;
import com.wotingfm.common.config.GlobalConfig;
import com.wotingfm.common.volley.VolleyCallback;
import com.wotingfm.common.volley.VolleyRequest;
import com.wotingfm.util.DialogUtils;
import com.wotingfm.util.ToastUtils;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * 修改群密码
 * 作者：xinlong on 2016/3/9
 * 邮箱：645700751@qq.com
 */
public class ModifyGroupPasswordActivity extends BaseActivity implements View.OnClickListener, TextWatcher {
    private Dialog dialog;                      // 加载数据对话框
    private EditText editOldPassword;           // 用户输入 旧密码
    private EditText editNewPassword;           // 用户输入 新密码
    private EditText editNewPasswordConfirm;    // 用户输入 确认新密码
    private Button btnModifyPassword;           // 确定修改密码

    private String oldPassword;                 // 旧密码
    private String newPassword;                 // 新密码
    private String passWordConfirm;             // 确认新密码
    private String groupId;                     // 群组 ID
    private String tag = "MODIFY_GROUP_PASSWORD_VOLLEY_REQUEST_CANCEL_TAG";
    private boolean isCancelRequest;

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.wt_back:              // 返回
                finish();
                break;
            case R.id.btn_modifypassword:   // 确定修改密码
                if(groupId == null || groupId.equals("")) {
                    ToastUtils.show_always(context, "获取群组 ID 失败，请返回重试!");
                    return ;
                }
                if (GlobalConfig.CURRENT_NETWORK_STATE_TYPE != -1) {
                    send();
                } else {
                    ToastUtils.show_always(ModifyGroupPasswordActivity.this, "网络连接失败，请稍后重试");
                }
                break;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_modify_group_password);

        initView();
    }

    // 初始化视图
    private void initView() {
        if(getIntent() != null) {
            groupId = getIntent().getStringExtra("GroupId");
        }

        findViewById(R.id.wt_back).setOnClickListener(this);                            // 返回

        btnModifyPassword = (Button) findViewById(R.id.btn_modifypassword);             // 确定修改密码
        btnModifyPassword.setOnClickListener(this);

        editOldPassword = (EditText) findViewById(R.id.edit_oldpassword);               // 旧密码
        editOldPassword.addTextChangedListener(this);

        editNewPassword = (EditText) findViewById(R.id.edit_newpassword);               // 新密码
        editNewPassword.addTextChangedListener(this);

        editNewPasswordConfirm = (EditText) findViewById(R.id.edit_confirmpassword);    // 确认新密码
        editNewPasswordConfirm.addTextChangedListener(this);
    }

    // 发送修改密码请求到服务器
    protected void send() {
        dialog = DialogUtils.Dialogph(context, "修改群密码提交请求");
        JSONObject jsonObject = VolleyRequest.getJsonObject(context);
        try {
            jsonObject.put("OldPassword", oldPassword);
            jsonObject.put("NewPassword", newPassword);
            jsonObject.put("GroupId", groupId);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        VolleyRequest.RequestPost(GlobalConfig.UpdateGroupPassWordUrl, tag, jsonObject, new VolleyCallback() {
            @Override
            protected void requestSuccess(JSONObject result) {
                if (dialog != null) dialog.dismiss();
                if (isCancelRequest) return;
                try {
                    String ReturnType = result.getString("ReturnType");
                    if (ReturnType != null && ReturnType.equals("1001")) {
                        ToastUtils.show_always(context, "密码修改成功");
                        finish();
                    } else {
                        String Message = result.getString("Message");
                        if (Message != null && !Message.trim().equals("")) {
                            ToastUtils.show_always(context, Message + "");
                        }
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            protected void requestError(VolleyError error) {
                if (dialog != null) dialog.dismiss();
                ToastUtils.showVolleyError(context);
            }
        });
    }

    // 检查数据的正确性
    @Override
    public void afterTextChanged(Editable s) {
        oldPassword = editOldPassword.getText().toString().trim();
        newPassword = editNewPassword.getText().toString().trim();
        passWordConfirm = editNewPasswordConfirm.getText().toString().trim();
        if ("".equalsIgnoreCase(oldPassword) || oldPassword.length() < 6) {
            btnModifyPassword.setEnabled(false);
            return ;
        }
        if ("".equalsIgnoreCase(newPassword) || newPassword.length() < 6) {
            btnModifyPassword.setEnabled(false);
            return ;
        }
        if ("".equalsIgnoreCase(newPassword) || passWordConfirm.length() < 6) {
            btnModifyPassword.setEnabled(false);
            return ;
        }
        if(!newPassword.equals(passWordConfirm)) {
            ToastUtils.show_always(context, "两次输入的密码不一致!");
            btnModifyPassword.setEnabled(false);
            return ;
        }
        btnModifyPassword.setEnabled(true);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        isCancelRequest = VolleyRequest.cancelRequest(tag);
        editOldPassword = null;
        editNewPassword = null;
        editNewPasswordConfirm = null;
        oldPassword = null;
        newPassword = null;
        passWordConfirm = null;
        dialog = null;
        context = null;
        groupId = null;
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
