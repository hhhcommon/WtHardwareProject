package com.wotingfm.ui.mine.picture;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;
import com.wotingfm.R;
import com.wotingfm.common.constant.StringConstant;
import com.wotingfm.util.PhoneMessage;

import java.util.ArrayList;

/**
 * 查看大图
 */
public class ViewBigPictureActivity extends Activity implements View.OnClickListener {
    private ArrayList<String> pictureUrlList = new ArrayList<>();// 保存图片地址
    private ArrayList<View> mViews = new ArrayList<>();

    private ViewPager mViewPager;
    private TextView textSum;

    private int num;

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.view_black_background) {
            finish();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_big_picture);

        handlerIntent();
        initView();
    }

    // 接收上个界面传递过来的数据
    private void handlerIntent() {
        Intent intent = getIntent();
        if (intent == null) {
            finish();
            return ;
        }
        pictureUrlList = intent.getStringArrayListExtra(StringConstant.PICTURE_URL);
        num = intent.getIntExtra(StringConstant.PICTURE_INDEX, 0);
    }

    // 初始化视图
    private void initView() {
        // 点击黑色的背景区域自动关闭界面
        findViewById(R.id.view_black_background).setOnClickListener(this);
        textSum = (TextView) findViewById(R.id.text_num);

        mViewPager = (ViewPager) findViewById(R.id.view_pager);
        mViewPager.setOffscreenPageLimit(1);

        // 展示图片
        setData();
    }

    // 展示图片
    private void setData() {
        for (int i = 0; i < pictureUrlList.size(); i++) {
            View layout = LayoutInflater.from(this).inflate(R.layout.layout_image, null);
            ImageView imageView = (ImageView) layout.findViewById(R.id.imageView);
            ViewGroup.LayoutParams pra = imageView.getLayoutParams();
            pra.width = PhoneMessage.ScreenWidth;
            imageView.setLayoutParams(pra);
            Picasso.with(this).load(pictureUrlList.get(i)).into(imageView);
            mViews.add(layout);
        }
        textSum.setText("1/" + mViews.size());

        textSum.setText(String.valueOf(num + 1) + "/" + mViews.size());
        mViewPager.setAdapter(new MyPagerAdapter());
        mViewPager.setCurrentItem(num);
        mViewPager.setOnTouchListener(new View.OnTouchListener() {

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                v.getParent().requestDisallowInterceptTouchEvent(true);
                return false;
            }
        });

        mViewPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageSelected(int arg0) {
            }

            @Override
            public void onPageScrolled(int arg0, float arg1, int arg2) {
                textSum.setText((arg0 + 1) + "/" + mViews.size());
                mViewPager.getParent().requestDisallowInterceptTouchEvent(true);
            }

            @Override
            public void onPageScrollStateChanged(int arg0) {
            }
        });
    }

    private class MyPagerAdapter extends PagerAdapter {
        @Override
        public void destroyItem(View v, int position, Object obj) {
			((ViewPager)v).removeView(mViews.get(position));
        }

        @Override
        public void finishUpdate(View arg0) {
        }

        @Override
        public int getCount() {
			return mViews.size();
        }

        @Override
        public Object instantiateItem(View v, int position) {
			((ViewPager)v).addView(mViews.get(position));
			return mViews.get(position);
        }

        @Override
        public boolean isViewFromObject(View arg0, Object arg1) {
            return arg0 == arg1;
        }

        @Override
        public void restoreState(Parcelable arg0, ClassLoader arg1) {
        }

        @Override
        public Parcelable saveState() {
            return null;
        }

        @Override
        public void startUpdate(View arg0) {
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (pictureUrlList != null) {
            pictureUrlList.clear();
            pictureUrlList = null;
        }
        if (mViews != null) {
            mViews.clear();
            mViews = null;
        }
        mViewPager = null;
    }
}
