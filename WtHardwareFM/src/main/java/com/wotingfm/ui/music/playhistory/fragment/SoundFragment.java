package com.wotingfm.ui.music.playhistory.fragment;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;

import com.wotingfm.R;
import com.wotingfm.ui.music.main.HomeActivity;
import com.wotingfm.ui.music.main.dao.SearchPlayerHistoryDao;
import com.wotingfm.ui.music.player.fragment.PlayerFragment;
import com.wotingfm.ui.music.player.model.PlayerHistory;
import com.wotingfm.ui.music.playhistory.activity.PlayHistoryActivity;
import com.wotingfm.ui.music.playhistory.adapter.PlayHistoryAdapter;
import com.wotingfm.common.constant.BroadcastConstants;
import com.wotingfm.common.constant.StringConstant;
import com.wotingfm.util.CommonUtils;
import com.wotingfm.util.L;
import com.wotingfm.util.ToastUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * 播放历史记录  声音界面
 * 
 * @author woting11
 */
public class SoundFragment extends Fragment{
    private Context context;
	private SearchPlayerHistoryDao dbDao;
    private PlayHistoryAdapter adapter;

    private View linearNull;			        // linear_null
    private View rootView;
	private ListView listView;

	private List<PlayerHistory> deleteList;		// 删除数据列表
	private List<PlayerHistory> checkList;		// 选中数据列表
    private ArrayList<PlayerHistory> playList;	// 播放历史声音列表

