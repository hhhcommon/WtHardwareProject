package com.wotingfm.activity.im.interphone.creategroup.main;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;

import com.wotingfm.R;
import com.wotingfm.activity.im.interphone.creategroup.create.CreateGroupItemActivity;
import com.wotingfm.common.constant.IntegerConstant;
import com.wotingfm.common.constant.StringConstant;

/**
 * 创建群组页面
 * Created by Administrator on 2016/8/24 0024.
 */
public class CreateGroupMainActivity extends Activity implements View.OnClickListener {

    @TargetApi(Build.VERSION_CODES.KITKAT)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_group_main);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);		// 透明状态栏
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);	// 透明导航栏

        initViews();
    }

    /*
     * 初始化视图
     */
    private void initViews(){
        TextView textTitle = (TextView) findViewById(R.id.text_title);
        textTitle.setText("创建群组");

        TextView leftBack = (TextView) findViewById(R.id.left_back);
        leftBack.setOnClickListener(this);

        View relativeCreatePublic = findViewById(R.id.relative_create_public);               // 创建公开群 无需验证可加入
        relativeCreatePublic.setOnClickListener(this);

        View relativeCreatePrivate = findViewById(R.id.relative_create_private);             // 创建密码群 需要密码才来加入
        relativeCreatePrivate.setOnClickListener(this);

        View relativeCreateVerification = findViewById(R.id.relative_create_verification);   // 创建验证群 需要通过验证才能加入
        relativeCreateVerification.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.left_back:                    // 返回到上一个页面
                finish();
                break;
            case R.id.relative_create_public:       // 创建公开群
                Intent intentPublic = new Intent(CreateGroupMainActivity.this, CreateGroupItemActivity.class);
                intentPublic.putExtra(StringConstant.CREATE_GROUP_TYPE, IntegerConstant.CREATE_GROUP_PUBLIC);
                startActivity(intentPublic);
                break;
            case R.id.relative_create_private:      // 创建密码群
                Intent intentPrivate = new Intent(CreateGroupMainActivity.this, CreateGroupItemActivity.class);
                intentPrivate.putExtra(StringConstant.CREATE_GROUP_TYPE, IntegerConstant.CREATE_GROUP_PRIVATE);
                startActivity(intentPrivate);
                break;
            case R.id.relative_create_verification: // 创建验证群
                Intent intentVerification = new Intent(CreateGroupMainActivity.this, CreateGroupItemActivity.class);
                intentVerification.putExtra(StringConstant.CREATE_GROUP_TYPE, IntegerConstant.CREATE_GROUP_VERIFICATION);
                startActivity(intentVerification);
                break;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        setContentView(R.layout.activity_null_view);
    }
}
