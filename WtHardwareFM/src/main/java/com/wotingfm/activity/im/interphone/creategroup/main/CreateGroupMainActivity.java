package com.wotingfm.activity.im.interphone.creategroup.main;

import android.content.Intent;
import android.view.View;

import com.wotingfm.R;
import com.wotingfm.activity.im.interphone.creategroup.create.CreateGroupItemActivity;
import com.wotingfm.activity.common.baseactivitya.AppBaseActivity;
import com.wotingfm.common.constant.IntegerConstant;
import com.wotingfm.common.constant.StringConstant;

/**
 * 创建群组页面
 * Created by Administrator on 2016/8/24 0024.
 */
public class CreateGroupMainActivity extends AppBaseActivity implements View.OnClickListener {

    @Override
    protected int setViewId() {
        return R.layout.activity_create_group_main;
    }

    @Override
    protected void init() {
        setTitle("创建群组");

        findViewById(R.id.relative_create_public).setOnClickListener(this);         // 创建公开群 无需验证就可加入
        findViewById(R.id.relative_create_private).setOnClickListener(this);        // 创建密码群 需要密码才来加入
        findViewById(R.id.relative_create_verification).setOnClickListener(this);   // 创建验证群 需要验证才能加入
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.relative_create_public:       // 创建公开群
                Intent intentPublic = new Intent(context, CreateGroupItemActivity.class);
                intentPublic.putExtra(StringConstant.CREATE_GROUP_TYPE, IntegerConstant.CREATE_GROUP_PUBLIC);
                startActivity(intentPublic);
                break;
            case R.id.relative_create_private:      // 创建密码群
                Intent intentPrivate = new Intent(context, CreateGroupItemActivity.class);
                intentPrivate.putExtra(StringConstant.CREATE_GROUP_TYPE, IntegerConstant.CREATE_GROUP_PRIVATE);
                startActivity(intentPrivate);
                break;
            case R.id.relative_create_verification: // 创建验证群
                Intent intentVerification = new Intent(context, CreateGroupItemActivity.class);
                intentVerification.putExtra(StringConstant.CREATE_GROUP_TYPE, IntegerConstant.CREATE_GROUP_VERIFICATION);
                startActivity(intentVerification);
                break;
        }
    }
}
