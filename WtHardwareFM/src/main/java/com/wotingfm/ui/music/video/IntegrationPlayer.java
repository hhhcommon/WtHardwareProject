package com.wotingfm.ui.music.video;

import android.content.Context;
import android.os.Handler;
import android.util.Log;

import com.kingsoft.media.httpcache.KSYProxyService;
import com.kingsoft.media.httpcache.OnErrorListener;
import com.wotingfm.common.application.BSApplication;
import com.wotingfm.common.config.GlobalConfig;

import java.io.File;

/**
 * 集成播放器
 * 作者：xinlong on 2016/11/29 15:54
 * 邮箱：645700751@qq.com
 */
public class IntegrationPlayer implements OnErrorListener {
    private Context mContext;

    private static IntegrationPlayer mPlayer;
    private KSYProxyService mProxy;// 缓存对象

    private VlcPlayer mVlcPlayer;// VLC 播放器
    private TtsPlayer mTtsPlayer;// TTS 播放器

    private boolean mIsVlcPlaying;// VLC 播放器正在播放
    private boolean mIsTtsPlaying;// TTS 播放器正在播放

    private String mediaType;// 播放的节目类型
    private String httpUrl;
    private String localUrl;

    private Handler mHandler = new Handler();

    private Runnable mVlcPlayRunnable = new Runnable() {
        @Override
        public void run() {
            vlcPlay(httpUrl, localUrl);
        }
    };

    private Runnable mTtsPlayRunnable = new Runnable() {
        @Override
        public void run() {
            ttsPlay(httpUrl, localUrl);
        }
    };

    // 实现单例 在这里执行初始化操作
    private IntegrationPlayer(Context context) {
        mContext = context;
        initCache();
        initVlcPlayer();
        initTtsPlayer(mContext);
    }

    public static IntegrationPlayer getInstance(Context context) {
        if(mPlayer == null) {
            synchronized (IntegrationPlayer.class) {
                if(mPlayer == null) {
                    mPlayer = new IntegrationPlayer(context);
                }
            }
        }
        return mPlayer;
    }

    // 初始化 VLC 播放器
    private void initVlcPlayer() {
        if(mVlcPlayer == null) mVlcPlayer = VlcPlayer.getInstance();
    }

    // 初始化 TTS 播放器
    private void initTtsPlayer(Context context) {
        if(context == null) {
            Log.w("TAG", "Init Error: Context is null.");
            return ;
        }
        if(mTtsPlayer == null) mTtsPlayer = TtsPlayer.getInstance(context);
    }

    // 初始化播放缓存
    private void initCache() {
        mProxy = BSApplication.getKSYProxy();
        mProxy.registerErrorListener(this);
        File file = new File(GlobalConfig.ksyPlayCache);// 设置缓存目录
        if (!file.exists()) if(!file.mkdir()) Log.v("TAG", "KSYProxy MkDir Error");
        mProxy.setCacheRoot(file);
        mProxy.setMaxCacheSize(500 * 1024 * 1024);// 缓存大小 500MB
        mProxy.startServer();
    }

    /**
     * 播放
     */
    public void startPlay(String mediaType, String httpUrl, String localUrl) {
        this.mediaType = mediaType;
        this.httpUrl = httpUrl;
        this.localUrl = localUrl;

        // 播放类型为空无法判断使用哪个播放器
        if(mediaType == null) return ;
        Log.i("TAG", "startPlay: mediaType -- > " + mediaType);

        // 播放地址为空
        if(isEmpty(this.httpUrl) && isEmpty(this.localUrl)) {
            Log.e("TAG", "Player Error: this url is null!!!");
            return ;
        }

        // 根据 mediaType 自动选择播放器
        switch (mediaType) {
            case "TTS":
                if(mVlcPlayer != null && mVlcPlayer.isPlaying() && mIsVlcPlaying) mVlcPlayer.stop();
                if(mVlcPlayRunnable != null) mHandler.removeCallbacks(mVlcPlayRunnable);
                if(mTtsPlayer == null) initTtsPlayer(mContext);
                mHandler.post(mTtsPlayRunnable);

                mIsTtsPlaying = true;
                mIsVlcPlaying = false;
                break;
            default:
                if(mTtsPlayer != null && mTtsPlayer.isPlaying() && mIsTtsPlaying) mTtsPlayer.stop();
                if(mTtsPlayRunnable != null) mHandler.removeCallbacks(mTtsPlayRunnable);
                if(mVlcPlayer == null) initVlcPlayer();

                mHandler.post(mVlcPlayRunnable);

                mIsVlcPlaying = true;
                mIsTtsPlaying = false;
                break;
        }
    }

