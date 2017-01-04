package com.wotingfm.ui.mine.set.feedback.feedbacklist.activity;

import android.app.Dialog;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ExpandableListView;

import com.android.volley.VolleyError;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.wotingfm.R;
import com.wotingfm.ui.baseactivity.BaseActivity;
import com.wotingfm.ui.mine.set.feedback.feedbacklist.adapter.FeedBackExpandAdapter;
import com.wotingfm.ui.mine.set.feedback.feedbacklist.model.OpinionMessage;
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
public class FeedbackListActivity extends BaseActivity implements OnClickListener {
    private Dialog dialog;
    private ExpandableListView mListView;

    private String tag = "FEEDBACK_LIST_VOLLEY_REQUEST_CANCEL_TAG";
    private boolean isCancelRequest;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_feedbacklistex);
        setView();
    }

    private void setView() {
        findViewById(R.id.head_left_btn).setOnClickListener(this);

        mListView = (ExpandableListView) findViewById(R.id.exlv_opinionlist);
        mListView.setGroupIndicator(null);

        if (GlobalConfig.CURRENT_NETWORK_STATE_TYPE != -1) {
            dialog = DialogUtils.Dialogph(context, "通讯中");
            send();
        } else {
            ToastUtils.show_always(context, "网络连接失败，请稍后重试!");
        }
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.head_left_btn) {
            finish();
        }
    }

    private void send() {
        JSONObject jsonObject = VolleyRequest.getJsonObject(context);
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
                        String ResponseString = result.getString("OpinionList");
                        List<OpinionMessage> OM = new Gson().fromJson(ResponseString, new TypeToken<List<OpinionMessage>>() {}.getType());
                        FeedBackExpandAdapter adapter = new FeedBackExpandAdapter(context, OM);
                        mListView.setAdapter(adapter);
                        for (int i = 0; i < adapter.getGroupCount(); i++) {
                            mListView.expandGroup(i);
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

    @Override
    protected void onDestroy() {
        super.onDestroy();
        isCancelRequest = VolleyRequest.cancelRequest(tag);
        dialog = null;
        mListView = null;
        setContentView(R.layout.activity_null);
    }
}
