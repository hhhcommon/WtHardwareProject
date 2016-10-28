package com.wotingfm.activity.im.interphone.groupmanage.joingrouplist.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;
import com.wotingfm.R;
import com.wotingfm.activity.im.interphone.groupmanage.model.UserInfo;
import com.wotingfm.common.config.GlobalConfig;
import com.wotingfm.util.BitmapUtils;

import java.util.List;

/**
 * 申请加入群组消息列表
 */
public class JoinGroupAdapter extends BaseAdapter implements OnClickListener{
	private List<UserInfo> list;
	private Context context;
	private Callback mCallback;
	private String url;

	public interface Callback {
        void click(View v);
	}

	public JoinGroupAdapter(Context context, List<UserInfo> list, Callback callback) {
		super();
		this.list = list;
		this.context = context;
		this.mCallback = callback;
	}

	public void ChangeData(List<UserInfo> list) {
		this.list = list;
		this.notifyDataSetChanged();
	}
	@Override
	public int getCount() {
		return list.size();
	}

	@Override
	public Object getItem(int position) {
		return list.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(final int position, View convertView, ViewGroup parent) {
		ViewHolder holder ;
		if (convertView == null) {
			convertView = LayoutInflater.from(context).inflate(R.layout.adapter_userinviteme, null);
			holder = new ViewHolder();
			holder.textview_invitename = (TextView) convertView.findViewById(R.id.tv_invitemeusername);// 邀请我的人名
			holder.textview_invitemessage = (TextView) convertView.findViewById(R.id.tv_invitemeusermessage);// 申请消息
			holder.imageview_inviteimage = (ImageView) convertView.findViewById(R.id.imageView_inviter);// 该人头像
			holder.textview_invitestauswait = (TextView) convertView.findViewById(R.id.textView_repeatstatus2);
			holder.textview_invitestausyes = (TextView) convertView.findViewById(R.id.textView_repeatstatus);
			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}
		UserInfo Inviter = list.get(position);
		holder.textview_invitestauswait.setText("同意");
		holder.textview_invitestausyes.setText("已同意");
		if (Inviter.getUserName() == null || Inviter.getUserName().equals("")) {
			holder.textview_invitename.setText("未知");
		} else {
			holder.textview_invitename.setText(Inviter.getUserName());
		}
		holder.textview_invitemessage.setText(Inviter.getInvitedUserName()+"邀请了"+Inviter.getUserName()+"进入群组");
		if (Inviter.getPortraitMini() == null || Inviter.getPortraitMini().equals("")
				|| Inviter.getPortraitMini().equals("null") || Inviter.getPortraitMini().trim().equals("")) {
			Bitmap bmp = BitmapUtils.readBitMap(context, R.mipmap.wt_image_tx_hy);
			holder.imageview_inviteimage.setImageBitmap(bmp);
		} else {
			if(Inviter.getPortraitMini().startsWith("http:")){
				url=Inviter.getPortraitMini();
			}else{
				url = GlobalConfig.imageurl+Inviter.getPortraitMini();
			}
			Picasso.with(context).load(url.replace("\\/", "/")).resize(100, 100).centerCrop().into(holder.imageview_inviteimage);
		}
		if (Inviter.getCheckType() == 1) {
			holder.textview_invitestauswait.setVisibility(View.VISIBLE);
			holder.textview_invitestausyes.setVisibility(View.GONE);
			holder.textview_invitestauswait.setOnClickListener(this);
			holder.textview_invitestauswait.setTag(position);
		} else if (Inviter.getCheckType() == 2) {
			holder.textview_invitestauswait.setVisibility(View.GONE);
			holder.textview_invitestausyes.setVisibility(View.VISIBLE);
		}
		return convertView;
	}

	class ViewHolder {
		public TextView textview_invitename;
		public TextView textview_invitemessage;
		public ImageView imageview_inviteimage;
		public TextView textview_invitestauswait;
		public TextView textview_invitestausyes;
	}

	@Override
	public void onClick(View v) {
		mCallback.click(v);
	}
}
