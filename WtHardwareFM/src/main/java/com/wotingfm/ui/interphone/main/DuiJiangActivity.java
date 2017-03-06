package com.wotingfm.ui.interphone.main;

import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;

import com.umeng.analytics.MobclickAgent;
import com.wotingfm.R;
import com.wotingfm.util.SequenceUUID;
import com.wotingfm.util.ToastUtils;


public class DuiJiangActivity extends AppCompatActivity {
    private static DuiJiangActivity context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_other);
        context = this;

        setType();
        DuiJiangActivity.open(new DuiJiangFragment());
    }

    // 适配顶栏样式
    private void setType() {
        String a = android.os.Build.VERSION.RELEASE;
        Log.e("系统版本号", a + "");
        Log.e("系统版本号截取", a.substring(0, a.indexOf(".")) + "");
        boolean v = false;
        if (Integer.parseInt(a.substring(0, a.indexOf("."))) >= 5) {
            v = true;
        }
        View tv_main = findViewById(R.id.tv_main);
        if (v) {
            tv_main.setVisibility(View.VISIBLE);
        } else {
            tv_main.setVisibility(View.GONE);
        }
    }

    public static void open(Fragment frg) {
        context.getSupportFragmentManager().beginTransaction()
                .add(R.id.fragment_content, frg)
                .addToBackStack(SequenceUUID.getUUID())
                .commit();
    }

    public static void close() {
        context.getSupportFragmentManager().popBackStack();
    }

    public static void hideShow(Fragment from, Fragment to) {
        context.getSupportFragmentManager().beginTransaction().
                hide(from).show(to).commit();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    /*
     * 手机实体返回按键的处理,硬件不加入
	 */

    // 手机实体返回按键的处理 与 onBackPress 同理
    long waitTime = 2000;
    long touchTime = 0;

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (event.getAction() == KeyEvent.ACTION_DOWN && KeyEvent.KEYCODE_BACK == keyCode) {
//           int num=context.getSupportFragmentManager().getBackStackEntryCount();
//            Log.e("此时返回栈中个数====",num+"");
            if (context.getSupportFragmentManager().getBackStackEntryCount() <= 1) {
                long currentTime = System.currentTimeMillis();
                if ((currentTime - touchTime) >= waitTime) {
                    ToastUtils.show_always(this, "再按一次退出");
                    touchTime = currentTime;
                } else {
//                MainActivity.onStop();
                    MobclickAgent.onKillProcess(this);
                    finish();
                    android.os.Process.killProcess(android.os.Process.myPid());
                }
            }
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
