package com.wotingfm.ui.interphone.notify.main;

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
import android.widget.ListView;

import com.wotingfm.R;
import com.wotingfm.common.constant.BroadcastConstants;
import com.wotingfm.ui.interphone.linkman.model.DBNotifyHistory;
import com.wotingfm.ui.interphone.main.DuiJiangActivity;
import com.wotingfm.ui.interphone.notify.adapter.NotifyNewsAdapter;
import com.wotingfm.ui.interphone.notify.dao.NotifyHistoryDao;
import com.wotingfm.ui.music.main.PlayerActivity;
import com.wotingfm.widget.TipView;

import java.util.List;


/**
 * 消息中心列表
 * 作者：xinlong on 2016/5/5 21:18
 * 邮箱：645700751@qq.com
 */
public class NotifyNewsFragment extends Fragment implements OnClickListener {
    private NotifyHistoryDao dbDao;
	private MessageReceiver Receiver;

    private List<DBNotifyHistory> list;
    private NotifyNewsAdapter adapter;

    private ListView mListView;
    private TipView tipView;// 没有数据提示
	private FragmentActivity context;
	private View rootView;
	private String type;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		if (rootView == null) {
			rootView = inflater.inflate(R.layout.activity_notifynews, container, false);
			rootView.setOnClickListener(this);
			context = getActivity();
			type=getArguments().getString("type");
			setView();                             // 设置界面
			initDao();                             // 初始化数据库命令执行对象
			getData();
		}
		return rootView;
	}

    @Override
    public void onResume() {
        super.onResume();
        if(Receiver == null) {		           // 注册广播
            Receiver = new MessageReceiver();
            IntentFilter filter = new IntentFilter();
            filter.addAction(BroadcastConstants.PUSH_REFRESHNEWS);
			context.registerReceiver(Receiver, filter);
        }
    }

    // 广播接收  用于刷新界面
	class MessageReceiver extends BroadcastReceiver{
		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			if(action.equals(BroadcastConstants.PUSH_REFRESHNEWS)){
				getData();
			}
		}
	}

	// 获取数据库的数据
	private void getData() {
		list = dbDao.queryHistory();
        if(list == null || list.size() <= 0) {
            tipView.setVisibility(View.VISIBLE);
            tipView.setTipView(TipView.TipStatus.NO_DATA, "您还没有收到任何的通知消息");
        } else {
            tipView.setVisibility(View.GONE);
            adapter = new NotifyNewsAdapter(context, list);
            mListView.setAdapter(adapter);
        }
	}

	// 初始化数据库命令执行对象
	private void initDao() {
		dbDao = new NotifyHistoryDao(context);
	}

	private void setView() {
		mListView = (ListView) rootView.findViewById(R.id.listview_history);
		rootView.findViewById(R.id.head_left_btn).setOnClickListener(this);

        tipView = (TipView)rootView. findViewById(R.id.tip_view);
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
		}
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		if(Receiver != null){
			context.unregisterReceiver(Receiver);
			Receiver = null;
		}
		dbDao = null;
		list = null;
		adapter = null;
		mListView = null;
	}
}
