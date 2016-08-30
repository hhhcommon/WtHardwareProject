package com.wotingfm.activity.common.interphone.groupmanage.memberdel;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.android.volley.VolleyError;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.wotingfm.R;
import com.wotingfm.activity.common.interphone.groupmanage.allgroupmember.model.UserInfo;
import com.wotingfm.activity.common.interphone.groupmanage.memberadd.adapter.CreateGroupMembersAddAdapter;
import com.wotingfm.common.config.GlobalConfig;
import com.wotingfm.common.constant.BroadcastConstants;
import com.wotingfm.common.volley.VolleyCallback;
import com.wotingfm.common.volley.VolleyRequest;
import com.wotingfm.common.widgetui.SideBar;
import com.wotingfm.manager.MyActivityManager;
import com.wotingfm.util.CharacterParser;
import com.wotingfm.util.CommonUtils;
import com.wotingfm.util.DialogUtils;
import com.wotingfm.util.PinyinComparator_a;
import com.wotingfm.util.ToastUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MemberDelActivity extends Activity implements View.OnClickListener{

    private Context context;
    private TextView mback;
    private String groupid;
    private CharacterParser characterParser;
    private PinyinComparator_a pinyinComparator;
    private boolean isCancelRequest;
    private String  tag="GROUP_MEMBER_DEL_VOLLEY_REQUEST_CANCEL_TAG";
    private Dialog dialog;
    private TextView tvNofriends;
    private SideBar sideBar;
    private TextView dialogs;
    private ListView listView;
    private EditText et_searh_content;
    private ImageView image_clear;
    private List<UserInfo> userlist;//获取的userlist
    private List<UserInfo> userlist2=new ArrayList<UserInfo>();
    private int sum=0;// 统计点选的人数
    private TextView tv_head_name;
    private TextView tv_right;
    private CreateGroupMembersAddAdapter adapter;
    private TextView tv_head_right;
    private List<String> addlist = new ArrayList<String>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_transfer_authority);
        context=this;
        InitActivity();// 初始化透明状态栏，统一回收的方法
        InitcharacterParser();//初始化汉字转拼音类
        handleIntent();// 处理其他页面传入的数据
        setview();// 设置界面
        setlistener();// 设置监听
     /*   if (GlobalConfig.CURRENT_NETWORK_STATE_TYPE != -1) {*/
        if(groupid!=null){
            dialog = DialogUtils.Dialogph(context, "正在获取群成员信息");
            send();
        }else{
            ToastUtils.show_allways(context,"获取组ID失败");
        }
       /* } else {
            ToastUtils.show_allways(context, "网络失败，请检查网络");
        }*/
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    private void send() {
        JSONObject jsonObject = VolleyRequest.getJsonObject(context);
        try {
            // 模块属性
            jsonObject.put("GroupId", groupid);
            jsonObject.put("UserId","6c310f2884a7");
        } catch (JSONException e) {
            e.printStackTrace();
        }

        VolleyRequest.RequestPost(GlobalConfig.grouptalkUrl, tag, jsonObject, new VolleyCallback() {
            private String ReturnType;
            private String Message;

            @Override
            protected void requestSuccess(JSONObject result) {
                if (dialog != null) {
                    dialog.dismiss();
                }
                Log.e("获取群成员返回值",""+result.toString());
                if(isCancelRequest){
                    return ;
                }
                String userlist1 = null;
                try {
                    ReturnType = result.getString("ReturnType");
                    userlist1 = result.getString("UserList");
                    Message = result.getString("Message");
                } catch (JSONException e1) {
                    e1.printStackTrace();
                }
                if (ReturnType != null && ReturnType.equals("1001")) {
                    try {
                        userlist = new Gson().fromJson(userlist1,new TypeToken<List<UserInfo>>() {}.getType());
                    } catch (Exception e1) {
                        e1.printStackTrace();
                    }
                    int sum = userlist.size();
                    userlist2.clear();
                    userlist2.addAll(userlist);
                    filledData(userlist2);
                    Collections.sort(userlist2, pinyinComparator);
                    adapter = new CreateGroupMembersAddAdapter(context, userlist2);
                    listView.setAdapter(adapter);
                    setinterface();
                } else if (ReturnType != null && ReturnType.equals("1002")) {
                    if(userlist==null||userlist.size()==0){

                    }else{
                        int sum = userlist.size();
                        userlist2.clear();
                        userlist2.addAll(userlist);
                        filledData(userlist2);
                        Collections.sort(userlist2, pinyinComparator);
                        adapter = new CreateGroupMembersAddAdapter(context, userlist2);
                        listView.setAdapter(adapter);
                        setinterface();
                    }
                }
                if (ReturnType != null && ReturnType.equals("1002")) {
                    ToastUtils.show_allways(context, "无法获取组Id");
                } else if (ReturnType != null && ReturnType.equals("T")) {
                    ToastUtils.show_allways(context, "异常返回值");
                } else if (ReturnType != null && ReturnType.equals("1011")) {
                    ToastUtils.show_allways(context, "组中无成员");
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

    // 实例化汉字转拼音类
    private void InitcharacterParser() {
        characterParser = CharacterParser.getInstance();	// 实例化汉字转拼音类
        pinyinComparator = new PinyinComparator_a();
    }

    // 初始化界面
    private void InitActivity() {
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);		//透明状态栏
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);	//透明导航栏
        MyActivityManager mam = MyActivityManager.getInstance();
        mam.pushOneActivity(this);
    }

    // 处理Intent
    private void handleIntent() {
        groupid= this.getIntent().getStringExtra("GroupId");
        groupid="81ce725fa1d3";
    }

    //设置监听
    private void setlistener() {
        tv_head_right.setOnClickListener(this);
        mback.setOnClickListener(this);
        image_clear.setOnClickListener(this);
        // 当输入框输入过汉字，且回复0后就要调用使用userlist2的原表数据
        et_searh_content.addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before,
                                      int count) {
            }
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count,
                                          int after) {
            }
            @Override
            public void afterTextChanged(Editable s) {
                String search_name = s.toString();
                if(userlist2!=null){
                    if (search_name == null || search_name.equals("")|| search_name.trim().equals("")) {
                        image_clear.setVisibility(View.INVISIBLE);
                        tvNofriends.setVisibility(View.GONE);
                        // 关键词为空
                        if (userlist == null || userlist.size() == 0) {
                            listView.setVisibility(View.GONE);
                        } else {
                            listView.setVisibility(View.VISIBLE);
                            userlist2.clear();
                            userlist2.addAll(userlist);
                            filledData(userlist2);
                            Collections.sort(userlist2, pinyinComparator);
                            adapter = new CreateGroupMembersAddAdapter(context, userlist2);
                            listView.setAdapter(adapter);
                            setinterface();
                        }
                    } else {
                        if(userlist2!=null&&userlist2.size()!=0){
                            userlist2.clear();
                            userlist2.addAll(userlist);
                            image_clear.setVisibility(View.VISIBLE);
                            search(search_name);
                        }else{
                            ToastUtils.show_allways(context,"网络异常，没有获取导数据");
                        }
                    }
                }else{
                    ToastUtils.show_allways(context,"网络异常，没有获取导数据");
                }
            }});
    }

    private void search(String search_name) {
        List<UserInfo> filterDateList = new ArrayList<UserInfo>();
        if (TextUtils.isEmpty(search_name)) {
            filterDateList = userlist2;
            tvNofriends.setVisibility(View.GONE);
        } else {
            filterDateList.clear();
            for (UserInfo sortModel : userlist2) {
                String name = sortModel.getName();
                if (name.indexOf(search_name.toString()) != -1
                        || characterParser.getSelling(name).startsWith(search_name.toString())) {
                    filterDateList.add(sortModel);
                }
            }
        }
        // 根据a-z进行排序
        Collections.sort(filterDateList, pinyinComparator);
        adapter.ChangeDate(filterDateList);
        userlist2.clear();
        userlist2.addAll(filterDateList);
        if (filterDateList.size() == 0) {
            tvNofriends.setVisibility(View.VISIBLE);
        }
    }

    private void setinterface() {

        adapter.setOnListener(new CreateGroupMembersAddAdapter.friendCheck() {
            @Override
            public void checkposition(int position) {
                sum = 0;
                if (userlist2.get(position).getCheckType() == 1) {
                    userlist2.get(position).setCheckType(2);
                } else {
                    userlist2.get(position).setCheckType(1);
                }
                for (int i = 0; i < userlist2.size(); i++) {
                    if (userlist2.get(i).getCheckType() == 2) {
                        sum++;
                    }
                }
                tv_head_right.setText("确定(" + sum + ")");
                adapter.notifyDataSetChanged();
            }
        });

        // 设置右侧触摸监听
        sideBar.setOnTouchingLetterChangedListener(new SideBar.OnTouchingLetterChangedListener() {
            @Override
            public void onTouchingLetterChanged(String s) {
                // 该字母首次出现的位置
                int position = adapter.getPositionForSection(s.charAt(0));
                if (position != -1) {
                    listView.setSelection(position);
                }
            }
        });

    }
    //设置view
    private void setview() {
        mback=(TextView)findViewById(R.id.wt_back);
        tvNofriends = (TextView) findViewById(R.id.title_layout_no_friends);
        sideBar = (SideBar) findViewById(R.id.sidebar);
        dialogs = (TextView) findViewById(R.id.dialog);
        sideBar.setTextView(dialogs);
        listView = (ListView) findViewById(R.id.country_lvcountry);
        et_searh_content = (EditText) findViewById(R.id.et_search);			// 搜索控件
        image_clear = (ImageView) findViewById(R.id.image_clear);
        tv_head_name=(TextView)findViewById(R.id.tv_head_name);
        tv_head_right=(TextView)findViewById(R.id.tv_head_right);

    }

    //设置onclick监听
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.wt_back:
                finish();
                break;
            case R.id.image_clear:
                image_clear.setVisibility(View.INVISIBLE);
                et_searh_content.setText("");
                tvNofriends.setVisibility(View.GONE);
                break;
            case R.id.tv_head_right:
                if (userlist2 != null && userlist2.size() > 0) {
                    for (int i = 0; i < userlist2.size(); i++) {
                        if (userlist2.get(i).getCheckType() == 2) {
                            addlist.add(userlist2.get(i).getUserId());
                        }
                    }
                }
                if (addlist!= null &&addlist.size() > 0) {
                    // 发送进入组的邀请
                    /*if (GlobalConfig.CURRENT_NETWORK_STATE_TYPE != -1) {
                        dialog = DialogUtils.Dialogph(context, "正在发送邀请");*/
                        sendMemberDelete();
                   /* } else {
                        ToastUtils.show_allways(context, "网络失败，请检查网络");
                    }
                } else {
                    ToastUtils.show_allways(context, "请您勾选您要邀请的好友");
                }*/
                break;
        }
    }}
    private  void  filledData(List<UserInfo> person) {
        for (int i = 0; i < person.size(); i++) {
            person.get(i).setName(person.get(i).getUserName());
            // 汉字转换成拼音
            String pinyin = characterParser.getSelling(person.get(i).getUserName());
            String sortString = pinyin.substring(0, 1).toUpperCase();
            // 正则表达式，判断首字母是否是英文字母
            if (sortString.matches("[A-Z]")) {
                person.get(i).setSortLetters(sortString.toUpperCase());
            } else {
                person.get(i).setSortLetters("#");
            }
        }
    }
    private void sendMemberDelete() {
        JSONObject jsonObject = VolleyRequest.getJsonObject(this);
        try {
            // 模块属性
            jsonObject.put("UserId", CommonUtils.getUserId(context));
            // 对s进行处理 去掉"[]"符号
            String s = userlist2.toString().replaceAll(" ", "");
            jsonObject.put("UserIds", s.substring(1, s.length() - 1));
            // groupid由上一个界面传递而来
            jsonObject.put("GroupId", groupid);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        VolleyRequest.RequestPost(GlobalConfig.KickOutMembersUrl, tag, jsonObject, new VolleyCallback() {
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
//					SessionId = result.getString("SessionId");
                    ReturnType = result.getString("ReturnType");
                    Message = result.getString("Message");
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                if (ReturnType != null && ReturnType.equals("1001")) {
                    ToastUtils.show_allways(context, "群成员已经成功删除");
                    sendBroadcast(new Intent(BroadcastConstants.REFRESH_GROUP));
                    finish();
                }else if (ReturnType != null && ReturnType.equals("1002")) {
                    ToastUtils.show_allways(context, "无法获取用户Id");
                } else if (ReturnType != null && ReturnType.equals("T")) {
                    ToastUtils.show_allways(context, "异常返回值");
                } else if (ReturnType != null && ReturnType.equals("200")) {
                    ToastUtils.show_allways(context, "尚未登录");
                } else if (ReturnType != null && ReturnType.equals("1003")) {
                    ToastUtils.show_allways(context, "异常返回值");
                } else if (ReturnType != null && ReturnType.equals("10021")) {
                    ToastUtils.show_allways(context, "用户不是该组的管理员");
                } else if (ReturnType != null && ReturnType.equals("0000")) {
                    ToastUtils.show_allways(context, "无法获取相关的参数");
                } else if (ReturnType != null && ReturnType.equals("1004")) {
                    ToastUtils.show_allways(context, "无法获取被踢出用户Id");
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


    @Override
    protected void onDestroy() {
        super.onDestroy();
        isCancelRequest = VolleyRequest.cancelRequest(tag);
        MyActivityManager mam = MyActivityManager.getInstance();
        mam.popOneActivity(this);
        mback=null;
        pinyinComparator = null;
        characterParser = null;
        tvNofriends= null;
        sideBar= null;
        dialogs= null;
        listView= null;
        et_searh_content = null;		// 搜索控件
        image_clear = null;
        tv_head_name = null;
        tv_head_right= null;
    }
}
