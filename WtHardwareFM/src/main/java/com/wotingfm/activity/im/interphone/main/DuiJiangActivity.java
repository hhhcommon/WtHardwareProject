package com.wotingfm.activity.im.interphone.main;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.wotingfm.R;
import com.wotingfm.activity.common.adapter.MyFragmentPagerAdapter;
import com.wotingfm.activity.im.interphone.chat.fragment.ChatFragment;
import com.wotingfm.activity.im.interphone.creategroup.main.CreateGroupMainActivity;
import com.wotingfm.activity.im.interphone.find.FindActivity;
import com.wotingfm.activity.im.interphone.linkman.fragment.LinkManFragment;
import com.wotingfm.activity.im.interphone.notify.activity.NotifyNewActivity;
import com.wotingfm.activity.im.interphone.scanning.activity.CaptureActivity;
import com.wotingfm.activity.person.login.activity.LoginActivity;
import com.wotingfm.common.constant.StringConstant;

import java.util.ArrayList;

/**
 * 对讲activity
 * @author 辛龙
 * 2016年1月19日
 */
public class DuiJiangActivity extends FragmentActivity {
	private static TextView view1;
	private static TextView view2;
	private ArrayList<Fragment> fragmentList;
	//	private Dialog adddialog;
	private LinearLayout lin_more;
	private SharedPreferences sharedPreferences;
	private PopupWindow adddialog;
	private static ViewPager mPager;
	private static DuiJiangActivity context;
	public static final String UPDATA_GROUP = "com.woting.UPDATA_GROUP";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_duijiang);
		context = this;
		sharedPreferences = context.getSharedPreferences("wotingfm", Context.MODE_PRIVATE);

		InitTextView();		// 初始化视图
		InitViewPager();	// 初始化ViewPager
		dialog(); 			// 初始化功能弹出框
	}

	/*
	 * "更多" 对话框
	 */
	private void dialog() {
		View dialog = LayoutInflater.from(context).inflate(R.layout.dialoga, null);
		LinearLayout lin_news = (LinearLayout) dialog.findViewById(R.id.lin_news);	// 新消息
		LinearLayout lin_a = (LinearLayout) dialog.findViewById(R.id.lin_a);		// 添加好友
		LinearLayout lin_b = (LinearLayout) dialog.findViewById(R.id.lin_b);		// 加入群组
		LinearLayout lin_c = (LinearLayout) dialog.findViewById(R.id.lin_c);		// 创建群组
		LinearLayout lin_d = (LinearLayout) dialog.findViewById(R.id.lin_d);		// 扫一扫

		/**
		 * 跳转到新消息
		 */
		lin_news.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				String login = sharedPreferences.getString(StringConstant.ISLOGIN, "false");// 是否登录
				if (login != null && !login.trim().equals("") && login.equals("true")) {
				} else {
				}
				//				startActivity(new Intent(context, ContactAddInformationActivity.class));
				adddialog.dismiss();
			}
		});

		/*
		 * 跳转到添加好友
		 */
		lin_a.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				String login = sharedPreferences.getString(StringConstant.ISLOGIN, "false");// 是否登录
				if (login != null && !login.trim().equals("") && login.equals("true")) {
//					Intent Intent = new Intent(context, FindActivity.class);
//					Bundle bundle = new Bundle();
//					bundle.putString(StringConstant.FIND_TYPE, StringConstant.FIND_TYPE_PERSON);
//					Intent.putExtras(bundle);
//					startActivity(Intent);
				} else {
//					startActivity(new Intent(context, LoginActivity.class));
				}

				Intent Intent = new Intent(context, FindActivity.class);
				Bundle bundle = new Bundle();
				bundle.putString(StringConstant.FIND_TYPE, StringConstant.FIND_TYPE_PERSON);
				Intent.putExtras(bundle);
				startActivity(Intent);

				adddialog.dismiss();
			}
		});

		/*
		 * 跳转到加入群组
		 */
		lin_b.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				String login = sharedPreferences.getString(StringConstant.ISLOGIN, "false");// 是否登录
				if (login != null && !login.trim().equals("") && login.equals("true")) {
//					Intent Intent = new Intent(context, FindActivity.class);
//					Bundle bundle = new Bundle();
//					bundle.putString(StringConstant.FIND_TYPE, StringConstant.FIND_TYPE_PERSON);
//					Intent.putExtras(bundle);
//					startActivity(Intent);
				} else {
//					startActivity(new Intent(context, LoginActivity.class));
				}

				Intent Intent = new Intent(context, FindActivity.class);
				Bundle bundle = new Bundle();
				bundle.putString(StringConstant.FIND_TYPE, StringConstant.FIND_TYPE_GROUP);
				Intent.putExtras(bundle);
				startActivity(Intent);

				adddialog.dismiss();
			}
		});

		/*
		 * 跳转到创建讨论组
		 */
		lin_c.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				String login = sharedPreferences.getString(StringConstant.ISLOGIN, "false");// 是否登录
				if (login != null && !login.trim().equals("") && login.equals("true")) {
					Intent intent = new Intent(context, CreateGroupMainActivity.class);
					startActivity(intent);
				} else {
					startActivity(new Intent(context, LoginActivity.class));
				}
				adddialog.dismiss();
			}
		});

		/*
		 * 跳转到扫描界面
		 */
		lin_d.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				String login = sharedPreferences.getString(StringConstant.ISLOGIN, "false");// 是否登录
				if (login != null && !login.trim().equals("") && login.equals("true")) {
					startActivity(new Intent(context, CaptureActivity.class));
				} else {
					startActivity(new Intent(context, LoginActivity.class));
				}
				adddialog.dismiss();
			}
		});

		adddialog = new PopupWindow(dialog);
