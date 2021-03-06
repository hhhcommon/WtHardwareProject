package com.wotingfm.ui.music.program.radiolist.fragment;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v4.app.Fragment;
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
import com.wotingfm.ui.music.program.radiolist.adapter.ForNullAdapter;
import com.wotingfm.ui.music.program.radiolist.adapter.ListInfoAdapter;
import com.wotingfm.ui.music.program.radiolist.main.RadioListFragment;
import com.wotingfm.ui.music.program.radiolist.model.Image;
import com.wotingfm.ui.music.program.radiolist.model.ListInfo;
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
 * 分类列表
 * woting11
 */
public class ClassifyFragment extends Fragment implements TipView.WhiteViewClick {
    private Context context;
    private SearchPlayerHistoryDao dbDao;  // 数据库
    private ListInfoAdapter adapter;
    private List<ListInfo> newList = new ArrayList<>();
    private List<ListInfo> subList;

    private View rootView;
    private XListView mListView;            // 列表
    private Dialog dialog;                  // 加载对话框
    private TipView tipView;                // 没有网络、没有数据提示

    private int page = 1;                   // 页码
    private int refreshType = 1;            // refreshType 1 为下拉加载  2 为上拉加载更多

    private String CatalogId;
    private String CatalogType;
    private Banner mLoopViewPager;
    private List<Image> imageList;
    private List<String> ImageStringList = new ArrayList<>();

    @Override
    public void onWhiteViewClick() {
        dialog = DialogUtils.Dialogph(context, "正在获取数据");
        sendRequest();
        getImage();
    }

