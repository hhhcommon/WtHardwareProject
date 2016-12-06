package com.wotingfm.activity.music.favorite.fragment;

import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;

import com.android.volley.VolleyError;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.wotingfm.R;
import com.wotingfm.activity.music.favorite.activity.FavoriteActivity;
import com.wotingfm.activity.music.favorite.adapter.FavorListAdapter;
import com.wotingfm.activity.music.program.album.activity.AlbumActivity;
import com.wotingfm.activity.music.program.fmlist.model.RankInfo;
import com.wotingfm.common.config.GlobalConfig;
import com.wotingfm.common.constant.BroadcastConstants;
import com.wotingfm.common.volley.VolleyCallback;
import com.wotingfm.common.volley.VolleyRequest;
import com.wotingfm.util.DialogUtils;
import com.wotingfm.util.L;
import com.wotingfm.util.ToastUtils;
import com.wotingfm.widget.xlistview.XListView;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.util.ArrayList;
import java.util.List;

/**
 * 我喜欢的专辑界面
 */
public class SequFragment extends Fragment {
	private FragmentActivity context;
    protected FavorListAdapter adapter;
    
	private Dialog dialog;
    private View rootView;
    private View linearNull;
	private XListView mListView;

    private List<String> delList;
    private List<RankInfo> SubList;
	private ArrayList<RankInfo> newList = new ArrayList<>();
    
	private int pageSizeNum = -1;// 先求余 如果等于0 最后结果不加1 如果不等于0 结果加一
    private int page = 1;
    private int refreshType = 1;// refreshType 1为下拉加载 2为上拉加载更多
    
