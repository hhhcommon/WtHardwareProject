package com.wotingfm.ui.interphone.find.result;

import android.app.Dialog;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;

import com.android.volley.VolleyError;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.wotingfm.R;
import com.wotingfm.common.config.GlobalConfig;
import com.wotingfm.common.constant.StringConstant;
import com.wotingfm.common.helper.CommonHelper;
import com.wotingfm.common.volley.VolleyCallback;
import com.wotingfm.common.volley.VolleyRequest;
import com.wotingfm.ui.common.model.GroupInfo;
import com.wotingfm.ui.interphone.find.friendadd.FriendAddFragment;
import com.wotingfm.ui.interphone.find.groupadd.GroupAddFragment;
import com.wotingfm.ui.interphone.find.result.adapter.FindFriendResultAdapter;
import com.wotingfm.ui.interphone.find.result.adapter.FindGroupResultAdapter;
import com.wotingfm.ui.interphone.group.groupcontrol.groupdetail.main.GroupDetailFragment;
import com.wotingfm.ui.interphone.main.DuiJiangActivity;
import com.wotingfm.ui.interphone.model.UserInviteMeInside;
import com.wotingfm.util.CommonUtils;
import com.wotingfm.util.DialogUtils;
import com.wotingfm.util.ToastUtils;
import com.wotingfm.widget.TipView;
import com.wotingfm.widget.xlistview.XListView;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * 搜索结果页面
 * @author 辛龙
 * 2016年1月20日
 */
public class FindNewsResultFragment extends Fragment implements OnClickListener, TipView.WhiteViewClick {
    private XListView mxlistview;
    private int RefreshType;// 1，下拉刷新 2，加载更多
    private Dialog dialog;
    private String searchstr;
    private ArrayList<UserInviteMeInside> UserList;
    private ArrayList<GroupInfo> GroupList;
    private String type;
    private int PageNum;
    private FindFriendResultAdapter adapter;
    private FindGroupResultAdapter adapters;
    private String tag = "FINDNEWS_RESULT_VOLLEY_REQUEST_CANCEL_TAG";
    private boolean isCancelRequest;

    private TipView tipView;// 搜索没有数据提示
    private FragmentActivity context;
    private View rootView;

    @Override
    public void onWhiteViewClick() {
        searchFriendOrGroup();
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (rootView == null) {
            rootView = inflater.inflate(R.layout.activity_findnews_result, container, false);
            rootView.setOnClickListener(this);
            context = getActivity();
            setView();
            setListener();
            searchstr = getArguments().getString("searchstr");
            type = getArguments().getString(StringConstant.FIND_TYPE);

            if (!TextUtils.isEmpty(type)) {
                searchFriendOrGroup();
            } else {
                tipView.setVisibility(View.VISIBLE);
                tipView.setTipView(TipView.TipStatus.IS_ERROR);
            }
        }
        return rootView;
    }


    // 初始化 View
    private void setView() {
        mxlistview = (XListView) rootView.findViewById(R.id.listview_querycontact);
        tipView = (TipView) rootView.findViewById(R.id.tip_view);
        tipView.setWhiteClick(this);
    }

    // 搜索好友或群组
    private void searchFriendOrGroup() {
        if (type.equals(StringConstant.FIND_TYPE_PERSON)) {// 搜索好友
            if (!searchstr.trim().equals("")) {
                if (GlobalConfig.CURRENT_NETWORK_STATE_TYPE != -1) {
                    dialog = DialogUtils.Dialogph(context, "正在获取数据");
                    PageNum = 1;
                    RefreshType = 1;
                    getFriend();
                } else {
                    tipView.setVisibility(View.VISIBLE);
                    tipView.setTipView(TipView.TipStatus.NO_NET);
                }
            } else {
                tipView.setVisibility(View.VISIBLE);
                tipView.setTipView(TipView.TipStatus.IS_ERROR);
            }
        } else if (type.equals(StringConstant.FIND_TYPE_GROUP)) {// 搜索群组
            if (!searchstr.trim().equals("")) {
                if (GlobalConfig.CURRENT_NETWORK_STATE_TYPE != -1) {
                    dialog = DialogUtils.Dialogph(context, "正在获取数据");
                    PageNum = 1;
                    RefreshType = 1;
                    getGroup();
                } else {
                    tipView.setVisibility(View.VISIBLE);
                    tipView.setTipView(TipView.TipStatus.NO_NET);
                }
            } else {
                tipView.setVisibility(View.VISIBLE);
                tipView.setTipView(TipView.TipStatus.IS_ERROR);
            }
        }
    }

