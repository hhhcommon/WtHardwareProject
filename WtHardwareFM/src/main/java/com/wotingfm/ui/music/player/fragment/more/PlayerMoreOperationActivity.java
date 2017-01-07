package com.wotingfm.ui.music.player.fragment.more;

import android.os.Bundle;
import android.view.View;

import com.wotingfm.R;
import com.wotingfm.ui.baseactivity.AppBaseActivity;

/**
 * 播放界面  ->  更多操作
 */
public class PlayerMoreOperationActivity extends AppBaseActivity implements View.OnClickListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_player_more_operation);

        initView();
        initEvent();
    }

    // 初始化视图
    private void initView() {

    }

    // 初始化点击事件
    private void initEvent() {
        findViewById(R.id.head_left_btn).setOnClickListener(this);// 返回
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.head_left_btn:// 返回
                finish();
                break;
        }
    }
}
