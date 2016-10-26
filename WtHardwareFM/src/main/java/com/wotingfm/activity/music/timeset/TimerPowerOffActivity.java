package com.wotingfm.activity.music.timeset;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.wotingfm.R;
import com.wotingfm.activity.common.baseactivity.AppBaseActivity;
import com.wotingfm.activity.music.player.fragment.PlayerFragment;
import com.wotingfm.common.config.GlobalConfig;
import com.wotingfm.common.constant.BroadcastConstant;
import com.wotingfm.service.timeroffservice;

/**
 * 定时关闭 关闭程序要以服务形式出现
 * @author 辛龙
 *         2016年4月1日
 */
public class TimerPowerOffActivity extends AppBaseActivity implements OnClickListener {
    private Intent intent;
    private LinearLayout linearPlayEnd;
    private ImageView imageTime10, imageTime20, imageTime30,
            imageTime40, imageTime50, imageTime60, imageTimeProgramOver, imageTimeNoStart;

    @Override
    protected int setViewId() {
        return R.layout.activity_timerpoweroff;
    }

    @Override
    protected void init() {
        setTitle("定时关闭");
        setRightText("00:00", null);
        setView();                                  // 设置界面
        IntentFilter mFilter = new IntentFilter();  // 注册广播里接收器
        mFilter.addAction(BroadcastConstant.TIMER_UPDATE);
        registerReceiver(mBroadcastReceiver, mFilter);
        // 设置Intent
        intent = new Intent(TimerPowerOffActivity.this, timeroffservice.class);
        intent.setAction(BroadcastConstant.TIMER_START);
        setImageTimeCheck(0);
    }

    private void setView() {
        findViewById(R.id.lin_10).setOnClickListener(this);             // 10分钟结束
        findViewById(R.id.lin_20).setOnClickListener(this);             // 20分钟结束
        findViewById(R.id.lin_30).setOnClickListener(this);             // 30分钟结束
        findViewById(R.id.lin_40).setOnClickListener(this);             // 40分钟结束
        findViewById(R.id.lin_50).setOnClickListener(this);             // 50分钟结束
        findViewById(R.id.lin_60).setOnClickListener(this);             // 60分钟结束
        findViewById(R.id.lin_nostart).setOnClickListener(this);        // 不启动服务

        linearPlayEnd = (LinearLayout) findViewById(R.id.lin_playend);  // 当前节目播放完结束
        linearPlayEnd.setOnClickListener(this);

        imageTime10 = (ImageView) findViewById(R.id.image_time_10);
        imageTime20 = (ImageView) findViewById(R.id.image_time_20);
        imageTime30 = (ImageView) findViewById(R.id.image_time_30);
        imageTime40 = (ImageView) findViewById(R.id.image_time_40);
        imageTime50 = (ImageView) findViewById(R.id.image_time_50);
        imageTime60 = (ImageView) findViewById(R.id.image_time_60);
        imageTimeProgramOver = (ImageView) findViewById(R.id.image_time_program_over);
        imageTimeNoStart = (ImageView) findViewById(R.id.image_time_nostart);

        View linView = findViewById(R.id.lin_view);

        if(GlobalConfig.playerobject != null && PlayerFragment.audioplay != null &&
                PlayerFragment.audioplay.isPlaying() && !GlobalConfig.playerobject.getMediaType().equals("RADIO")) {

            linearPlayEnd.setVisibility(View.VISIBLE);
            linView.setVisibility(View.VISIBLE);
        } else {
            linearPlayEnd.setVisibility(View.GONE);
            linView.setVisibility(View.GONE);
        }

        if(PlayerFragment.isCurrentPlay){
            linearPlayEnd.setClickable(false);
        }
    }

