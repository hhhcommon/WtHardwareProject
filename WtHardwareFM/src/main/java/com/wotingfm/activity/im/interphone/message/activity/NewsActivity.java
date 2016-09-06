package com.wotingfm.activity.im.interphone.message.activity;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.VolleyError;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.wotingfm.R;
import com.wotingfm.activity.im.interphone.message.adapter.NewsAdapter;
import com.wotingfm.activity.im.interphone.message.model.GroupInfo;
import com.wotingfm.activity.im.interphone.message.model.MessageInfo;
import com.wotingfm.activity.im.interphone.message.model.UserInviteMeInside;
import com.wotingfm.activity.common.baseactivity.AppBaseActivity;
import com.wotingfm.common.config.GlobalConfig;
import com.wotingfm.common.volley.VolleyCallback;
import com.wotingfm.common.volley.VolleyRequest;
import com.wotingfm.util.CommonUtils;
import com.wotingfm.util.DialogUtils;
import com.wotingfm.util.L;
import com.wotingfm.util.ToastUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * 需要处理的消息中心列表
 */
public class NewsActivity extends AppBaseActivity {
    private ListView dealMessageList;

    private String tag = "MESSAGE_NEWS_VOLLEY_REQUEST_CANCEL_TAG";
    private boolean isCancelRequest;

    protected ArrayList<GroupInfo> groupList;
    protected ArrayList<UserInviteMeInside> userList;
    private ArrayList<MessageInfo> message = new ArrayList<>();
    private NewsAdapter adapter;
    private int index;
    private Dialog delDialog;

    @Override
    protected int setViewId() {
        return R.layout.activity_news;
    }

    @Override
    protected void init() {
        setTitle("新的朋友");
        dealMessageList = findView(R.id.deal_message_list_view);
        Intent pushIntent = new Intent("push_newperson");
        Bundle bundle = new Bundle();
        bundle.putString("outmessage", "");
        pushIntent.putExtras(bundle);
        context.sendBroadcast(pushIntent);

        // 网络还没有初始化
        DialogUtils.showDialog(context);
        sendPerson();
//        if (GlobalConfig.CURRENT_NETWORK_STATE_TYPE != -1) {
//            DialogUtils.showDialog(context);
//            sendPerson();
//        } else {
//            ToastUtils.show_allways(this, "网络连接失败，请稍后重试");
//        }
        delDialog();
    }

