package com.wotingfm.activity.music.program.radiolist.adapter;

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
import com.wotingfm.common.config.GlobalConfig;
import com.wotingfm.util.BitmapUtils;

import java.util.List;

public class RadioListAdapter extends BaseAdapter  {
	private List<RankInfo> list;
	private Context context;

	public RadioListAdapter(Context context, List<RankInfo> list) {
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
	public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
		if (convertView == null) {
			holder = new ViewHolder();
			convertView = LayoutInflater.from(context).inflate(R.layout.adapter_item_radiolist, parent, false);
			holder.textRankTitle = (TextView) convertView.findViewById(R.id.RankTitle);// 台名
			holder.imageRank = (ImageView) convertView.findViewById(R.id.RankImageUrl);// 电台图标
			holder.textNumber = (TextView) convertView.findViewById(R.id.tv_num);
			holder.textTime = (TextView) convertView.findViewById(R.id.tv_time);
			holder.textRankPlaying = (TextView) convertView.findViewById(R.id.RankPlaying);
			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}
		RankInfo lists = list.get(position);
		if (lists.getContentName() == null || lists.getContentName().equals("")) {
			holder.textRankTitle.setText("未知");
		} else {
			holder.textRankTitle.setText(lists.getContentName());
		}
		if (lists.getContentImg() == null || lists.getContentImg().equals("")
				|| lists.getContentImg().equals("null")
				|| lists.getContentImg().trim().equals("")) {
			Bitmap bmp = BitmapUtils.readBitMap(context, R.mipmap.wt_bg_noimage);
			holder.imageRank.setImageBitmap(bmp);
		} else {
			String url;
			if(lists.getContentImg().startsWith("http")){
				 url =  lists.getContentImg();
			}else{
				 url = GlobalConfig.imageurl + lists.getContentImg();
			}
			Picasso.with(context).load(url.replace("\\/", "/")).resize(100, 100).centerCrop().into(holder.imageRank);
		}
		if (lists.getPlayCount() == null
				|| lists.getPlayCount().equals("")
				|| lists.getPlayCount().equals("null")) {
			holder.textNumber.setText("8000");
		} else {
			holder.textNumber.setText(lists.getPlayCount());
		}
		
		if (lists.getContentPub() == null
				|| lists.getContentPub().equals("")
				|| lists.getContentPub().equals("null")) {
			holder.textRankPlaying.setText("未知");
		} else {
			holder.textRankPlaying.setText(lists.getContentPub());
		}
		
		//节目时长
		if (lists.getContentTimes() == null
				|| lists.getContentTimes().equals("")
				|| lists.getContentTimes().equals("null")) {
			holder.textTime.setText(context.getString(R.string.play_time));
		} else {
			int minute = Integer.valueOf(lists.getContentTimes()) / (1000 * 60);
			int second = (Integer.valueOf(lists.getContentTimes()) / 1000) % 60;
			if(second < 10){
				holder.textTime.setText(minute + "\'" + " " + "0" + second + "\"");
			}else{
				holder.textTime.setText(minute + "\'" + " " + second + "\"");
			}
		}
		return convertView;
	}

    private class ViewHolder {
		public ImageView imageRank;
		public TextView textRankTitle;
		public TextView textNumber;
		public TextView textTime;
		public TextView textRankPlaying;
    }
}
