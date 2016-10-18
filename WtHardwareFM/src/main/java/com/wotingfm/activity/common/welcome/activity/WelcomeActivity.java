package com.wotingfm.activity.common.welcome.activity;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.WindowManager;
import android.widget.ImageView;
import com.wotingfm.R;
import com.wotingfm.activity.common.baseadapter.MyFragmentPagerAdapter;
import com.wotingfm.activity.common.welcome.fragment.WelcomeAFragment;
import com.wotingfm.activity.common.welcome.fragment.WelcomeBBBFragment;
import com.wotingfm.activity.common.welcome.fragment.WelcomeCFragment;
import java.util.ArrayList;
/**
 * 引导页
 * 作者：xinlong on 2016/4/27 21:18
 * 邮箱：645700751@qq.com
 */
public class WelcomeActivity extends FragmentActivity {
    private ImageView[] imageViews;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcomes);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);        // 透明状态栏
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);    // 透明导航栏
        imageViews = new ImageView[3];        //设置引导页下标小红点
        ViewGroup group = (ViewGroup) findViewById(R.id.viewGroup);
        for (int i = 0; i < 3; i++) {
            ImageView imageView = new ImageView(this);
            imageView.setLayoutParams(new LayoutParams(20, 20));
            imageView.setPadding(0, 0, 20, 20);
            imageViews[i] = imageView;
            if (i == 0) {
                //默认选中第一张图片
                imageViews[i].setBackgroundResource(R.mipmap.page_indicator_focused);
            } else {
                imageViews[i].setBackgroundResource(R.mipmap.page_indicator);
            }
            group.addView(imageViews[i]);
        }
        InitViewPager();
    }

    //初始化ViewPager
    private void InitViewPager() {
        ViewPager mPager = (ViewPager) findViewById(R.id.viewpager);
        mPager.setOffscreenPageLimit(1);
        ArrayList<Fragment> fragmentList = new ArrayList<Fragment>();
        Fragment btFragmentA = new WelcomeAFragment();
        Fragment btFragmentB = new WelcomeBBBFragment();
        Fragment btFragmentC = new WelcomeCFragment();
        fragmentList.add(btFragmentA);
        fragmentList.add(btFragmentB);
        fragmentList.add(btFragmentC);
        mPager.setAdapter(new MyFragmentPagerAdapter(getSupportFragmentManager(), fragmentList));
        mPager.setOnPageChangeListener(new GuidePageChangeListener());
        mPager.setCurrentItem(0);// 设置当前显示标签页为第一页
    }

    // 指引页面更改事件监听器
    class GuidePageChangeListener implements OnPageChangeListener {
        @Override
        public void onPageScrollStateChanged(int arg0) {
        }

        @Override
        public void onPageScrolled(int arg0, float arg1, int arg2) {
        }

        @Override
        public void onPageSelected(int arg0) {
            for (int i = 0; i < imageViews.length; i++) {
                imageViews[arg0].setBackgroundResource(R.mipmap.page_indicator_focused);
                if (arg0 != i) {
                    imageViews[i].setBackgroundResource(R.mipmap.page_indicator);
                }
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        imageViews = null;
    }
}
