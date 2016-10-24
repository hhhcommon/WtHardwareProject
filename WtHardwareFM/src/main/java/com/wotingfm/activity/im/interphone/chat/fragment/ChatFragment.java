package com.wotingfm.activity.im.interphone.chat.fragment;

import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.android.volley.VolleyError;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.squareup.picasso.Picasso;
import com.wotingfm.R;
import com.wotingfm.activity.im.common.message.MessageUtils;
import com.wotingfm.activity.im.common.message.MsgNormal;
import com.wotingfm.activity.im.common.message.content.MapContent;
import com.wotingfm.activity.im.common.model.ListInfo;
import com.wotingfm.activity.im.interphone.alert.CallAlertActivity;
import com.wotingfm.activity.im.interphone.chat.adapter.ChatListAdapter;
import com.wotingfm.activity.im.interphone.chat.adapter.GroupPersonAdapter;
import com.wotingfm.activity.im.interphone.chat.dao.SearchTalkHistoryDao;
import com.wotingfm.activity.im.interphone.chat.model.DBTalkHistorary;
import com.wotingfm.activity.im.interphone.chat.model.GroupTalkInside;
import com.wotingfm.activity.im.interphone.chat.model.TalkListGP;
import com.wotingfm.activity.im.interphone.creategroup.frienddetails.TalkPersonNewsActivity;
import com.wotingfm.activity.im.interphone.find.add.FriendAddActivity;
import com.wotingfm.activity.im.interphone.groupmanage.groupdetail.activity.GroupDetailActivity;
import com.wotingfm.activity.im.interphone.linkman.model.LinkMan;
import com.wotingfm.activity.im.interphone.linkman.model.TalkGroupInside;
import com.wotingfm.activity.im.interphone.main.DuiJiangActivity;
import com.wotingfm.activity.person.login.LoginActivity;
import com.wotingfm.common.application.BSApplication;
import com.wotingfm.common.config.GlobalConfig;
import com.wotingfm.common.constant.BroadcastConstant;
import com.wotingfm.common.constant.StringConstant;
import com.wotingfm.common.volley.VolleyCallback;
import com.wotingfm.common.volley.VolleyRequest;
import com.wotingfm.manager.InterPhoneControlManager;
import com.wotingfm.service.VoiceStreamRecordService;
import com.wotingfm.util.BitmapUtils;
import com.wotingfm.util.CommonUtils;
import com.wotingfm.util.DialogUtils;
import com.wotingfm.util.ToastUtils;
import com.wotingfm.util.VibratorUtils;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 对讲机-获取联系列表，包括群组跟个人
 * 作者：xinlong on 2016/1/18 21:18
 * 邮箱：645700751@qq.com
 */
