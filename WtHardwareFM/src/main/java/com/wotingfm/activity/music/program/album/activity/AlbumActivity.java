package com.wotingfm.activity.music.program.album.activity;

import android.annotation.TargetApi;
import android.app.Dialog;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

import com.umeng.socialize.Config;
import com.umeng.socialize.ShareAction;
import com.umeng.socialize.UMShareAPI;
import com.umeng.socialize.bean.SHARE_MEDIA;
import com.umeng.socialize.media.UMImage;
import com.wotingfm.R;
import com.wotingfm.activity.common.baseadapter.MyFragmentPagerAdapter;
import com.wotingfm.activity.music.player.adapter.ImageAdapter;
import com.wotingfm.activity.music.player.model.LanguageSearchInside;
import com.wotingfm.activity.music.player.model.sharemodel;
import com.wotingfm.activity.music.program.album.fragment.DetailsFragment;
import com.wotingfm.activity.music.program.album.fragment.ProgramFragment;
import com.wotingfm.activity.music.program.fmlist.model.RankInfo;
import com.wotingfm.common.config.GlobalConfig;
import com.wotingfm.util.DialogUtils;
import com.wotingfm.util.ShareUtils;
import com.wotingfm.util.ToastUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * 专辑页
 * @author 辛龙
 * 2016年4月1日
 */
public class AlbumActivity extends FragmentActivity implements OnClickListener {
    private AlbumActivity context;
    
    public static String contentName;
    public static String contentDesc;
    public static String id;
    public static String contentImg;

    public static Dialog shareDialog;
    private Dialog dialog1;
    private TextView textAlbumName;
    private ImageView imageRight;
    private ViewPager mPager;

