package com.wotingfm.activity.mine.contactus;

import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.LinearLayout;

import com.shenstec.activity.BaseActivity;
import com.wotingfm.R;

/**
 * 联系我们界面
 * 作者：xinlong on 2016/8/8
 * 邮箱：645700751@qq.com
 */
public class ContactUsActivity extends BaseActivity implements OnClickListener {
	private ContactUsActivity context;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_contactus);
		context = this;
		setView();	// 设置界面
	}

	/**
	 * 初始化视图
	 */
	private void setView() {
		LinearLayout head_left_btn = (LinearLayout) findViewById(R.id.head_left_btn);	// 返回
		head_left_btn.setOnClickListener(context);
	}

	@Override
	public void onClick(View v) {
		super.onClick(v);
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