//		adddialog = new PopupWindow(dialog, LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT, true);
		// 使其聚集
		adddialog.setFocusable(true);
		// 在android4.0/5.0系统上点击内容外部区域无法关闭问题
//		adddialog.setBackgroundDrawable(new BitmapDrawable());
		adddialog.setBackgroundDrawable(new ColorDrawable(0x00000000));
		// 设置允许在外点击消失
		adddialog.setOutsideTouchable(true);
		// 控制popupwindow的宽度和高度自适应  
		dialog.measure(View.MeasureSpec.UNSPECIFIED,   View.MeasureSpec.UNSPECIFIED);  
		adddialog.setWidth(dialog.getMeasuredWidth());  
		adddialog.setHeight(dialog.getMeasuredHeight() );  

		
		//		adddialog = new Dialog(context, R.style.MyDialog_duijiang);
		//		adddialog.setContentView(dialog);
		//		adddialog.setCanceledOnTouchOutside(true);
		//		adddialog.getWindow().setGravity(Gravity.TOP | Gravity.RIGHT);
		//		adddialog.getWindow().setBackgroundDrawableResource(R.color.dialog);
	}

	/*
	 * 初始化视图
	 */
	private void InitTextView() {
		lin_more = (LinearLayout) findViewById(R.id.lin_more);
		LinearLayout lin_news = (LinearLayout) findViewById(R.id.lin_news);

		/*
		 * 跳转到新消息界面
		 */
		lin_news.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(context, NotifyNewActivity.class);
				startActivity(intent);
			}
		});
		view1 = (TextView) findViewById(R.id.tv_guid1);
		view2 = (TextView) findViewById(R.id.tv_guid2);
		view1.setOnClickListener(new txListener(0));
		view2.setOnClickListener(new txListener(1));

		/*
		 * 弹出功能弹出框
		 */
		lin_more.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if(adddialog!=null&&adddialog.isShowing()){
					adddialog.dismiss();
				}else{
					//					adddialog.show();
					adddialog.showAsDropDown(lin_more);
				}
			}
		});
	}

	/*
	 * TextView 点击事件监听
	 */
	public class txListener implements OnClickListener {
		private int index = 0;

		public txListener(int i) {
			index = i;
		}

		@Override
		public void onClick(View v) {
			mPager.setCurrentItem(index);
			if (index == 0) {
				view1.setTextColor(context.getResources().getColor(R.color.dinglan_orange));
				view2.setTextColor(context.getResources().getColor(R.color.white));
				view1.setBackgroundDrawable(context.getResources().getDrawable(R.drawable.color_wt_circle_home_white));
				view2.setBackgroundDrawable(context.getResources().getDrawable(R.drawable.color_wt_circle_orange));
			} else if (index == 1) {
				view1.setTextColor(context.getResources().getColor(R.color.white));
				view2.setTextColor(context.getResources().getColor(R.color.dinglan_orange));
				view1.setBackgroundDrawable(context.getResources().getDrawable(R.drawable.color_wt_circle_orange));
				view2.setBackgroundDrawable(context.getResources().getDrawable(R.drawable.color_wt_circle_home_white));
			}
		}
	}

	public static void update() {
		mPager.setCurrentItem(0);
		view1.setTextColor(context.getResources().getColor(R.color.dinglan_orange));
		view2.setTextColor(context.getResources().getColor(R.color.white));
		view1.setBackgroundDrawable(context.getResources().getDrawable(R.drawable.color_wt_circle_home_white));
		view2.setBackgroundDrawable(context.getResources().getDrawable(R.drawable.color_wt_circle_orange));
	}

	/*
	 * 初始化ViewPager
	 */
	public void InitViewPager() {
		mPager = (ViewPager) findViewById(R.id.viewpager);
		mPager.setOffscreenPageLimit(1);
		fragmentList = new ArrayList<Fragment>();
		Fragment btFragment = new ChatFragment();			 // 电台首页
		Fragment btFragment1 = new LinkManFragment();				 // 通讯录
		fragmentList.add(btFragment);
		fragmentList.add(btFragment1);
		mPager.setAdapter(new MyFragmentPagerAdapter(getSupportFragmentManager(), fragmentList));
		mPager.setOnPageChangeListener(new MyOnPageChangeListener());// 页面变化时的监听器
		mPager.setCurrentItem(0);									 // 设置当前显示标签页为第一页
	}

	/*
	 * ViewPager 监听事件
	 */
	public class MyOnPageChangeListener implements OnPageChangeListener {

		@Override
		public void onPageScrolled(int arg0, float arg1, int arg2) {
		}

		@Override
		public void onPageScrollStateChanged(int arg0) {
		}

		@Override
		public void onPageSelected(int arg0) {
			if (arg0 == 0) {
				view1.setTextColor(context.getResources().getColor(R.color.dinglan_orange));
				view2.setTextColor(context.getResources().getColor(R.color.white));
				view1.setBackgroundDrawable(context.getResources().getDrawable(R.drawable.color_wt_circle_home_white));
				view2.setBackgroundDrawable(context.getResources().getDrawable(R.drawable.color_wt_circle_orange));
			} else if (arg0 == 1) {
				view1.setTextColor(context.getResources().getColor(R.color.white));
				view2.setTextColor(context.getResources().getColor(R.color.dinglan_orange));
				view1.setBackgroundDrawable(context.getResources().getDrawable(R.drawable.color_wt_circle_orange));
				view2.setBackgroundDrawable(context.getResources().getDrawable(R.drawable.color_wt_circle_home_white));
			}
		}
	}




}
