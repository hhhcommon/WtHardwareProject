package com.wotingfm.activity.music.playhistory.fragment;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ExpandableListView;
import android.widget.ExpandableListView.OnChildClickListener;
import android.widget.ExpandableListView.OnGroupClickListener;
import android.widget.TextView;

import com.wotingfm.R;
import com.wotingfm.activity.music.main.HomeActivity;
import com.wotingfm.activity.music.main.dao.SearchPlayerHistoryDao;
import com.wotingfm.activity.music.player.fragment.PlayerFragment;
import com.wotingfm.activity.music.player.model.PlayerHistory;
import com.wotingfm.activity.music.playhistory.activity.PlayHistoryActivity;
import com.wotingfm.activity.music.playhistory.adapter.PlayHistoryExpandableAdapter;
import com.wotingfm.activity.music.search.model.SuperRankInfo;
import com.wotingfm.common.constant.StringConstant;
import com.wotingfm.util.CommonUtils;
import com.wotingfm.util.ToastUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * 全部播放历史
 * 
 * @author woting11
 */
@SuppressLint("InflateParams")
public class TotalFragment extends Fragment {
	private View rootView;
	private Context context;
	private ExpandableListView mListView;	//播放历史列表
	private SearchPlayerHistoryDao dbDao;	//播放历史数据库
//	private PlayHistoryExpandableAdapter adapter;
//	private List<PlayerHistory> subList;	//播放历史数据
	private ArrayList<SuperRankInfo> list = new ArrayList<>();// 返回的节目list，拆分之前的list
	private Dialog delDialog;
	private boolean isLoad;					// 是否已经加载过
	private int delChildPosition = -1;
	private int delGroupPosition = -1;
	public static boolean isData = false;	// 是否有数据 
	public static boolean isDeleteSound;	// 标记单条删除记录为声音数据
	public static boolean isDeleteRadio;	// 标记单条删除记录为电台数据
	public static boolean isDeleteTTS;		// 标记单条删除记录为 TTS 数据

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		context = getActivity();
        dbDao = new SearchPlayerHistoryDao(context);	// 初始化数据库
		delDialog();									// 初始化对话框
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		if(rootView == null){
			rootView = inflater.inflate(R.layout.fragment_playhistory_total_layout, container, false);
			mListView = (ExpandableListView) rootView.findViewById(R.id.listview_history);
			mListView.setGroupIndicator(null);			// 去除indicator
			isLoad = true;
			getData();
		}

