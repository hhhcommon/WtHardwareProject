package com.wotingfm.activity.im.interphone.groupmanage.joingrouplist.activity;


import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.android.volley.VolleyError;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.wotingfm.R;
import com.wotingfm.activity.common.baseactivity.BaseActivity;
import com.wotingfm.activity.im.interphone.groupmanage.joingrouplist.adapter.joingrouplistadapter;
import com.wotingfm.activity.im.interphone.groupmanage.model.UserInfo;
import com.wotingfm.common.config.GlobalConfig;
import com.wotingfm.common.volley.VolleyCallback;
import com.wotingfm.common.volley.VolleyRequest;
import com.wotingfm.util.DialogUtils;
import com.wotingfm.util.ToastUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * 审核消息
 * 作者：xinlong on 2016/4/13
 * 邮箱：645700751@qq.com
 */
public class JoinGroupListActivity extends BaseActivity implements OnClickListener, joingrouplistadapter.Callback {
    private JoinGroupListActivity context;
    private Dialog dialog;
    private String groupId;
    private ListView lv_jiaqun;
    private LinearLayout lin_left;
    protected joingrouplistadapter adapter;
    private List<UserInfo> userList;
    private Integer onClickTV;
    private int dealType = 1;//1接受2拒绝
    private Dialog DelDialog;
    private int delPosition;
    private String tag = "JOIN_GROUP_LIST_VOLLEY_REQUEST_CANCEL_TAG";
    private boolean isCancelRequest;
    private ArrayList<UserInfo> list;
    private TextView mBack;
    private TextView tv_head_name;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_join_group_list);
        context = this;
        handleIntent();
        setView();
        setListener();
        if (groupId != null && !groupId.equals("")) {
            if (GlobalConfig.CURRENT_NETWORK_STATE_TYPE != -1) {
                dialog = DialogUtils.Dialogph(context, "正在获取群成员信息");
                send();
            } else {
                ToastUtils.show_always(context, "网络失败，请检查网络");
            }
        } else {
            ToastUtils.show_always(context, "获取groupid失败，请返回上一级界面重试");
        }
        DelDialog();
    }

    @Override
    protected void onResume() {
        super.onResume();
        tv_head_name.setText("审核消息");
    }

    private void DelDialog() {
        final View dialog1 = LayoutInflater.from(this).inflate(R.layout.dialog_exit_confirm, null);
        TextView tv_cancel = (TextView) dialog1.findViewById(R.id.tv_cancle);
        TextView tv_title = (TextView) dialog1.findViewById(R.id.tv_title);
        TextView tv_confirm = (TextView) dialog1.findViewById(R.id.tv_confirm);
        tv_title.setText("确定拒绝?");
        DelDialog = new Dialog(this, R.style.MyDialog);
        DelDialog.setContentView(dialog1);
        DelDialog.setCanceledOnTouchOutside(false);
        DelDialog.getWindow().setBackgroundDrawableResource(R.color.dialog);
        tv_cancel.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                DelDialog.dismiss();
            }
        });
        tv_confirm.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (GlobalConfig.CURRENT_NETWORK_STATE_TYPE != -1) {
                    DelDialog.dismiss();
                    dealType = 2;
                    sendRequest();
                } else {
                    ToastUtils.show_always(context, "网络失败，请检查网络");
                }
            }
        });
    }

    private void handleIntent() {
        groupId = this.getIntent().getStringExtra("GroupId");

    }

    private void send() {
        JSONObject jsonObject = VolleyRequest.getJsonObject(context);
        try {
            jsonObject.put("GroupId", groupId);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        VolleyRequest.RequestPost(GlobalConfig.checkVertifyUrl, tag, jsonObject, new VolleyCallback() {
            @Override
            protected void requestSuccess(JSONObject result) {
                if (dialog != null) dialog.dismiss();
                if (isCancelRequest) return;
                try {
                    String ReturnType = result.getString("ReturnType");
                    if (ReturnType != null && ReturnType.equals("1001")) {
                        try {
                            String userList1 = result.getString("InviteUserList");
                            userList = new Gson().fromJson(userList1, new TypeToken<List<UserInfo>>() {
                            }.getType());//userlist未包含用户名信息，此时从上一个页面中获取
                            for (int i = 0; i < userList.size(); i++) {
                                for (int j = 0; j < list.size(); j++) {
                                    if (userList.get(i).getInviteUserId() != null && userList.get(i).getInviteUserId().equals(list.get(j).getUserId())) {
                                        userList.get(i).setInvitedUserName(list.get(j).getUserName());
                                    }
                                }
                            }
                            adapter = new joingrouplistadapter(context, userList, context);
                            lv_jiaqun.setAdapter(adapter);
                            lv_jiaqun.setOnItemLongClickListener(new OnItemLongClickListener() {

                                @Override
                                public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                                    DelDialog.show();
                                    delPosition = position;
                                    return false;
                                }
                            });

                        } catch (Exception e1) {
                            e1.printStackTrace();
                        }
                    } else if (ReturnType != null && ReturnType.equals("1002")) {
                        ToastUtils.show_always(context, "无法获取用户Id");
                    } else if (ReturnType != null && ReturnType.equals("T")) {
                        ToastUtils.show_always(context, "异常返回值");
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
                }
            }
        });
    }

    private void setListener() {
        mBack.setOnClickListener(this);
    }

    private void setView() {
        lv_jiaqun = (ListView) findViewById(R.id.lv_jiaqun);
        mBack = (TextView) findViewById(R.id.wt_back);
        tv_head_name = (TextView) findViewById(R.id.tv_head_name);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.wt_back:
                finish();
                break;
        }
    }

    private void sendRequest() {
        JSONObject jsonObject = VolleyRequest.getJsonObject(context);
        try {
            // 模块属性
            jsonObject.put("DealType", dealType);
            if (dealType == 1) {
                jsonObject.put("InviteUserId", userList.get(onClickTV).getInviteUserId());
                jsonObject.put("BeInvitedUserId", userList.get(onClickTV).getBeInviteUserId());
            } else {
                jsonObject.put("InviteUserId", userList.get(delPosition).getInviteUserId());
                jsonObject.put("BeInvitedUserId", userList.get(delPosition).getBeInviteUserId());
            }
            jsonObject.put("GroupId", groupId);            // groupid由上一个界面传递而来
        } catch (JSONException e) {
            e.printStackTrace();
        }

        VolleyRequest.RequestPost(GlobalConfig.checkDealUrl, tag, jsonObject, new VolleyCallback() {

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
            }
        });
    }

    @Override
    public void click(View v) {
        onClickTV = (Integer) v.getTag();
        if (GlobalConfig.CURRENT_NETWORK_STATE_TYPE != -1) {
            dialog = DialogUtils.Dialogph(context, "正在获取数据");
            sendRequest();
        } else {
            ToastUtils.show_always(this, "网络连接失败，请稍后重试");
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        isCancelRequest = VolleyRequest.cancelRequest(tag);
        userList = null;
        list = null;
        adapter = null;
        lv_jiaqun = null;
        lin_left = null;
        context = null;
        setContentView(R.layout.activity_null);
    }
}
