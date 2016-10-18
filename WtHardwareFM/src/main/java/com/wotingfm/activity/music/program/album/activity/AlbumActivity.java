package com.wotingfm.activity.music.program.album.activity;

import android.app.Dialog;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

import com.umeng.socialize.Config;
import com.umeng.socialize.ShareAction;
import com.umeng.socialize.UMShareAPI;
import com.umeng.socialize.bean.SHARE_MEDIA;
import com.umeng.socialize.media.UMImage;
import com.wotingfm.R;
import com.wotingfm.activity.common.baseadapter.MyFragmentPagerAdapter;
import com.wotingfm.activity.music.player.adapter.ImageAdapter;
import com.wotingfm.activity.music.player.model.LanguageSearchInside;
import com.wotingfm.activity.music.player.model.sharemodel;
import com.wotingfm.activity.music.program.album.fragment.DetailsFragment;
import com.wotingfm.activity.music.program.album.fragment.ProgramFragment;
import com.wotingfm.activity.music.program.fmlist.model.RankInfo;
import com.wotingfm.common.config.GlobalConfig;
import com.wotingfm.util.DialogUtils;
import com.wotingfm.util.ShareUtils;
import com.wotingfm.util.ToastUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * 专辑页
 * @author 辛龙 
 * 2016年4月1日
 */
