package com.wotingfm.activity.music.main;

import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;

import com.umeng.analytics.MobclickAgent;
import com.wotingfm.R;
import com.wotingfm.activity.common.baseadapter.MyFragmentPagerAdapter;
import com.wotingfm.activity.im.interphone.notify.activity.NotifyNewActivity;
import com.wotingfm.activity.music.player.fragment.PlayerFragment;
import com.wotingfm.activity.music.program.main.ProgramFragment;
import com.wotingfm.activity.music.search.activity.SearchLikeActivity;
import com.wotingfm.common.application.BSApplication;
import com.wotingfm.util.ToastUtils;

import java.util.ArrayList;

/**
 * 内容主页
 * @author 辛龙
 * 2016年2月2日
 */
public class HomeActivity extends FragmentActivity {
	private static TextView view1;
	private static TextView view2;
	private static HomeActivity context;
	private static ViewPager mPager;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_home);
		context = this;
		InitTextView();
		InitViewPager();
	}

	private void InitTextView() {
		view1 = (TextView) findViewById(R.id.tv_guid1);
        view1.setOnClickListener(new txListener(0));

		view2 = (TextView) findViewById(R.id.tv_guid2);
        view2.setOnClickListener(new txListener(1));

		findViewById(R.id.lin_find).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// 跳转到搜索界面  原来的代码 要加在这里
				startActivity(new Intent(context, SearchLikeActivity.class));
			}
		});

        findViewById(R.id.lin_news).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                // 跳转到通知界面
                startActivity(new Intent(context, NotifyNewActivity.class));
            }
        });
	}

    // 更新视图
    private void updateView(int index) {
        if (index == 0) {
            view1.setTextColor(context.getResources().getColor(R.color.dinglan_orange));
            view2.setTextColor(context.getResources().getColor(R.color.white));
            view1.setBackgroundDrawable(context.getResources().getDrawable(	R.drawable.color_wt_circle_home_white));
            view2.setBackgroundDrawable(context.getResources().getDrawable(R.drawable.color_wt_circle_orange));
        } else if (index == 1) {
            view1.setTextColor(context.getResources().getColor(R.color.white));
            view2.setTextColor(context.getResources().getColor(R.color.dinglan_orange));
            view1.setBackgroundDrawable(context.getResources().getDrawable(R.drawable.color_wt_circle_orange));
            view2.setBackgroundDrawable(context.getResources().getDrawable(R.drawable.color_wt_circle_home_white));
        }
    }

	public class txListener implements OnClickListener {
		private int index = 0;
		public txListener(int i) {
			index = i;
		}

		@Override
		public void onClick(View v) {
			mPager.setCurrentItem(index);
            updateView(index);
		}
	}

	/*
	 * 初始化ViewPager
	 */
	public void InitViewPager() {
		mPager = (ViewPager) findViewById(R.id.viewpager);
		mPager.setOffscreenPageLimit(1);
		ArrayList<Fragment> fragmentList = new ArrayList<>();
		PlayerFragment playFragment = new PlayerFragment();
		ProgramFragment newsFragment = new ProgramFragment();
 	    fragmentList.add(playFragment);
		fragmentList.add(newsFragment);
		 //mPager.setAdapter(new MyFragmentChildPagerAdapter(getChildFragmentManager(), fragmentList));
		mPager.setAdapter(new MyFragmentPagerAdapter(getSupportFragmentManager(), fragmentList));
		mPager.setOnPageChangeListener(new MyOnPageChangeListener());	// 页面变化时的监听器
		mPager.setCurrentItem(0);	// 设置当前显示标签页为第一页mPager
	}

	public class MyOnPageChangeListener implements OnPageChangeListener {
		@Override
		public void onPageScrolled(int arg0, float arg1, int arg2) {
		}
		@Override
		public void onPageScrollStateChanged(int arg0) {
		}
		@Override
		public void onPageSelected(int arg0) {
            updateView(arg0);
		}
	}

	public static void UpdateViewPager() {
		mPager.setCurrentItem(0);// 设置当前显示标签页为第一页mPager
		view1.setTextColor(context.getResources().getColor(R.color.dinglan_orange));
		view2.setTextColor(context.getResources().getColor(R.color.white));
		view1.setBackgroundDrawable(context.getResources().getDrawable(R.drawable.color_wt_circle_home_white));
		view2.setBackgroundDrawable(context.getResources().getDrawable(R.drawable.color_wt_circle_orange));
	}


	//手机实体返回按键的处理 与onbackpress同理
	long waitTime = 2000;
	long touchTime = 0;
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (event.getAction() == KeyEvent.ACTION_DOWN && KeyEvent.KEYCODE_BACK == keyCode) {
			long currentTime = System.currentTimeMillis();
			if ((currentTime - touchTime) >= waitTime) {
				ToastUtils.show_always(this, "再按一次退出");
				touchTime = currentTime;
			} else {
				BSApplication.onStop();
				MobclickAgent.onKillProcess(this);
				finish();
				android.os.Process.killProcess(android.os.Process.myPid());
			}
			return true;
		}else if(event.getAction() == KeyEvent.ACTION_DOWN &&keyCode == KeyEvent.KEYCODE_VOLUME_DOWN){
			// 音量减小时应该执行的功能代码
			ToastUtils.show_always(this, "音量减小时应该执行的功能代码");
			return true;
		}else if(event.getAction() == KeyEvent.ACTION_DOWN &&keyCode == KeyEvent.KEYCODE_VOLUME_UP){
			// 音量增大时应该执行的功能代码
			ToastUtils.show_always(this, "音量增大时应该执行的功能代码");
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}

	@Override
	public Resources getResources() {
		Resources res = super.getResources();
		Configuration config = new Configuration();
		config.setToDefaults();
		res.updateConfiguration(config, res.getDisplayMetrics());
		return res;
	}

}
