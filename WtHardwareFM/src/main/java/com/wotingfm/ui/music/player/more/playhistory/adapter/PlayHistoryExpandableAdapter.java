package com.wotingfm.ui.music.player.more.playhistory.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;
import com.wotingfm.R;
import com.wotingfm.ui.music.player.model.PlayerHistory;
import com.wotingfm.ui.music.search.model.SuperRankInfo;
import com.wotingfm.util.AssembleImageUrlUtils;
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
		}
		return convertView;
	}

	@Override
	public View getChildView(int groupPosition, int childPosition,boolean isLastChild, View convertView, ViewGroup parent) {
		ViewHolder holder;
		if (convertView == null) {
			holder = new ViewHolder();
			convertView = LayoutInflater.from(context).inflate(R.layout.adapter_play_history, parent, false);

            // 六边形封面遮罩
            holder.imageMask = (ImageView) convertView.findViewById(R.id.image_mask);
            holder.imageMask.setImageBitmap(BitmapUtils.readBitMap(context, R.mipmap.wt_6_b_y_b));

			holder.textViewPlayName = (TextView) convertView.findViewById(R.id.RankTitle);// 节目名称
			holder.textViewPlayIntroduce = (TextView) convertView.findViewById(R.id.tv_last);// 上次播放时长
			holder.imageViewPlayImage = (ImageView) convertView.findViewById(R.id.RankImageUrl);// 节目图片
			holder.textNumber = (TextView) convertView.findViewById(R.id.text_number);
			holder.textRankContent = (TextView) convertView.findViewById(R.id.RankContent);
            holder.imageLast = (ImageView) convertView.findViewById(R.id.image_last);
			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}
		PlayerHistory lists = mSuperRankInfo.get(groupPosition).getHistoryList().get(childPosition);
        SimpleDateFormat format = new SimpleDateFormat("mm:ss", Locale.CHINA);

        // 封面
        String playImage = lists.getPlayerImage();
        if (playImage == null || playImage.equals("null") || playImage.trim().equals("")) {
            holder.imageViewPlayImage.setImageBitmap(BitmapUtils.readBitMap(context, R.mipmap.wt_image_playertx));
        } else {
            String url = AssembleImageUrlUtils.assembleImageUrl150(playImage);
            Picasso.with(context).load(url.replace("\\/", "/")).resize(100, 100).centerCrop().into(holder.imageViewPlayImage);
        }

        // 节目名
        String playName = lists.getPlayerName();
        if (playName != null && !playName.equals("")) {
            holder.textViewPlayName.setText(playName);
        }

        // 来源
        String playFrom = lists.getPlayerFrom();
        if (playFrom != null && !playFrom.equals("")) {
            holder.textRankContent.setText(playFrom);
        }

        // 收听次数
        String playNum = lists.getPlayCount();
        if (playNum != null && !playNum.equals("")) {
            holder.textNumber.setText(lists.getPlayCount());
        }

        // 上次播放时间
        String mediaType = lists.getPlayerMediaType();
        if(mediaType.equals("RADIO")) {
            holder.imageLast.setVisibility(View.GONE);
            holder.textViewPlayIntroduce.setVisibility(View.GONE);
        } else {
            holder.imageLast.setVisibility(View.VISIBLE);
            holder.textViewPlayIntroduce.setVisibility(View.VISIBLE);
            String playInTime = lists.getPlayerInTime();
            if (playInTime != null && !playInTime.equals("")) {
                format.setTimeZone(TimeZone.getTimeZone("GMT"));
                playInTime = "上次播放至" + format.format(Integer.valueOf(playInTime));
                holder.textViewPlayIntroduce.setText(playInTime);
            }
        }
		return convertView;
	}

	@Override
	public boolean isChildSelectable(int groupPosition, int childPosition) {
		return true;
	}
	
	class ViewHolder {
        public ImageView imageMask;// 六边形遮罩
        public TextView textName;
		public TextView textViewPlayName;
		public TextView textViewPlayIntroduce;
		public ImageView imageViewPlayImage;
		public TextView textNumber;
		public TextView textRankContent;
        public ImageView imageLast;
	}
}
