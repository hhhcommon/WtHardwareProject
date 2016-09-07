package com.wotingfm.devicecontrol;


import android.content.Context;

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

	/**
	 * 构造器
     */
	public WtDeviceControl(Context context) {
		if(mVoiceRecognizer==null){
			mVoiceRecognizer=new VoiceRecognizer(context);
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
		mVoiceRecognizer.startListen();
	}

	/**
	 * 语音指令-结束
	 */
	public void releaseVoiceStop() {
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
}