    /**
     * 获取个人消息
     */
    private void sendPerson() {
        String url = null;
        JSONObject jsonObject = VolleyRequest.getJsonObject(context);
        try {
            jsonObject.put("UserId", CommonUtils.getUserId(this));
            url = GlobalConfig.getInvitedMeListUrl;
        } catch (JSONException e) {
            e.printStackTrace();
        }
        if (url == null) {
            Toast.makeText(context, "请求链接错误", Toast.LENGTH_SHORT).show();
            return;
        }
        VolleyRequest.RequestPost(url, tag, jsonObject, new VolleyCallback() {
            private String ReturnType;
            private String Message;

            @Override
            protected void requestSuccess(JSONObject result) {
                if (isCancelRequest) {
                    return;
                }
                String ContactMeString = null;
                try {
                    ReturnType = result.getString("ReturnType");
                    Message = result.getString("Message");
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                if (ReturnType != null && ReturnType.equals("1001")) {
                    try {
                        ContactMeString = result.getString("UserList");
                    } catch (JSONException e1) {
                        e1.printStackTrace();
                    }
                    userList = new Gson().fromJson(ContactMeString, new TypeToken<List<UserInviteMeInside>>() {}.getType());
                } else if (ReturnType != null && ReturnType.equals("1002")) {
                    L.e("邀请信息", "页面加载失败，失败原因" + Message);
                } else if (ReturnType != null && ReturnType.equals("1011")) {
                    L.e("邀请信息", "所有的邀请信息都已经处理完毕");
                } else if (Message != null && !Message.trim().equals("")) {
                    L.e("邀请信息", "页面加载失败，失败原因" + Message);
                }
                sendGroup();
            }

            @Override
            protected void requestError(VolleyError error) {
                sendGroup();
            }
        });
    }

    /**
     * 获取群组消息
     */
    private void sendGroup() {
        String url = null;
        JSONObject jsonObject = VolleyRequest.getJsonObject(context);
        try {
            jsonObject.put("UserId", CommonUtils.getUserId(this));
            url = GlobalConfig.getInvitedMeGroupListUrl;
        } catch (JSONException e) {
            e.printStackTrace();
        }
        if (url == null) {
            Toast.makeText(context, "请求链接错误", Toast.LENGTH_SHORT).show();
            return;
        }

        VolleyRequest.RequestPost(url, tag, jsonObject, new VolleyCallback() {
            private String ReturnType;
            private String Message;

            @Override
            protected void requestSuccess(JSONObject result) {
                if (isCancelRequest) {
                    return;
                }
                String ContactMeString = null;
                try {
                    ReturnType = result.getString("ReturnType");
                    Message = result.getString("Message");
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                if (ReturnType != null && ReturnType.equals("1001")) {
                    try {
                        ContactMeString = result.getString("GroupList");
                    } catch (JSONException e1) {
                        e1.printStackTrace();
                    }
                    groupList = new Gson().fromJson(ContactMeString, new TypeToken<List<GroupInfo>>() {}.getType());
                } else if (ReturnType != null && ReturnType.equals("1002")) {
                    L.e("邀请信息", "页面加载失败，失败原因" + Message);
                } else if (ReturnType != null && ReturnType.equals("1011")) {
                    L.e("邀请信息", "无邀请我的用户组");
                } else if (Message != null && !Message.trim().equals("")) {
                    L.e("邀请信息", "页面加载失败，失败原因" + Message);
                }
                DialogUtils.closeDialog();
                setData();
            }

            @Override
            protected void requestError(VolleyError error) {
                DialogUtils.closeDialog();
            }
        });
    }

    protected void setData() {
        message.clear();
        if (userList != null && userList.size() > 0) {
            for (int i = 0; i < userList.size(); i++) {
                MessageInfo messageInfo = new MessageInfo();
                messageInfo.setMSType("person");
                messageInfo.setInviteMesage(userList.get(i).getInviteMesage());
                messageInfo.setUserId(userList.get(i).getUserId());
                messageInfo.setUserName(userList.get(i).getUserName());
                messageInfo.setType(userList.get(i).getType());
                messageInfo.setInviteTime(userList.get(i).getInviteTime());
                messageInfo.setPortrait(userList.get(i).getPortrait());
                message.add(messageInfo);
            }
        }
        if (groupList != null && groupList.size() > 0) {
            for (int i = 0; i < groupList.size(); i++) {
                MessageInfo messageInfo = new MessageInfo();
                messageInfo.setMSType("group");
                messageInfo.setType(groupList.get(i).getType());
                messageInfo.setGroupName(groupList.get(i).getGroupName());
                messageInfo.setGroupId(groupList.get(i).getGroupId());
                messageInfo.setUserName(groupList.get(i).getUserName());
                messageInfo.setPortraitMini(groupList.get(i).getProtraitMini());
                messageInfo.setUserId(groupList.get(i).getUserId());
                messageInfo.setInviteTime(groupList.get(i).getInviteTime());
                message.add(messageInfo);
            }
        }

        testData();

        if (message.size() > 0) {
            adapter = new NewsAdapter(context, message);
            dealMessageList.setAdapter(adapter);
            setAdapterListener();
        } else {
            ToastUtils.show_allways(context, "您没有未处理消息");
        }
    }

    // 测试数据  可删除 ------------------------------------------------------------------------------------------------
    private void testData(){
        MessageInfo messageInfo1 = new MessageInfo();
        messageInfo1.setMSType("person");
        messageInfo1.setInviteMesage("添加好友");
        messageInfo1.setUserId("84639b3eb658");
        messageInfo1.setUserName("我听科技");
        messageInfo1.setType(1);
        messageInfo1.setInviteTime(System.currentTimeMillis() + "");
        messageInfo1.setPortrait("http://pic.500px.me/picurl/vcg5da48ce9497b91f9c81c17958d4f882e?code=e165fb4d228d4402");
        message.add(messageInfo1);

        MessageInfo messageInfo2 = new MessageInfo();
        messageInfo2.setMSType("group");
        messageInfo2.setType(1);
        messageInfo2.setGroupName("我听技术");
        messageInfo2.setGroupId("45d4g56f5451");
        messageInfo2.setUserName("我听科技");
        messageInfo2.setPortraitMini("");
        messageInfo2.setUserId("84639b3eb658");
        messageInfo2.setInviteTime(System.currentTimeMillis() + "");
        message.add(messageInfo2);
    }
//  --------------------------------------------------------------------------------------------------------------------

    private void setAdapterListener() {
        adapter.setOnListener(new NewsAdapter.OnListener() {

            @Override
            public void agree(int position) {
                if (message != null && message.get(position) != null && message.get(position).getMSType() != null && !message.get(position).getMSType().equals("")) {
                    if (GlobalConfig.CURRENT_NETWORK_STATE_TYPE != -1) {
                        DialogUtils.showDialog(context);
                        index = position;
                        sendRequest(message.get(position), 1);
                    } else {
                        ToastUtils.show_allways(context, "网络连接失败，请稍后重试");
                    }
                }
            }

            @Override
            public void refused(int position) {
                index = position;
                delDialog.show();
            }
        });
    }

    /**
     * 处理消息对话框  同意 OR 拒绝
     */
    private void delDialog() {
        final View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_exit_confirm, null);
        TextView textCancel = (TextView) dialogView.findViewById(R.id.tv_cancle);
        TextView textTitle = (TextView) dialogView.findViewById(R.id.tv_title);
        TextView textConfirm = (TextView) dialogView.findViewById(R.id.tv_confirm);
        textTitle.setText("确定拒绝?");
        delDialog = new Dialog(context, R.style.MyDialog);
        delDialog.setContentView(dialogView);
        delDialog.setCanceledOnTouchOutside(false);
        delDialog.getWindow().setBackgroundDrawableResource(R.color.dialog);
        textCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                delDialog.dismiss();
            }
        });

        textConfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                delDialog.dismiss();
                if (message != null && message.get(index) != null && message.get(index).getMSType() != null && !message.get(index).getMSType().equals("")) {
                    if (GlobalConfig.CURRENT_NETWORK_STATE_TYPE != -1) {
                        DialogUtils.showDialog(context);
                        sendRequest(message.get(index), 2);
                    } else {
                        ToastUtils.show_allways(context, "网络连接失败，请稍后重试");
                    }
                }
            }
        });
    }

    /**
     * 处理消息 接受或者拒绝请求的方法
     */
    private void sendRequest(final MessageInfo messageInfo, final int type) {
        String url = null;
        JSONObject jsonObject = VolleyRequest.getJsonObject(context);
        try {
            jsonObject.put("UserId", CommonUtils.getUserId(this));
            if (messageInfo.getMSType().equals("person")) {
                jsonObject.put("InviteUserId", messageInfo.getUserId());
                if (type == 1) {
                    jsonObject.put("DealType", "1");
                } else if (type == 2) {
                    jsonObject.put("DealType", "2");
                }
                url = GlobalConfig.InvitedDealUrl;
            } else {
                jsonObject.put("InviteUserId", messageInfo.getUserId());
                if (type == 1) {
                    jsonObject.put("DealType", "1");
                } else if (type == 2) {
                    jsonObject.put("DealType", "2");
                }
                jsonObject.put("GroupId", messageInfo.getGroupId());
                url = GlobalConfig.InvitedGroupDealUrl;
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        if (url == null) {
            Toast.makeText(context, "请求连接错误", Toast.LENGTH_SHORT).show();
            return;
        }

        VolleyRequest.RequestPost(url, tag, jsonObject, new VolleyCallback() {
            private String ReturnType;
            private String Message;

            @Override
            protected void requestSuccess(JSONObject result) {
                DialogUtils.closeDialog();
                if (isCancelRequest) {
                    return;
                }
                try {
                    ReturnType = result.getString("ReturnType");
                    Message = result.getString("Message");
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                if (messageInfo.MSType.equals("person")) {
                    if (type == 1) {
                        if (ReturnType != null && ReturnType.equals("1001")) {
                            ToastUtils.show_allways(context, "添加成功");
                            /*
							 * 此处删除该条数据
							 */
                            message.remove(index);
                            adapter.notifyDataSetChanged();
                            Intent pushIntent = new Intent("push_refreshlinkman");
                            context.sendBroadcast(pushIntent);
                        } else if (ReturnType != null && ReturnType.equals("1002")) {
                            ToastUtils.show_allways(context, "添加失败，" + Message);
                        } else {
                            if (Message != null && !Message.trim().equals("")) {
                                ToastUtils.show_allways(context, Message + "");
                            } else {
                                ToastUtils.show_allways(context, "出现异常请稍后重试");
                            }
                        }
                    } else {
						/*
						 * 不管拒绝结果如何此条数据需要删除
						 * 此处删除该条数据
						 */
                        ToastUtils.show_allways(context, "已拒绝");
                        message.remove(index);
                        adapter.notifyDataSetChanged();
                    }
                } else {
                    if (type == 1) {
                        if (ReturnType != null && ReturnType.equals("1001")) {
                            ToastUtils.show_allways(context, "您已成功进入该组");
							/*
							 * 此处删除该条数据
							 */
                            message.remove(index);
                            adapter.notifyDataSetChanged();
                            Intent pushIntent = new Intent("push_refreshlinkman");
                            context.sendBroadcast(pushIntent);
                        } else if (ReturnType != null && ReturnType.equals("1002")) {
                            ToastUtils.show_allways(context, "添加失败，" + Message);
                        } else if (ReturnType != null && ReturnType.equals("10011")) {
                            ToastUtils.show_allways(context, "已经在组内了");
                        } else {
                            if (Message != null && !Message.trim().equals("")) {
                                ToastUtils.show_allways(context, Message + "");
                            } else {
                                ToastUtils.show_allways(context, "出现异常请稍后重试");
                            }
                        }
                    } else {
						/*
						 * 不管拒绝结果如何此条数据需要删除
						 * 此处删除该条数据
						 */
                        ToastUtils.show_allways(context, "已拒绝");
                        message.remove(index);
                        adapter.notifyDataSetChanged();
                    }
                }
            }

            @Override
            protected void requestError(VolleyError error) {
                DialogUtils.closeDialog();
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        isCancelRequest = VolleyRequest.cancelRequest(tag);
    }
}
