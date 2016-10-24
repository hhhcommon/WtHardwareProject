package com.wotingfm.activity.music.playhistory.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.squareup.picasso.Picasso;
import com.wotingfm.R;
import com.wotingfm.activity.music.player.model.PlayerHistory;
import com.wotingfm.activity.music.search.model.SuperRankInfo;
import com.wotingfm.util.BitmapUtils;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

public class PlayHistoryExpandableAdapter extends BaseExpandableListAdapter {
	private Context context;
	private List<SuperRankInfo> mSuperRankInfo;

	public PlayHistoryExpandableAdapter(Context context,List<SuperRankInfo> mSuperRankInfo) {
		this.context = context;
		this.mSuperRankInfo = mSuperRankInfo;
	}
	
	@Override
	public int getGroupCount() {
		return mSuperRankInfo.size();
	}

	@Override
	public int getChildrenCount(int groupPosition) {
		return mSuperRankInfo.get(groupPosition).getHistoryList().size();
	}

	@Override
	public Object getGroup(int groupPosition) {
		return mSuperRankInfo.get(groupPosition);
	}

	@Override
	public Object getChild(int groupPosition, int childPosition) {
		return mSuperRankInfo.get(groupPosition).getHistoryList().get(childPosition);
	}

	@Override
	public long getGroupId(int groupPosition) {
		return groupPosition;
	}

	@Override
	public long getChildId(int groupPosition, int childPosition) {
		return childPosition;
	}

	@Override
	public boolean hasStableIds() {
		return false;
	}

