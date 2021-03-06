package com.wotingfm.ui.music.program.tuijian.fragment;

import android.app.Dialog;
import android.content.Intent;
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
import com.wotingfm.ui.music.album.main.AlbumFragment;
import com.wotingfm.ui.music.main.ProgramActivity;
import com.wotingfm.ui.music.main.dao.SearchPlayerHistoryDao;
import com.wotingfm.ui.music.player.model.PlayerHistory;
import com.wotingfm.ui.music.program.fmlist.model.RankInfo;
import com.wotingfm.ui.music.program.radiolist.model.Image;
import com.wotingfm.ui.music.program.tuijian.adapter.RecommendListAdapter;
import com.wotingfm.util.CommonUtils;
import com.wotingfm.util.DialogUtils;
import com.wotingfm.util.L;
import com.wotingfm.util.PicassoBannerLoader;
import com.wotingfm.util.ToastUtils;
import com.wotingfm.widget.TipView;
import com.wotingfm.widget.xlistview.XListView;
import com.wotingfm.widget.xlistview.XListView.IXListViewListener;
import com.youth.banner.Banner;
import com.youth.banner.listener.OnBannerListener;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.util.ArrayList;
import java.util.List;

/**
 * 节目页----推荐页
 * 辛龙
 * 2016年3月30日
 */
public class RecommendFragment extends Fragment implements TipView.WhiteViewClick {
    private SearchPlayerHistoryDao dbDao;
    private FragmentActivity context;
    private RecommendListAdapter adapter;

    private Dialog dialog;// 加载数据对话框
    private View rootView;
    private View headView;
    private XListView mListView;

    private List<RankInfo> subList;
    private List<RankInfo> newList = new ArrayList<>();

    private String tag = "RECOMMEND_VOLLEY_REQUEST_CANCEL_TAG";
    private int page = 1;
    private int refreshType = 1; // refreshType 1为下拉加载 2为上拉加载更多
    private boolean isCancelRequest;

    private TipView tipView;// 没有网络、没有数据、加载错误提示
    private Banner mLoopViewPager;
    private List<String> ImageStringList = new ArrayList<>();

    @Override
    public void onWhiteViewClick() {
        dialog = DialogUtils.Dialogph(context, "数据加载中...");
        sendRequest();
        getImage();
    }

