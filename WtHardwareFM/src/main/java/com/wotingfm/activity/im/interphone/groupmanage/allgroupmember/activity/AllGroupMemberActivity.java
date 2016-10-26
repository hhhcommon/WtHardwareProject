package com.wotingfm.activity.im.interphone.groupmanage.allgroupmember.activity;

import android.app.Dialog;
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
import com.wotingfm.activity.im.interphone.find.add.FriendAddActivity;
import com.wotingfm.activity.im.interphone.groupmanage.allgroupmember.adapter.CreateGroupMembersAdapter;
import com.wotingfm.activity.im.interphone.groupmanage.model.UserInfo;
import com.wotingfm.activity.im.interphone.linkman.view.SideBar;
import com.wotingfm.common.config.GlobalConfig;
import com.wotingfm.common.volley.VolleyCallback;
import com.wotingfm.common.volley.VolleyRequest;
import com.wotingfm.util.CharacterParser;
import com.wotingfm.util.CommonUtils;
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
 * 群组详细资料  --> 全部成员
 * 作者：xinlong on 2016/3/9
 * 邮箱：645700751@qq.com
 */
public class AllGroupMemberActivity extends BaseActivity implements View.OnClickListener, TextWatcher {
    private CharacterParser characterParser;
    private PinyinComparator_a pinyinComparator;
    private CreateGroupMembersAdapter adapter;

    private SideBar sideBar;
    private Dialog dialog;
    private TextView tvNoFriends;       // 没有找到联系人
    private TextView textHeadName;      // 标题
    private ListView listView;          // 好友列表
    private EditText editSearchContent; // 搜索内容
    private ImageView imageClear;       // 清除

    private String groupId;
    private String tag = "GROUP_MEMBERS_VOLLEY_REQUEST_CANCEL_TAG";
    private boolean isCancelRequest;

    private List<UserInfo> userList;    // 获取的 userList
    private List<UserInfo> userList2 = new ArrayList<>();

    // 实例化汉字转拼音类
    private void initCharacterParser() {
        characterParser = CharacterParser.getInstance();
        pinyinComparator = new PinyinComparator_a();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.wt_back:          // 返回
                finish();
                break;
            case R.id.image_clear:      // 清空搜索框中的内容
                editSearchContent.setText("");
                imageClear.setVisibility(View.GONE);
                tvNoFriends.setVisibility(View.GONE);
                break;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_transfer_authority);

        initCharacterParser();
        initViews();
    }

    // 初始化控件
    private void initViews() {
        if(getIntent() != null) {
            groupId = getIntent().getStringExtra("GroupId");                // 上一个界面传递过来的数据
        }

        findViewById(R.id.tv_head_right).setVisibility(View.INVISIBLE);     // 右上角的"确定"
        findViewById(R.id.wt_back).setOnClickListener(this);                // 返回

        imageClear = (ImageView) findViewById(R.id.image_clear);            // 清除
        imageClear.setOnClickListener(this);

        editSearchContent = (EditText) findViewById(R.id.et_search);        // 搜索内容
        editSearchContent.addTextChangedListener(this);

        textHeadName = (TextView) findViewById(R.id.tv_head_name);          // 标题
        tvNoFriends = (TextView) findViewById(R.id.title_layout_no_friends);// 没有找到联系人

        TextView dialogs = (TextView) findViewById(R.id.dialog);
        sideBar = (SideBar) findViewById(R.id.sidebar);
        sideBar.setTextView(dialogs);
        sideBar.setOnTouchingLetterChangedListener(new MySideBarLis());     // 设置右侧触摸监听

        listView = (ListView) findViewById(R.id.country_lvcountry);         // 好友列表
        listView.setOnItemClickListener(new MyListItemLis());

        if (groupId != null) {
            dialog = DialogUtils.Dialogph(context, "正在获取群成员信息");
            send();
        } else {
            ToastUtils.show_always(context, "获取组 ID 失败");
        }
    }

    // 获取群组全部成员
    private void send() {
        JSONObject jsonObject = VolleyRequest.getJsonObject(context);
        try {
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
                    L.v("ReturnType -- > > " + ReturnType);

                    if(ReturnType == null) {
                        ToastUtils.show_always(context, "获取成员失败，请稍后再试");
                        return ;
                    }
                    if(ReturnType.equals("1001") || ReturnType.equals("1002")) {
                        try {
                            userList = new Gson().fromJson(result.getString("UserList"), new TypeToken<List<UserInfo>>() {}.getType());
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        if (userList != null && userList.size() != 0) {
                            String sizeString = "全部成员(" + userList.size() + ")";
                            textHeadName.setText(sizeString);
                            userList2.clear();
                            userList2.addAll(userList);
                            filledData(userList2);
                            Collections.sort(userList2, pinyinComparator);
                            adapter = new CreateGroupMembersAdapter(context, userList2);
                            listView.setAdapter(adapter);
                        }
                    } else {
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

    // 搜索群组中的成员并排序
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
        Collections.sort(filterDateList, pinyinComparator);// 根据a-z进行排序
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
            if (sortString.matches("[A-Z]")) {// 判断首字母是否是英文字母
                person.get(i).setSortLetters(sortString.toUpperCase());
            } else {
                person.get(i).setSortLetters("#");
            }
        }
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
    }

    // 当输入框输入过汉字且回复 0 后就要调用使用 userList2 的原表数据
    @Override
    public void afterTextChanged(Editable s) {
        String search_name = s.toString();
        if (search_name.equals("") || search_name.trim().equals("")) {
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
                listView.setAdapter(adapter = new CreateGroupMembersAdapter(context, userList2));
            }
        } else {
            userList2.clear();
            userList2.addAll(userList);
            imageClear.setVisibility(View.VISIBLE);
            search(search_name);
        }
    }

    class MyListItemLis implements AdapterView.OnItemClickListener {

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
                    isFriend = false;// 不是我的好友
                }
                if (isFriend) {// 是好友 跳转到好友详情界面
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
                } else {// 不是好友 跳转到非好友界面
                    Intent intent = new Intent(AllGroupMemberActivity.this, FriendAddActivity.class);
                    Bundle bundle = new Bundle();
                    bundle.putString("type", "TalkGroupNewsActivity_p");
                    bundle.putString("id", groupId);
                    bundle.putSerializable("data", userList2.get(position));
                    intent.putExtras(bundle);
                    startActivity(intent);
                }
            }
        }
    }

    // 设置右侧触摸监听
    class MySideBarLis implements SideBar.OnTouchingLetterChangedListener {

        @Override
        public void onTouchingLetterChanged(String s) {
            int position = adapter.getPositionForSection(s.charAt(0));// 该字母首次出现的位置
            if (position != -1) {
                listView.setSelection(position);
            }
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
        listView = null;
        editSearchContent = null;
        imageClear = null;
        textHeadName = null;
    }
}
