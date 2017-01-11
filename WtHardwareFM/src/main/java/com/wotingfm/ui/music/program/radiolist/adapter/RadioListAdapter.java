package com.wotingfm.ui.music.program.radiolist.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;
import com.wotingfm.R;
import com.wotingfm.ui.music.program.fmlist.model.RankInfo;
import com.wotingfm.common.config.GlobalConfig;
import com.wotingfm.util.AssembleImageUrlUtils;
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

            // 六边形封面遮罩
            holder.imageMask = (ImageView) convertView.findViewById(R.id.image_mask);
            holder.imageMask.setImageBitmap(BitmapUtils.readBitMap(context, R.mipmap.wt_6_b_y_b));

            holder.imageRank = (ImageView) convertView.findViewById(R.id.RankImageUrl);// 封面图片
            holder.textRankTitle = (TextView) convertView.findViewById(R.id.RankTitle);// 节目名
            holder.textRankPlaying = (TextView) convertView.findViewById(R.id.RankPlaying);// 来源
            holder.textNumber = (TextView) convertView.findViewById(R.id.tv_num);// 收听次数
            holder.textLast = (TextView) convertView.findViewById(R.id.tv_time);// 时长 OR 集数
            holder.imageLast = (ImageView) convertView.findViewById(R.id.image_last);// 时长 OR 集数 图标
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        RankInfo lists = list.get(position);
        String mediaType = lists.getMediaType();

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
        if (playCount == null || playCount.equals("")) {
            playCount = "1234";
        }
        Float count = Float.valueOf(playCount);
        if(count > 10000) {
            count = count / 10000;
            playCount = count + "万";
            if(count > 10000) {
                count = count / 10000;
                playCount = count + "亿";
            }
        }
        holder.textNumber.setText(playCount);

        if(mediaType != null) {
            switch (mediaType) {
                case "SEQU":
                    // 集数
                    holder.imageLast.setImageBitmap(BitmapUtils.readBitMap(context, R.mipmap.image_program_number));
                    String contentSubCount = lists.getContentSubCount();
                    if (contentSubCount != null && !contentSubCount.equals("")) {
                        contentSubCount = contentSubCount + "集";
                    }
                    holder.textLast.setText(contentSubCount);
                    break;
                default:
                    // 节目时长
                    holder.imageLast.setImageBitmap(BitmapUtils.readBitMap(context, R.mipmap.image_program_time));
                    String contentTime = lists.getContentTimes();
                    if (contentTime != null && !contentTime.equals("")) {
                        int minute = Integer.valueOf(contentTime) / (1000 * 60);
                        int second = (Integer.valueOf(contentTime) / 1000) % 60;
                        if(second < 10){
                            contentTime = minute + "\'" + " " + "0" + second + "\"";
                        }else{
                            contentTime = minute + "\'" + " " + second + "\"";
                        }
                        holder.textLast.setText(contentTime);
                    }
                    break;
            }
        }
        return convertView;
    }

    private class ViewHolder {
        public ImageView imageMask;// 六边形封面遮罩
        public ImageView imageRank;// 封面图片
        public TextView textRankTitle;// 节目名
        public TextView textRankPlaying;// 来源
        public TextView textNumber;// 收听次数
        public TextView textLast;// 时长 OR 集数
        public ImageView imageLast;// 时长 OR 集数 图标
    }
}
