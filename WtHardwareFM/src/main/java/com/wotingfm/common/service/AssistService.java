package com.wotingfm.common.service;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

/**
 * 空服务  用于取消播放器启动的服务在通知栏显示
 * Created by Administrator on 2016/12/14.
 */
public class AssistService extends Service {

    public class LocalBinder extends Binder {
        public AssistService getService() {
            return AssistService.this;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        Log.d("TAG", "AssistService: onBind()");
        return new LocalBinder();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d("TAG", "AssistService: onDestroy()");
    }
}
