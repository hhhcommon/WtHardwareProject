package com.wotingfm.ui.interphone.message.handlegroupapply.adapter;

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
import com.wotingfm.common.config.GlobalConfig;
import com.wotingfm.ui.common.model.UserInfo;
import com.wotingfm.util.AssembleImageUrlUtils;
import com.wotingfm.util.BitmapUtils;

import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.List;

public class HandleGroupApplyAdapter extends BaseAdapter implements OnClickListener{
	private List<UserInfo> list;
	private Context context;
	private UserInfo Inviter;
	private Callback mCallback;
	private SimpleDateFormat format;
	public interface Callback {
		public void click(View v);
	}

	public HandleGroupApplyAdapter(Context context, List<UserInfo> list,
								   Callback callback) {
		super();
		this.list = list;
		this.context = context;
		this.mCallback = callback;
		format = new SimpleDateFormat("yyyy-MM-dd HH:mm");
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
			holder = new ViewHolder();
			convertView = LayoutInflater.from(context).inflate(R.layout.adapter_userinviteme, null);
			holder.textview_invitename = (TextView) convertView.findViewById(R.id.tv_invitemeusername);// 邀请我的人名
			holder.textview_invitemessage = (TextView) convertView.findViewById(R.id.tv_invitemeusermessage);// 申请消息
			holder.imageview_inviteimage = (ImageView) convertView.findViewById(R.id.imageView_inviter);// 该人头像
			holder.textview_invitestauswait = (TextView) convertView.findViewById(R.id.textView_repeatstatus2);
			holder.textview_invitestausyes = (TextView) convertView.findViewById(R.id.textView_repeatstatus);
			holder.img_zhezhao = (ImageView) convertView.findViewById(R.id.img_zhezhao);
			Bitmap bmp_zhezhao = BitmapUtils.readBitMap(context, R.mipmap.wt_6_b_y_b);
			holder.img_zhezhao.setImageBitmap(bmp_zhezhao);
			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}
		Inviter = list.get(position);

		if (Inviter.getUserName() == null || Inviter.getUserName().equals("")) {
			holder.textview_invitename.setText("未知");
		} else {
			holder.textview_invitename.setText(Inviter.getUserName());
		}
		if (Inviter.getApplyTime() == null || Inviter.getApplyTime().equals("")) {
			holder.textview_invitemessage.setText("申请进入该群");
		} else {
			String time = format.format(new Date(Long.parseLong(Inviter.getApplyTime())));
			
			holder.textview_invitemessage.setText("于"+time+"申请进入该群");
		}
		if (Inviter.getPortraitMini() == null || Inviter.getPortraitMini().equals("")
				|| Inviter.getPortraitMini().equals("null") || Inviter.getPortraitMini().trim().equals("")) {
			holder.imageview_inviteimage.setImageResource(R.mipmap.wt_image_tx_hy);
		} else {
			String url;
			if(Inviter.getPortraitMini().startsWith("http:")){
				url=Inviter.getPortraitMini();
			}else{
				url = GlobalConfig.imageurl+Inviter.getPortraitMini();
			}
			url= AssembleImageUrlUtils.assembleImageUrl150(url);
			Picasso.with(context).load(url.replace("\\/", "/")).into(holder.imageview_inviteimage);
		}
		if (Inviter.getType() == 1) {
			holder.textview_invitestauswait.setVisibility(View.VISIBLE);
			holder.textview_invitestausyes.setVisibility(View.GONE);
			holder.textview_invitestauswait.setOnClickListener(this);
			holder.textview_invitestauswait.setTag(position);
		} else if (Inviter.getType() == 2) {
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
		public ImageView img_zhezhao;
	}

	@Override
	public void onClick(View v) {
		mCallback.click(v);
	}
}
