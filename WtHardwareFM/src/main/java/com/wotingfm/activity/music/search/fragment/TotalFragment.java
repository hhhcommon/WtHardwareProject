package com.wotingfm.activity.music.search.fragment;

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
import com.wotingfm.activity.common.main.MainActivity;
import com.wotingfm.activity.music.main.HomeActivity;
import com.wotingfm.activity.music.main.dao.SearchPlayerHistoryDao;
import com.wotingfm.activity.music.player.fragment.PlayerFragment;
import com.wotingfm.activity.music.player.model.PlayerHistory;
import com.wotingfm.activity.music.program.album.activity.AlbumActivity;
import com.wotingfm.activity.music.program.fmlist.model.RankInfo;
import com.wotingfm.activity.music.search.activity.SearchLikeActivity;
import com.wotingfm.activity.music.search.adapter.SearchContentAdapter;
import com.wotingfm.activity.music.search.model.SuperRankInfo;
import com.wotingfm.common.config.GlobalConfig;
import com.wotingfm.common.constant.BroadcastConstant;
import com.wotingfm.common.volley.VolleyCallback;
import com.wotingfm.common.volley.VolleyRequest;
import com.wotingfm.util.CommonUtils;
import com.wotingfm.util.DialogUtils;
import com.wotingfm.util.ToastUtils;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.util.ArrayList;
import java.util.List;

public class TotalFragment extends Fragment implements OnGroupClickListener, OnChildClickListener {
    private FragmentActivity context;
    private SearchContentAdapter searchAdapter;
    private SearchPlayerHistoryDao dbDao;

    private View rootView;
    private Dialog dialog;
    private ExpandableListView expandListView;

    private ArrayList<RankInfo> playList;       // 节目list
    private ArrayList<RankInfo> sequList;       // 专辑list
    private ArrayList<RankInfo> ttsList;        // tts
    private ArrayList<RankInfo> radioList;      // radio
    private ArrayList<SuperRankInfo> list = new ArrayList<>();// 返回的节目list，拆分之前的list
    private List<RankInfo> subList;

    protected String searchString;
    private String tag = "TOTAL_VOLLEY_REQUEST_CANCEL_TAG";
    private boolean isCancelRequest;

