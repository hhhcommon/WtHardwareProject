package com.wotingfm.ui.music.download.downloadlist.activity;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.wotingfm.R;
import com.wotingfm.ui.baseactivity.BaseActivity;
import com.wotingfm.ui.music.download.dao.FileInfoDao;
import com.wotingfm.ui.music.download.downloadlist.adapter.DownLoadListAdapter;
import com.wotingfm.ui.music.download.model.FileInfo;
import com.wotingfm.ui.music.main.dao.SearchPlayerHistoryDao;
import com.wotingfm.util.CommonUtils;
import com.wotingfm.util.ToastUtils;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
/**
 * 下载列表
 * @author 辛龙
 *2016年8月8日
 */
public class DownLoadListActivity extends BaseActivity implements OnClickListener {
	private ListView mlistview;
	private TextView head_name_tv;
	private TextView tv_sum;
	private TextView tv_totalcache;
	private LinearLayout lin_dinglan;
	private List<FileInfo> fileinfolist = new ArrayList<>();
	private DownLoadListAdapter adapter;
	private String sequname;
	private int positionnow = -1;	// 标记当前选中的位置
	private String sequid;
	private int sum = 0;
	private Dialog confirmdialog;
	private Dialog confirmdialog1;
	private DecimalFormat df;
	private SearchPlayerHistoryDao dbdao;
	private FileInfoDao FID;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_downloadlist);
		InitDao();
		handleIntent();
		setview();
		confirmdialog();// 确定是否删除记录弹窗
		df = new DecimalFormat("0.00");
	}

	@Override
	protected void onResume() {
		super.onResume();
		setListValue();// 给list赋初值
	}

	private void confirmdialog() {
		final View dialog1 = LayoutInflater.from(this).inflate(R.layout.dialog_exit_confirm, null);
		TextView tv_cancle = (TextView) dialog1.findViewById(R.id.tv_cancle);
		TextView tv_confirm = (TextView) dialog1.findViewById(R.id.tv_confirm);
		TextView tv_title = (TextView) dialog1.findViewById(R.id.tv_title);
		tv_title.setText("文件不存在，是否删除这条记录?");
		confirmdialog = new Dialog(this, R.style.MyDialog);
		confirmdialog.setContentView(dialog1);
		confirmdialog.setCanceledOnTouchOutside(true);
		confirmdialog.getWindow().setBackgroundDrawableResource(R.color.dialog);
		tv_cancle.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				confirmdialog.dismiss();
			}
		});

		tv_confirm.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// 这里添加删除数据库事件
				try {
					FID.deleteFileInfo(fileinfolist.get(positionnow).getLocalurl(), CommonUtils.getUserId(context));
					if (confirmdialog != null) {
						confirmdialog.dismiss();
					}
					setListValue();
					Intent p_intent = new Intent("push_down_completed");
					context.sendBroadcast(p_intent);
					ToastUtils.show_always(context, "此目录内已经没有内容");
				} catch (Exception e) {
					ToastUtils.show_always(context, "文件删除失败，请稍后重试");
					if (confirmdialog != null) {
						confirmdialog.dismiss();
					}
				}
			}
		});
	}

	private void InitDao() {
		FID = new FileInfoDao(DownLoadListActivity.this);
		dbdao = new SearchPlayerHistoryDao(DownLoadListActivity.this);
	}

	private void setListValue() {
		sum=0;
		fileinfolist = FID.queryFileInfo(sequid, CommonUtils.getUserId(context),0);
		if (fileinfolist.size() != 0) {
			lin_dinglan.setVisibility(View.VISIBLE);
			mlistview.setVisibility(View.VISIBLE);
			adapter = new DownLoadListAdapter(context, fileinfolist);
			mlistview.setAdapter(adapter);
			setItemListener();
			setInterface();
			tv_sum.setText("共" + fileinfolist.size() + "个节目");
			for(int i=0;i<fileinfolist.size();i++){
				sum += fileinfolist.get(i).getEnd();			
			}
			if(sum != 0){
				tv_totalcache.setText("共"+df.format(sum / 1000.0 / 1000.0) + "MB");
			}
		}else{
			lin_dinglan.setVisibility(View.GONE);
			adapter = new DownLoadListAdapter(context, fileinfolist);
			mlistview.setAdapter(adapter);
			Intent p_intent = new Intent("push_down_completed");
			context.sendBroadcast(p_intent);
			ToastUtils.show_always(context, "此目录内已经没有内容");
		}
	}

	private void setInterface() {
		adapter.setonListener(new DownLoadListAdapter.downloadlist() {
			@Override
			public void checkposition(int position) {
				deleteConfirmDialog(position);
				confirmdialog1.show();
			}
		});
	}

	/*
	 * 删除对话框
	 */
	private void deleteConfirmDialog(final int position) {
		final View dialog1 = LayoutInflater.from(context).inflate(R.layout.dialog_exit_confirm, null);
		TextView tv_cancle = (TextView) dialog1.findViewById(R.id.tv_cancle);
		TextView tv_confirm = (TextView) dialog1.findViewById(R.id.tv_confirm);
		TextView tv_title = (TextView) dialog1.findViewById(R.id.tv_title);
		tv_title.setText("是否删除这条记录");
		confirmdialog1 = new Dialog(context, R.style.MyDialog);
		confirmdialog1.setContentView(dialog1);
		confirmdialog1.setCanceledOnTouchOutside(false);
		confirmdialog1.getWindow().setBackgroundDrawableResource(R.color.dialog);
		tv_cancle.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				confirmdialog1.dismiss();
			}
		});

		tv_confirm.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				confirmdialog1.dismiss();
				FID.deleteFileInfo(fileinfolist.get(position).getLocalurl(), CommonUtils.getUserId(context));
				setListValue();
				Intent p_intent = new Intent("push_down_completed");
				context.sendBroadcast(p_intent);
			}
		});
	}

	private void setItemListener() {
		mlistview.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				//ToastUtil.show_always(context, "我的localurl是"+fileinfolist.get(position).getLocalurl());
				if(fileinfolist != null && fileinfolist.size() != 0){
					positionnow =position;
					FileInfo mFileInfo = fileinfolist.get(position);
					ToastUtils.show_always(context,""+fileinfolist.get(position).getLocalurl());
					/*if(mFileInfo.getLocalurl() != null && !mFileInfo.getLocalurl().equals("")){
						File file = new File(mFileInfo.getLocalurl());
						if (file.exists()) {
							String playername = mFileInfo.getFileName().substring(0, mFileInfo.getFileName().length() - 4);
							String playerimage = mFileInfo.getImageurl();
							String playerurl = mFileInfo.getUrl();
							String playerurI = mFileInfo.getLocalurl();
							String playlocalrurl = mFileInfo.getLocalurl();
							String playermediatype = "AUDIO";
							String playercontentshareurl = mFileInfo.getContentShareURL();
							String plaplayeralltime = mFileInfo.getPlayAllTime();
							String playerintime = "0";
							String playercontentdesc = mFileInfo.getContentDescn();
							String playernum = mFileInfo.getPlayCount();
							String playerzantype = "0";
							String playerfrom = mFileInfo.getPlayFrom();
							String playerfromid = "";
							String playerfromurl = "";
							String playeraddtime = Long.toString(System.currentTimeMillis());
							String bjuserid = CommonUtils.getUserId(context);
							String ContentFavorite = mFileInfo.getContentFavorite();
							String ContentId = mFileInfo.getContentId();
							String sequName = mFileInfo.getSequname();
							String sequId = mFileInfo.getSequid();
							String sequImg = mFileInfo.getSequimgurl();
							String sequDesc = mFileInfo.getSequdesc();

							//如果该数据已经存在数据库则删除原有数据，然后添加最新数据
							PlayerHistory history = new PlayerHistory(
									playername,  playerimage, playerurl, playerurI,playermediatype, 
									plaplayeralltime, playerintime, playercontentdesc, playernum,
									playerzantype,  playerfrom, playerfromid, playerfromurl,playeraddtime,bjuserid,playercontentshareurl,ContentFavorite,
									ContentId,playlocalrurl,sequName,sequId,sequDesc,sequImg);
							dbdao.deleteHistory(playerurl);
							dbdao.addHistory(history);
							if (GlobalConfig.CURRENT_NETWORK_STATE_TYPE != -1) {
								if (PlayerFragment.context != null) {
									MainActivity.changeToMusic();
									HomeActivity.UpdateViewPager();
									Intent push=new Intent(BroadcastConstants.PLAY_TEXT_VOICE_SEARCH);
									Bundle bundle1=new Bundle();
									bundle1.putString("text", mFileInfo.getFileName().substring(0, mFileInfo.getFileName().length() - 4));
									push.putExtras(bundle1);
									context.sendBroadcast(push);
									finish();
								} else {
									SharedPreferences.Editor et = BSApplication.SharedPreferences.edit();
									et.putString(StringConstant.PLAYHISTORYENTER, "true");
									et.putString(StringConstant.PLAYHISTORYENTERNEWS, mFileInfo.getFileName().substring(0, mFileInfo.getFileName().length() - 4));
									if (!et.commit()) {
										Log.v("commit", "数据 commit 失败!");
									}
									MainActivity.changeToMusic();
									HomeActivity.UpdateViewPager();
								}
							}else{
								//没网的状态下
								MainActivity.changeToMusic();
								HomeActivity.UpdateViewPager();
//								PlayerFragment.playNoNet();
							}
							setResult(1);
							finish();
							dbdao.closedb();
						} else {	// 此处要调对话框，点击同意删除对应的文件信息
							*//* ToastUtil.show_always(context, "文件已经被删除，是否删除本条记录"); *//*
							positionnow = position;
							confirmdialog.show();
						}
					}*/
				}
			}
		});
	}

	private void handleIntent() {
		Intent intent = getIntent();
		Bundle bundle = intent.getExtras();
		sequname = bundle.getString("sequname");
		sequid = bundle.getString("sequid");
	}

	private void setview() {
        findViewById(R.id.head_left_btn).setOnClickListener(this);// 返回按钮
		mlistview = (ListView) findViewById(R.id.lv_downloadlist);
		head_name_tv = (TextView) findViewById(R.id.head_name_tv);
		head_name_tv.setText(sequname);
		tv_sum = (TextView) findViewById(R.id.tv_sum);
		tv_totalcache = (TextView) findViewById(R.id.tv_totalcache);
		lin_dinglan = (LinearLayout) findViewById(R.id.lin_dinglan);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.head_left_btn:
			finish();
			break;
		}
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		mlistview=null;
		head_name_tv=null;
		tv_sum=null;
		tv_totalcache=null;
		lin_dinglan=null;
		fileinfolist.clear();
		fileinfolist=null;
		adapter=null;
		confirmdialog=null;
		confirmdialog1=null;
		df=null;
		dbdao=null;
		FID=null;
		context = this;
		setContentView(R.layout.activity_null);
	}
}
