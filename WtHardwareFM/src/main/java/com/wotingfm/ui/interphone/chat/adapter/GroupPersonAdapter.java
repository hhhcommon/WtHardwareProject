package com.wotingfm.ui.interphone.chat.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
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

import java.util.List;

public class GroupPersonAdapter extends BaseAdapter{
	private List<UserInfo> list;
	private Context context;
	//	private OnListener onListener;
	private UserInfo lists;
	private Bitmap bmp;
	private Bitmap bmps;
	private Bitmap bmpa;
	private Bitmap bmpb;
	public GroupPersonAdapter(Context context,List<UserInfo> list) {
		super();
		this.list = list;
		this.context = context;
		 bmp = BitmapUtils.readBitMap(context, R.mipmap.wt_6_b_y_b);
		 bmps = BitmapUtils.readBitMap(context, R.mipmap.wt_image_6_gray);
		 bmpa = BitmapUtils.readBitMap(context, R.mipmap.wt_image_tx_hy);
		 bmpb = BitmapUtils.readBitMap(context, R.mipmap.wt_image_tx_hy_gray);
	}
	
	public void ChangeDate(List<UserInfo> list){
		this.list = list;
		this.notifyDataSetChanged();
	}
	//	public void setOnListener(OnListener onListener) {
	//		this.onListener = onListener;
	//	}
	
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
		ViewHolder holder;
		if(convertView==null){
			holder=new ViewHolder();
			convertView=LayoutInflater.from(context).inflate(R.layout.adapter_groupperson, null);
			holder.tv_name = (TextView)convertView.findViewById(R.id.tv_name);
			holder.imageView_touxiang=(ImageView)convertView.findViewById(R.id.image);
			holder.image_bian=(ImageView)convertView.findViewById(R.id.image_bian);
			convertView.setTag(holder);
		}else{
			holder=(ViewHolder) convertView.getTag();
		}
		lists=list.get(position);
		if(lists.getUserName()==null||lists.getUserName().equals("")){
			holder.tv_name.setText("未知");
		}else{
			holder.tv_name.setText(lists.getUserName());
		}
		if(lists.getPortraitBig()==null||lists.getPortraitBig().equals("")||lists.getPortraitBig().equals("null")||lists.getPortraitBig().trim().equals("")){
			if(lists.getOnLine()==2){
				holder.imageView_touxiang.setImageBitmap(bmpa);
			}else{
				holder.imageView_touxiang.setImageBitmap(bmpb);
			}
		}else{
			String url;
			if(lists.getPortraitBig().startsWith("http")){
				url =  lists.getPortraitBig();
			}else{
				url = GlobalConfig.imageurl + lists.getPortraitBig();
			}
			url= AssembleImageUrlUtils.assembleImageUrl150(url);
			Picasso.with(context).load(url.replace("\\/", "/")).into(holder.imageView_touxiang);
		}
		if(lists.getOnLine()==2){
			holder.tv_name.setTextColor(context.getResources().getColor(R.color.dinglan_orange) );
			holder.image_bian.setImageBitmap(bmp);
		}else{
			holder.tv_name.setTextColor(context.getResources().getColor(R.color.darkgray) );
			holder.image_bian.setImageBitmap(bmps);
		}
		//		holder.lin_shanchu.setOnClickListener(new View.OnClickListener() {
		//			public void onClick(View v) {
		//				onListener.shanchu(position);
		//			}
		//		});	
		//		
		return convertView;
	}

	//	public interface OnListener {
	//		public void shanchu(int position);
	//	}
	class ViewHolder{
		public ImageView image_bian;
		public ImageView imageView_touxiang;
		public TextView tv_name;
	}
}