    // 使用 VLC 播放器播放
    private void vlcPlay(String httpUrl, String localUrl) {
        if(isEmpty(localUrl)) {
            if(GlobalConfig.playerObject != null && GlobalConfig.playerObject.getMediaType() != null) {
                if(GlobalConfig.playerObject.getMediaType().equals("AUDIO")) {
                    httpUrl = mProxy.getProxyUrl(httpUrl);
                }
            }
            if(mVlcPlayer.isPlaying()) mVlcPlayer.stop();
            mVlcPlayer.play(httpUrl);
        } else {
            mVlcPlayer.play(localUrl);
        }
        // 从上次停止处开始播放
        if(GlobalConfig.playerObject != null && GlobalConfig.playerObject.getMediaType().equals("AUDIO")) {
            String string = GlobalConfig.playerObject.getPlayerInTime();
            if(string != null && !string.equals("")) {
                long playInTime = Long.valueOf(string);
                mVlcPlayer.setTime(playInTime);
            }
        }
    }

    // 使用 TTS 播放器播放
    private void ttsPlay(String httpUrl, String localUrl) {
        if(isEmpty(localUrl)) {
            mTtsPlayer.play(httpUrl);
        } else {
            mTtsPlayer.play(localUrl);
        }
    }

    /**
     * 暂停
     */
    public void pausePlay() {
        if(mIsVlcPlaying && mVlcPlayer.isPlaying()) mVlcPlayer.pause();
        else if(mIsTtsPlaying && mTtsPlayer.isPlaying()) mTtsPlayer.pause();
    }

    /**
     * 继续播放
     */
    public void continuePlay() {
        if(mIsVlcPlaying) mVlcPlayer.continuePlay();
        else if(mIsTtsPlaying) mTtsPlayer.continuePlay();
    }

    /**
     * TTS 停止播放
     */
    public void stopPlay() {
        if(mIsTtsPlaying && mTtsPlayer.isPlaying()) {
            mTtsPlayer.stop();
        }
    }

    /**
     * 回收播放器资源
     */
    public void destroyPlayer() {
        if(mVlcPlayer != null) mVlcPlayer.destory();
        if(mTtsPlayer != null) mTtsPlayer.destory();
        if(mTtsPlayRunnable != null) mHandler.removeCallbacks(mTtsPlayRunnable);
        if(mVlcPlayRunnable != null) mHandler.removeCallbacks(mVlcPlayRunnable);
        if(mProxy != null) {
            mProxy.unregisterErrorListener(this);
            mProxy.shutDownServer();
        }
    }

    /**
     * 获取此时播放时间
     */
    public long getCurrentTime() {
        if(mediaType != null && mediaType.equals("TTS")) {
            return mTtsPlayer.getTime();
        } else {
            return mVlcPlayer.getTime();
        }
    }

    /**
     * 设置播放进度
     * @param time 此时的播放进度
     */
    public void setCurrentTime(long time) {
        if(mediaType != null && mediaType.equals("TTS")) {
            mTtsPlayer.setTime(time);
        } else {
            mVlcPlayer.setTime(time);
        }
    }

    /**
     * 获取总时长
     */
    public long getTotalTime() {
        if(mediaType != null && mediaType.equals("TTS")) {
            return mTtsPlayer.getTotalTime();
        } else {
            return mVlcPlayer.getTotalTime();
        }
    }

    /**
     * 播放器是否在播放
     */
    public boolean isPlaying() {
        return (mVlcPlayer.isPlaying() && mIsVlcPlaying) || (mTtsPlayer.isPlaying() && mIsTtsPlaying);
    }

    @Override
    public void OnError(int i) {
        Log.v("TAG", "PlayUrl -- > " + httpUrl);
        Log.v("TAG", "KSYProxyService Error -- > " + i);
    }

    // 判断播放地址是否为空
    private boolean isEmpty(String url) {
        return url == null || url.trim().equals("") || url.equals("null");
    }
}
