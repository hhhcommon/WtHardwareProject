package com.wotingfm.ui.music.main;

import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;

import com.wotingfm.R;
import com.wotingfm.ui.music.program.main.ProgramFragment;
import com.wotingfm.util.SequenceUUID;

/**
 * 内容主页
 * @author 辛龙
 * 2016年2月2日
 */
public class ProgramActivity extends FragmentActivity {
	public static ProgramActivity context;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_view_other);
		context=this;
		setType();

		ProgramActivity.open(new ProgramFragment());
	}

	public static void open(Fragment frg) {
		context.getSupportFragmentManager().beginTransaction()
				.add(R.id.fragment_content, frg)
				.addToBackStack(SequenceUUID.getUUID())
				.commit();
	}

	public static void close() {
		context.getSupportFragmentManager().popBackStackImmediate();
	}

	public static void hideShow(Fragment from,Fragment to) {
		context.getSupportFragmentManager().beginTransaction().
				hide(from).show(to).commit();
	}
	// 适配顶栏样式
	private void setType() {
		String a = android.os.Build.VERSION.RELEASE;
		Log.e("系统版本号", a + "");
		Log.e("系统版本号截取", a.substring(0, a.indexOf(".")) + "");
		boolean v = false;
		if (Integer.parseInt(a.substring(0, a.indexOf("."))) >= 5) {
			v = true;
		}
		View tv_main = findViewById(R.id.tv_main);
		if (v) {
			tv_main.setVisibility(View.VISIBLE);
		} else {
			tv_main.setVisibility(View.GONE);
		}
	}
	@Override
	protected void onDestroy() {
		super.onDestroy();
		context=null;
	}

	/*
     * 手机实体返回按键的处理,硬件不加入
     */
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (event.getAction() == KeyEvent.ACTION_DOWN && KeyEvent.KEYCODE_BACK == keyCode) {
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}
	// 设置android app 的字体大小不受系统字体大小改变的影响
	@Override
	public Resources getResources() {
		Resources res = super.getResources();
		Configuration config = new Configuration();
		config.setToDefaults();
		res.updateConfiguration(config, res.getDisplayMetrics());
		return res;
	}

}
