package com.wotingfm.activity.im.interphone.creategroup.main;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.wotingfm.R;
import com.wotingfm.activity.common.baseactivity.BaseActivity;
import com.wotingfm.activity.im.interphone.creategroup.create.CreateGroupItemActivity;
import com.wotingfm.common.constant.IntegerConstant;
import com.wotingfm.common.constant.StringConstant;

/**
 * 创建群组页面
 * Created by Administrator on 2016/8/24 0024.
 */
public class CreateGroupMainActivity extends BaseActivity implements View.OnClickListener {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_group_main);
        setView();
    }


    private void setView() {
        findViewById(R.id.head_left_btn).setOnClickListener(this);
        findViewById(R.id.lin_groupmain_first).setOnClickListener(this);
        findViewById(R.id.lin_groupmain_second).setOnClickListener(this);
        findViewById(R.id.lin_groupmain_third).setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.head_left_btn:
                finish();
                break;
            case R.id.lin_groupmain_first:       // 创建公开群
                Intent intentPublic = new Intent(context, CreateGroupItemActivity.class);
                intentPublic.putExtra(StringConstant.CREATE_GROUP_TYPE, IntegerConstant.CREATE_GROUP_PUBLIC);
                startActivityForResult(intentPublic, 1);
                break;
            case R.id.lin_groupmain_second:      // 创建密码群
                Intent intentPrivate = new Intent(context, CreateGroupItemActivity.class);
                intentPrivate.putExtra(StringConstant.CREATE_GROUP_TYPE, IntegerConstant.CREATE_GROUP_PRIVATE);
                startActivityForResult(intentPrivate, 1);
                break;
            case R.id.lin_groupmain_third: // 创建验证群
                Intent intentVerification = new Intent(context, CreateGroupItemActivity.class);
                intentVerification.putExtra(StringConstant.CREATE_GROUP_TYPE, IntegerConstant.CREATE_GROUP_VERIFICATION);
                startActivityForResult(intentVerification, 1);
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == 1 && resultCode == 1) {
            finish();
        }
    }
}
