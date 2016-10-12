package com.wotingfm.activity.person.login.activity;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.VolleyError;
import com.wotingfm.R;
import com.wotingfm.activity.person.forgetpassword.activity.ForgetPasswordActivity;
import com.wotingfm.activity.person.register.activity.RegisterActivity;
import com.wotingfm.common.config.GlobalConfig;
import com.wotingfm.common.constant.StringConstant;
import com.wotingfm.common.volley.VolleyCallback;
import com.wotingfm.common.volley.VolleyRequest;
import com.wotingfm.helper.InterPhoneControlHelper;
import com.wotingfm.manager.SharePreferenceManager;
import com.wotingfm.util.DialogUtils;
import com.wotingfm.util.ToastUtils;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

/**
 * 登录界面
 * @author 辛龙
 *  2016年2月23日
 */
public class LoginActivity extends Activity implements OnClickListener {
	private EditText edittext_username;
	private EditText edittext_password;
	private TextView tv_wjmm;
	private TextView btn_login;
	private TextView btn_register;
	private LinearLayout pubBtn;

	private String username;
	private String password;
	private Dialog dialog;
	private LoginActivity context;

	//	private int type = -1;// 标记从哪个页面来的
	private String tag = "LOGIN_VOLLEY_REQUEST_CANCEL_TAG";
	private boolean isCancelRequest;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_login);
		context = this;
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);		// 透明状态栏
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);	// 透明导航栏
		setView();
		setlistener();
	}

	//初始化视图
	private void setView(){
		pubBtn = (LinearLayout) findViewById(R.id.head_left_btn);	            // 返回按钮
		edittext_username = (EditText) findViewById(R.id.edittext_username);	// 输入用户名
		edittext_password = (EditText) findViewById(R.id.edittext_password);	// 输入密码按钮
		tv_wjmm = (TextView) findViewById(R.id.tv_wjmm);						// 忘记密码
		btn_login = (TextView) findViewById(R.id.btn_login);					// 登录按钮
		btn_register = (TextView) findViewById(R.id.btn_register);				// 注册按钮

		String phoneName = (String) SharePreferenceManager.getSharePreferenceValue(context, "USER_NAME", "USER_NAME", "");
		edittext_username.setText(phoneName);

	}

	private void setlistener() {
		pubBtn.setOnClickListener(this);
		tv_wjmm.setOnClickListener(this);
		btn_login.setOnClickListener(this);
		btn_register.setOnClickListener(this);
	}

	@Override
	protected void onResume() {
		super.onResume();
		handleIntent();
	}

	private void handleIntent() {
		//type = context.getIntent().getIntExtra("type", -1);
		String phonenum = context.getIntent().getStringExtra("phonenum");
		if (phonenum != null && !phonenum.equals("")) edittext_username.setText(phonenum);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.head_left_btn:
			finish();
			break;
		case R.id.btn_login:
			checkdata();		// 验证数据
			break;
		case R.id.btn_register:
			Intent intent = new Intent(this, RegisterActivity.class);
			startActivityForResult(intent, 0);		// 跳转到注册界面
			break;
		case R.id.tv_wjmm:
			Intent PasswordIntent = new Intent(this, ForgetPasswordActivity.class);
			startActivity(PasswordIntent);			// 跳转到忘记密码界面
			break;
		}
	}

	private void checkdata() {
		// 验证数据
		username = edittext_username.getText().toString().trim();
		password = edittext_password.getText().toString().trim();
		if (username == null || username.trim().equals("")) {
			Toast.makeText(this, "用户名不能为空", Toast.LENGTH_SHORT).show();
			// 验证失败，返回
			return;
		}
		if (password == null || password.trim().equals("")) {
			Toast.makeText(this, "密码不能为空", Toast.LENGTH_SHORT).show();
			// 验证失败，返回
			return;
		}
		// 验证成功提交数据
		if (GlobalConfig.CURRENT_NETWORK_STATE_TYPE != -1) {
			dialog = DialogUtils.Dialogph(context, "登录中");
			send();
		} else {
			ToastUtils.show_allways(this, "网络失败，请检查网络");
		}
	}

	private void send() {
		JSONObject jsonObject = VolleyRequest.getJsonObject(context);
		try {
			jsonObject.put("UserName", username);
			jsonObject.put("Password", password);
		} catch (JSONException e) {
			e.printStackTrace();
		}

		VolleyRequest.RequestPost(GlobalConfig.loginUrl, tag, jsonObject, new VolleyCallback() {
			private String SessionId;
			private String ReturnType;
			private String Message;

			@Override
			protected void requestSuccess(JSONObject result) {
				if (dialog != null) {
					dialog.dismiss();
				}
				if(isCancelRequest){
					return ;
				}
				try {
					ReturnType = result.getString("ReturnType");
					//					SessionId = result.getString("SessionId");
					Message = result.getString("Message");
				} catch (Exception e1) {
					e1.printStackTrace();
				}
				try {
					SessionId = result.getString("SessionId");
				} catch (JSONException e2) {
					e2.printStackTrace();
				}
				try {
					Message = result.getString("Message");
				} catch (JSONException e1) {
					e1.printStackTrace();
				}
				if (ReturnType != null && ReturnType.equals("1001")) {
					String imageurl=null;
					String imageurlbig=null;
					String userid=null;
					String returnusername=null;
					String phonenumber=null;
					try {
						String userinfo = result.getString("UserInfo");
						JSONTokener jsonParser = new JSONTokener(userinfo);
						JSONObject arg1 = (JSONObject) jsonParser.nextValue();
						returnusername = arg1.getString("UserName");
						userid = arg1.getString("UserId");
						imageurl = arg1.getString("PortraitMini");
						imageurlbig = arg1.getString("PortraitBig");
						phonenumber=arg1.getString("PhoneNum");
					} catch (Exception e) {
						e.printStackTrace();
					}
					// 通过shareperfrence存储用户的登录信息
					SharedPreferences sp = getSharedPreferences("wotingfm", Context.MODE_PRIVATE);
					Editor et = sp.edit();
					et.putString(StringConstant.USERID, userid);
					et.putString(StringConstant.ISLOGIN, "true");
					et.putString(StringConstant.USERNAME, returnusername);
                    et.putString(StringConstant.PHONENUMBER, phonenumber);
					et.putString(StringConstant.IMAGEURL, imageurl);
					et.putString(StringConstant.IMAGEURBIG, imageurlbig);
					et.putString(StringConstant.PERSONREFRESHB, "true");
					et.commit();
					context.sendBroadcast(new Intent("push_refreshlinkman"));
					//刷新下载界面
					context.sendBroadcast(new Intent("push_down_completed"));
					setResult(1);
					String phoneName = edittext_username.getText().toString().trim();
					SharePreferenceManager.saveBatchSharedPreference(context, "USER_NAME", "USER_NAME", phoneName);
					InterPhoneControlHelper.sendEntryMessage(context);
					finish();
				} else if (ReturnType != null && ReturnType.equals("1002")) {
					ToastUtils.show_allways(context, "服务器端无此用户");
				} else if (ReturnType != null && ReturnType.equals("1003")) {
					ToastUtils.show_allways(context, "密码错误");
				} else if (ReturnType != null && ReturnType.equals("0000")) {
					ToastUtils.show_allways(context, "发生未知错误，请稍后重试");
				} else if (ReturnType != null && ReturnType.equals("T")) {
					ToastUtils.show_allways(context, "发生未知错误，请稍后重试");
				} else {
					if (Message != null && !Message.trim().equals("")) {
						ToastUtils.show_allways(context, Message + "");
					}
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

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		switch (requestCode) {
		case 0: // 从注册界面返回数据，注册成功
			if (resultCode == 1) {
				setResult(1);
				finish();
			}
			break;
		}
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		isCancelRequest = VolleyRequest.cancelRequest(tag);
		edittext_username = null;
		edittext_password = null;
		tv_wjmm = null;
		btn_login = null;
		pubBtn=null;
		btn_register = null;
		username = null;
		password = null;
		dialog = null;
		context = null;
		tag = null;
		setContentView(R.layout.activity_null);
	}
}
