package com.wotingfm.ui.music.program.fmlist.activity;

import android.app.Dialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.TextView;

import com.android.volley.VolleyError;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.wotingfm.R;
import com.wotingfm.common.application.BSApplication;
import com.wotingfm.common.config.GlobalConfig;
import com.wotingfm.common.constant.BroadcastConstants;
import com.wotingfm.common.constant.StringConstant;
import com.wotingfm.common.volley.VolleyCallback;
import com.wotingfm.common.volley.VolleyRequest;
import com.wotingfm.ui.main.MainActivity;
import com.wotingfm.ui.music.main.ProgramActivity;
import com.wotingfm.ui.music.main.dao.SearchPlayerHistoryDao;
import com.wotingfm.ui.music.player.model.PlayerHistory;
import com.wotingfm.ui.music.program.diantai.model.RadioPlay;
import com.wotingfm.ui.music.program.fmlist.adapter.RankInfoAdapter;
import com.wotingfm.ui.music.program.fmlist.model.RankInfo;
import com.wotingfm.util.CommonUtils;
import com.wotingfm.util.DialogUtils;
import com.wotingfm.util.ToastUtils;
import com.wotingfm.widget.TipView;
import com.wotingfm.widget.xlistview.XListView;
import com.wotingfm.widget.xlistview.XListView.IXListViewListener;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.util.ArrayList;
import java.util.List;

/**
 * 电台列表
 *
 * @author 辛龙
 *         2016年8月8日
 */
public class FMListFragment extends Fragment implements View.OnClickListener, TipView.WhiteViewClick {
    private RankInfoAdapter adapter;
    private SharedPreferences shared = BSApplication.SharedPreferences;
    private SearchPlayerHistoryDao dbDao;
    private List<RankInfo> newList = new ArrayList<>();
    private List<RankInfo> subList;

    private Dialog dialog;
    private XListView mListView;
    private TextView textHead;
    private TipView tipView;// 没有网络、没有数据、数据错误提示

    private int viewType = 1;
    private int page = 1;
    private int refreshType = 1;// == 1 为下拉加载  == 2 为上拉加载更多

    private String CatalogType;
    private String CatalogId;
    private String tag = "FM_LIST_VOLLEY_REQUEST_CANCEL_TAG";
    private boolean isCancelRequest;
    private FragmentActivity context;
    private View rootView;

    @Override
    public void onWhiteViewClick() {
        dialog = DialogUtils.Dialogph(context, "正在获取数据");
        sendRequest();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (rootView == null) {
            rootView = inflater.inflate(R.layout.activity_fmlist, container, false);
            context = getActivity();
            initView();
            initEvent();
            initData();

            dialog = DialogUtils.Dialogph(context, "正在获取数据");
            sendRequest();
        }
        return rootView;
    }


    // 初始化视图
    private void initView() {
        tipView = (TipView) rootView.findViewById(R.id.tip_view);
        tipView.setWhiteClick(this);

        mListView = (XListView) rootView.findViewById(R.id.listview_fm);
        textHead = (TextView) rootView.findViewById(R.id.head_name_tv);
    }

    // 初始化数据
    private void initData() {
        dbDao = new SearchPlayerHistoryDao(context);// 初始化数据库对象

        String CatalogName;
        String type = getArguments().getString("fromtype");
        String Position = getArguments().getString("Position");
        if (Position == null || Position.trim().equals("")) {
            viewType = 1;
        } else {
            viewType = -1;
        }
        RadioPlay list;
        if (type != null && type.trim().equals("online")) {
            CatalogName = getArguments().getString("name");
            CatalogId = getArguments().getString("id");
        } else if (type != null && type.trim().equals("net")) {
            CatalogName = getArguments().getString("name");
            CatalogId = getArguments().getString("id");
            CatalogType = getArguments().getString("type");
            viewType = 2;
        } else if (type != null && type.trim().equals("cityRadio")) {
            CatalogName = getArguments().getString("name");
            CatalogId = getArguments().getString("id");
            CatalogType = getArguments().getString("type");
            viewType = 3;
        } else {
            list = (RadioPlay) getArguments().getSerializable("list");
            CatalogName = list.getCatalogName();
            CatalogId = list.getCatalogId();
        }
        textHead.setText(CatalogName);
    }

