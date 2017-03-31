package com.wotingfm.ui.interphone.group.groupcontrol.membershow;

import android.app.Dialog;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
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
import com.wotingfm.ui.common.model.UserInfo;
import com.wotingfm.ui.interphone.group.groupcontrol.groupdetail.main.GroupDetailFragment;
import com.wotingfm.ui.interphone.group.groupcontrol.groupnumdel.GroupMemberDelFragment;
import com.wotingfm.ui.interphone.group.groupcontrol.grouppersonnews.GroupPersonNewsFragment;
import com.wotingfm.ui.interphone.group.groupcontrol.memberadd.GroupMemberAddFragment;
import com.wotingfm.ui.interphone.group.groupcontrol.membershow.adapter.CreateGroupMembersAdapter;
import com.wotingfm.ui.interphone.group.groupcontrol.personnews.TalkPersonNewsFragment;
import com.wotingfm.ui.interphone.linkman.view.CharacterParser;
import com.wotingfm.ui.interphone.linkman.view.PinyinComparator;
import com.wotingfm.ui.interphone.linkman.view.SideBar;
import com.wotingfm.ui.interphone.main.DuiJiangActivity;
import com.wotingfm.util.CommonUtils;
import com.wotingfm.util.DialogUtils;
import com.wotingfm.util.ToastUtils;
import com.wotingfm.widget.TipView;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 展示全部群成员
 * @author 辛龙
 * 2016年4月13日
 */
public class GroupMembersFragment extends Fragment implements OnClickListener, TextWatcher, OnItemClickListener, SideBar.OnTouchingLetterChangedListener, TipView.WhiteViewClick {
    private CharacterParser characterParser = CharacterParser.getInstance();// 实例化汉字转拼音类
    private PinyinComparator pinyinComparator = new PinyinComparator();
    private CreateGroupMembersAdapter adapter;
    private SideBar sideBar;
    private List<UserInfo> srcList;
    private List<UserInfo> userList = new ArrayList<>();

    private Dialog dialog;
    private TextView dialogs;
    private TextView textHeadName;
    private ListView listView;
    private EditText editSearchContent;
    private ImageView imageClear;

    private TipView tipView;// 没有数据、数据出错提示
    private TipView tipSearchNull;// 搜索没有数据提示

    private String groupId;
    private String tag = "GROUP_MEMBERS_VOLLEY_REQUEST_CANCEL_TAG";
    private boolean isCancelRequest;
    private FragmentActivity context;
    private View rootView;
    private GroupMembersFragment ct;
    private Dialog choiceDialog;

