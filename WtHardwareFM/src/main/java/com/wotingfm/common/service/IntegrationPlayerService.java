package com.wotingfm.common.service;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Notification;
import android.app.Service;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Binder;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.iflytek.cloud.SpeechConstant;
import com.iflytek.cloud.SpeechError;
import com.iflytek.cloud.SpeechSynthesizer;
import com.iflytek.cloud.SynthesizerListener;
import com.kingsoft.media.httpcache.KSYProxyService;
import com.kingsoft.media.httpcache.OnCacheStatusListener;
import com.wotingfm.common.config.GlobalConfig;
import com.wotingfm.common.constant.BroadcastConstants;
import com.wotingfm.ui.music.player.model.LanguageSearchInside;
import com.wotingfm.util.L;
import com.wotingfm.util.ResourceUtil;

import org.videolan.libvlc.EventHandler;
import org.videolan.libvlc.LibVLC;
import org.videolan.libvlc.LibVlcException;
import org.videolan.vlc.util.VLCInstance;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * 集成播放器服务
 * Created by Administrator on 2016/12/14.
 */
public class IntegrationPlayerService extends Service implements OnCacheStatusListener {
    private LibVLC mVlc;// VLC 播放器
    private SpeechSynthesizer mTts;// 讯飞播放 TTS

    private KSYProxyService proxyService;// 金山云缓存

    private MyBinder mBinder = new MyBinder();
    private AssistServiceConnection mConnection;

    private List<LanguageSearchInside> playList = new ArrayList<>();

    private boolean isVlcPlaying;// VLC 播放器正在播放
    private boolean isTtsPlaying;// TTS 播放器正在播放
    private int count = 0;// 获取次数
    private int position = 0;// 当前播放节目在列表中的位置

    private String mediaType;// 播放类型
    private String httpUrl;// 网络播放地址
    private String localUrl;// 本地播放地址
    private long secondProgress;

    private Intent updateTimeIntent;// 更新时间
    private Intent totalTimeIntent;// 总时间
    private Intent updatePlayView;// 更新播放界面

    private Handler mHandler = new Handler();

    // 更新时间
    private Runnable mUpdatePlayTimeRunnable = new Runnable() {
        @Override
        public void run() {
            if(updateTimeIntent != null) {
                updateTimeIntent.putExtra("SECOND_PROGRESS", secondProgress);
                updateTimeIntent.putExtra("CURRENT_TIME", getCurrentTime());
                sendBroadcast(updateTimeIntent);
                mHandler.postDelayed(this, 1000);
            }
        }
    };

    // 更新时间
    private Runnable mTotalTimeRunnable = new Runnable() {
        @Override
        public void run() {
            long totalTime = getTotalTime();
            if(totalTimeIntent != null) {
                totalTimeIntent.putExtra("TOTAL_TIME", totalTime);
                totalTimeIntent.putExtra("MEDIA_TYPE", mediaType);
                L.v("TAG", "mediaType -- > > " + mediaType);
                sendBroadcast(totalTimeIntent);
            }
            if(totalTime == 0 && count < 10) {
                mHandler.postDelayed(this, 2000);
                count++;
            } else {
                count = 0;
                mHandler.removeCallbacks(this);
            }
        }
    };

    @Override
    public void onCreate() {
        setForeground();
        initCache();
        initVlc();
        initTts();
        initIntent();
    }

    // 初始化播放缓存
    private void initCache() {
        if(proxyService == null) proxyService = new KSYProxyService(this);
        File file = new File(ResourceUtil.getLocalUrlForKsy());// 设置缓存目录
        if (!file.exists()) if (!file.mkdir()) L.v("TAG", "KSYProxy MkDir Error");
        proxyService.setCacheRoot(file);
        proxyService.setMaxCacheSize(500 * 1024 * 1024);// 缓存大小 500MB
        proxyService.startServer();
    }

