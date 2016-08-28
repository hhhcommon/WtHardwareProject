package com.wotingfm.activity.common.interphone.groupmanage.groupdetail.activity.adapter;


import android.content.Context;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.shenstec.utils.image.ImageLoader;
import com.wotingfm.R;
import com.wotingfm.activity.common.interphone.groupmanage.allgroupmember.model.UserInfo;
import com.wotingfm.common.config.GlobalConfig;
import com.wotingfm.util.BitmapUtils;

import java.util.List;

public class GroupTalkAdapter extends BaseAdapter {
	private List<UserInfo> list;
	private Context context;
	private ImageLoader imageLoader;
	private UserInfo lists;
	private String url;
	
	public GroupTalkAdapter(Context context,List<UserInfo> list) {
		super();
		this.list = list;
		this.context = context;
		imageLoader=new ImageLoader(context);
	}
	
	public void ChangeData(List<UserInfo> list){
		this.list = list;
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
		ViewHolder holder=null;
		if(convertView==null){
			holder=new ViewHolder();
			convertView = LayoutInflater.from(context).inflate(R.layout.adapter_grouptalk, null);
			holder.tv_name = (TextView)convertView.findViewById(R.id.tv_name);//名
			holder.imageView_touxiang = (ImageView)convertView.findViewById(R.id.image);
			holder.headFrame = (ImageView) convertView.findViewById(R.id.head_frame);
			convertView.setTag(holder);
		}else{
			holder = (ViewHolder) convertView.getTag();
		}
		lists = list.get(position);
		if(lists.getType() == 1){
			holder.tv_name.setVisibility(View.VISIBLE);
			if(lists.getUserAliasName()!=null){
				holder.tv_name.setText(lists.getUserAliasName());
			}else{
				if(lists.getUserName()==null||lists.getUserName().equals("")){
					holder.tv_name.setText("未知");//名
				}else{
					holder.tv_name.setText(lists.getUserName());//名
				}}
			if(lists.getPortraitBig()==null||lists.getPortraitBig().equals("")||lists.getPortraitBig().equals("null")||lists.getPortraitBig().trim().equals("")){
				holder.imageView_touxiang.setImageResource(R.mipmap.wt_image_tx_hy);
			}else{
				if(lists.getPortraitMini().startsWith("http:")){
					url=lists.getPortraitMini();
				}else{
					url = GlobalConfig.imageurl+lists.getPortraitMini();
				}
				imageLoader.DisplayImage(url.replace( "\\/", "/"), holder.imageView_touxiang, false, false, null);
			}
		}else if(lists.getType() == 2){
			holder.tv_name.setText("添加");
			holder.headFrame.setVisibility(View.GONE);		// 08/04  头像的六边形边框  添加和剔除图标本身是六边形图标  所以隐藏其六边形边框
			Bitmap bp=BitmapUtils.readBitMap(context, R.mipmap.image_add);
			holder.imageView_touxiang.setImageBitmap(bp);
		}else{
			holder.tv_name.setText("删除");
			holder.headFrame.setVisibility(View.GONE);		// 08/04  头像的六边形边框  添加和剔除图标本身是六边形图标  所以隐藏其六边形边框
			Bitmap bp= BitmapUtils.readBitMap(context, R.mipmap.image_tichu);
			holder.imageView_touxiang.setImageBitmap(bp);
		}

		return convertView;
	}

	class ViewHolder{
		public ImageView imageView;
		public ImageView imageView_touxiang;
		public TextView tv_name;
		public ImageView headFrame;
	}
}
