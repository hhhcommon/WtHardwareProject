package com.wotingfm.activity.mine.feedback.feedbacklist.activity;

import android.app.Dialog;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ExpandableListView;
import android.widget.ExpandableListView.OnGroupClickListener;
import android.widget.LinearLayout;
import com.android.volley.VolleyError;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.wotingfm.R;
import com.wotingfm.activity.common.baseactivity.BaseActivity;
import com.wotingfm.activity.mine.feedback.feedbacklist.adapter.FeedBackExpandAdapter;
import com.wotingfm.activity.mine.feedback.feedbacklist.model.OpinionMessage;
import com.wotingfm.common.config.GlobalConfig;
import com.wotingfm.common.volley.VolleyCallback;
import com.wotingfm.common.volley.VolleyRequest;
import com.wotingfm.util.DialogUtils;
import com.wotingfm.util.ToastUtils;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

/**
 * 意见反馈列表
 * 作者：xinlong on 2016/8/1 21:18
 * 邮箱：645700751@qq.com
 */
public class FeedbackListActivity extends BaseActivity {
    protected Dialog dialog;
    private ExpandableListView mListView;
    private String tag = "FEEDBACKLIST_VOLLEY_REQUEST_CANCEL_TAG";
    private boolean isCancelRequest;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_feedbacklistex);
        setView();
        if (GlobalConfig.CURRENT_NETWORK_STATE_TYPE != -1) {
            dialog = DialogUtils.Dialogph(FeedbackListActivity.this, "通讯中");
            send();
        } else {
            ToastUtils.show_short(FeedbackListActivity.this, "网络连接失败，请稍后重试");
        }
    }

    private void setView() {
        mListView = (ExpandableListView) findViewById(R.id.exlv_opinionlist);
        LinearLayout mHeadLeftLn = (LinearLayout) findViewById(R.id.head_left_btn);
        mHeadLeftLn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    private void send() {
        JSONObject jsonObject = VolleyRequest.getJsonObject(this);
        try {
            jsonObject.put("Page", "1");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        VolleyRequest.RequestPost(GlobalConfig.FeedBackListUrl, tag, jsonObject, new VolleyCallback() {
            @Override
            protected void requestSuccess(JSONObject result) {
                if (dialog != null) dialog.dismiss();
                if (isCancelRequest) return;
                try {
                    String ReturnType = result.getString("ReturnType");
                    if (ReturnType != null && ReturnType.equals("1001")) {
                        try {
                            String ResponseString = result.getString("OpinionList");
                            List<OpinionMessage> OM = new Gson().fromJson(ResponseString, new TypeToken<List<OpinionMessage>>() {
                            }.getType());
                            if (OM == null || OM.size() == 0) {
                                ToastUtils.show_short(FeedbackListActivity.this, "数据获取异常请重试");
                                return;
                            }
                            // 此处开始配置adapter
                            FeedBackExpandAdapter adapter = new FeedBackExpandAdapter(FeedbackListActivity.this, OM);
                            mListView.setGroupIndicator(null);
                            mListView.setOnGroupClickListener(new OnGroupClickListener() {
                                @Override
                                public boolean onGroupClick(ExpandableListView parent, View v, int groupPosition, long id) {
                                    return true;
                                }
                            });

                            mListView.setAdapter(adapter);
                            for (int i = 0; i < adapter.getGroupCount(); i++) {
                                mListView.expandGroup(i);
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                    } else if (ReturnType != null && ReturnType.equals("1002")) {
                        ToastUtils.show_short(getApplicationContext(), "数据获取异常请重试");
                    } else {
                        ToastUtils.show_short(getApplicationContext(), "数据获取异常请重试");
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
        dialog = null;
        mListView = null;
        setContentView(R.layout.activity_null);
    }
}
