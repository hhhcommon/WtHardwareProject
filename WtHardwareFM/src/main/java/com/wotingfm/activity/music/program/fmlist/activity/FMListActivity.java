package com.wotingfm.activity.music.program.fmlist.activity;

import android.app.Dialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;

import com.android.volley.VolleyError;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.wotingfm.R;
import com.wotingfm.activity.common.baseactivity.AppBaseActivity;
import com.wotingfm.activity.music.main.HomeActivity;
import com.wotingfm.activity.music.main.dao.SearchPlayerHistoryDao;
import com.wotingfm.activity.music.player.fragment.PlayerFragment;
import com.wotingfm.activity.music.player.model.PlayerHistory;
import com.wotingfm.activity.music.program.diantai.model.RadioPlay;
import com.wotingfm.activity.music.program.fmlist.adapter.RankInfoAdapter;
import com.wotingfm.activity.music.program.fmlist.model.RankInfo;
import com.wotingfm.common.application.BSApplication;
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
import org.json.JSONTokener;

import java.util.ArrayList;
import java.util.List;

/**
 * 电台列表
 * @author 辛龙
 *         2016年8月8日
 */
public class FMListActivity extends AppBaseActivity {
    private SearchPlayerHistoryDao dbDao;
    protected RankInfoAdapter adapter;

    private List<RankInfo> newList = new ArrayList<>();
    private Dialog dialog;
    private XListView mListView;
    
    private int viewType = 1;
    private int page = 1;
    private int refreshType = 1;// refreshType 1 为下拉加载 2 为上拉加载更多
    private int pageSizeNum;
    private String catalogId;
    private String tag = "FM_LIST_VOLLEY_REQUEST_CANCEL_TAG";
    private boolean isCancelRequest;

    // 初始化数据库命令执行对象
    private void initDao() {
        dbDao = new SearchPlayerHistoryDao(context);
    }

    @Override
    protected int setViewId() {
        return R.layout.activity_fmlist;
    }

    @Override
    protected void init() {
        initDao();
        initListView();
        handleRequestType();

        dialog = DialogUtils.Dialogph(context, "正在获取数据");
        sendRequest();
    }

