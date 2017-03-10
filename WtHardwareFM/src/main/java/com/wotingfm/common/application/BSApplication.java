package com.wotingfm.common.application;

import android.app.Application;
import android.content.Context;
import android.os.Handler;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;
import com.wotingfm.common.config.GlobalConfig;
import com.wotingfm.common.config.SocketClientConfig;
import com.wotingfm.common.helper.CollocationHelper;
import com.wotingfm.common.helper.CommonHelper;
import com.wotingfm.common.helper.CrashHandler;
import com.wotingfm.util.PhoneMessage;

import java.util.ArrayList;
import java.util.List;

/**
 * BSApplication
 * 作者：xinlong on 2016/8/23 21:18
 * 邮箱：645700751@qq.com
 */
public class BSApplication extends Application {
    public static Context instance;
    private static RequestQueue queues;
    public static android.content.SharedPreferences SharedPreferences;
    public static SocketClientConfig scc;         // Socket连接客户端配置信息

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
        CrashHandler handler = CrashHandler.getInstance();
        handler.init(getApplicationContext());

        SharedPreferences = this.getSharedPreferences("wotingfm", Context.MODE_PRIVATE);
        CollocationHelper.setCollocation();  //设置配置文件

        queues = Volley.newRequestQueue(this);    // 初始化网络请求
        PhoneMessage.getPhoneInfo(instance);      // 获取手机信息
        CommonHelper.checkNetworkStatus(instance);// 网络设置获取

        List<String> _l = new ArrayList<>();      // 其中每个间隔要是0.5秒的倍数
        _l.add("INTE::500");                      // 第1次检测到未连接成功，隔0.5秒重连
        _l.add("INTE::500");                      // 第2次检测到未连接成功，隔0.5秒重连
        _l.add("INTE::1000");                     // 第3次检测到未连接成功，隔1秒重连
        _l.add("INTE::1000");                     // 第4次检测到未连接成功，隔1秒重连
        _l.add("INTE::2000");                     // 第5次检测到未连接成功，隔2秒重连
        _l.add("INTE::2000");                     // 第6次检测到未连接成功，隔2秒重连
        _l.add("INTE::5000");                     // 第7次检测到未连接成功，隔5秒重连
        _l.add("INTE::10000");                    // 第8次检测到未连接成功，隔10秒重连
        _l.add("INTE::60000");                    // 第9次检测到未连接成功，隔1分钟重连
        _l.add("GOTO::8");                        // 之后，调到第9步处理
        scc = new SocketClientConfig();
        scc.setReConnectWays(_l);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                initStaticFaces();                 // 读取assets里的图片资源
            }
        }, 0);
    }

    private void initStaticFaces() {
        try {
            ArrayList<String> staticFacesList = new ArrayList<>();
            String[] faces = getAssets().list("face/png");
            //将Assets中的表情名称转为字符串一一添加进staticFacesList
            for (int i = 0; i < faces.length; i++) {
                staticFacesList.add(faces[i]);
            }
            //去掉删除图片
            staticFacesList.remove("emotion_del_normal.png");
            GlobalConfig.staticFacesList = staticFacesList;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static Context getAppContext() {
        return instance;
    }

    public static RequestQueue getHttpQueues() {
        return queues;
    }

    @Override
    public void onTerminate() {
        super.onTerminate();
    }
}
