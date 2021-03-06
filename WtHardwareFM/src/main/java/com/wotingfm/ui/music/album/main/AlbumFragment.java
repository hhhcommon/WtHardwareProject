package com.wotingfm.ui.music.album.main;

import android.app.Dialog;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.android.volley.VolleyError;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.wotingfm.R;
import com.wotingfm.common.config.GlobalConfig;
import com.wotingfm.common.constant.StringConstant;
import com.wotingfm.common.volley.VolleyCallback;
import com.wotingfm.common.volley.VolleyRequest;
import com.wotingfm.ui.baseadapter.MyFragmentChildPagerAdapter;
import com.wotingfm.ui.music.album.fragment.DetailsFragment;
import com.wotingfm.ui.music.album.fragment.ProgramListFragment;
import com.wotingfm.ui.music.album.model.ResultInfo;
import com.wotingfm.ui.music.main.PlayerActivity;
import com.wotingfm.ui.music.main.ProgramActivity;
import com.wotingfm.ui.music.player.model.LanguageSearchInside;
import com.wotingfm.ui.music.program.accuse.activity.AccuseFragment;
import com.wotingfm.ui.music.program.fmlist.model.RankInfo;
import com.wotingfm.ui.music.search.main.SearchLikeActivity;
import com.wotingfm.util.DialogUtils;
import com.wotingfm.util.L;
import com.wotingfm.util.ToastUtils;
import com.wotingfm.widget.TipView;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * 专辑页
 * 辛龙
 * 2016年4月1日
 */
public class AlbumFragment extends Fragment implements OnClickListener, ViewPager.OnPageChangeListener, TipView.WhiteViewClick {
    private DetailsFragment detailsFragment;// 专辑详情
    private ProgramListFragment programFragment;// 专辑列表
    private static ResultInfo resultInfo = null;// 获取的专辑信息
    private ImageView[] imageViews;

    private TipView tipView;// 没有网络、没有数据提示
    private Dialog dialog;// 加载数据对话框
    private TextView textAlbumName;// 专辑名字

    private static String albumId;// 专辑 ID  从上一个界面传递过来
    private String tag = "ALBUM_VOLLEY_REQUEST_CANCEL_TAG";
    private boolean isCancelRequest;
    private FragmentActivity context;
    private View rootView;
    public static String fromType;

