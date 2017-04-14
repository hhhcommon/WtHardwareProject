package com.wotingfm.common.service;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;

import com.wotingfm.ui.music.download.service.DownloadClient;

/**
 * 应用核心服务
 * Created by Administrator on 2017/4/13.
 */
public class CoreService extends Service {
    private Context context;

    private LocationInfo locationInfo;// 定位信息
    private SocketClient socketClient;// Socket
    private DownloadClient downloadClient;// 下载
    private SubclassControl subclassControl;// 单对单控制
    private VoiceStreamRecord record;// 录音
    private VoiceStreamPlayer playerRecord;// 播放录音
    private NotificationClient notificationClient;// 通知消息

    @Override
    public void onCreate() {
        super.onCreate();
        context = getApplicationContext();

        initLocation();
        initSocket();
        initDownLoad();
        initSubclass();
        initRecord();
        initPlayRecord();
        initNotify();
    }

    // 开启定位
    private void initLocation() {
        locationInfo = new LocationInfo(context);
    }

    // SocketClient
    private void initSocket() {
        socketClient = new SocketClient(context);
        startForeground(4, socketClient.showNotification());
    }

    // 下载
    private void initDownLoad() {
        downloadClient = new DownloadClient(this);
    }

    // 单对单对讲控制
    private void initSubclass() {
        subclassControl = new SubclassControl(context);
    }

    // 录音
    private void initRecord() {
        record = new VoiceStreamRecord();
    }

    // 播放录音
    private void initPlayRecord() {
        playerRecord = new VoiceStreamPlayer(context);
    }

    // 初始化接收通知消息功能
    private void initNotify() {
        notificationClient = new NotificationClient(context);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        locationInfo.stopLocation();
        socketClient.onDestroy();
        downloadClient.unregister();
        subclassControl.unregister();
        record.stopRecord();
        playerRecord.onDestroy();
        notificationClient.unregister();
    }
}
