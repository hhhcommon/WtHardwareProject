package com.wotingfm.activity.person.modifypassword.activity;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.VolleyError;
import com.shenstec.activity.BaseActivity;
import com.wotingfm.R;
import com.wotingfm.activity.person.login.activity.LoginActivity;
import com.wotingfm.common.config.GlobalConfig;
import com.wotingfm.common.volley.VolleyCallback;
import com.wotingfm.common.volley.VolleyRequest;
import com.wotingfm.manager.MyActivityManager;
import com.wotingfm.util.CommonUtils;
import com.wotingfm.util.DialogUtils;
import com.wotingfm.util.PhoneMessage;
import com.wotingfm.util.ToastUtils;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * 修改密码
 * @author 辛龙
 * 2016年7月19日
 */
public class ModifyPasswordActivity extends BaseActivity {

	private ModifyPasswordActivity context;

	private EditText et_oldpassword;
	private EditText et_newpassword;
	private EditText et_newpassword_confirm;
	private TextView btn_password_modify;
	private LinearLayout lin_oldpassword;
	private LinearLayout lin_back;
	private Dialog dialog;

	private String oldpassword;
	private String newpassword;
	private String passwordconfirm;
	private String userid;
	private String phonenum;
	private String tag = "MODIFY_PASSWORD_VOLLEY_REQUEST_CANCEL_TAG";