public class AlbumActivity extends FragmentActivity implements OnClickListener {
	private AlbumActivity context;
	public static String ContentName;
	public static String ContentDesc;
	public static String id;
	public static String ContentImg;
	private TextView tv_album_name;
	private TextView mback;
	private ImageView img_right;
	private ViewPager mpager;
	public static String img_album;
	private UMImage image;
	public static Dialog Sharedialog;
	private int screenw;
	private Dialog dialog1;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_album);
		context = this;
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);// 透明状态栏
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);// 透明导航栏
		setview();            // 设置界面
		handleIntent();
		setlistener();
		UMShareAPI.get(context);// 初始化友盟
		sharedialog();
	}

	private void sharedialog() {
		final View dialog = LayoutInflater.from(context).inflate(R.layout.dialog_sharedialog, null);
		GridView mgallery = (GridView) dialog.findViewById(R.id.share_gallery);
		TextView tv_cancle = (TextView) dialog.findViewById(R.id.tv_cancle);
		Sharedialog = new Dialog(context, R.style.MyDialog);
		// 从底部上升到一个位置
		Sharedialog.setContentView(dialog);
		Window window = Sharedialog.getWindow();
		DisplayMetrics dm = new DisplayMetrics();
		context.getWindowManager().getDefaultDisplay().getMetrics(dm);
		screenw = dm.widthPixels;
		ViewGroup.LayoutParams params = dialog.getLayoutParams();
		params.width = (int) screenw;
		dialog.setLayoutParams(params);
		window.setGravity(Gravity.BOTTOM);
		window.setWindowAnimations(R.style.sharestyle);
		Sharedialog.setCanceledOnTouchOutside(true);
		Sharedialog.getWindow().setBackgroundDrawableResource(R.color.dialog);
		 /* Sharedialog.show(); */
		dialog1 = DialogUtils.Dialogphnoshow(context, "通讯中", dialog1);
		Config.dialog = dialog1;
		final List<sharemodel> mylist = ShareUtils.getShareModelList();
		ImageAdapter shareadapter = new ImageAdapter(context, mylist);
		mgallery.setAdapter(shareadapter);
		mgallery.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				SHARE_MEDIA Platform = mylist.get(position).getSharePlatform();
				CallShare(Platform);
				Sharedialog.dismiss();
			}
		});
		tv_cancle.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (Sharedialog.isShowing()) {
					Sharedialog.dismiss();
				}
			}
		});

	}
	protected void CallShare(SHARE_MEDIA Platform) {
		String sharename;
		String shareDesc;
		String shareContentImg;
		String shareurl;
		if (GlobalConfig.playerobject != null) {
			if (GlobalConfig.playerobject.getContentName() != null
					&& !GlobalConfig.playerobject.getContentName().equals("")) {
				sharename = GlobalConfig.playerobject.getContentName();
			} else {
				sharename = "我听我享听";
			}
			if (GlobalConfig.playerobject.getContentDesc() != null
					&& !GlobalConfig.playerobject.getContentDesc().equals("")) {
				shareDesc = GlobalConfig.playerobject.getContentDesc();
			} else {
				shareDesc = "暂无本节目介绍";
			}
			if (GlobalConfig.playerobject.getContentImg() != null
					&& !GlobalConfig.playerobject.getContentImg().equals("")) {
				shareContentImg = GlobalConfig.playerobject.getContentImg();
				image = new UMImage(context, shareContentImg);
			} else {
				shareContentImg = "http://182.92.175.134/img/logo-web.png";
				image = new UMImage(context, shareContentImg);
			}
			if (GlobalConfig.playerobject.getContentShareURL() != null
					&& !GlobalConfig.playerobject.getContentShareURL().equals("")) {
				shareurl = GlobalConfig.playerobject.getContentShareURL();
			} else {
				shareurl = "http://www.wotingfm.com/";
			}
			new ShareAction(context).setPlatform(Platform).withMedia(image)
					.withText(shareDesc).withTitle(sharename).withTargetUrl(shareurl).share();
		} else {
			ToastUtils.show_short(context, "没有数据");
		}
	}
	private void setlistener() {
		mback.setOnClickListener(context);
	}

	private void setview(){
		tv_album_name = (TextView) findViewById(R.id.tv_head_name);
		mback=(TextView)findViewById(R.id.wt_back);
		img_right=(ImageView)findViewById(R.id.img_head_right);
		mpager=(ViewPager)findViewById(R.id.mpager);
		ArrayList<Fragment> fragmentList = new ArrayList<>();
		DetailsFragment mdetailsFragment=new DetailsFragment();
		ProgramFragment mprogramFragment=new ProgramFragment();
		fragmentList.add(mdetailsFragment);
		fragmentList.add(mprogramFragment);
		mpager.setAdapter(new MyFragmentPagerAdapter(getSupportFragmentManager(), fragmentList));
		mpager.setOnPageChangeListener(new MyOnPageChangeListener());
		mpager.setCurrentItem(0);
		mpager.setOffscreenPageLimit(1);
		img_right.setOnClickListener(new AlbumChangeClickListener(1));
	}
	private void handleIntent() {
		String type = this.getIntent().getStringExtra("type");
		if (type != null && type.trim().equals("radiolistactivity")) {
			RankInfo list = (RankInfo) getIntent().getSerializableExtra("list");
			ContentName = list.getContentName();
			ContentDesc=list.getContentDesc();
			id = list.getContentId();
		} else if (type != null && type.trim().equals("recommend")) {
			ContentName=this.getIntent().getStringExtra("conentname");
			ContentDesc=this.getIntent().getStringExtra("conentname");
			id=this.getIntent().getStringExtra("conentid");
			ContentImg=this.getIntent().getStringExtra("contentimg");
		} else if (type != null && type.trim().equals("search")) {
			RankInfo list = (RankInfo) getIntent().getSerializableExtra("list");
			ContentName = list.getContentName();
			ContentDesc=list.getContentDesc();
			id = list.getContentId();
		} else if (type != null && type.trim().equals("main")) {
			//congmainlaide 再做一个
			ContentName = this.getIntent().getStringExtra("conentname");
			id = this.getIntent().getStringExtra("id");
		} else if(type != null && type.trim().equals("player")){
			ContentName=this.getIntent().getStringExtra("conentname");
			ContentDesc=this.getIntent().getStringExtra("conentname");
			id=this.getIntent().getStringExtra("conentid");
			ContentImg=this.getIntent().getStringExtra("contentimg");
		}else {
			LanguageSearchInside list = (LanguageSearchInside) getIntent().getSerializableExtra("list");
			ContentName = list.getContentName();
			ContentDesc=list.getContentDesc();
			id = list.getContentId();
		}
		if (ContentName != null && !ContentName.equals("")) {
			tv_album_name.setText(ContentName);
		} else {
			tv_album_name.setText("未知");
		}
		Log.e("本节目的专辑ID为", id + "");

	}

	@Override
	public void onClick(View v) {
		switch (v.getId()){
			case R.id.wt_back:
				finish();
				break;
	}
	}
	public class MyOnPageChangeListener implements ViewPager.OnPageChangeListener {

		@Override
		public void onPageScrolled(int arg0, float arg1, int arg2) {
		}

		@Override
		public void onPageScrollStateChanged(int arg0) {
		}

		@Override
		public void onPageSelected(int arg0) {

		}
	}
	public class AlbumChangeClickListener implements OnClickListener {


		public AlbumChangeClickListener(int i) {

		}

		@Override
		public void onClick(View v) {
			mpager.setCurrentItem(1);
		}
	}
}
