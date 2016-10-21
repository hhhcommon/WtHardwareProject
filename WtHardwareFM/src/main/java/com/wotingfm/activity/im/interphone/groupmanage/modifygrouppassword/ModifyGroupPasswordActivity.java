package com.wotingfm.activity.im.interphone.groupmanage.modifygrouppassword;

import android.app.Dialog;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

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
public class ModifyGroupPasswordActivity extends BaseActivity {

    private ModifyGroupPasswordActivity context;
    private EditText et_oldPassword;
    private EditText et_newPassword;
    private EditText et_newPassword_confirm;
    private TextView btn_password_modify;
    private boolean flag;
    private String oldPassword;
    private String newPassword;
    private String passWordConfirm;
    private Dialog dialog;
    private String groupId;
    private String tag = "MODIFY_GROUP_PASSWORD_VOLLEY_REQUEST_CANCEL_TAG";
    private boolean isCancelRequest;
    private TextView mBack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_modify_group_password);
        context = this;
        handleIntent();
        setView();
        setListener();
    }

    private void handleIntent() {
        groupId = this.getIntent().getStringExtra("GroupId");
    }

    private void setView() {
        et_oldPassword = (EditText) findViewById(R.id.edit_oldpassword);
        et_newPassword = (EditText) findViewById(R.id.edit_newpassword);
        et_newPassword_confirm = (EditText) findViewById(R.id.edit_confirmpassword);
        btn_password_modify = (TextView) findViewById(R.id.btn_modifypassword);
        mBack = (TextView) findViewById(R.id.wt_back);
    }

    private void setListener() {
        btn_password_modify.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Boolean result = checkData();
                if (result == true) {
                    if (groupId != null && !groupId.equals("")) {
                        if (GlobalConfig.CURRENT_NETWORK_STATE_TYPE != -1) {
                            send();
                        } else {
                            ToastUtils.show_always(ModifyGroupPasswordActivity.this, "网络连接失败，请稍后重试");
                        }
                    } else {
                        ToastUtils.show_always(context, "获取groupId失败，请返回上一级界面重试");
                    }
                }

            }
        });

        mBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    protected Boolean checkData() {
        oldPassword = et_oldPassword.getText().toString().trim();
        newPassword = et_newPassword.getText().toString().trim();
        passWordConfirm = et_newPassword_confirm.getText().toString().trim();
        flag = true;
        if ("".equalsIgnoreCase(oldPassword)) {
            Toast.makeText(this, "请输入您的旧密码", Toast.LENGTH_LONG).show();
            flag = false;
            return flag;
        }
        if ("".equalsIgnoreCase(newPassword)) {
            Toast.makeText(this, "请输入您的新密码", Toast.LENGTH_LONG).show();
            flag = false;
            return flag;
        }
        if (newPassword.length() < 6) {
            Toast.makeText(this, "密码请输入六位以上", Toast.LENGTH_LONG).show();
            flag = false;
            return flag;
        }
        if ("".equalsIgnoreCase(newPassword)) {
            Toast.makeText(this, "请再次输入密码", Toast.LENGTH_LONG).show();
            flag = false;
            return flag;
        }
        if (!newPassword.equals(passWordConfirm)) {
            Toast.makeText(this, "两次输入的密码不一致", Toast.LENGTH_LONG).show();
            flag = false;
            return flag;
        }
        if (passWordConfirm.length() < 6) {
            Toast.makeText(this, "密码请输入六位以上", Toast.LENGTH_LONG).show();
            flag = false;
            return flag;
        }
        return flag;
    }

    protected void send() {
        dialog = DialogUtils.Dialogph(this, "修改群密码提交请求");
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
                Log.e("修改群密码返回值", "" + result.toString());
                if (isCancelRequest) return;
                try {
                    // 获取列表
                    String ReturnType = result.getString("ReturnType");
                    if (ReturnType != null && ReturnType.equals("1001")) {
                        ToastUtils.show_always(ModifyGroupPasswordActivity.this, "密码修改成功");
                        finish();
                    } else if (ReturnType != null && ReturnType.equals("1002")) {
                        try {
                            String Message = result.getString("Message");
                            if (Message != null && !Message.trim().equals("")) {
                                ToastUtils.show_always(ModifyGroupPasswordActivity.this, Message + "");
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    } else {
                        try {
                            String Message = result.getString("Message");
                            if (Message != null && !Message.trim().equals("")) {
                                ToastUtils.show_always(ModifyGroupPasswordActivity.this, Message + "");
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
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        isCancelRequest = VolleyRequest.cancelRequest(tag);
        et_oldPassword = null;
        et_newPassword = null;
        et_newPassword_confirm = null;
        btn_password_modify = null;
        oldPassword = null;
        newPassword = null;
        passWordConfirm = null;
        dialog = null;
        mBack = null;
        context = null;
        groupId = null;
        tag = null;
        setContentView(R.layout.activity_null);
    }
}
