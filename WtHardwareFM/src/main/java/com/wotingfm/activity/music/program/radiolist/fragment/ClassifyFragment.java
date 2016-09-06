package com.wotingfm.activity.music.program.radiolist.fragment;

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
import com.wotingfm.R;
import com.wotingfm.activity.music.main.HomeActivity;
import com.wotingfm.activity.music.main.dao.SearchPlayerHistoryDao;
import com.wotingfm.activity.music.player.model.PlayerHistory;
import com.wotingfm.activity.music.program.album.activity.AlbumActivity;
import com.wotingfm.activity.music.program.radiolist.adapter.ListInfoAdapter;
import com.wotingfm.activity.music.program.radiolist.model.ListInfo;
import com.wotingfm.activity.music.program.radiolist.rollviewpager.RollPagerView;
import com.wotingfm.activity.music.program.radiolist.rollviewpager.adapter.LoopPagerAdapter;
import com.wotingfm.activity.music.program.radiolist.rollviewpager.hintview.IconHintView;
import com.wotingfm.common.config.GlobalConfig;
import com.wotingfm.common.volley.VolleyCallback;
import com.wotingfm.common.volley.VolleyRequest;
import com.wotingfm.helper.ImageLoader;
import com.wotingfm.util.CommonUtils;
import com.wotingfm.util.DialogUtils;
import com.wotingfm.util.ToastUtils;
import com.wotingfm.widget.xlistview.XListView;
import com.wotingfm.widget.xlistview.XListView.IXListViewListener;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.util.ArrayList;
import java.util.List;

/**
 * 分类列表
 * @author woting11
 */
public class ClassifyFragment extends Fragment{
	private View rootView;
	private Context context;
	private XListView mListView;			// 列表
	private Dialog dialog;					// 加载对话框
	private int page = 1;					// 页码
	private List<ListInfo> newList;
	private int pageSizeNumber;
	private SearchPlayerHistoryDao dbDao;	// 数据库
//	protected List<ListInfo> subList;
	protected ListInfoAdapter adapter;
	private int refreshType;				// refreshtype 1为下拉加载 2为上拉加载更多
	private String catalogId;
	private String catalogType;