    // 初始化点击事件
    private void initEvent() {
        rootView.findViewById(R.id.head_left_btn).setOnClickListener(this);

        mListView.setPullLoadEnable(true);
        mListView.setPullRefreshEnable(true);
        mListView.setSelector(new ColorDrawable(Color.TRANSPARENT));
        mListView.setXListViewListener(new IXListViewListener() {
            @Override
            public void onRefresh() {
                refreshType = 1;
                page = 1;
                sendRequest();
            }

            @Override
            public void onLoadMore() {
                refreshType = 2;
                sendRequest();
            }
        });
    }

    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.head_left_btn:// 返回
                ProgramActivity activity = (ProgramActivity) getActivity();
                activity.fm.popBackStack();
                break;
        }
    }

    // 发送网络请求获取数据
    private void sendRequest() {
        if (GlobalConfig.CURRENT_NETWORK_STATE_TYPE == -1) {
            if (dialog != null) dialog.dismiss();
            if (refreshType == 1) {
                tipView.setVisibility(View.VISIBLE);
                tipView.setTipView(TipView.TipStatus.NO_NET);
                mListView.stopRefresh();
            } else {
                mListView.stopLoadMore();
                ToastUtils.show_always(context, "请检查网络设置");
            }
            return;
        }

        VolleyRequest.RequestPost(GlobalConfig.getContentUrl, tag, setParam(), new VolleyCallback() {
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
                        JSONObject arg1 = (JSONObject) new JSONTokener(result.getString("ResultList")).nextValue();
                        subList = new Gson().fromJson(arg1.getString("List"), new TypeToken<List<RankInfo>>() {
                        }.getType());
                        if (subList != null && subList.size() >= 10) {
                            page++;
                            mListView.setPullLoadEnable(true);
                        } else {
                            mListView.stopLoadMore();
                            mListView.setPullLoadEnable(false);
                        }

                        if (refreshType == 1) newList.clear();
                        newList.addAll(subList);
                        if (adapter == null) {
                            mListView.setAdapter(adapter = new RankInfoAdapter(context, newList));
                        } else {
                            adapter.notifyDataSetChanged();
                        }
                        setListView();
                        tipView.setVisibility(View.GONE);
                    } catch (Exception e) {
                        e.printStackTrace();
                        if (refreshType == 1) {
                            tipView.setVisibility(View.VISIBLE);
                            tipView.setTipView(TipView.TipStatus.IS_ERROR);
                        }
                    }

                    if (refreshType == 1) {
                        mListView.stopRefresh();
                    } else {
                        mListView.stopLoadMore();
                    }
                } else {
                    if (refreshType == 1) {
                        tipView.setVisibility(View.VISIBLE);
                        tipView.setTipView(TipView.TipStatus.NO_DATA, "没有找到相关结果\n换个电台试试吧");
                    } else {
                        mListView.stopLoadMore();
                        mListView.setPullLoadEnable(false);
                    }
                }
            }

            @Override
            protected void requestError(VolleyError error) {
                if (dialog != null) dialog.dismiss();
                if (refreshType == 1) {
                    tipView.setVisibility(View.VISIBLE);
                    tipView.setTipView(TipView.TipStatus.IS_ERROR);
                }
            }
        });
    }

    private JSONObject setParam() {
        JSONObject jsonObject = VolleyRequest.getJsonObject(context);
        try {
            jsonObject.put("MediaType", "RADIO");
            String cityId = shared.getString(StringConstant.CITYID, "110000");
            if (viewType == 1) {
                // 获取当前城市下所有分类内容
                jsonObject.put("CatalogId", cityId);
                jsonObject.put("CatalogType", "2");
                jsonObject.put("PerSize", "3");
                jsonObject.put("ResultType", "3");
                jsonObject.put("PageSize", "10");
                jsonObject.put("Page", String.valueOf(page));
            } else if (viewType == 2) {
                jsonObject.put("CatalogId", CatalogId);
                jsonObject.put("CatalogType", CatalogType);
                jsonObject.put("PerSize", "3");
                jsonObject.put("ResultType", "3");
                jsonObject.put("PageSize", "10");
                jsonObject.put("Page", String.valueOf(page));
            } else if (viewType == 3) {
                jsonObject.put("CatalogId", CatalogId);
                jsonObject.put("CatalogType", CatalogType);
                jsonObject.put("ResultType", "3");
                jsonObject.put("PageSize", "50");
                jsonObject.put("Page", String.valueOf(page));
            } else {
                // 按照分类获取内容
                JSONObject js = new JSONObject();
                jsonObject.put("CatalogType", "1");
                jsonObject.put("CatalogId", CatalogId);
                js.put("CatalogType", "2");
                js.put("CatalogId", cityId);
                jsonObject.put("FilterData", js);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return jsonObject;
    }

    // 这里要改
    protected void setListView() {
        mListView.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (newList != null && newList.get(position - 1) != null && newList.get(position - 1).getMediaType() != null) {
                    String MediaType = newList.get(position - 1).getMediaType();
                    if (MediaType.equals("RADIO") || MediaType.equals("AUDIO")) {
                        String playername = newList.get(position - 1).getContentName();
                        String playerimage = newList.get(position - 1).getContentImg();
                        String playerurl = newList.get(position - 1).getContentPlay();
                        String playerurI = newList.get(position - 1).getContentURI();
                        String playcontentshareurl = newList.get(position - 1).getContentShareURL();
                        String playermediatype = newList.get(position - 1).getMediaType();
                        String plaplayeralltime = newList.get(position - 1).getContentTimes();
                        String playerintime = "0";
                        String playercontentdesc = newList.get(position - 1).getContentDescn();
                        String playernum = newList.get(position - 1).getPlayCount();
                        String playerzantype = "0";
                        String playerfrom = newList.get(position - 1).getContentPub();
                        String playerfromid = "";
                        String playerfromurl = "";
                        String playeraddtime = Long.toString(System.currentTimeMillis());
                        String bjuserid = CommonUtils.getUserId(context);
                        String ContentFavorite = newList.get(position - 1).getContentFavorite();
                        String ContentId = newList.get(position - 1).getContentId();
                        String localurl = newList.get(position - 1).getLocalurl();
                        String sequName = newList.get(position - 1).getSequName();
                        String sequId = newList.get(position - 1).getSequId();
                        String sequDesc = newList.get(position - 1).getSequDesc();
                        String sequImg = newList.get(position - 1).getSequImg();

                        // 如果该数据已经存在数据库则删除原有数据，然后添加最新数据
                        PlayerHistory history = new PlayerHistory(
                                playername, playerimage, playerurl, playerurI, playermediatype,
                                plaplayeralltime, playerintime, playercontentdesc, playernum,
                                playerzantype, playerfrom, playerfromid, playerfromurl, playeraddtime, bjuserid, playcontentshareurl,
                                ContentFavorite, ContentId, localurl, sequName, sequId, sequDesc, sequImg);
                        dbDao.deleteHistory(playerurl);
                        dbDao.addHistory(history);
                        MainActivity.changeOne();


                        Intent push = new Intent(BroadcastConstants.PLAY_TEXT_VOICE_SEARCH);
                        Bundle bundle1 = new Bundle();
                        bundle1.putString("text", newList.get(position - 1).getContentName());
                        push.putExtras(bundle1);
                        context.sendBroadcast(push);
                    }
                }
            }
        });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        isCancelRequest = VolleyRequest.cancelRequest(tag);
        mListView = null;
        dialog = null;
        textHead = null;
        if (dbDao != null) {
            dbDao.closedb();
            dbDao = null;
        }
        newList.clear();
        newList = null;
        if (subList != null) {
            subList.clear();
            subList = null;
        }
        adapter = null;
    }
}