package com.wotingfm.ui.music.playhistory.activity;

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
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.ImageView;
import android.widget.TextView;

import com.wotingfm.R;
import com.wotingfm.ui.baseactivity.AppBaseFragmentActivity;
import com.wotingfm.ui.music.main.dao.SearchPlayerHistoryDao;
import com.wotingfm.ui.music.playhistory.fragment.RadioFragment;
import com.wotingfm.ui.music.playhistory.fragment.SoundFragment;
import com.wotingfm.ui.music.playhistory.fragment.TTSFragment;
import com.wotingfm.ui.music.playhistory.fragment.TotalFragment;
import com.wotingfm.common.constant.BroadcastConstants;
import com.wotingfm.util.BitmapUtils;
import com.wotingfm.util.PhoneMessage;
import com.wotingfm.util.ToastUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * 播放历史
 * @author woting11
 */
public class PlayHistoryActivity extends AppBaseFragmentActivity implements View.OnClickListener {
    private SearchPlayerHistoryDao dbDao;	// 播放历史数据库
    private TotalFragment allFragment; 		// 全部
    private SoundFragment soundFragment; 	// 声音
    private RadioFragment radioFragment; 	// 电台
    private TTSFragment ttsFragment; 		// TTS

    private ViewPager viewPager;
    private Dialog delDialog;
    private Dialog confirmDialog;
    private TextView allText, soundText, radioText, ttsText, clearEmpty, openEdit;
    private ImageView image; 				// Cursor
    private ImageView imgAllCheck;

    private List<Fragment> fragmentList;
    private int currIndex; 					// 当前页卡编号
    private int bmpW; 						// 横线图片宽度
    private int offset; 					// 图片移动的偏移量
    private int dialogFlag = 0; 			// 编辑全选状态的变量 0 为未选中，1 为选中

    public static boolean isEdit = true; 	// 是否为编辑状态
    private boolean isDelete = false;

