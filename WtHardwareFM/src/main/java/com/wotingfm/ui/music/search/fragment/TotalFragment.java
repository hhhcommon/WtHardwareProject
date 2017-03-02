package com.wotingfm.ui.music.search.fragment;

import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ExpandableListView;
import android.widget.ExpandableListView.OnChildClickListener;
import android.widget.ExpandableListView.OnGroupClickListener;

import com.android.volley.VolleyError;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.wotingfm.R;
import com.wotingfm.common.config.GlobalConfig;
import com.wotingfm.common.constant.BroadcastConstants;
import com.wotingfm.common.volley.VolleyCallback;
import com.wotingfm.common.volley.VolleyRequest;
import com.wotingfm.ui.main.MainActivity;
import com.wotingfm.ui.music.main.PlayerActivity;
import com.wotingfm.ui.music.main.dao.SearchPlayerHistoryDao;
import com.wotingfm.ui.music.player.main.PlayerFragment;
import com.wotingfm.ui.music.player.model.PlayerHistory;
import com.wotingfm.ui.music.player.more.album.main.AlbumFragment;
import com.wotingfm.ui.music.program.fmlist.model.RankInfo;
import com.wotingfm.ui.music.search.adapter.SearchContentAdapter;
import com.wotingfm.ui.music.search.main.SearchLikeActivity;
import com.wotingfm.ui.music.search.model.SuperRankInfo;
import com.wotingfm.util.CommonUtils;
import com.wotingfm.util.DialogUtils;
import com.wotingfm.util.L;
import com.wotingfm.util.ToastUtils;
import com.wotingfm.widget.TipView;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.util.ArrayList;
import java.util.List;

public class TotalFragment extends Fragment implements OnGroupClickListener, OnChildClickListener, TipView.WhiteViewClick {
    private FragmentActivity context;
    private SearchPlayerHistoryDao dbDao;

    private TipView tipView;// 没有网络、没有数据、数据错误提示
    private View rootView;
    private Dialog dialog;
    private ExpandableListView expandListView;

    private ArrayList<RankInfo> playList;       // 节目 list
    private ArrayList<RankInfo> sequList;       // 专辑 list
    private ArrayList<RankInfo> radioList;      // radio
    private ArrayList<RankInfo> ttsList;        // tts
    private ArrayList<SuperRankInfo> list = new ArrayList<>();// 返回的节目list，拆分之前的list
    private List<RankInfo> subList;

    private String searchString;
    private String tag = "TOTAL_VOLLEY_REQUEST_CANCEL_TAG";
    private boolean isCancelRequest;

    @Override
    public void onWhiteViewClick() {
        dialog = DialogUtils.Dialogph(context, "通讯中");
        sendRequest();
    }

    @Override
    public boolean onGroupClick(ExpandableListView parent, View v, int groupPosition, long id) {
        SearchLikeActivity.updateViewPager(list.get(groupPosition).getKey());
        return true;
    }

