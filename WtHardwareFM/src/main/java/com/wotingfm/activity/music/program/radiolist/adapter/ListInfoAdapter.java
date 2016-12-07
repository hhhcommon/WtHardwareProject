package com.wotingfm.activity.music.program.radiolist.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;
import com.wotingfm.R;
import com.wotingfm.activity.music.program.radiolist.model.ListInfo;
import com.wotingfm.common.config.GlobalConfig;
import com.wotingfm.util.AssembleImageUrlUtils;
import com.wotingfm.util.BitmapUtils;

import java.util.List;

public class ListInfoAdapter extends BaseAdapter  {
	private List<ListInfo> list;
	private Context context;

	public ListInfoAdapter(Context context, List<ListInfo> list) {
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

            // 六边形封面遮罩
            holder.imageMask = (ImageView) convertView.findViewById(R.id.image_mask);
            holder.imageMask.setImageBitmap(BitmapUtils.readBitMap(context, R.mipmap.wt_6_b_y_b));

            holder.imageRank = (ImageView) convertView.findViewById(R.id.RankImageUrl);// 封面图片
			holder.textRankTitle = (TextView) convertView.findViewById(R.id.RankTitle);// 节目名
            holder.textRankPlaying = (TextView) convertView.findViewById(R.id.RankPlaying);// 来源
			holder.textNumber = (TextView) convertView.findViewById(R.id.tv_num);// 收听次数
			holder.textTime = (TextView) convertView.findViewById(R.id.tv_time);// 节目时长
			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}
		ListInfo lists = list.get(position);

        // 封面图片
        String contentImage = lists.getContentImg();
        if (contentImage == null || contentImage.equals("null") || contentImage.trim().equals("")) {
            holder.imageRank.setImageBitmap(BitmapUtils.readBitMap(context, R.mipmap.wt_image_playertx));
        } else {
            String url;
            if(lists.getContentImg().startsWith("http")){
                url =  contentImage;
            }else{
                url = GlobalConfig.imageurl + contentImage;
            }
            url = AssembleImageUrlUtils.assembleImageUrl150(url);
            Picasso.with(context).load(url.replace("\\/", "/")).resize(100, 100).centerCrop().into(holder.imageRank);
        }

        // 节目名
        String contentName = lists.getContentName();
		if (contentName != null && !contentName.equals("")) {
            holder.textRankTitle.setText(contentName);
		}

        // 来源
        String contentPub = lists.getContentPub();
        if (contentPub != null && !contentPub.equals("")) {
            holder.textRankPlaying.setText(contentPub);
        }

        // 收听次数
        String playCount = lists.getPlayCount();
		if (playCount != null && !playCount.equals("")) {
            holder.textNumber.setText(playCount);
		}

		// 节目时长
        String contentTime = lists.getContentTimes();
		if (contentTime != null && !contentTime.equals("")) {
            int minute = Integer.valueOf(contentTime) / (1000 * 60);
            int second = (Integer.valueOf(contentTime) / 1000) % 60;
            if(second < 10){
                contentTime = minute + "\'" + " " + "0" + second + "\"";
            }else{
                contentTime = minute + "\'" + " " + second + "\"";
            }
            holder.textTime.setText(contentTime);
		}
		return convertView;
	}

    private class ViewHolder {
        public ImageView imageMask;// 六边形封面遮罩
		public ImageView imageRank;// 封面图片
		public TextView textRankTitle;// 节目名
        public TextView textRankPlaying;// 来源
		public TextView textNumber;// 收听次数
		public TextView textTime;// 节目时长
    }
}
