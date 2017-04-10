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
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import com.wotingfm.R;
import com.wotingfm.common.constant.BroadcastConstants;
import com.wotingfm.common.constant.StringConstant;
import com.wotingfm.ui.interphone.main.DuiJiangActivity;
import com.wotingfm.ui.interphone.message.messagecenter.adapter.MessageSubscriberAdapter;
import com.wotingfm.ui.interphone.message.messagecenter.dao.MessageSubscriberDao;
import com.wotingfm.ui.interphone.message.messagecenter.model.DBSubscriberMessage;
import com.wotingfm.ui.music.album.main.AlbumFragment;
import com.wotingfm.ui.music.main.PlayerActivity;
import com.wotingfm.ui.music.program.radiolist.model.ListInfo;
import com.wotingfm.util.ToastUtils;
import com.wotingfm.widget.TipView;

import java.util.ArrayList;
import java.util.List;

public class MessageSubscriberFragment extends Fragment implements OnClickListener {
    private MessageSubscriberDao dbDao;
    private MessageReceiver Receiver;

    private View rootView;
    private FragmentActivity context;
    private TipView tipView;
    private ListView mListView;
    private String type;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (rootView == null) {
            rootView = inflater.inflate(R.layout.activity_notifynews, container, false);
            context = getActivity();
            type = getArguments().getString(StringConstant.FROM_TYPE);
            setView();                             // 设置界面
            initDao();                             // 初始化数据库命令执行对象
            getData();                             // 获取数据
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
        List<DBSubscriberMessage> list = dbDao.querySubscriberMessage();
        if (list == null || list.size() <= 0) {
            tipView.setVisibility(View.VISIBLE);
            tipView.setTipView(TipView.TipStatus.NO_DATA, "您还没有收到任何的通知消息");
        } else {
            tipView.setVisibility(View.GONE);
            // 测试代码
//            list.get(0).setSeqId("123456");
//            list.add(list.get(0));
//            list.add(list.get(0));
            dealList(list);
        }
    }

    private void dealList(List<DBSubscriberMessage> list) {

        // 组装专辑显示数据
//        ArrayList<DBSubscriberMessage> _list = new ArrayList<DBSubscriberMessage>();
//        for(int i=0;i<list.size();i++) {
//            if (!_list.contains(list.get(i))) {
//                _list.add(list.get(i));
//            }
//    }
//        Iterator<DBSubscriberMessage> it=list.iterator();
//        while(it.hasNext()){
//            DBSubscriberMessage a = it.next();
//            if(_list.contains(a)){
//                it.remove();
//            }else{
//                _list.add(a);
//            }
//        }

        // 组装专辑显示数据
        ArrayList<DBSubscriberMessage> _list = new ArrayList<>();
        if (_list.size() <= 0) {
            _list.add(list.get(0));
        }
        for (int i = 0; i < list.size(); i++) {
            String _id = list.get(i).getSeqId().trim();
            boolean type = false;
            for (int j = 0; j < _list.size(); j++) {
                // 如果数据出错则该条数据不要
                String id = _list.get(j).getSeqId().trim();
                try {
                    if (_id.equals(id)) {
                        type = false;
                        break;
                    } else {
                        type = true;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            if (type) {
                _list.add(list.get(i));
            }
        }
        // 组装更新数目
        getList(list, _list);
    }

    private void getList(List<DBSubscriberMessage> list, ArrayList<DBSubscriberMessage> _list) {
        for (int i = 0; i < _list.size(); i++) {
            int n = 0;
            String _id = _list.get(i).getSeqId();
            for (int j = 0; j < list.size(); j++) {
                // 如果数据出错则该条数据不要
                try {
                    if (_id.trim().equals(list.get(j).getSeqId())) {
                        n++;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            _list.get(i).setNum(String.valueOf(n));
        }

        MessageSubscriberAdapter adapter = new MessageSubscriberAdapter(context, _list);
        mListView.setAdapter(adapter);
        setOnItemClickListener(_list);
    }

    // listView的点击事件
    private void setOnItemClickListener(final ArrayList<DBSubscriberMessage> _list) {
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                ListInfo listInfo = new ListInfo();
                listInfo.setContentName(_list.get(position).getSeqName());
                listInfo.setContentDescn("");
                listInfo.setContentId(_list.get(position).getSeqId());

                // 跳往专辑界面
                AlbumFragment fragment = new AlbumFragment();
                Bundle bundle = new Bundle();
                bundle.putInt("fromType", -1);
                bundle.putString(StringConstant.FROM_TYPE, type);
                bundle.putString("type", "radiolistactivity");
                bundle.putSerializable("list", listInfo);
                fragment.setArguments(bundle);
                if (type != null) {
                    if (type.equals(StringConstant.TAG_PLAY)) {
                        PlayerActivity.open(fragment);
                    } else {
                        DuiJiangActivity.open(fragment);
                    }
                }
            }
        });
    }

    // 初始化数据库命令执行对象
    private void initDao() {
        dbDao = new MessageSubscriberDao(context);
    }

    private void setView() {
        TextView title = (TextView) rootView.findViewById(R.id.tv_center);
        title.setText("通知");
        tipView = (TipView) rootView.findViewById(R.id.tip_view);
        mListView = (ListView) rootView.findViewById(R.id.listview_history);
        rootView.findViewById(R.id.head_left_btn).setOnClickListener(this);
        rootView.findViewById(R.id.tv_delete).setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.head_left_btn:
                if (type != null) {
                    if (type.equals(StringConstant.TAG_PLAY)) {
                        PlayerActivity.close();
                    } else {
                        DuiJiangActivity.close();
                    }
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
        Fragment targetFragment = getTargetFragment();
        ((MessageFragment) targetFragment).setResult(2, 1);
        if (Receiver != null) {
            context.unregisterReceiver(Receiver);
            Receiver = null;
        }
        dbDao = null;
        mListView = null;
    }
}