	private int ViewType;		// =0时说明来自通过验证过手机号的请求，此时userid来自上一个传入，并且界面要发生改变
	private boolean flag;
	private boolean isCancelRequest;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_modify_password);
		context = this;
		setView();
		handleIntent();
		setListener();
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);		// 透明状态栏
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);	// 透明导航栏
		MyActivityManager mam = MyActivityManager.getInstance();
		mam.pushOneActivity(context);
	}

	private void handleIntent() {
		ViewType = context.getIntent().getIntExtra("origin", 1);
		userid = context.getIntent().getStringExtra("userid");
		phonenum=context.getIntent().getStringExtra("phonenum");
		if (ViewType == 0) {
			lin_oldpassword.setVisibility(View.GONE);
		}
	}

	private void setView() {
		et_oldpassword = (EditText) findViewById(R.id.edit_oldpassword);
		et_newpassword = (EditText) findViewById(R.id.edit_newpassword);
		et_newpassword_confirm = (EditText) findViewById(R.id.edit_confirmpassword);
		btn_password_modify = (TextView) findViewById(R.id.btn_modifypassword);
		lin_back = (LinearLayout) findViewById(R.id.head_left_btn);
		lin_oldpassword = (LinearLayout) findViewById(R.id.lin_oldpassword);
	}

	private void setListener() {
		btn_password_modify.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Boolean result = checkData();
				if (result == true) {
					if (GlobalConfig.CURRENT_NETWORK_STATE_TYPE != -1) {
						if (ViewType != 0) {
							send();
						} else {
							sendmodifypassword();
						}
					} else {
						ToastUtils.show_short(ModifyPasswordActivity.this, "网络连接失败，请稍后重试");
					}
				}
			}
		});
		
		lin_back.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				finish();
			}
		});
	}

	protected void sendmodifypassword() {
		JSONObject jsonObject = new JSONObject();
		try {
			jsonObject.put("SessionId", CommonUtils.getSessionId(this));
			jsonObject.put("MobileClass", PhoneMessage.model + "::" + PhoneMessage.productor);
			jsonObject.put("ScreenSize", PhoneMessage.ScreenWidth + "x" + PhoneMessage.ScreenHeight);
			jsonObject.put("IMEI", PhoneMessage.imei);
			PhoneMessage.getGps(this);
			jsonObject.put("GPS-longitude", PhoneMessage.longitude);
			jsonObject.put("GPS-latitude ", PhoneMessage.latitude);
			jsonObject.put("RetrieveUserId", userid);
			jsonObject.put("PCDType",GlobalConfig.PCDType);
			jsonObject.put("NewPassword", newpassword);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		
		VolleyRequest.RequestPost(GlobalConfig.updatePwd_AfterCheckPhoneOKUrl, tag, jsonObject, new VolleyCallback() {
//			private String SessionId;
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
				} catch (JSONException e) {
					e.printStackTrace();
				}
				if (ReturnType != null && ReturnType.equals("1001")) {
					ToastUtils.show_allways(ModifyPasswordActivity.this, "密码修改成功");
					Intent intent = new Intent(ModifyPasswordActivity.this, LoginActivity.class);
					intent.putExtra("phonenum",phonenum);
					startActivity(intent);
					finish();
				}
				if (ReturnType != null && ReturnType.equals("1002")) {
					ToastUtils.show_allways(ModifyPasswordActivity.this, "" + Message);
				} else {
					if (Message != null && !Message.trim().equals("")) {
						ToastUtils.show_allways(ModifyPasswordActivity.this, Message + "");
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

	protected Boolean checkData() {
		oldpassword = et_oldpassword.getText().toString().trim();
		newpassword = et_newpassword.getText().toString().trim();
		passwordconfirm = et_newpassword_confirm.getText().toString().trim();
		flag = true;
		if (ViewType != 0) {
			if ("".equalsIgnoreCase(oldpassword)) {
				Toast.makeText(this, "请输入您的旧密码", Toast.LENGTH_LONG).show();
				flag = false;
				return flag;
			}
			if ("".equalsIgnoreCase(newpassword)) {
				Toast.makeText(this, "请输入您的新密码", Toast.LENGTH_LONG).show();
				flag = false;
				return flag;
			}
		}
		if (newpassword.length() < 6) {
			Toast.makeText(this, "密码请输入六位以上", Toast.LENGTH_LONG).show();
			flag = false;
			return flag;
		}
		if ("".equalsIgnoreCase(newpassword)) {
			Toast.makeText(this, "请再次输入密码", Toast.LENGTH_LONG).show();
			flag = false;
			return flag;
		}
		if (!newpassword.equals(passwordconfirm)) {
			new AlertDialog.Builder(this).setMessage("两次输入的密码不一致").setPositiveButton("确定", null).show();
			flag = false;
			return flag;
		}
		if (passwordconfirm.length() < 6) {
			Toast.makeText(this, "密码请输入六位以上", Toast.LENGTH_LONG).show();
			flag = false;
			return flag;
		}
		return flag;
	}

	protected void send() {
		dialog = DialogUtils.Dialogph(this, "正在提交请求");
		JSONObject jsonObject = new JSONObject();
		try {
			jsonObject.put("SessionId", CommonUtils.getSessionId(this));
			jsonObject.put("MobileClass", PhoneMessage.model + "::" + PhoneMessage.productor);
			jsonObject.put("ScreenSize", PhoneMessage.ScreenWidth + "x" + PhoneMessage.ScreenHeight);
			jsonObject.put("IMEI", PhoneMessage.imei);
			PhoneMessage.getGps(this);
			jsonObject.put("GPS-longitude", PhoneMessage.longitude);
			jsonObject.put("GPS-latitude ", PhoneMessage.latitude);
			jsonObject.put("UserId", CommonUtils.getUserId(this));
			jsonObject.put("PCDType",GlobalConfig.PCDType);
			jsonObject.put("OldPassword", oldpassword);// 待改
			jsonObject.put("NewPassword", newpassword);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		
		VolleyRequest.RequestPost(GlobalConfig.modifyPasswordUrl, tag, jsonObject, new VolleyCallback() {
//			private String SessionId;
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
				} catch (JSONException e) {
					e.printStackTrace();
				}
				if (ReturnType != null && ReturnType.equals("1001")) {
					ToastUtils.show_allways(ModifyPasswordActivity.this, "密码修改成功");
					finish();
				}
				if (ReturnType != null && ReturnType.equals("1002")) {
					ToastUtils.show_allways(ModifyPasswordActivity.this, "" + Message);
				} else {
					if (Message != null && !Message.trim().equals("")) {
						ToastUtils.show_allways(ModifyPasswordActivity.this, Message + "");
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
	protected void onDestroy() {
		super.onDestroy();
		isCancelRequest = VolleyRequest.cancelRequest(tag);
		MyActivityManager mam = MyActivityManager.getInstance();
		mam.popOneActivity(context);
		et_oldpassword = null;
		et_newpassword = null;
		et_newpassword_confirm = null;
		btn_password_modify = null;
		oldpassword = null;
		newpassword = null;
		passwordconfirm = null;
		dialog = null;
		lin_back = null;
		context = null;
		lin_oldpassword = null;
		userid = null;
		phonenum = null;
		tag = null;
		setContentView(R.layout.activity_null);
	}
}
