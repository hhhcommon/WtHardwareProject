package com.wotingfm.activity.im.interphone.notify.activity;

import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import com.wotingfm.R;
import com.wotingfm.activity.common.baseActivity.AppBaseActivity;
import com.wotingfm.activity.im.interphone.linkman.dao.NotifyHistoryDao;
import com.wotingfm.activity.im.interphone.linkman.model.DBNotifyHistorary;
import com.wotingfm.activity.im.interphone.notify.adapter.NotifyListAdapter;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * 通知消息
 */
public class NotifyNewActivity extends AppBaseActivity implements View.OnClickListener{
    private MessageReceiver receiver;           // 更新通知列表的广播
    private NotifyHistoryDao dbDao;             // 数据库
    private Dialog deleteDialog;
    private NotifyListAdapter adapter;

    private ListView notifyListView;            // 显示通知列表
    private List<DBNotifyHistorary> list;       // 通知列表

    private int count;                          // 选中的数量
    private boolean isDelete;                   // 是否删除状态

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.right_more:               // 删除 列表为空则不显示
                if(isDelete) {
                    if(adapter.checkChooseNumber(list) == 0) {
                        adapter.setNoCheckState(list);
                    } else {
                        count = adapter.checkChooseNumber(list);
                        deleteDialog();
                    }
                } else {
                    adapter.setCheckState(list);
                }
                isDelete = !isDelete;
                break;
            case R.id.tv_confirm:               // 确定删除
                deleteDialog.dismiss();
                for(int i=0; i<list.size(); i++) {
                    if(list.get(i).getState() == 1) {
                        dbDao.deleteHistory(list.get(i).getAddTime());
                        list.remove(i);
                    }
                }
                adapter.setNoCheckState(list);  // 设置列表为非选择状态
                isDelete = false;
                break;
            case R.id.tv_cancle:                // 取消删除
                deleteDialog.dismiss();
                break;
        }
    }

    @Override
    protected int setViewId() {
        return R.layout.activity_notify_new;
    }

    @Override
    protected void init() {
        if(receiver == null) {		// 注册广播
            receiver = new MessageReceiver();
            IntentFilter filter = new IntentFilter();
            filter.addAction("push_refreshnews");
            registerReceiver(receiver, filter);
        }
        initDao();
        setTitle("消息中心");

        notifyListView = (ListView) findViewById(R.id.notify_list_view);// 消息列表
        getDate();
//        list = getNotifyNew();
//        adapter = new NotifyListAdapter(context, list);
//        adapter.setNoCheckState(list);// 设置列表为非选择状态
//        notifyListView.setAdapter(adapter);
//        if(list.size() > 0) {
//            setRightText("删除", this);
//        }
    }

    // 初始化数据库命令执行对象
    private void initDao() {
        dbDao = new NotifyHistoryDao(context);
    }

    // 获取数据库的数据
    private void getDate() {
        list = dbDao.queryHistory();
        adapter = new NotifyListAdapter(context, list);
        adapter.setNoCheckState(list);// 设置列表为非选择状态
        notifyListView.setAdapter(adapter);
        setListItemListener();
        if(list.size() > 0) {
            setRightText("删除", this);
        }
    }

    // 设置ListView的点击监听
    private void setListItemListener(){
        notifyListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, final int position, long id) {
                if(isDelete) {
                    adapter.setCheckChooseState(list, position);
                } else {
                    notifyContentDialog(position);
                }
            }
        });
    }

    // 长按删除对话框
    private void deleteDialog() {
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_exit_confirm, null);
        deleteDialog = new Dialog(this, R.style.MyDialog);
        TextView textTitle = (TextView) dialogView.findViewById(R.id.tv_title);
        textTitle.setText("确定删除选中的" + count + "条通知？");
        dialogView.findViewById(R.id.tv_confirm).setOnClickListener(this);
        dialogView.findViewById(R.id.tv_cancle).setOnClickListener(this);
        deleteDialog.setContentView(dialogView);
        deleteDialog.setCanceledOnTouchOutside(false);
        deleteDialog.getWindow().setBackgroundDrawableResource(R.color.dialog);
        deleteDialog.show();
    }

    // 显示消息具体内容
    private void notifyContentDialog(int position){
        View dialog = LayoutInflater.from(context).inflate(R.layout.dialog_notify_content, null);
        TextView textTitle = (TextView) dialog.findViewById(R.id.text_title);
        textTitle.setText(list.get(position).getTitle());
        TextView textContent = (TextView) dialog.findViewById(R.id.text_content);
        textContent.setText(list.get(position).getContent());
        final Dialog notifyContentDialog = new Dialog(context, R.style.MyDialog);
        notifyContentDialog.setContentView(dialog);
        notifyContentDialog.setCanceledOnTouchOutside(true);
        notifyContentDialog.getWindow().setBackgroundDrawableResource(R.color.dialog);
        notifyContentDialog.show();
        dialog.findViewById(R.id.btn_close).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                notifyContentDialog.dismiss();
            }
        });
    }

    // 广播接收  用于刷新界面
    class MessageReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if(action.equals("push_refreshnews")){
                getDate();
            }
        }
    }

    // 返回键功能
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (event.getAction() == KeyEvent.ACTION_DOWN && KeyEvent.KEYCODE_BACK == keyCode) {
            if (isDelete) {
                adapter.setNoCheckState(list);
                isDelete = false;
            } else {
                finish();
            }
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(receiver != null){
            unregisterReceiver(receiver);
            receiver = null;
        }
        dbDao = null;
        list = null;
        adapter = null;
        notifyListView = null;
    }

    // 获取消息列表
    private List<DBNotifyHistorary> getNotifyNew(){
        // 测试数据 -------------------------------------------
        List<DBNotifyHistorary> list = new ArrayList<>();
        DBNotifyHistorary notifyNewData;
        for(int i=0; i<10; i++){
            notifyNewData = new DBNotifyHistorary();
            notifyNewData.setTitle("消息标题_" + i);
            notifyNewData.setContent("测试数据，看到效果就可以删除测试数据，看到效果就可以删除测试数据，看到效果就可以删除_" + i);
            notifyNewData.setAddTime(new SimpleDateFormat("hh:mm", Locale.CHINA).format(System.currentTimeMillis()));
            list.add(notifyNewData);
        }
        setListItemListener();
        // -----------------------------------------------------
        return list;
    }
}
