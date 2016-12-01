package com.wotingfm.activity.mine.set.downloadposition;

import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.wotingfm.R;
import com.wotingfm.activity.common.baseactivity.BaseActivity;
import com.wotingfm.activity.music.common.service.DownloadService;

/**
 * 下载位置
 * @author 辛龙
 *2016年8月8日
 */
public class DownloadPositionActivity extends BaseActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_downloadposition);
		setView();
	}

	/**
	 * 初始化视图
	 */
	private void setView() {
		TextView tv_downloadposition = (TextView) findViewById(R.id.tv_downloadposition);
		LinearLayout head_left_btn=(LinearLayout)findViewById(R.id.head_left_btn);
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
		setContentView(R.layout.activity_null);
	}
}
