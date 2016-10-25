package com.wotingfm.activity.im.interphone.find.result;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;

import com.android.volley.VolleyError;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.wotingfm.R;
import com.wotingfm.activity.common.baseactivity.AppBaseActivity;
import com.wotingfm.activity.im.interphone.find.add.FriendAddActivity;
import com.wotingfm.activity.im.interphone.find.add.GroupAddActivity;
import com.wotingfm.activity.im.interphone.find.result.adapter.FindFriendResultAdapter;
import com.wotingfm.activity.im.interphone.find.result.adapter.FindGroupResultAdapter;
import com.wotingfm.activity.im.interphone.find.result.model.FindGroupNews;
import com.wotingfm.activity.im.interphone.find.result.model.UserInviteMeInside;
import com.wotingfm.activity.im.interphone.groupmanage.groupdetail.activity.GroupDetailActivity;
import com.wotingfm.common.config.GlobalConfig;
import com.wotingfm.common.constant.StringConstant;
import com.wotingfm.common.volley.VolleyCallback;
import com.wotingfm.common.volley.VolleyRequest;
import com.wotingfm.util.CommonUtils;
import com.wotingfm.util.DialogUtils;
import com.wotingfm.util.ToastUtils;
import com.wotingfm.widget.xlistview.XListView;
import com.wotingfm.widget.xlistview.XListView.IXListViewListener;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * 搜索结果页面
 * 作者：xinlong on 2016/1/19 21:18
 * 邮箱：645700751@qq.com
 */

public class FindNewsResultActivity extends AppBaseActivity {
    private XListView mXListView;
    private Dialog dialog;

    private ArrayList<UserInviteMeInside> UserList;
    private ArrayList<FindGroupNews> GroupList;

    private FindFriendResultAdapter adapter;
    private FindGroupResultAdapter adapters;
    private String searchStr;
    private String type;
    private String tag = "FINDNEWS_RESULT_VOLLEY_REQUEST_CANCEL_TAG";
    private boolean isCancelRequest;
    private int RefreshType;        // 1，下拉刷新 2，加载更多
    private int PageNum;

    @Override
    protected int setViewId() {
        return R.layout.activity_findnews_result;
    }

    @Override
    protected void init() {
        setTitle("搜索结果");
        setView();
        setListener();
        searchStr = this.getIntent().getStringExtra(StringConstant.FIND_CONTENT_TO_RESULT);
        type = this.getIntent().getStringExtra(StringConstant.FIND_TYPE);
        if (!type.trim().equals("")) {
            if (type.equals(StringConstant.FIND_TYPE_PERSON)) {
                // 搜索好友
                if (!searchStr.trim().equals("")) {
                    if (GlobalConfig.CURRENT_NETWORK_STATE_TYPE != -1) {
                        dialog = DialogUtils.Dialogph(FindNewsResultActivity.this, "正在获取数据");
                        PageNum = 1;
                        RefreshType = 1;
                        getFriend();
                    } else {
                        ToastUtils.show_always(FindNewsResultActivity.this, "网络连接失败，请稍后重试");
                    }
                } else {
                    // 如果当前界面没有接收到数据就给以友好提示
                    ToastUtils.show_always(FindNewsResultActivity.this, "网络连接失败，请稍后重试");
                }
            } else if (type.equals(StringConstant.FIND_TYPE_GROUP)) {
                // 搜索群组
                if (!searchStr.trim().equals("")) {
                    if (GlobalConfig.CURRENT_NETWORK_STATE_TYPE != -1) {
                        dialog = DialogUtils.Dialogph(FindNewsResultActivity.this, "正在获取数据");
                        PageNum = 1;
                        RefreshType = 1;
                        getGroup();
                    } else {
                        ToastUtils.show_always(FindNewsResultActivity.this, "网络连接失败，请稍后重试");
                    }
                } else {
                    // 如果当前界面没有接收到数据就给以友好提示
                    ToastUtils.show_always(FindNewsResultActivity.this, "网络连接失败，请稍后重试");
                }
            }
        } else {
            // 如果当前界面没有接收到搜索类型数据就给以友好提示
            ToastUtils.show_always(FindNewsResultActivity.this, "网络连接失败，请稍后重试");
        }
    }

    private void setView() {
        mXListView = (XListView) findViewById(R.id.listview_querycontact);
    }

