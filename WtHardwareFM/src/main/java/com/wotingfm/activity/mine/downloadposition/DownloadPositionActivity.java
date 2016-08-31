package com.wotingfm.activity.mine.downloadposition;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.wotingfm.R;
import com.wotingfm.activity.music.common.service.DownloadService;
import com.wotingfm.manager.MyActivityManager;

/**
 * 下载位置
 * @author 辛龙
 *2016年8月8日
 */
public class DownloadPositionActivity extends Activity{
	private DownloadPositionActivity context;
	private TextView tv_downloadposition;
	private LinearLayout head_left_btn;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_downloadposition);
		context = this;
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);		// 透明状态栏
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);	// 透明导航栏
		MyActivityManager mam = MyActivityManager.getInstance();
		mam.pushOneActivity(context);
		setview();
	}

	/**
	 * 初始化视图
	 */
	private void setview() {
		tv_downloadposition = (TextView) findViewById(R.id.tv_downloadposition);
		head_left_btn=(LinearLayout)findViewById(R.id.head_left_btn);
		
		// 设置下载位置的路径，当前只能看
		if (!DownloadService.DOWNLOAD_PATH.equals("") && DownloadService.DOWNLOAD_PATH != null) {
			tv_downloadposition.setText(DownloadService.DOWNLOAD_PATH);
		}
		
		head_left_btn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
			     finish();		
			}
		});
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		MyActivityManager mam = MyActivityManager.getInstance();
		mam.popOneActivity(context);
		tv_downloadposition = null;
		head_left_btn= null;
		context = null;
		setContentView(R.layout.activity_null);
	}
}
