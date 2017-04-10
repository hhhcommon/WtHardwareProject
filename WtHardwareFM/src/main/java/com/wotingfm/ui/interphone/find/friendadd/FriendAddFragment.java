package com.wotingfm.ui.interphone.find.friendadd;

import android.app.Dialog;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.android.volley.VolleyError;
import com.squareup.picasso.Picasso;
import com.wotingfm.R;
import com.wotingfm.common.application.BSApplication;
import com.wotingfm.common.config.GlobalConfig;
import com.wotingfm.common.volley.VolleyCallback;
import com.wotingfm.common.volley.VolleyRequest;
import com.wotingfm.ui.interphone.main.DuiJiangActivity;
import com.wotingfm.ui.interphone.model.UserInviteMeInside;
import com.wotingfm.util.AssembleImageUrlUtils;
import com.wotingfm.util.DialogUtils;
import com.wotingfm.util.ToastUtils;
import com.wotingfm.widget.TipView;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * 添加好友详情界面
 * @author 辛龙
 * 2016年1月20日
 */
public class FriendAddFragment extends Fragment implements OnClickListener {
	private TextView tv_add;
	private TextView tv_name;
	private TextView tv_id;
	private TextView tv_sign;
	private EditText et_news;
	private Dialog dialog;
	private SharedPreferences sharedPreferences= BSApplication.SharedPreferences;
	private String username;
	private ImageView image_touxiang;
	private LinearLayout lin_delete;
	private UserInviteMeInside contact;
	private String tag = "FRIEND_ADD_VOLLEY_REQUEST_CANCEL_TAG";
	private boolean isCancelRequest;