    @TargetApi(Build.VERSION_CODES.KITKAT)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_playhistory);

        dbDao = new SearchPlayerHistoryDao(context);    // 初始化数据库

        IntentFilter intentFilter = new IntentFilter();	// 注册广播
        intentFilter.addAction(BroadcastConstants.UPDATE_ACTION_ALL);
        intentFilter.addAction(BroadcastConstants.UPDATE_ACTION_CHECK);
        registerReceiver(myBroadcast, intentFilter);

        initDialog();
        initImage();
        initViews();
    }

    // 初始化视图
    private void initViews() {
        findViewById(R.id.head_left_btn).setOnClickListener(this);  // 左上返回键

        clearEmpty = (TextView) findViewById(R.id.clear_empty); 	// 清空
        clearEmpty.setOnClickListener(this);

        openEdit = (TextView) findViewById(R.id.open_edit); 		// 编辑
        openEdit.setOnClickListener(this);

        fragmentList = new ArrayList<>();					        // 存放 Fragment
        viewPager = (ViewPager) findViewById(R.id.viewpager);

        allText = (TextView) findViewById(R.id.text_all); 			// 全部
        allText.setOnClickListener(new TextViewListener(0));
        allFragment = new TotalFragment();
        fragmentList.add(allFragment);

        soundText = (TextView) findViewById(R.id.text_sound); 		// 声音
        soundText.setOnClickListener(new TextViewListener(1));
        soundFragment = new SoundFragment();
        fragmentList.add(soundFragment);

        radioText = (TextView) findViewById(R.id.text_radio); 		// 电台
        radioText.setOnClickListener(new TextViewListener(2));
        radioFragment = new RadioFragment();
        fragmentList.add(radioFragment);

        ttsText = (TextView) findViewById(R.id.text_tts); 			// TTS
        ttsText.setOnClickListener(new TextViewListener(3));
        ttsFragment = new TTSFragment();
        fragmentList.add(ttsFragment);

        viewPager.setAdapter(new MyFragmentPagerAdapter(getSupportFragmentManager()));
        viewPager.setOnPageChangeListener(new MyOnPageChangeListener());
        viewPager.setCurrentItem(0);// 设置当前显示标签页为第一页
    }

    // TextView 点击事件
    class TextViewListener implements View.OnClickListener {
        private int index = 0;

        public TextViewListener(int i) {
            index = i;
        }

        @Override
        public void onClick(View v) {
            viewPager.setCurrentItem(index);
        }
    }

    // ViewPager 设置适配器
    class MyFragmentPagerAdapter extends FragmentPagerAdapter {
        public MyFragmentPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int arg0) {
            return fragmentList.get(arg0);
        }

        @Override
        public int getCount() {
            return fragmentList.size();
        }
    }

    // ViewPager 监听事件设置
    class MyOnPageChangeListener implements ViewPager.OnPageChangeListener {
        private int one = offset * 2 + bmpW;	// 两个相邻页面的偏移量

        @Override
        public void onPageScrollStateChanged(int arg0) {
        }

        @Override
        public void onPageScrolled(int arg0, float arg1, int arg2) {
        }

        @Override
        public void onPageSelected(int arg0) {
            Animation animation = new TranslateAnimation(currIndex * one, arg0 * one, 0, 0);// 平移动画
            currIndex = arg0;
            animation.setFillAfter(true); 		// 动画终止时停留在最后一帧，不然会回到没有执行前的状态
            animation.setDuration(200); 		// 动画持续时间 0.2 秒
            image.startAnimation(animation); 	// 是用 ImageView 来显示动画的
            int i = currIndex + 1;
            if (i == 1) { // 全部
                allText.setTextColor(context.getResources().getColor(R.color.dinglan_orange));

                soundText.setTextColor(context.getResources().getColor(R.color.group_item_text2));
                radioText.setTextColor(context.getResources().getColor(R.color.group_item_text2));
                ttsText.setTextColor(context.getResources().getColor(R.color.group_item_text2));
                clearEmpty.setVisibility(View.VISIBLE);
                openEdit.setVisibility(View.GONE);
            }  else if (i == 2) { // 声音
                soundText.setTextColor(context.getResources().getColor(R.color.dinglan_orange));
                allText.setTextColor(context.getResources().getColor(R.color.group_item_text2));

                radioText.setTextColor(context.getResources().getColor(R.color.group_item_text2));
                ttsText.setTextColor(context.getResources().getColor(R.color.group_item_text2));
                clearEmpty.setVisibility(View.GONE);
                openEdit.setVisibility(View.VISIBLE);
            } else if (i == 3) { // 电台
                radioText.setTextColor(context.getResources().getColor(R.color.dinglan_orange));
                allText.setTextColor(context.getResources().getColor(R.color.group_item_text2));

                soundText.setTextColor(context.getResources().getColor(R.color.group_item_text2));
                ttsText.setTextColor(context.getResources().getColor(R.color.group_item_text2));
                clearEmpty.setVisibility(View.GONE);
                openEdit.setVisibility(View.VISIBLE);
            } else if (i == 4) { // TTS
                ttsText.setTextColor(context.getResources().getColor(R.color.dinglan_orange));
                allText.setTextColor(context.getResources().getColor(R.color.group_item_text2));

                soundText.setTextColor(context.getResources().getColor(R.color.group_item_text2));
                radioText.setTextColor(context.getResources().getColor(R.color.group_item_text2));
                clearEmpty.setVisibility(View.GONE);
                openEdit.setVisibility(View.VISIBLE);
            }
            setCancel();
        }
    }

    // 编辑设置
    private void setEdit() {
        int i = currIndex + 1;
        switch (i) {
            case 2: // 声音
                if (!SoundFragment.isData) {
                    ToastUtils.show_always(context, "没有历史播放数据");
                } else {
                    openEdit.setText("取消");
                    PlayHistoryActivity.isEdit = false;
                    soundFragment.setCheck(true);
                    delDialog.show();
                    soundFragment.setLinearVisibility();
                }
                break;
            case 3: // 电台
                if (!RadioFragment.isData) {
                    ToastUtils.show_always(context, "没有历史播放数据");
                } else {
                    openEdit.setText("取消");
                    PlayHistoryActivity.isEdit = false;
                    radioFragment.setCheck(true);
                    delDialog.show();
                    radioFragment.setLinearVisibility();
                }
                break;
            case 4: // TTS
                if (!TTSFragment.isData) {
                    ToastUtils.show_always(context, "没有历史播放数据");
                } else {
                    openEdit.setText("取消");
                    PlayHistoryActivity.isEdit = false;
                    ttsFragment.setCheck(true);
                    delDialog.show();
                    ttsFragment.setLinearVisibility();
                }
                break;
        }
    }

    // 取消设置
    private void setCancel(){
        int i = currIndex + 1;
        switch (i) {
            case 2: // 声音
                soundFragment.setCheck(false);
                soundFragment.setCheckStatus(0);
                soundFragment.setLinearHint();
                break;
            case 3: // 电台
                radioFragment.setCheck(false);
                radioFragment.setCheckStatus(0);
                radioFragment.setLinearHint();
                break;
            case 4: // TTS
                ttsFragment.setCheck(false);
                ttsFragment.setCheckStatus(0);
                ttsFragment.setLinearHint();
                break;
        }
        if(delDialog != null){
            delDialog.dismiss();
        }
        PlayHistoryActivity.isEdit = true;
        openEdit.setText("编辑");
        dialogFlag = 0;
    }

    // 设置 cursor 的宽
    public void initImage() {
        image = (ImageView) findViewById(R.id.cursor);
        ViewGroup.LayoutParams lp = image.getLayoutParams();
        lp.width = (PhoneMessage.ScreenWidth / 4);
        image.setLayoutParams(lp);
        bmpW = BitmapFactory.decodeResource(getResources(), R.mipmap.left_personal_bg).getWidth();
        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);
        int screenW = dm.widthPixels;
        offset = (screenW / 4 - bmpW) / 2;
        // imageView 设置平移，使下划线平移到初始位置（平移一个 offset）
        Matrix matrix = new Matrix();
        matrix.postTranslate(offset, 0);
        image.setImageMatrix(matrix);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.head_left_btn:	// 左上角返回键
                finish();
                break;
            case R.id.clear_empty:		// 清空数据
                if (TotalFragment.isData) {
                    confirmDialog.show();
                } else {
                    ToastUtils.show_always(context, "没有历史播放数据");
                }
                break;
            case R.id.open_edit:		// 编辑
                if (isEdit) {
                    setEdit();
                } else {
                    setCancel();
                }
                break;
            case R.id.lin_favorite_quanxuan:// 全选
                if (dialogFlag == 0) {
                    imgAllCheck.setImageBitmap(BitmapUtils.readBitMap(context, R.mipmap.wt_group_checked));
                    dialogFlag = 1;
                } else if(dialogFlag == 1){
                    imgAllCheck.setImageBitmap(BitmapUtils.readBitMap(context, R.mipmap.wt_group_nochecked));
                    dialogFlag = 0;
                }
                handleData(dialogFlag);
                break;
            case R.id.lin_favorite_shanchu:// 删除
                delete();
                if(isDelete){
                    allFragment.getData();
                    delDialog.dismiss();
                    setCancel();
                }else{
                    ToastUtils.show_always(context, "请选择你要删除的历史播放记录");
                }
                break;
            case R.id.tv_cancle:// 取消删除
                confirmDialog.dismiss();
                break;
            case R.id.tv_confirm:// 确定删除
                dbDao.deleteHistoryAll();
                allFragment.getData();
                if(SoundFragment.isData && SoundFragment.isLoad){
                    soundFragment.getData();
                }
                if(RadioFragment.isData && RadioFragment.isLoad){
                    radioFragment.getData();
                }
                if(TTSFragment.isData && TTSFragment.isLoad){
                    ttsFragment.getData();
                }
                confirmDialog.dismiss();
                break;
        }
    }

    // 初始化提示对话框
    private void initDialog() {
        // 编辑状态下的对话框 在界面底部显示
        final View dialog = LayoutInflater.from(context).inflate(R.layout.dialog_fravorite, null);
        dialog.findViewById(R.id.lin_favorite_quanxuan).setOnClickListener(this);
        dialog.findViewById(R.id.lin_favorite_shanchu).setOnClickListener(this);
        imgAllCheck = (ImageView) dialog.findViewById(R.id.img_fravorite_quanxuan);
        delDialog = new Dialog(context, R.style.MyDialog_duijiang);
        delDialog.setContentView(dialog); // 从底部上升到一个位置
        Window window = delDialog.getWindow();
        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);
        ViewGroup.LayoutParams params = dialog.getLayoutParams();
        params.width = dm.widthPixels;
        dialog.setLayoutParams(params);
        window.setGravity(Gravity.BOTTOM);
        window.setWindowAnimations(R.style.sharestyle);
        window.setFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE, WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE);
        delDialog.setCanceledOnTouchOutside(false);

        // 清空所有数据 对话框
        final View dialog1 = LayoutInflater.from(this).inflate(R.layout.dialog_exit_confirm, null);
        dialog1.findViewById(R.id.tv_cancle).setOnClickListener(this);
        dialog1.findViewById(R.id.tv_confirm).setOnClickListener(this);
        TextView textTitle = (TextView) dialog1.findViewById(R.id.tv_title);
        textTitle.setText("是否清空全部历史记录");

        confirmDialog = new Dialog(context, R.style.MyDialog);
        confirmDialog.setContentView(dialog1);
        confirmDialog.setCanceledOnTouchOutside(true);
        confirmDialog.getWindow().setBackgroundDrawableResource(R.color.dialog);
    }

    // 处理数据
    private void handleData(int status) {
        switch (currIndex) {
            case 1:// 声音
                soundFragment.setCheckStatus(status);
                break;
            case 2:// 电台
                radioFragment.setCheckStatus(status);
                break;
            case 3 :// TTS
                ttsFragment.setCheckStatus(status);
                break;
        }
    }

    // 删除数据
    private void delete(){
        int number = 0;
        String message = "";
        switch (currIndex) {
            case 1:// 声音
                number = soundFragment.deleteData();
                message = "声音";
                break;
            case 2:// 电台
                number = radioFragment.deleteData();
                message = "电台";
                break;
            case 3:// TTS
                number = ttsFragment.deleteData();
                message = "TTS";
                break;
        }
        if(number > 0){
            isDelete = true;
            ToastUtils.show_always(context, "删除了 " + number + " 条" + message + "播放历史记录");
        }
    }

    // 查看更多
    public void updateViewPager(String mediaType){
        int index = 0;
        if(mediaType != null && !mediaType.equals("")){
            if(mediaType.equals("AUDIO")){
                index = 1;
            }else if(mediaType.equals("RADIO")){
                index = 2;
            }else if(mediaType.equals("TTS")){
                index = 3;
            }
            viewPager.setCurrentItem(index);
        }
    }

    // 广播接收器  接收 Fragment 发送的广播  用于更新全选状态
    private BroadcastReceiver myBroadcast = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals(BroadcastConstants.UPDATE_ACTION_ALL)) {
                imgAllCheck.setImageBitmap(BitmapUtils.readBitMap(context, R.mipmap.wt_group_checked));
                dialogFlag = 1;
            }else if(action.equals(BroadcastConstants.UPDATE_ACTION_CHECK)){
                imgAllCheck.setImageBitmap(BitmapUtils.readBitMap(context, R.mipmap.wt_group_nochecked));
                dialogFlag = 0;
            }
        }
    };

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (event.getAction() == KeyEvent.ACTION_DOWN && KeyEvent.KEYCODE_BACK == keyCode) {
            if (!isEdit) {
                setCancel();
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
        unregisterReceiver(myBroadcast);// 反注册广播
        SoundFragment.isLoad = false;
        RadioFragment.isData = false;
        TTSFragment.isLoad = false;
        context = null;
        image = null;
        allText = null;
        soundText = null;
        radioText = null;
        ttsText = null;
        clearEmpty = null;
        openEdit = null;
        viewPager = null;
        soundFragment = null;
        radioFragment = null;
        ttsFragment = null;
        setContentView(R.layout.activity_null);
    }
}
