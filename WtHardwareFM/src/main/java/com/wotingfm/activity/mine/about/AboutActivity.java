package com.wotingfm.activity.mine.about;

import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.wotingfm.R;
import com.wotingfm.activity.common.baseactivity.BaseActivity;
import com.wotingfm.util.PhoneMessage;

/**
 * 关于 
 * @author 辛龙
 * 2016年3月9日
 */
public class AboutActivity extends BaseActivity implements OnClickListener {
	private AboutActivity context;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_about);
		context = this;
		setView();		//设置界面
	}

	/**
	 * 初始化视图
	 */
	private void setView() {
		LinearLayout head_left_btn=(LinearLayout)findViewById(R.id.head_left_btn);	// 返回
		head_left_btn.setOnClickListener(context);
		TextView tv_version=(TextView)findViewById(R.id.tv_verson);					// 版本号
		tv_version.setText(PhoneMessage.appVersonName);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.head_left_btn:	// 返回
			finish();
			break;
		}
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		context = null;
		setContentView(R.layout.activity_null);
	}
}
