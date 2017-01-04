package com.wotingfm.ui.mine.set.about;

import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;

import com.wotingfm.R;
import com.wotingfm.ui.baseactivity.BaseActivity;
import com.wotingfm.util.PhoneMessage;

/**
 * 关于
 * 作者：xinlong on 2016/3/9
 * 邮箱：645700751@qq.com
 */
public class AboutActivity extends BaseActivity implements OnClickListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);

        setView();
    }

    // 初始化视图
    private void setView() {
        findViewById(R.id.head_left_btn).setOnClickListener(this);// 返回

        TextView textVersion = (TextView) findViewById(R.id.tv_verson);
        String versionCode = PhoneMessage.appVersonName;// 版本号
        if (versionCode == null || versionCode.equals("")) {
            versionCode = "1.0.0.X.001";
        }
        textVersion.setText(versionCode);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.head_left_btn:// 返回
                finish();
                break;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        setContentView(R.layout.activity_null);
    }
}
