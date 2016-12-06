package com.wotingfm.activity.music.favorite.activity;

import android.annotation.TargetApi;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
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
import com.wotingfm.activity.common.baseactivity.AppBaseFragmentActivity;
import com.wotingfm.activity.common.baseadapter.MyFragmentPagerAdapter;
import com.wotingfm.activity.music.favorite.fragment.RadioFragment;
import com.wotingfm.activity.music.favorite.fragment.SequFragment;
import com.wotingfm.activity.music.favorite.fragment.SoundFragment;
import com.wotingfm.activity.music.favorite.fragment.TTSFragment;
import com.wotingfm.activity.music.favorite.fragment.TotalFragment;
import com.wotingfm.common.constant.BroadcastConstants;
import com.wotingfm.util.BitmapUtils;
import com.wotingfm.util.PhoneMessage;
import com.wotingfm.util.ToastUtils;

import java.util.ArrayList;

/**
 * 我喜欢的
 */
public class FavoriteActivity extends AppBaseFragmentActivity implements OnClickListener {
    private static FavoriteActivity context;
    private MyBroadcast mBroadcast;
    private TotalFragment totalFragment;
    private SequFragment sequFragment;
    private SoundFragment soundFragment;
    private RadioFragment radioFragment;
    private TTSFragment ttsFragment;

    private Dialog confirmDialog;
    private Dialog delDialog;
    private ImageView image;
    private ImageView imageSelectAll;// 全选
    private static TextView textTotal;// 全部
    private static TextView textSequ;// 专辑
    private static TextView textSound;// 声音
    private static TextView textRadio;// 电台
    private static TextView textTts;// TTS
    private static ViewPager mPager;
    private static TextView textEmpty ;// 清空
    private static TextView textEditor;// 加一个 bol 值，如果 key 值为 0 是为编辑状态，为 1 时显示完成

    private int bmpW;
    private int offset;
    private int dialogFlag = 0;// 编辑全选状态的变量 0 为未选中，1 为选中
    private int textFlag = 0;// 标记右上角 text 的状态 0 为编辑，1 为取消
    private static int lastIndex = -1;
    private static int currentIndex = 0;// 标记当前 viewpager 显示的页面
    public static boolean isEdit = false;// 是否为编辑状态

