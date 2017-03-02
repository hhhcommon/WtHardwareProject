package com.wotingfm.ui.music.download.main;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.TextView;

import com.wotingfm.R;
import com.wotingfm.ui.baseadapter.MyFragmentPagerAdapter;
import com.wotingfm.ui.music.download.fragment.DownLoadCompleted;
import com.wotingfm.ui.music.download.fragment.DownLoadUnCompleted;
import com.wotingfm.ui.music.main.PlayerActivity;

import java.util.ArrayList;

/**
 * 下载主页
 */
public class DownloadFragment extends Fragment implements OnClickListener {
    private TextView textCompleted;
    private TextView textUncompleted;
    private ViewPager mViewPager;
    public static Boolean isVisible = false;
    private FragmentActivity context;
    private View rootView;

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.lin_news:// 返回
                PlayerActivity activity = (PlayerActivity) getActivity();
                activity.fm.popBackStack();
                break;
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if(rootView==null) {
            rootView = inflater.inflate(R.layout.fragment_download, container, false);
            context = getActivity();
            initView();
            initViewPager();
        }
        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();
        isVisible = true;
    }

    // 设置界面
    private void initView() {
        rootView.findViewById(R.id.lin_news).setOnClickListener(this);// 返回

        textCompleted = (TextView) rootView.findViewById(R.id.tv_completed);
        textUncompleted = (TextView) rootView.findViewById(R.id.tv_uncompleted);
        mViewPager = (ViewPager) rootView.findViewById(R.id.viewpager);
    }

    // 初始化 ViewPager
    private void initViewPager() {
        ArrayList<Fragment> fragmentList = new ArrayList<>();
        fragmentList.add(new DownLoadCompleted());
        fragmentList.add(new DownLoadUnCompleted());
        mViewPager.setAdapter(new MyFragmentPagerAdapter(getChildFragmentManager(), fragmentList));
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
    public void onDestroy() {
        super.onDestroy();
        isVisible = false;
        textCompleted = null;
        textUncompleted = null;
        mViewPager = null;
    }
}
