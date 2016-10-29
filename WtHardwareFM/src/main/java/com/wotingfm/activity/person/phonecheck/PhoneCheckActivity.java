package com.wotingfm.activity.person.phonecheck;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.android.volley.VolleyError;
import com.wotingfm.R;
import com.wotingfm.activity.common.baseactivity.BaseActivity;
import com.wotingfm.activity.person.modifyphonenumber.ModifyPhoneNumberActivity;
import com.wotingfm.common.config.GlobalConfig;
import com.wotingfm.common.volley.VolleyCallback;
import com.wotingfm.common.volley.VolleyRequest;
import com.wotingfm.util.DialogUtils;
import com.wotingfm.util.ToastUtils;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * 账号绑定==找回密码--变更手机号
 * @author 辛龙
 * 2016年7月19日
 */
public class PhoneCheckActivity extends BaseActivity implements OnClickListener {

	private PhoneCheckActivity context;

	private LinearLayout head_left;
	private EditText et_phonenum;
	private EditText et_yzm;
	private TextView tv_getyzm;
	private TextView tv_next;
	private TextView tv_cxfasong;
	private TextView tv_next_default;


	private Dialog dialog;

	private String phoneNum;
	private String yanzhengma;
	private String tag = "PHONE_CHECK_VOLLEY_REQUEST_CANCEL_TAG";

	private CountDownTimer mCountDownTimer;

	private int sendType = 1;		// sendtype=1 掉发送验证码接口 sendtype=2时调重发验证码接口

	private boolean isCancelRequest;
	private String phoneNumber;


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_phonecheck);
		context = this;
		setview();	// 设置界面
		handleIntent();//接收数据
		setLisener();	// 设置监听

	}

	private void handleIntent() {
		phoneNumber = context.getIntent().getStringExtra("phoneNumber");
	}

	private void setview() {
		head_left = (LinearLayout) findViewById(R.id.head_left_btn);
		et_phonenum = (EditText) findViewById(R.id.et_phonenum);
		et_yzm = (EditText) findViewById(R.id.et_yzm);
		tv_getyzm = (TextView) findViewById(R.id.tv_getyzm);
		tv_next = (TextView) findViewById(R.id.tv_next);
		tv_cxfasong = (TextView) findViewById(R.id.tv_cxfasong);
		tv_next_default = (TextView) findViewById(R.id.tv_next_default);

	}

	private void setLisener() {
		head_left.setOnClickListener(this);
		tv_getyzm.setOnClickListener(this);
		tv_next.setOnClickListener(this);
		et_yzm.addTextChangedListener(new TextWatcher() {
			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
			}
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {
			}
			@Override
			public void afterTextChanged(Editable s) {
				if (s.length() == 6 && phoneNum != null && !phoneNum.equals("")) {
					tv_next_default.setVisibility(View.GONE);
					tv_next.setVisibility(View.VISIBLE);
				} else {
					tv_next.setVisibility(View.GONE);
					tv_next_default.setVisibility(View.VISIBLE);
				}
			}
		});
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.head_left_btn:
			finish();
			break;
		case R.id.tv_getyzm:
			checkYzm();		// 检查手机号是否为空，或者是否是一个正常手机号
			break;
		case R.id.tv_next:
			checkValue();	// 检查输入到页面的信息是否符合接口返回的结果进行验证
			break;
		}
	}