    @TargetApi(Build.VERSION_CODES.KITKAT)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_favorite);
        context = this;

        // 注册广播
        mBroadcast = new MyBroadcast();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BroadcastConstants.SET_ALL_IMAGE);
        intentFilter.addAction(BroadcastConstants.SET_NOT_ALL_IMAGE);
        registerReceiver(mBroadcast, intentFilter);

        initImage();
        initViews();
        initViewPager();
        initDialog();
    }

    // 初始化视图
    private void initViews() {
        findViewById(R.id.head_left_btn).setOnClickListener(this);// 返回按钮

        textTotal = (TextView) findViewById(R.id.tv_total);// 全部
        textTotal.setOnClickListener(new txListener(0));

        textSequ = (TextView) findViewById(R.id.tv_sequ);// 专辑
        textSequ.setOnClickListener(new txListener(1));

        textSound = (TextView) findViewById(R.id.tv_sound);// 声音
        textSound.setOnClickListener(new txListener(2));

        textRadio = (TextView) findViewById(R.id.tv_radio);// 电台
        textRadio.setOnClickListener(new txListener(3));

        textTts = (TextView) findViewById(R.id.tv_tts);// TTS
        textTts.setOnClickListener(new txListener(4));

        textEmpty = (TextView) findViewById(R.id.tv_qingkong);// 清空
        textEmpty.setOnClickListener(this);

        textEditor = (TextView) findViewById(R.id.tv_bianji);// 编辑
        textEditor.setOnClickListener(this);

        mPager = (ViewPager) findViewById(R.id.viewpager);
        mPager.setOffscreenPageLimit(1);
    }

    // 初始化 ViewPager
    private void initViewPager() {
        ArrayList<Fragment> fragmentList = new ArrayList<>();
        fragmentList.add(totalFragment = new TotalFragment());
        fragmentList.add(sequFragment = new SequFragment());
        fragmentList.add(soundFragment = new SoundFragment());
        fragmentList.add(radioFragment = new RadioFragment());
        fragmentList.add(ttsFragment = new TTSFragment());
        mPager.setAdapter(new MyFragmentPagerAdapter(getSupportFragmentManager(), fragmentList));
        mPager.setOnPageChangeListener(new MyOnPageChangeListener());
        mPager.setCurrentItem(0);// 设置当前显示标签页
    }

    // 页面变化时的监听器
    public class MyOnPageChangeListener implements ViewPager.OnPageChangeListener {
        private int one = offset * 2 + bmpW;// 两个相邻页面的偏移量
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
            animation.setDuration(200);// 动画持续时间 0.2 秒
            image.startAnimation(animation);// 是用 ImageView 来显示动画的
            currentIndex = currIndex;
            if (lastIndex == -1) {
                lastIndex = currentIndex;
            } else {
                if (lastIndex != currentIndex) {
                    handleData(2);
                    handleData(4);
                    if (delDialog.isShowing()) {
                        delDialog.dismiss();
                        imageSelectAll.setImageBitmap(BitmapUtils.readBitMap(context, R.mipmap.wt_group_nochecked));
                        dialogFlag = 0;
                    }
                    textFlag = 0;
                    textEditor.setText("编辑");
                    isEdit = false;
                    lastIndex = currentIndex;
                }
            }
            viewChange(currIndex);
        }
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
                    handleData(2);
                    handleData(4);
                    if (delDialog.isShowing()) {
                        delDialog.dismiss();
                        imageSelectAll.setImageBitmap(BitmapUtils.readBitMap(context, R.mipmap.wt_group_nochecked));
                        dialogFlag = 0;
                    }
                    textFlag = 0;
                    textEditor.setText("编辑");
                    lastIndex = currentIndex;
                }
            }
            viewChange(index);
        }
    }

    public static void updateViewPager(String mediaType) {
        if(mediaType == null || mediaType.equals("")) return ;
        int index;
        switch (mediaType) {
            case "SEQU":
                index = 1;
                break;
            case "AUDIO":
                index = 2;
                break;
            case "RADIO":
                index = 3;
                break;
            case "TTS":
                index = 4;
                break;
            default:
                index = 2;
                break;
        }
        mPager.setCurrentItem(index);
        currentIndex = index;
        viewChange(index);
    }

    // 动态设置 cursor 的宽
    private void initImage() {
        image = (ImageView) findViewById(R.id.cursor);
        ViewGroup.LayoutParams lp = image.getLayoutParams();
        lp.width = (PhoneMessage.ScreenWidth / 5);
        image.setLayoutParams(lp);
        bmpW = BitmapFactory.decodeResource(getResources(), R.mipmap.left_personal_bg).getWidth();
        DisplayMetrics dm = new DisplayMetrics();
        context.getWindowManager().getDefaultDisplay().getMetrics(dm);
        int screenW = dm.widthPixels;
        offset = (screenW / 5 - bmpW) / 2;
        // imageView 设置平移，使下划线平移到初始位置（平移一个 offset）
        Matrix matrix = new Matrix();
        matrix.postTranslate(offset, 0);
        image.setImageMatrix(matrix);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.head_left_btn:// 返回
                finish();
                break;
            case R.id.lin_favorite_shanchu:// 删除
                handleData(5);
                break;
            case R.id.lin_favorite_quanxuan:// 全选
                if (dialogFlag == 0) {
                    imageSelectAll.setImageBitmap(BitmapUtils.readBitMap(context, R.mipmap.wt_group_checked));
                    dialogFlag = 1;
                    handleData(3);
                } else {
                    imageSelectAll.setImageBitmap(BitmapUtils.readBitMap(context, R.mipmap.wt_group_nochecked));
                    dialogFlag = 0;
                    handleData(4);
                }
                break;
            case R.id.tv_cancle:// 取消删除
                confirmDialog.dismiss();
                break;
            case R.id.tv_confirm:// 确定删除
                totalFragment.delItem();
                confirmDialog.dismiss();
                break;
            case R.id.tv_qingkong:// 清空
                handleData(0);
                break;
            case R.id.tv_bianji:// 编辑
                if (textFlag == 0) {
                    handleData(1);
                } else {
                    isEdit = false;
                    textFlag = 0;
                    textEditor.setText("编辑");
                    handleData(2);
                    if (delDialog.isShowing()) {
                        delDialog.dismiss();
                        imageSelectAll.setImageBitmap(BitmapUtils.readBitMap(context, R.mipmap.wt_group_nochecked));
                        dialogFlag = 0;
                    }
                }
                break;
        }
    }

    // 四种参数 1 为打开该界面的隐藏栏，0 为收起隐藏栏，2 为全选，3 为取消全选
    private void handleData(int type) {
        if (currentIndex == 0) {
            // 全部 //1：先调 total 的查询全部方法 返回是否有值的弹窗
            int sum = totalFragment.getDelItemSum();
            if (type == 0) {
                if (sum != 0) {
                    confirmDialog.show();
                } else {
                    ToastUtils.show_always(context, "您还没有喜欢的数据");
                }
            }
        } else if (currentIndex == 1) {
            if (type == 1) {// 打开 view
                boolean flag = sequFragment.changeviewtype(1);
                if (flag) {
                    isEdit = true;
                    sequFragment.setViewVisibility();
                    sendBroadcast(new Intent(BroadcastConstants.SET_NOT_LOAD_REFRESH));
                    textFlag = 1;
                    textEditor.setText("取消");
                    if (delDialog != null) {
                        delDialog.show();
                    }
                } else {
                    ToastUtils.show_always(context, "当前页无数据");
                }
            } else if (type == 2) {// 隐藏 view
                sequFragment.changeviewtype(0);
                sequFragment.setViewHint();
                sendBroadcast(new Intent(BroadcastConstants.SET_LOAD_REFRESH));
            } else if (type == 3) {// 全选
                sequFragment.changechecktype(1);
            } else if (type == 4) {// 解除全选
                sequFragment.changechecktype(0);
            } else if (type == 5) {// 删除
                if (sequFragment.getdelitemsum() == 0) {
                    ToastUtils.show_always(context, "请选择您要删除的数据");
                    return;
                }
                if (delDialog.isShowing()) {
                    delDialog.dismiss();
                    imageSelectAll.setImageBitmap(BitmapUtils.readBitMap(context, R.mipmap.wt_group_nochecked));
                    dialogFlag = 0;
                }
                textFlag = 0;
                textEditor.setText("编辑");
                sequFragment.delitem();
                sequFragment.setViewHint();
                sendBroadcast(new Intent(BroadcastConstants.SET_LOAD_REFRESH));
            }
        } else if (currentIndex == 2) {
            // 声音
            if (type == 1) {// 打开 view
                boolean flag = soundFragment.changeviewtype(1);
                if (flag) {
                    isEdit = true;
                    soundFragment.setViewVisibility();
                    sendBroadcast(new Intent(BroadcastConstants.SET_NOT_LOAD_REFRESH));
                    textFlag = 1;
                    textEditor.setText("取消");
                    if (delDialog != null) {
                        delDialog.show();
                    }
                } else {
                    ToastUtils.show_always(context, "当前页无数据");
                }
            } else if (type == 2) {// 隐藏 view
                soundFragment.changeviewtype(0);
                soundFragment.setViewHint();
                sendBroadcast(new Intent(BroadcastConstants.SET_LOAD_REFRESH));
            } else if (type == 3) {// 全选
                soundFragment.changechecktype(1);
            } else if (type == 4) {// 解除全选
                soundFragment.changechecktype(0);
            } else if (type == 5) {// 删除
                if (soundFragment.getdelitemsum() == 0) {
                    ToastUtils.show_always(context, "请选择您要删除的数据");
                    return;
                }
                if (delDialog.isShowing()) {
                    delDialog.dismiss();
                    imageSelectAll.setImageBitmap(BitmapUtils.readBitMap(context, R.mipmap.wt_group_nochecked));
                    dialogFlag = 0;
                }
                textFlag = 0;
                textEditor.setText("编辑");
                soundFragment.delitem();
                soundFragment.setViewHint();
                sendBroadcast(new Intent(BroadcastConstants.SET_LOAD_REFRESH));
            }
        } else if (currentIndex == 3) {
            // 电台
            if (type == 1) {// 打开 view
                boolean flag = radioFragment.changeviewtype(1);
                if (flag) {
                    isEdit = true;
                    radioFragment.setViewVisibility();
                    sendBroadcast(new Intent(BroadcastConstants.SET_NOT_LOAD_REFRESH));
                    textFlag = 1;
                    textEditor.setText("取消");
                    if (delDialog != null) {
                        delDialog.show();
                    }
                } else {
                    ToastUtils.show_always(context, "当前页无数据");
                }
            } else if (type == 2) {// 隐藏 view
                radioFragment.changeviewtype(0);
                radioFragment.setViewHint();
                sendBroadcast(new Intent(BroadcastConstants.SET_LOAD_REFRESH));
            } else if (type == 3) {// 全选
                radioFragment.changechecktype(1);
            } else if (type == 4) {// 解除全选
                radioFragment.changechecktype(0);
            } else if (type == 5) {// 删除
                if (radioFragment.getdelitemsum() == 0) {
                    ToastUtils.show_always(context, "请选择您要删除的数据");
                    return;
                }
                if (delDialog.isShowing()) {
                    delDialog.dismiss();
                    imageSelectAll.setImageBitmap(BitmapUtils.readBitMap(context, R.mipmap.wt_group_nochecked));
                    dialogFlag = 0;
                }
                textFlag = 0;
                textEditor.setText("编辑");
                radioFragment.delitem();
                radioFragment.setViewHint();
                sendBroadcast(new Intent(BroadcastConstants.SET_LOAD_REFRESH));
            }
        } else if (currentIndex == 4) {
            // TTS
            if (type == 1) {// 打开 view
                boolean flag = ttsFragment.changeviewtype(1);
                if (flag) {
                    isEdit = true;
                    ttsFragment.setViewVisibility();
                    sendBroadcast(new Intent(BroadcastConstants.SET_NOT_LOAD_REFRESH));
                    textFlag = 1;
                    textEditor.setText("取消");
                    if (delDialog != null) {
                        delDialog.show();
                    }
                } else {
                    ToastUtils.show_always(context, "当前页无数据");
                }
            } else if (type == 2) {// 隐藏 view
                ttsFragment.changeviewtype(0);
                ttsFragment.setViewHint();
                sendBroadcast(new Intent(BroadcastConstants.SET_LOAD_REFRESH));
            } else if (type == 3) {// 全选
                ttsFragment.changechecktype(1);
            } else if (type == 4) {// 解除全选
                ttsFragment.changechecktype(0);
            } else if (type == 5) {// 删除
                if (ttsFragment.getdelitemsum() == 0) {
                    ToastUtils.show_always(context, "请选择您要删除的数据");
                    return;
                }
                if (delDialog.isShowing()) {
                    delDialog.dismiss();
                    imageSelectAll.setImageBitmap(BitmapUtils.readBitMap(context, R.mipmap.wt_group_nochecked));
                    dialogFlag = 0;
                }
                textFlag = 0;
                textEditor.setText("编辑");
                ttsFragment.delitem();
                ttsFragment.setViewHint();
                sendBroadcast(new Intent(BroadcastConstants.SET_LOAD_REFRESH));
            }
        }
    }

    // 界面更新
    public static void viewChange(int index) {
        if (index == 0) {
            textTotal.setTextColor(context.getResources().getColor(R.color.dinglan_orange));
            textSequ.setTextColor(context.getResources().getColor(R.color.group_item_text2));
            textSound.setTextColor(context.getResources().getColor(R.color.group_item_text2));
            textRadio.setTextColor(context.getResources().getColor(R.color.group_item_text2));
            textTts.setTextColor(context.getResources().getColor(R.color.group_item_text2));
            textEditor.setVisibility(View.GONE);
            textEmpty.setVisibility(View.VISIBLE);
        } else if (index == 1) {
            textTotal.setTextColor(context.getResources().getColor(R.color.group_item_text2));
            textSequ.setTextColor(context.getResources().getColor(R.color.dinglan_orange));
            textSound.setTextColor(context.getResources().getColor(R.color.group_item_text2));
            textRadio.setTextColor(context.getResources().getColor(R.color.group_item_text2));
            textTts.setTextColor(context.getResources().getColor(R.color.group_item_text2));
            textEmpty.setVisibility(View.GONE);
            textEditor.setVisibility(View.VISIBLE);
        } else if (index == 2) {
            textTotal.setTextColor(context.getResources().getColor(R.color.group_item_text2));
            textSequ.setTextColor(context.getResources().getColor(R.color.group_item_text2));
            textSound.setTextColor(context.getResources().getColor(R.color.dinglan_orange));
            textRadio.setTextColor(context.getResources().getColor(R.color.group_item_text2));
            textTts.setTextColor(context.getResources().getColor(R.color.group_item_text2));
            textEmpty.setVisibility(View.GONE);
            textEditor.setVisibility(View.VISIBLE);
        } else if (index == 3) {
            textTotal.setTextColor(context.getResources().getColor(R.color.group_item_text2));
            textSequ.setTextColor(context.getResources().getColor(R.color.group_item_text2));
            textSound.setTextColor(context.getResources().getColor(R.color.group_item_text2));
            textRadio.setTextColor(context.getResources().getColor(R.color.dinglan_orange));
            textTts.setTextColor(context.getResources().getColor(R.color.group_item_text2));
            textEmpty.setVisibility(View.GONE);
            textEditor.setVisibility(View.VISIBLE);
        } else if (index == 4) {
            textTotal.setTextColor(context.getResources().getColor(R.color.group_item_text2));
            textSequ.setTextColor(context.getResources().getColor(R.color.group_item_text2));
            textSound.setTextColor(context.getResources().getColor(R.color.group_item_text2));
            textRadio.setTextColor(context.getResources().getColor(R.color.group_item_text2));
            textTts.setTextColor(context.getResources().getColor(R.color.dinglan_orange));
            textEmpty.setVisibility(View.GONE);
            textEditor.setVisibility(View.VISIBLE);
        }
    }

    // 提示对话框初始化
    private void initDialog() {
        // delDialog 初始化
        final View dialog = LayoutInflater.from(context).inflate(R.layout.dialog_fravorite, null);
        dialog.findViewById(R.id.lin_favorite_quanxuan).setOnClickListener(this);// 全选
        dialog.findViewById(R.id.lin_favorite_shanchu).setOnClickListener(this);// 删除

        imageSelectAll = (ImageView) dialog.findViewById(R.id.img_fravorite_quanxuan);
        delDialog = new Dialog(context, R.style.MyDialog_duijiang);
        delDialog.setContentView(dialog);
        Window window = delDialog.getWindow();
        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);
        int scrEnw = dm.widthPixels;
        ViewGroup.LayoutParams params = dialog.getLayoutParams();
        params.width = scrEnw;
        dialog.setLayoutParams(params);
        window.setGravity(Gravity.BOTTOM);
        window.setWindowAnimations(R.style.sharestyle);
        window.setFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE, WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE);
        delDialog.setCanceledOnTouchOutside(false);

        // confirmDialog 初始化
        final View dialog1 = LayoutInflater.from(context).inflate(R.layout.dialog_exit_confirm, null);
        dialog1.findViewById(R.id.tv_cancle).setOnClickListener(this);
        dialog1.findViewById(R.id.tv_confirm).setOnClickListener(this);
        TextView textTitle = (TextView) dialog1.findViewById(R.id.tv_title);
        textTitle.setText("是否删除所有的喜欢数据?");

        confirmDialog = new Dialog(context, R.style.MyDialog);
        confirmDialog.setContentView(dialog1);
        confirmDialog.setCanceledOnTouchOutside(false);
        confirmDialog.getWindow().setBackgroundDrawableResource(R.color.dialog);
    }

    // 广播接收  用于更新全选
    private class MyBroadcast extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(BroadcastConstants.SET_ALL_IMAGE)) {
                imageSelectAll.setImageBitmap(BitmapUtils.readBitMap(context, R.mipmap.wt_group_checked));
                dialogFlag = 1;
            } else if (intent.getAction().equals(BroadcastConstants.SET_NOT_ALL_IMAGE)) {
                imageSelectAll.setImageBitmap(BitmapUtils.readBitMap(context, R.mipmap.wt_group_nochecked));
                dialogFlag = 0;
            }
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (event.getAction() == KeyEvent.ACTION_DOWN && KeyEvent.KEYCODE_BACK == keyCode) {
            if (isEdit) {
                handleData(2);
                handleData(4);
                if (delDialog.isShowing()) {
                    delDialog.dismiss();
                    imageSelectAll.setImageBitmap(BitmapUtils.readBitMap(context, R.mipmap.wt_group_nochecked));
                    dialogFlag = 0;
                }
                textFlag = 0;
                textEditor.setText("编辑");
                isEdit = false;
            } else {
                finish();
            }
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(mBroadcast);
        image = null;
        textTotal = null;
        textSequ = null;
        textSound = null;
        textRadio = null;
        textTts = null;
        textEmpty = null;
        textEditor = null;
        imageSelectAll = null;
        mPager = null;
        delDialog = null;
        confirmDialog = null;
        totalFragment = null;
        sequFragment = null;
        soundFragment = null;
        radioFragment = null;
        context = null;
        setContentView(R.layout.activity_null);
    }
}