    // 初始化数据库命令执行对象
    private void initDao() {
        dbDao = new SearchPlayerHistoryDao(context);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = getActivity();
        initDao();          // 初始化数据库命令执行对象
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (rootView == null) {
            rootView = inflater.inflate(R.layout.fragment_recommend, container, false);
            rootView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                }
            });
            tipView = (TipView) rootView.findViewById(R.id.tip_view);
            tipView.setWhiteClick(this);

            mListView = (XListView) rootView.findViewById(R.id.listView);
            headView = LayoutInflater.from(context).inflate(R.layout.headview_fragment_recommend, null);
            // 轮播图
            mLoopViewPager = (Banner) headView.findViewById(R.id.slideshowView);
            mListView.addHeaderView(headView);
            mListView.setSelector(new ColorDrawable(Color.TRANSPARENT));

            initListView();


            if (GlobalConfig.CURRENT_NETWORK_STATE_TYPE != -1) {// 发送网络请求
                dialog = DialogUtils.Dialogph(context, "数据加载中....");
                sendRequest();
                getImage();
            } else {
                tipView.setVisibility(View.VISIBLE);
                tipView.setTipView(TipView.TipStatus.NO_NET);
            }
        }
        return rootView;
    }

    // 初始化展示列表控件
    private void initListView() {
        mListView.setPullRefreshEnable(true);
        mListView.setPullLoadEnable(true);
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

    // 获取推荐列表
    private void sendRequest() {
        // 以下操作需要网络支持 所以没有网络则直接提示用户设置网络
        if (GlobalConfig.CURRENT_NETWORK_STATE_TYPE == -1) {
            if (dialog != null) dialog.dismiss();
            if (refreshType == 1) {
                tipView.setVisibility(View.VISIBLE);
                tipView.setTipView(TipView.TipStatus.NO_NET);
            }
            mListView.stopRefresh();
            mListView.stopLoadMore();
            return;
        }

        VolleyRequest.RequestPost(GlobalConfig.getContentUrl, tag, setParam(), new VolleyCallback() {
            private String returnType;

            @Override
            protected void requestSuccess(JSONObject result) {
                if (dialog != null) dialog.dismiss();
                if (isCancelRequest) return;
                try {
                    returnType = result.getString("ReturnType");
                    L.w("returnType -- > > " + returnType);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                if (returnType != null && returnType.equals("1001")) {
                    try {
                        page++;
                        JSONObject arg1 = (JSONObject) new JSONTokener(result.getString("ResultList")).nextValue();
                        subList = new Gson().fromJson(arg1.getString("List"), new TypeToken<List<RankInfo>>() {
                        }.getType());
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    if (refreshType == 1) newList.clear();
                    newList.addAll(subList);
                    if (adapter == null) {
                        mListView.setAdapter(adapter = new RecommendListAdapter(context, newList));
                    } else {
                        adapter.notifyDataSetChanged();
                    }
                    setListener();
                    tipView.setVisibility(View.GONE);
                } else {
                    mListView.setPullLoadEnable(false);
                    if (refreshType == 1) {
                        tipView.setVisibility(View.VISIBLE);
                        tipView.setTipView(TipView.TipStatus.NO_DATA, "专辑中没有节目\n换个专辑看看吧");
                    } else {
                        ToastUtils.show_always(context, "已经没有更多推荐了!");
                    }
                }

                // 无论何种返回值，都需要终止掉下拉刷新及上拉加载的滚动状态
                if (refreshType == 1) {
                    mListView.stopRefresh();
                } else {
                    mListView.stopLoadMore();
                }
            }

            @Override
            protected void requestError(VolleyError error) {
                if (dialog != null) dialog.dismiss();
                ToastUtils.showVolleyError(context);
                if (refreshType == 1) {
                    tipView.setVisibility(View.VISIBLE);
                    tipView.setTipView(TipView.TipStatus.NO_DATA, "专辑中没有节目\n换个专辑看看吧");
                }
            }
        });
    }

    private JSONObject setParam() {
        JSONObject jsonObject = VolleyRequest.getJsonObject(context);
        try {
            jsonObject.put("MediaType", "");
            jsonObject.put("CatalogType", "-1");// 001为一个结果 002为另一个
            jsonObject.put("CatalogId", "");
            jsonObject.put("Page", String.valueOf(page));
            jsonObject.put("PerSize", "3");
            jsonObject.put("ResultType", "3");
            jsonObject.put("PageSize", "10");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return jsonObject;
    }

    // 列表点击事件监听
    private void setListener() {
        mListView.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                position = position - 2;
                if (position < 0) {
                    L.w("TAG", "position error -- > " + position);
                    return;
                }
                if (newList != null && newList.get(position) != null && newList.get(position).getMediaType() != null) {
                    String MediaType = newList.get(position).getMediaType();
                    if (MediaType.equals(StringConstant.TYPE_RADIO) || MediaType.equals(StringConstant.TYPE_AUDIO)) {
                        String playername = newList.get(position).getContentName();
                        String playerimage = newList.get(position).getContentImg();
                        String playerurl = newList.get(position).getContentPlay();
                        String playerurI = newList.get(position).getContentURI();
                        String playermediatype = newList.get(position).getMediaType();
                        String playercontentshareurl = newList.get(position).getContentShareURL();
                        String plaplayeralltime = newList.get(position).getContentTimes();
                        String playerintime = "0";
                        String playercontentdescn = newList.get(position).getContentDescn();
                        String playernum = newList.get(position).getPlayCount();
                        String playerzantype = "0";
                        String playerfrom = newList.get(position).getContentPub();
                        String playerfromid = "";
                        String playerfromurl = "";
                        String playeraddtime = Long.toString(System.currentTimeMillis());
                        String bjuserid = CommonUtils.getUserId(context);
                        String ContentFavorite = newList.get(position).getContentFavorite();
                        String ContentId = newList.get(position).getContentId();
                        String localurl = newList.get(position).getLocalurl();
                        String sequname = newList.get(position).getSequName();
                        String sequid = newList.get(position).getSequId();
                        String sequdesc = newList.get(position).getSequDesc();
                        String sequimg = newList.get(position).getSequImg();

                        // 如果该数据已经存在数据库则删除原有数据，然后添加最新数据
                        PlayerHistory history = new PlayerHistory(
                                playername, playerimage, playerurl, playerurI, playermediatype,
                                plaplayeralltime, playerintime, playercontentdescn, playernum,
                                playerzantype, playerfrom, playerfromid, playerfromurl, playeraddtime, bjuserid, playercontentshareurl, ContentFavorite, ContentId, localurl, sequname, sequid, sequdesc, sequimg);
                        dbDao.deleteHistory(playerurl);
                        dbDao.addHistory(history);

                        Intent push = new Intent(BroadcastConstants.PLAY_TEXT_VOICE_SEARCH);
                        Bundle bundle1 = new Bundle();
                        bundle1.putString(StringConstant.TEXT_CONTENT, newList.get(position).getContentName());
                        push.putExtras(bundle1);
                        context.sendBroadcast(push);
                        MainActivity.changeOne();
                    } else if (MediaType.equals(StringConstant.TYPE_SEQU)) {
                        AlbumFragment fg = new AlbumFragment();
                        Bundle bundle = new Bundle();
                        bundle.putString("type", "recommend");
                        bundle.putSerializable("list", newList.get(position));
                        bundle.putString(StringConstant.FROM_TYPE, StringConstant.TAG_PROGRAM);
                        fg.setArguments(bundle);
                        ProgramActivity.open(fg);
                    } else {
                        ToastUtils.show_short(context, "暂不支持的Type类型");
                    }
                }
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (null != rootView) {
            ((ViewGroup) rootView.getParent()).removeView(rootView);
        }
    }

    // 请求网络获取分类信息
    private void getImage() {
        JSONObject jsonObject = VolleyRequest.getJsonObject(context);
        try {
            jsonObject.put("CatalogType", "-1");
            jsonObject.put("CatalogId", "cn10");
            jsonObject.put("Size", "10");// 此处需要改成-1
        } catch (JSONException e) {
            e.printStackTrace();
        }
        VolleyRequest.RequestPost(GlobalConfig.getImage, tag, jsonObject, new VolleyCallback() {
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
                        List<Image> imageList = new Gson().fromJson(result.getString("LoopImgs"), new TypeToken<List<Image>>() {
                        }.getType());
                   /*     mLoopViewPager.setAdapter(new LoopAdapter(mLoopViewPager, context, imageList));
                        mLoopViewPager.setHintView(new IconHintView(context, R.mipmap.indicators_now, R.mipmap.indicators_default));*/
                        mLoopViewPager.setImageLoader(new PicassoBannerLoader());

                        for (int i = 0; i < imageList.size(); i++) {
                            ImageStringList.add(imageList.get(i).getLoopImg());
                        }
                        mLoopViewPager.setImages(ImageStringList);

                        mLoopViewPager.setOnBannerListener(new OnBannerListener() {
                            @Override
                            public void OnBannerClick(int position) {
                                ToastUtils.show_always(context, ImageStringList.get(position - 1));
                            }
                        });
                        mLoopViewPager.start();
                        mLoopViewPager.setVisibility(View.VISIBLE);
                        tipView.setVisibility(View.GONE);
                    } catch (Exception e) {
                        e.printStackTrace();
                        mLoopViewPager.setVisibility(View.GONE);
                    }
                } else {
                    mLoopViewPager.setVisibility(View.GONE);
                }
            }

            @Override
            protected void requestError(VolleyError error) {
                mLoopViewPager.setVisibility(View.GONE);
            }
        });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        isCancelRequest = VolleyRequest.cancelRequest(tag);
        context = null;
        headView = null;
        adapter = null;
        subList = null;
        mListView = null;
        newList = null;
        rootView = null;
        tag = null;
        if (dbDao != null) {
            dbDao.closedb();
            dbDao = null;
        }
    }
}
