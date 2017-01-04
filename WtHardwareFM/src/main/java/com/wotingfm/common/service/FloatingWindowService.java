package com.wotingfm.common.service;

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
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.WindowManager;
import android.widget.LinearLayout;

import com.wotingfm.R;
import com.wotingfm.common.config.GlobalConfig;
import com.wotingfm.common.constant.BroadcastConstants;
import com.wotingfm.util.PhoneMessage;
/**
 * 悬浮窗服务----在BSApplication中启动
 * 作者：xinlong on 2016/9/5 11:37
 * 邮箱：645700751@qq.com
 */
public class FloatingWindowService extends Service {

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
		switch(operation) {
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
			switch(msg.what) {
			case HANDLE_CHECK_ACTIVITY:
					if(!isAdded) {
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
		floatView= LayoutInflater.from(this).inflate(R.layout.dialog_float, null);
		LinearLayout lin_a = (LinearLayout) floatView.findViewById(R.id.lin_a);
		LinearLayout lin_b = (LinearLayout) floatView.findViewById(R.id.lin_b);
		LinearLayout lin_c = (LinearLayout) floatView.findViewById(R.id.lin_c);
		final LinearLayout lin_d = (LinearLayout) floatView.findViewById(R.id.lin_d);
		floatView.setBackgroundDrawable(getApplicationContext().getResources().getDrawable(R.mipmap.aa));
        //btn_floatView.setText("我听科技");
        wm = (WindowManager) getApplicationContext().getSystemService(Context.WINDOW_SERVICE);
        params = new WindowManager.LayoutParams();
        //设置window type
        params.type = WindowManager.LayoutParams.TYPE_SYSTEM_ALERT;
        /*
         * 如果设置为params.type = WindowManager.LayoutParams.TYPE_PHONE;
         * 那么优先级会降低一些, 即拉下通知栏不可见
         */
        params.format = PixelFormat.RGBA_8888; // 设置图片格式，效果为背景透明
        // 设置Window flag
        params.flags = WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL|WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
		/*
         * 下面的flags属性的效果形同“锁定”。
         * 悬浮窗不可触摸，不接受任何事件,同时不影响后面的事件响应。
        wmParams.flags=LayoutParams.FLAG_NOT_TOUCH_MODAL
                               | LayoutParams.FLAG_NOT_FOCUSABLE
                               | LayoutParams.FLAG_NOT_TOUCHABLE;
         */
        // 设置悬浮窗的长得宽
        params.width = PhoneMessage.ScreenWidth/5;
        params.height = PhoneMessage.ScreenWidth/5;
        // 设置悬浮窗的Touch监听
		floatView.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
                //Intent intent = new Intent(getApplicationContext(),MainActivity.class);
                //intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP |Intent.FLAG_ACTIVITY_SINGLE_TOP);
                //intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK| Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
                //startActivity(intent);
				if(GlobalConfig.activityType ==1){
					GlobalConfig.activityType =2;
					Intent push=new Intent(BroadcastConstants.ACTIVITY_CHANGE);
					sendBroadcast(push);
					lin_d.setBackgroundResource(R.mipmap.aa);
				}else if(GlobalConfig.activityType ==2){
					GlobalConfig.activityType =3;
					Intent push=new Intent(BroadcastConstants.ACTIVITY_CHANGE);
					sendBroadcast(push);
					lin_d.setBackgroundResource(R.mipmap.cc);
				}else if(GlobalConfig.activityType ==3){
					GlobalConfig.activityType =1;
					Intent push=new Intent(BroadcastConstants.ACTIVITY_CHANGE);
					sendBroadcast(push);
					lin_d.setBackgroundResource(R.mipmap.bb);
				}

			}
		});
               //lin_a.setOnClickListener(new OnClickListener() {
		       //@Override
               //public void onClick(View v) {
               //lin_d.setBackgroundResource(R.mipmap.aa);
               //Log.e("悬浮窗","AAAAAAAA");
               //}
               //});
		       //
               //lin_b.setOnClickListener(new OnClickListener() {
               //@Override
               //public void onClick(View v) {
		       //lin_d.setBackgroundResource(R.mipmap.bb);
               //Log.e("悬浮窗","BBBBBBBB");
               //}
               //});
               //
               //lin_c.setOnClickListener(new OnClickListener() {
               //@Override
               //public void onClick(View v) {
               //lin_d.setBackgroundResource(R.mipmap.cc);
               //Log.e("悬浮窗","CCCCCCCC");
               //}
               //});
		floatView.setOnTouchListener(new OnTouchListener() {
        	int lastX, lastY;
        	int paramX, paramY;
			public boolean onTouch(View v, MotionEvent event) {
				switch(event.getAction()) {
				case MotionEvent.ACTION_UP:
					lastX = (int) event.getRawX();
					lastY = (int) event.getRawY();
					paramX = params.x;
					paramY = params.y;
                    //Intent intent = new Intent(getApplicationContext(),MainActivity.class);
                    //intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP |Intent.FLAG_ACTIVITY_SINGLE_TOP);
					//intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK| Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
                    //startActivity(intent);
					break;
				case MotionEvent.ACTION_DOWN:
					lastX = (int) event.getRawX();
					lastY = (int) event.getRawY();
					paramX = params.x;
					paramY = params.y;
					break;
				case MotionEvent.ACTION_MOVE:
					int dx = (int) event.getRawX() - lastX;
					int dy = (int) event.getRawY() - lastY;
					params.x = paramX + dx;
					params.y = paramY + dy;
					// 更新悬浮窗位置
			        wm.updateViewLayout(floatView, params);
					break;
				}
				return false;
			}
		});
        wm.addView(floatView, params);
        isAdded = true;
	}

}
