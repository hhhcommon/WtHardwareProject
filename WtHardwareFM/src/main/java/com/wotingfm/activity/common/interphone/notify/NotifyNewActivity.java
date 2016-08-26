package com.wotingfm.activity.common.interphone.notify;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import com.wotingfm.R;
import com.wotingfm.activity.common.interphone.notify.adapter.NotifyListAdapter;
import com.wotingfm.activity.common.interphone.notify.model.NotifyNewData;

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
                // 需要一个Type来判断消息类型
                if(position % 2 == 0){
                    // Dialog 样式需自定义  稍后更新
                    new AlertDialog.Builder(NotifyNewActivity.this)
                            .setTitle("审核类信息标题")
                            .setMessage("审核类信息的具体内容审核类信息的具体内容审核类信息的具体内容")
                            .setNegativeButton("同意", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    // 执行"同意"相应的操作并将消息标记为已读
                                    list.get(position).setNumber("0");
                                    adapter.notifyDataSetChanged();
                                }
                            })
                            .setPositiveButton("拒绝", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    // 执行"拒绝"相应的操作并将消息标记为已读
                                    list.get(position).setNumber("0");
                                    adapter.notifyDataSetChanged();
                                }
                            })
                            .show();
                } else {
                    new AlertDialog.Builder(NotifyNewActivity.this)
                            .setTitle("通知类信息标题")
                            .setMessage("通知类信息的具体内容通知类信息的具体内容通知类信息的具体内容")
                            .setNegativeButton("关闭", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    // 将消息标记为已读
                                    list.get(position).setNumber("0");
                                    adapter.notifyDataSetChanged();
                                }
                            })
                            .show();
                }
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
