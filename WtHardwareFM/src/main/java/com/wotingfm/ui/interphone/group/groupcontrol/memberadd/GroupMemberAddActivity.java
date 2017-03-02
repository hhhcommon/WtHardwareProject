package com.wotingfm.ui.interphone.group.groupcontrol.memberadd;

import android.app.Dialog;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.android.volley.VolleyError;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.wotingfm.R;
import com.wotingfm.common.config.GlobalConfig;
import com.wotingfm.common.volley.VolleyCallback;
import com.wotingfm.common.volley.VolleyRequest;
import com.wotingfm.ui.baseactivity.BaseActivity;
import com.wotingfm.ui.common.model.UserInfo;
import com.wotingfm.ui.interphone.group.groupcontrol.memberadd.adapter.CreateGroupMembersAddAdapter;
import com.wotingfm.ui.interphone.linkman.view.CharacterParser;
import com.wotingfm.ui.interphone.linkman.view.PinyinComparator;
import com.wotingfm.ui.interphone.linkman.view.SideBar;
import com.wotingfm.util.DialogUtils;
import com.wotingfm.util.ToastUtils;
import com.wotingfm.widget.TipView;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 群成员添加页
 * @author 辛龙
 * 2016年3月25日
 */
public class GroupMemberAddActivity extends BaseActivity implements
        OnClickListener, TextWatcher, SideBar.OnTouchingLetterChangedListener, TipView.WhiteViewClick {

    private CharacterParser characterParser = CharacterParser.getInstance();// 实例化汉字转拼音类
    private PinyinComparator pinyinComparator = new PinyinComparator();
    private List<UserInfo> userList;
    private List<UserInfo> userList2 = new ArrayList<>();
    private List<String> delList = new ArrayList<>();
    private CreateGroupMembersAddAdapter adapter;
    private SideBar sideBar;

    private Dialog dialog;
    private TextView dialogs;
    private ListView listView;
    private EditText editSearchContent;
    private ImageView imageClear;
    private TextView textHeadRight;

    private String groupId;
    private String tag = "GROUP_MEMBER_ADD_VOLLEY_REQUEST_CANCEL_TAG";
    private boolean isCancelRequest;

    private TipView tipView;// 没有网络没有数据提示
    private TipView tipSearchNull;// 搜索数据为空提示

    @Override
    public void onWhiteViewClick() {
        groupId = getIntent().getStringExtra("GroupId");
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
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_groupmembers_add);

        initView();
    }

    private void initView() {
        findViewById(R.id.head_left_btn).setOnClickListener(this);
        findViewById(R.id.head_right_btn).setOnClickListener(this);

        tipSearchNull = (TipView) findViewById(R.id.tip_search_null);
        tipView = (TipView) findViewById(R.id.tip_view);
        tipView.setWhiteClick(this);

        editSearchContent = (EditText) findViewById(R.id.et_search);// 搜索控件
        editSearchContent.addTextChangedListener(this);

        imageClear = (ImageView) findViewById(R.id.image_clear);
        imageClear.setOnClickListener(this);

        dialogs = (TextView) findViewById(R.id.dialog);
        sideBar = (SideBar) findViewById(R.id.sidrbar);
        sideBar.setTextView(dialogs);
        sideBar.setOnTouchingLetterChangedListener(this);

        listView = (ListView) findViewById(R.id.country_lvcountry);
        textHeadRight = (TextView) findViewById(R.id.tv_head);

        groupId = getIntent().getStringExtra("GroupId");// 获取传递给当前用户组的 GroupId
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

    // 主网络请求
    private void send() {
        JSONObject jsonObject = VolleyRequest.getJsonObject(context);
        try {
            jsonObject.put("Page", 1);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        VolleyRequest.RequestPost(GlobalConfig.getfriendlist, tag, jsonObject, new VolleyCallback() {
            private String ReturnType;

            @Override
            protected void requestSuccess(JSONObject result) {
                if (dialog != null) dialog.dismiss();
                if (isCancelRequest) return;
                try {
                    ReturnType = result.getString("ReturnType");
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                if (ReturnType != null && ReturnType.equals("1001")) {
                    try {
                        userList = new Gson().fromJson(result.getString("UserList"), new TypeToken<List<UserInfo>>() {}.getType());
                        userList2.clear();
                        userList2.addAll(userList);
                        filledData(userList2);
                        Collections.sort(userList2, pinyinComparator);
                        adapter = new CreateGroupMembersAddAdapter(context, userList2);
                        listView.setAdapter(adapter);
                        setInterface();
                        tipView.setVisibility(View.GONE);
                    } catch (Exception e1) {
                        e1.printStackTrace();
                        tipView.setVisibility(View.VISIBLE);
                        tipView.setTipView(TipView.TipStatus.IS_ERROR);
                    }
                } else {
                    tipView.setVisibility(View.VISIBLE);
                    tipView.setTipView(TipView.TipStatus.NO_DATA, "你还没有添加好友\n赶紧去添加好友邀请加入群组吧");
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

    private void filledData(List<UserInfo> person) {
        for (int i = 0; i < person.size(); i++) {
            person.get(i).setName(person.get(i).getUserName());
            String pinyin = characterParser.getSelling(person.get(i).getUserName());
            String sortString = pinyin.substring(0, 1).toUpperCase();
            if (sortString.matches("[A-Z]")) {// 判断首字母是否是英文字母
                person.get(i).setSortLetters(sortString.toUpperCase());
            } else {
                person.get(i).setSortLetters("#");
            }
        }
    }

    // 根据输入框中的值来过滤数据并更新 ListView
    private void search(String search_name) {
        List<UserInfo> filterDateList = new ArrayList<>();
        if (TextUtils.isEmpty(search_name)) {
            filterDateList = userList2;
            tipSearchNull.setVisibility(View.GONE);
        } else {
            filterDateList.clear();
            for (UserInfo sortModel : userList2) {
                String name = sortModel.getName();
                if (name.contains(search_name) || characterParser.getSelling(name).startsWith(search_name)) {
                    filterDateList.add(sortModel);
                }
            }
        }
        Collections.sort(filterDateList, pinyinComparator);// 根据 a - z 进行排序
        adapter.ChangeDate(filterDateList);
        userList2.clear();
        userList2.addAll(filterDateList);
        if (filterDateList.size() == 0) {
            tipSearchNull.setVisibility(View.VISIBLE);
            tipSearchNull.setTipView(TipView.TipStatus.NO_DATA, "没有找到该好友哟\n换个好友再试一次吧");
        } else {
            tipSearchNull.setVisibility(View.GONE);
            tipSearchNull.setTipView(TipView.TipStatus.NO_DATA, "没有找到该好友哟\n换个好友再试一次吧");
        }
    }

    // 实现接口的方法
    private void setInterface() {
        adapter.setOnListener(new CreateGroupMembersAddAdapter.friendCheck() {
            @Override
            public void checkposition(int position) {
                int sum = 0;
                if (userList2.get(position).getCheckType() == 1) {
                    userList2.get(position).setCheckType(2);
                } else {
                    userList2.get(position).setCheckType(1);
                }
                for (int i = 0; i < userList2.size(); i++) {
                    if (userList2.get(i).getCheckType() == 2) {
                        sum++;
                    }
                }
                textHeadRight.setText("确定(" + sum + ")");
                adapter.notifyDataSetChanged();
            }
        });
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.head_left_btn:
                finish();
                break;
            case R.id.head_right_btn:
                // 此处需要执行添加好友的请求，现在还没有，需要等待这个接口出来之后调用
                if (userList2 != null && userList2.size() > 0) {
                    for (int i = 0; i < userList2.size(); i++) {
                        if (userList2.get(i).getCheckType() == 2) {
                            delList.add(userList2.get(i).getUserId());
                        }
                    }
                }
                if (delList != null && delList.size() > 0) {
                    // 发送进入组的邀请
                    if (GlobalConfig.CURRENT_NETWORK_STATE_TYPE != -1) {
                        dialog = DialogUtils.Dialogph(context, "正在发送邀请");
                        sendGroupInvited();
                    } else {
                        ToastUtils.show_always(context, "网络失败，请检查网络");
                    }
                } else {
                    ToastUtils.show_always(context, "请您勾选您要邀请的好友");
                }
                break;
            case R.id.image_clear:
                imageClear.setVisibility(View.INVISIBLE);
                editSearchContent.setText("");
                tipSearchNull.setVisibility(View.GONE);
                break;
        }
    }

    private void sendGroupInvited() {
        JSONObject jsonObject = VolleyRequest.getJsonObject(context);
        try {
            String s = delList.toString();
            jsonObject.put("BeInvitedUserIds", s.substring(1, s.length() - 1).replaceAll(" ", ""));
            jsonObject.put("GroupId", groupId);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        VolleyRequest.RequestPost(GlobalConfig.sendInviteintoGroupUrl, tag, jsonObject, new VolleyCallback() {
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
                    ToastUtils.show_always(context, "组邀请已经发送，请等待对方接受");
                    setResult(1);
                    finish();
                } else if (ReturnType != null && ReturnType.equals("1002")) {
                    ToastUtils.show_always(context, "无法获取用户Id");
                } else if (ReturnType != null && ReturnType.equals("T")) {
                    ToastUtils.show_always(context, "异常返回值");
                } else if (ReturnType != null && ReturnType.equals("200")) {
                    ToastUtils.show_always(context, "尚未登录");
                } else if (ReturnType != null && ReturnType.equals("10031")) {
                    ToastUtils.show_always(context, "用户组不是验证群，不能采取这种方式邀请");
                } else if (ReturnType != null && ReturnType.equals("0000")) {
                    ToastUtils.show_always(context, "无法获取用户ID");
                } else if (ReturnType != null && ReturnType.equals("1004")) {
                    ToastUtils.show_always(context, "被邀请人不存在");
                } else {
                    if (Message != null && !Message.trim().equals("")) {
                        ToastUtils.show_always(context, Message + "");
                    }
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
    public void onTouchingLetterChanged(String s) {
        int position = adapter.getPositionForSection(s.charAt(0));// 该字母首次出现的位置
        if (position != -1) {
            listView.setSelection(position);
        }
    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
        String search_name = s.toString();
        if (search_name.trim().equals("")) {
            imageClear.setVisibility(View.INVISIBLE);
            tipSearchNull.setVisibility(View.GONE);
            if (userList == null || userList.size() == 0) {
                listView.setVisibility(View.GONE);
            } else {
                listView.setVisibility(View.VISIBLE);
                userList2.clear();
                userList2.addAll(userList);
                Collections.sort(userList2, pinyinComparator);
                adapter = new CreateGroupMembersAddAdapter(context, userList2);
                listView.setAdapter(adapter);
                setInterface();
            }
        } else {
            userList2.clear();
            userList2.addAll(userList);
            imageClear.setVisibility(View.VISIBLE);
            search(search_name);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        isCancelRequest = VolleyRequest.cancelRequest(tag);
        sideBar = null;
        dialogs = null;
        listView = null;
        editSearchContent = null;
        imageClear = null;
        textHeadRight = null;
        userList = null;
        userList2.clear();
        userList2 = null;
        adapter = null;
        pinyinComparator = null;
        setContentView(R.layout.activity_null);
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
    }

    @Override
    public void afterTextChanged(Editable s) {
    }
}