    // 设置选中图片的显示与隐藏
    private void setImageTimeCheck(int index) {
        switch (index) {
            case 10:    // 十分钟
                imageTime10.setVisibility(View.VISIBLE);
                imageTime20.setVisibility(View.GONE);
                imageTime30.setVisibility(View.GONE);
                imageTime40.setVisibility(View.GONE);
                imageTime50.setVisibility(View.GONE);
                imageTime60.setVisibility(View.GONE);
                imageTimeProgramOver.setVisibility(View.GONE);
                imageTimeNoStart.setVisibility(View.GONE);
                break;
            case 20:    // 二十分钟
                imageTime10.setVisibility(View.GONE);
                imageTime20.setVisibility(View.VISIBLE);
                imageTime30.setVisibility(View.GONE);
                imageTime40.setVisibility(View.GONE);
                imageTime50.setVisibility(View.GONE);
                imageTime60.setVisibility(View.GONE);
                imageTimeProgramOver.setVisibility(View.GONE);
                imageTimeNoStart.setVisibility(View.GONE);
                break;
            case 30:    // 三十分钟
                imageTime10.setVisibility(View.GONE);
                imageTime20.setVisibility(View.GONE);
                imageTime30.setVisibility(View.VISIBLE);
                imageTime40.setVisibility(View.GONE);
                imageTime50.setVisibility(View.GONE);
                imageTime60.setVisibility(View.GONE);
                imageTimeProgramOver.setVisibility(View.GONE);
                imageTimeNoStart.setVisibility(View.GONE);
                break;
            case 40:    // 四十分钟
                imageTime10.setVisibility(View.GONE);
                imageTime20.setVisibility(View.GONE);
                imageTime30.setVisibility(View.GONE);
                imageTime40.setVisibility(View.VISIBLE);
                imageTime50.setVisibility(View.GONE);
                imageTime60.setVisibility(View.GONE);
                imageTimeProgramOver.setVisibility(View.GONE);
                imageTimeNoStart.setVisibility(View.GONE);
                break;
            case 50:    // 五十分钟
                imageTime10.setVisibility(View.GONE);
                imageTime20.setVisibility(View.GONE);
                imageTime30.setVisibility(View.GONE);
                imageTime40.setVisibility(View.GONE);
                imageTime50.setVisibility(View.VISIBLE);
                imageTime60.setVisibility(View.GONE);
                imageTimeProgramOver.setVisibility(View.GONE);
                imageTimeNoStart.setVisibility(View.GONE);
                break;
            case 60:    // 六十分钟
                imageTime10.setVisibility(View.GONE);
                imageTime20.setVisibility(View.GONE);
                imageTime30.setVisibility(View.GONE);
                imageTime40.setVisibility(View.GONE);
                imageTime50.setVisibility(View.GONE);
                imageTime60.setVisibility(View.VISIBLE);
                imageTimeProgramOver.setVisibility(View.GONE);
                imageTimeNoStart.setVisibility(View.GONE);
                break;
            case 100:   // 当前节目播放完
                imageTime10.setVisibility(View.GONE);
                imageTime20.setVisibility(View.GONE);
                imageTime30.setVisibility(View.GONE);
                imageTime40.setVisibility(View.GONE);
                imageTime50.setVisibility(View.GONE);
                imageTime60.setVisibility(View.GONE);
                imageTimeProgramOver.setVisibility(View.VISIBLE);
                imageTimeNoStart.setVisibility(View.GONE);
                break;
            case 0:     // 不启动
                imageTime10.setVisibility(View.GONE);
                imageTime20.setVisibility(View.GONE);
                imageTime30.setVisibility(View.GONE);
                imageTime40.setVisibility(View.GONE);
                imageTime50.setVisibility(View.GONE);
                imageTime60.setVisibility(View.GONE);
                imageTimeProgramOver.setVisibility(View.GONE);
                imageTimeNoStart.setVisibility(View.VISIBLE);
                break;
        }
    }

    @Override
    public void onClick(View v) {
        int imageTimeCheck = 0;
        switch (v.getId()) {
            case R.id.lin_10:            // 十分钟
                imageTimeCheck = 10;
                intent.putExtra("time", 10);
                startService(intent);
			    PlayerFragment.isCurrentPlay = false;
                linearPlayEnd.setClickable(true);
                break;
            case R.id.lin_20:            // 二十分钟
                imageTimeCheck = 20;
                intent.putExtra("time", 20);
                startService(intent);
			    PlayerFragment.isCurrentPlay = false;
                linearPlayEnd.setClickable(true);
                break;
            case R.id.lin_30:            // 三十分钟
                imageTimeCheck = 30;
                intent.putExtra("time", 30);
                startService(intent);
			    PlayerFragment.isCurrentPlay = false;
                linearPlayEnd.setClickable(true);
                break;
            case R.id.lin_40:            // 四十分钟
                imageTimeCheck = 40;
                intent.putExtra("time", 40);
                startService(intent);
			    PlayerFragment.isCurrentPlay = false;
                linearPlayEnd.setClickable(true);
                break;
            case R.id.lin_50:            // 五十分钟
                imageTimeCheck = 50;
                intent.putExtra("time", 50);
                startService(intent);
			    PlayerFragment.isCurrentPlay = false;
                linearPlayEnd.setClickable(true);
                break;
            case R.id.lin_60:            // 六十分钟
                imageTimeCheck = 60;
                intent.putExtra("time", 60);
                startService(intent);
			    PlayerFragment.isCurrentPlay = false;
                linearPlayEnd.setClickable(true);
                break;
            case R.id.lin_playend:      // 当前节目播放完
                imageTimeCheck = 100;
                int time = PlayerFragment.timerService;
                intent.putExtra("time", time);
                startService(intent);
			    PlayerFragment.isCurrentPlay = true;
                linearPlayEnd.setClickable(false);
                break;
            case R.id.lin_nostart:      // 不启动
                imageTimeCheck = 0;
                Intent intent = new Intent(this, timeroffservice.class);
                intent.setAction(BroadcastConstant.TIMER_STOP);
                startService(intent);
                setRightText("00:00", null);
			    PlayerFragment.isCurrentPlay = false;
                linearPlayEnd.setClickable(true);
                break;
        }
        setImageTimeCheck(imageTimeCheck);
    }

    private boolean isCheck = true;

    // 广播接收器
    private BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, final Intent intent) {
            String action = intent.getAction();
            if (action.equals(BroadcastConstant.TIMER_UPDATE)) {
                String s = intent.getStringExtra("update");
                setRightText(s, null);
                if (isCheck) {
                    setImageTimeCheck(intent.getIntExtra("check_image", 0));
                    isCheck = false;
                }
            }
        }
    };

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(mBroadcastReceiver);
        intent = null;
        linearPlayEnd = null;
        imageTime10 = null;
        imageTime20 = null;
        imageTime30 = null;
        imageTime40 = null;
        imageTime50 = null;
        imageTime60 = null;
        imageTimeProgramOver = null;
        imageTimeNoStart = null;
    }
}
