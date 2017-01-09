package com.wotingfm.ui.music.program.album.activity;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Matrix;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.VolleyError;
import com.umeng.socialize.Config;
import com.umeng.socialize.ShareAction;
import com.umeng.socialize.UMShareAPI;
import com.umeng.socialize.UMShareListener;
import com.umeng.socialize.bean.SHARE_MEDIA;
import com.umeng.socialize.media.UMImage;
import com.wotingfm.R;
import com.wotingfm.ui.baseadapter.MyFragmentChildPagerAdapter;
import com.wotingfm.ui.music.comment.CommentActivity;
import com.wotingfm.ui.music.player.adapter.ImageAdapter;
import com.wotingfm.ui.music.player.model.LanguageSearchInside;
import com.wotingfm.ui.music.player.model.ShareModel;
import com.wotingfm.ui.music.program.album.fragment.DetailsFragment;
import com.wotingfm.ui.music.program.album.fragment.ProgramFragment;
import com.wotingfm.ui.music.program.fmlist.model.RankInfo;
import com.wotingfm.common.config.GlobalConfig;
import com.wotingfm.common.volley.VolleyCallback;
import com.wotingfm.common.volley.VolleyRequest;
import com.wotingfm.util.CommonUtils;
import com.wotingfm.util.DialogUtils;
import com.wotingfm.util.PhoneMessage;
import com.wotingfm.util.ShareUtils;
import com.wotingfm.util.ToastUtils;
import com.wotingfm.widget.HorizontalListView;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * 专辑页
 * @author 辛龙
 *         2016年4月1日
 */
public class AlbumActivity extends FragmentActivity implements OnClickListener {
    private AlbumActivity context;
    private String RadioName;
    public static TextView tv_album_name;
    public static ImageView img_album;
    public static String ContentDesc;
    public static String ContentImg;
    public static String ContentShareURL;
    public static String ContentName;
    public static String id;
    public static int returnResult = -1;        // =1说明信息获取正常，returntype=1001
    public static String ContentFavorite;        // 从网络获取的当前值，如果为空，表示页面并未获取到此值
    public static TextView tv_favorite;
    private LinearLayout head_left;
    private LinearLayout lin_share;
    private LinearLayout lin_favorite;
    private LinearLayout lin_pinglun;
    private ViewPager mPager;
    private Dialog dialog;
    private Dialog shareDialog;
    private Dialog dialog1;
    private UMImage image;

