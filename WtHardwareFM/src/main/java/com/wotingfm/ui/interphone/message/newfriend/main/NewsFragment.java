package com.wotingfm.ui.interphone.message.newfriend.main;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.VolleyError;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.wotingfm.R;
import com.wotingfm.common.config.GlobalConfig;
import com.wotingfm.common.constant.BroadcastConstants;
import com.wotingfm.common.volley.VolleyCallback;
import com.wotingfm.common.volley.VolleyRequest;
import com.wotingfm.ui.common.model.GroupInfo;
import com.wotingfm.ui.interphone.main.DuiJiangActivity;
import com.wotingfm.ui.interphone.message.newfriend.adapter.NewsAdapter;
import com.wotingfm.ui.interphone.message.newfriend.model.MessageInFo;
import com.wotingfm.ui.interphone.model.UserInviteMeInside;
import com.wotingfm.util.DialogUtils;
import com.wotingfm.util.ToastUtils;
import com.wotingfm.widget.TipView;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * 需要处理的消息中心列表
 * 作者：xinlong on 2016/5/5 21:18
 * 邮箱：645700751@qq.com
 */

public class NewsFragment extends Fragment implements OnClickListener {
    private NewsAdapter adapter;

    private ListView mListView;
    private Dialog dialog;
    private Dialog delDialog;
    private TipView tipView;// 没有网路、没有数据提示

    private ArrayList<UserInviteMeInside> UserList;
    private ArrayList<GroupInfo> GroupList;
    private ArrayList<MessageInFo> mes = new ArrayList<>();

