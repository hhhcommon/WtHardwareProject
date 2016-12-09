package com.wotingfm.util;


import com.wotingfm.R;
import com.wotingfm.activity.music.player.model.sharemodel;

import java.util.ArrayList;
import java.util.List;

public class PlayermoreUtil {

	private static List<sharemodel> list=new ArrayList<sharemodel>();

	public static List<sharemodel> getPlayMoreList(String type) {

		if(type!=null&&type.equals("AUDIO")){
			if(list.size()!=0){
				list.clear();
			}
			String[] textlist={"定时关闭","专辑","播放历史","我喜欢的","本地音频","预定节目单","实时路况"};
			int[] imglist={R.mipmap.img_play_more_dingshi,R.mipmap.img_play_more_lishi,R.mipmap.img_play_more_lishi,R.mipmap.img_play_more_like,R.mipmap.wt_icon_lktts,R.mipmap.img_play_more_jiemudan,R.mipmap.wt_icon_lktts};
			for(int i=0;i<textlist.length;i++){
				sharemodel sm=new sharemodel();
				sm.setShareImageUrl(imglist[i]);
				sm.setShareText(textlist[i]);
				list.add(sm);
			}
		}else{
			if(list.size()!=0){
				list.clear();
			}
		String[] textlist={"定时关闭","播放历史","我喜欢的","预定节目单","实时路况"};
		int[] imglist={R.mipmap.img_play_more_dingshi,R.mipmap.img_play_more_lishi,R.mipmap.img_play_more_like,R.mipmap.img_play_more_jiemudan,R.mipmap.wt_icon_lktts};
		for(int i=0;i<textlist.length;i++){
		 sharemodel sm=new sharemodel();
		 sm.setShareImageUrl(imglist[i]);
		 sm.setShareText(textlist[i]);
         list.add(sm);		 			
		}

		}
		return list;  
	}  

}
