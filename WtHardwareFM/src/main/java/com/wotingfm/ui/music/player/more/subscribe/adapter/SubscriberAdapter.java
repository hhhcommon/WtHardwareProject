package com.wotingfm.ui.music.player.more.subscribe.adapter;

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
import com.wotingfm.common.config.GlobalConfig;
import com.wotingfm.ui.music.player.more.subscribe.model.SubscriberInfo;
import com.wotingfm.util.AssembleImageUrlUtils;
import com.wotingfm.util.BitmapUtils;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

/**
 * 订阅列表
 * Created by Administrator on 2017/2/13.
 */
public class SubscriberAdapter extends BaseAdapter {
    private List<SubscriberInfo> list;
    private Context context;

    public SubscriberAdapter(Context context, List<SubscriberInfo> list) {
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
            convertView = LayoutInflater.from(context).inflate(R.layout.adapter_subscriber_list, parent, false);

            // 六边形封面图片遮罩
            Bitmap bitmapMask = BitmapUtils.readBitMap(context, R.mipmap.wt_6_b_y_b);
            holder.imageMask = (ImageView) convertView.findViewById(R.id.image_mask);
            holder.imageMask.setImageBitmap(bitmapMask);

            holder.imageCover = (ImageView) convertView.findViewById(R.id.image_cover);// 封面图片
            holder.textTitle = (TextView) convertView.findViewById(R.id.text_sequ_title);// 订阅的专辑名
            holder.textMediaName = (TextView) convertView.findViewById(R.id.text_media_name);// ContentMediaName
            holder.textUpdateTime = (TextView) convertView.findViewById(R.id.text_update_time);// 更新时间
            holder.textUpdateCount = (TextView) convertView.findViewById(R.id.text_update_count);// 更新数量

            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        SubscriberInfo lists = list.get(position);

        // 封面图片
        String contentImg = lists.getContentSeqImg();
        if (contentImg == null || contentImg.equals("null") || contentImg.trim().equals("")) {
            Bitmap bmp = BitmapUtils.readBitMap(context, R.mipmap.wt_image_playertx);
            holder.imageCover.setImageBitmap(bmp);
        } else {
            if (!contentImg.startsWith("http")) {
                contentImg = GlobalConfig.imageurl + contentImg;
            }
            contentImg = AssembleImageUrlUtils.assembleImageUrl150(contentImg);
            Picasso.with(context).load(contentImg.replace("\\/", "/")).resize(100, 100).centerCrop().into(holder.imageCover);
        }

        // 订阅的专辑名
        String sequName = lists.getContentSeqName();
        if (sequName == null || sequName.trim().equals("")) {
            sequName = "未知";
        }
        holder.textTitle.setText(sequName);

        // ContentMediaName
        String contentName = lists.getContentMediaName();
        if (contentName == null || contentName.trim().equals("")) {
            contentName = "未知";
        }
        holder.textMediaName.setText(contentName);

        // 更新时间
        String updateTime = new SimpleDateFormat("yyyy-MM-dd", Locale.CHINA).format(lists.getContentPubTime());
        if (updateTime == null) {
            updateTime = new SimpleDateFormat("yyyy-MM-dd", Locale.CHINA).format(System.currentTimeMillis());
        }
        holder.textUpdateTime.setText(updateTime);

        // 更新数量
        int count = lists.getUpdateCount();
        if (count > 0) {
            holder.textUpdateCount.setVisibility(View.VISIBLE);
            holder.textUpdateCount.setText(count + "更新");
        } else {
            holder.textUpdateCount.setVisibility(View.GONE);
        }

        return convertView;
    }

    class ViewHolder {
        public ImageView imageMask;// 六边形封面图片遮罩
        public ImageView imageCover;// 封面图片
        public TextView textTitle;// 订阅的专辑名
        public TextView textMediaName;// ContentMediaName
        public TextView textUpdateTime;// 更新时间
        public TextView textUpdateCount;// 更新数量
    }
}
