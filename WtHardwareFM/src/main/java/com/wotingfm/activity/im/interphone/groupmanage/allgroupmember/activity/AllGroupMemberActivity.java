package com.wotingfm.activity.im.interphone.groupmanage.allgroupmember.activity;


import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.android.volley.VolleyError;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.wotingfm.R;
import com.wotingfm.activity.common.baseactivity.BaseActivity;
import com.wotingfm.activity.im.interphone.creategroup.frienddetails.TalkPersonNewsActivity;
import com.wotingfm.activity.im.interphone.groupmanage.allgroupmember.adapter.CreateGroupMembersAdapter;
import com.wotingfm.activity.im.interphone.groupmanage.grouppersonnews.GroupPersonNewsActivity;
import com.wotingfm.activity.im.interphone.groupmanage.model.UserInfo;
import com.wotingfm.activity.im.interphone.linkman.view.SideBar;
import com.wotingfm.common.config.GlobalConfig;
import com.wotingfm.common.volley.VolleyCallback;
import com.wotingfm.common.volley.VolleyRequest;
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

/**
 * 所有组成员
 * 作者：xinlong on 2016/3/9
 * 邮箱：645700751@qq.com
 */
public class AllGroupMemberActivity extends BaseActivity implements View.OnClickListener {
    private Context context;
    private TextView mBack;
    private String groupId;
    private CharacterParser characterParser;
    private PinyinComparator_a pinyinComparator;
    private boolean isCancelRequest;
    private String tag = "GROUP_MEMBERS_VOLLEY_REQUEST_CANCEL_TAG";
    private Dialog dialog;
    private TextView tvNoFriends;
    private SideBar sideBar;
    private TextView dialogs;
    private ListView listView;
    private EditText et_searh_content;
    private ImageView image_clear;
    private List<UserInfo> userList;//获取的userList
    private List<UserInfo> userList2 = new ArrayList<UserInfo>();
    private CreateGroupMembersAdapter adapter;
    private TextView tv_head_name;
    private TextView tv_right;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_transfer_authority);
        context = this;
        initCharacterParser();         // 初始化汉字转拼音类
        handleIntent();                // 处理其他页面传入的数据
        setView();                     // 设置界面
        setListener();                 // 设置监听
        if (groupId != null) {
            dialog = DialogUtils.Dialogph(AllGroupMemberActivity.this, "正在获取群成员信息");
            send();
        } else {
            ToastUtils.show_always(context, "获取组ID失败");
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        tv_right = (TextView) findViewById(R.id.tv_head_right);
        tv_right.setVisibility(View.INVISIBLE);
        tv_head_name.setText("全部成员(" + 0 + ")");
    }

    private void send() {
        JSONObject jsonObject = VolleyRequest.getJsonObject(context);
        try {
            // 模块属性
            jsonObject.put("GroupId", groupId);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        VolleyRequest.RequestPost(GlobalConfig.grouptalkUrl, tag, jsonObject, new VolleyCallback() {
            @Override
            protected void requestSuccess(JSONObject result) {
                if (dialog != null) dialog.dismiss();
                if (isCancelRequest) return;
                try {
                    String ReturnType = result.getString("ReturnType");
                    if (ReturnType != null && ReturnType.equals("1001")) {
                        try {
                            String userList1 = result.getString("UserList");
                            userList = new Gson().fromJson(userList1, new TypeToken<List<UserInfo>>() {
                            }.getType());
                            int sum = userList.size();
                            // 给计数项设置值
                            tv_head_name.setText("全部成员(" + sum + ")");
                            userList2.clear();
                            userList2.addAll(userList);
                            filledData(userList2);
                            Collections.sort(userList2, pinyinComparator);
                            adapter = new CreateGroupMembersAdapter(context, userList2);
                            listView.setAdapter(adapter);
                            setInterface();
                        } catch (JSONException e1) {
                            e1.printStackTrace();
                        }
                    } else if (ReturnType != null && ReturnType.equals("1002")) {
                        if (userList == null || userList.size() == 0) {

                        } else {
                            int sum = userList.size();
                            // 给计数项设置值
                            tv_head_name.setText("全部成员(" + sum + ")");
                            userList2.clear();
                            userList2.addAll(userList);
                            filledData(userList2);
                            Collections.sort(userList2, pinyinComparator);
                            adapter = new CreateGroupMembersAdapter(context, userList2);
                            listView.setAdapter(adapter);
                            setInterface();
                        }
                    } else if (ReturnType != null && ReturnType.equals("1002")) {
                        ToastUtils.show_always(context, "无法获取组Id");
                    } else if (ReturnType != null && ReturnType.equals("T")) {
                        ToastUtils.show_always(context, "异常返回值");
                    } else if (ReturnType != null && ReturnType.equals("1011")) {
                        ToastUtils.show_always(context, "组中无成员");
                    } else {
                        ToastUtils.show_always(context, "获取成员失败，请稍后再试");
                    }
                } catch (JSONException e1) {
                    e1.printStackTrace();
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
    private void initCharacterParser() {
        characterParser = CharacterParser.getInstance();    // 实例化汉字转拼音类
        pinyinComparator = new PinyinComparator_a();
    }

    // 处理Intent
    private void handleIntent() {
        groupId = this.getIntent().getStringExtra("GroupId");
    }

    //设置监听
    private void setListener() {
        mBack.setOnClickListener(this);
        image_clear.setOnClickListener(this);
        // 当输入框输入过汉字，且回复0后就要调用使用userList2的原表数据
        et_searh_content.addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                String search_name = s.toString();
                if (search_name == null || search_name.equals("") || search_name.trim().equals("")) {
                    image_clear.setVisibility(View.INVISIBLE);
                    tvNoFriends.setVisibility(View.GONE);
                    // 关键词为空
                    if (userList == null || userList.size() == 0) {
                        listView.setVisibility(View.GONE);
                    } else {
                        listView.setVisibility(View.VISIBLE);
                        userList2.clear();
                        userList2.addAll(userList);
                        filledData(userList2);
                        Collections.sort(userList2, pinyinComparator);
                        adapter = new CreateGroupMembersAdapter(context, userList2);
                        listView.setAdapter(adapter);
                        setInterface();
                    }
                } else {
                    userList2.clear();
                    userList2.addAll(userList);
                    image_clear.setVisibility(View.VISIBLE);
                    search(search_name);
                }
            }
        });
    }

    private void search(String search_name) {
        List<UserInfo> filterDateList = new ArrayList<UserInfo>();
        if (TextUtils.isEmpty(search_name)) {
            filterDateList = userList2;
            tvNoFriends.setVisibility(View.GONE);
        } else {
            filterDateList.clear();
            for (UserInfo sortModel : userList2) {
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
        userList2.clear();
        userList2.addAll(filterDateList);
        if (filterDateList.size() == 0) {
            tvNoFriends.setVisibility(View.VISIBLE);
        }
    }

    private void setInterface() {
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                boolean isFriend = false;
                if (userList2.get(position).getUserId().equals(CommonUtils.getUserId(context))) {
                    ToastUtils.show_always(context, "点击的是本人");
                } else {
                    if (GlobalConfig.list_person != null && GlobalConfig.list_person.size() != 0) {
                        for (int i = 0; i < GlobalConfig.list_person.size(); i++) {
                            if (userList2.get(position).getUserId().equals(GlobalConfig.list_person.get(i).getUserId())) {
                                isFriend = true;
                                break;
                            }
                        }
                    } else {
                        // 不是我的好友
                        isFriend = false;
                    }
                    if (isFriend) {
                        //是好友 跳转到好友详情界面
                        UserInfo mUserInfo = new UserInfo();
                        mUserInfo.setPortraitBig(userList2.get(position).getPortraitBig());
                        mUserInfo.setPortraitMini(userList2.get(position).getPortraitMini());
                        mUserInfo.setUserName(userList2.get(position).getUserName());
                        mUserInfo.setUserId(userList2.get(position).getUserId());
                        mUserInfo.setUserAliasName(userList2.get(position).getUserAliasName());
                        Intent intent = new Intent(AllGroupMemberActivity.this, TalkPersonNewsActivity.class);
                        Bundle bundle = new Bundle();
                        bundle.putString("type", "GroupMemers");
                        bundle.putString("id", groupId);
                        bundle.putSerializable("data", mUserInfo);
                        intent.putExtras(bundle);
                        startActivity(intent);
                    } else {
                        //不是好友 跳转到非好友界面
                        Intent intent = new Intent(context, GroupPersonNewsActivity.class);
                        Bundle bundle = new Bundle();
                        bundle.putString("type", "GroupMemers");
                        bundle.putString("id", groupId);
                        bundle.putSerializable("data", userList2.get(position));
                        intent.putExtras(bundle);
                        startActivityForResult(intent, 2);
                    }
                }
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
    private void setView() {
        mBack = (TextView) findViewById(R.id.wt_back);
        tvNoFriends = (TextView) findViewById(R.id.title_layout_no_friends);
        sideBar = (SideBar) findViewById(R.id.sidebar);
        dialogs = (TextView) findViewById(R.id.dialog);
        sideBar.setTextView(dialogs);
        listView = (ListView) findViewById(R.id.country_lvcountry);
        et_searh_content = (EditText) findViewById(R.id.et_search);            // 搜索控件
        image_clear = (ImageView) findViewById(R.id.image_clear);
        tv_head_name = (TextView) findViewById(R.id.tv_head_name);
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
                tvNoFriends.setVisibility(View.GONE);
                break;
        }
    }

    private void filledData(List<UserInfo> person) {
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

    @Override
    protected void onDestroy() {
        super.onDestroy();
        isCancelRequest = VolleyRequest.cancelRequest(tag);
        mBack = null;
        pinyinComparator = null;
        characterParser = null;
        tvNoFriends = null;
        sideBar = null;
        dialogs = null;
        listView = null;
        et_searh_content = null;        // 搜索控件
        image_clear = null;
        tv_head_name = null;
    }
}
