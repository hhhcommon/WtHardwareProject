package com.wotingfm.activity.music.program.diantai.fragment;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ExpandableListView;
import android.widget.ExpandableListView.OnChildClickListener;
import android.widget.ExpandableListView.OnGroupClickListener;
import android.widget.GridView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.android.volley.VolleyError;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.wotingfm.R;
import com.wotingfm.activity.music.main.HomeActivity;
import com.wotingfm.activity.music.main.dao.SearchPlayerHistoryDao;
import com.wotingfm.activity.music.player.fragment.PlayerFragment;
import com.wotingfm.activity.music.player.model.PlayerHistory;
import com.wotingfm.activity.music.program.album.activity.AlbumActivity;
import com.wotingfm.activity.music.program.citylist.activity.CityListActivity;
import com.wotingfm.activity.music.program.diantai.adapter.citynewsadapter;
import com.wotingfm.activity.music.program.diantai.adapter.onlineAdapter;
import com.wotingfm.activity.music.program.diantai.model.RadioPlay;
import com.wotingfm.activity.music.program.fmlist.activity.FMListActivity;
import com.wotingfm.activity.music.program.fmlist.model.RankInfo;
import com.wotingfm.common.application.BSApplication;
import com.wotingfm.common.config.GlobalConfig;
import com.wotingfm.common.constant.BroadcastConstant;
import com.wotingfm.common.constant.StringConstant;
import com.wotingfm.common.volley.VolleyCallback;
import com.wotingfm.common.volley.VolleyRequest;
import com.wotingfm.util.CommonUtils;
import com.wotingfm.util.L;
import com.wotingfm.util.ToastUtils;
import com.wotingfm.widget.pulltorefresh.PullToRefreshLayout;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.util.ArrayList;
import java.util.List;

/**
 * 电台主页
 *
 * @author 辛龙 2016年2月26日
 */
public class OnLineFragment extends Fragment implements OnClickListener {
    private FragmentActivity context;
    private MessageReceiver receiver;
    private PullToRefreshLayout mPullToRefreshLayout;
    private SearchPlayerHistoryDao dbDao;
    private onlineAdapter adapter;

    private View rootView;
    private ExpandableListView listViewMain;
    private GridView gridView;
    private TextView textName;

    private List<RadioPlay> mainList;
    private List<RankInfo> mainListS;
    private List<RadioPlay> newList = new ArrayList<>();