    private String tag = "MESSAGE_NEWS_VOLLEY_REQUEST_CANCEL_TAG";
    private boolean isCancelRequest;
    private int Position;
    private FragmentActivity context;
    private View rootView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (rootView == null) {
            rootView = inflater.inflate(R.layout.activity_messagenews, container, false);
            rootView.setOnClickListener(this);
            context = getActivity();
            setView();// 设置界面
            Intent push = new Intent(BroadcastConstants.PUSH_NEWPERSON);
            Bundle bundle = new Bundle();
            bundle.putString("outMessage", "");
            push.putExtras(bundle);
            context.sendBroadcast(push);
            if (GlobalConfig.CURRENT_NETWORK_STATE_TYPE != -1) {
                dialog = DialogUtils.Dialogph(context, "通讯中");
                sendPerson();
            } else {
                tipView.setVisibility(View.VISIBLE);
                tipView.setTipView(TipView.TipStatus.NO_NET);
            }
            delDialog();
        }
        return rootView;
    }

    private void delDialog() {
        final View dialog1 = LayoutInflater.from(context).inflate(R.layout.dialog_exit_confirm, null);
        TextView tv_cancel = (TextView) dialog1.findViewById(R.id.tv_cancle);
        TextView tv_title = (TextView) dialog1.findViewById(R.id.tv_title);
        TextView tv_confirm = (TextView) dialog1.findViewById(R.id.tv_confirm);
        tv_title.setText("确定拒绝?");
        delDialog = new Dialog(context, R.style.MyDialog);
        delDialog.setContentView(dialog1);
        delDialog.setCanceledOnTouchOutside(false);
        delDialog.getWindow().setBackgroundDrawableResource(R.color.dialog);
        tv_cancel.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                delDialog.dismiss();
            }
        });

        tv_confirm.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                delDialog.dismiss();
                if (mes != null && mes.get(Position) != null && mes.get(Position).getMSType() != null && !mes.get(Position).getMSType().equals("")) {
                    if (GlobalConfig.CURRENT_NETWORK_STATE_TYPE != -1) {
                        dialog = DialogUtils.Dialogph(context, "正在获取数据");
                        sendRequest(mes.get(Position), 2);
                    } else {
                        ToastUtils.show_always(context, "网络连接失败，请稍后重试");
                    }
                }
            }
        });
    }

    private void setView() {
        mListView = (ListView) rootView.findViewById(R.id.listview_history);
        rootView.findViewById(R.id.head_left_btn).setOnClickListener(this);

        tipView = (TipView) rootView.findViewById(R.id.tip_view);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.head_left_btn:
                DuiJiangActivity.close();
                break;
        }
    }

    private void sendPerson() {
        JSONObject jsonObject = VolleyRequest.getJsonObject(context);
        VolleyRequest.RequestPost(GlobalConfig.getInvitedMeListUrl, tag, jsonObject, new VolleyCallback() {
            @Override
            protected void requestSuccess(JSONObject result) {
                if (isCancelRequest) return;
                String ContactMeString = null;
                try {
                    String ReturnType = result.getString("ReturnType");
                    if (ReturnType != null && ReturnType.equals("1001")) {
                        try {
                            ContactMeString = result.getString("UserList");
                        } catch (JSONException e1) {
                            e1.printStackTrace();
                        }
                        UserList = new Gson().fromJson(ContactMeString, new TypeToken<List<UserInviteMeInside>>() {
                        }.getType());
                        sendGroup();
                    } else if (ReturnType != null && ReturnType.equals("1002")) {
                        try {
                            String Message = result.getString("Message");
                            if (Message != null && !Message.trim().equals("")) {
                                Log.e("邀请信息", "页面加载失败，失败原因" + Message);
                                sendGroup();
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    } else if (ReturnType != null && ReturnType.equals("1011")) {
                        Log.e("邀请信息", "所有的邀请信息都已经处理完毕");
                        sendGroup();
                    } else {
                        try {
                            String Message = result.getString("Message");
                            if (Message != null && !Message.trim().equals("")) {
                                Log.e("邀请信息", "页面加载失败，失败原因" + Message);
                                sendGroup();
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
                sendGroup();
            }
        });
    }

    private void sendGroup() {
        JSONObject jsonObject = VolleyRequest.getJsonObject(context);

        VolleyRequest.RequestPost(GlobalConfig.getInvitedMeGroupListUrl, tag, jsonObject, new VolleyCallback() {

            @Override
            protected void requestSuccess(JSONObject result) {
                if (isCancelRequest) return;
                String ContactMeString = null;
                try {
                    String ReturnType = result.getString("ReturnType");
                    if (ReturnType != null && ReturnType.equals("1001")) {
                        try {
                            ContactMeString = result.getString("GroupList");
                        } catch (JSONException e1) {
                            e1.printStackTrace();
                        }
                        GroupList = new Gson().fromJson(ContactMeString, new TypeToken<List<GroupInfo>>() {
                        }.getType());
                    } else if (ReturnType != null && ReturnType.equals("1002")) {
                        try {
                            String Message = result.getString("Message");
                            if (Message != null && !Message.trim().equals("")) {
                                Log.e("邀请信息", "页面加载失败，失败原因" + Message);
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    } else if (ReturnType != null && ReturnType.equals("1011")) {
                        Log.e("邀请信息", "无邀请我的用户组");
                    } else {
                        try {
                            String Message = result.getString("Message");
                            if (Message != null && !Message.trim().equals("")) {
                                Log.e("邀请信息", "页面加载失败，失败原因" + Message);
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                if (dialog != null) dialog.dismiss();
                setData();
            }

            @Override
            protected void requestError(VolleyError error) {
                if (dialog != null) dialog.dismiss();
                ToastUtils.showVolleyError(context);
            }
        });
    }

    protected void setData() {
        mes.clear();
        try {
            if (UserList != null && UserList.size() > 0) {
                for (int i = 0; i < UserList.size(); i++) {
                    MessageInFo msInfo = new MessageInFo();
                    msInfo.setMSType("person");
                    msInfo.setInviteMesage(UserList.get(i).getInviteMesage());
                    msInfo.setUserId(UserList.get(i).getUserId());
                    msInfo.setNickName(UserList.get(i).getNickName());
                    msInfo.setType(UserList.get(i).getType());
                    msInfo.setInviteTime(UserList.get(i).getInviteTime());
                    //				msInfo.setUserAliasName(UserList.get(i).getUserAliasName());
                    //				msInfo.setRealName(UserList.get(i).getRealName());
                    //				msInfo.setUserNum(UserList.get(i).getUserNum());
                    //				msInfo.setPhoneNum(UserList.get(i).getPhoneNum());
                    //				msInfo.setEmail(UserList.get(i).getEmail());
                    //				msInfo.setDescn(UserList.get(i).getDescn());
                    msInfo.setPortrait(UserList.get(i).getPortrait());
                    //				msInfo.setPortraitBig(UserList.get(i).getPortraitBig());
                    //				msInfo.setPortraitMini(UserList.get(i).getPortraitMini());
                    mes.add(msInfo);
                }
            }
            if (GroupList != null && GroupList.size() > 0) {
                for (int i = 0; i < GroupList.size(); i++) {
                    MessageInFo msInfo = new MessageInFo();
                    msInfo.setMSType("group");
                    msInfo.setType(GroupList.get(i).getType());
                    msInfo.setGroupName(GroupList.get(i).getGroupName());
                    msInfo.setGroupId(GroupList.get(i).getGroupId());
                    msInfo.setNickName(GroupList.get(i).getNickName());
                    msInfo.setPortraitMini(GroupList.get(i).getPortraitMini());
                    msInfo.setUserId(GroupList.get(i).getUserId());
                    msInfo.setInviteTime(GroupList.get(i).getInviteTime());
                    mes.add(msInfo);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (mes != null && mes.size() > 0) {
            tipView.setVisibility(View.GONE);
            adapter = new NewsAdapter(context, mes);
            mListView.setAdapter(adapter);
            setAdapterListener();
        } else {
            tipView.setVisibility(View.VISIBLE);
            tipView.setTipView(TipView.TipStatus.NO_DATA, "您没有新的好友消息");
        }
    }

    private void setAdapterListener() {
        adapter.setOnListener(new NewsAdapter.OnListener() {
            @Override
            public void tongyi(int position) {
                if (mes != null && mes.get(position) != null && mes.get(position).getMSType() != null && !mes.get(position).getMSType().equals("")) {
                    if (GlobalConfig.CURRENT_NETWORK_STATE_TYPE != -1) {
                        dialog = DialogUtils.Dialogph(context, "正在获取数据");
                        Position = position;
                        sendRequest(mes.get(position), 1);
                    } else {
                        ToastUtils.show_always(context, "网络连接失败，请稍后重试");
                    }
                }
            }

            @Override
            public void jujue(int position) {
                Position = position;
                delDialog.show();
            }
        });
    }

    // 处理接收或者拒绝请求的方法
    private void sendRequest(final MessageInFo messageInFo, final int type) {
        String url = null;
        JSONObject jsonObject = VolleyRequest.getJsonObject(context);
        try {
            if (messageInFo.getMSType().equals("person")) {
                jsonObject.put("InviteUserId", messageInFo.getUserId());
                if (type == 1) {
                    jsonObject.put("DealType", "1");
                } else if (type == 2) {
                    jsonObject.put("DealType", "2");
                }
                url = GlobalConfig.InvitedDealUrl;
            } else {
                jsonObject.put("InviteUserId", messageInFo.getUserId());
                if (type == 1) {
                    jsonObject.put("DealType", "1");
                } else if (type == 2) {
                    jsonObject.put("DealType", "2");
                }
                jsonObject.put("GroupId", messageInFo.getGroupId());
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
                if (dialog != null) dialog.dismiss();
                if (isCancelRequest) return;
                try {
                    ReturnType = result.getString("ReturnType");
                    Message = result.getString("Message");
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                if (messageInFo.MSType.equals("person")) {
                    if (type == 1) {
                        if (ReturnType != null && ReturnType.equals("1001")) {
                            ToastUtils.show_always(context, "添加成功");
                            mes.remove(Position);// 此处删除该条数据
                            adapter.notifyDataSetChanged();
                            context.sendBroadcast(new Intent(BroadcastConstants.PUSH_REFRESH_LINKMAN));
                        } else if (ReturnType != null && ReturnType.equals("1002")) {
                            ToastUtils.show_always(context, "添加失败，" + Message);
                        } else {
                            if (Message != null && !Message.trim().equals("")) {
                                ToastUtils.show_always(context, Message + "");
                            } else {
                                ToastUtils.show_always(context, "出现异常请稍后重试");
                            }
                        }
                    } else {
                        /*
                         * 不管拒绝结果如何此条数据需要删除
						 * 此处删除该条数据
						 */
                        ToastUtils.show_always(context, "已拒绝");
                        mes.remove(Position);
                        adapter.notifyDataSetChanged();
                    }
                } else {
                    if (type == 1) {
                        if (ReturnType != null && ReturnType.equals("1001")) {
                            ToastUtils.show_always(context, "您已成功进入该组");
                            mes.remove(Position);// 此处删除该条数据
                            adapter.notifyDataSetChanged();
                            context.sendBroadcast(new Intent(BroadcastConstants.PUSH_REFRESH_LINKMAN));
                        } else if (ReturnType != null && ReturnType.equals("1002")) {
                            ToastUtils.show_always(context, "添加失败，" + Message);
                        } else if (ReturnType != null && ReturnType.equals("10011")) {
                            ToastUtils.show_always(context, "已经在组内了");
                        } else {
                            if (Message != null && !Message.trim().equals("")) {
                                ToastUtils.show_always(context, Message + "");
                            } else {
                                ToastUtils.show_always(context, "出现异常请稍后重试");
                            }
                        }
                    } else {
                        /*
                         * 不管拒绝结果如何此条数据需要删除
						 * 此处删除该条数据
						 */
                        ToastUtils.show_always(context, "已拒绝");
                        mes.remove(Position);
                        adapter.notifyDataSetChanged();
                    }
                }
            }

            @Override
            protected void requestError(VolleyError error) {
                if (dialog != null) dialog.dismiss();
            }
        });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        isCancelRequest = VolleyRequest.cancelRequest(tag);
        mListView = null;
    }
}