    /**
     * 创建 Fragment 实例
     */
    public static Fragment instance(String CatalogId, String CatalogType) {
        Fragment fragment = new ClassifyFragment();
        Bundle bundle = new Bundle();
        bundle.putString("CatalogId", CatalogId);
        bundle.putString("CatalogType", CatalogType);
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = getActivity();
        initDao();
        Bundle bundle = getArguments();// 取值 用以判断加载的数据
        CatalogId = bundle.getString("CatalogId");
        CatalogType = bundle.getString("CatalogType");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (rootView == null) {
            rootView = inflater.inflate(R.layout.fragment_radio_list_layout, container, false);
            rootView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                }
            });

            tipView = (TipView) rootView.findViewById(R.id.tip_view);
            tipView.setWhiteClick(this);

            View headView = LayoutInflater.from(context).inflate(R.layout.headview_acitivity_radiolist, null);
            // 轮播图
            mLoopViewPager = (Banner) headView.findViewById(R.id.slideshowView);

            mListView = (XListView) rootView.findViewById(R.id.listview_fm);
            mListView.addHeaderView(headView);
            setListener();
            if (GlobalConfig.CURRENT_NETWORK_STATE_TYPE != -1) {
                sendRequest();
                getImage();
            } else {
                tipView.setVisibility(View.VISIBLE);
                tipView.setTipView(TipView.TipStatus.NO_NET);
            }
        }
        if (dialog != null) dialog.dismiss();
        return rootView;
    }

    // 与onActivityCreated()方法 解决预加载问题
    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
      /*  if (isVisibleToUser && adapter == null && getActivity() != null) {
            dialog = DialogUtils.Dialogph(context, "正在获取数据");
            sendRequest();
            getImage();
        }*/
        super.setUserVisibleHint(isVisibleToUser);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setUserVisibleHint(getUserVisibleHint());
    }

    // 请求网络获取分类信息
    private void sendRequest() {
        if (GlobalConfig.CURRENT_NETWORK_STATE_TYPE == -1) {
            if (dialog != null) dialog.dismiss();
            if (refreshType == 1) {
                tipView.setVisibility(View.VISIBLE);
                tipView.setTipView(TipView.TipStatus.NO_NET);
                mListView.stopRefresh();
            } else {
                mListView.stopLoadMore();
            }
            return;
        }

        VolleyRequest.RequestPost(GlobalConfig.getContentUrl, RadioListFragment.tag, setParam(), new VolleyCallback() {
            private String ReturnType;

            @Override
            protected void requestSuccess(JSONObject result) {
                if (dialog != null) dialog.dismiss();
                if (RadioListFragment.isCancel()) return;
                try {
                    ReturnType = result.getString("ReturnType");
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                if (ReturnType != null && ReturnType.equals("1001")) {
                    try {
                        page++;
                        JSONObject arg1 = (JSONObject) new JSONTokener(result.getString("ResultList")).nextValue();
                        subList = new Gson().fromJson(arg1.getString("List"), new TypeToken<List<ListInfo>>() {
                        }.getType());
                        if (refreshType == 1) newList.clear();
                        newList.addAll(subList);
                        if (adapter == null) {
                            mListView.setAdapter(adapter = new ListInfoAdapter(context, newList));
                        } else {
                            adapter.notifyDataSetChanged();
                        }
                        setOnItem();
                        tipView.setVisibility(View.GONE);
                    } catch (Exception e) {
                        e.printStackTrace();
                        mListView.setAdapter(new ForNullAdapter(context));
                        if (imageList == null) {
                            if (refreshType == 1) {
                                tipView.setVisibility(View.VISIBLE);
                                tipView.setTipView(TipView.TipStatus.IS_ERROR);
                            }
                        }
                    }
                } else {
                    mListView.setPullLoadEnable(false);
                    mListView.setAdapter(new ForNullAdapter(context));
                    if (imageList == null) {
                        if (refreshType == 1) {
                            tipView.setVisibility(View.VISIBLE);
                            tipView.setTipView(TipView.TipStatus.NO_DATA, "数据君不翼而飞了\n点击界面会重新获取数据哟");
                        } else {
                            ToastUtils.show_always(context, "没有更多的数据了");
                        }
                    }
                }
                if (refreshType == 1) {
                    mListView.stopRefresh();
                } else {
                    mListView.stopLoadMore();
                }
            }

            @Override
            protected void requestError(VolleyError error) {
                if (dialog != null) dialog.dismiss();
                if (refreshType == 1) {
                    tipView.setVisibility(View.VISIBLE);
                    tipView.setTipView(TipView.TipStatus.IS_ERROR);
                } else {
                    ToastUtils.showVolleyError(context);
                }
            }
        });
    }

    private JSONObject setParam() {
        JSONObject jsonObject = VolleyRequest.getJsonObject(context);
        try {
            jsonObject.put("CatalogType", CatalogType);
            jsonObject.put("CatalogId", CatalogId);
            jsonObject.put("Page", String.valueOf(page));
            jsonObject.put("ResultType", "3");
            jsonObject.put("RelLevel", "2");
            jsonObject.put("PageSize", "10");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return jsonObject;
    }

    private void setOnItem() {
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
                        String playerName = newList.get(position).getContentName();
                        String playerImage = newList.get(position).getContentImg();
                        String playUrl = newList.get(position).getContentPlay();
                        String playUrI = newList.get(position).getContentURI();
                        String playContentShareUrl = newList.get(position).getContentShareURL();
                        String playMediaType = newList.get(position).getMediaType();
                        String playAllTime = newList.get(position).getContentTimes();
                        String playInTime = "0";
                        String playContentDesc = newList.get(position).getContentDescn();
                        String playNum = newList.get(position).getPlayCount();
                        String playZanType = "0";
                        String playFrom = newList.get(position).getContentPub();
                        String playFromId = "";
                        String playFromUrl = "";
                        String playAddTime = Long.toString(System.currentTimeMillis());
                        String bjUserId = CommonUtils.getUserId(context);
                        String ContentFavorite = newList.get(position).getContentFavorite();
                        String ContentId = newList.get(position).getContentId();
                        String localUrl = newList.get(position).getLocalurl();

                        String sequName = newList.get(position).getSequName();
                        String sequId = newList.get(position).getSequId();
                        String sequDesc = newList.get(position).getSequDesc();
                        String sequImg = newList.get(position).getSequImg();

                        //如果该数据已经存在数据库则删除原有数据，然后添加最新数据
                        PlayerHistory history = new PlayerHistory(
                                playerName, playerImage, playUrl, playUrI, playMediaType,
                                playAllTime, playInTime, playContentDesc, playNum,
                                playZanType, playFrom, playFromId, playFromUrl, playAddTime, bjUserId, playContentShareUrl,
                                ContentFavorite, ContentId, localUrl, sequName, sequId, sequDesc, sequImg);
                        dbDao.deleteHistory(playUrl);
                        dbDao.addHistory(history);

                        Intent push = new Intent(BroadcastConstants.PLAY_TEXT_VOICE_SEARCH);
                        Bundle bundle1 = new Bundle();
                        bundle1.putString(StringConstant.TEXT_CONTENT, newList.get(position).getContentName());
                        push.putExtras(bundle1);
                        context.sendBroadcast(push);
                        MainActivity.changeOne();
                    } else if (MediaType.equals(StringConstant.TYPE_SEQU)) {
                        AlbumFragment albumFragment = new AlbumFragment();
                        Bundle bundle = new Bundle();
                        bundle.putString("type", "radiolistactivity");
                        bundle.putSerializable("list", newList.get(position));
                        bundle.putString(StringConstant.FROM_TYPE, StringConstant.TAG_PROGRAM);
                        albumFragment.setArguments(bundle);
                        ProgramActivity.open(albumFragment);
                    }
                }
            }
        });
    }

    // 设置刷新、加载更多参数
    private void setListener() {
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

    // 初始化数据库命令执行对象
    private void initDao() {
        dbDao = new SearchPlayerHistoryDao(context);
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
         /*   jsonObject.put("CatalogType", CatalogType);
            jsonObject.put("CatalogId", CatalogId);
            jsonObject.put("Size", "4");// 此处需要改成-1*/
            jsonObject.put("CatalogType", CatalogType);
            jsonObject.put("CatalogId","cn17");
            jsonObject.put("Size", "4");

        } catch (JSONException e) {
            e.printStackTrace();
        }
        VolleyRequest.RequestPost(GlobalConfig.getImage, RadioListFragment.tag, jsonObject, new VolleyCallback() {
            private String ReturnType;

            @Override
            protected void requestSuccess(JSONObject result) {
                if (dialog != null) dialog.dismiss();
                if (RadioListFragment.isCancel()) return;
                try {
                    ReturnType = result.getString("ReturnType");
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                if (ReturnType != null && ReturnType.equals("1001")) {
                    try {
                        imageList = new Gson().fromJson(result.getString("LoopImgs"), new TypeToken<List<Image>>() {
                        }.getType());
//                        mLoopViewPager.setAdapter(new LoopAdapter(mLoopViewPager, context, imageList));
//                        mLoopViewPager.setHintView(new IconHintView(context, R.mipmap.indicators_now, R.mipmap.indicators_default));
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
}
