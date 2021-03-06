
package com.wotingfm.ui.music.program.main;

import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.ImageView;
import android.widget.TextView;

import com.wotingfm.R;
import com.wotingfm.ui.baseadapter.MyFragmentChildPagerAdapter;
import com.wotingfm.ui.main.MainActivity;
import com.wotingfm.ui.music.program.diantai.fragment.OnLineFragment;
import com.wotingfm.ui.music.program.fenlei.fragment.FenLeiFragment;
import com.wotingfm.ui.music.program.tuijian.fragment.RecommendFragment;
import com.wotingfm.util.PhoneMessage;

import java.util.ArrayList;

/**
 * 节目页
 *
 * @author 辛龙
 *         2016年2月26日
 */
public class ProgramFragment extends Fragment {
    private FragmentActivity context;
    private View rootView;
    private TextView view1;
    private TextView view2;
    private TextView view3;
    private ImageView image;
    private int bmpW;
    private int offset;
    private ViewPager mPager;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (rootView == null) {
            rootView = inflater.inflate(R.layout.fragment_wt_news, container, false);
            rootView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                }
            });
            context = getActivity();
            initTextView();
            initImage();
            initViewPager();
        }
        return rootView;
    }

    private void initTextView() {
        rootView.findViewById(R.id.head_left_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MainActivity.changeOne();
            }
        });
        rootView.findViewById(R.id.lin_find).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MainActivity.SearchLikeActivityJumpType=2;
                MainActivity.changeFive();
            }
        });

        view1 = (TextView) rootView.findViewById(R.id.tv_guid1);
        view2 = (TextView) rootView.findViewById(R.id.tv_guid2);
        view3 = (TextView) rootView.findViewById(R.id.tv_guid3);
        view1.setOnClickListener(new txListener(0));
        view2.setOnClickListener(new txListener(1));
        view3.setOnClickListener(new txListener(2));
    }

    public class txListener implements View.OnClickListener {
        private int index = 0;

        public txListener(int i) {
            index = i;
        }

        @Override
        public void onClick(View v) {
            mPager.setCurrentItem(index);
            initViews(index);
        }
    }

    /**
     * 初始化图片的位移像素
     */
    public void initImage() {
        image = (ImageView) rootView.findViewById(R.id.cursor);
        LayoutParams lp = image.getLayoutParams();
        lp.width = (PhoneMessage.ScreenWidth / 3);
        image.setLayoutParams(lp);
        bmpW = BitmapFactory.decodeResource(getResources(), R.mipmap.left_personal_bg).getWidth();
        DisplayMetrics dm = new DisplayMetrics();
        context.getWindowManager().getDefaultDisplay().getMetrics(dm);
        int screenW = dm.widthPixels;
        offset = (screenW / 3 - bmpW) / 2;
        // imageView 设置平移，使下划线平移到初始位置（平移一个offset）
        Matrix matrix = new Matrix();
        matrix.postTranslate(offset, 0);
        image.setImageMatrix(matrix);
    }

    /**
     * 初始化ViewPager
     */
    public void initViewPager() {
        mPager = (ViewPager) rootView.findViewById(R.id.viewpager);
        mPager.setOffscreenPageLimit(1);
        ArrayList<Fragment> fragmentList = new ArrayList<>();
        fragmentList.add(new RecommendFragment());
        fragmentList.add(new OnLineFragment());
        fragmentList.add(new FenLeiFragment());
        mPager.setAdapter(new MyFragmentChildPagerAdapter(getChildFragmentManager(), fragmentList));
        mPager.setOnPageChangeListener(new MyOnPageChangeListener());// 页面变化时的监听器
        mPager.setCurrentItem(0);// 设置当前显示标签页为第一页mPager
//		mPager.setScanScroll(false);
    }

    class MyOnPageChangeListener implements OnPageChangeListener {
        private int one = offset * 2 + bmpW;// 两个相邻页面的偏移量
        private int currIndex;

        @Override
        public void onPageScrolled(int arg0, float arg1, int arg2) {
        }

        @Override
        public void onPageScrollStateChanged(int arg0) {
        }

        @Override
        public void onPageSelected(int arg0) {
            Animation animation = new TranslateAnimation(currIndex * one, arg0 * one, 0, 0);// 平移动画
            currIndex = arg0;
            animation.setFillAfter(true);// 动画终止时停留在最后一帧，不然会回到没有执行前的状态
            animation.setDuration(200);// 动画持续时间0.2秒
            image.startAnimation(animation);// 是用ImageView来显示动画的
            initViews(currIndex);
        }
    }

    // 初始化视图
    private void initViews(int index) {
        if (index == 0) {
            view1.setTextColor(context.getResources().getColor(R.color.dinglan_orange));
            view2.setTextColor(context.getResources().getColor(R.color.group_item_text2));
            view3.setTextColor(context.getResources().getColor(R.color.group_item_text2));
        } else if (index == 1) {
            view1.setTextColor(context.getResources().getColor(R.color.group_item_text2));
            view2.setTextColor(context.getResources().getColor(R.color.dinglan_orange));
            view3.setTextColor(context.getResources().getColor(R.color.group_item_text2));
        } else if (index == 2) {
            view1.setTextColor(context.getResources().getColor(R.color.group_item_text2));
            view2.setTextColor(context.getResources().getColor(R.color.group_item_text2));
            view3.setTextColor(context.getResources().getColor(R.color.dinglan_orange));
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (null != rootView) {
            ((ViewGroup) rootView.getParent()).removeView(rootView);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        rootView = null;
        context = null;
        view1 = null;
        view2 = null;
        view3 = null;
        image = null;
        mPager = null;
    }
}
