package com.wotingfm.ui.music.program.album.activity;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.android.volley.VolleyError;
import com.wotingfm.R;
import com.wotingfm.common.config.GlobalConfig;
import com.wotingfm.common.volley.VolleyCallback;
import com.wotingfm.common.volley.VolleyRequest;
import com.wotingfm.ui.baseadapter.MyFragmentChildPagerAdapter;
import com.wotingfm.ui.music.player.model.LanguageSearchInside;
import com.wotingfm.ui.music.program.album.fragment.DetailsFragment;
import com.wotingfm.ui.music.program.album.fragment.ProgramFragment;
import com.wotingfm.ui.music.program.album.model.ContentInfo;
import com.wotingfm.ui.music.program.album.model.ResultInfo;
import com.wotingfm.ui.music.program.fmlist.model.RankInfo;
import com.wotingfm.util.DialogUtils;
import com.wotingfm.util.L;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * 专辑页
 * @author 辛龙
 * 2016年4月1日
 */
public class AlbumActivity extends FragmentActivity implements OnClickListener, ViewPager.OnPageChangeListener {
    private AlbumActivity context;
    private ImageView[] imageViews;

    private ResultInfo resultInfo;// 获取的专辑信息

    private Dialog dialog;// 加载数据对话框

    private TextView textAlbumName;// 专辑名字

    private String contentDesc;// 专辑介绍  从上一个界面传递过来
    private String albumId;// 专辑 ID  从上一个界面传递过来
    private String tag = "ALBUM_VOLLEY_REQUEST_CANCEL_TAG";
    private boolean isCancelRequest;

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.head_left_btn: // 返回
                finish();
                break;
            case R.id.head_right_btn:// 播放专辑

                break;
        }
    }

    // 获取专辑介绍
    public String getContentDesc() {
        return contentDesc;
    }

    // 获取专辑信息
    public ResultInfo getResultInfo() {
        return resultInfo;
    }

    // 获取专辑列表
    public List<ContentInfo> getContentList() {
        if(resultInfo != null) return resultInfo.getSubList();
        else return null;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_album);
        context = this;

        initView();
        initEvent();
        initViewPager();
    }

    // 初始化视图
    private void initView() {
        LinearLayout viewLinear = (LinearLayout) findViewById(R.id.view_group);// viewGroup 添加小圆点
        imageViews = new ImageView[2];// 设置页面下标小红点
        for (int i = 0; i < 2; i++) {
            ImageView imageView = new ImageView(this);
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
        textAlbumName = (TextView) findViewById(R.id.head_name_tv);// 专辑名字
        handleIntent();

        dialog = DialogUtils.Dialogph(context, "数据加载中...");
        sendRequest();
    }

    // 点击事件监听
    private void initEvent() {
        findViewById(R.id.head_left_btn).setOnClickListener(this);// 返回
        findViewById(R.id.head_right_btn).setOnClickListener(this);// 播放专辑
    }

    // 初始化 ViewPager
    public void initViewPager() {
        ViewPager mPager = (ViewPager) findViewById(R.id.viewpager);
        mPager.setOffscreenPageLimit(1);
        ArrayList<Fragment> fragmentList = new ArrayList<>();
        DetailsFragment detailsFragment = new DetailsFragment();// 专辑详情页
        ProgramFragment programFragment = new ProgramFragment();// 专辑列表页
        fragmentList.add(detailsFragment);
        fragmentList.add(programFragment);
        mPager.setAdapter(new MyFragmentChildPagerAdapter(getSupportFragmentManager(), fragmentList));
        mPager.setOnPageChangeListener(this);// 页面变化时的监听器
        mPager.setCurrentItem(0);// 设置当前显示标签页为第一页 mPager
    }

    // 处理上一个界面传递过来的数据
    private void handleIntent() {
        Intent intent = getIntent();
        if(intent == null) return ;
        String albumName;// 专辑名字
        RankInfo list;
        String type = getIntent().getStringExtra("type");
        switch (type) {
            case "radiolistactivity":
                list = (RankInfo) getIntent().getSerializableExtra("list");
                albumName = list.getContentName();
                contentDesc = list.getContentDescn();
                albumId = list.getContentId();
                break;
            case "recommend":
                list = (RankInfo) getIntent().getSerializableExtra("list");
                albumName = list.getContentName();
                contentDesc = list.getContentDescn();
                albumId = list.getContentId();
                break;
            case "search":
                list = (RankInfo) getIntent().getSerializableExtra("list");
                albumName = list.getContentName();
                contentDesc = list.getContentDescn();
                albumId = list.getContentId();
                break;
            case "main":
                albumName = getIntent().getStringExtra("conentname");
                albumId = getIntent().getStringExtra("id");
                break;
            case "player":
                albumName = getIntent().getStringExtra("contentName");
                contentDesc = getIntent().getStringExtra("contentDesc");
                albumId = getIntent().getStringExtra("contentId");
                break;
            default:
                LanguageSearchInside inside = (LanguageSearchInside) getIntent().getSerializableExtra("list");
                albumName = inside.getContentName();
                contentDesc = inside.getContentDescn();
                albumId = inside.getContentId();
                break;
        }
        if (albumName == null || albumName.equals("")) {
            albumName = "未知";
        }
        textAlbumName.setText(albumName);
    }

    // 获取网络数据
    private void sendRequest() {
        JSONObject jsonObject = VolleyRequest.getJsonObject(context);
        try {
            jsonObject.put("MediaType", "SEQU");
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
                        String resultInfoString = result.getString("ResultInfo");
                        L.i("TAG", resultInfoString);
//                        resultInfo = new Gson().fromJson(, new TypeToken<ResultInfo>() {}.getType());
//                        L.i("TAG", resultInfo.toString());
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            protected void requestError(VolleyError error) {
                if (dialog != null) dialog.dismiss();
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
    protected void onDestroy() {
        super.onDestroy();
        isCancelRequest = VolleyRequest.cancelRequest(tag);
        context = null;
        setContentView(R.layout.activity_null);
    }

    @Override
    public void onPageScrolled(int arg0, float arg1, int arg2) {
    }

    @Override
    public void onPageScrollStateChanged(int arg0) {
    }
}
