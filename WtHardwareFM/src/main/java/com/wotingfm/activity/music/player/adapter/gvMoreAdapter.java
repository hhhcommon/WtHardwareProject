package com.wotingfm.activity.music.player.adapter;


import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.wotingfm.R;
import com.wotingfm.activity.music.player.model.ShareModelA;

import java.util.List;


public class gvMoreAdapter extends BaseAdapter {
	private Context context;
	private List<ShareModelA> list;

	public gvMoreAdapter(Context context, List<ShareModelA> list) {
		this.context=context;
		this.list=list;
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
		ViewHolder holder;
		if (convertView == null) {
			convertView=LayoutInflater.from(context).inflate(R.layout.gv_play_more,null);
			holder = new ViewHolder();
			holder.tv=(TextView)convertView.findViewById(R.id.tv_gv);
			holder.img=(ImageView)convertView.findViewById(R.id.img_gv);
			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}
		ShareModelA mshare=list.get(position);
		holder.img.setImageResource(mshare.getShareImageUrl());		
		holder.tv.setText(mshare.getShareText());
		return convertView;
	}

	private class ViewHolder {
		public TextView tv;
		public ImageView img;
	}
}