    protected Fragment mFragmentContent;        // 上一个Fragment
    private TextView textDetails, textProgram;    // text_details text_program
    private ImageView imageCursor;                //cursor
    private int offset;                        // 图片移动的偏移量
    private int screenWidth;
    private boolean isCancelRequest;
    public static ImageView imageFavorite;
    private String tag = "ALBUM_VOLLEY_REQUEST_CANCEL_TAG";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_album);
        context = this;
        setView();            // 设置界面
        setListener();
        InitImage();
        InitViewPager();
        handleIntent();
        shareDialog();        // 分享dialog
    }

    private void setView() {
        tv_album_name = (TextView) findViewById(R.id.head_name_tv);
        img_album = (ImageView) findViewById(R.id.img_album);
        imageFavorite = (ImageView) findViewById(R.id.img_favorite);
        head_left = (LinearLayout) findViewById(R.id.head_left_btn);    // 返回按钮
        lin_share = (LinearLayout) findViewById(R.id.lin_share);        // 分享按钮
        lin_pinglun = (LinearLayout) findViewById(R.id.lin_pinglun);    // 评论
        lin_favorite = (LinearLayout) findViewById(R.id.lin_favorite);    // 喜欢按钮
        tv_favorite = (TextView) findViewById(R.id.tv_favorite);        // tv_favorite
        textDetails = (TextView) findViewById(R.id.text_details);        // 专辑详情
        textDetails.setOnClickListener(this);
        textProgram = (TextView) findViewById(R.id.text_program);        // 专辑列表
        textProgram.setOnClickListener(this);
    }

    private void setListener() {
        head_left.setOnClickListener(this);
        lin_share.setOnClickListener(this);
        lin_favorite.setOnClickListener(this);
        lin_pinglun.setOnClickListener(this);
    }

    /**
     * 设置cursor的宽
     */
    public void InitImage() {
        imageCursor = (ImageView) findViewById(R.id.cursor);
        ViewGroup.LayoutParams lp = imageCursor.getLayoutParams();
        lp.width = PhoneMessage.ScreenWidth / 2;
        imageCursor.setLayoutParams(lp);
        offset = PhoneMessage.ScreenWidth / 2;
        // imageView设置平移，使下划线平移到初始位置（平移一个offset）
        Matrix matrix = new Matrix();
        matrix.postTranslate(offset, 0);
        imageCursor.setImageMatrix(matrix);
    }

    /**
     * 初始化ViewPager
     */
    public void InitViewPager() {
        mPager = (ViewPager) findViewById(R.id.viewpager);
        mPager.setOffscreenPageLimit(1);
        ArrayList<Fragment> fragmentList = new ArrayList<Fragment>();
        DetailsFragment detailsFragment = new DetailsFragment();//专辑详情页
        ProgramFragment programFragment = new ProgramFragment();//专辑列表页
        fragmentList.add(detailsFragment);
        fragmentList.add(programFragment);
        mPager.setAdapter(new MyFragmentChildPagerAdapter(getSupportFragmentManager(), fragmentList));
        mPager.setOnPageChangeListener(new MyOnPageChangeListener());// 页面变化时的监听器
        mPager.setCurrentItem(0);// 设置当前显示标签页为第一页mPager
    }

    public class MyOnPageChangeListener implements ViewPager.OnPageChangeListener {
        private int one = offset;// 两个相邻页面的偏移量
        private int currIndex;
        @Override
        public void onPageScrolled(int arg0, float arg1, int arg2) {
        }

        @Override
        public void onPageScrollStateChanged(int arg0) {
        }

        @Override
        public void onPageSelected(int arg0) {
            Animation animation = new TranslateAnimation(currIndex * one, arg0 * one, 0, 0);// 平移动画
            currIndex = arg0;
            animation.setFillAfter(true);// 动画终止时停留在最后一帧，不然会回到没有执行前的状态
            animation.setDuration(200);// 动画持续时间0.2秒
            imageCursor.startAnimation(animation);// 是用ImageView来显示动画的
            if (arg0 == 0) {
                textDetails.setTextColor(context.getResources().getColor(R.color.dinglan_orange));
                textProgram.setTextColor(context.getResources().getColor(R.color.group_item_text2));// 全部
            } else if (arg0 == 1) {        // 专辑
                textProgram.setTextColor(context.getResources().getColor(R.color.dinglan_orange));
                textDetails.setTextColor(context.getResources().getColor(R.color.group_item_text2));
            }
        }
    }

    private void shareDialog() {
        final View dialog = LayoutInflater.from(context).inflate(R.layout.dialog_sharedialog, null);
        HorizontalListView mGallery = (HorizontalListView) dialog.findViewById(R.id.share_gallery);
        TextView tv_cancel = (TextView) dialog.findViewById(R.id.tv_cancle);
        shareDialog = new Dialog(context, R.style.MyDialog);
        // 从底部上升到一个位置
        shareDialog.setContentView(dialog);
        Window window = shareDialog.getWindow();
        DisplayMetrics dm = new DisplayMetrics();
        context.getWindowManager().getDefaultDisplay().getMetrics(dm);
        screenWidth = dm.widthPixels;
        ViewGroup.LayoutParams params = dialog.getLayoutParams();
        params.width = screenWidth;
        dialog.setLayoutParams(params);
        window.setGravity(Gravity.BOTTOM);
        window.setWindowAnimations(R.style.sharestyle);
        shareDialog.setCanceledOnTouchOutside(true);
        shareDialog.getWindow().setBackgroundDrawableResource(R.color.dialog);
        final List<ShareModel> mList = ShareUtils.getShareModelList();
        ImageAdapter shareAdapter = new ImageAdapter(context, mList);
        mGallery.setAdapter(shareAdapter);
        dialog1 = DialogUtils.Dialogphnoshow(context, "通讯中", dialog1);
        Config.dialog = dialog1;
        mGallery.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                SHARE_MEDIA Platform = mList.get(position).getSharePlatform();
                CallShare(Platform);
            }
        });

        tv_cancel.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                shareDialog.dismiss();
            }
        });
    }

    protected void CallShare(SHARE_MEDIA Platform) {
        if (returnResult == 1) {// 此处需从服务器获取分享所需要的信息，拿到字段后进行处理
            String shareName;
            String shareDesc;
            String shareContentImg;
            String shareUrl;
            if (ContentName != null && !ContentName.equals("")) {
                shareName = ContentName;
            } else {
                shareName = "我听我享听";
            }
            if (ContentDesc != null && !ContentDesc.equals("")) {
                shareDesc = ContentDesc;
            } else {
                shareDesc = "暂无本节目介绍";
            }
            if (ContentImg != null && !ContentImg.equals("")) {
                shareContentImg = ContentImg;
                image = new UMImage(context, shareContentImg);
            } else {
                shareContentImg = "http://182.92.175.134/img/logo-web.png";
                image = new UMImage(context, shareContentImg);
            }
            if (ContentShareURL != null && !ContentShareURL.equals("")) {
                shareUrl = ContentShareURL;
            } else {
                shareUrl = "http://www.wotingfm.com/";
            }
            dialog1 = DialogUtils.Dialogph(context, "分享中");
            Config.dialog = dialog1;
            new ShareAction(context).setPlatform(Platform).setCallback(umShareListener).withMedia(image)
                    .withText(shareDesc).withTitle(shareName).withTargetUrl(shareUrl).share();
        } else {
            ToastUtils.show_always(context, "分享失败，请稍后再试！");
        }
    }

    private UMShareListener umShareListener = new UMShareListener() {

        @Override
        public void onResult(SHARE_MEDIA platform) {
            Log.d("plat", "platform" + platform);
            Toast.makeText(context, platform + " 分享成功啦", Toast.LENGTH_SHORT).show();
            shareDialog.dismiss();
        }

        @Override
        public void onError(SHARE_MEDIA platform, Throwable t) {
            Toast.makeText(context, platform + " 分享失败啦", Toast.LENGTH_SHORT).show();
            shareDialog.dismiss();
        }

        @Override
        public void onCancel(SHARE_MEDIA platform) {
            ToastUtils.show_always(context, "用户退出认证");
            shareDialog.dismiss();
        }
    };


    private void handleIntent() {
        String type = this.getIntent().getStringExtra("type");
        if (type != null && type.trim().equals("radiolistactivity")) {
            RankInfo list = (RankInfo) getIntent().getSerializableExtra("list");
            RadioName = list.getContentName();
            ContentDesc = list.getContentDescn();
            id = list.getContentId();
        } else if (type != null && type.trim().equals("recommend")) {
            RankInfo list = (RankInfo) getIntent().getSerializableExtra("list");
            RadioName = list.getContentName();
            ContentDesc = list.getContentDescn();
            id = list.getContentId();
        } else if (type != null && type.trim().equals("search")) {
            RankInfo list = (RankInfo) getIntent().getSerializableExtra("list");
            RadioName = list.getContentName();
            ContentDesc = list.getContentDescn();
            id = list.getContentId();
        } else if (type != null && type.trim().equals("main")) {
            // 再做一个
            RadioName = this.getIntent().getStringExtra("conentname");
            id = this.getIntent().getStringExtra("id");
        } else if (type != null && type.trim().equals("player")) {
            // 再做一个
            RadioName = this.getIntent().getStringExtra("contentName");
            ContentDesc = this.getIntent().getStringExtra("contentDesc");
            id = this.getIntent().getStringExtra("contentId");
        } else {
            LanguageSearchInside list = (LanguageSearchInside) getIntent().getSerializableExtra("list");
            RadioName = list.getContentName();
            ContentDesc = list.getContentDescn();
            id = list.getContentId();
        }
        if (RadioName != null && !RadioName.equals("")) {
            tv_album_name.setText(RadioName);
        } else {
            tv_album_name.setText("未知");
        }
        Log.e("本节目的专辑ID为", id + "");
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.head_left_btn: // 左上角返回键
                finish();
                break;
            case R.id.lin_share: // 分享
                shareDialog.show();
                break;
            case R.id.lin_favorite: // 喜欢
                if (ContentFavorite != null && !ContentFavorite.equals("")) {
                    if (GlobalConfig.CURRENT_NETWORK_STATE_TYPE != -1) {
                        dialog = DialogUtils.Dialogph(context, "正在获取数据");
                        sendFavorite();
                    } else {
                        ToastUtils.show_always(context, "网络失败，请检查网络");
                    }
                } else {
                    ToastUtils.show_always(context, "数据出错了请稍后再试！");
                }
                break;
            case R.id.text_details: // 详情
                mPager.setCurrentItem(0);
                textDetails.setTextColor(context.getResources().getColor(R.color.dinglan_orange));
                textProgram.setTextColor(context.getResources().getColor(R.color.group_item_text2));
                break;
            case R.id.text_program: // 列表
                mPager.setCurrentItem(1);
                textProgram.setTextColor(context.getResources().getColor(R.color.dinglan_orange));
                textDetails.setTextColor(context.getResources().getColor(R.color.group_item_text2));
                break;
            case R.id.lin_pinglun: // 评论
                if(!TextUtils.isEmpty(id)){
                    if(CommonUtils.getUserIdNoImei(context)!=null&&!CommonUtils.getUserIdNoImei(context).equals("")){
                        Intent intent=new Intent(context, CommentActivity.class);
                        intent.putExtra("contentId",GlobalConfig.playerObject.getContentId());
                        intent.putExtra("MediaType","SEQU");
                        startActivity(intent);
                    }else{
                        ToastUtils.show_always(context,"请先登录~~");
                    }
                }else{
                    ToastUtils.show_always(context,"当前播放的节目的信息有误，无法获取评论列表");
                }
                break;
        }
    }

    /**
     * 发送网络请求  获取喜欢数据
     */
    private void sendFavorite() {
        JSONObject jsonObject = VolleyRequest.getJsonObject(context);
        try {
            jsonObject.put("MediaType", "SEQU");
            jsonObject.put("ContentId", id);
            if (ContentFavorite.equals("0")) {
                jsonObject.put("Flag", "1");
            } else {
                jsonObject.put("Flag", "0");
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        VolleyRequest.RequestPost(GlobalConfig.clickFavoriteUrl, tag, jsonObject, new VolleyCallback() {
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
                try {
                    ReturnType = result.getString("ReturnType");
                    Message = result.getString("Message");
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                // 根据返回值来对程序进行解析
                if (ReturnType != null) {
                    if (ReturnType.equals("1001")) {
                        if (ContentFavorite.equals("0")) {
                            ContentFavorite = "1";
                            tv_favorite.setText("已喜欢");
                            imageFavorite.setImageDrawable(getResources().getDrawable(R.mipmap.wt_img_liked));
                        } else {
                            ContentFavorite = "0";
                            tv_favorite.setText("喜欢");
                            imageFavorite.setImageDrawable(getResources().getDrawable(R.mipmap.wt_img_like));
                        }
                    } else if (ReturnType.equals("0000")) {
                        ToastUtils.show_always(context, "无法获取相关的参数");
                    } else if (ReturnType.equals("1002")) {
                        ToastUtils.show_always(context, "无法获得内容类别");
                    } else if (ReturnType.equals("1003")) {
                        ToastUtils.show_always(context, "无法获得内容Id");
                    } else if (ReturnType.equals("1004")) {
                        ToastUtils.show_always(context, "所指定的节目不存在");
                    } else if (ReturnType.equals("1005")) {
                        ToastUtils.show_always(context, "已经喜欢了此内容");
                    } else if (ReturnType.equals("1006")) {
                        ToastUtils.show_always(context, "还未喜欢此内容");
                    } else if (ReturnType.equals("T")) {
                        ToastUtils.show_always(context, "获取列表异常");
                    } else {
                        ToastUtils.show_always(context, Message + "");
                    }
                } else {
                    ToastUtils.show_always(context, "ReturnType==null");
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        UMShareAPI.get(context).onActivityResult(requestCode, resultCode, data);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        isCancelRequest = VolleyRequest.cancelRequest(tag);
        context = null;
        RadioName = null;
        tv_album_name = null;
        img_album = null;
        ContentDesc = null;
        ContentImg = null;
        ContentShareURL = null;
        ContentName = null;
        id = null;
        ContentFavorite = null;
        tv_favorite = null;
        head_left = null;
        lin_share = null;
        lin_favorite = null;
        dialog = null;
        shareDialog = null;
        dialog1 = null;
        image = null;
        mFragmentContent = null;
        textDetails = null;
        textProgram = null;
        imageCursor = null;
        setContentView(R.layout.activity_null);
    }
}