    @Override
    public void onWhiteViewClick() {
        if (GlobalConfig.CURRENT_NETWORK_STATE_TYPE != -1) {
            dialog = DialogUtils.Dialogph(context, "正在获取群成员信息");
            send();
        } else {
            tipView.setVisibility(View.VISIBLE);
            tipView.setTipView(TipView.TipStatus.NO_NET);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (rootView == null) {
            rootView = inflater.inflate(R.layout.activity_groupmembers, container, false);
            rootView.setOnClickListener(this);
            context = getActivity();
            ct=this;
            initView();
            initDialog();
        }
        return rootView;
    }

    private void initDialog() {
        View dialog1 = LayoutInflater.from(context).inflate(R.layout.dialog_group_choice, null);
        dialog1.findViewById(R.id.tv_cancel).setOnClickListener(this);
        dialog1.findViewById(R.id.tv_add).setOnClickListener(this);
        dialog1.findViewById(R.id.tv_del).setOnClickListener(this);
        choiceDialog = new Dialog(context, R.style.MyDialog);
        choiceDialog.setContentView(dialog1);
        choiceDialog.setCanceledOnTouchOutside(true);
        choiceDialog.getWindow().setBackgroundDrawableResource(R.color.dialog);
    }

    private void initView() {
        tipView = (TipView) rootView.findViewById(R.id.tip_view);
        tipView.setWhiteClick(this);
        tipSearchNull = (TipView) rootView.findViewById(R.id.tip_search_null);

        groupId = getArguments().getString("GroupId");
        rootView.findViewById(R.id.head_left_btn).setOnClickListener(this);

        editSearchContent = (EditText) rootView.findViewById(R.id.et_search);      // 搜索控件
        editSearchContent.addTextChangedListener(this);

        //lin_head_right
        rootView.findViewById(R.id.lin_head_right).setOnClickListener(this);       // 管理

        imageClear = (ImageView) rootView.findViewById(R.id.image_clear);
        imageClear.setOnClickListener(this);

        listView = (ListView) rootView.findViewById(R.id.country_lvcountry);
        listView.setOnItemClickListener(this);

        dialogs = (TextView)rootView. findViewById(R.id.dialog);
        sideBar = (SideBar) rootView.findViewById(R.id.sidrbar);
        sideBar.setTextView(dialogs);
        sideBar.setOnTouchingLetterChangedListener(this);

        textHeadName = (TextView) rootView.findViewById(R.id.head_name_tv);// 更新当前组员人数的控件

        if (GlobalConfig.CURRENT_NETWORK_STATE_TYPE != -1) {
            dialog = DialogUtils.Dialogph(context, "正在获取群成员信息");
            send();
        } else {
            tipView.setVisibility(View.VISIBLE);
            tipView.setTipView(TipView.TipStatus.NO_NET);
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.head_left_btn:
                DuiJiangActivity.close();
                break;
            case R.id.image_clear:
                imageClear.setVisibility(View.INVISIBLE);
                editSearchContent.setText("");
                tipSearchNull.setVisibility(View.GONE);
                break;
            case R.id.lin_head_right:
                if(choiceDialog!=null&&!choiceDialog.isShowing()){
                    choiceDialog.show();
                }
                break;
            case R.id.tv_cancel:
                if(choiceDialog!=null&&choiceDialog.isShowing()){
                    choiceDialog.dismiss();
                }
                break;
            case R.id.tv_add:
                GroupMemberAddFragment fg = new GroupMemberAddFragment();
                Bundle bundle = new Bundle();
                bundle.putString("GroupId", groupId);
                fg.setArguments(bundle);
                DuiJiangActivity.open(fg);
                if(choiceDialog!=null&&choiceDialog.isShowing()){
                    choiceDialog.dismiss();
                }
                break;
            case R.id.tv_del:
                GroupMemberDelFragment fg1 = new GroupMemberDelFragment();
                Bundle bundle1 = new Bundle();
                bundle1.putString("GroupId", groupId);
                fg1.setArguments(bundle1);
                fg1.setTargetFragment(ct, 2);
                DuiJiangActivity.open(fg1);
                if(choiceDialog!=null&&choiceDialog.isShowing()){
                    choiceDialog.dismiss();
                }
                break;
        }
    }

    // 更新界面
    public void RefreshFragmentData(){
        Fragment targetFragment = getTargetFragment();
        ((GroupDetailFragment) targetFragment).RefreshFragment();
      //  ToastUtils.show_always(context,"我回到ｍｅｍｂｅｒ啦" );
        DuiJiangActivity.close();
    }

    // 网络请求主函数
    private void send() {
        JSONObject jsonObject = VolleyRequest.getJsonObject(context);
        try {
            jsonObject.put("GroupId", groupId);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        VolleyRequest.RequestPost(GlobalConfig.grouptalkUrl, tag, jsonObject, new VolleyCallback() {
            private String ReturnType;
            private String Message;

            @Override
            protected void requestSuccess(JSONObject result) {
                if (dialog != null) dialog.dismiss();
                if (isCancelRequest) return;
                try {
                    ReturnType = result.getString("ReturnType");
                    Message = result.getString("Message");
                    Log.v("ReturnType", "ReturnType > > " + ReturnType + " == Message > > " + Message);
                } catch (JSONException e1) {
                    e1.printStackTrace();
                }
                if(ReturnType == null || ReturnType.equals("")) {
                    tipView.setVisibility(View.VISIBLE);
                    tipView.setTipView(TipView.TipStatus.IS_ERROR);
                    return ;
                }
                if (ReturnType.equals("1001") || ReturnType.equals("1002")) {
                    try {
                        srcList = new Gson().fromJson(result.getString("UserList"), new TypeToken<List<UserInfo>>() {}.getType());
                    } catch (Exception e1) {
                        e1.printStackTrace();
                        tipView.setVisibility(View.VISIBLE);
                        tipView.setTipView(TipView.TipStatus.IS_ERROR);
                    }
                    if (srcList != null && srcList.size() != 0) {
                        tipView.setVisibility(View.GONE);
                        int sum = srcList.size();
                        textHeadName.setText("全部成员(" + sum + ")");
                        userList.clear();
                        userList.addAll(srcList);
                        filledData(userList);
                        Collections.sort(userList, pinyinComparator);
                        listView.setAdapter(adapter = new CreateGroupMembersAdapter(context, userList));
                    } else {
                        tipView.setVisibility(View.VISIBLE);
                        tipView.setTipView(TipView.TipStatus.NO_DATA, "群组中没有成员!");
                    }
                } else {
                    tipView.setVisibility(View.VISIBLE);
                    tipView.setTipView(TipView.TipStatus.NO_DATA, "群组中没有成员!");
                }
            }

            @Override
            protected void requestError(VolleyError error) {
                if (dialog != null) dialog.dismiss();
                ToastUtils.showVolleyError(context);
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

    // 根据输入框中的值来过滤数据并更新ListView
    private void search(String searchName) {
        List<UserInfo> filterDateList = new ArrayList<>();
        if (TextUtils.isEmpty(searchName)) {
            filterDateList = userList;
            tipSearchNull.setVisibility(View.GONE);
        } else {
            filterDateList.clear();
            for (UserInfo sortModel : userList) {
                String name = sortModel.getName();
                if (name.contains(searchName) || characterParser.getSelling(name).startsWith(searchName)) {
                    filterDateList.add(sortModel);
                }
            }
        }
        if (filterDateList.size() == 0) {
            tipSearchNull.setVisibility(View.VISIBLE);
            tipSearchNull.setTipView(TipView.TipStatus.NO_DATA, "没有找到该好友哟\n换个好友再试一次吧");
            listView.setVisibility(View.GONE);
        } else {
            tipSearchNull.setVisibility(View.GONE);
            listView.setVisibility(View.VISIBLE);
            Collections.sort(filterDateList, pinyinComparator);// 根据 a - z 进行排序
            adapter.ChangeDate(filterDateList);
            userList.clear();
            userList.addAll(filterDateList);
        }
    }

    @Override
    public void onTouchingLetterChanged(String s) {
        int position = adapter.getPositionForSection(s.charAt(0));
        if (position != -1) {
            listView.setSelection(position);
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        boolean isFriend = false;
        if (userList.get(position).getUserId().equals(CommonUtils.getUserId(context))) {
            ToastUtils.show_always(context, "点击的是本人");
        } else {
            if (GlobalConfig.list_person != null && GlobalConfig.list_person.size() != 0) {
                for (int i = 0; i < GlobalConfig.list_person.size(); i++) {
                    if (userList.get(position).getUserId().equals(GlobalConfig.list_person.get(i).getUserId())) {
                        isFriend = true;
                        break;
                    }
                }
            } else {
                isFriend = false;// 不是我的好友
            }
            if (isFriend) {
                UserInfo tp = new UserInfo();
                tp.setPortraitBig(userList.get(position).getPortraitBig());
                tp.setPortraitMini(userList.get(position).getPortraitMini());
                tp.setUserName(userList.get(position).getUserName());
                tp.setUserId(userList.get(position).getUserId());
                tp.setUserAliasName(userList.get(position).getUserAliasName());

                TalkPersonNewsFragment fg4 = new TalkPersonNewsFragment();
                Bundle bundle = new Bundle();
                bundle.putString("type", "GroupMemers");
                bundle.putString("id", groupId);
                bundle.putSerializable("data", tp);
                fg4.setArguments(bundle);
                DuiJiangActivity.open(fg4);

            } else {

                GroupPersonNewsFragment fg4 = new GroupPersonNewsFragment();
                Bundle bundle = new Bundle();
                bundle.putString("type", "GroupMemers");
                bundle.putString("id", groupId);
                bundle.putSerializable("data", userList.get(position));
                fg4.setArguments(bundle);
                fg4.setTargetFragment(ct, 2);
                DuiJiangActivity.open(fg4);
            }
        }
    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
        String searchName = s.toString();
        if (searchName.trim().equals("")) {
            imageClear.setVisibility(View.INVISIBLE);
            tipSearchNull.setVisibility(View.GONE);
            if (srcList == null || srcList.size() == 0) {
                listView.setVisibility(View.GONE);
            } else {
                listView.setVisibility(View.VISIBLE);
                userList.clear();
                userList.addAll(srcList);
                filledData(userList);
                Collections.sort(userList, pinyinComparator);
                adapter = new CreateGroupMembersAdapter(context, userList);
                listView.setAdapter(adapter);
            }
        } else {
            userList.clear();
            userList.addAll(srcList);
            imageClear.setVisibility(View.VISIBLE);
            search(searchName);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        isCancelRequest = VolleyRequest.cancelRequest(tag);
        adapter = null;
        sideBar = null;
        dialogs = null;
        listView = null;
        textHeadName = null;
        editSearchContent = null;
        imageClear = null;
        pinyinComparator = null;
        characterParser = null;
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
    }

    @Override
    public void afterTextChanged(Editable s) {
    }
}