    // 设置对应的点击事件
    private void setListener() {
        rootView.findViewById(R.id.head_left_btn).setOnClickListener(this);
        mxlistview.setPullRefreshEnable(false);
        mxlistview.setPullLoadEnable(false);
        mxlistview.setXListViewListener(new XListView.IXListViewListener() {
            @Override
            public void onRefresh() {
                if (type.equals(StringConstant.FIND_TYPE_PERSON)) {// 获取刷新好友数据
                    if (GlobalConfig.CURRENT_NETWORK_STATE_TYPE != -1) {
                        RefreshType = 1;
                        PageNum = 1;
                        getFriend();
                    } else {
                        tipView.setVisibility(View.VISIBLE);
                        tipView.setTipView(TipView.TipStatus.NO_NET);
                    }
                } else if (type.equals("group")) {// 获取刷新群组数据
                    if (GlobalConfig.CURRENT_NETWORK_STATE_TYPE != -1) {
                        RefreshType = 1;
                        PageNum = 1;
                        getGroup();
                    } else {
                        tipView.setVisibility(View.VISIBLE);
                        tipView.setTipView(TipView.TipStatus.NO_NET);
                    }
                }
            }

            @Override
            public void onLoadMore() {
                if (!type.trim().equals("")) {
                    if (type.equals(StringConstant.FIND_TYPE_PERSON)) {// 获取加载更多好友数据
                        if(CommonHelper.checkNetwork(context)) {
                            RefreshType = 2;
                            PageNum = PageNum + 1;
                            getFriend();
                        }
                    } else if (type.equals(StringConstant.FIND_TYPE_GROUP)) {// 获取加载更多群组数据
                        if(CommonHelper.checkNetwork(context)) {
                            RefreshType = 2;
                            PageNum = PageNum + 1;
                            getGroup();
                        }
                    }
                }
            }
        });
    }

