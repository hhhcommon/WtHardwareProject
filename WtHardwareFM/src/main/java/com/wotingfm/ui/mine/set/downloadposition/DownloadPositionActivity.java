package com.wotingfm.ui.mine.set.downloadposition;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.wotingfm.R;
import com.wotingfm.ui.baseactivity.BaseActivity;
import com.wotingfm.ui.music.common.service.DownloadService;

/**
 * 下载位置
 * @author 辛龙
 * 2016年8月8日
 */
public class DownloadPositionActivity extends BaseActivity implements View.OnClickListener{

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_downloadposition);

		setView();
	}

	// 初始化视图
	private void setView() {
        findViewById(R.id.head_left_btn).setOnClickListener(this);

		TextView textDownloadPosition = (TextView) findViewById(R.id.tv_downloadposition);
		if (!DownloadService.DOWNLOAD_PATH.equals("") && DownloadService.DOWNLOAD_PATH != null) {
            textDownloadPosition.setText(DownloadService.DOWNLOAD_PATH);
		}
	}

    @Override
    public void onClick(View v) {
        if(v.getId() == R.id.head_left_btn) {
            finish();
        }
    }

	@Override
	protected void onDestroy() {
		super.onDestroy();
		setContentView(R.layout.activity_null);
	}
}
