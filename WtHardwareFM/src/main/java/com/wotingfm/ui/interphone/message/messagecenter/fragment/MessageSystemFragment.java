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
import android.widget.ListView;
import android.widget.TextView;

import com.wotingfm.R;
import com.wotingfm.common.constant.BroadcastConstants;
import com.wotingfm.common.constant.StringConstant;
import com.wotingfm.ui.interphone.linkman.model.DBNotifyHistory;
import com.wotingfm.ui.interphone.main.DuiJiangActivity;
import com.wotingfm.ui.interphone.message.messagecenter.adapter.NotifyNewsAdapter;
import com.wotingfm.ui.interphone.message.messagecenter.dao.MessageSystemDao;
import com.wotingfm.ui.music.main.PlayerActivity;
import com.wotingfm.util.ToastUtils;
import com.wotingfm.widget.TipView;

import java.util.List;

/**
 * 系统消息（暂时没有）
 * 作者：xinlong on 2016/5/5 21:18
 * 邮箱：645700751@qq.com
 */
public class MessageSystemFragment extends Fragment implements OnClickListener {
    private MessageReceiver Receiver;

    private List<DBNotifyHistory> list;
    private View rootView;
    private FragmentActivity context;
    private TipView tipView;
    private ListView mListView;
    private NotifyNewsAdapter adapter;
    private MessageSystemDao dbDao;
    private String type;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (rootView == null) {
            rootView = inflater.inflate(R.layout.activity_notifynews, container, false);
            context = getActivity();
            type = getArguments().getString(StringConstant.FROM_TYPE);
            setView();                             // 设置界面
            initDao();                             // 初始化数据库命令执行对象
            getData();                             // 获取数据                        // 获取数据
        }
        return rootView;
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
                getData();
            }
        }
    }

    // 获取数据库的数据
    private void getData() {
        list = dbDao.querySystemNews();
        if (list == null || list.size() <= 0) {
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
        dbDao = new MessageSystemDao(context);
    }

    private void setView() {
        TextView title = (TextView) rootView.findViewById(R.id.tv_center);
        title.setText("系统");
        tipView = (TipView) rootView.findViewById(R.id.tip_view);
        mListView = (ListView) rootView.findViewById(R.id.listview_history);
        rootView.findViewById(R.id.head_left_btn).setOnClickListener(this);
        rootView.findViewById(R.id.tv_delete).setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.head_left_btn:
                if (type == null || type.equals("")) return ;
                switch (type) {
                    case StringConstant.TAG_PLAY:
                        PlayerActivity.close();
                        break;
                    case StringConstant.TAG_CHAT:
                        DuiJiangActivity.close();
                        break;
                }
                break;
            case R.id.tv_delete:
                ToastUtils.show_short(context, "删除按钮");
                break;
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (Receiver != null) {
            context.unregisterReceiver(Receiver);
            Receiver = null;
        }
        dbDao = null;
        list = null;
        adapter = null;
        mListView = null;
    }
}