    // 初始化广播需要的 Intent
    private void initIntent() {
        if(updateTimeIntent == null) {// 当前播放时间
            updateTimeIntent = new Intent();
            updateTimeIntent.setAction(BroadcastConstants.UPDATE_PLAY_CURRENT_TIME);
        }
        if(totalTimeIntent == null) {// 总时间
            totalTimeIntent = new Intent();
            totalTimeIntent.setAction(BroadcastConstants.UPDATE_PLAY_TOTAL_TIME);
        }
        if(updatePlayView == null) {// 更新播放界面
            updatePlayView = new Intent();
            updatePlayView.setAction(BroadcastConstants.UPDATE_PLAY_VIEW);
        }
    }

    // 初始化 VLC 播放器
    private void initVlc() {
        if(mVlc != null) return ;
        try {
            mVlc = VLCInstance.getLibVlcInstance();
        } catch (LibVlcException e) {
            e.printStackTrace();
        }
        EventHandler em = EventHandler.getInstance();
        em.addHandler(mVlcHandler);
    }

    // 初始化讯飞播放 TTS
    private void initTts() {
        if(mTts == null){
            mTts = SpeechSynthesizer.createSynthesizer(this, null);
            setParamTTS();
        }
    }

    // 开始播放
    public void startPlay(int index) {
        if(index < 0 || index >= playList.size()) return ;
        position = index;
        secondProgress = 0;
        LanguageSearchInside playObject = playList.get(position);
        if(initPlayObject(playObject)) {
            switch (mediaType) {
                case "TTS":
                    playTts(httpUrl);
                    break;
                default:
                    playAudio(httpUrl, localUrl);
                    break;
            }
            mHandler.postDelayed(mUpdatePlayTimeRunnable, 1000);

            L.w("TAG", "position -- > > " + position);

            updatePlayView.putExtra("PLAY_POSITION", position);
            sendBroadcast(updatePlayView);
        }
    }

    // 暂停播放
    public void pausePlay() {
        if(isVlcPlaying && mVlc.isPlaying()) mVlc.pause();
        else if(isTtsPlaying && mTts.isSpeaking()) mTts.stopSpeaking();
        if(mediaType.equals("AUDIO")) {
            if(mUpdatePlayTimeRunnable != null) mHandler.removeCallbacks(mUpdatePlayTimeRunnable);
        }
    }

    // 继续播放
    public void continuePlay() {
        if(!isVlcPlaying && !isTtsPlaying) {
            startPlay(0);
        } else {
            if (isVlcPlaying) mVlc.play();
            else playTts(httpUrl);
            if (mediaType.equals("AUDIO")) {
                mHandler.postDelayed(mUpdatePlayTimeRunnable, 1000);
            }
        }
    }

    // 指定播放位置
    public void setPlayTime(long currentTime) {
        mVlc.setTime(currentTime);
    }

    // 是否正在播放
    public boolean isAudioPlaying() {
        return (isVlcPlaying && mVlc.isPlaying()) || (isTtsPlaying && mTts.isSpeaking());
    }

    // 初始化播放对象
    private boolean initPlayObject(LanguageSearchInside playObject) {
        if(playObject == null) return false;
        GlobalConfig.playerObject = playList.get(position);
        mediaType = GlobalConfig.playerObject.getMediaType();
        httpUrl = GlobalConfig.playerObject.getContentPlay();
        localUrl = GlobalConfig.playerObject.getLocalurl();
        return mediaType != null && (httpUrl != null || localUrl != null);
    }

    // 播放节目
    private void playAudio(String contentPlay, String localUrl) {
        if(mTts != null && mTts.isSpeaking() && isTtsPlaying) stopTts();
        if(mVlc == null) initVlc();
        else if(mVlc.isPlaying() && isVlcPlaying) mVlc.stop();

        if(localUrl != null) {// 播放本地 URL
            mVlc.playMRL(localUrl);
        } else {
            if(mediaType.equals("AUDIO")) {
                if (!isCacheFinish(contentPlay)) {// 判断是否已经缓存过  没有则开始缓存
                    proxyService.registerCacheStatusListener(this, contentPlay);
                } else {
                    secondProgress = -1;
                }
                contentPlay = proxyService.getProxyUrl(contentPlay);
                mVlc.playMRL(contentPlay);
            } else {
                mVlc.playMRL(contentPlay);
            }
        }
        mHandler.postDelayed(mTotalTimeRunnable, 1000);

        isVlcPlaying = true;
        isTtsPlaying = false;
    }

