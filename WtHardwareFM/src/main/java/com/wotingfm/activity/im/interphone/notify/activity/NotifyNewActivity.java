package com.wotingfm.activity.im.interphone.notify.activity;

import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import com.wotingfm.R;
import com.wotingfm.activity.common.baseactivity.AppBaseActivity;
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
public class NotifyNewActivity extends AppBaseActivity {
    private ListView notifyListView;
    private NotifyListAdapter adapter;
    private List<DBNotifyHistorary> list;
    private NotifyHistoryDao dbDao;
    private MessageReceiver receiver;

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
        deleteDialog();

        notifyListView = (ListView) findViewById(R.id.notify_list_view);// 消息列表
        list = getNotifyNew();
        adapter = new NotifyListAdapter(context, list);
        notifyListView.setAdapter(adapter);
    }

    /*
	 * 初始化数据库命令执行对象
	 */
    private void initDao() {
        dbDao = new NotifyHistoryDao(context);
    }

    /*
	 * 获取数据库的数据
	 */
    private void getDate() {
        list = dbDao.queryHistory();
        adapter = new NotifyListAdapter(context, list);
        notifyListView.setAdapter(adapter);
        setListItemListener();
        clickLongDelete();
    }

    /*
     * 设置ListView的点击监听
     */
    private void setListItemListener(){
        notifyListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, final int position, long id) {
                notifyContentDialog(position);
            }
        });
    }

    // 长按删除
    private void clickLongDelete() {
        notifyListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                NotifyNewActivity.this.position = position;
                deleteDialog.show();
                return true;
            }
        });
    }

    private Dialog deleteDialog;
    private int position;

    // 长按删除对话框
    private void deleteDialog() {
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_exit_confirm, null);
        TextView textTitle = (TextView) dialogView.findViewById(R.id.tv_title);
        textTitle.setText("确定删除通知？");
        dialogView.findViewById(R.id.tv_confirm).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                deleteDialog.dismiss();
                dbDao.deleteHistory(list.get(position).getAddTime());
                list.remove(position);
                adapter.notifyDataSetChanged();
            }
        });
        dialogView.findViewById(R.id.tv_cancle).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                deleteDialog.dismiss();
            }
        });
        deleteDialog = new Dialog(this, R.style.MyDialog);
        deleteDialog.setContentView(dialogView);
        deleteDialog.setCanceledOnTouchOutside(false);
        deleteDialog.getWindow().setBackgroundDrawableResource(R.color.dialog);
    }

    /*
     * 显示消息具体内容
     */
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

    /*
	 * 广播接收  用于刷新界面
	 */
    class MessageReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if(action.equals("push_refreshnews")){
                getDate();
            }
        }
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

    /*
     * 获取消息列表
     */
    private List<DBNotifyHistorary> getNotifyNew(){
        // 测试数据 -------------------------------------------
        List<DBNotifyHistorary> list = new ArrayList<>();
        DBNotifyHistorary notifyNewData;
        for(int i=0; i<100; i++){
            notifyNewData = new DBNotifyHistorary();
            notifyNewData.setTitle("消息标题_" + i);
            notifyNewData.setContent("测试数据，看到效果就可以删除测试数据，看到效果就可以删除测试数据，看到效果就可以删除_" + i);
            notifyNewData.setAddTime(new SimpleDateFormat("hh:mm", Locale.CHINA).format(System.currentTimeMillis()));
            list.add(notifyNewData);
        }
        setListItemListener();
        clickLongDelete();
        // -----------------------------------------------------
        return list;
    }
}