    @TargetApi(Build.VERSION_CODES.KITKAT)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_album);
        context = this;
        UMShareAPI.get(context);// 初始化友盟
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);// 透明状态栏
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);// 透明导航栏
        
        setView();            // 设置界面
        handleIntent();
        shareDialog();
    }

    private void setView() {
        findViewById(R.id.wt_back).setOnClickListener(context);
        textAlbumName = (TextView) findViewById(R.id.tv_head_name);
        imageRight = (ImageView) findViewById(R.id.img_head_right);
        imageRight.setOnClickListener(this);

        ArrayList<Fragment> fragmentList = new ArrayList<>();
        fragmentList.add(new DetailsFragment());
        fragmentList.add(new ProgramFragment());

        mPager = (ViewPager) findViewById(R.id.view_pager);
        mPager.setAdapter(new MyFragmentPagerAdapter(getSupportFragmentManager(), fragmentList));
//        mPager.setOnPageChangeListener(new MyOnPageChangeListener());
        mPager.setCurrentItem(0);
        mPager.setOffscreenPageLimit(1);
    }

    private void handleIntent() {
        String type = getIntent().getStringExtra("type");
        if (type != null && type.trim().equals("radiolistactivity")) {
            RankInfo list = (RankInfo) getIntent().getSerializableExtra("list");
            contentName = list.getContentName();
            contentDesc = list.getContentDesc();
            id = list.getContentId();
            contentImg = list.getContentImg();
        } else if (type != null && type.trim().equals("recommend")) {
            contentName = this.getIntent().getStringExtra("conentname");
            contentDesc = this.getIntent().getStringExtra("conentname");
            id = getIntent().getStringExtra("conentid");
            contentImg = getIntent().getStringExtra("contentimg");
        } else if (type != null && type.trim().equals("search")) {
            RankInfo list = (RankInfo) getIntent().getSerializableExtra("list");
            contentName = list.getContentName();
            contentDesc = list.getContentDesc();
            id = list.getContentId();
            contentImg = list.getContentImg();
        } else if (type != null && type.trim().equals("main")) {
            contentName = getIntent().getStringExtra("conentname");
            id = getIntent().getStringExtra("id");
        } else if (type != null && type.trim().equals("player")) {
            contentName = getIntent().getStringExtra("contentName");
            contentDesc = getIntent().getStringExtra("contentDesc");
            id = getIntent().getStringExtra("contentId");
            contentImg = getIntent().getStringExtra("contentImg");
        } else if (type != null && type.trim().equals("total")) {
            contentName = getIntent().getStringExtra("contentName");
            contentDesc = getIntent().getStringExtra("contentDesc");
            id = getIntent().getStringExtra("conentId");
            contentImg = getIntent().getStringExtra("contentImg");
        } else {
            LanguageSearchInside list = (LanguageSearchInside) getIntent().getSerializableExtra("list");
            contentName = list.getContentName();
            contentDesc = list.getContentDesc();
            id = list.getContentId();
        }
        if (contentName != null && !contentName.equals("")) {
            textAlbumName.setText(contentName);
        } else {
            textAlbumName.setText("未知");
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.wt_back:
                finish();
                break;
            case R.id.img_head_right:
                mPager.setCurrentItem(1);
                break;
        }
    }

    protected void callShare(SHARE_MEDIA Platform) {
        String shareName;
        String shareDesc;
        String shareContentImg;
        String shareUrl;
        UMImage image;

        if (GlobalConfig.playerobject != null) {
            if (GlobalConfig.playerobject.getContentName() != null && !GlobalConfig.playerobject.getContentName().equals("")) {
                shareName = GlobalConfig.playerobject.getContentName();
            } else {
                shareName = "我听我享听";
            }
            if (GlobalConfig.playerobject.getContentDesc() != null && !GlobalConfig.playerobject.getContentDesc().equals("")) {
                shareDesc = GlobalConfig.playerobject.getContentDesc();
            } else {
                shareDesc = "暂无本节目介绍";
            }
            if (GlobalConfig.playerobject.getContentImg() != null && !GlobalConfig.playerobject.getContentImg().equals("")) {
                shareContentImg = GlobalConfig.playerobject.getContentImg();
                image = new UMImage(context, shareContentImg);
            } else {
                shareContentImg = "http://182.92.175.134/img/logo-web.png";
                image = new UMImage(context, shareContentImg);
            }
            if (GlobalConfig.playerobject.getContentShareURL() != null && !GlobalConfig.playerobject.getContentShareURL().equals("")) {
                shareUrl = GlobalConfig.playerobject.getContentShareURL();
            } else {
                shareUrl = "http://www.wotingfm.com/";
            }
            new ShareAction(context).setPlatform(Platform).withMedia(image)
                    .withText(shareDesc).withTitle(shareName).withTargetUrl(shareUrl).share();
        } else {
            ToastUtils.show_short(context, "没有数据");
        }
    }

    private void shareDialog() {
        final View dialog = LayoutInflater.from(context).inflate(R.layout.dialog_sharedialog, null);
        GridView mGallery = (GridView) dialog.findViewById(R.id.share_gallery);

        shareDialog = new Dialog(context, R.style.MyDialog);
        shareDialog.setContentView(dialog);
        Window window = shareDialog.getWindow();
        DisplayMetrics dm = new DisplayMetrics();
        context.getWindowManager().getDefaultDisplay().getMetrics(dm);
        int scrEnw = dm.widthPixels;
        ViewGroup.LayoutParams params = dialog.getLayoutParams();
        params.width = scrEnw;
        dialog.setLayoutParams(params);
        window.setGravity(Gravity.BOTTOM);
        window.setWindowAnimations(R.style.sharestyle);
        shareDialog.setCanceledOnTouchOutside(true);
        shareDialog.getWindow().setBackgroundDrawableResource(R.color.dialog);
        dialog1 = DialogUtils.Dialogphnoshow(context, "通讯中", dialog1);
        Config.dialog = dialog1;

        final List<sharemodel> mList = ShareUtils.getShareModelList();
        mGallery.setAdapter(new ImageAdapter(context, mList));
        mGallery.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                SHARE_MEDIA Platform = mList.get(position).getSharePlatform();
                callShare(Platform);
                shareDialog.dismiss();
            }
        });

        dialog.findViewById(R.id.tv_cancle).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (shareDialog.isShowing()) {
                    shareDialog.dismiss();
                }
            }
        });
    }

    @Override
    public Resources getResources() {
        Resources res = super.getResources();
        Configuration config = new Configuration();
        config.setToDefaults();
        res.updateConfiguration(config, res.getDisplayMetrics());
        return res;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        textAlbumName = null;
        imageRight = null;
        mPager = null;
        setContentView(R.layout.activity_null);
    }
}
