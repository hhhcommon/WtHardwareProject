package com.wotingfm.ui.interphone.message.messagecenter.fragment;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.TextView;

import com.wotingfm.R;
import com.wotingfm.common.constant.BroadcastConstants;
import com.wotingfm.ui.interphone.linkman.model.DBNotifyHistory;
import com.wotingfm.ui.interphone.main.DuiJiangActivity;
import com.wotingfm.ui.interphone.message.messagecenter.dao.MessageNotifyDao;
import com.wotingfm.ui.interphone.message.messagecenter.dao.MessageSubscriberDao;
import com.wotingfm.ui.interphone.message.messagecenter.dao.MessageSystemDao;
import com.wotingfm.ui.interphone.message.messagecenter.model.DBSubscriberMessage;
import com.wotingfm.ui.music.main.PlayerActivity;

import java.util.List;


/**
 * 消息中心列表
 * 作者：xinlong on 2016/5/5 21:18
 * 邮箱：645700751@qq.com
 */
public class MessageFragment extends Fragment implements OnClickListener {
	private MessageReceiver Receiver;
	private MessageNotifyDao dbDaoNotify;
	private MessageSubscriberDao dbDaoSubscriber;
	private MessageSystemDao dbDaoSystem;
	private TextView tv_system, tv_subscribe, tv_group_messageN, tv_group_messageR;
	private View rootView;
	private FragmentActivity context;
	private String type;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		if (rootView == null) {
			rootView = inflater.inflate(R.layout.activity_message, container, false);
			rootView.setOnClickListener(this);
			context = getActivity();
			type=getArguments().getString("type");
			setView();                             // 设置界面
			initDao();                             // 初始化数据库命令执行对象
			setDateForSystem();
			setDateForNotify();
			setDateForSubscriber();
		}
		return rootView;
	}

	private void setView() {
		rootView.findViewById(R.id.head_left_btn).setOnClickListener(this);

		rootView.findViewById(R.id.lin_system).setOnClickListener(this);
		rootView.findViewById(R.id.lin_subscribe).setOnClickListener(this);
		rootView.findViewById(R.id.lin_group_messageN).setOnClickListener(this);
		rootView.findViewById(R.id.lin_group_messageR).setOnClickListener(this);

		tv_system = (TextView) rootView.findViewById(R.id.tv_system);
		tv_subscribe = (TextView) rootView.findViewById(R.id.tv_subscribe);
		tv_group_messageN = (TextView) rootView.findViewById(R.id.tv_group_messageN);
		tv_group_messageR = (TextView) rootView.findViewById(R.id.tv_group_messageR);

	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
			case R.id.head_left_btn:
				if(type!=null){
					if(type.equals("music")){
						PlayerActivity.close();
					}else{
						DuiJiangActivity.close();
					}
				}
				break;
			case R.id.lin_system:
				if(type!=null){
					MessageSystemFragment fragment1 = new MessageSystemFragment();
					Bundle bundle = new Bundle();
					bundle.putString("type", type);
					fragment1.setArguments(bundle);
					if(type.equals("music")){
						PlayerActivity.open(fragment1);
					}else{
						DuiJiangActivity.open(fragment1);
					}
				}
				break;
			case R.id.lin_subscribe:
				if(type!=null){
					MessageSubscriberFragment fragment2 = new MessageSubscriberFragment();
					Bundle bundle = new Bundle();
					bundle.putString("type", type);
					fragment2.setArguments(bundle);
					fragment2.setTargetFragment(this, 2);
					if(type.equals("music")){
						PlayerActivity.open(fragment2);
					}else{
						DuiJiangActivity.open(fragment2);
					}
				}
				break;
			case R.id.lin_group_messageN:
				if(type!=null){
					MessageNotifyFragment fragment3 = new MessageNotifyFragment();
					Bundle bundle = new Bundle();
					bundle.putString("type", type);
					fragment3.setArguments(bundle);
					fragment3.setTargetFragment(this, 3);
					if(type.equals("music")){
						PlayerActivity.open(fragment3);
					}else{
						DuiJiangActivity.open(fragment3);
					}
				}
				break;
			case R.id.lin_group_messageR:
				break;
		}
	}

	// 处理返回数据
	public void setResult(int resultCode, int type) {
		if (resultCode == 1) {
			if (type == 1) {
				setDateForSystem();
			}
		} else if (resultCode == 2) {
			if (type == 1) {
				setDateForSubscriber();
			}
		} else if (resultCode == 3) {
			if (type == 1) {
				setDateForNotify();
			}
		}
	}

	@Override
	public void onResume() {
		super.onResume();
		if (Receiver == null) {                   // 注册广播
			Receiver = new MessageReceiver();
			IntentFilter filter = new IntentFilter();
			filter.addAction(BroadcastConstants.PUSH_REFRESHNEWS);
			context.registerReceiver(Receiver, filter);
		}
	}

	// 广播接收  用于刷新界面
	class MessageReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			if (action.equals(BroadcastConstants.PUSH_REFRESHNEWS)) {
				setDateForSystem();
				setDateForNotify();
				setDateForSubscriber();
			}
		}
	}

	// 设置系统消息条数
	private void setDateForSystem() {
		List<DBNotifyHistory> sys_list = dbDaoSystem.querySystemNews();
		if (sys_list != null && sys_list.size() > 0) {
			tv_system.setVisibility(View.VISIBLE);
			int num = sys_list.size();
			if (num >= 100) {
				tv_system.setText("…");
			} else {
				tv_system.setText(String.valueOf(num));
			}
		} else {
			tv_system.setVisibility(View.INVISIBLE);
		}
	}

	// 设置通知消息条数
	private void setDateForNotify() {
		List<DBNotifyHistory> n_list = dbDaoNotify.queryNotifyMessageNoOther();
		if (n_list != null && n_list.size() > 0) {
			tv_group_messageN.setVisibility(View.VISIBLE);
			int num = n_list.size();
			if (num >= 100) {
				tv_group_messageN.setText("…");
			} else {
				tv_group_messageN.setText(String.valueOf(num));
			}
		} else {
			tv_group_messageN.setVisibility(View.INVISIBLE);
		}
	}

	// 设置订阅消息条数
	private void setDateForSubscriber() {
		List<DBSubscriberMessage> s_list = dbDaoSubscriber.querySubscriberMessage();
		if (s_list != null && s_list.size() > 0) {
			tv_subscribe.setVisibility(View.VISIBLE);
			int num = s_list.size();
			if (num >= 100) {
				tv_subscribe.setText("…");
			} else {
				tv_subscribe.setText(String.valueOf(num));
			}
		} else {
			tv_subscribe.setVisibility(View.INVISIBLE);
		}

	}

	// 初始化数据库命令执行对象
	private void initDao() {
		dbDaoNotify = new MessageNotifyDao(context); // 通知消息
		dbDaoSubscriber = new MessageSubscriberDao(context);// 订阅消息
		dbDaoSystem = new MessageSystemDao(context);// 系统消息
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		if (Receiver != null) {
			context.unregisterReceiver(Receiver);
			Receiver = null;
		}
	}
}