    // 初始化控件
    private void initListView() {
        mListView = (XListView) findViewById(R.id.listview_fm);
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
                if (page <= pageSizeNum) {
                    refreshType = 2;
                    sendRequest();
                } else {
                    mListView.stopLoadMore();
                    ToastUtils.show_always(context, "已经没有最新的数据了");
                }
            }
        });
    }

    // 处理上一个界面跳转携带的数据
    private void handleRequestType() {
        String type = getIntent().getStringExtra("fromtype");
        String position = getIntent().getStringExtra("Position");
        if (position != null && !position.trim().equals("")) {
            viewType = -1;
        }
        String catalogName;
        RadioPlay list;
        if (type != null && type.trim().equals("online")) {
            catalogName = getIntent().getStringExtra("name");
            catalogId = getIntent().getStringExtra("id");
        } else {
            list = (RadioPlay) getIntent().getSerializableExtra("list");
            catalogName = list.getCatalogName();
            catalogId = list.getCatalogId();
        }
        setTitle(catalogName);
    }

    // 请求网络获取网络数据
    private void sendRequest() {
        // 以下操作需要网络支持
        if (GlobalConfig.CURRENT_NETWORK_STATE_TYPE == -1) {
            ToastUtils.show_always(context, "网络失败，请检查网络");
            return ;
        }

        VolleyRequest.RequestPost(GlobalConfig.getContentUrl, tag, setParam(), new VolleyCallback() {
            private String returnType;

            @Override
            protected void requestSuccess(JSONObject result) {
                if (dialog != null) {
                    dialog.dismiss();
                }
                if (isCancelRequest) {
                    return;
                }
                page++;
                try {
                    returnType = result.getString("ReturnType");
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                if (returnType != null && returnType.equals("1001")) {
                    try {
                        JSONObject arg1 = (JSONObject) new JSONTokener(result.getString("ResultList")).nextValue();
                        pageSizeNum = Integer.valueOf(arg1.getString("PageSize"));
                        List<RankInfo> subList = new Gson().fromJson(arg1.getString("List"), new TypeToken<List<RankInfo>>() {}.getType());
                        if (refreshType == 1) {
                            mListView.stopRefresh();
                            newList.clear();
                            newList.addAll(subList);
                            mListView.setAdapter(adapter = new RankInfoAdapter(FMListActivity.this, newList));
                        } else if (refreshType == 2) {
                            mListView.stopLoadMore();
                            newList.addAll(subList);
                            adapter.notifyDataSetChanged();
                        }
                        setListView();
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                } else {
                    mListView.stopLoadMore();
                }
            }

            @Override
            protected void requestError(VolleyError error) {
                if (dialog != null) {
                    dialog.dismiss();
                }
                ToastUtils.showVolleyError(context);
            }
        });
    }

    // 获取网络数据需要提交的参数
    private JSONObject setParam() {
        JSONObject jsonObject = VolleyRequest.getJsonObject(context);
        try {
//            jsonObject.put("UserId", CommonUtils.getUserId(FMListActivity.this));
            jsonObject.put("MediaType", "RADIO");
            String cityId = BSApplication.SharedPreferences.getString(StringConstant.CITYID, "110000");
            if (viewType == 1) {    // 获取当前城市下所有分类内容
                jsonObject.put("CatalogId", cityId);
                jsonObject.put("CatalogType", "2");
            } else {                // 按照分类获取内容
                JSONObject js = new JSONObject();
                jsonObject.put("CatalogType", "1");
                jsonObject.put("CatalogId", catalogId);
                js.put("CatalogType", "2");
                js.put("CatalogId", cityId);
                jsonObject.put("FilterData", js);
            }
            jsonObject.put("PerSize", "3");
            jsonObject.put("ResultType", "3");
            jsonObject.put("PageSize", "10");
            jsonObject.put("Page", String.valueOf(page));
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
                        String plaplayeralltime = "0";
                        String playerintime = "0";
                        String playercontentdesc = newList.get(position - 1).getCurrentContent();
                        String playernum = newList.get(position - 1).getWatchPlayerNum();
                        String playerzantype = "0";
                        String playerfrom = "";
                        String playerfromid = "";
                        String playerfromurl = "";
                        String playeraddtime = Long.toString(System.currentTimeMillis());
                        String bjuserid = CommonUtils.getUserId(context);
                        String ContentFavorite = newList.get(position - 1).getContentFavorite();
                        String ContentId = newList.get(position - 1).getContentId();
                        String localurl = newList.get(position - 1).getLocalurl();
                        String sequname = newList.get(position - 1).getSequName();
                        String sequid = newList.get(position - 1).getSequId();
                        String sequdesc =newList.get(position - 1).getSequDesc();
                        String sequimg =newList.get(position - 1).getSequImg();
                        //如果该数据已经存在数据库则删除原有数据，然后添加最新数据
                        PlayerHistory history = new PlayerHistory(
                                playername, playerimage, playerurl, playerurI, playermediatype,
                                plaplayeralltime, playerintime, playercontentdesc, playernum,
                                playerzantype, playerfrom, playerfromid, playerfromurl, playeraddtime, bjuserid, playcontentshareurl, ContentFavorite, ContentId, localurl,sequname,sequid,sequdesc,sequimg);
                        dbDao.deleteHistory(playerurl);
                        dbDao.addHistory(history);
                        HomeActivity.UpdateViewPager();
                        PlayerFragment.SendTextRequest(newList.get(position - 1).getContentName(), context);
                        finish();
                    }
                }
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        isCancelRequest = VolleyRequest.cancelRequest(tag);
        if (dbDao != null) {
            dbDao.closedb();
            dbDao = null;
        }
        if(newList != null) {
            newList.clear();
            newList = null;
        }
        mListView = null;
        dialog = null;
        adapter = null;
    }
}