    @Override
    public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id) {
        String mediaType = null;
        try {
            mediaType = list.get(groupPosition).getList().get(childPosition).getMediaType();
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (mediaType == null) return true;
        switch (mediaType) {
            case "RADIO":
            case "AUDIO":
                String playername = list.get(groupPosition).getList().get(childPosition).getContentName();
                String playerimage = list.get(groupPosition).getList().get(childPosition).getContentImg();
                String playerurl = list.get(groupPosition).getList().get(childPosition).getContentPlay();
                String playerurI = list.get(groupPosition).getList().get(childPosition).getContentURI();
                String playermediatype = list.get(groupPosition).getList().get(childPosition).getMediaType();
                String plaplayeralltime =list.get(groupPosition).getList().get(childPosition).getContentTimes() ;
                String playerintime = "0";
                String playercontentdesc = list.get(groupPosition).getList().get(childPosition).getContentDescn();
                String playernum = list.get(groupPosition).getList().get(childPosition).getPlayCount();
                String playerzantype =list.get(groupPosition).getList().get(childPosition).getContentFavorite();
                String playerfrom = list.get(groupPosition).getList().get(childPosition).getContentPub();
                String playerfromid = "";
                String playerfromurl = "";
                String playeraddtime = Long.toString(System.currentTimeMillis());
                String bjuserid = CommonUtils.getUserId(context);
                String playcontentshareurl = list.get(groupPosition).getList().get(childPosition).getContentShareURL();
                String ContentFavorite = list.get(groupPosition).getList().get(childPosition).getContentFavorite();
                String ContentId = list.get(groupPosition).getList().get(childPosition).getContentId();
                String localurl = list.get(groupPosition).getList().get(childPosition).getLocalurl();
                String sequname = list.get(groupPosition).getList().get(childPosition).getSequName();
                String sequid = list.get(groupPosition).getList().get(childPosition).getSequId();
                String sequdesc = list.get(groupPosition).getList().get(childPosition).getSequDesc();
                String sequimg = list.get(groupPosition).getList().get(childPosition).getSequImg();
                // 如果该数据已经存在数据库则删除原有数据，然后添加最新数据
                PlayerHistory history = new PlayerHistory(
                        playername, playerimage, playerurl, playerurI, playermediatype,
                        plaplayeralltime, playerintime, playercontentdesc, playernum,
                        playerzantype, playerfrom, playerfromid, playerfromurl, playeraddtime, bjuserid,
                        playcontentshareurl, ContentFavorite, ContentId, localurl, sequname, sequid, sequdesc, sequimg);
                dbDao.deleteHistory(playerurl);
                dbDao.addHistory(history);
                MainActivity.changeOne();
                Intent push=new Intent(BroadcastConstants.PLAY_TEXT_VOICE_SEARCH);
                Bundle bundle1=new Bundle();
                bundle1.putString("text",playername);
                push.putExtras(bundle1);
                context.sendBroadcast(push);
                break;
            case "SEQU":
                AlbumFragment fg= new AlbumFragment();
                Bundle bundle = new Bundle();
                bundle.putString("type", "search");
                bundle.putSerializable("list", list.get(groupPosition).getList().get(childPosition));
                fg.setArguments(bundle);
                PlayerActivity.open(fg);
                break;
        }
        return true;
    }

    // 初始化数据库
    private void initDao() {
        dbDao = new SearchPlayerHistoryDao(context);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = getActivity();
        IntentFilter mFilter = new IntentFilter();
        mFilter.addAction(BroadcastConstants.SEARCH_VIEW_UPDATE);
        context.registerReceiver(mBroadcastReceiver, mFilter);
        initDao();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (rootView == null) {
            rootView = inflater.inflate(R.layout.fragment_favorite_total, container, false);

            tipView = (TipView) rootView.findViewById(R.id.tip_view);
            tipView.setWhiteClick(this);

            expandListView = (ExpandableListView) rootView.findViewById(R.id.ex_listview);
            expandListView.setGroupIndicator(null);// 去除 indicator
            expandListView.setOnGroupClickListener(this);
            expandListView.setOnChildClickListener(this);
        }
        return rootView;
    }

    private void sendRequest() {
        if (GlobalConfig.CURRENT_NETWORK_STATE_TYPE == -1) {
            tipView.setVisibility(View.VISIBLE);
            tipView.setTipView(TipView.TipStatus.NO_NET);
            return ;
        }

        VolleyRequest.RequestPost(GlobalConfig.getSearchByText, tag, setParam(), new VolleyCallback() {
            private String ReturnType;

            @Override
            protected void requestSuccess(JSONObject result) {
                if (dialog != null) dialog.dismiss();
                if (isCancelRequest) return;
                expandListView.setVisibility(View.GONE);
                try {
                    ReturnType = result.getString("ReturnType");
                    L.v("ReturnType", "ReturnType -- > > " + ReturnType);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                if (ReturnType != null && ReturnType.equals("1001")) {
                    try {
                        JSONObject arg1 = (JSONObject) new JSONTokener(result.getString("ResultList")).nextValue();
                        subList = new Gson().fromJson(arg1.getString("List"), new TypeToken<List<RankInfo>>() {}.getType());
                        if (subList == null || subList.size() == 0) return;
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    list.clear();
                    if (playList != null) playList.clear();
                    if (sequList != null) sequList.clear();
                    if (radioList != null) radioList.clear();
                    if (ttsList != null) ttsList.clear();
                    for (int i = 0, size = subList.size(); i < size; i++) {
                        if (subList.get(i).getMediaType() != null && !subList.get(i).getMediaType().equals("")) {
                            if (subList.get(i).getMediaType().equals("AUDIO")) {
                                if (playList == null) playList = new ArrayList<>();
                                if (playList.size() < 3) playList.add(subList.get(i));
                            } else if (subList.get(i).getMediaType().equals("SEQU")) {
                                if (sequList == null) sequList = new ArrayList<>();
                                if (sequList.size() < 3) sequList.add(subList.get(i));
                            } else if (subList.get(i).getMediaType().equals("TTS")) {
                                if (ttsList == null) ttsList = new ArrayList<>();
                                if (ttsList.size() < 3) ttsList.add(subList.get(i));
                            } else if (subList.get(i).getMediaType().equals("RADIO")) {
                                if (radioList == null) radioList = new ArrayList<>();
                                if (radioList.size() < 3) radioList.add(subList.get(i));
                            }
                        }
                    }
                    if (playList != null && playList.size() != 0) {
                        SuperRankInfo mSuperRankInfo = new SuperRankInfo();
                        mSuperRankInfo.setKey(playList.get(0).getMediaType());
                        mSuperRankInfo.setList(playList);
                        list.add(mSuperRankInfo);
                    }
                    if (sequList != null && sequList.size() != 0) {
                        SuperRankInfo mSuperRankInfo1 = new SuperRankInfo();
                        mSuperRankInfo1.setKey(sequList.get(0).getMediaType());
                        mSuperRankInfo1.setList(sequList);
                        list.add(mSuperRankInfo1);
                    }
                    if (ttsList != null && ttsList.size() != 0) {
                        SuperRankInfo mSuperRankInfo1 = new SuperRankInfo();
                        mSuperRankInfo1.setKey(ttsList.get(0).getMediaType());
                        mSuperRankInfo1.setList(ttsList);
                        list.add(mSuperRankInfo1);
                    }
                    if (radioList != null && radioList.size() != 0) {
                        SuperRankInfo mSuperRankInfo1 = new SuperRankInfo();
                        mSuperRankInfo1.setKey(radioList.get(0).getMediaType());
                        mSuperRankInfo1.setList(radioList);
                        list.add(mSuperRankInfo1);
                    }
                    if (list.size() > 0) {
                        expandListView.setAdapter(new SearchContentAdapter(context, list));
                        expandListView.setVisibility(View.VISIBLE);
                        for (int i = 0; i < list.size(); i++) {
                            expandListView.expandGroup(i);
                        }
                        tipView.setVisibility(View.GONE);
                    } else {
                        tipView.setVisibility(View.VISIBLE);
                        tipView.setTipView(TipView.TipStatus.NO_DATA, "没有找到相关结果\n试试其他词，不要太逆天哟");
                    }
                } else {
                    tipView.setVisibility(View.VISIBLE);
                    tipView.setTipView(TipView.TipStatus.NO_DATA, "没有找到相关结果\n试试其他词，不要太逆天哟");
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

    private JSONObject setParam() {
        JSONObject jsonObject = VolleyRequest.getJsonObject(context);
        try {
            if (searchString != null && !searchString.equals("")) {
                jsonObject.put("PageSize", "12");
                jsonObject.put("SearchStr", searchString);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return jsonObject;
    }

    // 广播接收器
    private BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals(BroadcastConstants.SEARCH_VIEW_UPDATE)) {
                searchString = intent.getStringExtra("SearchStr");
                if (searchString != null && !searchString.equals("")) {
                    dialog = DialogUtils.Dialogph(context, "通讯中");
                    sendRequest();
                }
            }
        }
    };

    @Override
    public void onDestroyView() {
        super.onDestroy();
        if (null != rootView) {
            ((ViewGroup) rootView.getParent()).removeView(rootView);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        isCancelRequest = VolleyRequest.cancelRequest(tag);
        expandListView = null;
        dbDao = null;
        context.unregisterReceiver(mBroadcastReceiver);
        rootView = null;
        context = null;
        dialog = null;
        playList = null;
        sequList = null;
        ttsList = null;
        radioList = null;
        list = null;
        subList = null;
        searchString = null;
        tag = null;
    }
}
