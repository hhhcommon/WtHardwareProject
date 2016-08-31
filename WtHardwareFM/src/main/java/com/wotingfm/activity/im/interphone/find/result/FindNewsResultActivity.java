package com.wotingfm.activity.im.interphone.find.result;

import com.android.volley.VolleyError;
import com.wotingfm.R;
import com.wotingfm.activity.im.interphone.find.result.adapter.FindFriendResultAdapter;
import com.wotingfm.activity.im.interphone.find.result.adapter.FindGroupResultAdapter;
import com.wotingfm.activity.im.interphone.find.result.model.FindGroupNews;
import com.wotingfm.activity.im.interphone.find.result.model.UserInviteMeInside;
import com.wotingfm.common.base.BaseActivity;
import com.wotingfm.common.config.GlobalConfig;
import com.wotingfm.common.volley.VolleyCallback;
import com.wotingfm.common.volley.VolleyRequest;
import com.wotingfm.util.CommonUtils;
import com.wotingfm.util.ToastUtils;
import com.wotingfm.widget.xlistview.XListView;
import com.wotingfm.widget.xlistview.XListView.IXListViewListener;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * 搜索结果页面
 *
 * @author 辛龙
 *         2016年1月20日
 */
public class FindNewsResultActivity extends BaseActivity {
    private XListView mXListView;
    private int RefreshType;        // 1，下拉刷新 2，加载更多
    private String searchstr;
    private ArrayList<UserInviteMeInside> UserList;
    private ArrayList<FindGroupNews> GroupList;
    private String type;
    private int PageNum;
    private FindFriendResultAdapter adapter;
    private FindGroupResultAdapter adapters;
    private FindNewsResultActivity context;
    private String tag = "FIND_NEWS_RESULT_VOLLEY_REQUEST_CANCEL_TAG";
    private boolean isCancelRequest;

    @Override
    protected int setViewId() {
        return R.layout.activity_findnews_result;
    }

    @Override
    protected void init() {
        mXListView = (XListView) findViewById(R.id.listview_querycontact);
    }

    // 设置对应的点击事件
    private void setListener() {
        mXListView.setPullRefreshEnable(false);
        mXListView.setPullLoadEnable(false);
        mXListView.setXListViewListener(new IXListViewListener() {
            @Override
            public void onRefresh() {
                // 数据请求
                if (!type.trim().equals("")) {
                    if (type.equals("friend")) {
                        // 获取刷新好友数据
                        if (GlobalConfig.CURRENT_NETWORK_STATE_TYPE != -1) {
                            RefreshType = 1;
                            PageNum = 1;
                            getFriend();
                        } else {
                            ToastUtils.show_allways(FindNewsResultActivity.this, "网络连接失败，请稍后重试");
                        }
                    } else if (type.equals("group")) {
                        // 获取刷新群组数据
                        if (GlobalConfig.CURRENT_NETWORK_STATE_TYPE != -1) {
                            RefreshType = 1;
                            PageNum = 1;
                            getGroup();
                        } else {
                            ToastUtils.show_allways(FindNewsResultActivity.this, "网络连接失败，请稍后重试");
                        }
                    }
                }
            }

            @Override
            public void onLoadMore() {
                if (!type.trim().equals("")) {
                    if (type.equals("friend")) {
                        // 获取加载更多好友数据
                        if (GlobalConfig.CURRENT_NETWORK_STATE_TYPE != -1) {
                            RefreshType = 2;
                            PageNum = PageNum + 1;
                            getFriend();
                        } else {
                            ToastUtils.show_allways(FindNewsResultActivity.this, "网络连接失败，请稍后重试");
                        }
                    } else if (type.equals("group")) {
                        // 获取加载更多群组数据
                        if (GlobalConfig.CURRENT_NETWORK_STATE_TYPE != -1) {
                            RefreshType = 2;
                            PageNum = PageNum + 1;
                            getGroup();
                        } else {
                            ToastUtils.show_allways(FindNewsResultActivity.this, "网络连接失败，请稍后重试");
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
            jsonObject.put("UserId", CommonUtils.getUserId(this));
            jsonObject.put("Page", PageNum);
            jsonObject.put("SearchStr", searchstr);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        VolleyRequest.RequestPost(GlobalConfig.searchStrangerUrl, tag, jsonObject, new VolleyCallback() {
            //			private String SessionId;
            private String ReturnType;
            private String Message;
            private String ContactMeString;

            @Override
            protected void requestSuccess(JSONObject result) {
                if (isCancelRequest) {
                    return;
                }
                try {
                    ReturnType = result.getString("ReturnType");
//					SessionId = result.getString("SessionId");
                    Message = result.getString("Message");
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                try {
                    ContactMeString = result.getString("UserList");
                } catch (JSONException e1) {
                    e1.printStackTrace();
                }
                if (ReturnType != null && ReturnType.equals("1001")) {

                }
            }

            @Override
            protected void requestError(VolleyError error) {

            }
        });
    }

    /*
     * 获取群组数据
     */
    protected void getGroup() {
        JSONObject jsonObject = VolleyRequest.getJsonObject(context);
        try {
            jsonObject.put("UserId", CommonUtils.getUserId(this));
            jsonObject.put("Page", PageNum);
            jsonObject.put("SearchStr", searchstr);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        VolleyRequest.RequestPost(GlobalConfig.searchStrangerGroupUrl, tag, jsonObject, new VolleyCallback() {
            //			private String SessionId;
            private String ReturnType;
            private String Message;
            private String GroupMeString;

            @Override
            protected void requestSuccess(JSONObject result) {
                if (isCancelRequest) {
                    return;
                }
                try {
                    ReturnType = result.getString("ReturnType");
//					SessionId = result.getString("SessionId");
                    Message = result.getString("Message");
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                try {
                    GroupMeString = result.getString("GroupList");
                } catch (JSONException e1) {
                    e1.printStackTrace();
                }
                if (ReturnType != null && ReturnType.equals("1001")) {

                } else if (ReturnType != null && ReturnType.equals("1002")) {
                    if (RefreshType == 1) {
                        mXListView.stopRefresh();
                    } else {
                        mXListView.stopLoadMore();
                    }
                    // 获取数据失败
                    if (Message != null && !Message.trim().equals("")) {
                        ToastUtils.show_allways(FindNewsResultActivity.this, Message);
                    } else {
                        ToastUtils.show_allways(FindNewsResultActivity.this, "数据获取失败，请稍候再试");
                    }
                } else {
                    if (RefreshType == 1) {
                        mXListView.stopRefresh();
                    } else {
                        mXListView.stopLoadMore();
                    }
                    if (Message != null && !Message.trim().equals("")) {
                        ToastUtils.show_allways(FindNewsResultActivity.this, Message);
                    } else {
                        ToastUtils.show_allways(FindNewsResultActivity.this, "数据获取失败，请稍候再试");
                    }
                }
            }

            @Override
            protected void requestError(VolleyError error) {

            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        isCancelRequest = VolleyRequest.cancelRequest(tag);
    }
}
