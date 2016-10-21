package com.wotingfm.activity.music.program.fmlist.adapter;

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

// 这个代码写完了 然后要求对应着代码加载看看
public class RankInfoAdapter extends BaseAdapter   {
	private List<RankInfo> list;
	private Context context;

	public RankInfoAdapter(Context context, List<RankInfo> list) {
		super();
		this.list = list;
		this.context = context;
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
			convertView = LayoutInflater.from(context).inflate(R.layout.adapter_rankinfo, parent, false);
			holder.textRankTitle = (TextView) convertView.findViewById(R.id.RankTitle);     // 台名
			holder.imageRankImage = (ImageView) convertView.findViewById(R.id.RankImageUrl);// 电台图标
			holder.textNumber = (TextView) convertView.findViewById(R.id.tv_num);
			holder.textRankPlaying=(TextView)convertView.findViewById(R.id.RankPlaying);
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
		if(lists.getContentPub()== null|| lists.getContentPub().equals("")){
			holder.textRankPlaying.setText("未知");
		}else{
			holder.textRankPlaying.setText("正在直播：" + lists.getContentPub());
		}
		if (lists.getContentImg() == null || lists.getContentImg().equals("")
				|| lists.getContentImg().equals("null") || lists.getContentImg().trim().equals("")) {
			Bitmap bmp = BitmapUtils.readBitMap(context, R.mipmap.wt_image_playertx);
			holder.imageRankImage.setImageBitmap(bmp);
		} else {
			String url;
			if(lists.getContentImg().startsWith("http")){
				 url =  lists.getContentImg();
			}else{
				 url = GlobalConfig.imageurl + lists.getContentImg();
			}
			Picasso.with(context).load(url.replace("\\/", "/")).resize(100, 100).centerCrop().into(holder.imageRankImage);
		}
		if (lists.getWatchPlayerNum() == null
				|| lists.getWatchPlayerNum().equals("") || lists.getWatchPlayerNum().equals("null")) {
			holder.textNumber.setText("8000");
		} else {
			holder.textNumber.setText(lists.getWatchPlayerNum());
		}
		return convertView;
	}

	class ViewHolder {
		public ImageView imageRankImage;
		public TextView textRankTitle;
		public TextView textNumber;
		public TextView textRankPlaying;
	}
}
