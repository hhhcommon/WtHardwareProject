package com.wotingfm.activity.music.program.radiolist.activity;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Dialog;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.android.volley.VolleyError;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.wotingfm.R;
import com.wotingfm.activity.music.program.fenlei.model.fenLeiName;
import com.wotingfm.activity.music.program.radiolist.adapter.MyPagerAdapter;
import com.wotingfm.activity.music.program.radiolist.fragment.ClassifyFragment;
import com.wotingfm.activity.music.program.radiolist.fragment.RecommendFragment;
import com.wotingfm.activity.music.program.radiolist.model.CatalogData;
import com.wotingfm.activity.music.program.radiolist.model.SubCata;
import com.wotingfm.common.config.GlobalConfig;
import com.wotingfm.common.volley.VolleyCallback;
import com.wotingfm.common.volley.VolleyRequest;
import com.wotingfm.util.CommonUtils;
import com.wotingfm.util.DialogUtils;
import com.wotingfm.util.ToastUtils;
import com.wotingfm.widget.PagerSlidingTabStrip;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * 某一分类数据
 * @author 辛龙 
 * 2016年4月5日
 */
public class RadioListActivity extends FragmentActivity implements OnClickListener {
	private TextView mTextViewHead;
//	public static String categoryName;
	public static String categoryType;
	public static String id;
	private Dialog dialog;					// 加载对话框
	private List<String> list;
	private List<Fragment> fragments;
	private PagerSlidingTabStrip pageSlidingTab;
	private ViewPager viewPager;
	private int count = 1;
	public static final String tag = "RADIO_LIST_VOLLEY_REQUEST_CANCEL_TAG";
	public static boolean isCancelRequest;

	@TargetApi(Build.VERSION_CODES.KITKAT)
	@SuppressLint("InlinedApi")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_radiolist);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);		// 透明状态栏
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);	// 透明导航栏
		fragments = new ArrayList<>();
        setView();
		HandleRequestType();
		if(list == null){
			list = new ArrayList<>();
			list.add("推荐");
            RecommendFragment recommendFragment = new RecommendFragment();
			fragments.add(recommendFragment);
		}
		sendRequest();
		dialog = DialogUtils.Dialogph(this, "正在获取数据");
	}

	/**
	 * 接收上一个页面传递过来的数据
	 */
	private void HandleRequestType() {
		Intent listIntent = getIntent();
		if (listIntent != null) {
			fenLeiName list = (fenLeiName) listIntent.getSerializableExtra("Catalog");
            String categoryName = list.getCatalogName();
            categoryType = list.getCatalogType();
			id = list.getCatalogId();
            mTextViewHead.setText(categoryName);
		}
	}

	/**
	 * 请求网络获取分类信息
	 */
	private void sendRequest(){
		VolleyRequest.RequestPost(GlobalConfig.getCatalogUrl, tag, setParam(), new VolleyCallback() {
			private String returnType;
			private List<SubCata> subCateList;
			private String CatalogData;

			@Override
			protected void requestSuccess(JSONObject result) {
//				closeDialog();
				Log.e("获取分类列表返回值",""+result.toString());
				try {
                    returnType = result.getString("ReturnType");
					CatalogData = result.getString("CatalogData");
				} catch (JSONException e) {
					e.printStackTrace();
				}
				if (returnType != null && returnType.equals("1001")) {
					CatalogData catalogData = new Gson().fromJson(CatalogData, new TypeToken<CatalogData>() {}.getType());
                    subCateList = catalogData.getSubCata();
					if(subCateList != null && subCateList.size() > 0){
						for(int i=0; i<subCateList.size(); i++){
							list.add(subCateList.get(i).getCatalogName());
							fragments.add(ClassifyFragment.instance(subCateList.get(i).getCatalogId(), subCateList.get(i).getCatalogType()));
							count++;
						}
					}
					viewPager.setAdapter(new MyPagerAdapter(getSupportFragmentManager(), list, fragments));
					pageSlidingTab.setViewPager(viewPager);
					if(count == 1){
						pageSlidingTab.setVisibility(View.GONE);
					}
				} else {
					ToastUtils.show_always(RadioListActivity.this, "暂没有该分类数据");
				}
			}

			@Override
			protected void requestError(VolleyError error) {
				closeDialog();
			}
		});
	}

	private JSONObject setParam(){
		JSONObject jsonObject = VolleyRequest.getJsonObject(RadioListActivity.this);
		try {
			jsonObject.put("UserId", CommonUtils.getUserId(RadioListActivity.this));
			jsonObject.put("CatalogType", categoryType);
			jsonObject.put("CatalogId", id);
			jsonObject.put("Page", "1");
			jsonObject.put("ResultType", "1");
			jsonObject.put("RelLevel", "0");
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return jsonObject;
	}

	/**
	 * 关闭加载对话框
	 */
	public void closeDialog(){
		if (dialog != null) {
			dialog.dismiss();
		}
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

	/**
	 * 初始化界面
	 */
	private void setView() {
        LinearLayout head_left_btn = (LinearLayout) findViewById(R.id.head_left_btn);
		head_left_btn.setOnClickListener(this);
        mTextViewHead = (TextView) findViewById(R.id.head_name_tv);
		pageSlidingTab = (PagerSlidingTabStrip) findViewById(R.id.tabs_title);
		viewPager = (ViewPager) findViewById(R.id.view_pager);
		pageSlidingTab.setIndicatorHeight(4);								// 滑动指示器的高度
		pageSlidingTab.setIndicatorColorResource(R.color.dinglan_orange);	// 滑动指示器的颜色
		pageSlidingTab.setDividerColorResource(R.color.WHITE);				// 菜单之间的分割线颜色
		pageSlidingTab.setSelectedTextColorResource(R.color.dinglan_orange);// 选中的字体颜色
		pageSlidingTab.setTextColorResource(R.color.wt_login_third);		// 默认字体颜色
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
		isCancelRequest = VolleyRequest.cancelRequest(tag);
		pageSlidingTab = null;
		viewPager = null;
        mTextViewHead = null;
		dialog = null;
		if(list != null){
			list.clear();
			list = null;
		}
		if(fragments != null){
			fragments.clear();
			fragments = null;
		}
		setContentView(R.layout.activity_null);
	}
}
