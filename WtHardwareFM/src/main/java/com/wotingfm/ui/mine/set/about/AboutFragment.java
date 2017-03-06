package com.wotingfm.ui.mine.set.about;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.TextView;

import com.wotingfm.R;
import com.wotingfm.ui.mine.main.MineActivity;
import com.wotingfm.util.PhoneMessage;

/**
 * 关于
 * 作者：xinlong on 2016/3/9
 * 邮箱：645700751@qq.com
 */
public class AboutFragment extends Fragment implements OnClickListener {

    private View rootView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (rootView == null) {
            rootView = inflater.inflate(R.layout.activity_about, container, false);
            rootView.setOnClickListener(this);
            setView();
        }
        return rootView;
    }

    // 初始化视图
    private void setView() {
        rootView.findViewById(R.id.head_left_btn).setOnClickListener(this);// 返回

        TextView textVersion = (TextView) rootView.findViewById(R.id.tv_verson);
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
                MineActivity.close();
                break;
        }
    }

}
