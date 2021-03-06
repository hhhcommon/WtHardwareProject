package com.wotingfm.ui.music.program.diantai.main;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ExpandableListView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.android.volley.VolleyError;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.wotingfm.R;
import com.wotingfm.common.config.GlobalConfig;
import com.wotingfm.common.constant.BroadcastConstants;
import com.wotingfm.common.constant.StringConstant;
import com.wotingfm.common.volley.VolleyCallback;
import com.wotingfm.common.volley.VolleyRequest;
import com.wotingfm.ui.main.MainActivity;
import com.wotingfm.ui.music.main.ProgramActivity;
import com.wotingfm.ui.music.main.dao.SearchPlayerHistoryDao;
import com.wotingfm.ui.music.player.model.PlayerHistory;
import com.wotingfm.ui.music.program.diantai.main.adapter.OnLinesRadioAdapter;
import com.wotingfm.ui.music.program.diantai.model.RadioPlay;
import com.wotingfm.ui.music.program.fmlist.adapter.RankInfoAdapter;
import com.wotingfm.ui.music.program.fmlist.model.RankInfo;
import com.wotingfm.util.CommonUtils;
import com.wotingfm.util.DialogUtils;
import com.wotingfm.util.ToastUtils;
import com.wotingfm.widget.TipView;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.util.ArrayList;
import java.util.List;

/**
 * 城市电台
 */
public class CityRadioFragment extends Fragment implements View.OnClickListener, TipView.WhiteViewClick {
    private SearchPlayerHistoryDao dbDao;
    private List<RadioPlay> newList = new ArrayList<>();
    private List<RadioPlay> SubList;
    private List<RankInfo> SubListList;
    private RankInfoAdapter adapterList;
    private OnLinesRadioAdapter adapter;

    private TipView tipView;// 没有网络、没有数据、加载错误提示
    private Dialog dialog;
    private ImageView head_left_btn;
    private TextView mTextView_Head;
    private ListView mlistView_main;
    private ExpandableListView mListView;

    private String CatalogName;
    private String CatalogId;
    private String tag = "RADIO_CITY_VOLLEY_REQUEST_CANCEL_TAG";
    private boolean isCancelRequest;
    private FragmentActivity context;
    private View rootView;

    @Override
    public void onWhiteViewClick() {
        if (GlobalConfig.CURRENT_NETWORK_STATE_TYPE != -1 && CatalogId != null) {
            dialog = DialogUtils.Dialogph(context, "正在获取数据");
            if (CatalogId.equals("810000") || CatalogId.equals("710000") || CatalogId.equals("820000")) {
                sendTwice();// 处理那几个特殊的崩溃省市
                mListView.setVisibility(View.GONE);
                mlistView_main.setVisibility(View.VISIBLE);
            } else {
                send();
            }
        } else {
            tipView.setVisibility(View.VISIBLE);
            tipView.setTipView(TipView.TipStatus.NO_NET);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (rootView == null) {
            rootView = inflater.inflate(R.layout.activity_radio_nation, container, false);
            rootView.setOnClickListener(this);
            context = getActivity();
            setView();
            handleIntent();
            setListener();
            initDao();
        }
        return rootView;
    }

    private void handleIntent() {
        String type = getArguments().getString("fromtype");
        if (type != null && type.trim().equals("city")) {
            CatalogName = getArguments().getString("name");
            CatalogId = getArguments().getString("id");
        }
        if (!TextUtils.isEmpty(CatalogName)) {
            mTextView_Head.setText(CatalogName);
        }
        if (GlobalConfig.CURRENT_NETWORK_STATE_TYPE != -1 && CatalogId != null) {
            dialog = DialogUtils.Dialogph(context, "正在获取数据");
            if (CatalogId.equals("810000") || CatalogId.equals("710000") || CatalogId.equals("820000")) {
                sendTwice();// 处理那几个特殊的崩溃省市
                mListView.setVisibility(View.GONE);
                mlistView_main.setVisibility(View.VISIBLE);
            } else {
                send();
            }
        } else {
            tipView.setVisibility(View.VISIBLE);
            tipView.setTipView(TipView.TipStatus.NO_NET);
        }
    }

    private void setView() {
        tipView = (TipView) rootView.findViewById(R.id.tip_view);
        tipView.setWhiteClick(this);

        mListView = (ExpandableListView) rootView.findViewById(R.id.listview_fm);
        mlistView_main = (ListView) rootView.findViewById(R.id.listview_lv);
        head_left_btn = (ImageView) rootView.findViewById(R.id.head_left_btn);
        mTextView_Head = (TextView) rootView.findViewById(R.id.head_name_tv);
        mListView.setOnGroupClickListener(new ExpandableListView.OnGroupClickListener() {
            @Override
            public boolean onGroupClick(ExpandableListView parent, View v, int groupPosition, long id) {
                return true;
            }
        });
        mListView.setSelector(new ColorDrawable(Color.TRANSPARENT));
        mListView.setGroupIndicator(null);
    }

    private void setListener() {
        head_left_btn.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.head_left_btn:
                ProgramActivity.close();
                break;
        }
    }