//	@Override
//	protected void onStop() {
//		super.onStop();
//		if(mcountDownTimer!=null){
//			mcountDownTimer.cancel();
//		}
//	}

	private void checkValue() {
		yanzhengma = et_yzm.getText().toString().trim();
		if ("".equalsIgnoreCase(phoneNum)) {
			ToastUtils.show_always(this, "手机号码不能为空");
			return;
		}
		if ("".equalsIgnoreCase(yanzhengma)) {
			ToastUtils.show_always(this, "验证码码不能为空");
			return;
		}
		if (yanzhengma.length() != 6) {
			ToastUtils.show_always(this, "请输入六位验证码");
			return;
		}
		if (GlobalConfig.CURRENT_NETWORK_STATE_TYPE != -1) {
			dialog = DialogUtils.Dialogph(context, "正在验证手机号");
			sendRequest();
		} else {
			ToastUtils.show_short(context, "网络失败，请检查网络");
		}
	}

	private void checkYzm() {
		//检查手机号内容是否为空 检查输入数字是否为手机号 发送网络请求 返回值如果为正常的话 开启线程 每一秒刷新一次一下按钮
		phoneNum = et_phonenum.getText().toString().trim();
		if ("".equalsIgnoreCase(phoneNum)) {
			ToastUtils.show_always(this, "手机号码不能为空");
			return;
		}
		if ("".equalsIgnoreCase(phoneNum) || phoneNum.length() != 11) {// 检查输入数字是否为手机号
			ToastUtils.show_always(context, "请输入正确的手机号码!");
			return;
		}
		if (GlobalConfig.CURRENT_NETWORK_STATE_TYPE != -1) {
			dialog = DialogUtils.Dialogph(context, "正在验证手机号");
			if (sendType == 1) {
				sendFindPassword();
			} else {
				Resend();
			}
		} else {
			ToastUtils.show_short(context, "网络失败，请检查网络");
		}
	}

	private void timerDown() {
		mCountDownTimer = new CountDownTimer(60000, 1000) {
			@Override
			public void onTick(long millisUntilFinished) {
//				if(context==null){
//					if(mcountDownTimer!=null){
//						mcountDownTimer.onFinish();
//					}
//					return;
//				}
				if(/*context!=null&&*/mCountDownTimer!=null&&tv_cxfasong!=null){
				tv_cxfasong.setText(millisUntilFinished / 1000 + "s后重新发送");
				}
			}

			@Override
			public void onFinish() {
//				if(context==null){
//					return;
//				}
				if(tv_cxfasong!=null){
					tv_cxfasong.setVisibility(View.GONE);
				}
				if(tv_getyzm!=null){
					tv_getyzm.setVisibility(View.VISIBLE);
				}
			}
		}.start();
	}

	// 查找密码的相关接口
	private void sendFindPassword() {
		JSONObject jsonObject = VolleyRequest.getJsonObject(context);
		try {
			// 模块属性
			jsonObject.put("PhoneNum", phoneNum);
		} catch (JSONException e) {
			e.printStackTrace();
		}

		VolleyRequest.RequestPost(GlobalConfig.retrieveByPhoneNumUrl, tag, jsonObject, new VolleyCallback() {
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
					Message = result.getString("Message");
				} catch (JSONException e) {
					e.printStackTrace();
				}
				if (ReturnType != null && ReturnType.equals("1001")) {
					ToastUtils.show_always(context, "验证码已经发送");
					sendType = 2;
					timerDown();		// 每秒减1
					et_phonenum.setEnabled(false);
					tv_getyzm.setVisibility(View.GONE);
					tv_cxfasong.setVisibility(View.VISIBLE);
				} else if (ReturnType != null && ReturnType.equals("T")) {
					ToastUtils.show_always(context, "异常返回值");
				} else if (ReturnType != null && ReturnType.equals("1002")) {
					ToastUtils.show_always(context, "此手机号在系统内没有注册");
				} else {
					if (Message != null && !Message.trim().equals("")) {
						ToastUtils.show_always(context, Message + "");
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

	// 再次发送验证码
	private void Resend() {
		JSONObject jsonObject =VolleyRequest.getJsonObject(context);
		try {
			// 模块属性
			jsonObject.put("PhoneNum", phoneNum);
			// OperType
			jsonObject.put("OperType", "1");

		} catch (JSONException e) {
			e.printStackTrace();
		}

		VolleyRequest.RequestPost(GlobalConfig.reSendPhoneCheckCodeNumUrl, tag, jsonObject, new VolleyCallback() {
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
					Message = result.getString("Message");
				} catch (JSONException e) {
					e.printStackTrace();
				}
				if (ReturnType != null && ReturnType.equals("1001")) {
					ToastUtils.show_always(context, "验证码已经再次发送，请查收");
				} else if (ReturnType != null && ReturnType.equals("T")) {
					ToastUtils.show_always(context, "异常返回值");
				} else {
					if (Message != null && !Message.trim().equals("")) {
						ToastUtils.show_always(context, Message + "");
					}
				}
			}

			@Override
			protected void requestError(VolleyError error) {
				if (dialog != null) {
					dialog.dismiss();
					ToastUtils.show_always(context, "VolleyError捕获到异常");
				}
			}
		});
	}

	// 提交数据到服务器进行验证
	private void sendRequest() {
		JSONObject jsonObject = VolleyRequest.getJsonObject(context);
		try {
			// 模块属性
			jsonObject.put("PhoneNum", phoneNum);
			jsonObject.put("CheckCode", yanzhengma);
			jsonObject.put("NeedUserId", "true");

		} catch (JSONException e) {
			e.printStackTrace();
		}

		VolleyRequest.RequestPost(GlobalConfig.checkPhoneCheckCodeUrl, tag, jsonObject, new VolleyCallback() {

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
					Message = result.getString("Message");
				} catch (JSONException e) {
					e.printStackTrace();
				}
				if (ReturnType != null && ReturnType.equals("1001")) {
                   ToastUtils.show_always(context, "验证成功,跳往修改密码界面");
				   Intent intent = new Intent(context, ModifyPhoneNumberActivity.class);
					startActivityForResult(intent,1);
				} else if (ReturnType != null && ReturnType.equals("T")) {
					ToastUtils.show_always(context, "异常返回值");
				} else if (ReturnType != null && ReturnType.equals("1002")) {
					ToastUtils.show_always(context, "验证码不匹配");
				}else {
					if (Message != null && !Message.trim().equals("")) {
						ToastUtils.show_always(context, Message + "");
					}
				}
			}

			@Override
			protected void requestError(VolleyError error) {
				if (dialog != null) {
					dialog.dismiss();
					ToastUtils.show_always(context, "VolleyError捕获到异常");
				}
			}
		});
	}


	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		switch (requestCode) {
			case 1:
				if(resultCode==1){
					finish();
				} break;
		}
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		isCancelRequest = VolleyRequest.cancelRequest(tag);
		if (mCountDownTimer != null) {
			mCountDownTimer.cancel();
			mCountDownTimer = null;
		}
		context = null;
		head_left = null;
		et_phonenum = null;
	    et_yzm = null;
	    tv_getyzm = null;
		tv_next = null;
		phoneNum = null;
		dialog = null;
	    mCountDownTimer = null;
		tv_cxfasong = null;
		yanzhengma = null;
		tv_next_default = null;
		tag = null;
		setContentView(R.layout.activity_null);
	}
}
