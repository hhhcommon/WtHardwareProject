package com.wotingfm.activity.music.download.activity;

import android.annotation.TargetApi;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.StatFs;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.text.format.Formatter;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.TextView;

import com.wotingfm.R;
import com.wotingfm.activity.common.baseadapter.MyFragmentPagerAdapter;
import com.wotingfm.activity.music.download.fragment.DownLoadCompleted;
import com.wotingfm.activity.music.download.fragment.DownLoadUnCompleted;

import java.util.ArrayList;

/**
 * 下载主页
 */
public class DownloadActivity extends FragmentActivity {
    private TextView textCompleted;
    private TextView textUncompleted;
    private TextView textMemory;
    private ViewPager viewPagerDownload;
    public static Boolean isVisible = false;

    @TargetApi(Build.VERSION_CODES.KITKAT)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_download);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);        // 透明状态栏
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);    // 透明导航栏
        setView();
    }

    @Override
    protected void onResume() {
        super.onResume();
        isVisible = true;
    }

    /**
     * 设置界面
     */
    private void setView() {
        findViewById(R.id.left_image).setOnClickListener(new OnClickListener() { // 返回
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        TextView textTitle = (TextView) findViewById(R.id.text_title);
        textTitle.setText("本地音频");

        textMemory = (TextView) findViewById(R.id.text_memory);
        getAvailSpace();

        textCompleted = (TextView) findViewById(R.id.tv_completed);     // 已下载
        textUncompleted = (TextView) findViewById(R.id.tv_uncompleted); // 正在下载
        viewPagerDownload = (ViewPager) findViewById(R.id.viewpager);

        ArrayList<Fragment> fragmentList = new ArrayList<>();
        Fragment mDownLoadFragment = new DownLoadCompleted();
        Fragment mDownLoadUnFragment = new DownLoadUnCompleted();
        fragmentList.add(mDownLoadFragment);
        fragmentList.add(mDownLoadUnFragment);
        viewPagerDownload.setAdapter(new MyFragmentPagerAdapter(getSupportFragmentManager(), fragmentList));
        viewPagerDownload.setOnPageChangeListener(new MyOnPageChangeListener());
        viewPagerDownload.setCurrentItem(0);
        viewPagerDownload.setOffscreenPageLimit(1);
        textCompleted.setOnClickListener(new DownloadClickListener(0));
        textUncompleted.setOnClickListener(new DownloadClickListener(1));
    }

    public class MyOnPageChangeListener implements OnPageChangeListener {

        @Override
        public void onPageScrolled(int arg0, float arg1, int arg2) {
        }

        @Override
        public void onPageScrollStateChanged(int arg0) {
        }

        @Override
        public void onPageSelected(int arg0) {
            if (arg0 == 0) {
                textCompleted.setBackgroundResource(R.color.dinglan_orange_light);
                textUncompleted.setBackgroundResource(R.color.WHITE);
            } else if (arg0 == 1) {
                textUncompleted.setBackgroundResource(R.color.dinglan_orange_light);
                textCompleted.setBackgroundResource(R.color.WHITE);
            }
        }
    }

    public class DownloadClickListener implements OnClickListener {
        private int index = 0;

        public DownloadClickListener(int i) {
            index = i;
        }

        @Override
        public void onClick(View v) {
            viewPagerDownload.setCurrentItem(index);        // 界面切换字体的改变
            if (index == 0) {
                textCompleted.setBackgroundResource(R.color.dinglan_orange_light);
                textUncompleted.setBackgroundResource(R.color.WHITE);
            } else if (index == 1) {
                textUncompleted.setBackgroundResource(R.color.dinglan_orange_light);
                textCompleted.setBackgroundResource(R.color.WHITE);
            }
        }
    }

    /**
     * 根据路劲获取某个目录的可用空间
     */
    private void getAvailSpace() {
        StatFs statFs = new StatFs(Environment.getExternalStorageDirectory().getPath());
        long blockSize;
        long totalBlocks;
        long availableBlocks;

        // 由于API18（Android4.3）以后getBlockSize过时并且改为了getBlockSizeLong
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
            blockSize = statFs.getBlockSizeLong();// 获取分区的大小
            totalBlocks = statFs.getBlockCountLong();// 获取分区的个数
            availableBlocks = statFs.getAvailableBlocksLong();// 获取可用分区的个数
        } else {
            blockSize = statFs.getBlockSize();// 获取分区的大小
            totalBlocks = statFs.getBlockCount();// 获取分区的个数
            availableBlocks = statFs.getAvailableBlocks();// 获取可用分区的个数
        }

        // 利用formatFileSize函数把字节转换为用户等看懂的大小数值单位
        String totalText = Formatter.formatFileSize(getBaseContext(), blockSize * totalBlocks);
        // String availableText = Formatter.formatFileSize(getBaseContext(), blockSize * availableBlocks);
        String userText = Formatter.formatFileSize(getBaseContext(), (blockSize * totalBlocks) - (blockSize * availableBlocks));
        textMemory.setText("已用" + userText + "/总容量" + totalText);
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        isVisible = false;
        textCompleted = null;
        textUncompleted = null;
        viewPagerDownload = null;
        setContentView(R.layout.activity_null);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == 1) {
            finish();
        }
    }
}
