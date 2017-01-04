package com.wotingfm.ui.im.interphone.groupmanage.handlegroupapply;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ListView;
import android.widget.TextView;

import com.android.volley.VolleyError;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.wotingfm.R;
import com.wotingfm.ui.common.baseactivity.BaseActivity;
import com.wotingfm.ui.im.interphone.groupmanage.joingrouplist.adapter.JoinGroupAdapter;
import com.wotingfm.ui.im.interphone.groupmanage.model.UserInfo;
import com.wotingfm.common.config.GlobalConfig;
import com.wotingfm.common.volley.VolleyCallback;
import com.wotingfm.common.volley.VolleyRequest;
import com.wotingfm.util.DialogUtils;
import com.wotingfm.util.L;
import com.wotingfm.util.ToastUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * 审核群详细资料中的审核消息 -- > 群组管理员需要处理的加群消息
 * 作者：xinlong on 2016/4/13
 * 邮箱：645700751@qq.com
 */
public class HandleGroupApplyActivity extends BaseActivity implements
        OnClickListener, JoinGroupAdapter.Callback, OnItemLongClickListener {

    protected JoinGroupAdapter adapter;
    private List<UserInfo> userList;

    private Dialog dialog;
    private Dialog delDialog;
    private ListView joinGroupList;

    private int onClickTV;
    private int dealType = 1;// == 1 接受   == 2 拒绝
    private int delPosition;
    private boolean isCancelRequest;
    private String tag = "HANDLE_GROUP_APPLY_VOLLEY_REQUEST_CANCEL_TAG";
    private String groupId;
    private ArrayList<UserInfo> list;

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.wt_back:// 返回
                finish();
                break;
            case R.id.tv_cancle:// 取消
                delDialog.dismiss();
                break;
            case R.id.tv_confirm:// 确定拒绝
                delDialog.dismiss();
                dealType = 2;
                sendRequest();
                break;
        }
    }

    @Override
    public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
        delDialog.show();
        delPosition = position;
        return false;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_join_group_list);

        delDialog();// 初始化对话框
        initView();
    }

    private void delDialog() {
        final View dialog1 = LayoutInflater.from(context).inflate(R.layout.dialog_exit_confirm, null);
        dialog1.findViewById(R.id.tv_cancle).setOnClickListener(this);// 取消
        dialog1.findViewById(R.id.tv_confirm).setOnClickListener(this);// 确定

        TextView textTitle = (TextView) dialog1.findViewById(R.id.tv_title);
        textTitle.setText("确定拒绝?");
        delDialog = new Dialog(context, R.style.MyDialog);
        delDialog.setContentView(dialog1);
        delDialog.setCanceledOnTouchOutside(false);
        delDialog.getWindow().setBackgroundDrawableResource(R.color.dialog);
    }

    // 初始化视图
    private void initView() {
        findViewById(R.id.wt_back).setOnClickListener(this);
        joinGroupList = (ListView) findViewById(R.id.lv_jiaqun);

        Intent intent = this.getIntent();
        Bundle bundle = intent.getExtras();
        groupId = bundle.getString("GroupId");
        if(groupId == null || groupId.equals("")) {
            ToastUtils.show_always(context, "获取 groupId 失败，请返回重试!");
            return ;
        }

        dialog = DialogUtils.Dialogph(context, "正在获取群成员信息");
        send();
    }

    // 获取需要审核的消息列表
    private void send() {
        // 以下需要网络支持 没有网络则直接提醒用户设置网络
        if(GlobalConfig.CURRENT_NETWORK_STATE_TYPE == -1) {
            ToastUtils.show_always(context, "网络连接失败，请检查网络!");
            return ;
        }

        JSONObject jsonObject = VolleyRequest.getJsonObject(context);
        try {
            jsonObject.put("GroupId", groupId);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        VolleyRequest.RequestPost(GlobalConfig.JoinGroupListUrl, tag, jsonObject, new VolleyCallback() {
            @Override
            protected void requestSuccess(JSONObject result) {
                if (dialog != null) dialog.dismiss();
                if (isCancelRequest) return;
                try {
                    String ReturnType = result.getString("ReturnType");
                    L.v("ReturnType -- > > " + ReturnType);

                    if (ReturnType != null && ReturnType.equals("1001")) {
                        userList = new Gson().fromJson(result.getString("UserList"), new TypeToken<List<UserInfo>>() {}.getType());
                        joinGroupList.setAdapter(adapter = new JoinGroupAdapter(context, userList, HandleGroupApplyActivity.this));
                        joinGroupList.setOnItemLongClickListener(HandleGroupApplyActivity.this);
                    } else if (ReturnType != null && ReturnType.equals("1011")) {
                        ToastUtils.show_always(context, "没有待您审核的消息");
                    } else {
                        ToastUtils.show_always(context, "获取失败，请重试!");
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

    private void sendRequest() {
        // 以下需要网络支持 没有网络则直接提醒用户设置网络
        if(GlobalConfig.CURRENT_NETWORK_STATE_TYPE == -1) {
            ToastUtils.show_always(context, "网络连接失败，请检查网络!");
            return ;
        }

        JSONObject jsonObject = VolleyRequest.getJsonObject(context);
        try {
            jsonObject.put("DealType", dealType);
            if (dealType == 1) {
                jsonObject.put("ApplyUserId", userList.get(onClickTV).getUserId());
            } else {
                jsonObject.put("ApplyUserId", userList.get(delPosition).getUserId());
            }
            jsonObject.put("GroupId", groupId);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        VolleyRequest.RequestPost(GlobalConfig.applyDealUrl, tag, jsonObject, new VolleyCallback() {
            @Override
            protected void requestSuccess(JSONObject result) {
                if (dialog != null) dialog.dismiss();
                if (isCancelRequest) return;
                try {
                    String ReturnType = result.getString("ReturnType");
                    if (ReturnType != null && ReturnType.equals("1001")) {
                        if (dealType == 1) {
                            userList.get(onClickTV).setCheckType(2);
                        } else {
                            userList.remove(delPosition);
                        }
                        adapter.notifyDataSetChanged();
                        dealType = 1;
                        setResult(1);
                    } else if (ReturnType != null && ReturnType.equals("1002")) {
                        ToastUtils.show_always(context, "无法获取用户Id");
                    } else if (ReturnType != null && ReturnType.equals("T")) {
                        ToastUtils.show_always(context, "异常返回值");
                    } else if (ReturnType != null && ReturnType.equals("200")) {
                        ToastUtils.show_always(context, "尚未登录");
                    } else if (ReturnType != null && ReturnType.equals("1003")) {
                        ToastUtils.show_always(context, "异常返回值");
                    } else if (ReturnType != null && ReturnType.equals("10031")) {
                        ToastUtils.show_always(context, "用户组不是验证群，不能采取这种方式邀请");
                    } else if (ReturnType != null && ReturnType.equals("0000")) {
                        ToastUtils.show_always(context, "无法获取用户ID");
                    } else if (ReturnType != null && ReturnType.equals("1004")) {
                        ToastUtils.show_always(context, "被邀请人不存在");
                    } else if (ReturnType != null && ReturnType.equals("1011")) {
                        ToastUtils.show_always(context, "没有待您审核的消息");
                    } else {
                        try {
                            String Message = result.getString("Message");
                            if (Message != null && !Message.trim().equals("")) {
                                ToastUtils.show_always(context, Message + "");
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
                    dealType = 1;
                }
                ToastUtils.showVolleyError(context);
            }
        });
    }

    @Override
    public void click(View v) {
        onClickTV = (int) v.getTag();
        dialog = DialogUtils.Dialogph(context, "正在获取数据");
        sendRequest();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        isCancelRequest = VolleyRequest.cancelRequest(tag);
        userList = null;
        adapter = null;
        joinGroupList = null;
        setContentView(R.layout.activity_null);
    }
}
