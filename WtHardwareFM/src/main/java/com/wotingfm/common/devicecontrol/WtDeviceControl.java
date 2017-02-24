package com.wotingfm.common.devicecontrol;


import android.app.Dialog;
import android.content.Context;
import android.graphics.drawable.AnimationDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;

import com.wotingfm.R;
import com.wotingfm.ui.interphone.chat.fragment.ChatFragment;
import com.wotingfm.ui.music.video.VoiceRecognizer;
import com.wotingfm.util.ToastUtils;

/**
 * 控制接口的实现类
 * 作者：xinlong on 2016/8/23 21:18
 * 邮箱：645700751@qq.com
 */
public class WtDeviceControl {
    private final Context context;
    private VoiceRecognizer mVoiceRecognizer;
    private Dialog voiceDialog;
    private AnimationDrawable draw_img;
    private long Vibrate = 100;

    /**
     * 构造器
     */
    public WtDeviceControl(Context context) {
        this.context = context;
//        if (mVoiceRecognizer == null) {
//            mVoiceRecognizer = new VoiceRecognizer(context);
//            Dialog(context);
//        }
    }

    /**
     * 暂停
     */
    public void pushCenter() {

//        try {
//            PlayerFragment.enterCenter();
//        } catch (Exception e) {
//            e.printStackTrace();
//            VibratorUtils.Vibrate(context, Vibrate);
//        }

    }

    /**
     * 点击上一首按钮
     */
    public void pushUpButton() {
//        try {
//            PlayerFragment.playLast();
//        } catch (Exception e) {
//            e.printStackTrace();
//            VibratorUtils.Vibrate(context, Vibrate);
//        }
    }

    /**
     * 点击下一首按钮
     */
    public void pushDownButton() {
//        try {
//            PlayerFragment.playNext();
//        } catch (Exception e) {
//            e.printStackTrace();
//            VibratorUtils.Vibrate(context, Vibrate);
//        }
    }

    /**
     * 设置当前声音
     */
    public void setVolumn() {
    }

    /**
     * 语音指令-开始
     */
    public void pushVoiceStart() {
   /*     if (draw_img.isRunning()) {
        } else {
            draw_img.start();
        }
        voiceDialog.show();
        mVoiceRecognizer.startListen();*/
    }

    /**
     * 语音指令-结束
     */
    public void releaseVoiceStop() {
    /*    if (draw_img.isRunning()) {
            draw_img.stop();
        }
        voiceDialog.dismiss();
        mVoiceRecognizer.stopListen();*/
    }

    /**
     * 按下语音通话
     */
    public void pushPTT() {

        ToastUtils.show_always(context,"按下了对讲按钮");
        if(ChatFragment.isVisible!=true){

        }else{
        try {
            ChatFragment.press();
        } catch (Exception e) {
            e.printStackTrace();
           // VibratorUtils.Vibrate(context, Vibrate);
        }
        }
    }

    /**
     * 抬起语音通话
     */
    public void releasePTT() {
        ToastUtils.show_always(context,"松开了对讲按钮");
        if(ChatFragment.isVisible!=true){

        }else{
            try {
                ChatFragment.jack();
            } catch (Exception e) {
                e.printStackTrace();
                // VibratorUtils.Vibrate(context, Vibrate);
            }
        }
    }

    private void Dialog(Context context) {
        final View dialog = LayoutInflater.from(context).inflate(R.layout.dialog_voice_ripple, null);
        ImageView img = (ImageView) dialog.findViewById(R.id.imageView);
        img.setBackgroundResource(R.drawable.talk_show);
        draw_img = (AnimationDrawable) img.getBackground();
        voiceDialog = new Dialog(context, R.style.MyDialog);
        voiceDialog.setContentView(dialog);
        voiceDialog.setCanceledOnTouchOutside(true);
        voiceDialog.getWindow().setBackgroundDrawableResource(R.color.dialog);

    }
}
