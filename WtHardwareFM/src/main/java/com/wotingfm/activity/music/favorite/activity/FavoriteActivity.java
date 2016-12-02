package com.wotingfm.activity.music.favorite.activity;

import android.annotation.TargetApi;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.ImageView;
import android.widget.TextView;

import com.wotingfm.R;
import com.wotingfm.activity.common.baseadapter.MyFragmentPagerAdapter;
import com.wotingfm.activity.music.favorite.fragment.RadioFragment;
import com.wotingfm.activity.music.favorite.fragment.SequFragment;
import com.wotingfm.activity.music.favorite.fragment.SoundFragment;
import com.wotingfm.activity.music.favorite.fragment.TTSFragment;
import com.wotingfm.activity.music.favorite.fragment.TotalFragment;
import com.wotingfm.manager.MyActivityManager;
import com.wotingfm.util.PhoneMessage;
import com.wotingfm.util.ToastUtils;

import java.util.ArrayList;

/**
 * 我喜欢的
 */
public class FavoriteActivity extends FragmentActivity implements OnClickListener {
    private static FavoriteActivity context;
    private MyBroadcast mBroadcast;
    private TotalFragment totalFragment;
    private SequFragment sequfragment;
    private SoundFragment soundfragment;
    private RadioFragment radiofragment;
    private TTSFragment ttsfragment;


    private Dialog confirmDialog;
    private Dialog delDialog;
    private ImageView image;
    private ImageView imgQXuan;
    private static TextView tv_total;
    private static TextView tv_sequ;
    private static TextView tv_sound;
    private static TextView tv_radio;
    private static TextView tv_tts;
    private static ViewPager mPager;
    private static TextView tv_qingkong;
    private static TextView tv_bianji;// 加一个bol值，如果key值为0是为编辑状态，为1时显示完成

    private int bmpW;
    private int offset;
    private int dialogFlag = 0;// 编辑全选状态的变量0为未选中，1为选中
    private int textFlag = 0;// 标记右上角text的状态0为编辑，1为取消
    private static int lastIndex = -1;
    private static int currentIndex = 0;// 标记当前viewpager显示的页面
    public static final String VIEW_UPDATE = "VIEW_UPDATE";
    public static final String SET_ALL_IMAGE = "SET_ALL_IMAGE";// 全选
    public static final String SET_NOT_ALL_IMAGE = "SET_NOT_ALL_IMAGE";// 非全选
    public static final String SET_NOT_LOAD_REFRESH = "SET_NOT_LOAD_REFRESH";// 禁止刷新加载
    public static final String SET_LOAD_REFRESH = "SET_LOAD_REFRESH";// 允许刷新加载

    public static boolean isEdit = false;// 是否为编辑状态

