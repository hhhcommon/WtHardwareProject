package com.wotingfm.activity.common;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import android.view.View;
import android.widget.TextView;
import com.wotingfm.R;
import com.wotingfm.activity.common.interphone.groupmanage.groupdetail.activity.GroupDetailAcitivity;

/**
 * 主页
 * 作者：xinlong on 2016/8/23 22:59
 * 邮箱：645700751@qq.com
 */
public class MainActivity extends Activity{
    private MainActivity context;
    private TextView tv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        context=this;
        tv=(TextView)findViewById(R.id.tv_hhh);
        tv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(context, GroupDetailAcitivity.class));
            }
        });

    }
}