    private void setItemListener() {// 设置item对应的点击事件
        mxlistview.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, int position, long arg3) {
                if (!type.trim().equals("")) {
                    if (type.equals(StringConstant.FIND_TYPE_PERSON)) {
                        if (position > 0) {
                            if (UserList != null && UserList.size() > 0) {

                                FriendAddFragment fg = new FriendAddFragment();
                                Bundle bundles = new Bundle();
                                bundles.putSerializable("contact",UserList.get(position - 1));
                                fg.setArguments(bundles);
                                DuiJiangActivity.open(fg);

                            } else {
                                ToastUtils.show_always(context, "获取数据异常");
                            }
                        }
                    } else if (type.equals(StringConstant.FIND_TYPE_GROUP)) {
                        if (position > 0) {
                            if (GroupList != null && GroupList.size() > 0) {
                                if (GroupList.get(position - 1).getGroupCreator().equals(CommonUtils.getUserId(context))) {

                                    GroupDetailFragment fg = new GroupDetailFragment();
                                    Bundle bundles = new Bundle();
                                    bundles.putSerializable("data", GroupList.get(position - 1));
                                    bundles.putString("type", "groupaddactivity");
                                    fg.setArguments(bundles);
                                    DuiJiangActivity.open(fg);

                                } else {

                                    GroupAddFragment fg = new GroupAddFragment();
                                    Bundle bundles = new Bundle();
                                    bundles.putSerializable("contact", GroupList.get(position - 1));
                                    fg.setArguments(bundles);
                                    DuiJiangActivity.open(fg);

                                }
                            } else {
                                ToastUtils.show_always(context, "获取数据异常");
                            }
                        }
                    }
                }
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

    // 获取好友数据
    protected void getFriend() {
        JSONObject jsonObject = VolleyRequest.getJsonObject(context);
        try {
            jsonObject.put("Page", PageNum);
            jsonObject.put("SearchStr", searchstr);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        VolleyRequest.RequestPost(GlobalConfig.searchStrangerUrl, tag, jsonObject, new VolleyCallback() {
            private String ReturnType;
            private String ContactMeString;

            @Override
            protected void requestSuccess(JSONObject result) {
                if (dialog != null) dialog.dismiss();
                if (isCancelRequest) return;
                try {
                    ReturnType = result.getString("ReturnType");
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                try {
                    ContactMeString = result.getString("UserList");
                } catch (JSONException e1) {
                    e1.printStackTrace();
                }
                if (ReturnType != null && ReturnType.equals("1001")) {
                    UserList = new Gson().fromJson(ContactMeString, new TypeToken<List<UserInviteMeInside>>() {}.getType());
                    if (UserList != null && UserList.size() > 0) {
                        tipView.setVisibility(View.GONE);
                        if (RefreshType == 1) {
                            mxlistview.setAdapter(adapter = new FindFriendResultAdapter(context, UserList));
                            mxlistview.stopRefresh();
                        } else {
                            adapter.ChangeData(UserList);
                            mxlistview.stopLoadMore();
                        }
                        setItemListener();    // 设置 item 的点击事件
                    } else {
                        if(RefreshType == 1) {
                            tipView.setVisibility(View.VISIBLE);
                            tipView.setTipView(TipView.TipStatus.NO_DATA, "没有找到该好友哟\n换个好友再试一次吧");
                        }
                    }
                } else {
                    if(RefreshType == 1) {
                        tipView.setVisibility(View.VISIBLE);
                        tipView.setTipView(TipView.TipStatus.NO_DATA, "没有找到该好友哟\n换个好友再试一次吧");
                    }
                }
                if (RefreshType == 1) {
                    mxlistview.stopRefresh();
                } else {
                    mxlistview.stopLoadMore();
                }
            }

            @Override
            protected void requestError(VolleyError error) {
                if (dialog != null) dialog.dismiss();
                ToastUtils.showVolleyError(context);
                if(RefreshType == 1) {
                    tipView.setVisibility(View.VISIBLE);
                    tipView.setTipView(TipView.TipStatus.IS_ERROR);
                }
            }
        });
    }

    // 获取群组数据
    protected void getGroup() {
        JSONObject jsonObject = VolleyRequest.getJsonObject(context);
        try {
            jsonObject.put("Page", PageNum);
            jsonObject.put("SearchStr", searchstr);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        VolleyRequest.RequestPost(GlobalConfig.searchStrangerGroupUrl, tag, jsonObject, new VolleyCallback() {
            private String ReturnType;
            private String GroupMeString;

            @Override
            protected void requestSuccess(JSONObject result) {
                if (dialog != null) dialog.dismiss();
                if (isCancelRequest) return;
                try {
                    ReturnType = result.getString("ReturnType");
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                try {
                    GroupMeString = result.getString("GroupList");
                } catch (JSONException e1) {
                    e1.printStackTrace();
                }
                if (ReturnType != null && ReturnType.equals("1001")) {
                    GroupList = new Gson().fromJson(GroupMeString, new TypeToken<List<GroupInfo>>() {}.getType());
                    if (GroupList != null && GroupList.size() > 0) {
                        tipView.setVisibility(View.GONE);
                        if (RefreshType == 1) {
                            mxlistview.setAdapter(adapters = new FindGroupResultAdapter(context, GroupList));
                            mxlistview.stopRefresh();
                        } else {
                            adapters.ChangeData(GroupList);
                            mxlistview.stopLoadMore();
                        }
                        setItemListener();    // 设置 item 的点击事件
                    } else {
                        tipView.setVisibility(View.VISIBLE);
                        tipView.setTipView(TipView.TipStatus.NO_DATA, "没有找到该群组哟\n换个群组再试一次吧");
                    }
                } else {
                    tipView.setVisibility(View.VISIBLE);
                    tipView.setTipView(TipView.TipStatus.NO_DATA, "没有找到该群组哟\n换个群组再试一次吧");
                }
                if (RefreshType == 1) {
                    mxlistview.stopRefresh();
                } else {
                    mxlistview.stopLoadMore();
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
    public void onDestroy() {
        super.onDestroy();
        isCancelRequest = VolleyRequest.cancelRequest(tag);
        UserList = null;
        GroupList = null;
        mxlistview = null;
        adapter = null;
        adapters = null;
        context = null;
        dialog = null;
        searchstr = null;
        type = null;
        tag = null;
    }
}
