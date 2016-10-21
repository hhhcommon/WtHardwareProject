package com.wotingfm.activity.music.program.diantai.adapter;

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
import com.wotingfm.activity.music.program.fmlist.model.RankInfo;
import com.wotingfm.util.BitmapUtils;

import java.util.List;

public class citynewsadapter extends BaseAdapter {
	private List<RankInfo> list;
	private Context context;

	public citynewsadapter(Context context, List<RankInfo> list) {
		this.context = context;
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
		ViewHolder holder;
		if (convertView == null) {
			holder = new ViewHolder();
			convertView = LayoutInflater.from(context).inflate(R.layout.adapter_fragment_radio_grid, parent, false);
			holder.textRankTitle = (TextView) convertView.findViewById(R.id.tv_name);          // 台名
			holder.imageRankImage = (ImageView) convertView.findViewById(R.id.RankImageUrl);   // 电台图标
			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}
		RankInfo lists = list.get(position);
		holder.textRankTitle.setText(lists.getContentName());
		if (lists.getContentImg() == null || lists.getContentImg().equals("")
				|| lists.getContentImg().equals("null") || lists.getContentImg().trim().equals("")) {
			
			Bitmap bmp = BitmapUtils.readBitMap(context, R.mipmap.wt_image_playertx);
			holder.imageRankImage.setImageBitmap(bmp);
		} else {
			String url = lists.getContentImg();
			Picasso.with(context).load(url.replace("\\/", "/")).resize(100, 100).centerCrop().into(holder.imageRankImage);
		}
		return convertView;
	}

	private class ViewHolder {
		public ImageView imageRankImage;
		public TextView textRankTitle;
	}
}
