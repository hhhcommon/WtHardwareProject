package com.wotingfm.activity.im.interphone.alert;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.squareup.picasso.Picasso;
import com.wotingfm.R;
import com.wotingfm.activity.common.baseactivity.BaseActivity;
import com.wotingfm.activity.common.main.MainActivity;
import com.wotingfm.activity.im.interphone.chat.dao.SearchTalkHistoryDao;
import com.wotingfm.activity.im.interphone.chat.fragment.ChatFragment;
import com.wotingfm.activity.im.interphone.chat.model.DBTalkHistorary;
import com.wotingfm.activity.im.interphone.main.DuiJiangActivity;
import com.wotingfm.common.config.GlobalConfig;
import com.wotingfm.helper.InterPhoneControlHelper;
import com.wotingfm.manager.MyActivityManager;
import com.wotingfm.service.SubclassService;
import com.wotingfm.util.BitmapUtils;
import com.wotingfm.util.CommonUtils;


public class ReceiveAlertActivity extends BaseActivity implements OnClickListener {
	public static ReceiveAlertActivity instance;
	private ImageView imageview;
	private TextView tv_name;
	private LinearLayout lin_call;
	private LinearLayout lin_guaduan;
	private String image;
	private String name;
//	private TextView tv_news;
	private SearchTalkHistoryDao dbDao;
	private String id;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.dialog_receivecall);
		instance = this;
		//设置界面
//		tv_news = (TextView) findViewById(R.id.tv_news);	
		imageview = (ImageView) findViewById(R.id.image);	
		tv_name = (TextView) findViewById(R.id.tv_name);	
		lin_call = (LinearLayout) findViewById(R.id.lin_call);	
		lin_guaduan = (LinearLayout) findViewById(R.id.lin_guaduan);	
		
		
		//查找当前好友的展示信息
		id = SubclassService.callerId;
		if(GlobalConfig.list_person!=null){
		for(int i=0; i<GlobalConfig.list_person.size(); i++){
			if(id.equals(GlobalConfig.list_person.get(i).getUserId())){
				image = GlobalConfig.list_person.get(i).getPortraitBig();
				name = GlobalConfig.list_person.get(i).getUserName();
				break;
			}
		}
		}else{
			image = null;
			name = "我听科技";
		}
		
		//适配好友展示信息
		tv_name.setText(name);
		if(image==null||image.equals("")||image.equals("null")||image.trim().equals("")){
			Bitmap bmp = BitmapUtils.readBitMap(instance, R.mipmap.wt_image_tx_hy);
			imageview.setImageBitmap(bmp);
		}else{
			String url = GlobalConfig.imageurl+image;
			Picasso.with(instance).load(url.replace("\\/", "/")).resize(100, 100).centerCrop().into(imageview);
		}
		
		//设置监听
		lin_call.setOnClickListener(this);
		lin_guaduan.setOnClickListener(this);
		initDao();		//初始化数据库
	}

	private void initDao() {
		dbDao = new SearchTalkHistoryDao(instance);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.lin_call:
			SubclassService.isallow=true;
			InterPhoneControlHelper.PersonTalkAllow(getApplicationContext(), SubclassService.callid, SubclassService.callerId);//接收应答
			if(SubclassService.musicPlayer!=null){
				SubclassService.musicPlayer.stop();
				SubclassService.musicPlayer = null;
			}
			ChatFragment.isCalling=true;
//			Intent intent = new Intent(getApplicationContext(),MainActivity.class);
//			//intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP |Intent.FLAG_ACTIVITY_SINGLE_TOP);
//			intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK| Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
//			startActivity(intent);
			addUser();
			break;
		case R.id.lin_guaduan:
			SubclassService.isallow=true;
			InterPhoneControlHelper.PersonTalkOver(getApplicationContext(), SubclassService.callid, SubclassService.callerId);//拒绝应答
			if(SubclassService.musicPlayer!=null){
				SubclassService.musicPlayer.stop();
				SubclassService.musicPlayer = null;
			}
			this.finish();
			break;
		}
	}

	public void addUser() {
		//获取最新激活状态的数据
		String addTime = Long.toString(System.currentTimeMillis());
		String bjUserId =CommonUtils.getUserId(instance);
		//如果该数据已经存在数据库则删除原有数据，然后添加最新数据
		dbDao.deleteHistory(id);
		DBTalkHistorary history = new DBTalkHistorary( bjUserId,  "user",  id, addTime);
		dbDao.addTalkHistory(history);
		DBTalkHistorary talkDb = dbDao.queryHistory().get(0);//得到数据库里边数据
		ChatFragment.zhiDingPerson(talkDb);
		MyActivityManager mam = MyActivityManager.getInstance();
		mam.finishAllActivity();
		//对讲主页界面更新
		MainActivity.tabHost.setCurrentTabByTag("one");
		DuiJiangActivity.update();
		finish();
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (event.getAction() == KeyEvent.ACTION_DOWN&& KeyEvent.KEYCODE_BACK == keyCode) {
			SubclassService.isallow=true;
			InterPhoneControlHelper.PersonTalkOver(getApplicationContext(), SubclassService.callid, SubclassService.callerId);//拒绝应答
			if(SubclassService.musicPlayer != null){
				SubclassService.musicPlayer.stop();
				SubclassService.musicPlayer = null;
			}
			finish();
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		instance = null;
		imageview = null;
		tv_name = null;
		lin_call = null;
		lin_guaduan = null;
		image = null;
		name = null;
		id = null;
		if(dbDao != null){
			dbDao = null;
		}
		setContentView(R.layout.activity_null);
	}
}
