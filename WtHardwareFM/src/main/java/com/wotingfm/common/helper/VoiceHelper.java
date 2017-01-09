package com.wotingfm.common.helper;

import android.content.Context;
import android.media.AudioManager;
import com.wotingfm.common.application.BSApplication;

/**
 * 音量管理
 * @author 辛龙
 */
public class VoiceHelper {

    public static  int stepVolume;
    public static AudioManager audioMgr;
    public static VoiceHelper instance;
    public static int curVolume;

    private VoiceHelper() {
        audioMgr = (AudioManager) BSApplication.instance.getSystemService(Context.AUDIO_SERVICE);
        // 获取最大音乐音量
        int maxVolume = audioMgr.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
        // 每次调整的音量大概为最大音量的1/100
        stepVolume = maxVolume / 100;
    }

    /**
     * 单例模式
     */
    public static VoiceHelper getInstance() {
        if (instance == null) {
            instance = new VoiceHelper();

        }
        return instance;
    }

    //调节音量
    public static void initVoice() {
        curVolume = audioMgr.getStreamVolume(AudioManager.STREAM_MUSIC);
        audioMgr.setStreamVolume(AudioManager.STREAM_MUSIC, stepVolume, AudioManager.FLAG_PLAY_SOUND);
    }

    //恢复音量
    public static void recoverVoice() {
        audioMgr.setStreamVolume(AudioManager.STREAM_MUSIC, curVolume, AudioManager.FLAG_PLAY_SOUND);
    }

}
