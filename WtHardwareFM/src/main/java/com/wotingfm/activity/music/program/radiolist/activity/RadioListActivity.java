package com.wotingfm.activity.music.program.radiolist.activity;

import android.annotation.TargetApi;
import android.app.Dialog;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.TextView;

import com.android.volley.VolleyError;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.wotingfm.R;
import com.wotingfm.activity.music.program.fenlei.model.FLeiName;
import com.wotingfm.activity.music.program.radiolist.adapter.MyPagerAdapter;
import com.wotingfm.activity.music.program.radiolist.fragment.ClassifyFragment;
import com.wotingfm.activity.music.program.radiolist.fragment.RecommendFragment;
import com.wotingfm.activity.music.program.radiolist.model.CatalogData;
import com.wotingfm.activity.music.program.radiolist.model.SubCata;
import com.wotingfm.common.config.GlobalConfig;
import com.wotingfm.common.volley.VolleyCallback;
import com.wotingfm.common.volley.VolleyRequest;
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
    private Dialog dialog;					// 加载对话框
    private PagerSlidingTabStrip pageSlidingTab;
    private ViewPager viewPager;

	private List<String> list = new ArrayList<>();
	private List<Fragment> fragments = new ArrayList<>();

//    public static String categoryName;
    public static String categoryType;
    public static String id;
	public static final String tag = "RADIO_LIST_VOLLEY_REQUEST_CANCEL_TAG";
	public static boolean isCancelRequest;
    private int count = 1;

	@TargetApi(Build.VERSION_CODES.KITKAT)
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_radiolist);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);		// 透明状态栏
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);	// 透明导航栏
        setView();
		HandleRequestType();

        list.add("推荐");
        fragments.add(new RecommendFragment());

		sendRequest();
		dialog = DialogUtils.Dialogph(this, "正在获取数据");
	}

	// 接收上一个页面传递过来的数据
	private void HandleRequestType() {
		Intent listIntent = getIntent();
		if (listIntent != null) {
			FLeiName list = (FLeiName) listIntent.getSerializableExtra("Catalog");
            String categoryName = list.getCatalogName();
            categoryType = list.getCatalogType();
			id = list.getCatalogId();

            TextView mTextViewHead = (TextView) findViewById(R.id.head_name_tv);
            mTextViewHead.setText(categoryName);
		}
	}

	// 请求网络获取分类信息
	private void sendRequest(){
        if(GlobalConfig.CURRENT_NETWORK_STATE_TYPE == -1) {
            ToastUtils.show_always(RadioListActivity.this, "网络连接失败，请稍后重试!");
            return ;
        }

		VolleyRequest.RequestPost(GlobalConfig.getCatalogUrl, tag, setParam(), new VolleyCallback() {
			private String returnType;
			private List<SubCata> subCateList;

			@Override
			protected void requestSuccess(JSONObject result) {
//				closeDialog();
				try {
                    returnType = result.getString("ReturnType");
				} catch (JSONException e) {
					e.printStackTrace();
				}
                try {
                    if (returnType != null && returnType.equals("1001")) {
                        CatalogData catalogData = new Gson().fromJson(result.getString("CatalogData"), new TypeToken<CatalogData>() {}.getType());
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
                } catch (JSONException e) {
                    e.printStackTrace();
                }
			}

			@Override
			protected void requestError(VolleyError error) {
				closeDialog();
                ToastUtils.showVolleyError(RadioListActivity.this);
			}
		});
	}

	private JSONObject setParam(){
		JSONObject jsonObject = VolleyRequest.getJsonObject(RadioListActivity.this);
		try {
//			jsonObject.put("UserId", CommonUtils.getUserId(RadioListActivity.this));
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

	// 初始化界面
	private void setView() {
        findViewById(R.id.head_left_btn).setOnClickListener(this);

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
