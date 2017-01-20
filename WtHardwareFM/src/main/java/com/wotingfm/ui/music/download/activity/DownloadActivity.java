package com.wotingfm.ui.music.download.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;

import com.wotingfm.R;
import com.wotingfm.ui.baseactivity.AppBaseFragmentActivity;
import com.wotingfm.ui.baseadapter.MyFragmentPagerAdapter;
import com.wotingfm.ui.music.download.fragment.DownLoadCompleted;
import com.wotingfm.ui.music.download.fragment.DownLoadUnCompleted;

import java.util.ArrayList;

/**
 * 下载主页
 */
public class DownloadActivity extends AppBaseFragmentActivity implements OnClickListener {
    private TextView textCompleted;
    private TextView textUncompleted;
    private ViewPager mViewPager;
    public static Boolean isVisible = false;

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.lin_news:// 返回
                finish();
                break;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_download);
        initView();
        initViewPager();
    }

    @Override
    protected void onResume() {
        super.onResume();
        isVisible = true;
    }

    // 设置界面
    private void initView() {
        findViewById(R.id.lin_news).setOnClickListener(this);// 返回

        textCompleted = (TextView) findViewById(R.id.tv_completed);
        textUncompleted = (TextView) findViewById(R.id.tv_uncompleted);
        mViewPager = (ViewPager) findViewById(R.id.viewpager);
    }

    // 初始化 ViewPager
    private void initViewPager() {
        ArrayList<Fragment> fragmentList = new ArrayList<>();
        fragmentList.add(new DownLoadCompleted());
        fragmentList.add(new DownLoadUnCompleted());
        mViewPager.setAdapter(new MyFragmentPagerAdapter(getSupportFragmentManager(), fragmentList));
        mViewPager.setOnPageChangeListener(new MyOnPageChangeListener());
        mViewPager.setCurrentItem(0);
        mViewPager.setOffscreenPageLimit(1);
        textCompleted.setOnClickListener(new DownloadClickListener(0));
        textUncompleted.setOnClickListener(new DownloadClickListener(1));
    }

    // 更新界面
    private void updateView(int index) {
        if (index == 0) {
            handleView(index);
        } else if (index == 1) {
            handleView(index);
        }
    }

    private void handleView(int index) {
        if (index == 0) {
            textCompleted.setTextColor(context.getResources().getColor(R.color.dinglan_orange));
            textCompleted.setBackgroundResource(R.drawable.color_wt_circle_home_white);
            textUncompleted.setTextColor(context.getResources().getColor(R.color.white));
            textUncompleted.setBackgroundResource(R.drawable.color_wt_circle_orange);
        } else {
            textUncompleted.setTextColor(context.getResources().getColor(R.color.dinglan_orange));
            textUncompleted.setBackgroundResource(R.drawable.color_wt_circle_home_white);
            textCompleted.setTextColor(context.getResources().getColor(R.color.white));
            textCompleted.setBackgroundResource(R.drawable.color_wt_circle_orange);
        }
    }

    // 跳转
    public void toActivity(Intent intent) {
        startActivityForResult(intent, 1001);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1001 && resultCode == 1) {
            finish();
        }
    }

    class DownloadClickListener implements OnClickListener {
        private int index = 0;

        public DownloadClickListener(int i) {
            index = i;
        }

        @Override
        public void onClick(View v) {
            mViewPager.setCurrentItem(index);        // 界面切换字体的改变
            updateView(index);
        }
    }

    class MyOnPageChangeListener implements OnPageChangeListener {

        @Override
        public void onPageScrolled(int arg0, float arg1, int arg2) {
        }

        @Override
        public void onPageScrollStateChanged(int arg0) {
        }

        @Override
        public void onPageSelected(int arg0) {
            updateView(arg0);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        isVisible = false;
        textCompleted = null;
        textUncompleted = null;
        mViewPager = null;
        setContentView(R.layout.activity_null);
    }
}