    private int refreshType;    // refreshType 1为下拉加载 2为上拉加载更多
    private int page = 1;       // 数的问题
    private String beginCatalogId;
    private String returnType;
    private String cityId;
    private String cityName;
    private String tag = "ONLINE_VOLLEY_REQUEST_CANCEL_TAG";
    private boolean isCancelRequest;

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.lin_country:// 国家台
                String cityIdCountry = BSApplication.SharedPreferences.getString(StringConstant.CITYID, "110000");
//                String cityNameCountry = BSApplication.SharedPreferences.getString(StringConstant.CITYNAME, "北京");
                Intent intentCountry = new Intent(context, FMListActivity.class);
                Bundle bundleCountry = new Bundle();
                bundleCountry.putString("fromtype", "online");
                bundleCountry.putString("name", "国家台");
                bundleCountry.putString("type", "2");
                bundleCountry.putString("id", cityIdCountry);
                intentCountry.putExtras(bundleCountry);
                startActivity(intentCountry);
                break;
            case R.id.lin_province:// 省市台
                startActivityForResult(new Intent(context, CityListActivity.class), 0);
                break;
            case R.id.lin_internet:// 网络台
                String cityIdInternet = BSApplication.SharedPreferences.getString(StringConstant.CITYID, "110000");
//                String cityNameInternet = BSApplication.SharedPreferences.getString(StringConstant.CITYNAME, "北京");
                Intent intentInternet = new Intent(context, FMListActivity.class);
                Bundle bundleInternet = new Bundle();
                bundleInternet.putString("fromtype", "online");
                bundleInternet.putString("name", "网络台");
                bundleInternet.putString("type", "2");
                bundleInternet.putString("id", cityIdInternet);
                intentInternet.putExtras(bundleInternet);
                startActivity(intentInternet);
                break;
            case R.id.lin_head_more:// 更多
                String cityId = BSApplication.SharedPreferences.getString(StringConstant.CITYID, "110000");
                String cityName = BSApplication.SharedPreferences.getString(StringConstant.CITYNAME, "北京");
                Intent intent = new Intent(context, FMListActivity.class);
                Bundle bundle = new Bundle();
                bundle.putString("fromtype", "online");
                bundle.putString("name", cityName);
                bundle.putString("type", "2");
                bundle.putString("id", cityId);
                intent.putExtras(bundle);
                startActivity(intent);
                break;
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = getActivity();
        initDao();// 初始化数据库命令执行对象
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (rootView == null) {
            rootView = inflater.inflate(R.layout.fragment_radio, container, false);
            listViewMain = (ExpandableListView) rootView.findViewById(R.id.listView_main);
            View headView = LayoutInflater.from(context).inflate(R.layout.head_online, null);
            textName = (TextView) headView.findViewById(R.id.tv_name);
            gridView = (GridView) headView.findViewById(R.id.gridView);

            headView.findViewById(R.id.lin_head_more).setOnClickListener(this);
            headView.findViewById(R.id.lin_country).setOnClickListener(this);// 国家台
            headView.findViewById(R.id.lin_province).setOnClickListener(this);// 省市台
            headView.findViewById(R.id.lin_internet).setOnClickListener(this);// 网络台

            // 取消默认selector
            gridView.setSelector(new ColorDrawable(Color.TRANSPARENT));
            mPullToRefreshLayout = (PullToRefreshLayout) rootView.findViewById(R.id.refresh_view);
            setView();
            listViewMain.addHeaderView(headView);
            if (receiver == null) {
                receiver = new MessageReceiver();
                IntentFilter filter = new IntentFilter();
                filter.addAction(BroadcastConstant.CITY_CHANGE);
                context.registerReceiver(receiver, filter);
            }
        }
        return rootView;
    }

    class MessageReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals(BroadcastConstant.CITY_CHANGE)) {
                if (GlobalConfig.CityName != null) {
                    cityName = GlobalConfig.CityName;
                }
                textName.setText(cityName);
                page = 1;
                beginCatalogId = "";
                refreshType = 1;
                getCity();
                send();
                Editor et = BSApplication.SharedPreferences.edit();
                et.putString(StringConstant.CITYTYPE, "false");
                if(!et.commit()) {
                    L.w("数据 commit 失败!");
                }
            }
        }

    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        // 发送网络请求
        if (GlobalConfig.CURRENT_NETWORK_STATE_TYPE != -1) {
            refreshType = 1;
            beginCatalogId = "";
            String cityName = BSApplication.SharedPreferences.getString(StringConstant.CITYNAME, "北京");
            textName.setText(cityName);
            getCity();
            send();
        } else {
//			listView_main.stopLoadMore();
          /*  mPullToRefreshLayout.loadmoreFinish(PullToRefreshLayout.FAIL);*/
            ToastUtils.show_short(context, "网络失败，请检查网络");
        }
    }

    // 初始化数据库命令执行对象
    private void initDao() {
        dbDao = new SearchPlayerHistoryDao(context);
    }

    private void setView() {
        listViewMain.setOnGroupClickListener(new OnGroupClickListener() {
            @Override
            public boolean onGroupClick(ExpandableListView parent, View v, int groupPosition, long id) {
                return true;
            }
        });

        listViewMain.setGroupIndicator(null);
        mPullToRefreshLayout.setOnRefreshListener(new PullToRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh(PullToRefreshLayout pullToRefreshLayout) {
                if (GlobalConfig.CURRENT_NETWORK_STATE_TYPE != -1) {
                    refreshType = 1;
                    page = 1;
                    beginCatalogId = "";
                    send();
                } else {
                    ToastUtils.show_short(context, "网络失败，请检查网络");
                }
            }

            @Override
            public void onLoadMore(PullToRefreshLayout pullToRefreshLayout) {
                if (GlobalConfig.CURRENT_NETWORK_STATE_TYPE != -1) {
                    refreshType = 2;
                    send();
                    ToastUtils.show_short(context, "正在请求" + page + "页信息");
                } else {
                    ToastUtils.show_short(context, "网络失败，请检查网络");
                }
            }

        });
        listViewMain.setSelector(new ColorDrawable(Color.TRANSPARENT));
    }

    private void getCity() {
        // 此处在 splashActivity 中 refreshB 设置成 true
        cityId = BSApplication.SharedPreferences.getString(StringConstant.CITYID, "110000");
        if (GlobalConfig.AdCode != null && !GlobalConfig.AdCode.equals("")) {
            cityId = GlobalConfig.AdCode;
        }
        JSONObject jsonObject = VolleyRequest.getJsonObject(context);
        try {
//            jsonObject.put("UserId", CommonUtils.getUserId(context));
            jsonObject.put("MediaType", "RADIO");
            jsonObject.put("CatalogType", "2");
            jsonObject.put("CatalogId", cityId);
            jsonObject.put("Page", "1");
            jsonObject.put("PerSize", "3");
            jsonObject.put("ResultType", "3");
            jsonObject.put("PageSize", "3");
        } catch (JSONException e) {
            e.printStackTrace();
        }

        VolleyRequest.RequestPost(GlobalConfig.getContentUrl, tag, jsonObject, new VolleyCallback() {
            private citynewsadapter adapters;

            @Override
            protected void requestSuccess(JSONObject result) {
                if (isCancelRequest) {
                    return;
                }
                try {
                    returnType = result.getString("ReturnType");
                    if (returnType != null && returnType.equals("1001")) {
                        String ResultList = result.getString("ResultList");
                        JSONTokener jsonParser = new JSONTokener(ResultList);
                        JSONObject arg1 = (JSONObject) jsonParser.nextValue();
                        String MainList = arg1.getString("List");
                        mainListS = new Gson().fromJson(MainList, new TypeToken<List<RankInfo>>() {}.getType());
                        if (adapters == null) {
                            gridView.setAdapter(adapters = new citynewsadapter(context, mainListS));
                        } else {
                            adapters.notifyDataSetChanged();
                        }
                        gridListener();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            protected void requestError(VolleyError error) {
                ToastUtils.showVolleyError(context);
            }
        });
    }

    protected void gridListener() {
        gridView.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (mainListS != null && mainListS.get(position) != null && mainListS.get(position).getMediaType() != null) {
                    String MediaType = mainListS.get(position).getMediaType();
                    if (MediaType.equals("RADIO") || MediaType.equals("AUDIO")) {
                        String playername = mainListS.get(position).getContentName();
                        String playerimage = mainListS.get(position).getContentImg();
                        String playerurl = mainListS.get(position).getContentPlay();
                        String playerurI = mainListS.get(position).getContentURI();
                        String playermediatype = mainListS.get(position).getMediaType();
                        String playcontentshareurl = mainListS.get(position).getContentShareURL();
                        String plaplayeralltime = "0";
                        String playerintime = "0";
                        String playercontentdesc = mainListS.get(position).getCurrentContent();
                        String playernum = mainListS.get(position).getWatchPlayerNum();
                        String playerzantype = "0";
                        String playerfrom = "";
                        String playerfromid = "";
                        String playerfromurl = "";
                        String playeraddtime = Long.toString(System.currentTimeMillis());
                        String bjuserid = CommonUtils.getUserId(context);
                        String ContentFavorite = mainListS.get(position).getContentFavorite();
                        String ContentId = mainListS.get(position).getContentId();
                        String localurl = mainListS.get(position).getLocalurl();

                        //如果该数据已经存在数据库则删除原有数据，然后添加最新数据
                        PlayerHistory history = new PlayerHistory(
                                playername, playerimage, playerurl, playerurI, playermediatype,
                                plaplayeralltime, playerintime, playercontentdesc, playernum,
                                playerzantype, playerfrom, playerfromid, playerfromurl, playeraddtime, bjuserid, playcontentshareurl, ContentFavorite, ContentId, localurl);
                        dbDao.deleteHistory(playerurl);
                        dbDao.addHistory(history);
                        PlayerFragment.SendTextRequest(mainListS.get(position).getContentName(), context);
                        HomeActivity.UpdateViewPager();
                    }
                }
            }
        });
    }

    private void send() {
        JSONObject jsonObject = VolleyRequest.getJsonObject(context);
        try {
            jsonObject.put("MediaType", "RADIO");
            jsonObject.put("CatalogType", "1");// 按地区分类
            JSONObject js = new JSONObject();
            js.put("CatalogType", "2");
            js.put("CatalogId", cityId);
            jsonObject.put("FilterData", js);
            jsonObject.put("BeginCatalogId", beginCatalogId);
            jsonObject.put("Page", String.valueOf(page));
            jsonObject.put("PerSize", "3");
            jsonObject.put("ResultType", "1");
            jsonObject.put("PageSize", "10");
        } catch (JSONException e) {
            e.printStackTrace();
        }

        VolleyRequest.RequestPost(GlobalConfig.getContentUrl, tag, jsonObject, new VolleyCallback() {

            @Override
            protected void requestSuccess(JSONObject result) {
                if (isCancelRequest) {
                    return;
                }
                page++;
                try {
                    returnType = result.getString("ReturnType");
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                try {
                    if (returnType != null && returnType.equals("1001")) {
                        JSONObject arg1 = (JSONObject) new JSONTokener(result.getString("ResultList")).nextValue();
                        beginCatalogId = arg1.getString("BeginCatalogId");
                        mainList = new Gson().fromJson(arg1.getString("List"), new TypeToken<List<RadioPlay>>() {}.getType());
                        if (refreshType == 1) {
                            mPullToRefreshLayout.refreshFinish(PullToRefreshLayout.SUCCEED);
                            newList.clear();
                            newList.addAll(mainList);
                            if (adapter == null) {
                                listViewMain.setAdapter(adapter = new onlineAdapter(context, newList));
                            } else {
                                adapter.notifyDataSetChanged();
                            }
                        } else if (refreshType == 2) {
                            mPullToRefreshLayout.loadmoreFinish(PullToRefreshLayout.SUCCEED);
                            newList.addAll(mainList);
                            adapter.notifyDataSetChanged();
                        }
                        for (int i = 0; i < newList.size(); i++) {
                            listViewMain.expandGroup(i);
                        }
                        setItemListener();
                    } else {
                        ToastUtils.show_allways(context, "暂无数据");
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            protected void requestError(VolleyError error) {
                ToastUtils.showVolleyError(context);
            }
        });
    }

    // 初始一号位置为0,0
    protected void setItemListener() {
        listViewMain.setOnChildClickListener(new OnChildClickListener() {
            @Override
            public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id) {
                if (newList != null && newList.get(groupPosition).getList().get(childPosition) != null
                        && newList.get(groupPosition).getList().get(childPosition).getMediaType() != null) {
                    String MediaType = newList.get(groupPosition).getList().get(childPosition).getMediaType();
                    if (MediaType.equals("RADIO") || MediaType.equals("AUDIO")) {
                        String playername = newList.get(groupPosition).getList().get(childPosition).getContentName();
                        String playerimage = newList.get(groupPosition).getList().get(childPosition).getContentImg();
                        String playerurl = newList.get(groupPosition).getList().get(childPosition).getContentPlay();
                        String playerurI = newList.get(groupPosition).getList().get(childPosition).getContentURI();
                        String playermediatype = newList.get(groupPosition).getList().get(childPosition).getMediaType();
                        String playcontentshareurl = newList.get(groupPosition).getList().get(childPosition).getContentShareURL();
                        String plaplayeralltime = "0";
                        String playerintime = "0";
                        String playercontentdesc = newList.get(groupPosition).getList().get(childPosition).getCurrentContent();
                        String playernum = newList.get(groupPosition).getList().get(childPosition).getWatchPlayerNum();
                        String playerzantype = "0";
                        String playerfrom = "";
                        String playerfromid = "";
                        String playerfromurl = "";
                        String playeraddtime = Long.toString(System.currentTimeMillis());
                        String bjuserid = CommonUtils.getUserId(context);
                        String ContentFavorite = newList.get(groupPosition).getList().get(childPosition).getContentFavorite();
                        String ContentId = newList.get(groupPosition).getList().get(childPosition).getContentId();
                        String localurl = newList.get(groupPosition).getList().get(childPosition).getLocalurl();

                        //如果该数据已经存在数据库则删除原有数据，然后添加最新数据
                        PlayerHistory history = new PlayerHistory(
                                playername, playerimage, playerurl, playerurI, playermediatype,
                                plaplayeralltime, playerintime, playercontentdesc, playernum,
                                playerzantype, playerfrom, playerfromid, playerfromurl, playeraddtime, bjuserid, playcontentshareurl, ContentFavorite, ContentId, localurl);
                        dbDao.deleteHistory(playerurl);
                        dbDao.addHistory(history);
                        HomeActivity.UpdateViewPager();
						PlayerFragment.SendTextRequest(newList.get(groupPosition).getList().get(childPosition).getContentName(), context);
                    } else if (MediaType.equals("SEQU")) {
                        Intent intent = new Intent(context, AlbumActivity.class);
                        Bundle bundle = new Bundle();
                        bundle.putString("type", "player");
                        bundle.putString("conentname", newList.get(groupPosition).getList().get(childPosition).getContentName());
                        bundle.putString("conentdesc", newList.get(groupPosition).getList().get(childPosition).getContentDesc());
                        bundle.putString("conentid", newList.get(groupPosition).getList().get(childPosition).getContentId());
                        bundle.putString("contentimg", newList.get(groupPosition).getList().get(childPosition).getContentImg());
                        intent.putExtras(bundle);
                        startActivity(intent);
                    } else {
                        ToastUtils.show_short(context, "暂不支持的Type类型");
                    }
                }
                return false;
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        String cityType = BSApplication.SharedPreferences.getString(StringConstant.CITYTYPE, "false");
        cityName = BSApplication.SharedPreferences.getString(StringConstant.CITYNAME, "北京");
        cityId = BSApplication.SharedPreferences.getString(StringConstant.CITYID, "110000");
        if (GlobalConfig.CityName != null) {
            cityName = GlobalConfig.CityName;
        }
        if (cityType.equals("true")) {
            textName.setText(cityName);
            page = 1;
            beginCatalogId = "";
            refreshType = 1;
            getCity();
            send();
            Editor et = BSApplication.SharedPreferences.edit();
            et.putString(StringConstant.CITYTYPE, "false");
            if(!et.commit()) {
                L.w("数据 commit 失败!");
            }
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (null != rootView) {
            ((ViewGroup) rootView.getParent()).removeView(rootView);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        isCancelRequest = VolleyRequest.cancelRequest(tag);
        context.unregisterReceiver(receiver);
    }
}