    private TipView tipView;// 数据错误提示
	private FragmentActivity context;
	private View rootView;


	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		if (rootView == null) {
			rootView = inflater.inflate(R.layout.activity_friendadds, container, false);
			rootView.setOnClickListener(this);
			context = getActivity();
			contact = (UserInviteMeInside) getArguments().getSerializable("contact");
			setView();		// 设置界面
			setListener();	// 设置监听
			if(contact != null){
				setValue();
			} else {
				tipView.setVisibility(View.VISIBLE);
				tipView.setTipView(TipView.TipStatus.IS_ERROR);
			}
		}
		return rootView;
	}

	private void setView() {
		lin_delete = (LinearLayout) rootView.findViewById(R.id.lin_delete);//验证信息清空
		et_news= (EditText) rootView.findViewById(R.id.et_news);//验证信息输入框
		image_touxiang = (ImageView) rootView.findViewById(R.id.image_touxiang);//头像
		tv_name = (TextView) rootView.findViewById(R.id.tv_name);//姓名
		tv_id = (TextView) rootView.findViewById(R.id.tv_id);//id号
		tv_sign = (TextView) rootView.findViewById(R.id.tv_sign);
		tv_add = (TextView) rootView.findViewById(R.id.tv_add);//添加好友

        tipView = (TipView) rootView.findViewById(R.id.tip_view);
	}

	private void setValue() {
		// 数据适配
		if(contact.getNickName()==null||contact.getNickName().equals("")){
			tv_name.setText("未知");
		}else{
			tv_name.setText(contact.getNickName());
		}
		if(contact.getUserNum()==null||contact.getUserNum().equals("")){
			tv_id.setVisibility(View.GONE);
		}else{
			tv_id.setVisibility(View.VISIBLE);
			tv_id.setText("ID: " + contact.getUserNum());
		}
		if(contact.getUserSign()==null||contact.getUserSign().equals("")){
			tv_sign.setVisibility(View.GONE);
		}else{
			tv_sign.setVisibility(View.VISIBLE);
			tv_sign.setText(contact.getUserSign());
		}
		if(contact.getPortraitMini()==null||contact.getPortraitMini().equals("")||contact.getPortraitMini().equals("null")||contact.getPortraitMini().trim().equals("")){
			image_touxiang.setImageResource(R.mipmap.wt_image_tx_hy);
		}else{
			String url;
			if(contact.getPortraitMini().startsWith("http:")){
				url=contact.getPortraitMini();
			}else{
				url = GlobalConfig.imageurl+contact.getPortraitMini();
			}
			url= AssembleImageUrlUtils.assembleImageUrl150(url);
			Picasso.with(context).load(url.replace("\\/", "/")).resize(100, 100).centerCrop().into(image_touxiang);
		}
		if(username==null||username.equals("")){
			et_news.setText("");
		}else{
			et_news.setText("我是 "+username);
		}
	}

	private void setListener() {
		rootView.findViewById(R.id.head_left_btn).setOnClickListener(this);
		tv_add.setOnClickListener(this);
		lin_delete.setOnClickListener(this);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.head_left_btn:
			DuiJiangActivity.close();
			break;
		case R.id.lin_delete:// 验证信息清空
			et_news.setText("");
			break;
		case R.id.tv_add:// 点击申请添加按钮
			String news = et_news.getText().toString().trim();
			if(news.equals("")){
				ToastUtils.show_always(context, "请输入验证信息");
			}else{
				if (GlobalConfig.CURRENT_NETWORK_STATE_TYPE != -1) {
					dialog = DialogUtils.Dialogph(context, "申请中");
					sendRequest();
				} else {
					ToastUtils.show_always(context,"网络连接失败，请稍后重试");
				}
			}
			break;
		}
	}

	private void sendRequest(){
		JSONObject jsonObject = VolleyRequest.getJsonObject(context);
		try {
			jsonObject.put("BeInvitedUserId", contact.getUserId());
			jsonObject.put("InviteMsg", et_news.getText().toString().trim());
		} catch (JSONException e) {
			e.printStackTrace();
		}
		VolleyRequest.RequestPost(GlobalConfig.sendInviteUrl, tag, jsonObject, new VolleyCallback() {
			private String ReturnType;
			private String Message;

			@Override
			protected void requestSuccess(JSONObject result) {
				if (dialog != null) dialog.dismiss();
				if(isCancelRequest) return ;
				try {
					ReturnType = result.getString("ReturnType");
					Message = result.getString("Message");
				} catch (JSONException e) {
					e.printStackTrace();
				}
				if (ReturnType != null && ReturnType.equals("1001")) {
					ToastUtils.show_always(context, "验证发送成功，等待好友审核" );
				}else if (ReturnType != null && ReturnType.equals("1002")) {
					ToastUtils.show_always(context, "添加失败, 请稍后再试 ");
				} else if (ReturnType != null && ReturnType.equals("T")) {
					ToastUtils.show_always(context, "添加失败, 请稍后再试 ");
				} else if (ReturnType != null && ReturnType.equals("200")) {
					ToastUtils.show_always(context, "您未登录 ");
				} else if (ReturnType != null && ReturnType.equals("0000")) {
					ToastUtils.show_always(context, "添加失败, 请稍后再试 ");
				} else if (ReturnType != null && ReturnType.equals("1003")) {
					ToastUtils.show_always(context, "添加好友不存在 ");
				} else if (ReturnType != null && ReturnType.equals("1004")) {
					ToastUtils.show_always(context, "您已经是他好友了 ");
				} else if (ReturnType != null && ReturnType.equals("1005")) {
					ToastUtils.show_always(context, "对方已经邀请您为好友了，请查看 ");
				} else if (ReturnType != null && ReturnType.equals("1006")) {
					ToastUtils.show_always(context, "添加失败, 请稍后再试 ");
				} else if (ReturnType != null && ReturnType.equals("1007")) {
					ToastUtils.show_always(context, "您已经添加过了 ");
				} else {
					if (Message != null && !Message.trim().equals("")) {
						ToastUtils.show_always(context, Message + "");
					}else{
						ToastUtils.show_always(context, "添加失败, 请稍后再试 ");
					}
				}
			}
			
			@Override
			protected void requestError(VolleyError error) {
				if (dialog != null) dialog.dismiss();
                ToastUtils.showVolleyError(context);
			}
		});
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		isCancelRequest = VolleyRequest.cancelRequest(tag);
		lin_delete = null;
		et_news = null;
		image_touxiang = null;
		tv_name = null;
		tv_id = null;
		tv_sign = null;
		tv_add = null;
		sharedPreferences = null;
		context = null;
		dialog = null;
		username = null;
		contact = null;
		tag = null;
	}
}