	private String tag = "SEQU_VOLLEY_REQUEST_CANCEL_TAG";
	private boolean isCancelRequest;
	private boolean isDel;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		context = getActivity();
        IntentFilter mFilter = new IntentFilter();
        mFilter.addAction(BroadcastConstants.VIEW_UPDATE);
        mFilter.addAction(BroadcastConstants.SET_NOT_LOAD_REFRESH);
        mFilter.addAction(BroadcastConstants.SET_LOAD_REFRESH);
        context.registerReceiver(mBroadcastReceiver, mFilter);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		if (rootView == null) {
			rootView = inflater.inflate(R.layout.fragment_favorite_sound, container, false);
			linearNull = rootView.findViewById(R.id.linear_null);
			mListView = (XListView) rootView.findViewById(R.id.listView);
			mListView.setSelector(new ColorDrawable(Color.TRANSPARENT));

			// 发送网络请求
			if (GlobalConfig.CURRENT_NETWORK_STATE_TYPE != -1) {
				send();
			} else {
				ToastUtils.show_short(context, "网络失败，请检查网络");
			}
		}
		return rootView;
	}
	
	/**
	 * 设置 View 隐藏
	 */
	public void setViewHint(){
		linearNull.setVisibility(View.GONE);
	}
	
	/**
	 * 设置 View 可见  解决全选 Dialog 挡住 ListView 最底下一条 Item 问题
	 */
	public void setViewVisibility(){
		linearNull.setVisibility(View.VISIBLE);
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		setView();
	}

	private void setListener() {
		adapter.setOnListener(new FavorListAdapter.favorCheck() {
			@Override
			public void checkposition(int position) {
				if (newList.get(position).getChecktype() == 0) {
					newList.get(position).setChecktype(1);
				} else {
					newList.get(position).setChecktype(0);
				}
				ifAll();
				adapter.notifyDataSetChanged();
			}
		});

		mListView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				if (FavoriteActivity.isEdit) {
					if (newList.get(position - 1).getChecktype() == 0) {
						newList.get(position - 1).setChecktype(1);
					} else {
						newList.get(position - 1).setChecktype(0);
					}
					ifAll();
					adapter.notifyDataSetChanged();
				} else {
					if (newList != null && newList.get(position - 1) != null
							&& newList.get(position - 1).getMediaType() != null) {
						Intent intent = new Intent(context, AlbumActivity.class);
						Bundle bundle = new Bundle();
						bundle.putString("type", "radiolistactivity");
						bundle.putSerializable("list", newList.get(position - 1));
						intent.putExtras(bundle);
						startActivity(intent);
						context.finish();
					}
				}
			}
		});
	}

	/*
	 * 初始化视图
	 */
	private void setView() {
		mListView.setPullRefreshEnable(true);
		mListView.setPullLoadEnable(true);
		mListView.setXListViewListener(new XListView.IXListViewListener() {

			@Override
			public void onRefresh() {
				if (GlobalConfig.CURRENT_NETWORK_STATE_TYPE != -1) {
                    refreshType = 1;
					page = 1;
					send();
				} else {
					mListView.stopRefresh();
					ToastUtils.show_short(context, "网络失败，请检查网络");
				}
			}

			public void onLoadMore() {
				if (page <= pageSizeNum) {
					if (GlobalConfig.CURRENT_NETWORK_STATE_TYPE != -1) {
                        refreshType = 2;
						send();
					} else {
						ToastUtils.show_short(context, "网络失败，请检查网络");
					}
				} else {
					mListView.stopLoadMore();
					mListView.setPullLoadEnable(false);
					ToastUtils.show_always(context, "已经是最后一页了");
				}
			}
		});
	}

	/*
	 * 发送网络请求
	 */
	private void send() {
		JSONObject jsonObject = VolleyRequest.getJsonObject(context);
		try {
			jsonObject.put("MediaType", "SEQU");
			jsonObject.put("Page", String.valueOf(page));
		} catch (JSONException e) {
			e.printStackTrace();
		}
		
		VolleyRequest.RequestPost(GlobalConfig.getFavoriteListUrl, tag, jsonObject, new VolleyCallback() {
			private String ReturnType;

			@Override
			protected void requestSuccess(JSONObject result) {
				if (dialog != null) {
					dialog.dismiss();
				}
				if(isCancelRequest){
					return ;
				}
				page++;
				try {
					ReturnType = result.getString("ReturnType");
                    L.w("ReturnType -- > > " + ReturnType);

                    if (ReturnType != null && ReturnType.equals("1001")) {
                        if(isDel){
                            ToastUtils.show_always(context, "已删除");
                            isDel = false;
                        }
                        JSONObject arg1 = (JSONObject) new JSONTokener(result.getString("ResultList")).nextValue();
                        SubList = new Gson().fromJson(arg1.getString("FavoriteList"), new TypeToken<List<RankInfo>>() {}.getType());
                        try {
                            String allCountString = arg1.getString("AllCount");
                            String pageSizeString = arg1.getString("PageSize");
                            if (allCountString != null && !allCountString.equals("") && pageSizeString != null && !pageSizeString.equals("")) {
                                int allCountInt = Integer.valueOf(allCountString);
                                int pageSizeInt = Integer.valueOf(pageSizeString);
                                if(pageSizeInt < 10 || allCountInt < 10){
                                    mListView.stopLoadMore();
                                    mListView.setPullLoadEnable(false);
                                }else{
                                    mListView.setPullLoadEnable(true);
                                    if (allCountInt % pageSizeInt == 0) {
                                        pageSizeNum = allCountInt / pageSizeInt;
                                    } else {
                                        pageSizeNum = allCountInt / pageSizeInt + 1;
                                    }
                                }
                            } else {
                                ToastUtils.show_always(context, "页码获取异常");
                            }

                            if(refreshType == 1) {
                                newList.clear();
                            }
                            newList.addAll(SubList);
                            if (adapter == null) {
                                mListView.setAdapter(adapter = new FavorListAdapter(context, newList));
                            } else {
                                adapter.notifyDataSetChanged();
                            }
                            setListener();
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        if(SubList != null){
                            SubList.clear();
                        }
                    }else {
                        mListView.setVisibility(View.GONE);
                        ToastUtils.show_always(context, "无数据");
                    }
				} catch (JSONException e) {
					e.printStackTrace();
				}
                // 无论何种返回值，都需要终止掉上拉刷新及下拉加载的滚动状态
                if (refreshType == 1) {
                    mListView.stopRefresh();
                } else {
                    mListView.stopLoadMore();
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

	// 更改界面的view布局 让每个item都可以显示点选框
	public boolean changeviewtype(int type) {
		if (newList != null & newList.size() != 0) {
			for (int i = 0; i < newList.size(); i++) {
				newList.get(i).setViewtype(type);
			}
			if (type == 0) {
				for (int i = 0; i < newList.size(); i++) {
					newList.get(i).setChecktype(0);
				}
			}
			adapter.notifyDataSetChanged();
			return true;
		} else {
			return false;
		}
	}

	/**
	 * 点击全选时的方法
	 */
	public void changechecktype(int type) {
		if (adapter != null) {
			for (int i = 0; i < newList.size(); i++) {
				newList.get(i).setChecktype(type);
			}
			adapter.notifyDataSetChanged();
		}
	}

	/**
	 * 获取当前页面选中的为选中的数目
	 */
	public int getdelitemsum() {
		int sum = 0;
		for (int i = 0; i < newList.size(); i++) {
			if (newList.get(i).getChecktype() == 1) {
				sum++;
			}
		}
		return sum;
	}
	
	/**
	 * 判断是否全部选择
	 */
	public void ifAll(){
		if(getdelitemsum() == newList.size()){
			Intent intentAll = new Intent();
			intentAll.setAction(BroadcastConstants.SET_ALL_IMAGE);
			context.sendBroadcast(intentAll);
		}else{
			Intent intentNotAll = new Intent();
			intentNotAll.setAction(BroadcastConstants.SET_NOT_ALL_IMAGE);
			context.sendBroadcast(intentNotAll);
		}
	}

	/**
	 * 删除
	 */
	public void delitem() {
		if (GlobalConfig.CURRENT_NETWORK_STATE_TYPE != -1) {
			dialog = DialogUtils.Dialogph(context, "正在删除");
			for (int i = 0; i < newList.size(); i++) {
				if (newList.get(i).getChecktype() == 1) {
					if (delList == null) {
						delList = new ArrayList<>();
						String type = newList.get(i).getMediaType();
						String contentId = newList.get(i).getContentId();
						delList.add(type + "::" + contentId);
					} else {
						String type = newList.get(i).getMediaType();
						String contentId = newList.get(i).getContentId();
						delList.add(type + "::" + contentId);
					}
				}
			}
            refreshType = 1;
            sendRequest();
		} else {
			ToastUtils.show_always(context, "网络失败，请检查网络");
		}
	}

	// 删除单条喜欢
	protected void sendRequest() {
		JSONObject jsonObject = VolleyRequest.getJsonObject(context);
		try {
			// 对s进行处理 去掉"[]"符号
			String s = delList.toString();
			jsonObject.put("DelInfos", s.substring(1, s.length() - 1).replaceAll(" ", ""));
		} catch (JSONException e) {
			e.printStackTrace();
		}
		
		VolleyRequest.RequestPost(GlobalConfig.delFavoriteListUrl, tag, jsonObject, new VolleyCallback() {
            private String ReturnType;
			private String Message;

			@Override
			protected void requestSuccess(JSONObject result) {
				isDel = true;
				delList.clear();
				if(isCancelRequest){
					return ;
				}
				try {
					ReturnType = result.getString("ReturnType");
					Message = result.getString("Message");
                    L.w("ReturnType -- > > " + ReturnType + " ==== Message -- > > " + Message);
                } catch (JSONException e) {
					e.printStackTrace();
				}
				if (ReturnType != null && ReturnType.equals("1001")) {
					context.sendBroadcast(new Intent(BroadcastConstants.VIEW_UPDATE));
				} else {
                    ToastUtils.show_always(context, "删除失败!");
				}
			}
			
			@Override
			protected void requestError(VolleyError error) {
				if (dialog != null) {
					dialog.dismiss();
				}
				delList.clear();
                ToastUtils.showVolleyError(context);
			}
		});
	}

	// 广播接收器  用于更新界面
	private BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
            switch (intent.getAction()) {
                case BroadcastConstants.VIEW_UPDATE:
                    if (GlobalConfig.CURRENT_NETWORK_STATE_TYPE != -1) {
                        page = 1;
                        send();
                    } else {
                        ToastUtils.show_always(context, "网络失败，请检查网络");
                    }
                    break;
                case BroadcastConstants.SET_NOT_LOAD_REFRESH:
                    if (isVisible()) {
                        mListView.setPullRefreshEnable(false);
                        mListView.setPullLoadEnable(false);
                    }
                    break;
                case BroadcastConstants.SET_LOAD_REFRESH:
                    if (isVisible()) {
                        mListView.setPullRefreshEnable(true);
                        if (newList.size() >= 10) {
                            mListView.setPullLoadEnable(true);
                        }
                    }
                    break;
            }
		}
	};

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
		isCancelRequest = VolleyRequest.cancelRequest(tag);
		mListView = null;
		context.unregisterReceiver(mBroadcastReceiver);
		context = null;
		dialog = null;
		SubList = null;
		newList = null;
		rootView = null;
		adapter = null;
		delList = null;
		linearNull = null;
		tag = null;
	}
}