    private View relativeView;

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.head_left_btn: // 返回
                if (fromType == null || fromType.equals("")) return ;
                switch (fromType) {
                    case StringConstant.TAG_PLAY:
                        PlayerActivity.close();
                        break;
                    case StringConstant.TAG_PROGRAM:
                        ProgramActivity.close();
                        break;
                    case StringConstant.TAG_SEARCH:
                        SearchLikeActivity.close();
                        break;
                }
                detailsFragment = null;// 专辑详情
                programFragment = null;// 专辑列表
                break;
            case R.id.head_right_btn:// 举报
                if (GlobalConfig.playerObject != null && !TextUtils.isEmpty(albumId)) {
                    if (fromType == null) return ;
                    AccuseFragment fragment = new AccuseFragment();
                    Bundle bundle = new Bundle();
                    bundle.putString("ContentId", albumId);
                    bundle.putString("MediaType", StringConstant.TYPE_SEQU);
                    bundle.putString(StringConstant.FROM_TYPE, fromType);
                    fragment.setArguments(bundle);
                    switch (fromType) {
                        case StringConstant.TAG_PLAY:
                            PlayerActivity.open(fragment);
                            break;
                        case StringConstant.TAG_PROGRAM:
                            ProgramActivity.open(fragment);
                            break;
                        case StringConstant.TAG_SEARCH:
                            SearchLikeActivity.open(fragment);
                            break;
                    }
                } else {
                    ToastUtils.show_always(context, "获取本专辑信息有误，请回退回上一级界面重试");
                }
                break;
        }
    }

    // 获取专辑信息
    public static ResultInfo getResultInfo() {
        return resultInfo;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (rootView == null) {
            rootView = inflater.inflate(R.layout.activity_album, container, false);
            rootView.setOnClickListener(this);
            context = getActivity();
            fromType = getArguments().getString(StringConstant.FROM_TYPE);// search
            initView();
            initEvent();
        }
        return rootView;
    }

    // 初始化视图
    private void initView() {
        relativeView = rootView.findViewById(R.id.relative_view);
        tipView = (TipView) rootView.findViewById(R.id.tip_view);
        tipView.setWhiteClick(this);
        relativeView.setVisibility(View.GONE);
        tipView.setVisibility(View.VISIBLE);
        tipView.setTipView(TipView.TipStatus.NO_DATA, "数据君不翼而飞了\n点击界面会重新获取数据哟");

        LinearLayout viewLinear = (LinearLayout) rootView.findViewById(R.id.view_group);// viewGroup 添加小圆点
        imageViews = new ImageView[2];// 设置页面下标小红点
        for (int i = 0; i < 2; i++) {
            ImageView imageView = new ImageView(context);
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(20, 20);
            lp.setMargins(20, 0, 20, 0);
            imageView.setLayoutParams(lp);
            imageView.setPadding(0, 0, 20, 20);
            imageViews[i] = imageView;
            if (i == 0) {
                imageViews[i].setBackgroundResource(R.mipmap.wt_image_album_page_indication_focused);// 默认选中第一张图片
            } else {
                imageViews[i].setBackgroundResource(R.mipmap.wt_image_album_page_indication);
            }
            viewLinear.addView(imageViews[i]);
        }
        textAlbumName = (TextView) rootView.findViewById(R.id.head_name_tv);// 专辑名字
        handleIntent();

        if (GlobalConfig.CURRENT_NETWORK_STATE_TYPE != -1) {
            dialog = DialogUtils.Dialogph(context, "数据加载中...");
            sendRequest();
        } else {
            tipView.setVisibility(View.VISIBLE);
            tipView.setTipView(TipView.TipStatus.NO_NET);
        }
    }

    // 点击事件监听
    private void initEvent() {
        rootView.findViewById(R.id.head_left_btn).setOnClickListener(this);// 返回
        rootView.findViewById(R.id.head_right_btn).setOnClickListener(this);// 播放专辑
    }

    // 初始化 ViewPager
    public void initViewPager() {
        ViewPager mPager = (ViewPager) rootView.findViewById(R.id.viewpager);
        mPager.setOffscreenPageLimit(1);
        ArrayList<Fragment> fragmentList = new ArrayList<>();
        detailsFragment = new DetailsFragment();// 专辑详情页
        programFragment = new ProgramListFragment();// 专辑列表页
        fragmentList.add(detailsFragment);
        fragmentList.add(programFragment);
        mPager.setAdapter(new MyFragmentChildPagerAdapter(getChildFragmentManager(), fragmentList));
        mPager.setOnPageChangeListener(this);// 页面变化时的监听器
        mPager.setCurrentItem(0);// 设置当前显示标签页为第一页 mPager
    }

    // 处理上一个界面传递过来的数据
    private void handleIntent() {
        String albumName;// 专辑名字
        RankInfo list;
        String type = getArguments().getString("type");
        if (type == null) return ;
        switch (type) {
            case "radiolistactivity":
                list = (RankInfo) getArguments().getSerializable("list");
                albumName = list.getContentName();
                albumId = list.getContentId();
                break;
            case "recommend":
                list = (RankInfo) getArguments().getSerializable("list");
                albumName = list.getContentName();
                albumId = list.getContentId();
                break;
            case "search":
                list = (RankInfo) getArguments().getSerializable("list");
                albumName = list.getContentName();
                albumId = list.getContentId();
                break;
            case "main":
                albumName = getArguments().getString("conentname");
                albumId = getArguments().getString("id");
                break;
            case "player":
                albumName = getArguments().getString("contentName");
                albumId = getArguments().getString("contentId");
                break;
            default:
                LanguageSearchInside inside = (LanguageSearchInside) getArguments().getSerializable("list");
                albumName = inside.getContentName();
                albumId = inside.getContentId();
                break;
        }
        if (albumName == null || albumName.equals("")) {
            albumName = "未知";
        }
        textAlbumName.setText(albumName);
    }

    // 获取 ID
    public static String getAlbumId() {
        return albumId;
    }

    // 获取网络数据
    private void sendRequest() {
        JSONObject jsonObject = VolleyRequest.getJsonObject(context);
        try {
            jsonObject.put("MediaType", StringConstant.TYPE_SEQU);
            jsonObject.put("ContentId", albumId);
            jsonObject.put("Page", "1");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        VolleyRequest.RequestPost(GlobalConfig.getContentById, tag, jsonObject, new VolleyCallback() {
            @Override
            protected void requestSuccess(JSONObject result) {
                if (dialog != null) dialog.dismiss();
                if (isCancelRequest) return;
                try {
                    String ReturnType = result.getString("ReturnType");
                    if (ReturnType != null && ReturnType.equals("1001")) {
                        resultInfo = new Gson().fromJson(result.getString("ResultInfo"), new TypeToken<ResultInfo>() {
                        }.getType());
                        L.i("TAG", resultInfo.toString());

                        if (resultInfo != null) {
                            initViewPager();
                            tipView.setVisibility(View.GONE);
                            relativeView.setVisibility(View.VISIBLE);
                            if (programFragment != null) {
                                String descn = resultInfo.getContentDescn();
                                String name = resultInfo.getContentName();
                                String img = resultInfo.getContentImg();
                                String id = resultInfo.getContentId();
                                programFragment.setInfo(descn, name, img, id);// 设置下载需要的信息

                                L.w("TAG", "img -- > " + img);
                            }
                        } else {
                            relativeView.setVisibility(View.GONE);
                            tipView.setVisibility(View.VISIBLE);
                            tipView.setTipView(TipView.TipStatus.NO_DATA, "数据君不翼而飞了\n点击界面会重新获取数据哟");
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    relativeView.setVisibility(View.GONE);
                    tipView.setVisibility(View.VISIBLE);
                    tipView.setTipView(TipView.TipStatus.IS_ERROR);
                }
            }

            @Override
            protected void requestError(VolleyError error) {
                if (dialog != null) dialog.dismiss();
                ToastUtils.showVolleyError(context);
                relativeView.setVisibility(View.GONE);
                tipView.setVisibility(View.VISIBLE);
                tipView.setTipView(TipView.TipStatus.IS_ERROR);
            }
        });
    }

    @Override
    public void onPageSelected(int arg0) {
        for (int i = 0; i < imageViews.length; i++) {
            imageViews[arg0].setBackgroundResource(R.mipmap.wt_image_album_page_indication_focused);
            if (arg0 != i) {
                imageViews[i].setBackgroundResource(R.mipmap.wt_image_album_page_indication);
            }
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        isCancelRequest = VolleyRequest.cancelRequest(tag);
    }

    @Override
    public void onPageScrolled(int arg0, float arg1, int arg2) {
    }

    @Override
    public void onPageScrollStateChanged(int arg0) {
    }

    @Override
    public void onWhiteViewClick() {
        if (GlobalConfig.CURRENT_NETWORK_STATE_TYPE != -1) {
            dialog = DialogUtils.Dialogph(context, "数据加载中...");
            sendRequest();
        } else {
            tipView.setVisibility(View.VISIBLE);
            tipView.setTipView(TipView.TipStatus.NO_NET);
        }
    }
}
