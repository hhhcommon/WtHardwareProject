package com.wotingfm.activity.im.interphone.notify;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.Dialog;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import com.wotingfm.R;
import com.wotingfm.activity.im.interphone.notify.adapter.NotifyListAdapter;
import com.wotingfm.activity.im.interphone.notify.model.NotifyNewData;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * 消息中心
 */
public class NotifyNewActivity extends Activity {
    private ListView notifyListView;
    private NotifyListAdapter adapter;
    private List<NotifyNewData> list;
    private Dialog notifyContentDialog;

    @TargetApi(Build.VERSION_CODES.KITKAT)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notify_new);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);        // 透明状态栏
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);    // 透明导航栏

        initViews();
    }

    // 初始化视图
    private void initViews(){
        TextView textTitle = (TextView) findViewById(R.id.text_title);
        textTitle.setText("消息中心");

        // 返回 结束当前界面
        findViewById(R.id.left_back).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        notifyListView = (ListView) findViewById(R.id.notify_list_view);// 消息列表
        list = getNotifyNew();
        adapter = new NotifyListAdapter(NotifyNewActivity.this, list);
        notifyListView.setAdapter(adapter);
        setListItemListener();
    }

    /**
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

    /**
     * 显示消息具体内容
     */
    private void notifyContentDialog(int position){
        View dialog = LayoutInflater.from(this).inflate(R.layout.dialog_notify_content, null);
        TextView textTitle = (TextView) dialog.findViewById(R.id.text_title);
        textTitle.setText(list.get(position).getTitle());
        TextView textContent = (TextView) dialog.findViewById(R.id.text_content);
        textContent.setText(list.get(position).getContent());
        notifyContentDialog = new Dialog(this, R.style.MyDialog);
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

    /**
     * 获取消息列表
     */
    private List<NotifyNewData> getNotifyNew(){
        // 测试数据 -------------------------------------------
        List<NotifyNewData> list = new ArrayList<>();
        NotifyNewData notifyNewData;
        for(int i=0; i<100; i++){
            notifyNewData = new NotifyNewData();
            notifyNewData.setTitle("消息标题_" + i);
            int number = new Random().nextInt(300);
            if(number > 99){
                notifyNewData.setNumber("(" + 99 + "+" + ")");
            } else {
                notifyNewData.setNumber("(" + number + ")");
            }
            notifyNewData.setContent("测试数据，看到效果就可以删除_" + i);
            notifyNewData.setTime(new SimpleDateFormat("hh:mm").format(System.currentTimeMillis()));
            list.add(notifyNewData);
        }
        // -----------------------------------------------------
        return list;
    }
}
