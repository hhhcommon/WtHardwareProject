package com.wotingfm.activity.im.interphone.groupmanage.modifygrouppassword;

import android.app.Activity;
import android.app.Dialog;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import com.android.volley.VolleyError;
import com.wotingfm.R;
import com.wotingfm.common.config.GlobalConfig;
import com.wotingfm.common.volley.VolleyCallback;
import com.wotingfm.common.volley.VolleyRequest;
import com.wotingfm.util.CommonUtils;
import com.wotingfm.util.DialogUtils;
import com.wotingfm.util.ToastUtils;

import org.json.JSONException;
import org.json.JSONObject;

public class ModifyGroupPasswordActivity extends Activity {

    private ModifyGroupPasswordActivity context;
    private EditText et_oldpassword;
    private EditText et_newpassword;
    private EditText et_newpassword_confirm;
    private TextView btn_password_modify;
    private boolean flag;
    private String oldpassword;
    private String newpassword;
    private String passwordconfirm;
    private Dialog dialog;
    private String groupid;
    private String tag = "MODIFY_GROUP_PASSWORD_VOLLEY_REQUEST_CANCEL_TAG";
    private boolean isCancelRequest;
    private TextView mback;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_modify_group_password);
        context=this;
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);		// 透明状态栏
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);	// 透明导航栏
        handleIntent();
        setView();
        setListener();
    }
    private void handleIntent() {
        groupid= this.getIntent().getStringExtra("GroupId");
        groupid="81ce725fa1d3";
        /*Bundle bundle = intent.getExtras();
        groupid = bundle.getString("GroupId");*/
    }

    private void setView() {
        et_oldpassword = (EditText) findViewById(R.id.edit_oldpassword);
        et_newpassword = (EditText) findViewById(R.id.edit_newpassword);
        et_newpassword_confirm = (EditText) findViewById(R.id.edit_confirmpassword);
        btn_password_modify = (TextView) findViewById(R.id.btn_modifypassword);
        mback = (TextView) findViewById(R.id.wt_back);
    }

    private void setListener() {
        btn_password_modify.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Boolean result = checkData();
                if (result == true) {
                  /*  if (groupid != null && !groupid.equals("")) {
                        if (GlobalConfig.CURRENT_NETWORK_STATE_TYPE != -1) {*/
                            send();
							/* ToastUtil.show_short(context, "接口尚未完成"); */
                    /*    } else {
                            ToastUtils.show_allways(ModifyGroupPasswordActivity.this, "网络连接失败，请稍后重试");
                        }
                    } else {
                        ToastUtils.show_allways(context, "获取groupid失败，请返回上一级界面重试");
                    }*/
                }

            }
        });

        mback.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    protected Boolean checkData() {
        oldpassword = et_oldpassword.getText().toString().trim();
        newpassword = et_newpassword.getText().toString().trim();
        passwordconfirm = et_newpassword_confirm.getText().toString().trim();
        flag = true;
        if ("".equalsIgnoreCase(oldpassword)) {
            Toast.makeText(this, "请输入您的旧密码", Toast.LENGTH_LONG).show();
            flag = false;
            return flag;
        }
        if ("".equalsIgnoreCase(newpassword)) {
            Toast.makeText(this, "请输入您的新密码", Toast.LENGTH_LONG).show();
            flag = false;
            return flag;
        }
        if (newpassword.length() < 6) {
            Toast.makeText(this, "密码请输入六位以上", Toast.LENGTH_LONG).show();
            flag = false;
            return flag;
        }
        if ("".equalsIgnoreCase(newpassword)) {
            Toast.makeText(this, "请再次输入密码", Toast.LENGTH_LONG).show();
            flag = false;
            return flag;
        }
        if (!newpassword.equals(passwordconfirm)) {
            Toast.makeText(this, "两次输入的密码不一致", Toast.LENGTH_LONG).show();
            flag = false;
            return flag;
        }
        if (passwordconfirm.length() < 6) {
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
            jsonObject.put("UserId", CommonUtils.getUserId(this));
            jsonObject.put("OldPassword", oldpassword);
            jsonObject.put("NewPassword", newpassword);
            jsonObject.put("GroupId", groupid);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        VolleyRequest.RequestPost(GlobalConfig.UpdateGroupPassWordUrl, tag, jsonObject, new VolleyCallback() {
            //			private String SessionId;
            private String ReturnType;
            //			private String GroupName;
            private String Message;
//			private String PlayHistory;

            @Override
            protected void requestSuccess(JSONObject result) {
                if (dialog != null) {
                    dialog.dismiss();
                }
                Log.e("修改群密码返回值", ""+result.toString());
                if(isCancelRequest){
                    return ;
                }
                try {
                    // 获取列表
//					PlayHistory = result.getString("PlayHistory");
                    ReturnType = result.getString("ReturnType");
//					SessionId = result.getString("SessionId");
                    Message = result.getString("Message");
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                if (ReturnType != null && ReturnType.equals("1001")) {
                    ToastUtils.show_allways(ModifyGroupPasswordActivity.this, "密码修改成功");
                    finish();
                }else if (ReturnType != null && ReturnType.equals("1002")) {
                    ToastUtils.show_allways(ModifyGroupPasswordActivity.this, "" + Message);
                } else {
                    if (Message != null && !Message.trim().equals("")) {
                        ToastUtils.show_allways(ModifyGroupPasswordActivity.this, Message + "");
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
        et_oldpassword = null;
        et_newpassword = null;
        et_newpassword_confirm = null;
        btn_password_modify = null;
        oldpassword = null;
        newpassword = null;
        passwordconfirm = null;
        dialog = null;
        mback=null;
        context = null;
        groupid = null;
        tag = null;
        setContentView(R.layout.activity_null);
    }
}
