package com.wotingfm.common.application;

import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;
import com.umeng.socialize.PlatformConfig;
import com.wotingfm.activity.music.common.service.DownloadService;
import com.wotingfm.common.config.GlobalConfig;
import com.wotingfm.common.config.SocketClientConfig;
import com.wotingfm.common.constant.KeyConstant;
import com.wotingfm.devicecontrol.WtDeviceControl;
import com.wotingfm.helper.CommonHelper;
import com.wotingfm.receiver.NetWorkChangeReceiver;
import com.wotingfm.service.FloatingWindowService;
import com.wotingfm.service.NotificationService;
import com.wotingfm.service.SocketService;
import com.wotingfm.service.SubclassService;
import com.wotingfm.service.VoiceStreamPlayerService;
import com.wotingfm.service.VoiceStreamRecordService;
import com.wotingfm.util.PhoneMessage;

import java.util.ArrayList;
import java.util.List;

/**
 * BSApplication
 * 作者：xinlong on 2016/8/23 21:18
 * 邮箱：645700751@qq.com
 */
public class BSApplication extends Application {
    private static Context instance;
    private static RequestQueue queues;
    private NetWorkChangeReceiver netWorkChangeReceiver = null;
    public  static android.content.SharedPreferences SharedPreferences;

    @Override
    public void onCreate() {
        super.onCreate();
        instance=this;
        SharedPreferences = this.getSharedPreferences("wotingfm", Context.MODE_PRIVATE);    //保存引导页查看状态
        InitThird();
        queues = Volley.newRequestQueue(this);
        PhoneMessage.getPhoneInfo(instance);//获取手机信息
        List<String> _l=new ArrayList<String>();//其中每个间隔要是0.5秒的倍数
        _l.add("INTE::500");   //第1次检测到未连接成功，隔0.5秒重连
        _l.add("INTE::500");  //第2次检测到未连接成功，隔0.5秒重连
        _l.add("INTE::1000");  //第3次检测到未连接成功，隔1秒重连
        _l.add("INTE::1000");  //第4次检测到未连接成功，隔1秒重连
        _l.add("INTE::2000"); //第5次检测到未连接成功，隔2秒重连
        _l.add("INTE::2000"); //第6次检测到未连接成功，隔2秒重连
        _l.add("INTE::5000"); //第7次检测到未连接成功，隔5秒重连
        _l.add("INTE::10000"); //第8次检测到未连接成功，隔10秒重连
        _l.add("INTE::60000"); //第9次检测到未连接成功，隔1分钟重连
        _l.add("GOTO::8");//之后，调到第9步处理
        SocketClientConfig scc = new SocketClientConfig();
        scc.setReConnectWays(_l);
        GlobalConfig.scc=scc;
        //socket服务
        startService(new Intent(this, SocketService.class));
        //录音服务
        startService(new Intent(this, VoiceStreamRecordService.class));
        //播放服务
        startService(new Intent(this, VoiceStreamPlayerService.class));
        //启动全局弹出框服务
        startService(new Intent(this, SubclassService.class));
        startService(new Intent(this, DownloadService.class));
        startService(new Intent(this, NotificationService.class));
        CommonHelper.checkNetworkStatus(instance);//网络设置获取
        this.registerNetWorkChangeReceiver(new NetWorkChangeReceiver(this));// 注册网络状态及返回键监听

        WtDeviceControl mControl = new WtDeviceControl(instance);
        GlobalConfig.device=mControl;
        startService(new Intent(this, FloatingWindowService.class));
    }

    private void InitThird() {
        PlatformConfig.setWeixin(KeyConstant.WEIXIN_KEY, KeyConstant.WEIXIN_SECRET);
        PlatformConfig.setQQZone(KeyConstant.QQ_KEY, KeyConstant.QQ_SECRET);
        PlatformConfig.setSinaWeibo(KeyConstant.WEIBO_KEY,KeyConstant.WEIBO_SECRET);
    }

    public static Context getAppContext(){
        return instance;
    }

    public static RequestQueue getHttpQueues() {
        return queues;
    }

    /*** 注册网络监听者 */
    private void registerNetWorkChangeReceiver(NetWorkChangeReceiver netWorkChangeReceiver) {
        this.netWorkChangeReceiver = netWorkChangeReceiver;
        IntentFilter filter = new IntentFilter();
        filter.addAction(NetWorkChangeReceiver.intentFilter);
        this.registerReceiver(netWorkChangeReceiver, filter);
    }

    /** 取消网络变化监听者 */
    private void unRegisterNetWorkChangeReceiver(NetWorkChangeReceiver netWorkChangeReceiver) {
        this.unregisterReceiver(netWorkChangeReceiver);
    }

    @Override
    public void onTerminate() {
        super.onTerminate();
        unRegisterNetWorkChangeReceiver(this.netWorkChangeReceiver);
    }

}
