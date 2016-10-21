package com.wotingfm.activity.music.program.diantai.fragment;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
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
import android.widget.TextView;

import com.android.volley.VolleyError;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.wotingfm.R;
import com.wotingfm.activity.music.main.HomeActivity;
import com.wotingfm.activity.music.main.dao.SearchPlayerHistoryDao;
import com.wotingfm.activity.music.player.fragment.PlayerFragment;
import com.wotingfm.activity.music.player.model.PlayerHistory;
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

    private int refreshType = 1;    // refreshType 1为下拉加载 2为上拉加载更多
    private int page = 1;           // 数的问题
    private String beginCatalogId = "";
    private String returnType;
    private String cityId;
    private String cityName;
    private String tag = "ONLINE_VOLLEY_REQUEST_CANCEL_TAG";
    private boolean isCancelRequest;

    // 初始化数据库命令执行对象
    private void initDao() {
        dbDao = new SearchPlayerHistoryDao(context);
    }

    @Override
    public void onClick(View v) {
        if(v.getId() == R.id.lin_province) {    // 省市台
            startActivityForResult(new Intent(context, CityListActivity.class), 0);
            return ;
        }
        String cityName;
        switch (v.getId()) {
            case R.id.lin_country:              // 国家台
                cityName = "国家台";
                break;
            case R.id.lin_internet:             // 网络台
                cityName = "网络台";
                break;
            case R.id.lin_head_more:            // 更多
                cityName = BSApplication.SharedPreferences.getString(StringConstant.CITYNAME, "北京");
                break;
            default:
                cityName = "国家台";
                break;
        }
        String cityId = BSApplication.SharedPreferences.getString(StringConstant.CITYID, "110000");
        Intent intent = new Intent(context, FMListActivity.class);
        Bundle bundle = new Bundle();
        bundle.putString("fromtype", "online");
        bundle.putString("name", cityName);
        bundle.putString("type", "2");
        bundle.putString("id", cityId);
        intent.putExtras(bundle);
        startActivity(intent);
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
            textName.setText(cityName = BSApplication.SharedPreferences.getString(StringConstant.CITYNAME, "北京"));

            gridView = (GridView) headView.findViewById(R.id.gridView);
            gridView.setSelector(new ColorDrawable(Color.TRANSPARENT));         // 取消默认 selector

            headView.findViewById(R.id.lin_head_more).setOnClickListener(this); // 更多
            headView.findViewById(R.id.lin_country).setOnClickListener(this);   // 国家台
            headView.findViewById(R.id.lin_province).setOnClickListener(this);  // 省市台
            headView.findViewById(R.id.lin_internet).setOnClickListener(this);  // 网络台

            mPullToRefreshLayout = (PullToRefreshLayout) rootView.findViewById(R.id.refresh_view);
            listViewMain.addHeaderView(headView);
            if (receiver == null) {
                receiver = new MessageReceiver();
                IntentFilter filter = new IntentFilter();
                filter.addAction(BroadcastConstant.CITY_CHANGE);
                context.registerReceiver(receiver, filter);
            }

            initListView();
            getCity();
            sendRequest();
        }
        return rootView;
    }

    // 初始化展示列表控件
    private void initListView() {
        listViewMain.setSelector(new ColorDrawable(Color.TRANSPARENT));
        listViewMain.setGroupIndicator(null);

        listViewMain.setOnGroupClickListener(new OnGroupClickListener() {
            @Override
            public boolean onGroupClick(ExpandableListView parent, View v, int groupPosition, long id) {
                return true;
            }
        });

        mPullToRefreshLayout.setOnRefreshListener(new PullToRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh(PullToRefreshLayout pullToRefreshLayout) {
                refreshType = 1;
                page = 1;
                beginCatalogId = "";
                sendRequest();
            }

            @Override
            public void onLoadMore(PullToRefreshLayout pullToRefreshLayout) {
                refreshType = 2;
                sendRequest();
            }
        });
    }

    // 获取所选择的城市电台
    private void getCity() {
        if (GlobalConfig.CURRENT_NETWORK_STATE_TYPE == -1) {
            ToastUtils.show_always(context, "网络失败，请检查网络");
            return ;
        }

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

            @Override
            protected void requestSuccess(JSONObject result) {
                if (isCancelRequest) {
                    return;
                }
                try {
                    returnType = result.getString("ReturnType");
                    if (returnType != null && returnType.equals("1001")) {
                        JSONObject arg1 = (JSONObject) new JSONTokener(result.getString("ResultList")).nextValue();
                        mainListS = new Gson().fromJson(arg1.getString("List"), new TypeToken<List<RankInfo>>() {}.getType());
                        if(mainListS != null && mainListS.size() > 0) {
                            gridView.setAdapter(new citynewsadapter(context, mainListS));
                            gridListener();
                        }
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

    // 获取城市分类电台
    private void sendRequest() {
        if (GlobalConfig.CURRENT_NETWORK_STATE_TYPE == -1) {
            ToastUtils.show_always(context, "网络失败，请检查网络");
            return ;
        }

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
                    if (returnType != null && returnType.equals("1001")) {
                        JSONObject arg1 = (JSONObject) new JSONTokener(result.getString("ResultList")).nextValue();
                        beginCatalogId = arg1.getString("BeginCatalogId");
                        mainList = new Gson().fromJson(arg1.getString("List"), new TypeToken<List<RadioPlay>>() {}.getType());

                        mPullToRefreshLayout.refreshFinish(PullToRefreshLayout.SUCCEED);
                        if (refreshType == 1) {
                            newList.clear();
                        }
                        newList.addAll(mainList);
                        if (adapter == null) {
                            listViewMain.setAdapter(adapter = new onlineAdapter(context, newList));
                        } else {
                            adapter.notifyDataSetChanged();
                        }
                        for (int i = 0; i < newList.size(); i++) {
                            listViewMain.expandGroup(i);
                        }
                        setItemListener();
                    } else {
                        ToastUtils.show_always(context, "暂无数据");
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

    // 城市电台
    protected void gridListener() {
        gridView.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                setItemLis(mainListS, position);
            }
        });
    }

    // 初始一号位置为0,0  城市分类电台
    protected void setItemListener() {
        listViewMain.setOnChildClickListener(new OnChildClickListener() {
            @Override
            public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id) {
                setItemLis(newList.get(groupPosition).getList(), childPosition);
                return false;
            }
        });
    }

    // 电台节目的点击事件监听
    private void setItemLis(List<RankInfo> list, int position) {
        if (list != null && list.get(position) != null && list.get(position).getMediaType() != null) {
            String mediaType = list.get(position).getMediaType();
            if(mediaType == null || mediaType.equals("")) {
                return ;
            }
            if (mediaType.equals("RADIO") || mediaType.equals("AUDIO")) {
                String playername = list.get(position).getContentName();
                String playerimage = list.get(position).getContentImg();
                String playerurl = list.get(position).getContentPlay();
                String playerurI = list.get(position).getContentURI();
                String playermediatype = list.get(position).getMediaType();
                String playcontentshareurl = list.get(position).getContentShareURL();
                String plaplayeralltime = "0";
                String playerintime = "0";
                String playercontentdesc = list.get(position).getCurrentContent();
                String playernum = list.get(position).getWatchPlayerNum();
                String playerzantype = "0";
                String playerfrom = "";
                String playerfromid = "";
                String playerfromurl = "";
                String playeraddtime = Long.toString(System.currentTimeMillis());
                String bjuserid = CommonUtils.getUserId(context);
                String ContentFavorite = list.get(position).getContentFavorite();
                String ContentId = list.get(position).getContentId();
                String localurl = list.get(position).getLocalurl();
                String sequname =  list.get(position).getSequName();
                String sequid =  list.get(position).getSequId();
                String sequdesc = list.get(position).getSequDesc();
                String sequimg = list.get(position).getSequImg();
                // 如果该数据已经存在数据库则删除原有数据，然后添加最新数据
                PlayerHistory history = new PlayerHistory(
                        playername, playerimage, playerurl, playerurI, playermediatype,
                        plaplayeralltime, playerintime, playercontentdesc, playernum,
                        playerzantype, playerfrom, playerfromid, playerfromurl, playeraddtime, bjuserid, playcontentshareurl, ContentFavorite, ContentId,
                        localurl,sequname,sequid,sequdesc,sequimg);
                dbDao.deleteHistory(playerurl);
                dbDao.addHistory(history);
                PlayerFragment.SendTextRequest(list.get(position).getContentName(), context);
                HomeActivity.UpdateViewPager();
            }
        }
    }

    // 用户选择的城市发生变化时同步更新城市电台和分类电台的广播
    class MessageReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals(BroadcastConstant.CITY_CHANGE)) {
                if (GlobalConfig.CityName != null && !cityName.equals(GlobalConfig.CityName)) {
                    cityName = GlobalConfig.CityName;
                    textName.setText(cityName);
                    page = 1;
                    beginCatalogId = "";
                    refreshType = 1;
                    getCity();
                    sendRequest();
                    SharedPreferences.Editor et = BSApplication.SharedPreferences.edit();
                    et.putString(StringConstant.CITYTYPE, "false");
                    if(!et.commit()) {
                        L.w("数据 commit 失败!");
                    }
                }
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
        if(receiver != null) {
            context.unregisterReceiver(receiver);
            receiver = null;
        }
    }
}