	@Override
	public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
		ViewHolder holder;
		if (convertView == null) {
			convertView = LayoutInflater.from(context).inflate(R.layout.adapter_fragment_radio_list, parent, false);
			holder = new ViewHolder();
			holder.textName = (TextView) convertView.findViewById(R.id.tv_name);
			holder.linearMore = (LinearLayout) convertView.findViewById(R.id.lin_head_more);
			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}
		String key = mSuperRankInfo.get(groupPosition).getKey();
		if (key != null && !key.equals("")) {
			if (key.equals("AUDIO")) {
				holder.textName.setText("声音");
			} else if (key.equals("RADIO")) {
				holder.textName.setText("电台");
			} else if (key.equals("TTS")){
				holder.textName.setText("TTS");
			}
		} else {
			holder.textName.setText("我听");
		}
		return convertView;
	}

	@Override
	public View getChildView(int groupPosition, int childPosition,boolean isLastChild, View convertView, ViewGroup parent) {
		ViewHolder holder;
        SimpleDateFormat format;
        String url;
        int a;
		if (convertView == null) {
			holder = new ViewHolder();
			convertView = LayoutInflater.from(context).inflate(R.layout.adapter_play_history, parent, false);
			holder.textViewPlayName = (TextView) convertView.findViewById(R.id.RankTitle);// 节目名称
			holder.textViewPlayIntroduce = (TextView) convertView.findViewById(R.id.tv_last);// 上次播放时长
			holder.imageViewPlayImage = (ImageView) convertView.findViewById(R.id.RankImageUrl);// 节目图片
			holder.textNumber = (TextView) convertView.findViewById(R.id.text_number);
			holder.textRankContent = (TextView) convertView.findViewById(R.id.RankContent);
			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}
		PlayerHistory lists = mSuperRankInfo.get(groupPosition).getHistoryList().get(childPosition);
		if (lists.getPlayerMediaType().equals("RADIO")) {
			if (lists.getPlayerName() == null || lists.getPlayerName().equals("")) {
				holder.textViewPlayName.setText("未知");
			} else {
				holder.textViewPlayName.setText(lists.getPlayerName());
			}
			if (lists.getPlayerNum() == null || lists.getPlayerNum().equals("")) {
				holder.textNumber.setText("8888");
			} else {
				holder.textNumber.setText(lists.getPlayerNum());
			}
			if (lists.getContentPub() == null || lists.getContentPub().equals("")) {
				holder.textRankContent.setText("我听科技");
			} else {
				holder.textRankContent.setText(lists.getContentPub());
			}
			if (lists.getPlayerInTime() == null | lists.getPlayerInTime().equals("")) {
				holder.textViewPlayIntroduce.setText("未知");
			} else {
				format = new SimpleDateFormat("mm:ss", Locale.CHINA);
				format.setTimeZone(TimeZone.getTimeZone("GMT"));
				a = Integer.valueOf(lists.getPlayerInTime());
				String s = format.format(a);
				holder.textViewPlayIntroduce.setText("上次播放至" + s);
			}
			if (lists.getPlayerImage() == null || lists.getPlayerImage().equals("") 
					|| lists.getPlayerImage().equals("null") || lists.getPlayerImage().trim().equals("")) {
				Bitmap bmp = BitmapUtils.readBitMap(context, R.mipmap.wt_image_playertx);
				holder.imageViewPlayImage.setImageBitmap(bmp);
			} else {
				url = lists.getPlayerImage();
				Picasso.with(context).load(url.replace("\\/", "/")).resize(100, 100).centerCrop().into(holder.imageViewPlayImage);
			}
		} else if(lists.getPlayerMediaType().equals("AUDIO")){
			if (lists.getPlayerName() == null || lists.getPlayerName().equals("")) {
				holder.textViewPlayName.setText("未知");
			} else {
				holder.textViewPlayName.setText(lists.getPlayerName());
			}
			if (lists.getPlayerNum() == null || lists.getPlayerNum().equals("")) {
				holder.textNumber.setText("8888");
			} else {
				holder.textNumber.setText(lists.getPlayerNum());
			}
			if (lists.getContentPub() == null || lists.getContentPub().equals("")) {
				holder.textRankContent.setText("我听科技");
			} else {
				holder.textRankContent.setText(lists.getContentPub());
			}
			if (lists.getPlayerInTime() == null | lists.getPlayerInTime().equals("")) {
				holder.textViewPlayIntroduce.setText("未知");
			} else {
				format = new SimpleDateFormat("mm:ss", Locale.CHINA);
				format.setTimeZone(TimeZone.getTimeZone("GMT"));
				a = Integer.valueOf(lists.getPlayerInTime());
				String s = format.format(a);
				holder.textViewPlayIntroduce.setText("上次播放至" + s);
			}
			if (lists.getPlayerImage() == null || lists.getPlayerImage().equals("") 
					|| lists.getPlayerImage().equals("null") || lists.getPlayerImage().trim().equals("")) {
				Bitmap bmp = BitmapUtils.readBitMap(context, R.mipmap.wt_image_playertx);
				holder.imageViewPlayImage.setImageBitmap(bmp);
			} else {
				url = lists.getPlayerImage();
				Picasso.with(context).load(url.replace("\\/", "/")).resize(100, 100).centerCrop().into(holder.imageViewPlayImage);
			}
			
		}else if(lists.getPlayerMediaType().equals("TTS")){
			if (lists.getPlayerName() == null || lists.getPlayerName().equals("")) {
				holder.textViewPlayName.setText("未知");
			} else {
				holder.textViewPlayName.setText(lists.getPlayerName());
			}
			if (lists.getPlayerNum() == null || lists.getPlayerNum().equals("")) {
				holder.textNumber.setText("8888");
			} else {
				holder.textNumber.setText(lists.getPlayerNum());
			}
			if (lists.getContentPub() == null || lists.getContentPub().equals("")) {
				holder.textRankContent.setText("我听科技");
			} else {
				holder.textRankContent.setText(lists.getContentPub());
			}
			if (lists.getPlayerInTime() == null | lists.getPlayerInTime().equals("")) {
				holder.textViewPlayIntroduce.setText("未知");
			} else {
				format = new SimpleDateFormat("mm:ss", Locale.CHINA);
				format.setTimeZone(TimeZone.getTimeZone("GMT"));
				a = Integer.valueOf(lists.getPlayerInTime());
				String s = format.format(a);
				holder.textViewPlayIntroduce.setText("上次播放至" + s);
			}
			if (lists.getPlayerImage() == null || lists.getPlayerImage().equals("") 
					|| lists.getPlayerImage().equals("null") || lists.getPlayerImage().trim().equals("")) {
				Bitmap bmp = BitmapUtils.readBitMap(context, R.mipmap.wt_image_playertx);
				holder.imageViewPlayImage.setImageBitmap(bmp);
			} else {
				url = lists.getPlayerImage();
				Picasso.with(context).load(url.replace("\\/", "/")).resize(100, 100).centerCrop().into(holder.imageViewPlayImage);
			}
		}
		return convertView;
	}

	@Override
	public boolean isChildSelectable(int groupPosition, int childPosition) {
		return true;
	}
	
	class ViewHolder {
		private TextView textName;
		public LinearLayout linearMore;
		public TextView textViewPlayName;
		public TextView textViewPlayIntroduce;
		public ImageView imageViewPlayImage;
		public TextView textNumber;
		public TextView textRankContent;
	}
}
