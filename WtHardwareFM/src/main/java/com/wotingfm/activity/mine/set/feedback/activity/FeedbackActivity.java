package com.wotingfm.activity.mine.set.feedback.activity;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.VolleyError;
import com.wotingfm.R;
import com.wotingfm.activity.common.baseactivity.BaseActivity;
import com.wotingfm.activity.mine.set.feedback.feedbacklist.activity.FeedbackListActivity;
import com.wotingfm.common.config.GlobalConfig;
import com.wotingfm.common.volley.VolleyCallback;
import com.wotingfm.common.volley.VolleyRequest;
import com.wotingfm.util.DialogUtils;
import com.wotingfm.util.ToastUtils;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * 提交意见反馈
 * 作者：xinlong on 2016/8/1 21:18
 * 邮箱：645700751@qq.com
 */
public class FeedbackActivity extends BaseActivity implements OnClickListener {
    private EditText mEditContent;
    private Dialog dialog;
    private String sEditContent;
    private String tag = "FEEDBACK_VOLLEY_REQUEST_CANCEL_TAG";
    private boolean isCancelRequest;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_feedback);
        setView();
    }

    private void setView() {
        mEditContent = (EditText) findViewById(R.id.edit_feedback_content);
        TextView mBtnFeedback = (TextView) findViewById(R.id.submit_button);
        mBtnFeedback.setOnClickListener(this);
        LinearLayout mHeadLeftLn = (LinearLayout) findViewById(R.id.head_left_btn);
        mHeadLeftLn.setOnClickListener(this);
        LinearLayout mHeadRightLn = (LinearLayout) findViewById(R.id.head_right_btn);
        mHeadRightLn.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.submit_button:
                checkData();
                break;
            case R.id.head_left_btn:
                finish();
                break;
            case R.id.head_right_btn:
                Intent intent = new Intent(this, FeedbackListActivity.class);
                startActivity(intent);
                break;
        }
    }

    private void checkData() {
        sEditContent = mEditContent.getText().toString().trim();
        if ("".equalsIgnoreCase(sEditContent)) {
            Toast.makeText(this, "请您输入您的意见", Toast.LENGTH_LONG).show();
        } else {
            if (GlobalConfig.CURRENT_NETWORK_STATE_TYPE != -1) {
                dialog = DialogUtils.Dialogph(FeedbackActivity.this, "反馈中");
                send();
            } else {
                ToastUtils.show_always(FeedbackActivity.this, "网络失败，请检查网络");
            }
        }
    }

    private void send() {
        JSONObject jsonObject = VolleyRequest.getJsonObject(this);
        try {
            jsonObject.put("PCDType", GlobalConfig.PCDType);
            jsonObject.put("Opinion", sEditContent);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        VolleyRequest.RequestPost(GlobalConfig.FeedBackUrl, tag, jsonObject, new VolleyCallback() {
            @Override
            protected void requestSuccess(JSONObject result) {
                if (dialog != null) dialog.dismiss();
                if (isCancelRequest) return;
                try {
                    String ReturnType = result.getString("ReturnType");
                    if (ReturnType != null && ReturnType.equals("1001")) {
                        ToastUtils.show_always(getApplicationContext(), "提交成功");
                        Intent intent = new Intent(FeedbackActivity.this, FeedbackListActivity.class);
                        startActivity(intent);
                        finish();
                    } else if (ReturnType != null && ReturnType.equals("1002")) {
                        ToastUtils.show_always(FeedbackActivity.this, "出错了,提交失败");
                    } else {
                        try {
                            String Message = result.getString("Message");
                            if (Message != null && !Message.trim().equals("")) {
                                Toast.makeText(FeedbackActivity.this, "提交意见反馈失败, " + Message, Toast.LENGTH_SHORT).show();
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
                if (dialog != null) dialog.dismiss();
                ToastUtils.show_always(FeedbackActivity.this, "出错了,提交失败");
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        isCancelRequest = VolleyRequest.cancelRequest(tag);
        mEditContent = null;
        dialog = null;
        sEditContent = null;
        tag = null;
        mEditContent = null;
        setContentView(R.layout.activity_null);
    }
}