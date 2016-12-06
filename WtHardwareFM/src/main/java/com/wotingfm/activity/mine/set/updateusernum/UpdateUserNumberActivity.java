package com.wotingfm.activity.mine.set.updateusernum;

import android.app.Dialog;
import android.content.SharedPreferences;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
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

/**
 * 设置用户号  只能设置一次
 * Created by Administrator on 2016/11/9 0009.
 */
public class UpdateUserNumberActivity extends AppBaseActivity implements View.OnClickListener {
    private Dialog confirmDialog;
    private Dialog dialog;
    private EditText editUserNum;
    private Button btnConfirm;
    private TextView textDesc;

    private String userNum;    // 用户输入的用户号码
    private String tag = "UPDATE_USER_NUM_VOLLEY_REQUEST_CANCEL_TAG";
    private boolean isCancelRequest;

    @Override
    protected int setViewId() {
        return R.layout.activity_update_user_num;
    }

    @Override
    protected void init() {
        setTitle("用户号");

        editUserNum = (EditText) findViewById(R.id.edit_usr_num);
        editUserNum.addTextChangedListener(new MyEditListener());

        btnConfirm = (Button) findViewById(R.id.btn_confirm);
        btnConfirm.setOnClickListener(this);

        initDialog();
    }

    // 初始化提示对话框
    private void initDialog() {
        View dialog = LayoutInflater.from(context).inflate(R.layout.dialog_usernumber, null);
        dialog.findViewById(R.id.tv_cancel).setOnClickListener(this);
        dialog.findViewById(R.id.tv_confirm).setOnClickListener(this);

        textDesc = (TextView) dialog.findViewById(R.id.tv_desc);
        confirmDialog = new Dialog(context, R.style.MyDialog);
        confirmDialog.setContentView(dialog);
        confirmDialog.setCanceledOnTouchOutside(true);
        confirmDialog.getWindow().setBackgroundDrawableResource(R.color.dialog);
    }

    // 判断数据是否填写完整
    private boolean isComplete() {
        userNum = editUserNum.getText().toString().trim();
        return !"".equalsIgnoreCase(userNum) && userNum.length() >= 6;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_confirm:      // 确定修改
                if (isComplete()) {
                    textDesc.setText("用户号是账号的唯一凭证，只能修改一次。\n\n请再次确认，用户号：" + userNum);
                    confirmDialog.show();
                }
                break;
            case R.id.tv_cancel:
                confirmDialog.dismiss();
                break;
            case R.id.tv_confirm:
                if (GlobalConfig.CURRENT_NETWORK_STATE_TYPE != -1) {
                    dialog = DialogUtils.Dialogph(context, "正在提交...");
                    send();
                } else {
                    ToastUtils.show_always(context, "网络连接失败，请检查网络!");
                }
                break;
        }
    }

    private void send() {
        JSONObject jsonObject = VolleyRequest.getJsonObject(context);
        try {
            jsonObject.put("UserNum", userNum);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        VolleyRequest.RequestPost(GlobalConfig.updateUserUrl, tag, jsonObject, new VolleyCallback() {
            @Override
            protected void requestSuccess(JSONObject result) {
                if (dialog != null) dialog.dismiss();
                if (isCancelRequest) return;
                try {
                    String returnType = result.getString("ReturnType");
                    if (returnType != null && returnType.equals("1001")) {
                        ToastUtils.show_always(context, "用户号修改成功!");
                        SharedPreferences.Editor et = BSApplication.SharedPreferences.edit();
                        et.putString(StringConstant.USER_NUM, userNum);
                        if (!et.commit()) L.w("commit", " 数据 commit 失败!");
                        setResult(1);
                        finish();
                    } else {
                        ToastUtils.show_always(context, "用户号修改失败!");
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
            if (isComplete()) {
                btnConfirm.setBackgroundResource(R.drawable.wt_commit_button_background);
            } else {
                btnConfirm.setBackgroundResource(R.drawable.bg_graybutton);
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        isCancelRequest = VolleyRequest.cancelRequest(tag);// 根据 TAG 取消网络请求
    }
}
