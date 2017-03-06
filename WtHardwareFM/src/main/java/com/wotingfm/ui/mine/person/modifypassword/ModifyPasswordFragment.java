package com.wotingfm.ui.mine.person.modifypassword;

import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import com.android.volley.VolleyError;
import com.wotingfm.R;
import com.wotingfm.common.config.GlobalConfig;
import com.wotingfm.common.volley.VolleyCallback;
import com.wotingfm.common.volley.VolleyRequest;
import com.wotingfm.ui.mine.main.MineActivity;
import com.wotingfm.util.DialogUtils;
import com.wotingfm.util.ToastUtils;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * 修改密码
 * @author 辛龙
 * 2016年7月19日
 */
public class ModifyPasswordFragment extends Fragment implements OnClickListener {
    private Dialog dialog;
    private EditText editOldPassword;// 输入 旧密码
    private EditText editNewPassword;// 输入新密码
    private EditText editNewPasswordConfirm;// 输入 确认新密码

    private String oldPassword;// 旧密码
    private String newPassword;// 新密码
    private String passwordConfirm;// 确定新密码
    private String tag = "MODIFY_PASSWORD_VOLLEY_REQUEST_CANCEL_TAG";

    private boolean isCancelRequest;
    private FragmentActivity context;
    private View rootView;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (rootView == null) {
            rootView = inflater.inflate(R.layout.activity_modify_password, container, false);
            rootView.setOnClickListener(this);
            context = getActivity();
            initView();
        }
        return rootView;
    }

    // 初始化视图
    private void initView() {
        rootView.findViewById(R.id.head_left_btn).setOnClickListener(this);// 返回
        rootView.findViewById(R.id.btn_modifypassword).setOnClickListener(this);// 确定修改密码

        editOldPassword = (EditText) rootView.findViewById(R.id.edit_oldpassword);// 输入 旧密码
        editNewPassword = (EditText) rootView.findViewById(R.id.edit_newpassword);// 输入 新密码
        editNewPasswordConfirm = (EditText) rootView.findViewById(R.id.edit_confirmpassword);// 输入 确定新密码
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.head_left_btn:// 返回
                MineActivity.close();
                break;
            case R.id.btn_modifypassword:// 确定修改密码
                if (checkData()) {
                    if (GlobalConfig.CURRENT_NETWORK_STATE_TYPE != -1) {
                        send();
                    } else {
                        ToastUtils.show_always(context, "网络连接失败，请稍后重试");
                    }
                }
                break;
        }
    }


    protected boolean checkData() {
        oldPassword = editOldPassword.getText().toString().trim();
        newPassword = editNewPassword.getText().toString().trim();
        passwordConfirm = editNewPasswordConfirm.getText().toString().trim();
        if ("".equalsIgnoreCase(oldPassword)) {
            Toast.makeText(context, "请输入您的旧密码", Toast.LENGTH_LONG).show();
            return false;
        }
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
            new AlertDialog.Builder(context).setMessage("两次输入的密码不一致").setPositiveButton("确定", null).show();
            return false;
        }
        if (passwordConfirm.length() < 6) {
            Toast.makeText(context, "密码请输入六位以上", Toast.LENGTH_LONG).show();
            return false;
        }
        return true;
    }

    // 发送修改密码请求
    protected void send() {
        dialog = DialogUtils.Dialogph(context, "正在提交请求");
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
                if (dialog != null) dialog.dismiss();
                if (isCancelRequest) return;
                try {
                    ReturnType = result.getString("ReturnType");
                    Message = result.getString("Message");
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                if (ReturnType != null && ReturnType.equals("1001")) {
                    ToastUtils.show_always(context, "密码修改成功");
                    MineActivity.close();
                }
                if (ReturnType != null && ReturnType.equals("1002")) {
                    ToastUtils.show_always(context, "" + Message);
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

    @Override
    public void onDestroy() {
        super.onDestroy();
        isCancelRequest = VolleyRequest.cancelRequest(tag);
        editOldPassword = null;
        editNewPassword = null;
        editNewPasswordConfirm = null;
        oldPassword = null;
        newPassword = null;
        passwordConfirm = null;
        dialog = null;
        tag = null;
    }
}