	public static boolean isData = false;		// 是否有数据 
	public static boolean isLoad;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		context = getActivity();
		initDao();
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		if(rootView == null){
			rootView = inflater.inflate(R.layout.fragment_playhistory_sound_layout, container, false);
			listView = (ListView) rootView.findViewById(R.id.list_view);
			linearNull = rootView.findViewById(R.id.linear_null);
			getData();
			isLoad = true;
		}
		return rootView;
	}
	
	// 初始化数据库命令执行对象
	private void initDao() {
        dbDao = new SearchPlayerHistoryDao(context);
	}
	
	/**
	 * 查询数据库  获取历史播放数据
	 */
	public void getData(){
		listView.setVisibility(View.GONE);
		isData = false;
        List<PlayerHistory> subList = dbDao.queryHistory();
		playList = null;
		if (subList != null && subList.size() > 0) {
			for (int i = 0; i < subList.size(); i++) {
				if (subList.get(i).getPlayerMediaType() != null && !subList.get(i).getPlayerMediaType().equals("")) {
					if (subList.get(i).getPlayerMediaType().equals("AUDIO")) {
						if (playList == null) {
							playList = new ArrayList<>();
						}
						playList.add(subList.get(i));
						isData = true;
					}
				}
			}
			if(playList == null){
				playList = new ArrayList<>();
			}
			adapter = new PlayHistoryAdapter(context, playList);
			listView.setAdapter(adapter);
			setInterface();
			listView.setVisibility(View.VISIBLE);
		}
	}
	
	@Override
	public void setUserVisibleHint(boolean isVisibleToUser) {
		super.setUserVisibleHint(isVisibleToUser);
		if(isVisibleToUser && isLoad && !isData){
			ToastUtils.show_always(context, "没有历史播放记录");
		}
		if(isVisibleToUser && TotalFragment.isDeleteSound){
			getData();
			TotalFragment.isDeleteSound = false;
		}
	}
	
	// 更新是否全选状态
	private void ifAll(){
		if(checkList == null){
			checkList = new ArrayList<>();
		}
		for(int i=0; i<playList.size(); i++){
			if(playList.get(i).getStatus() == 1 && !checkList.contains(playList.get(i))){
				checkList.add(playList.get(i));
			}else if(playList.get(i).getStatus() == 0 && checkList.contains(playList.get(i))){
				checkList.remove(playList.get(i));
			}
		}
		if(checkList.size() == playList.size()){
			Intent intentAll = new Intent();
			intentAll.setAction(BroadcastConstants.UPDATE_ACTION_ALL);
			context.sendBroadcast(intentAll);
		}else{
			Intent intentNoCheck = new Intent();
			intentNoCheck.setAction(BroadcastConstants.UPDATE_ACTION_CHECK);
			context.sendBroadcast(intentNoCheck);
		}
	}
	
	/**
	 * 设置 View 隐藏
	 */
	public void setLinearHint(){
		linearNull.setVisibility(View.GONE);
	}
	
	/**
	 * 设置 View 可见  解决全选 Dialog 挡住 ListView 最底下一条 Item 问题
	 */
	public void setLinearVisibility(){
		linearNull.setVisibility(View.VISIBLE);
	}
	
	/**
	 * 实现接口  设置点击事件
	 */
	private void setInterface() {
		adapter.setOnclick(new PlayHistoryAdapter.PlayHistoryCheck() {
			@Override
			public void checkPosition(int position) {
				if(playList.get(position).getStatus() == 0){
					playList.get(position).setStatus(1);
				}else if(playList.get(position).getStatus() == 1){
					playList.get(position).setStatus(0);
				}
				adapter.notifyDataSetChanged();
				ifAll();
			}
		});
		
		/**
		 * ListView Item 点击事件监听 
		 * 在编辑状态下点击为选中  不在编辑状态下则跳转到播放界面
		 */
		listView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				if(!PlayHistoryActivity.isEdit){
					if(playList.get(position).getStatus() == 0){
						playList.get(position).setStatus(1);
					}else if(playList.get(position).getStatus() == 1){
						playList.get(position).setStatus(0);
					}
					adapter.notifyDataSetChanged();
					ifAll();
				}else{
					if(playList != null && playList.get(position) != null){
						String playerName = playList.get(position).getPlayerName();
						String playerImage = playList.get(position).getPlayerImage();
						String playerUrl = playList.get(position).getPlayerUrl();
						String playerUri = playList.get(position).getPlayerUrI();
						String playerMediaType = playList.get(position).getPlayerMediaType();
						String playerAllTime = playList.get(position).getContentTimes();
						String playerInTime = "0";
						String playerContentDesc = playList.get(position).getPlayerContentDescn();
						String playerNum = playList.get(position).getPlayCount();
						String playerZanType = "0";
						String playerFrom =playList.get(position).getContentPub();
						String playerFromId = "";
						String playerFromUrl = playList.get(position).getPlayerFromUrl();
						String playerAddTime = Long.toString(System.currentTimeMillis());
						String bjUserId = CommonUtils.getUserId(context);
						String contentFavorite = playList.get(position).getContentFavorite();
						String playShareUrl = playList.get(position).getPlayContentShareUrl();
						String contentId = playList.get(position).getContentID();
						String localUrl = playList.get(position).getLocalurl();
						String sequname = playList.get(position).getSequName();
						String sequid = playList.get(position).getSequId();
						String sequdesc =playList.get(position).getSequDesc();
						String sequimg = playList.get(position).getSequImg();
						
						//如果该数据已经存在数据库则删除原有数据，然后添加最新数据
						PlayerHistory history = new PlayerHistory(
                                playerName, playerImage, playerUrl, playerUri, playerMediaType,
                                playerAllTime, playerInTime, playerContentDesc, playerNum,
                                playerZanType, playerFrom, playerFromId, playerFromUrl,
                                playerAddTime, bjUserId, playShareUrl, contentFavorite, contentId, localUrl,sequname,sequid,sequdesc,sequimg);
                        dbDao.deleteHistory(playerUrl);
                        dbDao.addHistory(history);
						if(PlayerFragment.context != null){
							HomeActivity.UpdateViewPager();
							Intent push=new Intent(BroadcastConstants.PLAY_TEXT_VOICE_SEARCH);
							Bundle bundle1=new Bundle();
							bundle1.putString("text", playList.get(position).getPlayerName());
							push.putExtras(bundle1);
							context.sendBroadcast(push);
							getActivity().finish();
						}else{
							SharedPreferences sp = context.getSharedPreferences("wotingfm", Context.MODE_PRIVATE);
							SharedPreferences.Editor et = sp.edit();
							et.putString(StringConstant.PLAYHISTORYENTER, "true");
							et.putString(StringConstant.PLAYHISTORYENTERNEWS, playList.get(position).getPlayerName());
							if(!et.commit()) {
                                L.v("数据 commit 失败!");
                            }
							HomeActivity.UpdateViewPager();
							getActivity().finish();
						}
					}
				}
			}
		});
	}
	
	/**
	 * 设置可选状态
	 */
	public void setCheck(boolean checkStatus){
		if(playList != null && playList.size() > 0){
			for(int i=0; i<playList.size(); i++){
				playList.get(i).setCheck(checkStatus);
			}
			adapter.notifyDataSetChanged();
		}
	}
	
	/**
	 * 设置是否选中
	 */
	public void setCheckStatus(int status){
		if(playList!= null && playList.size() > 0){
			for(int i=0; i<playList.size(); i++){
				playList.get(i).setStatus(status);
			}
			adapter.notifyDataSetChanged();
		}
	}
	
	/**
	 * 删除数据
	 */
	public int deleteData(){
		int number = 0;
		for(int i=0; i<playList.size(); i++){
			if(deleteList == null){
				deleteList = new ArrayList<>();
			}
			if(playList.get(i).getStatus() == 1){
				deleteList.add(playList.get(i));
			}
			number = deleteList.size();
		}
		if(deleteList.size() > 0){
			for(int i=0; i<deleteList.size(); i++){
				String url = deleteList.get(i).getPlayerUrl();
                dbDao.deleteHistory(url);
			}
			if(checkList != null && checkList.size() > 0){
				checkList.clear();
			}
			adapter.notifyDataSetChanged();
			deleteList.clear();
			getData();
		}
		return number;
	}
	
	@Override
	public void onDestroyView() {
		super.onDestroyView();
		if (null != rootView) {
			((ViewGroup) rootView.getParent()).removeView(rootView);   
		}
	}
	
	@Override
	public void onDestroy() {
		super.onDestroy();
		rootView = null;
		context = null;
		listView = null;
		adapter = null;
		deleteList = null;
		playList = null;
		checkList = null;
		linearNull = null;
		if(dbDao != null){
            dbDao.closedb();
            dbDao = null;
		}
	}
}
