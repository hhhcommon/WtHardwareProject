package com.wotingfm.activity.music.program.schedule.activity;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ListView;
import android.widget.TextView;

import com.wotingfm.R;
import com.wotingfm.activity.common.baseactivity.BaseActivity;
import com.wotingfm.util.ToastUtils;

public class ScheduleActivity extends BaseActivity implements OnClickListener {
    public static final String tag = "MUSIC_SCHEDULE_TAG";
    public static boolean isCancelRequest;
    private TextView tv_Name;
    private ListView lv_Main;
    private String ContentId;
    private String ContentName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_schedule);
        setView();
        handleRequestType();
    }

    // 接收上一个页面传递过来的数据
    private void handleRequestType() {
        Intent intent = getIntent();
        ContentId = intent.getStringExtra("ContentId");
        ContentName = intent.getStringExtra("ContentName");
        if (ContentName != null && !ContentName.equals("")) {
            tv_Name.setText(ContentName);
        } else {
            tv_Name.setText(ContentId);
        }

        if (ContentId != null && !ContentId.equals("")) {
            //正常获取到ContentId 进行网络操作

        } else {
            ToastUtils.show_always(context, "数据获取异常");
        }
    }

    // 初始化界面
    private void setView() {
        findViewById(R.id.head_left_btn).setOnClickListener(this);
        tv_Name = (TextView) findViewById(R.id.head_name_tv);
        lv_Main = (ListView) findViewById(R.id.lv_main);
        lv_Main.setSelector(new ColorDrawable(Color.TRANSPARENT));
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.head_left_btn:
                finish();
                break;
        }
    }
}