		return rootView;
	}

	/**
	 * 得到数据库里边数据
	 */
	public void getData() {
		mListView.setVisibility(View.GONE);
		isData = false;
        List<PlayerHistory> subList = dbDao.queryHistory();
		if (subList != null && subList.size() > 0) {
			list.clear();
			ArrayList<PlayerHistory> playList = null;
			ArrayList<PlayerHistory> ttsList = null;
			ArrayList<PlayerHistory> radioList = null;

			/**
			 * 循环遍历  对数据库里的数据进行分类
			 */
			for (int i = 0; i < subList.size(); i++) {
				isData = true;
				if (subList.get(i).getPlayerMediaType()!=null && !subList.get(i).getPlayerMediaType().equals("")) {
					if (subList.get(i).getPlayerMediaType().equals("AUDIO")) {
						if (playList == null) {
                            playList = new ArrayList<>();
                            playList.add(subList.get(i));
						} else {
							if(playList.size()<3){
                                playList.add(subList.get(i));
							}
						}
					} else if (subList.get(i).getPlayerMediaType().equals("RADIO")) {
						if (radioList == null) {
                            radioList = new ArrayList<>();
                            radioList.add(subList.get(i));
						} else {
							if(radioList.size()<3){
                                radioList.add(subList.get(i));
							}
						}
					}else if (subList.get(i).getPlayerMediaType().equals("TTS")) {
						if (ttsList == null) {
                            ttsList = new ArrayList<>();
                            ttsList.add(subList.get(i));
						} else {
							if(ttsList.size()<3){
                                ttsList.add(subList.get(i));
							}
						}
					}
				}
			}
			if (playList != null && playList.size() > 0) {
				SuperRankInfo mSuperRankInfo = new SuperRankInfo();
				mSuperRankInfo.setKey(playList.get(0).getPlayerMediaType());
				mSuperRankInfo.setHistoryList(playList);
				list.add(mSuperRankInfo);
			}
			if (radioList != null  && radioList.size() > 0) {
				SuperRankInfo mSuperRankInfo1 = new SuperRankInfo();
				mSuperRankInfo1.setKey(radioList.get(0).getPlayerMediaType());
				mSuperRankInfo1.setHistoryList(radioList);
				list.add(mSuperRankInfo1);
			}
			if (ttsList != null &&  ttsList.size() > 0) {
				SuperRankInfo mSuperRankInfo1= new SuperRankInfo();
				mSuperRankInfo1.setKey(ttsList.get(0).getPlayerMediaType());
				mSuperRankInfo1.setHistoryList(ttsList);
				list.add(mSuperRankInfo1);
			}
            PlayHistoryExpandableAdapter adapter = new PlayHistoryExpandableAdapter(context, list);
			mListView.setAdapter(adapter);
			for (int i = 0; i < list.size(); i++) {
				mListView.expandGroup(i);
			}
			setItemListener();
			setListener();
			mListView.setVisibility(View.VISIBLE);
		}
		if(!isData){
			ToastUtils.show_always(context, "没有历史播放记录");
		}
	}

	/**
	 * 没有历史播放记录时向用户友好提示
	 */
	@Override
	public void setUserVisibleHint(boolean isVisibleToUser) {
		super.setUserVisibleHint(isVisibleToUser);
		if(isVisibleToUser && isLoad && !isData){
			ToastUtils.show_always(context, "没有历史播放记录");
		}
	}

	/**
	 * 设置ExpanableListView 的 Item 的点击事件
	 */
	protected void setItemListener() {
		mListView.setOnChildClickListener(new OnChildClickListener() {
			@Override
			public boolean onChildClick(ExpandableListView parent, View v,int groupPosition, int childPosition, long id) {
				String MediaType = list.get(groupPosition).getHistoryList().get(childPosition).getPlayerMediaType();
				if (MediaType != null && !MediaType.equals("SEQU")) {
					String playerName = list.get(groupPosition).getHistoryList().get(childPosition).getPlayerName();
					String playerImage = list.get(groupPosition).getHistoryList().get(childPosition).getPlayerImage();
					String playerUrl = list.get(groupPosition).getHistoryList().get(childPosition).getPlayerUrl();
					String playerUri = list.get(groupPosition).getHistoryList().get(childPosition).getPlayerUrI();
					String playerMediaType = list.get(groupPosition).getHistoryList().get(childPosition).getPlayerMediaType();
					String playerAllTime = "0";
					String playerInTime = "0";
					String playerContentDesc = list.get(groupPosition).getHistoryList().get(childPosition).getPlayerContentDesc();
					String playerNum = list.get(groupPosition).getHistoryList().get(childPosition).getPlayerNum();
					String playerZanType = "0";
					String playerFrom = "";
					String playerFromId = "";
					String playerFromUrl = list.get(groupPosition).getHistoryList().get(childPosition).getPlayerFromUrl();
					String playerAddTime = Long.toString(System.currentTimeMillis());
					String bjUserId = CommonUtils.getUserId(context);
					String contentFavorite = list.get(groupPosition).getHistoryList().get(childPosition).getContentFavorite();
					String playShareUrl = list.get(groupPosition).getHistoryList().get(childPosition).getPlayContentShareUrl();
					String contentId = list.get(groupPosition).getHistoryList().get(childPosition).getContentID();
					String localUrl = list.get(groupPosition).getHistoryList().get(childPosition).getLocalurl();

					PlayerHistory history = new PlayerHistory(
                            playerName, playerImage, playerUrl, playerUri, playerMediaType,
                            playerAllTime, playerInTime, playerContentDesc, playerNum,
                            playerZanType,  playerFrom, playerFromId, playerFromUrl,
                            playerAddTime, bjUserId, playShareUrl, contentFavorite, contentId, localUrl);

					//如果该数据已经存在数据库则删除原有数据，然后添加最新数据
					if(playerMediaType != null && playerMediaType.equals("TTS")){
                        dbDao.deleteHistoryById(contentId);
					}else {
                        dbDao.deleteHistory(playerUrl);
					}
                    dbDao.addHistory(history);
					if(PlayerFragment.context!=null){
						HomeActivity.UpdateViewPager();
						String s = list.get(groupPosition).getHistoryList().get(childPosition).getPlayerName();
						PlayerFragment.SendTextRequest(s, context);
						getActivity().finish();
					}else{
						SharedPreferences sp = context.getSharedPreferences("wotingfm", Context.MODE_PRIVATE);
						SharedPreferences.Editor et = sp.edit();
						et.putString(StringConstant.PLAYHISTORYENTER, "true");
						et.putString(StringConstant.PLAYHISTORYENTERNEWS,list.get(groupPosition).getHistoryList().get(childPosition).getPlayerName());
						et.commit();
						HomeActivity.UpdateViewPager();
						getActivity().finish();
					}
				}
				return true; 
			}
		});
	}

	/**
	 * 屏蔽group点击事件  点击更多跳转到对应的界面查看全部历史播放记录
	 * 长按删除	长按 ExpandableListView 的 Item 弹出删除对话框
	 */
	private void setListener(){
		mListView.setOnGroupClickListener(new OnGroupClickListener() {
			@Override
			public boolean onGroupClick(ExpandableListView parent, View v, int groupPosition, long id) {		
				((PlayHistoryActivity)getActivity()).updateViewPager(list.get(groupPosition).getKey());
				return true;
			}
		});

		mListView.setOnItemLongClickListener(new OnItemLongClickListener() {
			@Override
			public boolean onItemLongClick(AdapterView<?> parent, View childView, int flatPos, long id){
				if (ExpandableListView.getPackedPositionType(id) == ExpandableListView.PACKED_POSITION_TYPE_CHILD){
					long packedPos = ((ExpandableListView) parent).getExpandableListPosition(flatPos);
                    delGroupPosition = ExpandableListView.getPackedPositionGroup(packedPos);
                    delChildPosition = ExpandableListView.getPackedPositionChild(packedPos);
					if(delGroupPosition != -1 && delChildPosition != -1){
						delDialog.show();
					}
				}
				return true;
			}
		});
	}

	/**
	 * 长按 ExpandableListView 的 Item 弹出删除对话框
	 */
	private void delDialog() {
		final View dialog1 = LayoutInflater.from(context).inflate(R.layout.dialog_exit_confirm, null);
		TextView textCancel = (TextView) dialog1.findViewById(R.id.tv_cancle);
		TextView textTitle = (TextView) dialog1.findViewById(R.id.tv_title);
		TextView textConfirm = (TextView) dialog1.findViewById(R.id.tv_confirm);
        textTitle.setText("确定删除这条播放记录?");
		delDialog = new Dialog(context, R.style.MyDialog);
		delDialog.setContentView(dialog1);
		delDialog.setCanceledOnTouchOutside(false);
		delDialog.getWindow().setBackgroundDrawableResource(R.color.dialog);

        textCancel.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				delDialog.dismiss();
			}
		});

        textConfirm.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				String playType = list.get(delGroupPosition).getHistoryList().get(delChildPosition).getPlayerMediaType();

				//"TTS" 类型的删除条件为 ContentID, 其他类型为 url
				if(playType != null && !playType.equals("") && playType.equals("TTS")){
					String contentId = list.get(delGroupPosition).getHistoryList().get(delChildPosition).getContentID();
                    dbDao.deleteHistoryById(contentId);
					isDeleteTTS = true;
				}else if(playType != null && !playType.equals("") && playType.equals("RADIO")){
					String url = list.get(delGroupPosition).getHistoryList().get(delChildPosition).getPlayerUrl();
                    dbDao.deleteHistory(url);
					isDeleteRadio = true;
				}else if(playType != null && !playType.equals("") && playType.equals("AUDIO")){
					String url = list.get(delGroupPosition).getHistoryList().get(delChildPosition).getPlayerUrl();
                    dbDao.deleteHistory(url);
					isDeleteSound = true;
				}
				getData();
				delDialog.dismiss();
			}
		});
	}

	@Override
	public void onDestroyView() {
		super.onDestroy();
		if (null != rootView) {
			((ViewGroup) rootView.getParent()).removeView(rootView);   
		}
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		mListView = null;
		rootView = null;
		context = null;
		list = null;
		delDialog = null;
		if(dbDao != null){
            dbDao.closedb();
            dbDao = null;
		}
	}
}
