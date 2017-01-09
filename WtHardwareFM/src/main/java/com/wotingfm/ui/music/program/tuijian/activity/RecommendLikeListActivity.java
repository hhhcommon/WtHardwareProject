package com.wotingfm.ui.music.program.tuijian.activity;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;

import com.android.volley.VolleyError;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.wotingfm.R;
import com.wotingfm.common.config.GlobalConfig;
import com.wotingfm.common.constant.BroadcastConstants;
import com.wotingfm.common.volley.VolleyCallback;
import com.wotingfm.common.volley.VolleyRequest;
import com.wotingfm.ui.baseactivity.AppActivity;
import com.wotingfm.ui.music.main.HomeActivity;
import com.wotingfm.ui.music.main.dao.SearchPlayerHistoryDao;
import com.wotingfm.ui.music.player.model.PlayerHistory;
import com.wotingfm.ui.music.program.album.activity.AlbumActivity;
import com.wotingfm.ui.music.program.fmlist.model.RankInfo;
import com.wotingfm.ui.music.program.tuijian.adapter.RecommendListAdapter;
import com.wotingfm.util.CommonUtils;
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
 * 猜你喜欢  更多列表
 * @author woting11
 */
public class RecommendLikeListActivity extends AppActivity {
    private RecommendListAdapter adapterLikeList;
	private XListView mListView;			// 列表
	private Dialog dialog;					// 加载对话框

	private List<RankInfo> newList = new ArrayList<>();
	protected List<RankInfo> subList;

	private SearchPlayerHistoryDao dbDao;	// 数据库
	private String tag = "RECOMMEND_LIKE_VOLLEY_REQUEST_CANCEL_TAG";
	private boolean isCancelRequest;
    private int page = 1;					// 页码
    private int refreshType = 1;		    // refreshType 1为下拉加载 2为上拉加载更多
    private int pageSizeNum;

    // 初始化数据库命令执行对象
    private void initDao() {
        dbDao = new SearchPlayerHistoryDao(context);
    }

    @Override
    protected int setViewId() {
        return R.layout.activity_recommend_like_list_layout;
    }

    @Override
    protected void init() {
        setTitle("猜你喜欢");
        initDao();
        initListView();

        dialog = DialogUtils.Dialogph(context, "正在获取数据...");
        sendRequest();
    }