    private void setListener() {// 设置对应的点击事件
        mXListView.setPullRefreshEnable(false);
        mXListView.setPullLoadEnable(false);
        mXListView.setXListViewListener(new IXListViewListener() {
            @Override
            public void onRefresh() {
                // 数据请求
                if (!type.trim().equals("")) {
                    if (type.equals(StringConstant.FIND_TYPE_PERSON)) {
                        // 获取刷新好友数据
                        if (GlobalConfig.CURRENT_NETWORK_STATE_TYPE != -1) {
                            RefreshType = 1;
                            PageNum = 1;
                            getFriend();
                        } else {
                            ToastUtils.show_always(FindNewsResultActivity.this, "网络连接失败，请稍后重试");
                        }
                    } else if (type.equals(StringConstant.FIND_TYPE_GROUP)) {
                        // 获取刷新群组数据
                        if (GlobalConfig.CURRENT_NETWORK_STATE_TYPE != -1) {
                            RefreshType = 1;
                            PageNum = 1;
                            getGroup();
                        } else {
                            ToastUtils.show_always(FindNewsResultActivity.this, "网络连接失败，请稍后重试");
                        }
                    }
                }
            }

            @Override
            public void onLoadMore() {
                if (!type.trim().equals("")) {
                    if (type.equals(StringConstant.FIND_TYPE_PERSON)) {
                        // 获取加载更多好友数据
                        if (GlobalConfig.CURRENT_NETWORK_STATE_TYPE != -1) {
                            RefreshType = 2;
                            PageNum = PageNum + 1;
                            getFriend();
                        } else {
                            ToastUtils.show_always(FindNewsResultActivity.this, "网络连接失败，请稍后重试");
                        }
                    } else if (type.equals(StringConstant.FIND_TYPE_GROUP)) {
                        // 获取加载更多群组数据
                        if (GlobalConfig.CURRENT_NETWORK_STATE_TYPE != -1) {
                            RefreshType = 2;
                            PageNum = PageNum + 1;
                            getGroup();
                        } else {
                            ToastUtils.show_always(FindNewsResultActivity.this, "网络连接失败，请稍后重试");
                        }
                    }
                }
            }
        });
    }