    // 停止 VLC 播放
    private void stopVlc() {
        if(mVlc != null && isVlcPlaying) {
            if(mUpdatePlayTimeRunnable != null) mHandler.removeCallbacks(mUpdatePlayTimeRunnable);
            isVlcPlaying = false;
            mVlc.stop();
        }
    }

    // 播放 TTS
    private void playTts(String contentPlay) {
        if(mVlc != null && mVlc.isPlaying() && isVlcPlaying) stopVlc();
        if(mTts == null) initTts();
        mTts.startSpeaking(contentPlay, mTtsListener);
        mHandler.postDelayed(mTotalTimeRunnable, 1000);

        isTtsPlaying = true;
        isVlcPlaying = false;
    }

    // 停止播放 TTS
    private void stopTts() {
        if(mTts != null && isTtsPlaying) {
            if(mUpdatePlayTimeRunnable != null) mHandler.removeCallbacks(mUpdatePlayTimeRunnable);
            isTtsPlaying = false;
            mTts.stopSpeaking();
        }
    }

    // 获取当前播放时间
    private long getCurrentTime() {
        long currentTime = 0;
        switch (mediaType) {
            case "TTS":
            case "RADIO":
                currentTime = System.currentTimeMillis();
                break;
            case "AUDIO":
                currentTime = mVlc.getTime();
                break;
        }
        return currentTime;
    }

    // 获取时间总长度
    private long getTotalTime() {
        switch (mediaType) {
            case "TTS":
            case "RADIO":
                return -1;
            case "AUDIO":
                L.v("TAG", "totalTime -- > > " + mVlc.getLength());
                return mVlc.getLength();
        }
        return -1;
    }

    // 更新播放列表
    public void updatePlayList(List<LanguageSearchInside> list) {
        if(list != null) {
            if(playList != null) playList.clear();
            playList = list;
        }
        if(!isVlcPlaying && !isTtsPlaying) {// 第一次进入应用给 playerObject 赋值
            position = 0;
            GlobalConfig.playerObject = playList.get(position);
            updatePlayView.putExtra("PLAY_POSITION", position);
            sendBroadcast(updatePlayView);
        }
    }

    // 判断是否已经缓存完成
    private boolean isCacheFinish(String url) {
        HashMap<String, File> cacheMap = proxyService.getCachedFileList();
        File cacheFile = cacheMap.get(url);
        return cacheFile != null && cacheFile.length() > 0;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        hideNotification();
        recovery();
        return super.onUnbind(intent);
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    private Notification showNotification() {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this)
                .setAutoCancel(false)
                .setOngoing(true);
        Notification notification = builder.build();
        startService(new Intent(this, IntegrationPlayerService.class));
        startForeground(3, notification);
        return notification;
    }

    private void hideNotification() {
        stopForeground(true);
        stopSelf();
    }

    private void setForeground() {
        // sdk < 18 , 直接调用 startForeground 即可,不会在通知栏创建通知
        if (Build.VERSION.SDK_INT < 18) {
            showNotification();
            return;
        }

        if (null == mConnection) mConnection = new AssistServiceConnection();
        bindService(new Intent(this, AssistService.class), mConnection, Service.BIND_AUTO_CREATE);
    }

    @Override
    public void OnCacheStatus(String url, long sourceLength, int percentsAvailable) {
        L.i("TAG", "OnCacheStatus: percentsAvailable == " + percentsAvailable);
        secondProgress = getTotalTime() * percentsAvailable / 100;
    }

    public class MyBinder extends Binder {
        public IntegrationPlayerService getService() {
            return IntegrationPlayerService.this;
        }
    }

