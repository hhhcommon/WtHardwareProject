package com.wotingfm.ui.interphone.group.groupcontrol.modifygrouppassword;

import android.app.Dialog;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.EditText;

import com.android.volley.VolleyError;
import com.wotingfm.R;
import com.wotingfm.common.config.GlobalConfig;
import com.wotingfm.common.helper.CommonHelper;
import com.wotingfm.common.volley.VolleyCallback;
import com.wotingfm.common.volley.VolleyRequest;
import com.wotingfm.ui.interphone.main.DuiJiangActivity;
import com.wotingfm.util.DialogUtils;
import com.wotingfm.util.ToastUtils;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * 更改群密码
 *
 * @author 辛龙
 *         2016年7月19日
 */
public class ModifyGroupPasswordFragment extends Fragment implements OnClickListener {
    private Dialog dialog;
    private EditText et_oldpassword;
    private EditText et_newpassword;
    private EditText et_newpassword_confirm;

    private String oldpassword;
    private String newpassword;
    private String passwordconfirm;
    private String groupid;
    private String tag = "MODIFY_GROUP_PASSWORD_VOLLEY_REQUEST_CANCEL_TAG";

    private boolean isCancelRequest;
    private FragmentActivity context;
    private View rootView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (rootView == null) {
            rootView = inflater.inflate(R.layout.activity_modify_grouppassword, container, false);
            rootView.setOnClickListener(this);
            context = getActivity();
            initView();
        }
        return rootView;
    }

    // 初始化视图
    private void initView() {
        groupid = getArguments().getString("GroupId");// 获取上个界面传递过来的 ID

        rootView.findViewById(R.id.head_left_btn).setOnClickListener(this);
        rootView.findViewById(R.id.btn_modifypassword).setOnClickListener(this);

        et_oldpassword = (EditText) rootView.findViewById(R.id.edit_oldpassword);
        et_newpassword = (EditText) rootView.findViewById(R.id.edit_newpassword);
        et_newpassword_confirm = (EditText) rootView.findViewById(R.id.edit_confirmpassword);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.head_left_btn:
                DuiJiangActivity.close();
                break;
            case R.id.btn_modifypassword:
                if (CommonHelper.checkNetwork(context)) {
                    if (checkData()) {
                        if (groupid != null && !groupid.equals("")) {
                            send();
                        } else {
                            ToastUtils.show_always(context, "获取 groupId 失败，请返回上一级界面重试");
                        }
                    }
                }
                break;
        }
    }

    // 检查数据的正确性
    protected boolean checkData() {
        oldpassword = et_oldpassword.getText().toString().trim();
        newpassword = et_newpassword.getText().toString().trim();
        passwordconfirm = et_newpassword_confirm.getText().toString().trim();
        if ("".equalsIgnoreCase(oldpassword)) {
            ToastUtils.show_always(context, "请输入您的旧密码");
            return false;
        }
        if ("".equalsIgnoreCase(newpassword)) {
            ToastUtils.show_always(context, "请输入您的新密码");
            return false;
        }
        if (newpassword.length() < 6) {
            ToastUtils.show_always(context, "密码请输入六位以上");
            return false;
        }
        if ("".equalsIgnoreCase(newpassword)) {
            ToastUtils.show_always(context, "请再次输入密码");
            return false;
        }
        if (!newpassword.equals(passwordconfirm)) {
            ToastUtils.show_always(context, "两次输入的密码不一致");
            return false;
        }
        if (passwordconfirm.length() < 6) {
            ToastUtils.show_always(context, "密码请输入六位以上");
            return false;
        }
        return true;
    }

    protected void send() {
        dialog = DialogUtils.Dialogph(context, "修改群密码提交请求");
        JSONObject jsonObject = VolleyRequest.getJsonObject(context);
        try {
            jsonObject.put("OldPassword", oldpassword);
            jsonObject.put("NewPassword", newpassword);
            jsonObject.put("GroupId", groupid);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        VolleyRequest.RequestPost(GlobalConfig.UpdateGroupPassWordUrl, tag, jsonObject, new VolleyCallback() {
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
                    DuiJiangActivity.close();
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
        et_oldpassword = null;
        et_newpassword = null;
        et_newpassword_confirm = null;
        oldpassword = null;
        newpassword = null;
        passwordconfirm = null;
        dialog = null;
        groupid = null;
        tag = null;
    }
}
