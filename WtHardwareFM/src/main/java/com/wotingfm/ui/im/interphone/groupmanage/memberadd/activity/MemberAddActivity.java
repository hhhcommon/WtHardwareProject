package com.wotingfm.ui.im.interphone.groupmanage.memberadd.activity;

import android.app.Dialog;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.android.volley.VolleyError;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.wotingfm.R;
import com.wotingfm.ui.common.baseactivity.BaseActivity;
import com.wotingfm.ui.im.interphone.groupmanage.memberadd.adapter.MembersAddAdapter;
import com.wotingfm.ui.im.interphone.groupmanage.model.UserInfo;
import com.wotingfm.ui.im.interphone.linkman.view.SideBar;
import com.wotingfm.common.config.GlobalConfig;
import com.wotingfm.common.volley.VolleyCallback;
import com.wotingfm.common.volley.VolleyRequest;
import com.wotingfm.util.CharacterParser;
import com.wotingfm.util.DialogUtils;
import com.wotingfm.util.L;
import com.wotingfm.util.PinyinComparator_a;
import com.wotingfm.util.ToastUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 添加群成员
 * 作者：xinlong on 2016/3/9
 * 邮箱：645700751@qq.com
 */
public class MemberAddActivity extends BaseActivity implements View.OnClickListener,
        SideBar.OnTouchingLetterChangedListener, TextWatcher, MembersAddAdapter.FriendCheck {
    private CharacterParser characterParser;
    private PinyinComparator_a pinyinComparator;
    private MembersAddAdapter adapter;
    private SideBar sideBar;

    private List<UserInfo> userList;// 获取的 userList
    private List<UserInfo> userList2 = new ArrayList<>();
    private List<String> addList = new ArrayList<>();

    private Dialog dialog;
    private TextView tvNoFriends;
    private TextView dialogs;
    private TextView textHeadRight;
    private ListView listView;
    private EditText editSearchContent;
    private ImageView imageClear;

    private String groupId;
    private String tag = "GROUP_MEMBER_ADD_VOLLEY_REQUEST_CANCEL_TAG";
    private boolean isCancelRequest;

    // 实例化汉字转拼音类
    private void initCharacterParser() {
        characterParser = CharacterParser.getInstance();
        pinyinComparator = new PinyinComparator_a();
    }

    // 设置右侧触摸监听
    @Override
    public void onTouchingLetterChanged(String s) {
        int position = adapter.getPositionForSection(s.charAt(0));// 该字母首次出现的位置
        if (position != -1) {
            listView.setSelection(position);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_transfer_authority);

        initCharacterParser();// 初始化汉字转拼音类
        initView();// 设置界面
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.wt_back:// 返回
                finish();
                break;
            case R.id.image_clear:// 清空输入框中输入的内容
                editSearchContent.setText("");
                imageClear.setVisibility(View.GONE);
                tvNoFriends.setVisibility(View.GONE);
                break;
            case R.id.tv_head_right:// 确定发出邀请 - > 右上角的已选择的人数统计
                if (userList2 == null || userList2.size() <= 0) {// 列表为空 没有好友或没有获取到用户的联系人
                    finish();
                    return ;
                }
                for (int i = 0; i < userList2.size(); i++) {// 将用户勾选的好友添加到另一个 List 中
                    if (userList2.get(i).getCheckType() == 2) {
                        addList.add(userList2.get(i).getUserId());
                    }
                }
                if(addList == null || addList.size() <= 0) {// 如用户勾选的列表为空则提示用户选择联系人
                    ToastUtils.show_always(context, "请选择您要邀请的好友!");
                    return ;
                }
                if (GlobalConfig.CURRENT_NETWORK_STATE_TYPE != -1) {// 向用户勾选的联系人发出进群组邀请
                    dialog = DialogUtils.Dialogph(context, "正在发送邀请");
                    sendGroupInvited();
                } else {
                    ToastUtils.show_always(context, "网络连接失败，请检查网络!");
                }
                break;
        }
    }

    // 设置 view
    private void initView() {
        findViewById(R.id.wt_back).setOnClickListener(this);// 返回

        sideBar = (SideBar) findViewById(R.id.sidebar);
        dialogs = (TextView) findViewById(R.id.dialog);
        sideBar.setTextView(dialogs);
        sideBar.setOnTouchingLetterChangedListener(this);// 设置右侧触摸监听

        tvNoFriends = (TextView) findViewById(R.id.title_layout_no_friends);
        listView = (ListView) findViewById(R.id.country_lvcountry);

        editSearchContent = (EditText) findViewById(R.id.et_search);// 搜索框输入内容
        editSearchContent.addTextChangedListener(this);

        imageClear = (ImageView) findViewById(R.id.image_clear);// 清除输入框中输入的内容
        imageClear.setOnClickListener(this);

        textHeadRight = (TextView) findViewById(R.id.tv_head_right);// 确定
        textHeadRight.setOnClickListener(this);

        groupId = getIntent().getStringExtra("GroupId");// 群组 ID
        if(groupId == null || groupId.equals("")) {
            ToastUtils.show_always(context, "获取组 ID 失败, 请返回重试!");
            return ;
        }
        if (GlobalConfig.CURRENT_NETWORK_STATE_TYPE != -1) {
            dialog = DialogUtils.Dialogph(context, "正在获取群成员信息");
            send();
        } else {
            ToastUtils.show_always(context, "网络失败，请检查网络");
        }
    }

    // 获取全部群组成员
    private void send() {
        JSONObject jsonObject = VolleyRequest.getJsonObject(context);
        try {
            jsonObject.put("GroupId", groupId);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        VolleyRequest.RequestPost(GlobalConfig.getfriendlist, tag, jsonObject, new VolleyCallback() {
            @Override
            protected void requestSuccess(JSONObject result) {
                if (dialog != null) dialog.dismiss();
                if (isCancelRequest) return;
                try {
                    String ReturnType = result.getString("ReturnType");
                    L.v("ReturnType -- > > " + ReturnType);

                    if(ReturnType == null) {
                        ToastUtils.show_always(context, "获取成员失败，请稍后再试");
                        return ;
                    }
                    if (ReturnType.equals("1001") || ReturnType.equals("1002")) {
                        try {
                            userList = new Gson().fromJson(result.getString("UserList"), new TypeToken<List<UserInfo>>() {}.getType());
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        userList2.clear();
                        userList2.addAll(userList);
                        filledData(userList2);
                        Collections.sort(userList2, pinyinComparator);
                        listView.setAdapter(adapter = new MembersAddAdapter(context, userList2));
                        adapter.setOnListener(MemberAddActivity.this);
                    } else if(ReturnType.equals("1011")) {
                        ToastUtils.show_always(context, "您还没有好友，快去添加吧");
                    }else{
                        ToastUtils.show_always(context, "获取成员失败，请稍后再试");
                    }
                } catch (JSONException e1) {
                    e1.printStackTrace();
                }
            }

            @Override
            protected void requestError(VolleyError error) {
                if (dialog != null) dialog.dismiss();
                ToastUtils.showVolleyError(context);
            }
        });
    }

    // 搜索好友
    private void search(String search_name) {
        List<UserInfo> filterDateList = new ArrayList<>();
        if (TextUtils.isEmpty(search_name)) {
            filterDateList = userList2;
            tvNoFriends.setVisibility(View.GONE);
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
            tvNoFriends.setVisibility(View.VISIBLE);
        }
    }

    private void filledData(List<UserInfo> person) {
        for (int i = 0; i < person.size(); i++) {
            person.get(i).setName(person.get(i).getUserName());
            String pinyin = characterParser.getSelling(person.get(i).getUserName());// 汉字转换成拼音
            String sortString = pinyin.substring(0, 1).toUpperCase();
            if (sortString.matches("[A-Z]")) {// 正则表达式，判断首字母是否是英文字母
                person.get(i).setSortLetters(sortString.toUpperCase());
            } else {
                person.get(i).setSortLetters("#");
            }
        }
    }

    // 发送进入组的邀请
    private void sendGroupInvited() {
        JSONObject jsonObject = VolleyRequest.getJsonObject(context);
        try {
            jsonObject.put("GroupId", groupId);
            String s = addList.toString();// 对 s 进行处理 去掉"[]"符号
            jsonObject.put("BeInvitedUserIds", s.substring(1, s.length() - 1).replaceAll(" ", ""));
        } catch (JSONException e) {
            e.printStackTrace();
        }

        VolleyRequest.RequestPost(GlobalConfig.sendInviteintoGroupUrl, tag, jsonObject, new VolleyCallback() {
            @Override
            protected void requestSuccess(JSONObject result) {
                if (dialog != null) dialog.dismiss();
                if (isCancelRequest) return;
                try {
                    String ReturnType = result.getString("ReturnType");
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
                    } else if (ReturnType != null && ReturnType.equals("1003")) {
                        ToastUtils.show_always(context, "异常返回值");
                    } else if (ReturnType != null && ReturnType.equals("10031")) {
                        ToastUtils.show_always(context, "用户组不是验证群，不能采取这种方式邀请");
                    } else if (ReturnType != null && ReturnType.equals("0000")) {
                        ToastUtils.show_always(context, "无法获取用户ID");
                    } else if (ReturnType != null && ReturnType.equals("1004")) {
                        ToastUtils.show_always(context, "被邀请人不存在");
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
                if (dialog != null) dialog.dismiss();
                ToastUtils.showVolleyError(context);
            }
        });
    }

    @Override
    public void checkPosition(int position) {
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
        String sumString = "确定(" + sum + ")";
        textHeadRight.setText(sumString);
        adapter.notifyDataSetChanged();
    }

    @Override
    public void afterTextChanged(Editable s) {
        String searchName = s.toString();
        if (userList2 != null) {
            if (searchName.equals("") || searchName.trim().equals("")) {
                imageClear.setVisibility(View.GONE);
                tvNoFriends.setVisibility(View.GONE);
                if (userList == null || userList.size() == 0) {// 关键词为空
                    listView.setVisibility(View.GONE);
                } else {
                    listView.setVisibility(View.VISIBLE);
                    userList2.clear();
                    userList2.addAll(userList);
                    filledData(userList2);
                    Collections.sort(userList2, pinyinComparator);
                    if(adapter == null) {
                        listView.setAdapter(adapter = new MembersAddAdapter(context, userList2));
                        adapter.setOnListener(MemberAddActivity.this);
                    } else {
                        adapter.notifyDataSetChanged();
                    }
                }
            } else {
                if (userList2 != null && userList2.size() != 0) {
                    userList2.clear();
                    userList2.addAll(userList);
                    imageClear.setVisibility(View.VISIBLE);
                    search(searchName);
                } else {
                    ToastUtils.show_always(context, "网络异常，没有获取导数据");
                }
            }
        } else {
            ToastUtils.show_always(context, "网络异常，没有获取导数据");
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        isCancelRequest = VolleyRequest.cancelRequest(tag);
        pinyinComparator = null;
        characterParser = null;
        tvNoFriends = null;
        sideBar = null;
        dialogs = null;
        listView = null;
        editSearchContent = null;
        imageClear = null;
        textHeadRight = null;
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
    }
}
