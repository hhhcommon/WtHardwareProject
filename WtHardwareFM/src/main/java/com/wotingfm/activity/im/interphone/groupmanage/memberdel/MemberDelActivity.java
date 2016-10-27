package com.wotingfm.activity.im.interphone.groupmanage.memberdel;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.android.volley.VolleyError;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.wotingfm.R;
import com.wotingfm.activity.common.baseactivity.BaseActivity;
import com.wotingfm.activity.im.interphone.groupmanage.memberadd.adapter.MembersAddAdapter;
import com.wotingfm.activity.im.interphone.groupmanage.model.UserInfo;
import com.wotingfm.activity.im.interphone.linkman.view.SideBar;
import com.wotingfm.common.config.GlobalConfig;
import com.wotingfm.common.constant.BroadcastConstant;
import com.wotingfm.common.volley.VolleyCallback;
import com.wotingfm.common.volley.VolleyRequest;
import com.wotingfm.util.CharacterParser;
import com.wotingfm.util.DialogUtils;
import com.wotingfm.util.PinyinComparator_a;
import com.wotingfm.util.ToastUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 删除群成员
 * 作者：xinlong on 2016/3/9
 * 邮箱：645700751@qq.com
 */
public class MemberDelActivity extends BaseActivity implements View.OnClickListener {