    // 初始化控件
    private void initListView() {
        mListView = (XListView) findViewById(R.id.listview_fm);
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
                if (page <= pageSizeNum) {
                    refreshType = 2;
                    sendRequest();
                } else {
                    mListView.stopLoadMore();
                    ToastUtils.show_always(context, "已经没有最新的数据了");
                }
            }
        });
    }

    // 请求网络数据 获取列表
	private void sendRequest(){
        // 以下操作需要网络支持
        if (GlobalConfig.CURRENT_NETWORK_STATE_TYPE == -1) {
            ToastUtils.show_always(context, "网络连接失败，请稍后重试!");
            mListView.stopRefresh();
            mListView.stopLoadMore();
            return ;
        }

		VolleyRequest.RequestPost(GlobalConfig.getContentUrl, tag, setParam(), new VolleyCallback() {
            String returnType;

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
                    returnType = result.getString("ReturnType");
                    L.v("returnType -- > > " + returnType);
                } catch (JSONException e) {
					e.printStackTrace();
				}
				if (returnType != null && returnType.equals("1001")) {
                    try {
                        JSONObject arg1 = (JSONObject) new JSONTokener(result.getString("ResultList")).nextValue();
                        subList = new Gson().fromJson(arg1.getString("List"), new TypeToken<List<RankInfo>>() {}.getType());

                        String pageSizeString = arg1.getString("PageSize");
                        String allCountString = arg1.getString("AllCount");
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
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    if (refreshType == 1) {
                        newList.clear();
                    }
                    newList.addAll(subList);
                    if (adapterLikeList == null) {
                        mListView.setAdapter(adapterLikeList = new RecommendListAdapter(context, newList, false));
                    } else {
                        adapterLikeList.notifyDataSetChanged();
                    }
                    setOnItem();
				} else {
                    ToastUtils.show_always(context, "暂无数据");
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
                ToastUtils.showVolleyError(context);
			}
		});
	}

    // 请求网络需要提交的参数
	private JSONObject setParam(){
		JSONObject jsonObject = VolleyRequest.getJsonObject(context);
		try {
			jsonObject.put("MediaType", "");
			jsonObject.put("CatalogType", "-1");// 001为一个结果 002为另一个
			jsonObject.put("CatalogId", "");
			jsonObject.put("Page", String.valueOf(page));
			jsonObject.put("PerSize", "3");
			jsonObject.put("ResultType", "3");
			jsonObject.put("PageSize", "10");
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return jsonObject;
	}

    // List 点击事件监听
	private void setOnItem() {
		mListView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view,int position, long id) {
				if(newList != null && newList.get(position - 1) != null && newList.get(position - 1).getMediaType() != null){
					String MediaType = newList.get(position - 1).getMediaType();
					if (MediaType.equals("RADIO") || MediaType.equals("AUDIO")) {
						String playername = newList.get(position - 1).getContentName();
						String playerimage = newList.get(position - 1).getContentImg();
						String playerurl = newList.get(position - 1).getContentPlay();
						String playerurI = newList.get(position - 1).getContentURI();
						String playcontentshareurl=newList.get(position-1).getContentShareURL();
						String playermediatype = newList.get(position - 1).getMediaType();
						String plaplayeralltime = "0";
						String playerintime = "0";
						String playercontentdesc = newList.get(position - 1).getCurrentContent();
						String playernum = newList.get(position - 1).getWatchPlayerNum();
						String playerzantype = "0";
						String playerfrom = "";
						String playerfromid = "";
						String playerfromurl = "";
						String playeraddtime = Long.toString(System.currentTimeMillis());
						String bjuserid =CommonUtils.getUserId(context);
						String ContentFavorite= newList.get(position - 1).getContentFavorite();
						String ContentId= newList.get(position-1).getContentId();
						String localurl=newList.get(position-1).getLocalurl();
						String sequname = newList.get(position - 1).getSequName();
						String sequid = newList.get(position - 1).getSequId();
						String sequdesc =newList.get(position - 1).getSequDesc();
						String sequimg =newList.get(position - 1).getSequImg();
						
						// 如果该数据已经存在数据库则删除原有数据，然后添加最新数据
						PlayerHistory history = new PlayerHistory(
								playername,  playerimage, playerurl,playerurI, playermediatype, 
								 plaplayeralltime, playerintime, playercontentdesc, playernum,
								 playerzantype,  playerfrom, playerfromid,playerfromurl, playeraddtime,bjuserid,playcontentshareurl,ContentFavorite,ContentId,localurl,sequname,sequid,sequdesc,sequimg);
                        dbDao.deleteHistory(playerurl);
                        dbDao.addHistory(history);
						HomeActivity.UpdateViewPager();

						Intent push=new Intent(BroadcastConstants.PLAY_TEXT_VOICE_SEARCH);
						Bundle bundle1=new Bundle();
						bundle1.putString("text",newList.get(position - 1).getContentName());
						push.putExtras(bundle1);
						context.sendBroadcast(push);
						finish();
					} else if (MediaType.equals("SEQU")) {
						Intent intent = new Intent(context, AlbumActivity.class);
						Bundle bundle = new Bundle();
						bundle.putString("type", "radiolistactivity");
						bundle.putSerializable("list", newList.get(position - 1));
						intent.putExtras(bundle);
						startActivityForResult(intent, 1);
					} else {
						ToastUtils.show_short(context, "暂不支持的Type类型");
					}
				}
			}
		});
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		switch (requestCode) {
		case 1:
			if (resultCode == 1) {
				finish();
			}
			break;
		}
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		isCancelRequest = VolleyRequest.cancelRequest(tag);
		mListView = null;
		dialog = null;
		newList = null;
        subList = null;
		adapterLikeList = null;
		tag = null;
		if(dbDao != null){
            dbDao.closedb();
            dbDao = null;
		}
	}
}