    private class AssistServiceConnection implements ServiceConnection {
        @Override
        public void onServiceDisconnected(ComponentName name) {
            Log.d("TAG", "MyService: onServiceDisconnected");
        }

        @Override
        public void onServiceConnected(ComponentName name, IBinder binder) {
            Log.d("TAG", "MyService: onServiceConnected");
            // sdk >= 18 的，会在通知栏显示 service 正在运行，这里不要让用户感知，所以这里的实现方式是利用 2 个同进程的 service，利用相同的 notificationID，
            // 2 个 service 分别 startForeground，然后只在 1 个 service 里 stopForeground，这样即可去掉通知栏的显示
            Service assistService = ((AssistService.LocalBinder) binder).getService();
            startForeground(3, showNotification());
            assistService.startForeground(3, showNotification());
            assistService.stopForeground(true);
            unbindService(mConnection);
            mConnection = null;
        }
    }

    // VLC
    @SuppressLint("HandlerLeak")
    private Handler mVlcHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if (msg == null || msg.getData() == null) return ;
            switch (msg.getData().getInt("event")) {
                case EventHandler.MediaPlayerEncounteredError:// 播放出现错误播下一首
                    break;
                case EventHandler.MediaPlayerEndReached:// 播放完成播下一首
                    if(mUpdatePlayTimeRunnable != null) {
                        mHandler.removeCallbacks(mUpdatePlayTimeRunnable);
                    }
                    position++;
                    if(position > playList.size() - 1) {
                        position = 0;
                    }
                    startPlay(position);

                    Log.e("TAG", "========= MediaPlayerEndReached =========");
                    break;
            }
        }
    };

    // 配置讯飞参数
    private void setParamTTS() {
        mTts.setParameter(SpeechConstant.PARAMS, null);// 清空参数
        mTts.setParameter(SpeechConstant.ENGINE_TYPE, SpeechConstant.TYPE_CLOUD);// 根据合成引擎设置相应参数
        mTts.setParameter(SpeechConstant.VOICE_NAME, "vixf");// 设置在线合成发音人
        mTts.setParameter(SpeechConstant.VOLUME, "50");// 设置合成音量
        mTts.setParameter(SpeechConstant.STREAM_TYPE, "3");// 设置播放器音频流类型
        mTts.setParameter(SpeechConstant.AUDIO_FORMAT, "pcm");
    }

    // TTS 播放监听
    private SynthesizerListener mTtsListener = new SynthesizerListener() {
        @Override
        public void onCompleted(SpeechError arg0) {
//			PlayerFragment.playNext();
        }

        @Override
        public void onBufferProgress(int arg0, int arg1, int arg2, String arg3) {
        }

        @Override
        public void onEvent(int arg0, int arg1, int arg2, Bundle arg3) {
        }

        @Override
        public void onSpeakBegin() {
        }

        @Override
        public void onSpeakPaused() {
        }

        @Override
        public void onSpeakProgress(int arg0, int arg1, int arg2) {
        }

        @Override
        public void onSpeakResumed() {
        }
    };

    // 回收资源
    private void recovery() {
        if(mUpdatePlayTimeRunnable != null) {
            mHandler.removeCallbacks(mUpdatePlayTimeRunnable);
            mUpdatePlayTimeRunnable = null;
        }
        if(mTotalTimeRunnable != null) {
            mHandler.removeCallbacks(mTotalTimeRunnable);
            mTotalTimeRunnable = null;
        }
        if(playList != null) {
            playList.clear();
            playList = null;
        }
        if(mVlc != null) {
            mVlc.stop();
            mVlc.destroy();
            mVlc = null;
        }
        if(mTts != null) {
            mTts.stopSpeaking();
            mTts.destroy();
            mTts = null;
        }

        mBinder = null;
        mConnection = null;
        mediaType = null;
        httpUrl = null;
        localUrl = null;
        updateTimeIntent = null;
        totalTimeIntent = null;

        Log.d("TAG", "recovery resources");
    }
}