    @TargetApi(Build.VERSION_CODES.KITKAT)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_favorite);
        context = this;
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);        // 透明状态栏
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);    // 透明导航栏
        MyActivityManager mam = MyActivityManager.getInstance();
        mam.pushOneActivity(context);

        // 注册广播
        mBroadcast = new MyBroadcast();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(FavoriteActivity.SET_ALL_IMAGE);
        intentFilter.addAction(FavoriteActivity.SET_NOT_ALL_IMAGE);
        registerReceiver(mBroadcast, intentFilter);

        setView();
        setListener();
        initImage();
        initViewPager();
        delDialog();
        confirmDialog();
    }

    // 初始化 ViewPager
    private void initViewPager() {
        ArrayList<Fragment> fragmentList = new ArrayList<>();
        totalFragment = new TotalFragment();
        sequfragment = new SequFragment();
        soundfragment = new SoundFragment();
        radiofragment = new RadioFragment();
        ttsfragment = new TTSFragment();
        fragmentList.add(totalFragment);
        fragmentList.add(sequfragment);
        fragmentList.add(soundfragment);
        fragmentList.add(radiofragment);
        fragmentList.add(ttsfragment);
        mPager.setAdapter(new MyFragmentPagerAdapter(getSupportFragmentManager(), fragmentList));
        mPager.setOnPageChangeListener(new MyOnPageChangeListener());    // 页面变化时的监听器
        mPager.setCurrentItem(0);                                        // 设置当前显示标签页为第
    }

    // 页面变化时的监听器
    public class MyOnPageChangeListener implements ViewPager.OnPageChangeListener {
        private int one = offset * 2 + bmpW;    // 两个相邻页面的偏移量
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
            image.startAnimation(animation);// 是用ImageView来显示动画的
            currentIndex = currIndex;
            if (lastIndex == -1) {
                lastIndex = currentIndex;
            } else {
                if (lastIndex != currentIndex) {
                    handledata(2);
                    handledata(4);
                    if (delDialog.isShowing()) {
                        delDialog.dismiss();
                        imgQXuan.setImageResource(R.mipmap.wt_group_nochecked);
                        dialogFlag = 0;
                    }
                    textFlag = 0;
                    tv_bianji.setText("编辑");
                    isEdit = false;
                    lastIndex = currentIndex;
                }
            }
            viewChange(currIndex);
        }
    }

    private void setListener() {
        findViewById(R.id.head_left_btn).setOnClickListener(this);// 返回按钮
        tv_total.setOnClickListener(new txListener(0));
        tv_sequ.setOnClickListener(new txListener(1));
        tv_sound.setOnClickListener(new txListener(2));
        tv_radio.setOnClickListener(new txListener(3));
        tv_tts.setOnClickListener(new txListener(4));

        tv_qingkong.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                handledata(0);
            }
        });

        tv_bianji.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (textFlag == 0) {
                    handledata(1);
                } else {
                    isEdit = false;
                    textFlag = 0;
                    tv_bianji.setText("编辑");
                    handledata(2);
                    if (delDialog.isShowing()) {
                        delDialog.dismiss();
                        imgQXuan.setImageResource(R.mipmap.wt_group_nochecked);
                        dialogFlag = 0;
                    }
                }
            }
        });
    }

    // 初始化视图
    private void setView() {
        tv_total = (TextView) findViewById(R.id.tv_total);// 全部
        tv_sequ = (TextView) findViewById(R.id.tv_sequ);// 专辑
        tv_sound = (TextView) findViewById(R.id.tv_sound);// 声音
        tv_radio = (TextView) findViewById(R.id.tv_radio);// 电台
        tv_tts = (TextView) findViewById(R.id.tv_tts);// TTS
        mPager = (ViewPager) findViewById(R.id.viewpager);
        mPager.setOffscreenPageLimit(1);
        tv_qingkong = (TextView) findViewById(R.id.tv_qingkong);
        tv_bianji = (TextView) findViewById(R.id.tv_bianji);
    }

    // TextView 事件监听
    public class txListener implements OnClickListener {
        private int index = 0;

        public txListener(int i) {
            index = i;
        }

        @Override
        public void onClick(View v) {
            mPager.setCurrentItem(index);
            currentIndex = index;
            if (lastIndex == -1) {
                lastIndex = currentIndex;
            } else {
                if (lastIndex != currentIndex) {
                    handledata(2);
                    handledata(4);
                    if (delDialog.isShowing()) {
                        delDialog.dismiss();
                        imgQXuan.setImageResource(R.mipmap.wt_group_nochecked);
                        dialogFlag = 0;
                    }
                    textFlag = 0;
                    tv_bianji.setText("编辑");
                    lastIndex = currentIndex;
                }
            }
            viewChange(index);
        }
    }

    public static void updateViewPager(String mediaType) {
        int index = 0;
        if (mediaType != null && !mediaType.equals("")) {
            if (mediaType.equals("SEQU")) {
                index = 1;
            } else if (mediaType.equals("AUDIO")) {
                index = 2;
            } else if (mediaType.equals("RADIO")) {
                index = 3;
            } else if (mediaType.equals("TTS")) {
                index = 4;
            } else {
                ToastUtils.show_always(context, "mediaType不属于已经分类的四种类型");
            }
            mPager.setCurrentItem(index);
            currentIndex = index;
            viewChange(index);
        } else {
            ToastUtils.show_always(context, "传进来的mediaType值为空");

        }
    }

    /**
     * 动态设置cursor的宽
     */
    public void initImage() {
        image = (ImageView) findViewById(R.id.cursor);
        ViewGroup.LayoutParams lp = image.getLayoutParams();
        lp.width = (PhoneMessage.ScreenWidth / 5);
        image.setLayoutParams(lp);
        bmpW = BitmapFactory.decodeResource(getResources(), R.mipmap.left_personal_bg).getWidth();
        DisplayMetrics dm = new DisplayMetrics();
        context.getWindowManager().getDefaultDisplay().getMetrics(dm);
        int screenW = dm.widthPixels;
        offset = (screenW / 5 - bmpW) / 2;
        // imageView设置平移，使下划线平移到初始位置（平移一个offset）
        Matrix matrix = new Matrix();
        matrix.postTranslate(offset, 0);
        image.setImageMatrix(matrix);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.head_left_btn:
                finish();
                break;
        }
    }

    // 四种参数 1为打开该界面的隐藏栏，0为收起隐藏栏，2为全选，3为取消全选
    private void handledata(int type) {
        if (currentIndex == 0) {
            // 全部 //1：先调total的查询全部方法 返回是否有值的弹窗
            int sum = totalFragment.getDelItemSum();
            if (type == 0) {
                if (sum != 0) {
                    confirmDialog.show();
                } else {
                    ToastUtils.show_always(context, "您还没有喜欢的数据");
                }
            }
        } else if (currentIndex == 1) {
            if (type == 1) {// 打开view
                boolean flag = sequfragment.changeviewtype(1);
                if (flag) {
                    isEdit = true;
                    sequfragment.setViewVisibility();
                    sendBroadcast(new Intent(FavoriteActivity.SET_NOT_LOAD_REFRESH));
                    textFlag = 1;
                    tv_bianji.setText("取消");
                    if (delDialog != null) {
                        delDialog.show();
                    }
                } else {
                    ToastUtils.show_always(context, "当前页无数据");
                }
            } else if (type == 2) {// 隐藏view
                sequfragment.changeviewtype(0);
                sequfragment.setViewHint();
                sendBroadcast(new Intent(FavoriteActivity.SET_LOAD_REFRESH));
            } else if (type == 3) {// 全选
                sequfragment.changechecktype(1);
            } else if (type == 4) {// 解除全选
                sequfragment.changechecktype(0);
            } else if (type == 5) {// 删除
                if (sequfragment.getdelitemsum() == 0) {
                    ToastUtils.show_always(context, "请选择您要删除的数据");
                    return;
                }
                if (delDialog.isShowing()) {
                    delDialog.dismiss();
                    imgQXuan.setImageResource(R.mipmap.wt_group_nochecked);
                    dialogFlag = 0;
                }
                textFlag = 0;
                tv_bianji.setText("编辑");
                sequfragment.delitem();
                sequfragment.setViewHint();
                sendBroadcast(new Intent(FavoriteActivity.SET_LOAD_REFRESH));
            }
        } else if (currentIndex == 2) {
            // 声音
            if (type == 1) {// 打开view
                boolean flag = soundfragment.changeviewtype(1);
                if (flag) {
                    isEdit = true;
                    soundfragment.setViewVisibility();
                    sendBroadcast(new Intent(FavoriteActivity.SET_NOT_LOAD_REFRESH));
                    textFlag = 1;
                    tv_bianji.setText("取消");
                    if (delDialog != null) {
                        delDialog.show();
                    }
                } else {
                    ToastUtils.show_always(context, "当前页无数据");
                }
            } else if (type == 2) {// 隐藏view
                soundfragment.changeviewtype(0);
                soundfragment.setViewHint();
                sendBroadcast(new Intent(FavoriteActivity.SET_LOAD_REFRESH));
            } else if (type == 3) {// 全选
                soundfragment.changechecktype(1);
            } else if (type == 4) {// 解除全选
                soundfragment.changechecktype(0);
            } else if (type == 5) {// 删除
                if (soundfragment.getdelitemsum() == 0) {
                    ToastUtils.show_always(context, "请选择您要删除的数据");
                    return;
                }
                if (delDialog.isShowing()) {
                    delDialog.dismiss();
                    imgQXuan.setImageResource(R.mipmap.wt_group_nochecked);
                    dialogFlag = 0;
                }
                textFlag = 0;
                tv_bianji.setText("编辑");
                soundfragment.delitem();
                soundfragment.setViewHint();
                sendBroadcast(new Intent(FavoriteActivity.SET_LOAD_REFRESH));
            }
        } else if (currentIndex == 3) {
            // 电台
            if (type == 1) {// 打开view
                boolean flag = radiofragment.changeviewtype(1);
                if (flag) {
                    isEdit = true;
                    radiofragment.setViewVisibility();
                    sendBroadcast(new Intent(FavoriteActivity.SET_NOT_LOAD_REFRESH));
                    textFlag = 1;
                    tv_bianji.setText("取消");
                    if (delDialog != null) {
                        delDialog.show();
                    }
                } else {
                    ToastUtils.show_always(context, "当前页无数据");
                }
            } else if (type == 2) {// 隐藏view
                radiofragment.changeviewtype(0);
                radiofragment.setViewHint();
                sendBroadcast(new Intent(FavoriteActivity.SET_LOAD_REFRESH));
            } else if (type == 3) {// 全选
                radiofragment.changechecktype(1);
            } else if (type == 4) {// 解除全选
                radiofragment.changechecktype(0);
            } else if (type == 5) {// 删除
                if (radiofragment.getdelitemsum() == 0) {
                    ToastUtils.show_always(context, "请选择您要删除的数据");
                    return;
                }
                if (delDialog.isShowing()) {
                    delDialog.dismiss();
                    imgQXuan.setImageResource(R.mipmap.wt_group_nochecked);
                    dialogFlag = 0;
                }
                textFlag = 0;
                tv_bianji.setText("编辑");
                radiofragment.delitem();
                radiofragment.setViewHint();
                sendBroadcast(new Intent(FavoriteActivity.SET_LOAD_REFRESH));
            }
        } else if (currentIndex == 4) {
            // TTS
            if (type == 1) {// 打开view
                boolean flag = ttsfragment.changeviewtype(1);
                if (flag) {
                    isEdit = true;
                    ttsfragment.setViewVisibility();
                    sendBroadcast(new Intent(FavoriteActivity.SET_NOT_LOAD_REFRESH));
                    textFlag = 1;
                    tv_bianji.setText("取消");
                    if (delDialog != null) {
                        delDialog.show();
                    }
                } else {
                    ToastUtils.show_always(context, "当前页无数据");
                }
            } else if (type == 2) {// 隐藏view
                ttsfragment.changeviewtype(0);
                ttsfragment.setViewHint();
                sendBroadcast(new Intent(FavoriteActivity.SET_LOAD_REFRESH));
            } else if (type == 3) {// 全选
                ttsfragment.changechecktype(1);
            } else if (type == 4) {// 解除全选
                ttsfragment.changechecktype(0);
            } else if (type == 5) {// 删除
                if (ttsfragment.getdelitemsum() == 0) {
                    ToastUtils.show_always(context, "请选择您要删除的数据");
                    return;
                }
                if (delDialog.isShowing()) {
                    delDialog.dismiss();
                    imgQXuan.setImageResource(R.mipmap.wt_group_nochecked);
                    dialogFlag = 0;
                }
                textFlag = 0;
                tv_bianji.setText("编辑");
                ttsfragment.delitem();
                ttsfragment.setViewHint();
                sendBroadcast(new Intent(FavoriteActivity.SET_LOAD_REFRESH));
            }
        }
    }

    // 界面更新
    public static void viewChange(int index) {
        if (index == 0) {
            tv_total.setTextColor(context.getResources().getColor(R.color.dinglan_orange));
            tv_sequ.setTextColor(context.getResources().getColor(R.color.group_item_text2));
            tv_sound.setTextColor(context.getResources().getColor(R.color.group_item_text2));
            tv_radio.setTextColor(context.getResources().getColor(R.color.group_item_text2));
            tv_tts.setTextColor(context.getResources().getColor(R.color.group_item_text2));
            tv_bianji.setVisibility(View.GONE);
            tv_qingkong.setVisibility(View.VISIBLE);
        } else if (index == 1) {
            tv_total.setTextColor(context.getResources().getColor(R.color.group_item_text2));
            tv_sequ.setTextColor(context.getResources().getColor(R.color.dinglan_orange));
            tv_sound.setTextColor(context.getResources().getColor(R.color.group_item_text2));
            tv_radio.setTextColor(context.getResources().getColor(R.color.group_item_text2));
            tv_tts.setTextColor(context.getResources().getColor(R.color.group_item_text2));
            tv_qingkong.setVisibility(View.GONE);
            tv_bianji.setVisibility(View.VISIBLE);
        } else if (index == 2) {
            tv_total.setTextColor(context.getResources().getColor(R.color.group_item_text2));
            tv_sequ.setTextColor(context.getResources().getColor(R.color.group_item_text2));
            tv_sound.setTextColor(context.getResources().getColor(R.color.dinglan_orange));
            tv_radio.setTextColor(context.getResources().getColor(R.color.group_item_text2));
            tv_tts.setTextColor(context.getResources().getColor(R.color.group_item_text2));
            tv_qingkong.setVisibility(View.GONE);
            tv_bianji.setVisibility(View.VISIBLE);
        } else if (index == 3) {
            tv_total.setTextColor(context.getResources().getColor(R.color.group_item_text2));
            tv_sequ.setTextColor(context.getResources().getColor(R.color.group_item_text2));
            tv_sound.setTextColor(context.getResources().getColor(R.color.group_item_text2));
            tv_radio.setTextColor(context.getResources().getColor(R.color.dinglan_orange));
            tv_tts.setTextColor(context.getResources().getColor(R.color.group_item_text2));
            tv_qingkong.setVisibility(View.GONE);
            tv_bianji.setVisibility(View.VISIBLE);
        } else if (index == 4) {
            tv_total.setTextColor(context.getResources().getColor(R.color.group_item_text2));
            tv_sequ.setTextColor(context.getResources().getColor(R.color.group_item_text2));
            tv_sound.setTextColor(context.getResources().getColor(R.color.group_item_text2));
            tv_radio.setTextColor(context.getResources().getColor(R.color.group_item_text2));
            tv_tts.setTextColor(context.getResources().getColor(R.color.dinglan_orange));
            tv_qingkong.setVisibility(View.GONE);
            tv_bianji.setVisibility(View.VISIBLE);
        }
    }

    // delDialog 初始化
    private void delDialog() {
        final View dialog = LayoutInflater.from(context).inflate(R.layout.dialog_fravorite, null);
        imgQXuan = (ImageView) dialog.findViewById(R.id.img_fravorite_quanxuan);
        delDialog = new Dialog(context, R.style.MyDialog_duijiang);
        delDialog.setContentView(dialog);
        Window window = delDialog.getWindow();
        DisplayMetrics dm = new DisplayMetrics();
        context.getWindowManager().getDefaultDisplay().getMetrics(dm);
        int scrEnw = dm.widthPixels;
        ViewGroup.LayoutParams params = dialog.getLayoutParams();
        params.width = scrEnw;
        dialog.setLayoutParams(params);
        window.setGravity(Gravity.BOTTOM);
        window.setWindowAnimations(R.style.sharestyle);
        window.setFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE, WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE);
        delDialog.setCanceledOnTouchOutside(false);

        // 全选
        dialog.findViewById(R.id.lin_favorite_quanxuan).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (dialogFlag == 0) {
                    imgQXuan.setImageResource(R.mipmap.wt_group_checked);
                    dialogFlag = 1;
                    handledata(3);
                } else {
                    imgQXuan.setImageResource(R.mipmap.wt_group_nochecked);
                    dialogFlag = 0;
                    handledata(4);
                }
            }
        });

        // 删除
        dialog.findViewById(R.id.lin_favorite_shanchu).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                handledata(5);
            }
        });
    }

    // confirmDialog 初始化
    private void confirmDialog() {
        final View dialog1 = LayoutInflater.from(context).inflate(R.layout.dialog_exit_confirm, null);
        TextView textTitle = (TextView) dialog1.findViewById(R.id.tv_title);
        textTitle.setText("是否删除所有的喜欢数据?");
        confirmDialog = new Dialog(context, R.style.MyDialog);
        confirmDialog.setContentView(dialog1);
        confirmDialog.setCanceledOnTouchOutside(false);
        confirmDialog.getWindow().setBackgroundDrawableResource(R.color.dialog);

        dialog1.findViewById(R.id.tv_cancle).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                confirmDialog.dismiss();
            }
        });

        dialog1.findViewById(R.id.tv_confirm).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                totalFragment.delItem();
                confirmDialog.dismiss();
            }
        });
    }

    // 广播接收  用于更新全选
    private class MyBroadcast extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(SET_ALL_IMAGE)) {
                imgQXuan.setImageResource(R.mipmap.wt_group_checked);
                dialogFlag = 1;
            } else if (intent.getAction().equals(SET_NOT_ALL_IMAGE)) {
                imgQXuan.setImageResource(R.mipmap.wt_group_nochecked);
                dialogFlag = 0;
            }
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (event.getAction() == KeyEvent.ACTION_DOWN && KeyEvent.KEYCODE_BACK == keyCode) {
            if (isEdit) {
                handledata(2);
                handledata(4);
                if (delDialog.isShowing()) {
                    delDialog.dismiss();
                    imgQXuan.setImageResource(R.mipmap.wt_group_nochecked);
                    dialogFlag = 0;
                }
                textFlag = 0;
                tv_bianji.setText("编辑");
                isEdit = false;
            } else {
                finish();
            }
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    // 设置android app 的字体大小不受系统字体大小改变的影响
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
        MyActivityManager mam = MyActivityManager.getInstance();
        mam.popOneActivity(context);
        unregisterReceiver(mBroadcast);
        image = null;
        tv_total = null;
        tv_sequ = null;
        tv_sound = null;
        tv_radio = null;
        tv_tts = null;
        tv_qingkong = null;
        tv_bianji = null;
        imgQXuan = null;
        mPager = null;
        delDialog = null;
        confirmDialog = null;
        totalFragment = null;
        sequfragment = null;
        soundfragment = null;
        radiofragment = null;
        context = null;
        setContentView(R.layout.activity_null);
    }
}
