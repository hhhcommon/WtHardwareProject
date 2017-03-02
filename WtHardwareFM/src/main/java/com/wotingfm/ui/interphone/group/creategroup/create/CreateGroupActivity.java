package com.wotingfm.ui.interphone.group.creategroup.create;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.LinearLayout;

import com.android.volley.VolleyError;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.wotingfm.R;
import com.wotingfm.common.config.GlobalConfig;
import com.wotingfm.common.volley.VolleyCallback;
import com.wotingfm.common.volley.VolleyRequest;
import com.wotingfm.ui.baseactivity.BaseActivity;
import com.wotingfm.ui.interphone.group.creategroup.CreateGroupContentActivity;
import com.wotingfm.ui.interphone.group.creategroup.model.Freq;
import com.wotingfm.util.ToastUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

/**
 * 创建群主页
 * @author 辛龙
 * 2016年5月16日
 */
public class CreateGroupActivity extends BaseActivity implements OnClickListener {
	private LinearLayout lin_groupmain_first;
	private LinearLayout lin_groupmain_second;
	private LinearLayout lin_groupmain_third;
	private String tag = "CREATE_MAIN_GET_VOLLEY_REQUEST_CANCEL_TAG";
	private boolean isCancelRequest;
	private List<Freq> freqList;
	private boolean getFreq;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_create_group_main);
		if (GlobalConfig.CURRENT_NETWORK_STATE_TYPE != -1) {
			sendFreq();
		} else {
			ToastUtils.show_always(context, "网络失败，请检查网络");
		}
		setView();
		setListener();
	}

	// 获取对讲的频率，存储在Manifest当中
	private void sendFreq() {
		JSONObject jsonObject = VolleyRequest.getJsonObject(context);
		try {
			jsonObject.put("CatalogType", "11");
			jsonObject.put("ResultType", "2");
			jsonObject.put("RelLevel", "0");
			jsonObject.put("Page", "1");
		} catch (JSONException e) {
			e.printStackTrace();
		}

		VolleyRequest.RequestPost(GlobalConfig.getCatalogUrl, tag, jsonObject, new VolleyCallback() {
			@Override
			protected void requestSuccess(JSONObject result) {
				if (isCancelRequest) return;
				try {
					String ReturnType = result.getString("ReturnType");
					//Log.v("ReturnType", "ReturnType -- > > " + ReturnType);
					if(!TextUtils.isEmpty(ReturnType)){
						if (ReturnType.equals("1001") || ReturnType.equals("10011")) {
							String ResultList = result.getString("CatalogData");
							freqList = new Gson().fromJson(ResultList, new TypeToken<List<Freq>>() {}.getType());
							GlobalConfig.FreqList=freqList;
							getFreq=true;
						} else {
							ToastUtils.show_always(context, "获取对讲频率失败");
						}
					}else{
						ToastUtils.show_always(context, "获取对讲频率失败");
					}
				} catch (JSONException e) {
					e.printStackTrace();
				}
			}

			@Override
			protected void requestError(VolleyError error) {
				ToastUtils.showVolleyError(context);
			}
		});

	}


	private void setListener() {
        findViewById(R.id.head_left_btn).setOnClickListener(this);
		lin_groupmain_first.setOnClickListener(this);
		lin_groupmain_second.setOnClickListener(this);
		lin_groupmain_third.setOnClickListener(this);
	}

	private void setView() {
		lin_groupmain_first = (LinearLayout) findViewById(R.id.lin_groupmain_first);
		lin_groupmain_second = (LinearLayout) findViewById(R.id.lin_groupmain_second);
		lin_groupmain_third = (LinearLayout) findViewById(R.id.lin_groupmain_third);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.head_left_btn:
			finish();
			break;
		case R.id.lin_groupmain_first:
			if(getFreq){
			Bundle bundle = new Bundle();
			bundle.putString("Type", "Open");
			Intent intent = new Intent(CreateGroupActivity.this,CreateGroupContentActivity.class);
			intent.putExtras(bundle);
			startActivityForResult(intent, 1);
			}else{
				ToastUtils.show_always(context,"当前网络情况不佳，暂时无法为您创建群组");
			}
			break;
		case R.id.lin_groupmain_second:
//			ToastUtil.show_short(context, "密码群");
			if(getFreq){
			Bundle bundle1 = new Bundle();
			bundle1.putString("Type", "PassWord");
			Intent intent1 = new Intent(CreateGroupActivity.this,CreateGroupContentActivity.class);
			intent1.putExtras(bundle1);
			startActivity(intent1);
			}else{
			ToastUtils.show_always(context,"当前网络情况不佳，暂时无法为您创建群组");
			}
			break;
		case R.id.lin_groupmain_third:
//			ToastUtil.show_short(context, "验证群");
			if(getFreq){
			Bundle bundle2 = new Bundle();
			bundle2.putString("Type", "Validate");
			Intent intent2 = new Intent(CreateGroupActivity.this,CreateGroupContentActivity.class);
			intent2.putExtras(bundle2);
			startActivity(intent2);
			}else{
			ToastUtils.show_always(context,"当前网络情况不佳，暂时无法为您创建群组");
			}
			break;
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		switch (requestCode) {
		case 1:
			if(resultCode==1){
				finish();
			}
			break;
		}
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		context = null;
		lin_groupmain_first = null;
		lin_groupmain_second = null;
		lin_groupmain_third = null;
		setContentView(R.layout.activity_null);
		isCancelRequest = VolleyRequest.cancelRequest(tag);
	}


}