    @Override
    public boolean onGroupClick(ExpandableListView parent, View v, int groupPosition, long id) {
        ((SearchLikeActivity)context).updateViewPager(list.get(groupPosition).getKey());
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
        if(mediaType == null) {
            return true;
        }
        switch (mediaType) {
            case "RADIO":
            case "AUDIO":
                String playername = list.get(groupPosition).getList().get(childPosition).getContentName();
                String playerimage = list.get(groupPosition).getList().get(childPosition).getContentImg();
                String playerurl = list.get(groupPosition).getList().get(childPosition).getContentPlay();
                String playerurI = list.get(groupPosition).getList().get(childPosition).getContentURI();
                String playermediatype = list.get(groupPosition).getList().get(childPosition).getMediaType();
                String plaplayeralltime = "0";
                String playerintime = "0";
                String playercontentdesc = list.get(groupPosition).getList().get(childPosition).getCurrentContent();
                String playernum = list.get(groupPosition).getList().get(childPosition).getWatchPlayerNum();
                String playerzantype = "0";
                String playerfrom = "";
                String playerfromid = "";
                String playerfromurl = "";
                String playeraddtime = Long.toString(System.currentTimeMillis());
                String bjuserid = CommonUtils.getUserId(context);
                String playcontentshareurl = list.get(groupPosition).getList().get(childPosition).getContentShareURL();
                String ContentFavorite = list.get(groupPosition).getList().get(childPosition).getContentFavorite();
                String ContentId = list.get(groupPosition).getList().get(childPosition).getContentId();
                String localurl = list.get(groupPosition).getList().get(childPosition).getLocalurl();
                String sequname =list.get(groupPosition).getList().get(childPosition).getSequName();
                String sequid =list.get(groupPosition).getList().get(childPosition).getSequId();
                String sequdesc =list.get(groupPosition).getList().get(childPosition).getSequDesc();
                String sequimg =list.get(groupPosition).getList().get(childPosition).getSequImg();
                //如果该数据已经存在数据库则删除原有数据，然后添加最新数据
                PlayerHistory history = new PlayerHistory(
                        playername,  playerimage, playerurl, playerurI,playermediatype,
                        plaplayeralltime, playerintime, playercontentdesc, playernum,
                        playerzantype,  playerfrom, playerfromid, playerfromurl,playeraddtime,bjuserid,
                        playcontentshareurl,ContentFavorite,ContentId,localurl,sequname,sequid,sequdesc,sequimg);
                dbDao.deleteHistory(playerurl);
                dbDao.addHistory(history);
                MainActivity.changeToMusic();
                HomeActivity.UpdateViewPager();
                PlayerFragment.SendTextRequest(list.get(groupPosition).getList().get(childPosition).getContentName(), context.getApplicationContext());
                context.finish();
                break;
            case "SEQU":
                Intent intent = new Intent(context, AlbumActivity.class);
                Bundle bundle = new Bundle();
                bundle.putString("type", "search");
                bundle.putSerializable("list", list.get(groupPosition).getList().get(childPosition));
                intent.putExtras(bundle);
                startActivity(intent);
                break;
            default:
                ToastUtils.show_short(context, "暂不支持的Type类型");
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
        mFilter.addAction(BroadcastConstant.SEARCH_VIEW_UPDATE);
        context.registerReceiver(mBroadcastReceiver, mFilter);
        initDao();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (rootView == null) {
            rootView = inflater.inflate(R.layout.fragment_favorite_total, container, false);
            expandListView = (ExpandableListView) rootView.findViewById(R.id.ex_listview);
            expandListView.setGroupIndicator(null);// 去除 indicator
            expandListView.setOnGroupClickListener(this);
            expandListView.setOnChildClickListener(this);
        }
        return rootView;
    }

    private void sendRequest() {
        VolleyRequest.RequestPost(GlobalConfig.getSearchByText, tag, setParam(), new VolleyCallback() {
            private String ReturnType;
            private String Message;

            @Override
            protected void requestSuccess(JSONObject result) {
                if (dialog != null) {
                    dialog.dismiss();
                }
                if (isCancelRequest) {
                    return;
                }
                expandListView.setVisibility(View.GONE);
                try {
                    ReturnType = result.getString("ReturnType");
                    Message = result.getString("Message");
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                if (ReturnType != null && ReturnType.equals("1001")) {
                    try {
                        JSONObject arg1 = (JSONObject) new JSONTokener(result.getString("ResultList")).nextValue();
                        subList = new Gson().fromJson(arg1.getString("List"), new TypeToken<List<RankInfo>>() {}.getType());
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    list.clear();
                    if (playList != null) {
                        playList.clear();
                    }
                    if (sequList != null) {
                        sequList.clear();
                    }
                    if (subList.size() >= 0) {
                        for (int i = 0; i < subList.size(); i++) {
                            if (subList.get(i).getMediaType() != null && !subList.get(i).getMediaType().equals("")) {
                                if (subList.get(i).getMediaType().equals("AUDIO")) {
                                    if (playList == null) {
                                        playList = new ArrayList<>();
                                        playList.add(subList.get(i));
                                    } else {
                                        if (playList.size() < 3) {
                                            playList.add(subList.get(i));
                                        }
                                    }
                                } else if (subList.get(i).getMediaType().equals("SEQU")) {
                                    if (sequList == null) {
                                        sequList = new ArrayList<>();
                                        sequList.add(subList.get(i));
                                    } else {
                                        if (sequList.size() < 3) {
                                            sequList.add(subList.get(i));
                                        }
                                    }
                                } else if (subList.get(i).getMediaType().equals("TTS")) {
                                    if (ttsList == null) {
                                        ttsList = new ArrayList<>();
                                        ttsList.add(subList.get(i));
                                    } else {
                                        if (ttsList.size() < 3) {
                                            ttsList.add(subList.get(i));
                                        }
                                    }
                                } else if (subList.get(i).getMediaType().equals("RADIO")) {
                                    if (radioList == null) {
                                        radioList = new ArrayList<>();
                                        radioList.add(subList.get(i));
                                    } else {
                                        if (radioList.size() < 3) {
                                            radioList.add(subList.get(i));
                                        }

                                    }
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
                        if (list.size() != 0) {
                            searchAdapter = new SearchContentAdapter(context, list);
                            expandListView.setAdapter(searchAdapter);
                            for (int i = 0; i < list.size(); i++) {
                                expandListView.expandGroup(i);
                            }
                            expandListView.setVisibility(View.VISIBLE);
                        } else {
                            ToastUtils.show_short(context, "没有数据");
                        }
                    } else {
                        ToastUtils.show_short(context, "数据获取异常");
                    }
                } else if (ReturnType != null && ReturnType.equals("1002")) {
                    ToastUtils.show_always(context, "" + Message);
                } else if (ReturnType != null && ReturnType.equals("1011")) {
                    ToastUtils.show_always(context, "" + Message);
                    expandListView.setVisibility(View.GONE);
                } else {
                    if (Message != null && !Message.trim().equals("")) {
                        ToastUtils.show_always(context, Message + "");
                    }
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

    private JSONObject setParam() {
        JSONObject jsonObject = VolleyRequest.getJsonObject(context);
        try {
            jsonObject.put("PageSize", "12");
            if (searchString != null && !searchString.equals("")) {
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
            if (action.equals(BroadcastConstant.SEARCH_VIEW_UPDATE)) {
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
        searchAdapter = null;
        searchString = null;
        tag = null;
    }
}
