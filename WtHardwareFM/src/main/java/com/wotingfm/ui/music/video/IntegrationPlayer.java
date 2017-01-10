package com.wotingfm.ui.music.video;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.util.Log;

import com.wotingfm.common.service.IntegrationPlayerService;
import com.wotingfm.ui.music.player.model.LanguageSearchInside;

import java.util.List;

/**
 * 集成播放器
 * 作者：xinlong on 2016/11/29 15:54
 * 邮箱：645700751@qq.com
 */
public class IntegrationPlayer {
    private static IntegrationPlayer mPlayer;

    private IntegrationPlayerService mService;
    private ServiceConnection mAudioServiceConnection;

    private boolean mBound;// 绑定服务

    public static IntegrationPlayer getInstance() {
        if(mPlayer == null) {
            synchronized (IntegrationPlayer.class) {
                if(mPlayer == null) mPlayer = new IntegrationPlayer();
            }
        }
        return mPlayer;
    }

    /**
     * 绑定服务
     */
    public void bindService(Context context) {
        if(context == null) return ;
        context = context.getApplicationContext();
        if(!mBound) {
            Intent intent = new Intent(context, IntegrationPlayerService.class);
            mAudioServiceConnection = new ServiceConnection() {
                @Override
                public void onServiceConnected(ComponentName name, IBinder service) {
                    IntegrationPlayerService.MyBinder mBinder = (IntegrationPlayerService.MyBinder) service;
                    mService = mBinder.getService();
                    Log.v("TAG", "Service Bind success");
                }

                @Override
                public void onServiceDisconnected(ComponentName name) {
                    mService = null;
                    mBound = false;
                }
            };
            mBound = context.bindService(intent, mAudioServiceConnection, Context.BIND_AUTO_CREATE);
        }
    }

    /**
     * 解除绑定服务
     */
    public void unbindService(Context context) {
        if(context == null) return ;
        context = context.getApplicationContext();
        if(mBound) {
            mBound = false;
            context.unbindService(mAudioServiceConnection);
            mService = null;
            mAudioServiceConnection = null;
        }
    }

    /**
     * 更新播放列表
     */
    public void updatePlayList(List<LanguageSearchInside> list) {
        if(mBound && mService != null) {
            mService.updatePlayList(list);
        }
    }

    /**
     * 播放
     */
    public void startPlay(int index) {
        if(mBound && mService != null) {
            mService.startPlay(index);
        }
    }

    /**
     * 暂停播放
     */
    public void pausePlay() {
        if(mBound && mService != null) {
            mService.pausePlay();
        }
    }

    /**
     * 继续播放
     */
    public void continuePlay() {
        if(mBound && mService != null) {
            mService.continuePlay();
        }
    }

    /**
     * 播放状态 暂停 OR 正在播放
     */
    public boolean playStatus() {
        return mService.isAudioPlaying();
    }

    /**
     * 从指定时间开始播放
     */
    public void setPlayCurrentTime(long currentTime) {
        mService.setPlayTime(currentTime);
    }

    /**
     * 更新下载列表
     */
    public void updateLocalList() {
        mService.updateLocalList();
    }
}
