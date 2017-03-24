package com.wotingfm.ui.interphone.group.groupcontrol.joingrouplist;

import android.app.Dialog;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import com.android.volley.VolleyError;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.wotingfm.R;
import com.wotingfm.common.config.GlobalConfig;
import com.wotingfm.common.volley.VolleyCallback;
import com.wotingfm.common.volley.VolleyRequest;
import com.wotingfm.ui.common.model.GroupInfo;
import com.wotingfm.ui.interphone.group.groupcontrol.groupmanage.GroupManagerFragment;
import com.wotingfm.ui.interphone.group.groupcontrol.joingrouplist.adapter.JoinGroupAdapter;
import com.wotingfm.ui.interphone.group.groupcontrol.joingrouplist.model.CheckInfo;
import com.wotingfm.ui.interphone.main.DuiJiangActivity;
import com.wotingfm.util.DialogUtils;
import com.wotingfm.util.ToastUtils;
import com.wotingfm.widget.TipView;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * 审核消息
 * @author 辛龙
 * 2016年4月13日
 */
public class JoinGroupListFragment extends Fragment implements OnClickListener, JoinGroupAdapter.Callback, TipView.WhiteViewClick {
    protected JoinGroupAdapter adapter;
    private List<CheckInfo> userList;
    private List<GroupInfo> list;

    private Dialog delDialog;
    private Dialog dialog;
    private ListView joinListView;
    
    private int dealType = 1;// == 1 接受  == 2 拒绝
    private int delPosition;
    private int onClickTv;
    
    private String groupId;
    private String tag = "JOIN_GROUP_LIST_VOLLEY_REQUEST_CANCEL_TAG";
    private boolean isCancelRequest;

    private TipView tipView;// 没有网路、没有数据提示
    private FragmentActivity context;
    private View rootView;

