package com.wotingfm.activity.music.comment.model;

import java.io.Serializable;

public class ChatInfo implements Serializable {

	public int iconFromResId;
	public String iconFromUrl;
	public String content;
	public String time;
	public int fromOrTo;// 0 是收到的消息
	@Override
	public String toString() {
		return "ChatInfoEntity [iconFromResId=" + iconFromResId
				+ ", iconFromUrl=" + iconFromUrl + ", content=" + content
				+ ", time=" + time + ", fromOrTo=" + fromOrTo + "]";
	}
}
