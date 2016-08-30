package com.wotingfm.activity.common.interphone.groupmanage.joingroup.activity;


import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.android.volley.VolleyError;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.wotingfm.R;
import com.wotingfm.activity.common.interphone.groupmanage.allgroupmember.model.UserInfo;
import com.wotingfm.activity.common.interphone.groupmanage.joingroup.activity.adapter.joingrouplistadapter;
import com.wotingfm.common.config.GlobalConfig;
import com.wotingfm.common.volley.VolleyCallback;
import com.wotingfm.common.volley.VolleyRequest;
import com.wotingfm.manager.MyActivityManager;
import com.wotingfm.util.CommonUtils;
import com.wotingfm.util.DialogUtils;
import com.wotingfm.util.ToastUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * 审核消息
 * @author 辛龙
 * 2016年4月13日
 */
public class JoinGroupListActivity extends Activity implements OnClickListener, joingrouplistadapter.Callback {
    private JoinGroupListActivity context;
    private Dialog dialog;
    private String groupid;
    private ListView lv_jiaqun;
    private LinearLayout lin_left;
    protected joingrouplistadapter adapter;
    private List<UserInfo> userlist ;
    private Integer onclicktv;
    private int dealtype=1;//1接受2拒绝
    private Dialog DelDialog;
    private int delposition;
    private String tag = "JOIN_GROUP_LIST_VOLLEY_REQUEST_CANCEL_TAG";
    private boolean isCancelRequest;
    private ArrayList<UserInfo> list;
    private TextView mback;
    private TextView tv_head_name;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_join_group_list);
        context = this;
        MyActivityManager mam = MyActivityManager.getInstance();
        mam.pushOneActivity(context);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);		// 透明状态栏
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);	// 透明导航栏
        handleIntent();
        setview();
        setlistener();
      /*  if (groupid != null && !groupid.equals("")) {
            if (GlobalConfig.CURRENT_NETWORK_STATE_TYPE != -1) {
                dialog = DialogUtils.Dialogph(context, "正在获取群成员信息");
                send();
         } else {
               ToastUtils.show_allways(context, "网络失败，请检查网络");
           }
        } else {
            ToastUtils.show_allways(context, "获取groupid失败，请返回上一级界面重试");
        }*/
        send();
        DelDialog();
    }

    @Override
    protected void onResume() {
        super.onResume();
        tv_head_name.setText("审核消息");
    }

    private void DelDialog() {
        final View dialog1 = LayoutInflater.from(this).inflate(R.layout.dialog_exit_confirm, null);
        TextView tv_cancle = (TextView) dialog1.findViewById(R.id.tv_cancle);
        TextView tv_title = (TextView) dialog1.findViewById(R.id.tv_title);
        TextView tv_confirm = (TextView) dialog1.findViewById(R.id.tv_confirm);
        tv_title.setText("确定拒绝?");
        DelDialog = new Dialog(this, R.style.MyDialog);
        DelDialog.setContentView(dialog1);
        DelDialog.setCanceledOnTouchOutside(false);
        DelDialog.getWindow().setBackgroundDrawableResource(R.color.dialog);
        tv_cancle.setOnClickListener(new OnClickListener() {
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
                    dealtype=2;
                    sendrequest();
                } else {
                    ToastUtils.show_allways(context, "网络失败，请检查网络");
                }
            }
        });
    }

    private void handleIntent() {
        groupid= this.getIntent().getStringExtra("GroupId");
        groupid="cf4e42f02b39";
    }

    private void send() {
        JSONObject jsonObject = VolleyRequest.getJsonObject(context);
        try {
            jsonObject.put("UserId", "6c310f2884a7");
            jsonObject.put("GroupId", groupid);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        VolleyRequest.RequestPost(GlobalConfig.checkVertifyUrl, tag, jsonObject, new VolleyCallback() {
            //			private String SessionId;
            private String ReturnType;
            private String Message;

            @Override
            protected void requestSuccess(JSONObject result) {
                if (dialog != null) {
                    dialog.dismiss();
                }
                if(isCancelRequest){
                    return ;
                }
                String userlist1 = null;
                try {
                    ReturnType = result.getString("ReturnType");
//					SessionId = result.getString("SessionId");
                    userlist1 = result.getString("InviteUserList");
                    Message = result.getString("Message");
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                if (ReturnType != null && ReturnType.equals("1001")) {
                    try {
                        userlist = new Gson().fromJson(userlist1,new TypeToken<List<UserInfo>>() {}.getType());
                    } catch (Exception e1) {
                        e1.printStackTrace();
                    }
                    //userlist未包含用户名信息，此时从上一个页面中获取
                    for(int i=0;i<userlist.size();i++){
                        for(int j=0;j<list.size();j++){
                            if(userlist.get(i).getInviteUserId()!=null&&userlist.get(i).getInviteUserId().equals(list.get(j).getUserId())){
                                userlist.get(i).setInvitedUserName(list.get(j).getUserName());
                            }
                        }
                    }
                    adapter = new joingrouplistadapter(context, userlist,context);
                    lv_jiaqun.setAdapter(adapter);
                    lv_jiaqun.setOnItemLongClickListener(new OnItemLongClickListener() {

                        @Override
                        public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                            DelDialog.show();
                            delposition=position;
                            return false;
                        }
                    });
                }else if (ReturnType != null && ReturnType.equals("1002")) {
                    ToastUtils.show_allways(context, "无法获取用户Id");
                } else if (ReturnType != null && ReturnType.equals("T")) {
                    ToastUtils.show_allways(context, "异常返回值");
                } else if (ReturnType != null && ReturnType.equals("1011")) {
                    ToastUtils.show_allways(context, "没有待您审核的消息");
                } else {
                    if (Message != null && !Message.trim().equals("")) {
                        ToastUtils.show_allways(context, Message + "");
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

    private void setlistener() {
        mback.setOnClickListener(this);
    }

    private void setview() {
        lv_jiaqun = (ListView) findViewById(R.id.lv_jiaqun);
        mback=(TextView)findViewById(R.id.wt_back);
        tv_head_name=(TextView)findViewById(R.id.tv_head_name);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.wt_back:
                finish();
                break;
        }
    }

    private void sendrequest() {
        JSONObject jsonObject = VolleyRequest.getJsonObject(context);
        try {
            // 模块属性
            jsonObject.put("UserId", "6c310f2884a7");
            jsonObject.put("DealType", dealtype);
            if(dealtype==1){
                jsonObject.put("InviteUserId", userlist.get(onclicktv).getInviteUserId());
                jsonObject.put("BeInvitedUserId", userlist.get(onclicktv).getBeInviteUserId());
            }else{
                jsonObject.put("InviteUserId", userlist.get(delposition).getInviteUserId());
                jsonObject.put("BeInvitedUserId", userlist.get(delposition).getBeInviteUserId());
            }
            jsonObject.put("GroupId", groupid);			// groupid由上一个界面传递而来
        } catch (JSONException e) {
            e.printStackTrace();
        }

        VolleyRequest.RequestPost(GlobalConfig.checkDealUrl, tag, jsonObject, new VolleyCallback() {
            private String ReturnType;
            private String Message;
//			private String SessionId;

            @Override
            protected void requestSuccess(JSONObject result) {
                if (dialog != null) {
                    dialog.dismiss();
                }
                if(isCancelRequest){
                    return ;
                }
                try {
                    ReturnType = result.getString("ReturnType");
//					SessionId = result.getString("SessionId");
                    Message = result.getString("Message");
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                if (ReturnType != null && ReturnType.equals("1001")) {
                    if(dealtype == 1){
                        userlist.get(onclicktv).setCheckType(2);
                    }else{
                        userlist.remove(delposition);
                    }
                    adapter.notifyDataSetChanged();
                    dealtype = 1;
                } else if (ReturnType != null && ReturnType.equals("1002")) {
                    ToastUtils.show_allways(context, "无法获取用户Id");
                } else if (ReturnType != null && ReturnType.equals("T")) {
                    ToastUtils.show_allways(context, "异常返回值");
                } else if (ReturnType != null && ReturnType.equals("200")) {
                    ToastUtils.show_allways(context, "尚未登录");
                } else if (ReturnType != null && ReturnType.equals("1003")) {
                    ToastUtils.show_allways(context, "异常返回值");
                } else if (ReturnType != null && ReturnType.equals("10031")) {
                    ToastUtils.show_allways(context, "用户组不是验证群，不能采取这种方式邀请");
                } else if (ReturnType != null && ReturnType.equals("0000")) {
                    ToastUtils.show_allways(context, "无法获取用户ID");
                } else if (ReturnType != null && ReturnType.equals("1004")) {
                    ToastUtils.show_allways(context, "被邀请人不存在");
                } else if (ReturnType != null && ReturnType.equals("1011")) {
                    ToastUtils.show_allways(context, "没有待您审核的消息");
                } else {
                    if (Message != null && !Message.trim().equals("")) {
                        ToastUtils.show_allways(context, Message + "");
                    }
                }
            }

            @Override
            protected void requestError(VolleyError error) {
                if (dialog != null) {
                    dialog.dismiss();
                    dealtype = 1;
                }
            }
        });
    }

    @Override
    public void click(View v) {
        onclicktv = (Integer) v.getTag();
        if (GlobalConfig.CURRENT_NETWORK_STATE_TYPE != -1) {
            dialog = DialogUtils.Dialogph(context, "正在获取数据");
            sendrequest();
        } else {
            ToastUtils.show_allways(this, "网络连接失败，请稍后重试");
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        isCancelRequest = VolleyRequest.cancelRequest(tag);
        MyActivityManager mam = MyActivityManager.getInstance();
        mam.popOneActivity(context);
        userlist = null;
        list = null;
        adapter = null;
        lv_jiaqun = null;
        lin_left = null;
        context = null;
        setContentView(R.layout.activity_null);
    }
}