    @Override
    public void onWhiteViewClick() {
        if (groupId != null && !groupId.equals("")) {
            if (GlobalConfig.CURRENT_NETWORK_STATE_TYPE != -1) {
                dialog = DialogUtils.Dialogph(context, "正在获取群成员信息");
                send();
            } else {
                tipView.setVisibility(View.VISIBLE);
                tipView.setTipView(TipView.TipStatus.NO_NET);
            }
        } else {
            tipView.setVisibility(View.VISIBLE);
            tipView.setTipView(TipView.TipStatus.IS_ERROR);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (rootView == null) {
            rootView = inflater.inflate(R.layout.activity_joingrouplist, container, false);
            rootView.setOnClickListener(this);
            context = getActivity();
            handleIntent();
            setView();
            delDialog();

            if (groupId != null && !groupId.equals("")) {
                if (GlobalConfig.CURRENT_NETWORK_STATE_TYPE != -1) {
                    dialog = DialogUtils.Dialogph(context, "正在获取群成员信息");
                    send();
                } else {
                    tipView.setVisibility(View.VISIBLE);
                    tipView.setTipView(TipView.TipStatus.NO_NET);
                }
            } else {
                tipView.setVisibility(View.VISIBLE);
                tipView.setTipView(TipView.TipStatus.IS_ERROR);
            }
        }
        return rootView;
    }

    private void setView() {
        tipView = (TipView) rootView.findViewById(R.id.tip_view);
        tipView.setWhiteClick(this);

        rootView.findViewById(R.id.head_left_btn).setOnClickListener(this);
        joinListView = (ListView) rootView.findViewById(R.id.lv_jiaqun);
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
                if (GlobalConfig.CURRENT_NETWORK_STATE_TYPE != -1) {
                    delDialog.dismiss();
                    dealType = 2;
                    sendRequest();
                } else {
                    ToastUtils.show_always(context, "网络失败，请检查网络");
                }
            }
        });
    }

    private void handleIntent() {
        groupId = getArguments().getString("GroupId");
        list = (ArrayList<GroupInfo>) getArguments().getSerializable("userlist");
    }

    private void send() {
        JSONObject jsonObject = VolleyRequest.getJsonObject(context);
        try {
            jsonObject.put("GroupId", groupId);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        VolleyRequest.RequestPost(GlobalConfig.checkVertifyUrl, tag, jsonObject, new VolleyCallback() {
            private String ReturnType;

            @Override
            protected void requestSuccess(JSONObject result) {
                if (dialog != null) dialog.dismiss();
                if (isCancelRequest) return;
                try {
                    ReturnType = result.getString("ReturnType");
                    if (ReturnType != null && ReturnType.equals("1001")) {
                        userList = new Gson().fromJson(result.getString("InviteUserList"), new TypeToken<List<CheckInfo>>() {}.getType());
                        // userList 未包含用户名信息，此时从上一个页面中获取
                        for (int i = 0; i < userList.size(); i++) {
                            for (int j = 0; j < list.size(); j++) {
                                if (userList.get(i).getInviteUserId() != null && userList.get(i).getInviteUserId().equals(list.get(j).getUserId())) {
                                    userList.get(i).setInvitedUserName(list.get(j).getUserName());
                                }
                            }
                        }
                        tipView.setVisibility(View.GONE);
                        adapter = new JoinGroupAdapter(context, userList, JoinGroupListFragment.this);
                        joinListView.setAdapter(adapter);
                        joinListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
                            @Override
                            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                                delDialog.show();
                                delPosition = position;
                                return false;
                            }
                        });
                    } else {
                        tipView.setVisibility(View.VISIBLE);
                        tipView.setTipView(TipView.TipStatus.NO_DATA, "暂时没有加群消息需要处理~~");
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                    tipView.setVisibility(View.VISIBLE);
                    tipView.setTipView(TipView.TipStatus.IS_ERROR);
                }
            }

            @Override
            protected void requestError(VolleyError error) {
                if (dialog != null) dialog.dismiss();
                ToastUtils.showVolleyError(context);
                tipView.setVisibility(View.VISIBLE);
                tipView.setTipView(TipView.TipStatus.IS_ERROR);
            }
        });
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.head_left_btn:
                DuiJiangActivity.close();
                break;
        }
    }

    private void sendRequest() {
        JSONObject jsonObject = VolleyRequest.getJsonObject(context);
        try {
            jsonObject.put("DealType", dealType);
            if (dealType == 1) {
                jsonObject.put("InviteUserId", userList.get(onClickTv).getInviteUserId());
                jsonObject.put("BeInvitedUserId", userList.get(onClickTv).getBeInviteUserId());
            } else {
                jsonObject.put("InviteUserId", userList.get(delPosition).getInviteUserId());
                jsonObject.put("BeInvitedUserId", userList.get(delPosition).getBeInviteUserId());
            }
            jsonObject.put("GroupId", groupId);            // groupid由上一个界面传递而来
        } catch (JSONException e) {
            e.printStackTrace();
        }

        VolleyRequest.RequestPost(GlobalConfig.checkDealUrl, tag, jsonObject, new VolleyCallback() {
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
                    if (dealType == 1) {
                        userList.get(onClickTv).setCheckType(2);
                    } else {
                        userList.remove(delPosition);
                    }
                    adapter.notifyDataSetChanged();
                    dealType = 1;
                    Fragment fg=getTargetFragment();
                    ((GroupManagerFragment)fg).RefreshFragmentManager();
                    DuiJiangActivity.close();
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
                    if (Message != null && !Message.trim().equals("")) {
                        ToastUtils.show_always(context, Message + "");
                    }
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
        onClickTv = (Integer) v.getTag();
        if (GlobalConfig.CURRENT_NETWORK_STATE_TYPE != -1) {
            dialog = DialogUtils.Dialogph(context, "正在获取数据");
            sendRequest();
        } else {
            ToastUtils.show_always(context, "网络连接失败，请稍后重试");
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        isCancelRequest = VolleyRequest.cancelRequest(tag);
        userList = null;
        list = null;
        adapter = null;
        joinListView = null;
    }
}
