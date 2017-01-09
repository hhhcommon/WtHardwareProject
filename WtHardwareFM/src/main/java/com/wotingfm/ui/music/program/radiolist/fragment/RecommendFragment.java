package com.wotingfm.ui.music.program.radiolist.fragment;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageView;

import com.android.volley.VolleyError;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.squareup.picasso.Picasso;
import com.wotingfm.R;
import com.wotingfm.ui.music.main.HomeActivity;
import com.wotingfm.ui.music.main.dao.SearchPlayerHistoryDao;
import com.wotingfm.ui.music.player.fragment.PlayerFragment;
import com.wotingfm.ui.music.player.model.PlayerHistory;
import com.wotingfm.ui.music.program.album.activity.AlbumActivity;
import com.wotingfm.ui.music.program.fmlist.model.RankInfo;
import com.wotingfm.ui.music.program.radiolist.activity.RadioListActivity;
import com.wotingfm.ui.music.program.radiolist.adapter.RadioListAdapter;
import com.wotingfm.common.config.GlobalConfig;
import com.wotingfm.common.constant.BroadcastConstants;
import com.wotingfm.common.volley.VolleyCallback;
import com.wotingfm.common.volley.VolleyRequest;
import com.wotingfm.util.CommonUtils;
import com.wotingfm.util.DialogUtils;
import com.wotingfm.util.ToastUtils;
import com.wotingfm.widget.rollviewpager.RollPagerView;
import com.wotingfm.widget.rollviewpager.adapter.LoopPagerAdapter;
import com.wotingfm.widget.rollviewpager.hintview.IconHintView;
import com.wotingfm.widget.xlistview.XListView;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.util.ArrayList;
import java.util.List;

/**
 * 分类推荐列表
 * @author woting11
 */
public class RecommendFragment extends Fragment{
    private Context context;
    private SearchPlayerHistoryDao dbDao;	// 数据库
    private RadioListAdapter adapter;
    private ArrayList<RankInfo> newList = new ArrayList<>();
    private List<RankInfo> subList;

    private Dialog dialog;					// 加载对话框
	private View rootView;
	private XListView mListView;			// 列表

