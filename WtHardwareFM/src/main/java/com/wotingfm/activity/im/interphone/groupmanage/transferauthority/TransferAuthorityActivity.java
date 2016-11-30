package com.wotingfm.activity.im.interphone.groupmanage.transferauthority;

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
import com.wotingfm.activity.common.baseactivity.BaseActivity;
import com.wotingfm.activity.im.interphone.groupmanage.memberadd.adapter.MembersAddAdapter;
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
 * 移交管理员权限
 * 作者：xinlong on 2016/3/9
 * 邮箱：645700751@qq.com
 */
public class TransferAuthorityActivity extends BaseActivity implements
        View.OnClickListener, TextWatcher, SideBar.OnTouchingLetterChangedListener, MembersAddAdapter.FriendCheck {

    private CharacterParser characterParser;
    private PinyinComparator_a pinyinComparator;
    private MembersAddAdapter adapter;
    private SideBar sideBar;

    private List<UserInfo> userList;        // 获取的 userList
    private List<UserInfo> userList2 = new ArrayList<>();

    private Dialog dialog;                  // 加载数据对话框
    private TextView tvNoFriends;           // 搜索为空时的提示
    private TextView dialogs;               // SideBar 的选中字母提示
    private ListView listView;              // 群组成员列表
    private EditText editSearchContent;     // 搜索输入框
    private ImageView imageClear;           // 清空输入内容

    private String toUserId;                // 接受管理员身份转移的用户 ID
    private String groupId;                 // 群组 ID
    private String tag = "TRANSFER_AUTHORITY_VOLLEY_REQUEST_CANCEL_TAG";
    private boolean isCancelRequest;

    // 实例化汉字转拼音类
    private void initCharacterParser() {
        characterParser = CharacterParser.getInstance();
        pinyinComparator = new PinyinComparator_a();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_transfer_authority);

        initCharacterParser();
        initView();
    }

    // 初始化视图
    private void initView() {
        findViewById(R.id.wt_back).setOnClickListener(this);                    // 返回
        findViewById(R.id.tv_head_right).setOnClickListener(this);              // 确定

        TextView textHeadName = (TextView) findViewById(R.id.tv_head_name);     // 标题
        textHeadName.setText("移交管理员权限");

        dialogs = (TextView) findViewById(R.id.dialog);
        sideBar = (SideBar) findViewById(R.id.sidebar);
        sideBar.setTextView(dialogs);
        sideBar.setOnTouchingLetterChangedListener(this);                       // 设置右侧触摸监听

        tvNoFriends = (TextView) findViewById(R.id.title_layout_no_friends);    // 搜索为空时的提示
        listView = (ListView) findViewById(R.id.country_lvcountry);

        editSearchContent = (EditText) findViewById(R.id.et_search);            // 搜索输入框
        editSearchContent.addTextChangedListener(this);

        imageClear = (ImageView) findViewById(R.id.image_clear);                // 清空搜索输入内容
        imageClear.setOnClickListener(this);

        groupId = getIntent().getStringExtra("GroupId");
        if(groupId == null || groupId.equals("")) {
            ToastUtils.show_always(context, "获取组 ID 失败, 请重试!");
            return ;
        }
        if (GlobalConfig.CURRENT_NETWORK_STATE_TYPE != -1) {
            dialog = DialogUtils.Dialogph(context, "正在获取群成员信息");
            send();
        } else {
            ToastUtils.show_always(context, "网络失败，请检查网络");
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.wt_back:          // 返回
                finish();
                break;
            case R.id.image_clear:      // 清空搜索内容
                imageClear.setVisibility(View.GONE);
                tvNoFriends.setVisibility(View.GONE);
                editSearchContent.setText("");
                break;
            case R.id.tv_head_right:    // 确定转移管理员权限
                boolean isHave = false;
                if (userList2 != null && userList2.size() > 0) {
                    for (int i = 0; i < userList2.size(); i++) {
                        if (userList2.get(i).getCheckType() == 2) {
                            toUserId = userList2.get(i).getUserId();
                            isHave = true;
                        }
                    }
                }
                if (!isHave) {          // 防空
                    ToastUtils.show_always(context, "请勾选您要移交权限的成员!");
                    return;
                }
                if (GlobalConfig.CURRENT_NETWORK_STATE_TYPE != -1) {
                    dialog = DialogUtils.Dialogph(context, "正在变更管理员");
                    sendTransferAuthority();
                } else {
                    ToastUtils.show_always(context, "网络失败，请检查网络");
                }
                break;
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

        VolleyRequest.RequestPost(GlobalConfig.grouptalkUrl, tag, jsonObject, new VolleyCallback() {
            @Override
            protected void requestSuccess(JSONObject result) {
                if (dialog != null) dialog.dismiss();
                if (isCancelRequest) return;
                try {
                    String ReturnType = result.getString("ReturnType");
                    L.v("ReturnType -- > > " + ReturnType);

                    if(ReturnType == null || ReturnType.equals("")) {
                        ToastUtils.show_always(context, "获取群组成员失败，请重试!");
                        return ;
                    }
                    if (ReturnType.equals("1001") || ReturnType.equals("1002")) {
                        try {
                            userList = new Gson().fromJson(result.getString("UserList"), new TypeToken<List<UserInfo>>() {}.getType());
                            if(userList != null && userList.size() > 0) {
                                for(int i=0; i<userList.size(); i++) {
                                    if(userList.get(i).getUserId().equals(CommonUtils.getUserId(context))) {
                                        // 移交管理员权限不能操作用户本人 所以此操作不需要显示用户本人
                                        userList.remove(i);
                                        break;
                                    }
                                }
                                userList2.clear();
                                userList2.addAll(userList);
                                filledData(userList2);
                                Collections.sort(userList2, pinyinComparator);
                                listView.setAdapter(adapter = new MembersAddAdapter(context, userList2));
                                adapter.setOnListener(TransferAuthorityActivity.this);
                            }
                        } catch (Exception e1) {
                            e1.printStackTrace();
                        }
                    }  else {
                        ToastUtils.show_always(context, "获取群组成员失败，请重试!");
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

    // 搜索
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
            if (sortString.matches("[A-Z]")) {// 判断首字母是否是英文字母
                person.get(i).setSortLetters(sortString.toUpperCase());
            } else {
                person.get(i).setSortLetters("#");
            }
        }
    }

    // 管理员权限转移
    private void sendTransferAuthority() {
        JSONObject jsonObject = VolleyRequest.getJsonObject(context);
        try {
            jsonObject.put("ToUserId", toUserId);
            jsonObject.put("GroupId", groupId);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        VolleyRequest.RequestPost(GlobalConfig.changGroupAdminnerUrl, tag, jsonObject, new VolleyCallback() {
            @Override
            protected void requestSuccess(JSONObject result) {
                if (dialog != null) dialog.dismiss();
                if (isCancelRequest) return;
                try {
                    String ReturnType = result.getString("ReturnType");

                    if (ReturnType != null && ReturnType.equals("1001")) {
                        ToastUtils.show_always(context, "管理员权限移交成功");
//                        sendBroadcast(new Intent(BroadcastConstant.REFRESH_GROUP));
                        setResult(RESULT_OK);
                        finish();
                    } else if (ReturnType != null && ReturnType.equals("1002")) {
                        ToastUtils.show_always(context, "无法获取用户Id");
                    } else if (ReturnType != null && ReturnType.equals("1003")) {
                        ToastUtils.show_always(context, "用户不存在");
                    } else if (ReturnType != null && ReturnType.equals("10021")) {
                        ToastUtils.show_always(context, "用户不是该组的管理员");
                    } else if (ReturnType != null && ReturnType.equals("0000")) {
                        ToastUtils.show_always(context, "无法获取相关的参数");
                    } else if (ReturnType != null && ReturnType.equals("1004")) {
                        ToastUtils.show_always(context, "无法获取移交用户Id");
                    } else if (ReturnType != null && ReturnType.equals("10041")) {
                        ToastUtils.show_always(context, "被移交用户不在该组");
                    } else if (ReturnType != null && ReturnType.equals("T")) {
                        ToastUtils.show_always(context, "异常返回值");
                    } else if (ReturnType != null && ReturnType.equals("200")) {
                        ToastUtils.show_always(context, "尚未登录");
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
        if (userList2.get(position).getCheckType() == 1) {
            for (int i = 0; i < userList2.size(); i++) {
                userList2.get(i).setCheckType(1);
            }
            userList2.get(position).setCheckType(2);
        } else {
            userList2.get(position).setCheckType(1);
        }
        adapter.notifyDataSetChanged();
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
    public void afterTextChanged(Editable s) {
        String search_name = s.toString();
        if (userList2 != null) {
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
                    listView.setAdapter(adapter = new MembersAddAdapter(context, userList2));
                    adapter.setOnListener(TransferAuthorityActivity.this);
                }
            } else {
                if (userList2 != null && userList2.size() != 0) {
                    userList2.clear();
                    userList2.addAll(userList);
                    imageClear.setVisibility(View.VISIBLE);
                    search(search_name);
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
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
    }
}