    private void setItemListener() {// 设置 item 对应的点击事件
        mXListView.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, int position, long arg3) {
                if (!type.trim().equals("")) {
                    if (type.equals(StringConstant.FIND_TYPE_PERSON)) {
                        if (position > 0) {
                            if (UserList != null && UserList.size() > 0) {
                                Intent intent = new Intent(FindNewsResultActivity.this, FriendAddActivity.class);
                                Bundle bundle = new Bundle();
                                bundle.putSerializable("contact", UserList.get(position - 1));
                                intent.putExtras(bundle);
                                startActivity(intent);
                            } else {
                                ToastUtils.show_always(FindNewsResultActivity.this, "获取数据异常");
                            }
                        }
                    } else if (type.equals(StringConstant.FIND_TYPE_GROUP)) {
                        if (position > 0) {
                            if (GroupList != null && GroupList.size() > 0) {
                                if (GroupList.get(position - 1).getGroupCreator().equals(CommonUtils.getUserId(context))) {
                                    Intent intent = new Intent(FindNewsResultActivity.this, GroupDetailActivity.class);
                                    Bundle bundle = new Bundle();
                                    bundle.putSerializable("data", GroupList.get(position - 1));
                                    bundle.putString("type", "FindNewsResultActivity");
                                    intent.putExtras(bundle);
                                    startActivity(intent);
                                } else {
                                    Intent intent = new Intent(FindNewsResultActivity.this, GroupAddActivity.class);
                                    Bundle bundle = new Bundle();
                                    bundle.putSerializable("contact", GroupList.get(position - 1));
                                    intent.putExtras(bundle);
                                    startActivity(intent);
                                }
                            } else {
                                ToastUtils.show_always(FindNewsResultActivity.this, "获取数据异常");
                            }
                        }
                    }
                }
            }
        });
    }

    /*
     * 获取好友数据
     */
    protected void getFriend() {
        JSONObject jsonObject = VolleyRequest.getJsonObject(context);
        try {
            jsonObject.put("Page", PageNum);
            jsonObject.put("SearchStr", searchStr);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        VolleyRequest.RequestPost(GlobalConfig.searchStrangerUrl, tag, jsonObject, new VolleyCallback() {
            @Override
            protected void requestSuccess(JSONObject result) {
                if (dialog != null) dialog.dismiss();
                if (isCancelRequest) return;
                try {
                    String ReturnType = result.getString("ReturnType");
                    if (ReturnType != null && ReturnType.equals("1001")) {
                        try {
                            String ContactMeString = result.getString("UserList");
                            UserList = new Gson().fromJson(ContactMeString, new TypeToken<List<UserInviteMeInside>>() {
                            }.getType());
                            if (UserList != null && UserList.size() > 0) {
                                if (RefreshType == 1) {
                                    adapter = new FindFriendResultAdapter(FindNewsResultActivity.this, UserList);
                                    mXListView.setAdapter(adapter);
                                    mXListView.stopRefresh();
                                } else {
                                    adapter.ChangeData(UserList);
                                    mXListView.stopLoadMore();
                                }
                                setItemListener();    // 设置item的点击事件
                            } else {
                                ToastUtils.show_always(FindNewsResultActivity.this, "数据获取失败，请稍候再试");    // json解析失败
                            }
                        } catch (JSONException e1) {
                            e1.printStackTrace();
                        }
                    } else if (ReturnType != null && ReturnType.equals("1002")) {
                        if (RefreshType == 1) {
                            mXListView.stopRefresh();
                        } else {
                            mXListView.stopLoadMore();
                        }
                        // 获取数据失败
                        ToastUtils.show_always(FindNewsResultActivity.this, "数据获取失败，请稍候再试");
                    } else {
                        if (RefreshType == 1) {
                            mXListView.stopRefresh();
                        } else {
                            mXListView.stopLoadMore();
                        }
                        ToastUtils.show_always(FindNewsResultActivity.this, "数据获取失败，请稍候再试");
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

    /*
     * 获取群组数据
     */
    protected void getGroup() {
        JSONObject jsonObject = VolleyRequest.getJsonObject(context);
        try {
            jsonObject.put("Page", PageNum);
            jsonObject.put("SearchStr", searchStr);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        VolleyRequest.RequestPost(GlobalConfig.searchStrangerGroupUrl, tag, jsonObject, new VolleyCallback() {
            @Override
            protected void requestSuccess(JSONObject result) {
                if (dialog != null) dialog.dismiss();
                if (isCancelRequest) return;
                try {
                    String ReturnType = result.getString("ReturnType");
                    if (ReturnType != null && ReturnType.equals("1001")) {
                        try {
                            String GroupMeString = result.getString("GroupList");
                            GroupList = new Gson().fromJson(GroupMeString, new TypeToken<List<FindGroupNews>>() {
                            }.getType());
                            if (GroupList != null && GroupList.size() > 0) {
                                if (RefreshType == 1) {
                                    adapters = new FindGroupResultAdapter(FindNewsResultActivity.this, GroupList);
                                    mXListView.setAdapter(adapters);
                                    mXListView.stopRefresh();
                                } else {
                                    adapters.ChangeData(GroupList);
                                    mXListView.stopLoadMore();
                                }
                                setItemListener();    // 设置item的点击事件
                            } else {
                                ToastUtils.show_always(FindNewsResultActivity.this, "数据获取失败，请稍候再试");    // json解析失败
                            }
                        } catch (JSONException e1) {
                            e1.printStackTrace();
                        }
                    } else if (ReturnType != null && ReturnType.equals("1002")) {
                        if (RefreshType == 1) {
                            mXListView.stopRefresh();
                        } else {
                            mXListView.stopLoadMore();
                        }
                        // 获取数据失败
                        ToastUtils.show_always(FindNewsResultActivity.this, "数据获取失败，请稍候再试");
                    } else {
                        if (RefreshType == 1) {
                            mXListView.stopRefresh();
                        } else {
                            mXListView.stopLoadMore();
                        }
                        ToastUtils.show_always(FindNewsResultActivity.this, "数据获取失败，请稍候再试");
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
        UserList = null;
        GroupList = null;
        mXListView = null;
        adapter = null;
        adapters = null;
        context = null;
        dialog = null;
        searchStr = null;
        type = null;
        tag = null;
        setContentView(R.layout.activity_null);
    }
}