	private int page = 1;					// 页码
	private int pageSizeNum;
	private int refreshType = 1;			// refreshType 1 为下拉加载  2 为上拉加载更多
    private boolean isFirst = true;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		context = getActivity();
		initDao();
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		if(rootView == null){
			rootView = inflater.inflate(R.layout.fragment_radio_list_layout, container, false);
			View headView = LayoutInflater.from(context).inflate(R.layout.headview_acitivity_radiolist, null);
			// 轮播图
			RollPagerView mLoopViewPager= (RollPagerView) headView.findViewById(R.id.slideshowView);
			mLoopViewPager.setAdapter(new LoopAdapter(mLoopViewPager));
			mLoopViewPager.setHintView(new IconHintView(context,R.mipmap.indicators_now,R.mipmap.indicators_default));
			mListView = (XListView) rootView.findViewById(R.id.listview_fm);
			mListView.addHeaderView(headView);
			setListener();
		}
		return rootView;
	}

	// 与onActivityCreated()方法 解决预加载问题
	@Override
	public void setUserVisibleHint(boolean isVisibleToUser) {
		if(isVisibleToUser && adapter == null && getActivity() != null){
			if (GlobalConfig.CURRENT_NETWORK_STATE_TYPE != -1) {
				if(!isFirst){
					dialog = DialogUtils.Dialogph(context, "正在获取数据");
				}
				sendRequest();
				isFirst = false;
			} else {
				ToastUtils.show_short(context, "网络连接失败，请稍后重试");
			}
		}
		super.setUserVisibleHint(isVisibleToUser);
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		setUserVisibleHint(getUserVisibleHint());
	}

	// 请求网络数据
	public void sendRequest() {
        if(GlobalConfig.CURRENT_NETWORK_STATE_TYPE == -1) {
            ToastUtils.show_always(context, "网络连接失败，请检查网络设置!");
            if(dialog != null) dialog.dismiss();
            ((RadioListActivity)getActivity()).closeDialog();
            if(refreshType == 1) {
                mListView.stopRefresh();
            } else {
                mListView.stopLoadMore();
            }
            return ;
        }
		VolleyRequest.RequestPost(GlobalConfig.getContentUrl, RadioListActivity.tag, setParam(), new VolleyCallback() {
			private String ReturnType;

			@Override
			protected void requestSuccess(JSONObject result) {
				((RadioListActivity)getActivity()).closeDialog();
				if (dialog != null) dialog.dismiss();
				if(((RadioListActivity)getActivity()).isCancel()) return ;
				page++;
				try {
					ReturnType = result.getString("ReturnType");
					if (ReturnType != null && ReturnType.equals("1001")) {
						JSONObject arg1 = (JSONObject) new JSONTokener(result.getString("ResultList")).nextValue();
						subList = new Gson().fromJson(arg1.getString("List"), new TypeToken<List<RankInfo>>() {}.getType());
                        try {
                            String pageSizeString = arg1.getString("PageSize");
                            String allCountString = arg1.getString("AllCount");
                            if (allCountString != null && !allCountString.equals("") && pageSizeString != null && !pageSizeString.equals("")) {
                                int allCountInt = Integer.valueOf(allCountString);
                                int pageSizeInt = Integer.valueOf(pageSizeString);
                                if(allCountInt < 10 || pageSizeInt < 10){
                                    mListView.stopLoadMore();
                                    mListView.setPullLoadEnable(false);
                                }else{
                                    mListView.setPullLoadEnable(true);
                                    if (allCountInt  % pageSizeInt == 0) {
                                        pageSizeNum = allCountInt  / pageSizeInt;
                                    } else {
                                        pageSizeNum = allCountInt  / pageSizeInt + 1;
                                    }
                                }
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
						if (refreshType == 1) newList.clear();
                        newList.addAll(subList);
                        if(adapter == null) {
                            mListView.setAdapter(adapter = new RadioListAdapter(context, newList));
                        } else {
                            adapter.notifyDataSetChanged();
                        }
						setOnItem();
					}
				} catch (JSONException e) {
					e.printStackTrace();
				}

                if(refreshType == 1) {
                    mListView.stopRefresh();
                } else {
                    mListView.stopLoadMore();
                }
			}

			@Override
			protected void requestError(VolleyError error) {
				if (dialog != null) dialog.dismiss();
				((RadioListActivity)getActivity()).closeDialog();
				ToastUtils.showVolleyError(context);
			}
		});
	}

	private JSONObject setParam(){
		JSONObject jsonObject = VolleyRequest.getJsonObject(context);
		try {
			jsonObject.put("MediaType", "");
			jsonObject.put("CatalogType", RadioListActivity.catalogType);
			jsonObject.put("CatalogId", RadioListActivity.id);
			jsonObject.put("Page", String.valueOf(page));
			jsonObject.put("PerSize", "3");
			jsonObject.put("ResultType", "2");
			jsonObject.put("PageSize", "10");
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return jsonObject;
	}

	private void setOnItem() {
		mListView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view,int position, long id) {
				if(newList != null &&position>=2){
					if( newList.get(position - 2) != null && newList.get(position - 2).getMediaType() != null){
						String MediaType = newList.get(position - 2).getMediaType();
						if (MediaType.equals("RADIO") || MediaType.equals("AUDIO")) {
							String playerName = newList.get(position - 2).getContentName();
							String playerImage = newList.get(position - 2).getContentImg();
							String playUrl = newList.get(position - 2).getContentPlay();
							String playUrI = newList.get(position - 2).getContentURI();
							String playContentShareUrl=newList.get(position - 2).getContentShareURL();
							String playMediaType = newList.get(position - 2).getMediaType();
							String playAllTime = newList.get(position - 2).getContentTimes();
							String playInTime = "0";
							String playContentDesc = newList.get(position - 2).getContentDescn();
							String playNum = newList.get(position - 2).getPlayCount();
							String playZanType = "0";
							String playFrom = newList.get(position - 2).getContentPub();
							String playFromId = "";
							String playFromUrl = "";
							String playAddTime = Long.toString(System.currentTimeMillis());
							String bjUserId =CommonUtils.getUserId(context);
							String ContentFavorite= newList.get(position - 2).getContentFavorite();
							String ContentId= newList.get(position - 2).getContentId();
							String localUrl=newList.get(position - 2).getLocalurl();

							String sequName=newList.get(position-2).getSequName();
							String sequId=newList.get(position-2).getSequId();
							String sequDesc=newList.get(position-2).getSequDesc();
							String sequImg=newList.get(position-2).getSequImg();

							// 如果该数据已经存在数据库则删除原有数据，然后添加最新数据
							PlayerHistory history = new PlayerHistory(
									playerName,  playerImage, playUrl, playUrI,playMediaType,
									playAllTime, playInTime, playContentDesc, playNum,
									playZanType, playFrom , playFromId,playFromUrl,playAddTime,bjUserId,playContentShareUrl,
									ContentFavorite,ContentId,localUrl,sequName,sequId,sequDesc,sequImg);
							dbDao.deleteHistory(playUrl);
							dbDao.addHistory(history);
							HomeActivity.UpdateViewPager();
							PlayerFragment.TextPage=1;
							Intent push=new Intent(BroadcastConstants.PLAY_TEXT_VOICE_SEARCH);
							Bundle bundle1=new Bundle();
							bundle1.putString("text",newList.get(position - 2).getContentName());
							push.putExtras(bundle1);
							context.sendBroadcast(push);
							getActivity().finish();
						} else if (MediaType.equals("SEQU")) {
							Intent intent = new Intent(context, AlbumActivity.class);
							Bundle bundle = new Bundle();
							bundle.putString("type", "radiolistactivity");
							bundle.putSerializable("list", newList.get(position - 2));
							intent.putExtras(bundle);
							startActivityForResult(intent, 1);
						}
					}
				}

			}
		});
	}

	// 设置刷新、加载更多参数
	private void setListener() {
		mListView.setPullLoadEnable(true);
		mListView.setPullRefreshEnable(true);
		mListView.setSelector(new ColorDrawable(Color.TRANSPARENT));
		mListView.setXListViewListener(new XListView.IXListViewListener() {
			@Override
			public void onRefresh() {
                refreshType = 1;
                page = 1;
                sendRequest();
			}

			@Override
			public void onLoadMore() {
				if (page <=pageSizeNum) {
                    refreshType = 2;
                    sendRequest();
				} else {
					mListView.stopLoadMore();
					mListView.setPullLoadEnable(false);
				}

			}
		});
	}

	// 初始化数据库命令执行对象
	private void initDao() {
		dbDao = new SearchPlayerHistoryDao(context);
	}

	@Override
	public void onDestroyView() {
		super .onDestroyView();
		if (null != rootView) {
			((ViewGroup) rootView.getParent()).removeView(rootView);
		}
	}

	private class LoopAdapter extends LoopPagerAdapter{
		private int count = images.length;

		public LoopAdapter(RollPagerView viewPager){
			super(viewPager);
		}

		@Override
		public View getView(ViewGroup container, int position) {
			ImageView view = new ImageView(container.getContext());
			view.setScaleType(ImageView.ScaleType.FIT_XY);
			view.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
			Picasso.with(context).load(images[position%count]).into(view);			return view;
		}

		@Override
		public int getRealCount() {
			return count;
		}
	}

	public String[] images = {
			"http://pic.500px.me/picurl/vcg5da48ce9497b91f9c81c17958d4f882e?code=e165fb4d228d4402",
			"http://pic.500px.me/picurl/49431365352e4e94936d4562a7fbc74a---jpg?code=647e8e97cd219143",
			"http://pic.500px.me/picurl/vcgd5d3cfc7257da293f5d2686eec1068d1?code=2597028fc68bd766",
			"http://pic.500px.me/picurl/vcg1aa807a1b8bd1369e4f983e555d5b23b?code=c0c4bb78458e5503",
	};
}