	/**
     * 创建 Fragment 实例
     */
    public static Fragment instance(String CatalogId, String CatalogType) {
    	Fragment fragment = new ClassifyFragment();
		Bundle bundle = new Bundle();
		bundle.putString("CatalogId", CatalogId);
		bundle.putString("CatalogType", CatalogType);
		fragment.setArguments(bundle);
		return fragment;
    }

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		context = getActivity();
		initDao();
        refreshType = 1;
		Bundle bundle = getArguments();                 //取值 用以判断加载的数据
        catalogId = bundle.getString("CatalogId");
        catalogType = bundle.getString("CatalogType");
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		if(rootView == null){
			rootView = inflater.inflate(R.layout.fragment_radio_list_layout, container, false);
            View headView = LayoutInflater.from(context).inflate(R.layout.headview_acitivity_radiolist, null);
			// 轮播图
            RollPagerView mLoopViewPager= (RollPagerView) headView.findViewById(R.id.slideshowView);
//	        mLoopViewPager.setPlayDelay(5000);
			mLoopViewPager.setAdapter(new LoopAdapter(mLoopViewPager));
			mLoopViewPager.setHintView(new IconHintView(context,R.mipmap.indicators_now,R.mipmap.indicators_default));
            mListView = (XListView) rootView.findViewById(R.id.listview_fm);
            mListView.addHeaderView(headView);
			setListener();
		}
		if (dialog != null) {
			dialog.dismiss();
		}
		return rootView;
	}

	/**
	 * 与onActivityCreated()方法 解决预加载问题 
	 */
	@Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        if(isVisibleToUser && adapter == null && getActivity() != null){
            if (GlobalConfig.CURRENT_NETWORK_STATE_TYPE != -1) {
				dialog = DialogUtils.Dialogph(context, "正在获取数据");
                newList = new ArrayList<>();
				sendRequest();
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

	/**
	 * 请求网络获取分类信息
	 */
	private void sendRequest(){
		VolleyRequest.RequestPost(GlobalConfig.getContentUrl, setParam(), new VolleyCallback() {
			private String returnType;
			private String resultList;
			private String stringSubList;

			@Override
			protected void requestSuccess(JSONObject result) {
				if (dialog != null) {
					dialog.dismiss();
				}
				page++;
				try {
                    returnType = result.getString("ReturnType");
                    resultList = result.getString("ResultList");
				} catch (JSONException e) {
					e.printStackTrace();
				}
				if (returnType != null && returnType.equals("1001")) {
					try {
						JSONTokener jsonParser = new JSONTokener(resultList);
						JSONObject arg1 = (JSONObject) jsonParser.nextValue();
                        stringSubList = arg1.getString("List");
						String pageSizeString = arg1.getString("PageSize");
						String allCountString = arg1.getString("AllCount");
						if(Integer.valueOf(pageSizeString) < 10){
                            mListView.stopLoadMore();
                            mListView.setPullLoadEnable(false);
						}else{
                            mListView.setPullLoadEnable(true);
						}
						if (allCountString != null && !allCountString.equals("") && pageSizeString != null && !pageSizeString.equals("")) {
							int allCount = Integer.valueOf(allCountString);
							int pageSize = Integer.valueOf(pageSizeString);
							// 先求余 如果等于0 最后结果不加1 如果不等于0 结果加一
							if (allCount % pageSize == 0) {
                                pageSizeNumber = allCount / pageSize;
							} else {
                                pageSizeNumber = allCount / pageSize + 1;
							}
						} else {
							ToastUtils.show_allways(context, "页码获取异常");
						}
                        List<ListInfo> subList = new Gson().fromJson(stringSubList, new TypeToken<List<ListInfo>>() {}.getType());
						if (refreshType == 1) {
                            mListView.stopRefresh();
                            newList.clear();
                            newList.addAll(subList);
							adapter = new ListInfoAdapter(context, newList);
                            mListView.setAdapter(adapter);
						} else if (refreshType == 2) {
                            mListView.stopLoadMore();
                            newList.addAll(subList);
							adapter.notifyDataSetChanged();
						}
                        setOnItem();
					} catch (JSONException e) {
						e.printStackTrace();
					}
				} else {
					ToastUtils.show_allways(context, "暂没有该分类数据");
				}
			}

			@Override
			protected void requestError(VolleyError error) {
				if (dialog != null) {
					dialog.dismiss();
				}
			}
		});
	}

	private JSONObject setParam(){
		JSONObject jsonObject = VolleyRequest.getJsonObject(context);
		try {
			jsonObject.put("UserId", CommonUtils.getUserId(context));
			jsonObject.put("CatalogType", catalogType);
			jsonObject.put("CatalogId", catalogId);
			jsonObject.put("Page", String.valueOf(page));
			jsonObject.put("ResultType", "3");
			jsonObject.put("RelLevel", "2");
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
				if(newList != null && newList.get(position - 2) != null && newList.get(position - 2).getMediaType() != null){
					String MediaType = newList.get(position - 2).getMediaType();
					if (MediaType.equals("RADIO") || MediaType.equals("AUDIO")) {
						String playername = newList.get(position - 2).getContentName();
						String playerimage = newList.get(position - 2).getContentImg();
						String playerurl = newList.get(position - 2).getContentPlay();
						String playerurI = newList.get(position - 2).getContentURI();
						String playcontentshareurl = newList.get(position - 2).getContentShareURL();
						String playermediatype = newList.get(position - 2).getMediaType();
						String plaplayeralltime = "0";
						String playerintime = "0";
						String playercontentdesc = newList.get(position - 2).getContentDesc();
						String playernum = newList.get(position - 2).getPlayCount();
						String playerzantype = "0";
						String playerfrom = "";
						String playerfromid = "";
						String playerfromurl = "";
						String playeraddtime = Long.toString(System.currentTimeMillis());
						String bjuserid = CommonUtils.getUserId(context);
						String ContentFavorite = newList.get(position - 2).getContentFavorite();
						String ContentId = newList.get(position - 2).getContentId();
						String localurl = newList.get(position - 2).getLocalurl();

						//如果该数据已经存在数据库则删除原有数据，然后添加最新数据
						PlayerHistory history = new PlayerHistory(
								playername,  playerimage, playerurl,playerurI, playermediatype, 
								 plaplayeralltime, playerintime, playercontentdesc, playernum,
								 playerzantype,  playerfrom, playerfromid,playerfromurl, playeraddtime,bjuserid,playcontentshareurl,ContentFavorite,ContentId,localurl);
                        dbDao.deleteHistory(playerurl);
                        dbDao.addHistory(history);

						HomeActivity.UpdateViewPager();
//						PlayerFragment.SendTextRequest(newlist.get(position - 2).getContentName(),context);
						getActivity().finish();
					} else if (MediaType.equals("SEQU")) {
						Intent intent = new Intent(context, AlbumActivity.class);
						Bundle bundle = new Bundle();
						bundle.putString("type", "radiolistactivity");
						bundle.putSerializable("list", newList.get(position - 2));
						intent.putExtras(bundle);
						startActivityForResult(intent, 1);
					} else {
						ToastUtils.show_short(context, "暂不支持的Type类型");
					}
				}
			}
		});
	}

	/**
	 * 设置刷新、加载更多参数
	 */
	private void setListener() {
        mListView.setPullLoadEnable(true);
        mListView.setPullRefreshEnable(true);
        mListView.setSelector(new ColorDrawable(Color.TRANSPARENT));
        mListView.setXListViewListener(new IXListViewListener() {
			@Override
			public void onRefresh() {
				if (GlobalConfig.CURRENT_NETWORK_STATE_TYPE != -1) {
                    refreshType = 1;
					page = 1;
					sendRequest();
				} else {
					ToastUtils.show_short(context, "网络失败，请检查网络");
				}
			}

			@Override
			public void onLoadMore() {
				if (page <= pageSizeNumber) {
					if (GlobalConfig.CURRENT_NETWORK_STATE_TYPE != -1) {
                        refreshType = 2;
						sendRequest();
						ToastUtils.show_short(context, "正在请求" + page + "页信息");
					} else {
						ToastUtils.show_short(context, "网络失败，请检查网络");
					}
				} else {
                    mListView.stopLoadMore();
                    mListView.setPullLoadEnable(false);
					ToastUtils.show_short(context, "已经没有最新的数据了");
				}
			}
		});
	}

	/**
	 * 初始化数据库命令执行对象
	 */
	private void initDao() {
        dbDao = new SearchPlayerHistoryDao(context);
	}

	@Override
	public void onDestroyView() {
		super.onDestroyView(); 
		if (null != rootView) {
			((ViewGroup) rootView.getParent()).removeView(rootView);   
		}
	}
	
	private class LoopAdapter extends LoopPagerAdapter {
        public LoopAdapter(RollPagerView viewPager) {
			super(viewPager);
		}

		private int count = imgs.length;

        @Override
        public View getView(ViewGroup container, int position) {
            ImageView view = new ImageView(container.getContext());
            view.setScaleType(ImageView.ScaleType.FIT_XY);
            view.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
            new ImageLoader(context).DisplayImage(imgs[position%count],view, false, false, null, null);
            return view;
        }

		@Override
		public int getRealCount() {
			return count;
		}
    }

	public String[] imgs = {
        "http://pic.500px.me/picurl/vcg5da48ce9497b91f9c81c17958d4f882e?code=e165fb4d228d4402",
        "http://pic.500px.me/picurl/49431365352e4e94936d4562a7fbc74a---jpg?code=647e8e97cd219143",
        "http://pic.500px.me/picurl/vcgd5d3cfc7257da293f5d2686eec1068d1?code=2597028fc68bd766",
        "http://pic.500px.me/picurl/vcg1aa807a1b8bd1369e4f983e555d5b23b?code=c0c4bb78458e5503",
	};
}
