package com.wotingfm.activity.music.program.tuijian.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.wotingfm.R;
import com.wotingfm.activity.music.program.fmlist.model.RankInfo;
import com.wotingfm.common.config.GlobalConfig;
import com.wotingfm.helper.ImageLoader;
import com.wotingfm.util.BitmapUtils;

import java.util.List;

public class RecommendListAdapter extends BaseAdapter {
    private List<RankInfo> list;
    private Context context;
    private ImageLoader imageLoader;
    private Bitmap bmp;
    private boolean isHintVisibility;

    public RecommendListAdapter(Context context, List<RankInfo> list, boolean isHintVisibility) {
        this.context = context;
        this.list = list;
        imageLoader = new ImageLoader(context);
//        this.isHintVisibility = isHintVisibility;
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
            convertView = LayoutInflater.from(context).inflate(R.layout.adapter_fragment_recommend, parent, false);
            holder.textViewRankTitle = (TextView) convertView.findViewById(R.id.RankTitle);// 台名
            holder.imageViewRankImage = (ImageView) convertView.findViewById(R.id.RankImageUrl);// 电台图标
            holder.mTextNumber = (TextView) convertView.findViewById(R.id.tv_num);
            holder.textRankContent = (TextView) convertView.findViewById(R.id.RankContent);
            holder.textTotal = (TextView) convertView.findViewById(R.id.tv_total);
            holder.imageHintVisibility = (ImageView) convertView.findViewById(R.id.image_hint_visibility);
            holder.imageNumberTime = (ImageView) convertView.findViewById(R.id.image_number_time);
            bmp = BitmapUtils.readBitMap(context, R.mipmap.wt_image_playertx);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        RankInfo lists = list.get(position);
        if (lists.getContentName() == null || lists.getContentName().equals("")) {
            holder.textViewRankTitle.setText("未知");
        } else {
            holder.textViewRankTitle.setText(lists.getContentName());
        }
        if (lists.getContentImg() == null
                || lists.getContentImg().equals("null") || lists.getContentImg().trim().equals("")) {

            holder.imageViewRankImage.setImageBitmap(bmp);
        } else {
            String url;
            if (lists.getContentImg().startsWith("http")) {
                url = lists.getContentImg();
            } else {
                url = GlobalConfig.imageurl + lists.getContentImg();
            }
            imageLoader.DisplayImage(url.replace("\\/", "/"), holder.imageViewRankImage, false, false, null, null);
        }
        if (lists.getMediaType() != null) {
            if (lists.getMediaType().equals("SEQU")) {
                isHintVisibility = false;
                holder.imageNumberTime.setImageResource(R.mipmap.image_program_number);
                if (lists.getContentSubCount() == null
                        || lists.getContentSubCount().equals("") || lists.getContentSubCount().equals("null")) {

                    holder.textTotal.setText("8000" + "集");
                } else {
                    holder.textTotal.setText(lists.getContentSubCount() + "集");
                }
            } else if (lists.getMediaType().equals("AUDIO")) {
                isHintVisibility = false;
                holder.imageNumberTime.setImageResource(R.mipmap.image_program_time);
                // 节目时长
                if (lists.getContentTimes() == null
                        || lists.getContentTimes().equals("") || lists.getContentTimes().equals("null")) {
                    holder.textTotal.setText(context.getString(R.string.play_time));
                } else {
                    int minute = Integer.valueOf(lists.getContentTimes()) / (1000 * 60);
                    int second = (Integer.valueOf(lists.getContentTimes()) / 1000) % 60;
                    if (second < 10) {
                        holder.textTotal.setText(minute + "\'" + " " + "0" + second + "\"");
                    } else {
                        holder.textTotal.setText(minute + "\'" + " " + second + "\"");
                    }
                }
            } else if(lists.getMediaType().equals("RADIO")) {
                isHintVisibility = true;
                holder.imageNumberTime.setVisibility(View.GONE);
                holder.textTotal.setVisibility(View.GONE);
            }
        }
        if (isHintVisibility) {
            holder.imageHintVisibility.setVisibility(View.GONE);
        } else {
            holder.imageHintVisibility.setVisibility(View.VISIBLE);
        }
        if (lists.getPlayCount() == null || lists.getPlayCount().equals("") || lists.getPlayCount().equals("null")) {
            holder.mTextNumber.setText("8000");
        } else {
            holder.mTextNumber.setText(lists.getPlayCount());
        }
        if (lists.getContentPub() == null || lists.getContentPub().equals("") || lists.getContentPub().equals("null")) {
            holder.textRankContent.setText("未知");
        } else {
            holder.textRankContent.setText(lists.getContentPub());
        }
        return convertView;
    }

    static class ViewHolder {
        public ImageView imageViewRankImage;
        public TextView textViewRankTitle;
        public TextView mTextNumber;
        public TextView textRankContent;
        public TextView textTotal;
        public ImageView imageHintVisibility;
        public ImageView imageNumberTime;
    }
}
