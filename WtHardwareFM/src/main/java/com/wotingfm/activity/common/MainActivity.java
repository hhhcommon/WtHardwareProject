package com.wotingfm.activity.common;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.wotingfm.R;
import com.wotingfm.activity.common.interphone.creategroup.main.CreateGroupMainActivity;

/**
 * 主页
 * 作者：xinlong on 2016/8/23 22:59
 * 邮箱：645700751@qq.com
 */
public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // 跳转到创建群组界面 此处还未开发 但应该有一个按钮可以跳转到创建群组界面
        findViewById(R.id.create_group).setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, CreateGroupMainActivity.class));
            }
        });
    }
}
