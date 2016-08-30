package com.wotingfm.activity.common;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

import com.wotingfm.R;
import com.wotingfm.activity.common.interphone.groupmanage.groupdetail.activity.GroupDetailAcitivity;
import com.wotingfm.activity.common.interphone.groupmanage.memberadd.activity.MemberAddActivity;

/**
 * 主页
 * 作者：xinlong on 2016/8/23 22:59
 * 邮箱：645700751@qq.com
 */
public class MainActivity extends Activity {
    private Button create_group;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        create_group=(Button)findViewById(R.id.create_group);
        create_group.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this,GroupDetailAcitivity.class));
            }
        });

    }
}