    private Context context;
    private TextView mBack;
    private String groupId;
    private CharacterParser characterParser;
    private PinyinComparator_a pinyinComparator;
    private boolean isCancelRequest;
    private String tag = "GROUP_MEMBER_DEL_VOLLEY_REQUEST_CANCEL_TAG";
    private Dialog dialog;
    private TextView tvNoFriends;
    private SideBar sideBar;
    private TextView dialogs;
    private ListView listView;
    private EditText et_searh_content;
    private ImageView image_clear;
    private List<UserInfo> userList;//获取的userlist
    private List<UserInfo> userList2 = new ArrayList<UserInfo>();
    private int sum = 0;// 统计点选的人数
    private TextView tv_head_name;
    private TextView tv_right;
    private MembersAddAdapter adapter;
    private TextView tv_head_right;
    private List<String> addList = new ArrayList<String>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_transfer_authority);
        context = this;
        InitCharacterParser();              // 初始化汉字转拼音类
        handleIntent();                     // 处理其他页面传入的数据
        setView();                          // 设置界面
        setListener();                      // 设置监听
        if (GlobalConfig.CURRENT_NETWORK_STATE_TYPE != -1) {
            if (groupId != null) {
                dialog = DialogUtils.Dialogph(context, "正在获取群成员信息");
                send();
            } else {
                ToastUtils.show_always(context, "获取组ID失败");
            }
        } else {
            ToastUtils.show_always(context, "网络失败，请检查网络");
        }
    }

    private void send() {
        JSONObject jsonObject = VolleyRequest.getJsonObject(context);
        try {
            jsonObject.put("GroupId", groupId); // 模块属性
        } catch (JSONException e) {
            e.printStackTrace();
        }

        VolleyRequest.RequestPost(GlobalConfig.grouptalkUrl, tag, jsonObject, new VolleyCallback() {
            @Override
            protected void requestSuccess(JSONObject result) {
                if (dialog != null) dialog.dismiss();
                Log.e("获取群成员返回值", "" + result.toString());
                if (isCancelRequest) return;
                try {
                    String ReturnType = result.getString("ReturnType");
                    if (ReturnType != null && ReturnType.equals("1001")) {
                        try {
                            String userList1 = result.getString("UserList");
                            userList = new Gson().fromJson(userList1, new TypeToken<List<UserInfo>>() {
                            }.getType());
                        } catch (Exception e1) {
                            e1.printStackTrace();
                        }
                        int sum = userList.size();
                        userList2.clear();
                        userList2.addAll(userList);
                        filledData(userList2);
                        Collections.sort(userList2, pinyinComparator);
                        adapter = new MembersAddAdapter(context, userList2);
                        listView.setAdapter(adapter);
                        setInterface();
                    } else if (ReturnType != null && ReturnType.equals("1002")) {
                        if (userList == null || userList.size() == 0) {

                        } else {
                            int sum = userList.size();
                            userList2.clear();
                            userList2.addAll(userList);
                            filledData(userList2);
                            Collections.sort(userList2, pinyinComparator);
                            adapter = new MembersAddAdapter(context, userList2);
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
                        try {
                            String Message = result.getString("Message");
                            if (Message != null && !Message.trim().equals("")) {
                                ToastUtils.show_always(context, Message + "");
                            }
                        } catch (JSONException e1) {
                            e1.printStackTrace();
                        }
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
    private void InitCharacterParser() {
        characterParser = CharacterParser.getInstance();    // 实例化汉字转拼音类
        pinyinComparator = new PinyinComparator_a();
    }

    // 处理Intent
    private void handleIntent() {
        groupId = this.getIntent().getStringExtra("GroupId");
    }

    //设置监听
    private void setListener() {
        tv_head_right.setOnClickListener(this);
        mBack.setOnClickListener(this);
        image_clear.setOnClickListener(this);
        // 当输入框输入过汉字，且回复0后就要调用使用userlist2的原表数据
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
                if (userList2 != null) {
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
                            adapter = new MembersAddAdapter(context, userList2);
                            listView.setAdapter(adapter);
                            setInterface();
                        }
                    } else {
                        if (userList2 != null && userList2.size() != 0) {
                            userList2.clear();
                            userList2.addAll(userList);
                            image_clear.setVisibility(View.VISIBLE);
                            search(search_name);
                        } else {
                            ToastUtils.show_always(context, "网络异常，没有获取导数据");
                        }
                    }
                } else {
                    ToastUtils.show_always(context, "网络异常，没有获取导数据");
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
        adapter.setOnListener(new MembersAddAdapter.FriendCheck() {
            @Override
            public void checkPosition(int position) {
                sum = 0;
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
        tv_head_right = (TextView) findViewById(R.id.tv_head_right);

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
            case R.id.tv_head_right:
                if (userList2 != null && userList2.size() > 0) {
                    for (int i = 0; i < userList2.size(); i++) {
                        if (userList2.get(i).getCheckType() == 2) {
                            addList.add(userList2.get(i).getUserId());
                        }
                    }
                }
                if (addList != null && addList.size() > 0) {
                    // 发送进入组的邀请
                    if (GlobalConfig.CURRENT_NETWORK_STATE_TYPE != -1) {
                        dialog = DialogUtils.Dialogph(context, "正在发送邀请");
                        sendMemberDelete();
                    } else {
                        ToastUtils.show_always(context, "网络失败，请检查网络");
                    }
                } else {
                    ToastUtils.show_always(context, "请您勾选您要邀请的好友");
                }
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

    private void sendMemberDelete() {
        JSONObject jsonObject = VolleyRequest.getJsonObject(this);
        try {
            // 对s进行处理 去掉"[]"符号
            String s = userList2.toString().replaceAll(" ", "");
            jsonObject.put("UserIds", s.substring(1, s.length() - 1));
            // groupid由上一个界面传递而来
            jsonObject.put("GroupId", groupId);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        VolleyRequest.RequestPost(GlobalConfig.KickOutMembersUrl, tag, jsonObject, new VolleyCallback() {
            @Override
            protected void requestSuccess(JSONObject result) {
                if (dialog != null) dialog.dismiss();
                if (isCancelRequest) return;
                try {
                    String ReturnType = result.getString("ReturnType");
                    if (ReturnType != null && ReturnType.equals("1001")) {
                        ToastUtils.show_always(context, "群成员已经成功删除");
                        sendBroadcast(new Intent(BroadcastConstant.REFRESH_GROUP));
                        finish();
                    } else if (ReturnType != null && ReturnType.equals("1002")) {
                        ToastUtils.show_always(context, "无法获取用户Id");
                    } else if (ReturnType != null && ReturnType.equals("T")) {
                        ToastUtils.show_always(context, "异常返回值");
                    } else if (ReturnType != null && ReturnType.equals("200")) {
                        ToastUtils.show_always(context, "尚未登录");
                    } else if (ReturnType != null && ReturnType.equals("1003")) {
                        ToastUtils.show_always(context, "异常返回值");
                    } else if (ReturnType != null && ReturnType.equals("10021")) {
                        ToastUtils.show_always(context, "用户不是该组的管理员");
                    } else if (ReturnType != null && ReturnType.equals("0000")) {
                        ToastUtils.show_always(context, "无法获取相关的参数");
                    } else if (ReturnType != null && ReturnType.equals("1004")) {
                        ToastUtils.show_always(context, "无法获取被踢出用户Id");
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
        tv_head_right = null;
    }
}
