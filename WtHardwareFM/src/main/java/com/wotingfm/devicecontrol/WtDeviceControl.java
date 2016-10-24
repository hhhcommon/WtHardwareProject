package com.wotingfm.devicecontrol;


import android.app.Dialog;
import android.content.Context;
import android.graphics.drawable.AnimationDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;

import com.wotingfm.R;
import com.wotingfm.activity.im.interphone.chat.fragment.ChatFragment;
import com.wotingfm.activity.music.player.fragment.PlayerFragment;
import com.wotingfm.activity.music.video.VoiceRecognizer;

/**
 * 控制接口的实现类
 * 作者：xinlong on 2016/8/23 21:18
 * 邮箱：645700751@qq.com
 */
public class WtDeviceControl {
	private VoiceRecognizer mVoiceRecognizer;
	private Dialog voiceDialog;
	private AnimationDrawable draw_img;

	/**
	 * 构造器
     */
	public WtDeviceControl(Context context) {
		if(mVoiceRecognizer==null){
			mVoiceRecognizer=new VoiceRecognizer(context);
			Dialog(context);
		}
	}

	/**
	 * 点击中间按钮
	 */
	public void pushCenter() {
		PlayerFragment.enterCenter();
	}

	/**
	 * 点击上一首按钮
	 */
	public void pushUpButton() {
		PlayerFragment.playLast();
	}

	/**
	 * 点击下一首按钮
	 */
	public void pushDownButton() {
		PlayerFragment.playNext();
	}

	/**
	 * 设置当前声音
	 */
	public void setVolumn() {}

	/**
	 * 语音指令-开始
	 */
	public void pushVoiceStart() {
		if (draw_img.isRunning()) {
		} else {
			draw_img.start();
		}
		voiceDialog.show();
		mVoiceRecognizer.startListen();
	}

	/**
	 * 语音指令-结束
	 */
	public void releaseVoiceStop() {
		if (draw_img.isRunning()) {
			draw_img.stop();
		}
		voiceDialog.dismiss();
		mVoiceRecognizer.stopListen();
	}

	/**
	 * 按下语音通话
	 */
	public void pushPTT() {
		ChatFragment.press();
	}

	/**
	 * 抬起语音通话
	 */
	public void releasePTT() {
		ChatFragment.jack();
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