    private void send() {
        VolleyRequest.RequestPost(GlobalConfig.getContentUrl, tag, setParamSend(), new VolleyCallback() {
            private String ResultList;
            private String StringSubList;
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
                        ResultList = result.getString("ResultList");
                        JSONTokener jsonParser = new JSONTokener(ResultList);
                        JSONObject arg1 = (JSONObject) jsonParser.nextValue();
                        try {
                            StringSubList = arg1.getString("List");
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        try {
                            SubList = new Gson().fromJson(StringSubList, new TypeToken<List<RadioPlay>>() {
                            }.getType());
                            if (SubList == null || SubList.size() == 0) {
                                mListView.setVisibility(View.GONE);
                                mlistView_main.setVisibility(View.VISIBLE);
                                sendTwice();
                            } else {
                                if (adapter == null) {
                                    mListView.setAdapter(adapter = new OnLinesRadioAdapter(context, SubList));
                                } else {
                                    adapter.notifyDataSetChanged();
                                }
                                for (int i = 0; i < SubList.size(); i++) {
                                    mListView.expandGroup(i);
                                }
                            }
                            setListViewExpand();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        tipView.setVisibility(View.VISIBLE);
                        tipView.setTipView(TipView.TipStatus.IS_ERROR);
                    }
                } else {
                    tipView.setVisibility(View.VISIBLE);
                    tipView.setTipView(TipView.TipStatus.NO_DATA);
                }
            }

            @Override
            protected void requestError(VolleyError error) {
                ToastUtils.showVolleyError(context);
                if (dialog != null) dialog.dismiss();
                tipView.setVisibility(View.VISIBLE);
                tipView.setTipView(TipView.TipStatus.IS_ERROR);
            }
        });
    }

    private void setListViewExpand() {
        mListView.setOnChildClickListener(new ExpandableListView.OnChildClickListener() {
            @Override
            public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id) {
                if (SubList != null && SubList.get(groupPosition).getList().get(childPosition) != null
                        && SubList.get(groupPosition).getList().get(childPosition).getMediaType() != null) {
                    String MediaType = SubList.get(groupPosition).getList().get(childPosition).getMediaType();
                    if (MediaType.equals(StringConstant.TYPE_RADIO) || MediaType.equals(StringConstant.TYPE_AUDIO)) {
                        String playName = SubList.get(groupPosition).getList().get(childPosition).getContentName();
                        String playImage = SubList.get(groupPosition).getList().get(childPosition).getContentImg();
                        String playUrl = SubList.get(groupPosition).getList().get(childPosition).getContentPlay();
                        String playUri = SubList.get(groupPosition).getList().get(childPosition).getContentURI();
                        String playMediaType = SubList.get(groupPosition).getList().get(childPosition).getMediaType();
                        String playContentShareUrl = SubList.get(groupPosition).getList().get(childPosition).getContentShareURL();
                        String playAllTime = SubList.get(groupPosition).getList().get(childPosition).getContentTimes();
                        String playInTime = "0";
                        String playContentDesc = SubList.get(groupPosition).getList().get(childPosition).getContentDescn();
                        String playerNum = SubList.get(groupPosition).getList().get(childPosition).getPlayCount();
                        String playZanType = "0";
                        String playFrom = SubList.get(groupPosition).getList().get(childPosition).getContentPub();
                        String playFromId = "";
                        String playFromUrl = "";
                        String playAddTime = Long.toString(System.currentTimeMillis());
                        String bjUserId = CommonUtils.getUserId(context);
                        String ContentFavorite = SubList.get(groupPosition).getList().get(childPosition).getContentFavorite();
                        String ContentId = SubList.get(groupPosition).getList().get(childPosition).getContentId();
                        String localUrl = SubList.get(groupPosition).getList().get(childPosition).getLocalurl();

                        String sequName = SubList.get(groupPosition).getList().get(childPosition).getSequName();
                        String sequId = SubList.get(groupPosition).getList().get(childPosition).getSequId();
                        String sequDesc = SubList.get(groupPosition).getList().get(childPosition).getSequDesc();
                        String sequImg = SubList.get(groupPosition).getList().get(childPosition).getSequImg();

                        // 如果该数据已经存在数据库则删除原有数据，然后添加最新数据
                        PlayerHistory history = new PlayerHistory(
                                playName, playImage, playUrl, playUri, playMediaType,
                                playAllTime, playInTime, playContentDesc, playerNum,
                                playZanType, playFrom, playFromId, playFromUrl, playAddTime, bjUserId, playContentShareUrl,
                                ContentFavorite, ContentId, localUrl, sequName, sequId, sequDesc, sequImg);
                        dbDao.deleteHistory(playUrl);
                        dbDao.addHistory(history);

                        Intent push = new Intent(BroadcastConstants.PLAY_TEXT_VOICE_SEARCH);
                        Bundle bundle1 = new Bundle();
                        bundle1.putString(StringConstant.TEXT_CONTENT, SubList.get(groupPosition).getList().get(childPosition).getContentName());
                        push.putExtras(bundle1);
                        context.sendBroadcast(push);
                        MainActivity.changeOne();
                    } else {
                        ToastUtils.show_short(context, "暂不支持的Type类型");
                    }
                }
                return false;
            }
        });
    }

    private void sendTwice() {
        VolleyRequest.RequestPost(GlobalConfig.getContentUrl, tag, setParam(), new VolleyCallback() {
            private String ResultList;
            private String StringSubList;
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
                        ResultList = result.getString("ResultList");
                        JSONTokener jsonParser = new JSONTokener(ResultList);
                        JSONObject arg1 = (JSONObject) jsonParser.nextValue();
                        try {
                            StringSubList = arg1.getString("List");
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        try {
                            SubListList = new Gson().fromJson(StringSubList, new TypeToken<List<RankInfo>>() {
                            }.getType());
                            if (adapterList == null) {
                                mlistView_main.setAdapter(adapterList = new RankInfoAdapter(context, SubListList));
                            } else {
                                adapter.notifyDataSetChanged();
                            }
                            setListView();
                        } catch (Exception e) {
                            e.printStackTrace();
                            tipView.setVisibility(View.VISIBLE);
                            tipView.setTipView(TipView.TipStatus.IS_ERROR);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        tipView.setVisibility(View.VISIBLE);
                        tipView.setTipView(TipView.TipStatus.IS_ERROR);
                    }
                } else {
                    tipView.setVisibility(View.VISIBLE);
                    tipView.setTipView(TipView.TipStatus.NO_DATA);
                }
            }

            @Override
            protected void requestError(VolleyError error) {
                if (dialog != null) dialog.dismiss();
                tipView.setVisibility(View.VISIBLE);
                tipView.setTipView(TipView.TipStatus.IS_ERROR);
            }
        });
    }

    private JSONObject setParam() {
        JSONObject jsonObject = VolleyRequest.getJsonObject(context);
        try {
            jsonObject.put("MediaType", "RADIO");
            jsonObject.put("CatalogId", CatalogId);
            jsonObject.put("CatalogType", "2");
            jsonObject.put("ResultType", "3");
            jsonObject.put("PageSize", "20");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return jsonObject;
    }

    private JSONObject setParamSend() {
        JSONObject jsonObject = VolleyRequest.getJsonObject(context);
        try {
            jsonObject.put("MediaType", "RADIO");
            jsonObject.put("CatalogId", CatalogId);
            jsonObject.put("CatalogType", "2");
            jsonObject.put("PerSize", "3");
            jsonObject.put("ResultType", "1");
            jsonObject.put("PageSize", "50");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return jsonObject;
    }

    private void initDao() {// 初始化数据库命令执行对象
        dbDao = new SearchPlayerHistoryDao(context);
    }

    // 这里要改
    protected void setListView() {
        mlistView_main.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (SubListList != null && SubListList.get(position) != null && SubListList.get(position).getMediaType() != null) {
                    String MediaType = SubListList.get(position).getMediaType();
                    if (MediaType.equals("RADIO") || MediaType.equals("AUDIO")) {
                        String playername = SubListList.get(position).getContentName();
                        String playerimage = SubListList.get(position).getContentImg();
                        String playerurl = SubListList.get(position).getContentPlay();
                        String playerurI = SubListList.get(position).getContentURI();
                        String playcontentshareurl = SubListList.get(position).getContentShareURL();
                        String playermediatype = SubListList.get(position).getMediaType();
                        String plaplayeralltime = "0";
                        String playerintime = "0";
                        String playercontentdesc = SubListList.get(position).getCurrentContent();
                        String playernum = SubListList.get(position).getPlayCount();
                        String playerzantype = "0";
                        String playerfrom = SubListList.get(position).getContentPub();
                        String playerfromid = "";
                        String playerfromurl = "";
                        String playeraddtime = Long.toString(System.currentTimeMillis());
                        String bjuserid = CommonUtils.getUserId(context);
                        String ContentFavorite = SubListList.get(position).getContentFavorite();
                        String ContentId = SubListList.get(position).getContentId();
                        String localurl = SubListList.get(position).getLocalurl();
                        String sequName = SubListList.get(position).getSequName();
                        String sequId = SubListList.get(position).getSequId();
                        String sequDesc = SubListList.get(position).getSequDesc();
                        String sequImg = SubListList.get(position).getSequImg();

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
                        bundle1.putString("text", SubListList.get(position).getContentName());
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
        head_left_btn = null;
        mListView = null;
        dialog = null;
        mTextView_Head = null;
        if (dbDao != null) {
            dbDao.closedb();
            dbDao = null;
        }
        newList.clear();
        newList = null;
        if (SubList != null) {
            SubList.clear();
            SubList = null;
        }
        adapter = null;
    }
}