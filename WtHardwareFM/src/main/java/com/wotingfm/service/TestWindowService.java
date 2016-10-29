package com.wotingfm.service;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;

import com.wotingfm.R;
import com.wotingfm.common.config.GlobalConfig;
import com.wotingfm.util.PhoneMessage;

/**
 * 测试悬浮窗服务
 * 作者：xinlong on 2016/9/5 11:37
 * 邮箱：645700751@qq.com
 */
public class TestWindowService extends Service {

    public static final String OPERATION = "operation";
    public static final int OPERATION_SHOW = 100;
    public static final int OPERATION_HIDE = 101;
    private static final int HANDLE_CHECK_ACTIVITY = 200;

    private boolean isAdded = false; // 是否已增加悬浮窗
    private static WindowManager wm;
    private static WindowManager.LayoutParams params;
    private View floatView;


    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        createFloatView();
    }

    @Override
    public void onStart(Intent intent, int startId) {
        super.onStart(intent, startId);
        int operation = intent.getIntExtra(OPERATION, OPERATION_SHOW);
        switch (operation) {
            case OPERATION_SHOW:
                mHandler.removeMessages(HANDLE_CHECK_ACTIVITY);
                mHandler.sendEmptyMessage(HANDLE_CHECK_ACTIVITY);
                break;
            case OPERATION_HIDE:
                mHandler.removeMessages(HANDLE_CHECK_ACTIVITY);
                break;
        }
    }

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case HANDLE_CHECK_ACTIVITY:
                    if (!isAdded) {
                        wm.addView(floatView, params);
                        isAdded = true;
                    }
                    mHandler.sendEmptyMessageDelayed(HANDLE_CHECK_ACTIVITY, 1000);
                    break;
            }
        }
    };

    /**
     * 创建悬浮窗
     */
    private void createFloatView() {
        floatView = LayoutInflater.from(this).inflate(R.layout.dialog_float_test, null);
        TextView tv_1 = (TextView) floatView.findViewById(R.id.textView1);
        TextView tv_2 = (TextView) floatView.findViewById(R.id.textView2);
        TextView tv_3 = (TextView) floatView.findViewById(R.id.textView3);
        TextView tv_4 = (TextView) floatView.findViewById(R.id.textView4);
        TextView tv_5 = (TextView) floatView.findViewById(R.id.textView5);
        wm = (WindowManager) getApplicationContext().getSystemService(Context.WINDOW_SERVICE);
        params = new WindowManager.LayoutParams();
        //设置window type
        params.type = WindowManager.LayoutParams.TYPE_SYSTEM_ALERT;
        params.format = PixelFormat.RGBA_8888; // 设置图片格式，效果为背景透明
        // 设置Window flag
        params.flags = WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
        params.width = PhoneMessage.ScreenWidth;
        params.height = 150;
        params.y = 700;

        tv_3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                GlobalConfig.device.pushUpButton();
            }
        });
        tv_4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                GlobalConfig.device.pushCenter();
            }
        });
        tv_5.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                GlobalConfig.device.pushDownButton();
            }
        });

        tv_1.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        GlobalConfig.device.pushVoiceStart();//按下状态
                        break;
                    case MotionEvent.ACTION_UP:
                        GlobalConfig.device.releaseVoiceStop();//抬起手后的操作
                        break;
                }
                return true;
            }
        });

        tv_2.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        //语音按钮按下操作
                        GlobalConfig.device.pushPTT();//按下状态
                        break;
                    case MotionEvent.ACTION_UP:
                        GlobalConfig.device.releasePTT();//抬起手后的操作
                        break;
                }
                return true;
            }
        });
        wm.addView(floatView, params);
        isAdded = true;
    }

}