public class ChatFragment extends Fragment implements OnClickListener{
	public static FragmentActivity context;
	private static ChatListAdapter adapter;
	private static ListView mListView;
	private  MessageReceiver Receiver;
	private static ImageView image_grouptx;
	private static TextView tv_groupname;
	private static SearchTalkHistoryDao dbDao;
	private View rootView;
	private TextView talkingName;
	private GridView gridView_person;
	private android.app.Dialog dialog;
	private static TextView tv_num;
	private static TextView tv_grouptype;
	public static LinearLayout lin_head;
	private static TextView tv_allnum;
	public static boolean isCalling=false;//是否是在通话状态;
	private static List<DBTalkHistorary> historydatabaselist;//list里边的数据
	private SharedPreferences shared= BSApplication.SharedPreferences;
	public static LinearLayout lin_second;
	private RelativeLayout Relative_listview;
	public static LinearLayout lin_notalk;
	public static LinearLayout lin_personhead;
	private static TextView tv_personname;
	private static ImageView image_persontx;
	private  ImageView image_personvoice;
	private ImageView image_group_persontx;
	private ImageView image_voice;
	private TextView talking_news;
	private static AnimationDrawable draw;
	private AnimationDrawable draw_group;
	private String UserName;
	private static android.app.Dialog confirmDialog;
	private static String groupId;
	public static String interPhoneType;
	public static String interPhoneId;
	private static List<GroupTalkInside> groupPersonList =new ArrayList<GroupTalkInside>();//组成员
	private static ArrayList<GroupTalkInside> groupPersonLists = new ArrayList<GroupTalkInside>();
	private static ArrayList<TalkListGP> allList = new ArrayList<TalkListGP>();//所有数据库数据
	private static int enterGroupType;
	private static int dialogType;
	private static boolean isTalking = false;
	private long Vibrate = 100;
	private TextView gridView_tv;
	private static String phoneId;
	private static List<ListInfo> listInFo;
	private static Gson gson = new Gson();
	private String tag = "TALKOLDLIST_VOLLEY_REQUEST_CANCEL_TAG";
	private boolean isCancelRequest;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		context = this.getActivity();
		initDao();// 初始化数据库
		rtRec();//注册广播接收socketService的数据
	}

	private void rtRec() {
		if(Receiver == null) {
			Receiver=new MessageReceiver();
			IntentFilter filter=new IntentFilter();
			filter.addAction(BroadcastConstant.SOCKET_PUSH);
			filter.addAction(BroadcastConstant.UP_DATA_GROUP);
			filter.addAction(BroadcastConstant.PUSH_VOICE_IMAGE_REFRESH);
			context.registerReceiver(Receiver, filter);

			IntentFilter filter1=new IntentFilter();
			filter1.addAction(BroadcastConstant.PUSH_BACK);
			filter1.setPriority(500);
			context.registerReceiver(Receiver, filter1);
		}
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,Bundle savedInstanceState) {
		rootView = inflater.inflate(R.layout.fragment_talkoldlist,container, false);
		setView();//设置界面
		return rootView;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		listener();
		Dialog();
	}

	@Override
	public void onResume() {
		super.onResume();
		//此处在splashActivity中refreshB设置成true
		UserName = shared.getString(StringConstant.USERNAME, "");
		String personRefresh = shared.getString(StringConstant.PERSONREFRESHB, "false");
		String isLogin = shared.getString(StringConstant.ISLOGIN, "false");
		if(isLogin.equals("true")){
			if(personRefresh.equals("true")){
				Relative_listview.setVisibility(View.VISIBLE);
				//显示此时没有人通话界面
				lin_notalk.setVisibility(View.VISIBLE);
				lin_personhead.setVisibility(View.GONE);
				lin_head.setVisibility(View.GONE);
				lin_second.setVisibility(View.GONE);
				gettxl();
				Editor et=shared.edit();
				et.putString(StringConstant.PERSONREFRESHB,"false");
				et.commit();
			}
		}else{
			//显示未登录
			Relative_listview.setVisibility(View.GONE);
			lin_second.setVisibility(View.VISIBLE);
		}
	}

	// 初始化数据库命令执行对象
	private void initDao() {
		dbDao = new SearchTalkHistoryDao(context);
	}

	private void setView() {
		lin_notalk = (LinearLayout) rootView.findViewById(R.id.lin_notalk);//没有对讲时候的界面
		lin_personhead = (LinearLayout) rootView.findViewById(R.id.lin_personhead);//有个人对讲时候的界面
		tv_personname = (TextView) rootView.findViewById(R.id.tv_personname);	//个人对讲时候的好友名字
		image_persontx = (ImageView) rootView.findViewById(R.id.image_persontx);	//个人对讲时候的好友头像
		image_personvoice= (ImageView) rootView.findViewById(R.id.image_personvoice);	//个人对讲声音波
		lin_head = (LinearLayout) rootView.findViewById(R.id.lin_head);//有群组对讲时候的界面
		image_grouptx = (ImageView) rootView.findViewById(R.id.image_grouptx);	//群组对讲时候群组头像
		tv_groupname = (TextView) rootView.findViewById(R.id.tv_groupname);	//群组对讲时候的群名
		tv_grouptype = (TextView) rootView.findViewById(R.id.tv_grouptype);	//群组对讲时候的群类型名
		tv_num = (TextView) rootView.findViewById(R.id.tv_num);	//群组对讲时候的群在线人数
		tv_allnum = (TextView) rootView.findViewById(R.id.tv_allnum);	//群组对讲时候的群所有成员人数
		talkingName = (TextView) rootView.findViewById(R.id.talkingname);	//群组对讲时候对讲人姓名
		image_group_persontx = (ImageView) rootView.findViewById(R.id.image_group_persontx);	//群组对讲时候对讲人头像
		gridView_person = (GridView) rootView.findViewById(R.id.gridView_person);	//群组对讲时候对讲成员展示
		gridView_person.setSelector(new ColorDrawable(Color.TRANSPARENT));	// 取消GridView的默认背景色
		gridView_tv= (TextView) rootView.findViewById(R.id.gridView_tv);	//群组对讲时候通话解释
		image_voice = (ImageView) rootView.findViewById(R.id.image_voice);	//群组对讲声音波
		talking_news= (TextView) rootView.findViewById(R.id.talking_news);	//群组对讲时候通话解释
		mListView = (ListView) rootView.findViewById(R.id.listView);	//
		Relative_listview = (RelativeLayout) rootView.findViewById(R.id.Relative_listview);//
		lin_second = (LinearLayout) rootView.findViewById(R.id.lin_second);//

		image_personvoice.setBackgroundResource(R.drawable.talk_show);
		draw = (AnimationDrawable) image_personvoice.getBackground();
		image_personvoice.setVisibility(View.INVISIBLE);

		image_voice.setBackgroundResource(R.drawable.talk_show);
		draw_group = (AnimationDrawable) image_voice.getBackground();
		image_voice.setVisibility(View.INVISIBLE);

		talkingName.setVisibility(View.INVISIBLE);
		ImageView imageView_guaduan1 = (ImageView) rootView.findViewById(R.id.imageView_guaduan1);
		ImageView imageView_zhuanhuan1 = (ImageView) rootView.findViewById(R.id.imageView_zhuanhuan1);
		ImageView imageView_guaduan2 = (ImageView) rootView.findViewById(R.id.imageView_guaduan2);
		ImageView imageView_zhuanhuan2 = (ImageView) rootView.findViewById(R.id.imageView_zhuanhuan2);

		lin_second.setOnClickListener(this);
		image_grouptx.setOnClickListener(this);
		imageView_guaduan1.setOnClickListener(this);
		imageView_zhuanhuan1.setOnClickListener(this);
		imageView_guaduan2.setOnClickListener(this);
		imageView_zhuanhuan2.setOnClickListener(this);
	}

	private void listener() {

	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
			case R.id.image_grouptx:
				//查看群成员
				checkGroup();
				break;
			case R.id.lin_second:
				Intent intent = new Intent(context, LoginActivity.class);
				startActivity(intent);
				break;
			case R.id.imageView_guaduan1:
				//挂断
				hangUp();
				break;
			case R.id.imageView_guaduan2:
				//挂断
				hangUp();
				break;
		}
	}

	//挂断
	private void hangUp() {
		if(interPhoneType.equals("user")){
			//挂断电话
			isCalling=false;
			InterPhoneControlManager.PersonTalkHangUp(context, InterPhoneControlManager.bdcallid);
			historydatabaselist = dbDao.queryHistory();//得到数据库里边数据
			getList();
			if(allList.size()==0){
				if(adapter==null){
					adapter=new ChatListAdapter(context, allList,"0");
					mListView.setAdapter(adapter);
				}else{
					adapter.ChangeDate(allList, "0");
				}
			}else{
				if(adapter==null){
					adapter=new ChatListAdapter(context, allList,allList.get(allList.size()-1).getId());
					mListView.setAdapter(adapter);
				}else{
					adapter.ChangeDate(allList, allList.get(allList.size()-1).getId());
				}
			}
			setListener();
			setImageView(2,"","");
			lin_notalk.setVisibility(View.VISIBLE);
			lin_personhead.setVisibility(View.GONE);
			lin_head.setVisibility(View.GONE);
			gridView_person.setVisibility(View.GONE);
			gridView_tv.setVisibility(View.GONE);
		}else{
			InterPhoneControlManager.Quit(context, interPhoneId);//退出小组
			historydatabaselist = dbDao.queryHistory();//得到数据库里边数据
			getList();
			if(allList.size()==0){
				if(adapter==null){
					adapter=new ChatListAdapter(context, allList,"0");
					mListView.setAdapter(adapter);
				}else{
					adapter.ChangeDate(allList, "0");
				}
			}else{
				if(adapter==null){
					adapter=new ChatListAdapter(context, allList,allList.get(allList.size()-1).getId());
					mListView.setAdapter(adapter);
				}else{
					adapter.ChangeDate(allList, allList.get(allList.size()-1).getId());
				}
			}
			setListener();
			setImageView(2,"","");
			lin_notalk.setVisibility(View.VISIBLE);
			lin_personhead.setVisibility(View.GONE);
			lin_head.setVisibility(View.GONE);
			gridView_person.setVisibility(View.GONE);
			gridView_tv.setVisibility(View.GONE);
		}
	}

	//查看群成员
	private void checkGroup() {
		if(groupPersonList!=null&&groupPersonList.size()!=0){
			groupPersonLists.clear();
			if(listInFo!=null&&listInFo.size()>0){
				for(int j=0;j<listInFo.size();j++){
					String id = listInFo.get(j).getUserId().trim();
					if(id!=null&&!id.equals("")){
						for(int i=0;i<groupPersonList.size();i++){
							String ids = groupPersonList.get(i).getUserId();
							if(id.equals(ids)){
								groupPersonList.get(i).setOnLine(2);
								groupPersonLists.add(groupPersonList.get(i));
							}
						}
					}
				}
			}else{
				String id = CommonUtils.getUserId(context);
				if(id!=null&&!id.equals("")){
					for(int i=0;i<groupPersonList.size();i++){
						String ids = groupPersonList.get(i).getUserId();
						if(id.equals(ids)){
							groupPersonList.get(i).setOnLine(2);
							groupPersonLists.add(groupPersonList.get(i));
						}
					}
				}
			}
			for(int h=0;h<groupPersonList.size();h++){
				if(groupPersonList.get(h).getOnLine()!=2){
					groupPersonLists.add(groupPersonList.get(h));
				}
			}
			GroupPersonAdapter adapter = new GroupPersonAdapter(context, groupPersonLists);
			gridView_person.setAdapter(adapter);
			gridView_person.setOnItemClickListener(new OnItemClickListener() {
				@Override
				public void onItemClick(AdapterView<?> parent,View view, int position, long id) {
					boolean isFriend = false;
					if(GlobalConfig.list_person!=null&&GlobalConfig.list_person.size()!=0){
						for(int i=0;i<GlobalConfig.list_person.size();i++){
							if(groupPersonLists.get(position).getUserId().equals(GlobalConfig.list_person.get(i).getUserId())){
								isFriend=true;
								break;
							}
						}
					}else{
						//不是我的好友
						isFriend=false;
					}
					if(isFriend){
						Intent intent = new Intent(context,TalkPersonNewsActivity.class);
						Bundle bundle = new Bundle();
						bundle.putString("type", "talkoldlistfragment_p");
						bundle.putSerializable("data", groupPersonLists.get(position));
						intent.putExtras(bundle);
						startActivity(intent);
					}else{
						Intent intent = new Intent(context,FriendAddActivity.class);
						Bundle bundle = new Bundle();
						bundle.putString("type", "talkoldlistfragment_p");
						bundle.putString("id", interPhoneId);
						bundle.putSerializable("data", groupPersonLists.get(position));
						intent.putExtras(bundle);
						startActivityForResult(intent, 1);
					}
				}
			});

			if(gridView_person.getVisibility()==View.VISIBLE){
				gridView_person.setVisibility(View.GONE);
				gridView_tv.setVisibility(View.GONE);
			}else{
				gridView_person.setVisibility(View.VISIBLE);
				gridView_tv.setVisibility(View.VISIBLE);
			}
		}
	}

	public void setImageView(int i,String userName,String url) {
		//设置有人说话时候界面友好交互
		//发送消息线程
		if(i==1){
			talkingName.setVisibility(View.VISIBLE);
			if(userName.equals(UserName)){
				talkingName.setText("我");
			}else{
				talkingName.setText(userName);
			}
			talking_news.setText("正在通话");
			image_voice.setVisibility(View.VISIBLE);
			if(url==null||url.equals("")||url.equals("null")||url.trim().equals("")){
				Bitmap bmp = BitmapUtils.readBitMap(context, R.mipmap.wt_image_tx_hy);
				image_group_persontx.setImageBitmap(bmp);
			}else{
				String urls = GlobalConfig.imageurl+url;
				Picasso.with(context).load(urls.replace( "\\/", "/")).resize(100, 100).centerCrop().into(image_group_persontx);
			}
			if (draw_group.isRunning()) {
			} else {
				draw_group.start();
			}
		}else{
			talkingName.setVisibility(View.INVISIBLE);
			talking_news.setText("无人通话");
			if (draw_group.isRunning()) {
				draw_group.stop();
			}
			image_group_persontx.setImageResource(R.mipmap.wt_image_tx_hy);
			image_voice.setVisibility(View.INVISIBLE);
		}
	}

	/**
	 * 设置对讲组为激活状态
	 * @param
	 */
	public static void zhiDingGroupss(String groupIds ) {
		Intent intent = new Intent();
		intent.setAction(BroadcastConstant.UP_DATA_GROUP);
		context.sendBroadcast(intent);
		enterGroupType=1;
		groupId=groupIds;
		tv_num.setText("1");
		listInFo=null;
		InterPhoneControlManager.Enter(context, groupIds);//发送进入组的数据，socket
		getGridViewPerson(groupIds);//获取群成员
	}

	/**
	 * 设置对讲组为激活状态
	 */
	public static void zhiDingGroup(TalkGroupInside talkGroupInside) {
		Intent intent = new Intent();
		intent.setAction(BroadcastConstant.UP_DATA_GROUP);
		context.sendBroadcast(intent);
		enterGroupType=1;
		groupId=talkGroupInside.getGroupId();
		tv_num.setText("1");
		listInFo=null;
		InterPhoneControlManager.Enter(context, talkGroupInside.getGroupId());//发送进入组的数据，socket
		getGridViewPerson(talkGroupInside.getGroupId());//获取群成员
	}

	/**
	 * 设置对讲组2为激活状态
	 */
	public static void zhiDingGroups(TalkGroupInside talkGroupInside) {
		Intent intent = new Intent();
		intent.setAction(BroadcastConstant.UP_DATA_GROUP);
		context.sendBroadcast(intent);
		enterGroupType=2;
		groupId=talkGroupInside.getGroupId();
		tv_num.setText("1");
		listInFo=null;
		InterPhoneControlManager.Enter(context, talkGroupInside.getGroupId());//发送进入组的数据，socket
		getGridViewPerson(talkGroupInside.getGroupId());//获取群成员
	}

	/**
	 * 设置个人为激活状态/设置第一条为激活状态
	 */
	public static  void zhiDingPerson(DBTalkHistorary talkdb) {
		historydatabaselist = dbDao.queryHistory();//得到数据库里边数据
		getList();
		setDatePerson();
	}

	private static  void setListener() {
		adapter.setOnListener(new ChatListAdapter.OnListener() {
			@Override
			public void zhiding(int position) {
				groupId = allList.get(position).getId();
				if(isCalling){
					//此时有对讲状态
					if(interPhoneType.equals("user")){
						//对讲状态为个人时，弹出框展示
						String interPhoneTypes = allList.get(position).getTyPe();
						if(interPhoneTypes!=null&&!interPhoneTypes.equals("")&&interPhoneTypes.equals("user")){
							dialogType=1;
							phoneId=allList.get(position).getId();
						}else{
							dialogType=2;
						}
						confirmDialog.show();
					}else{
						InterPhoneControlManager.Quit(context, interPhoneId);//退出小组
						String interPhoneTypes = allList.get(position).getTyPe();
						if(interPhoneTypes!=null&&!interPhoneTypes.equals("")&&interPhoneTypes.equals("user")){
							call(allList.get(position).getId());
						}else{
							zhiDingGroupss(groupId);
						}
					}
				}else{
					String interPhoneTypes = allList.get(position).getTyPe();
					if(interPhoneTypes!=null&&!interPhoneTypes.equals("")&&interPhoneTypes.equals("user")){
						call(allList.get(position).getId());
					}else{
						zhiDingGroupss(groupId);
					}
				}
			}
		});

		mListView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				String type = allList.get(position).getTyPe();
				if(type!=null&&type.equals("group")){
					//跳转到群组详情页面
					Intent intent=new Intent(context,GroupDetailActivity.class);
					Bundle bundle=new Bundle();
					bundle.putString("type", "talkoldlistfragment");
					bundle.putString("activationid",interPhoneId);
					bundle.putSerializable("data",allList.get(position));
					intent.putExtras(bundle);
					context.startActivity(intent);
				}else{
					// 跳转到详细信息界面
					Intent intent = new Intent(context,GroupDetailActivity.class);
					Bundle bundle = new Bundle();
					bundle.putString("type", "talkoldlistfragment");
					bundle.putSerializable("data", allList.get(position));
					intent.putExtras(bundle);
					context.startActivity(intent);
				}
			}
		});
	}

	protected static void call(String id) {
		Intent it = new Intent(context,CallAlertActivity.class);
		Bundle bundle = new Bundle();
		bundle.putString("id", id);
		it.putExtras(bundle);
		context.startActivity(it);
	}

	public void gettxl() {
		//第一次获取群成员跟组
		if (GlobalConfig.CURRENT_NETWORK_STATE_TYPE != -1) {
			dialog = DialogUtils.Dialogph(context, "正在获取数据");
			JSONObject jsonObject = VolleyRequest.getJsonObject(context);
			try {
				jsonObject.put("UserId", CommonUtils.getUserId(context));
			} catch (JSONException e) {
				e.printStackTrace();
			}

			VolleyRequest.RequestPost(GlobalConfig.gettalkpersonsurl, tag, jsonObject, new VolleyCallback() {

				@Override
				protected void requestSuccess(JSONObject result) {
					if (dialog != null) {
						dialog.dismiss();
					}
					if(isCancelRequest){
						return ;
					}
					LinkMan list = new LinkMan();
					list = new Gson().fromJson(result.toString(), new TypeToken<LinkMan>(){}.getType());
					try {
						try {
							GlobalConfig.list_group = list.getGroupList().getGroups();
						} catch (Exception e1) {
							e1.printStackTrace();
						}
						try {
							GlobalConfig.list_person = list.getFriendList().getFriends();
						} catch (Exception e) {
							e.printStackTrace();
						}
					} catch (Exception e) {
						e.printStackTrace();
					}
					//获取到群成员后
					update(context);//第一次进入该界面
				}
				@Override
				protected void requestError(VolleyError error) {
					if (dialog != null) {
						dialog.dismiss();
					}
					//获取到群成员后
					update(context);//第一次进入该界面
				}
			});
		} else {
			ToastUtils.show_always(context, "网络失败，请检查网络");
		}
	}

	/*
	 * 第一次进入该界面
	 */
	private void update(Context context){
		//得到数据库里边数据
		historydatabaselist = dbDao.queryHistory();
		//得到真实的数据
		getList();
		if(allList==null||allList.size()==0){
			//此时数据库里边没有数据，界面不变
			isCalling=false;
		}else{
			// 此处数据需要处理，第一条数据为激活状态组
			//第一条数据的状态
			//			String type = alllist.get(0).getTyPe();//对讲类型，个人跟群组
			//			String id = alllist.get(0).getId();//对讲组：groupid
			//			if(type!=null&&!type.equals("")&&type.equals("user")){
			//若上次退出前的通话状态是单对单通话则不处理
			isCalling=false;
			if(adapter==null){
				adapter=new ChatListAdapter(context, allList,allList.get(allList.size()-1).getId());
				mListView.setAdapter(adapter);
			}else{
				adapter.ChangeDate(allList, allList.get(allList.size()-1).getId());
			}
			setListener();
		}
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		switch (requestCode) {
			case 1:
				if(resultCode==1){
					getGridViewPerson(interPhoneId);//获取群成员
				}
				break;
		}
	}

	public void addGroup(String id) {
		//获取最新激活状态的数据
		String groupid = id;
		String type = "group";
		String addtime = Long.toString(System.currentTimeMillis());
		String bjuserid =CommonUtils.getUserId(context);
		//如果该数据已经存在数据库则删除原有数据，然后添加最新数据
		DBTalkHistorary history = new DBTalkHistorary( bjuserid,  type,  groupid, addtime);
		dbDao.addTalkHistory(history);
		historydatabaselist = dbDao.queryHistory();//得到数据库里边数据
		getList();
	}

	public void setDateGroup() {
		//设置组为激活状态
		lin_notalk.setVisibility(View.GONE);
		lin_personhead.setVisibility(View.GONE);
		lin_head.setVisibility(View.VISIBLE);
		lin_second.setVisibility(View.GONE);
		TalkListGP firstdate = allList.remove(0);
		interPhoneType= firstdate.getTyPe();//对讲类型，个人跟群组
		interPhoneId=firstdate.getId();//对讲组：groupid
		tv_groupname.setText(firstdate.getName());
		if(firstdate.getGroupType()==null||firstdate.getGroupType().equals("")||firstdate.getGroupType().equals("1")){
			tv_grouptype.setText("公开群");
		}else if(firstdate.getGroupType().equals("0")){
			tv_grouptype.setText("审核群");
		}else if(firstdate.getGroupType().equals("2")){
			tv_grouptype.setText("密码群");
		}
		if(firstdate.getPortrait()==null||firstdate.getPortrait().equals("")||firstdate.getPortrait().trim().equals("")){
			image_grouptx.setImageResource(R.mipmap.wt_image_tx_qz);
		}else{
			String url = GlobalConfig.imageurl+firstdate.getPortrait();
			Picasso.with(context).load(url.replace( "\\/", "/")).resize(100, 100).centerCrop().into(image_grouptx);
		}
		if(allList.size()==0){
			if(adapter==null){
				adapter=new ChatListAdapter(context, allList,"0");
				mListView.setAdapter(adapter);
			}else{
				adapter.ChangeDate(allList, "0");
			}
		}else{
			if(adapter==null){
				adapter=new ChatListAdapter(context, allList,allList.get(allList.size()-1).getId());
				mListView.setAdapter(adapter);
			}else{
				adapter.ChangeDate(allList, allList.get(allList.size()-1).getId());
			}
		}
		setListener();
	}

	public static void setDatePerson() {
		//设置个人为激活状态
		isCalling=true;
		TalkListGP firstDate = allList.remove(0);
		interPhoneType= firstDate.getTyPe();//
		interPhoneId=firstDate.getId();//
		lin_notalk.setVisibility(View.GONE);
		lin_personhead.setVisibility(View.VISIBLE);
		lin_head.setVisibility(View.GONE);
		lin_second.setVisibility(View.GONE);
		tv_personname.setText(firstDate.getName());
		if(firstDate.getPortrait()==null||firstDate.getPortrait().equals("")||firstDate.getPortrait().trim().equals("")){
			image_persontx.setImageResource(R.mipmap.wt_image_tx_qz);
		}else{
			String url = GlobalConfig.imageurl+firstDate.getPortrait();
			Picasso.with(context).load(url.replace( "\\/", "/")).resize(100, 100).centerCrop().into(image_persontx);
		}
		if(allList.size()==0){
			if(adapter==null){
				adapter=new ChatListAdapter(context, allList,"0");
				mListView.setAdapter(adapter);
			}else{
				adapter.ChangeDate(allList, "0");
			}
		}else{
			if(adapter==null){
				adapter=new ChatListAdapter(context, allList,allList.get(allList.size()-1).getId());
				mListView.setAdapter(adapter);
			}else{
				adapter.ChangeDate(allList, allList.get(allList.size()-1).getId());
			}
		}
		setListener();
	}

	private static   void getList() {
		allList.clear();
		try {
			if(historydatabaselist!=null&&historydatabaselist.size()>0){
				for(int i=0;i<historydatabaselist.size();i++){
					if(historydatabaselist.get(i).getTyPe().equals("user")){
						if(GlobalConfig.list_person!=null&&GlobalConfig.list_person.size()!=0){
							for(int j=0;j<GlobalConfig.list_person.size();j++){
								String id = historydatabaselist.get(i).getID();
								if(id!=null&&!id.equals("")&&id.equals(GlobalConfig.list_person.get(j).getUserId())){
									TalkListGP ListGP = new TalkListGP();
									ListGP.setTruename(GlobalConfig.list_person.get(j).getTruename());
									ListGP.setId(GlobalConfig.list_person.get(j).getUserId());
									ListGP.setName(GlobalConfig.list_person.get(j).getUserName());
									ListGP.setUserAliasName(GlobalConfig.list_person.get(j).getUserAliasName());
									ListGP.setPortrait(GlobalConfig.list_person.get(j).getPortraitBig());
									ListGP.setAddTime(historydatabaselist.get(i).getAddTime());
									ListGP.setTyPe(historydatabaselist.get(i).getTyPe());
									ListGP.setDescn(GlobalConfig.list_person.get(j).getDescn());
									ListGP.setUserNum(GlobalConfig.list_person.get(j).getUserNum());
									allList.add(ListGP);
									break;
								}
							}
						}
					}else{
						if(GlobalConfig.list_group!=null&&GlobalConfig.list_group.size()!=0){
							for(int j=0;j<GlobalConfig.list_group.size();j++){
								String id = historydatabaselist.get(i).getID();
								if(id!=null&&!id.equals("")&&id.equals(GlobalConfig.list_group.get(j).getGroupId())){
									TalkListGP ListGP = new TalkListGP();
									ListGP.setCreateTime(GlobalConfig.list_group.get(j).getCreateTime());
									ListGP.setGroupCount(GlobalConfig.list_group.get(j).getGroupCount());
									ListGP.setGroupCreator(GlobalConfig.list_group.get(j).getGroupCreator());
									ListGP.setGroupDesc(GlobalConfig.list_group.get(j).getGroupDesc());
									ListGP.setId(GlobalConfig.list_group.get(j).getGroupId());
									ListGP.setPortrait(GlobalConfig.list_group.get(j).getGroupImg());
									ListGP.setGroupManager(GlobalConfig.list_group.get(j).getGroupManager());
									ListGP.setGroupMyAlias(GlobalConfig.list_group.get(j).getGroupMyAlias());
									ListGP.setName(GlobalConfig.list_group.get(j).getGroupName());
									ListGP.setGroupNum(GlobalConfig.list_group.get(j).getGroupNum());
									ListGP.setGroupSignature(GlobalConfig.list_group.get(j).getGroupSignature());
									ListGP.setGroupType(GlobalConfig.list_group.get(j).getGroupType());
									ListGP.setAddTime(historydatabaselist.get(i).getAddTime());
									ListGP.setTyPe(historydatabaselist.get(i).getTyPe());
									allList.add(ListGP);
									break;
								}
							}
						}
					}
				}
			}
		} catch (Exception e) {
			Log.e("getList异常", e.toString());
		}
	}

	private static void getGridViewPerson(String interPhoneId) {
		JSONObject jsonObject = VolleyRequest.getJsonObject(context);
		try {
			//模块属性
			jsonObject.put("GroupId", interPhoneId);
		} catch (JSONException e) {
			e.printStackTrace();
		}

		VolleyRequest.RequestPost(GlobalConfig.grouptalkUrl, jsonObject, new VolleyCallback() {

			@Override
			protected void requestSuccess(JSONObject result) {
				String UserList = null;
				try {
					UserList = result.getString("UserList");
				} catch (JSONException e) {
					e.printStackTrace();
				}
				if(groupPersonList!=null){
					groupPersonList .clear();
				}else{
					groupPersonList =new ArrayList<GroupTalkInside>();
				}
				groupPersonList=gson.fromJson(UserList, new TypeToken<List<GroupTalkInside>>(){}.getType());
				if(groupPersonList!=null&&groupPersonList.size()>0){
					tv_allnum.setText("/"+groupPersonList.size());
				}else{
					tv_allnum.setText("/0");
				}
			}
			@Override
			protected void requestError(VolleyError error) {
			}
		});
	}

	private void Dialog() {
		final View dialog = LayoutInflater.from(context).inflate(R.layout.dialog_talk_person_del, null);
		TextView tv_cancel = (TextView) dialog.findViewById(R.id.tv_cancle);
		TextView tv_confirm = (TextView) dialog.findViewById(R.id.tv_confirm);
		confirmDialog = new Dialog(context, R.style.MyDialog);
		confirmDialog.setContentView(dialog);
		confirmDialog.setCanceledOnTouchOutside(true);
		confirmDialog.getWindow().setBackgroundDrawableResource(R.color.dialog);
		tv_cancel.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				confirmDialog.dismiss();
			}
		});
		tv_confirm.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				InterPhoneControlManager.PersonTalkHangUp(context, InterPhoneControlManager.bdcallid);
				if(dialogType==1){
					InterPhoneControlManager.PersonTalkHangUp(context, InterPhoneControlManager.bdcallid);
					isCalling=false;
					lin_notalk.setVisibility(View.VISIBLE);
					lin_personhead.setVisibility(View.GONE);
					lin_head.setVisibility(View.GONE);
					call(phoneId);
					confirmDialog.dismiss();
				}else{
					InterPhoneControlManager.PersonTalkHangUp(context, InterPhoneControlManager.bdcallid);
					isCalling=false;
					lin_notalk.setVisibility(View.VISIBLE);
					lin_personhead.setVisibility(View.GONE);
					lin_head.setVisibility(View.GONE);
					zhiDingGroupss(groupId);
					//对讲主页界面更新
					DuiJiangActivity.update();
					confirmDialog.dismiss();
				}
			}
		});
	}

	// 接收socket的数据进行处理
	class MessageReceiver extends BroadcastReceiver{
		@Override
		public void onReceive(Context context, Intent intent) {
			String action=intent.getAction();
			if(action.equals(BroadcastConstant.SOCKET_PUSH)){
				//				MsgNormal message = (MsgNormal) intent.getSerializableExtra("outmessage");
				byte[] bt = intent.getByteArrayExtra("outmessage");
				//				Log.e("接收器中数据", Arrays.toString(bt)+"");
				try {
					MsgNormal message = (MsgNormal) MessageUtils.buildMsgByBytes(bt);

					if(message!=null){
						int biztype = message.getBizType();
						if(biztype==1){
							int cmdType = message.getCmdType();
							if(cmdType==2){
								int command = message.getCommand();
								if(command==9){
									int returnType = message.getReturnType();
									switch (returnType) {
										case 0xff://TTT
											//请求通话出异常了
											VibratorUtils.Vibrate(ChatFragment.context, Vibrate);
											VoiceStreamRecordService.stop();
											ToastUtils.show_always(context, "请求通话—出异常了");
											break;
										case 0x00:
											//没有有效登录用户
											VibratorUtils.Vibrate(ChatFragment.context, Vibrate);
											VoiceStreamRecordService.stop();
											ToastUtils.show_always(context, "没有有效登录用户");
											break;
										case 0x02:
											//无法获取用户组
											VibratorUtils.Vibrate(ChatFragment.context, Vibrate);
											VoiceStreamRecordService.stop();
											ToastUtils.show_always(context, "无法获取用户组");
											break;
										case 0x01:
											//用户可以通话了
											isTalking=true;
											ToastUtils.show_short(context, "可以说话");
											// image_button.setBackground(context.getResources().getDrawable(R.mipmap.wt_duijiang_button_pressed));
											VoiceStreamRecordService.send();
											break;
										case 0x04:
											//用户不在所指定的组
											VibratorUtils.Vibrate(ChatFragment.context, Vibrate);
											VoiceStreamRecordService.stop();
											ToastUtils.show_always(context, "用户不在所指定的组");
											break;
										case 0x05:
											//进入组的人员不足两人
											VibratorUtils.Vibrate(ChatFragment.context, Vibrate);
											VoiceStreamRecordService.stop();
											ToastUtils.show_always(context, "进入组的人员不足两人");
											break;
										case 0x08:
											//有人在说话，无权通话
											VibratorUtils.Vibrate(ChatFragment.context, Vibrate);
											VoiceStreamRecordService.stop();
											ToastUtils.show_always(context, "有人在说话");
											break;
										case 0x90:
											//用户在电话通话
											VibratorUtils.Vibrate(ChatFragment.context, Vibrate);
											VoiceStreamRecordService.stop();
											ToastUtils.show_always(context, "用户在电话通话");
											break;
										default:
											break;
									}
								}else if(command==0x0a){
									int returnType = message.getReturnType();
									switch (returnType) {
										case 0xff://TTT
											//结束对讲出异常
											isTalking=false;
											ToastUtils.show_short(context, "结束对讲—出异常");
											break;
										case 0x00:
											//没有有效登录用户
											isTalking=false;
											ToastUtils.show_always(context, "数据出错，请注销后重新登录账户");
											break;
										case 0x02:
											//无法获取用户组
											isTalking=false;
											ToastUtils.show_always(context, "无法获取用户组");
											break;
										case 0x01:
											//成功结束对讲
											isTalking=false;
											ToastUtils.show_short(context, "结束对讲—成功");
											break;
										case 0x04:
											//	用户不在组
											isTalking=false;
											ToastUtils.show_short(context, "结束对讲");
											break;
										case 0x05:
											//	对讲人不是你，无需退出
											isTalking=false;
											ToastUtils.show_short(context, "对讲人不是你，无需退出");
											break;

										default:
											break;
									}
								}else if(command==0x10){
									//组内有人说话
									ToastUtils.show_short(context, "组内人有人说话，有人按下说话钮");
									MapContent data = (MapContent) message.getMsgContent();
									//说话人
									String talkUserId  =  data.get("TalkUserId")+"";
									if(groupPersonList!=null&&groupPersonList.size()!=0){
										for(int i=0;i<groupPersonList.size();i++){
											if(groupPersonList.get(i).getUserId().equals(talkUserId)){
												setImageView(1,groupPersonList.get(i).getUserName(),groupPersonList.get(i).getPortraitMini());
											}
										}
									}
								}else if(command==0x20){
									//组内人说话完毕，有人松手
									setImageView(2,"","");
									ToastUtils.show_short(context, "组内人说话完毕，有人松手");
								}
							}else if(cmdType==1){
								int command = message.getCommand();
								if(command==9){
									int returnType = message.getReturnType();
									switch (returnType) {
										case 0xff://TT
											//进入组出异常
											isCalling=false;
											ToastUtils.show_short(context, "进入组—出异常");
											break;
										case 0x00:
											//没有有效登录用户
											isCalling=false;
											ToastUtils.show_always(context, "数据出错，请注销后重新登录账户");
											break;
										case 0x01:
											//进入组成功
											isCalling=true;
											ToastUtils.show_short(context, "进入组—成功");
											if(enterGroupType==2){
												InterPhoneControlManager.Quit(context, interPhoneId);//退出小组
												String id = groupId;//对讲组：groupid
												dbDao.deleteHistory(id);
												addGroup(id);//加入到数据库
												setDateGroup();
											}else{
												String id = groupId;//对讲组：groupid
												dbDao.deleteHistory(id);
												addGroup(id);//加入到数据库
												setDateGroup();
											}
											break;
										case 0x02:
											//无法获取用户组
											isCalling=false;
											ToastUtils.show_short(context, "无法获取用户组");
											break;
										case 0x04:
											//用户不在该组
											isCalling=false;
											ToastUtils.show_short(context, "进入组—用户不在该组");
											break;
										case 0x08:
											//用户已在组
											isCalling=true;
											if(enterGroupType==2){
												InterPhoneControlManager.Quit(context, interPhoneId);//退出小组
												String id = groupId;//对讲组：groupid
												dbDao.deleteHistory(id);
												addGroup(id);//加入到数据库
												setDateGroup();
											}else{
												String id = groupId;//对讲组：groupid
												dbDao.deleteHistory(id);
												addGroup(id);//加入到数据库
												setDateGroup();
											}
											ToastUtils.show_short(context, "进入组—用户已在组");
											break;
										default:
											break;
									}
								}else if(command==0x0a){
									int returnType = message.getReturnType();
									switch (returnType) {
										case 0xff://TT
											//退出租出异常
											ToastUtils.show_short(context, "退出租—出异常");
											isCalling=false;
											break;
										case 0x00:
											//没有有效登录用户
											isCalling=false;
											ToastUtils.show_always(context, "数据出错，请注销后重新登录账户");
											break;
										case 0x01:
											//退出租成功
											ToastUtils.show_short(context, "退出组—成功");
											isCalling=false;
											break;
										case 0x02:
											//退出租成功
											isCalling=false;
											ToastUtils.show_short(context, "无法获取用户组");
											break;
										case 0x04:
											//用户不在该组
											ToastUtils.show_short(context, "退出租—用户不在该组");
											isCalling=false;
											break;
										case 0x08:
											//用户已退出组
											ToastUtils.show_short(context, "退出租—用户已退出组");
											isCalling=false;
											break;
										default:
											break;
									}

								}else if(command==0x10){
									//服务端发来的组内成员的变化
									ToastUtils.show_always(context, "服务端发来的组内成员的变化");
									try {
										MapContent data = (MapContent) message.getMsgContent();
										Map<String, Object> map = data.getContentMap();
										String news = new Gson ().toJson(map);

										JSONTokener jsonParser = new JSONTokener(news);
										JSONObject arg1 = (JSONObject) jsonParser.nextValue();
										String inGroupUsers = arg1.getString("InGroupUsers");

										listInFo = new Gson().fromJson(inGroupUsers, new TypeToken<List<ListInfo>>() {}.getType());
										//组内所有在线成员
										//组内有人说话时，根据这个list数据，得到该成员信息啊：头像，昵称等
										Log.i("组内成员人数", listInFo.size()+"");
										tv_num.setText(listInFo.size()+"");
									} catch (Exception e) {
										e.printStackTrace();
									}
								}else if(command==0x20){
									try{
										MapContent data = (MapContent) message.getMsgContent();
										Map<String, Object> map = data.getContentMap();
										String news = new Gson ().toJson(map);

										JSONTokener jsonParser = new JSONTokener(news);
										JSONObject arg1 = (JSONObject) jsonParser.nextValue();
										String userinfos = arg1.getString("UserInfo");

										ListInfo userinfo  = new Gson().fromJson(userinfos, new TypeToken<ListInfo>() {}.getType());
										String groupids = data.get("GroupId")+"";
										listInFo.add(userinfo);
										Log.i("组内成员人数", listInFo.size()+"");
										tv_num.setText(listInFo.size()+"");
										getGridViewPerson(groupids);
										//有人加入组
										ToastUtils.show_short(context, "有人加入组");
									} catch (Exception e) {
										e.printStackTrace();
									}
								}else if(command==0x30){
									//有人退出组
									try{
										MapContent data = (MapContent) message.getMsgContent();
										Map<String, Object> map = data.getContentMap();
										String news = new Gson ().toJson(map);

										JSONTokener jsonParser = new JSONTokener(news);
										JSONObject arg1 = (JSONObject) jsonParser.nextValue();
										String userinfos = arg1.getString("UserInfo");

										ListInfo userinfo  = new Gson().fromJson(userinfos, new TypeToken<ListInfo>() {}.getType());

										String userinfoid = userinfo.getUserId();
										String groupids = data.get("GroupId")+"";
										for(int i=0;i<listInFo.size();i++){
											if(listInFo.get(i).getUserId().equals(userinfoid)){
												listInFo.remove(i);
											}
										}
										Log.i("组内成员人数", listInFo.size()+"");
										tv_num.setText(listInFo.size()+"");
										getGridViewPerson(groupids);
										ToastUtils.show_short(context, "有人退出组");
									} catch (Exception e) {
										e.printStackTrace();
									}
								}

							}
						}else  if(biztype==2){
							int cmdType = message.getCmdType();
							if(cmdType==2){
								int command = message.getCommand();
								if(command==9){
									int returnType = message.getReturnType();
									switch (returnType) {
										case 0xff://TT
											//请求通话出异常了
											VibratorUtils.Vibrate(ChatFragment.context, Vibrate);
											VoiceStreamRecordService.stop();
											ToastUtils.show_always(context, "请求通话—出异常了");
											break;
										case 0x02:
											//无权通话
											VibratorUtils.Vibrate(ChatFragment.context, Vibrate);
											VoiceStreamRecordService.stop();
											ToastUtils.show_always(context, "无法获取用户组");
											break;
										case 0x01:
											//用户可以通话了
											isTalking=true;
											ToastUtils.show_short(context, "可以说话");
											image_personvoice.setVisibility(View.VISIBLE);
											if (draw.isRunning()) {
											} else {
												draw.start();
											}
											// image_button.setBackground(context.getResources().getDrawable(R.drawable.wt_duijiang_button_pressed));
											VoiceStreamRecordService.send();
											break;
										case 0x04:
											//用户无权通话
											VibratorUtils.Vibrate(ChatFragment.context, Vibrate);
											VoiceStreamRecordService.stop();
											ToastUtils.show_always(context, "不能对讲，有人在说话");
											break;
										case 0x05:
											//无权通话
											VibratorUtils.Vibrate(ChatFragment.context, Vibrate);
											VoiceStreamRecordService.stop();
											ToastUtils.show_always(context, "不能对讲，状态错误");
											break;
										default:
											break;
									}
								}else if(command==0x0a){
									int returnType = message.getReturnType();
									switch (returnType) {
										case 0xff://TT
											//结束对讲出异常
											isTalking=false;
											if (draw.isRunning()) {
												draw.stop();
											}
											image_personvoice.setVisibility(View.INVISIBLE);
											ToastUtils.show_short(context, "结束对讲—出异常");
											break;
										case 0x02:
											//	无法获取用户
											isTalking=false;
											if (draw.isRunning()) {
												draw.stop();
											}
											image_personvoice.setVisibility(View.INVISIBLE);
											ToastUtils.show_short(context, "无法获取用户");
											break;
										case 0x01:
											//成功结束对讲
											isTalking=false;
											if (draw.isRunning()) {
												draw.stop();
											}
											image_personvoice.setVisibility(View.INVISIBLE);
											ToastUtils.show_short(context, "结束对讲—成功");
											break;
										case 0x04:
											//	清除者和当前通话者不同，无法处理
											isTalking=false;
											if (draw.isRunning()) {
												draw.stop();
											}
											image_personvoice.setVisibility(View.INVISIBLE);
											ToastUtils.show_short(context, "清除者和当前通话者不同，无法处理");
											break;
										case 0x05:
											//	状态错误
											isTalking=false;
											if (draw.isRunning()) {
												draw.stop();
											}
											image_personvoice.setVisibility(View.INVISIBLE);
											ToastUtils.show_short(context, "状态错误");
											break;
										default:
											break;
									}
								}
							}
						}
					}
				} catch (Exception e1) {
					e1.printStackTrace();
				}
			}else if(action.equals(BroadcastConstant.UP_DATA_GROUP)){
				if(gridView_person != null){
					gridView_person.setVisibility(View.GONE);
				}
			}else if(action.equals(BroadcastConstant.PUSH_BACK)){
				//MsgNormal message = (MsgNormal) intent.getSerializableExtra("outmessage");
				//Log.i("talkOldListFragment弹出框服务push_back", "接收到的socket服务的信息"+message+"");
				byte[] bt = intent.getByteArrayExtra("outmessage");
				try {
					MsgNormal message = (MsgNormal) MessageUtils.buildMsgByBytes(bt);

					if(message != null){
						int cmdType = message.getCmdType();
						if(cmdType==1){
							int command = message.getCommand();
							if(command==0x30){
								//挂断电话的数据处理
								isCalling = false;
								historydatabaselist = dbDao.queryHistory();//得到数据库里边数据
								getList();
								if(allList.size() == 0){
									if(adapter == null){
										adapter=new ChatListAdapter(context, allList,"0");
										mListView.setAdapter(adapter);
									}else{
										adapter.ChangeDate(allList, "0");
									}
								}else{
									if(adapter==null){
										adapter=new ChatListAdapter(context, allList,allList.get(allList.size()-1).getId());
										mListView.setAdapter(adapter);
									}else{
										adapter.ChangeDate(allList, allList.get(allList.size()-1).getId());
									}
								}
								setListener();
								lin_notalk.setVisibility(View.VISIBLE);
								lin_personhead.setVisibility(View.GONE);
								lin_head.setVisibility(View.GONE);
							}
						}
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}else if(action.equals(BroadcastConstant.PUSH_VOICE_IMAGE_REFRESH)){
				int seqNum = intent.getIntExtra("seqNum", -1);
				if(interPhoneType.equals("group")){
				}else{
					//此处处理个人对讲的逻辑
					if(seqNum<0){
						if(image_personvoice.getVisibility()==View.VISIBLE){
							image_personvoice.setVisibility(View.INVISIBLE);
							if (draw.isRunning()) {
								draw.stop();
							}
						}
					}else{
						if(image_personvoice.getVisibility()==View.INVISIBLE){
							image_personvoice.setVisibility(View.VISIBLE);
							if (draw.isRunning()) {
							} else {
								draw.start();
							}
						}
					}
				}
			}
		}
	}

	//抬手后的操作
	public static void jack() {
		//此处处理群组对讲的逻辑
		if(isTalking){
			if(interPhoneType.equals("group")){
				new Handler().postDelayed(new Runnable() {
					@Override
					public void run() {
						VoiceStreamRecordService.stop();
						InterPhoneControlManager.Loosen(context, interPhoneId);//发送取消说话控制
						if (draw.isRunning()) {
							draw.stop();
						}
						Log.e("对讲页面====", "录音机停止+发送取消说话控制+延时0.30秒");
					}
				}, 300);
			}else{//此处处理个人对讲的逻辑
				VoiceStreamRecordService.stop();
				InterPhoneControlManager.PersonTalkPressStop(context, interPhoneId);//发送取消说话控制
				if (draw.isRunning()) {
					draw.stop();
				}
			}
		}else{
			VoiceStreamRecordService.stop();
		}
	}

	// 按下的动作
	public static void press() {
		// 此处处理群组对讲的逻辑
		if(interPhoneType.equals("group")){
			InterPhoneControlManager.Press(context, interPhoneId);
			VoiceStreamRecordService.stop();
			VoiceStreamRecordService.start(context, interPhoneId,"group");
		}else{
			//此处处理个人对讲的逻辑
			InterPhoneControlManager.PersonTalkPressStart(context, interPhoneId);
			VoiceStreamRecordService.stop();
			VoiceStreamRecordService.start(context, interPhoneId,"person");
		}
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		isCancelRequest = VolleyRequest.cancelRequest(tag);
		if(Receiver!=null){
			context.unregisterReceiver(Receiver);
			Receiver=null;
		}
	}
}
