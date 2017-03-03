package com.wotingfm.ui.mine.set.downloadposition;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.wotingfm.R;
import com.wotingfm.ui.mine.MineActivity;
import com.wotingfm.ui.music.download.service.DownloadService;

/**
 * 下载位置
 * @author 辛龙
 * 2016年8月8日
 */
public class DownloadPositionFragment extends Fragment implements View.OnClickListener{

	private View rootView;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		if (rootView == null) {
			rootView = inflater.inflate(R.layout.activity_downloadposition, container, false);
			rootView.setOnClickListener(this);
			setView();
		}
		return rootView;
	}

	// 初始化视图
	private void setView() {
		rootView.findViewById(R.id.head_left_btn).setOnClickListener(this);

		TextView textDownloadPosition = (TextView) rootView.findViewById(R.id.tv_downloadposition);
		if (!DownloadService.DOWNLOAD_PATH.equals("") && DownloadService.DOWNLOAD_PATH != null) {
            textDownloadPosition.setText(DownloadService.DOWNLOAD_PATH);
		}
	}

    @Override
    public void onClick(View v) {
        if(v.getId() == R.id.head_left_btn) {
			MineActivity.close();
        }
    }

}
