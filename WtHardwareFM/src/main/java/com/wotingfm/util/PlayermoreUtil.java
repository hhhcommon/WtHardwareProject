package com.wotingfm.util;


import com.wotingfm.R;
import com.wotingfm.activity.music.player.model.sharemodel;

import java.util.ArrayList;
import java.util.List;

public class PlayermoreUtil {
	
	public static List<sharemodel> getPlayMoreList() {
		List<sharemodel> list=new ArrayList<sharemodel>();
		String[] textlist={"定时关闭","下载","播放历史","我喜欢的","本地音频","预定节目单","实时路况"};
		int[] imglist={R.mipmap.play_record_img,R.mipmap.wt_play_xiazai,R.mipmap.play_record_img,R.mipmap.wt_dianzan_nomal,R.mipmap.wt_icon_lktts,R.mipmap.wt_icon_lktts,R.mipmap.wt_icon_lktts};
		for(int i=0;i<textlist.length;i++){
		 sharemodel sm=new sharemodel();
		 sm.setShareImageUrl(imglist[i]);
		 sm.setShareText(textlist[i]);
         list.add(sm);		 			
		}
		return list;  
	}  

}
