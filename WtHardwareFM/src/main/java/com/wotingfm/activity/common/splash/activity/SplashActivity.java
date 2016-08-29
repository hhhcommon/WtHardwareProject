package com.wotingfm.activity.common.splash.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.widget.ImageView;

import com.android.volley.VolleyError;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.wotingfm.R;
import com.wotingfm.activity.common.main.MainActivity;
import com.wotingfm.activity.common.splash.model.UserInfo;
import com.wotingfm.common.config.GlobalConfig;
import com.wotingfm.common.constant.StringConstant;
import com.wotingfm.common.volley.VolleyCallback;
import com.wotingfm.common.volley.VolleyRequest;
import com.wotingfm.util.BitmapUtils;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * 启动页面，第一个activity
 * 作者：xinlong on 2016/8/23 21:18
 * 邮箱：645700751@qq.com
 */
public class SplashActivity extends Activity {
	private SharedPreferences sharedPreferences;
	private String first;
//	private Dialog dialog;
	private Bitmap bmp;
	private String tag = "SPLASH_VOLLEY_REQUEST_CANCEL_TAG";
	private boolean isCancelRequest;
	private ImageView imageView;
	/**
	 * ATTENTION: This was auto-generated to implement the App Indexing API.
	 * See https://g.co/AppIndexing/AndroidStudio for more information.
	 */
	private GoogleApiClient client;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_splash);
		imageView = (ImageView) findViewById(R.id.imageView1);
		bmp = BitmapUtils.readBitMap(SplashActivity.this, R.mipmap.splash);
		imageView.setImageBitmap(bmp);
		sharedPreferences = this.getSharedPreferences("wotingfm", Context.MODE_PRIVATE);
		first = sharedPreferences.getString(StringConstant.FIRST, "0");//是否是第一次登录
		Editor et = sharedPreferences.edit();
		et.putString(StringConstant.PERSONREFRESHB, "true");
		et.commit();
		new Handler().postDelayed(new Runnable() {
			@Override
			public void run() {
				send();
			}
		}, 1000);
		// ATTENTION: This was auto-generated to implement the App Indexing API.
		// See https://g.co/AppIndexing/AndroidStudio for more information.
		client = new GoogleApiClient.Builder(this).addApi(AppIndex.API).build();
	}

	protected void send() {
		// 获取请求网络公共属性
		JSONObject jsonObject = VolleyRequest.getJsonObject(SplashActivity.this);
		VolleyRequest.RequestPost(GlobalConfig.splashUrl, tag, jsonObject, new VolleyCallback() {
			private String ReturnType;
			private String SessionId;
			private String UserInfos;

			@Override
			protected void requestSuccess(JSONObject result) {
				if (isCancelRequest) {
					return;
				}
				try {
					ReturnType = result.getString("ReturnType");
					SessionId = result.getString("SessionId");
				} catch (JSONException e) {
					e.printStackTrace();
				}
				try {
					UserInfos = result.getString("UserInfo");
				} catch (JSONException e) {
					e.printStackTrace();
				}
				if (ReturnType.equals("1001")) {
					Editor et = sharedPreferences.edit();
					et.putString(StringConstant.SESSIONID, SessionId);
					if (UserInfos == null || UserInfos.trim().equals("")) {
						et.putString(StringConstant.USERID, "userid");
						et.putString(StringConstant.USERNAME, "username");
						et.putString(StringConstant.IMAGEURL, "imageurl");
						et.putString(StringConstant.IMAGEURBIG, "imageurlbig");
						et.commit();
					} else {
						UserInfo list = new Gson().fromJson(UserInfos, new TypeToken<UserInfo>() {}.getType());
						String userid = list.getUserId();
						String username = list.getUserName();
						String imageurl = list.getPortraitMini();
						String imageurlbig = list.getPortraitBig();
						et.putString(StringConstant.USERID, userid);
						et.putString(StringConstant.IMAGEURL, imageurl);
						et.putString(StringConstant.IMAGEURBIG, imageurlbig);
						et.putString(StringConstant.USERNAME, username);
						et.commit();
					}
				}
				if (first != null && first.equals("1")) {
					startActivity(new Intent(SplashActivity.this, MainActivity.class));//跳转到主页
				} else {
					startActivity(new Intent(SplashActivity.this, MainActivity.class));//跳转到引导页
				}
				//				overridePendingTransition(R.anim.wt_fade, R.anim.wt_hold);
				//				overridePendingTransition(R.anim.wt_zoom_enter, R.anim.wt_zoom_exit);
				finish();
			}

			@Override
			protected void requestError(VolleyError error) {
				if (first != null && first.equals("1")) {
					startActivity(new Intent(SplashActivity.this, MainActivity.class));//跳转到主页
				} else {
					startActivity(new Intent(SplashActivity.this, MainActivity.class));//跳转到引导页
				}
//				overridePendingTransition(R.anim.wt_fade, R.anim.wt_hold);
//				overridePendingTransition(R.anim.wt_zoom_enter, R.anim.wt_zoom_exit);
				finish();
			}
		});
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		isCancelRequest = VolleyRequest.cancelRequest(tag);
		sharedPreferences = null;
		first = null;
		tag = null;
		imageView.setImageBitmap(null);
		if (bmp != null && !bmp.isRecycled()) {
			bmp.recycle();
			bmp = null;
		}
		sharedPreferences = null;
		imageView = null;
		setContentView(R.layout.activity_null_view);
	}
}
