package com.wotingfm.ui.mine.main;

import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

import com.wotingfm.R;
import com.wotingfm.ui.baseactivity.BaseFragmentActivity;
import com.wotingfm.util.SequenceUUID;


public class MineActivity extends BaseFragmentActivity {
    private static MineActivity context;
    private static View textMain;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_other);
        context=this;
        textMain = findViewById(R.id.tv_main);
        MineActivity.open(new MineFragment());
    }

    /**
     * 打开新的 Fragment
     */
    public static void open(Fragment frg) {
        context.getSupportFragmentManager().beginTransaction()
                .add(R.id.fragment_content, frg)
                .addToBackStack(SequenceUUID.getUUID())
                .commit();
        if (context.getSupportFragmentManager().getBackStackEntryCount() > 0) {
            textMain.setVisibility(View.VISIBLE);
        }

        // 隐藏键盘
        InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(textMain.getWindowToken(), 0);
    }

    /**
     * 关闭已经打开的 Fragment
     */
    public static void close() {
        context.getSupportFragmentManager().popBackStackImmediate();
        if (context.getSupportFragmentManager().getBackStackEntryCount() == 1) {
            textMain.setVisibility(View.GONE);
        }

        // 隐藏键盘
        InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(textMain.getWindowToken(), 0);
    }

    // 手机实体返回按键的处理,硬件不加入
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (event.getAction() == KeyEvent.ACTION_DOWN && KeyEvent.KEYCODE_BACK == keyCode) {
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    // 设置android app 的字体大小不受系统字体大小改变的影响
    @Override
    public Resources getResources() {
        Resources res = super.getResources();
        Configuration config = new Configuration();
        config.setToDefaults();
        res.updateConfiguration(config, res.getDisplayMetrics());
        return res;
    }

}
