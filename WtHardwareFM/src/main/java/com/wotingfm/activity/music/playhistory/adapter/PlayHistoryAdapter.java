package com.wotingfm.activity.music.playhistory.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;
import com.wotingfm.R;
import com.wotingfm.activity.music.player.model.PlayerHistory;
import com.wotingfm.util.AssembleImageUrlUtils;
import com.wotingfm.util.BitmapUtils;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

public class PlayHistoryAdapter extends BaseAdapter {
    private List<PlayerHistory> list;
    private Context context;
    private PlayHistoryCheck playCheck;

    public PlayHistoryAdapter(Context context, List<PlayerHistory> list) {
        this.list = list;
        this.context = context;
    }

    public void ChangeDate(List<PlayerHistory> list) {
        this.list = list;
        this.notifyDataSetChanged();
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

    public void setOnclick(PlayHistoryCheck playCheck) {
        this.playCheck = playCheck;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            holder = new ViewHolder();
            convertView = LayoutInflater.from(context).inflate(R.layout.adapter_play_history, parent, false);

            // 六边形封面遮罩
            holder.imageMask = (ImageView) convertView.findViewById(R.id.image_mask);
            holder.imageMask.setImageBitmap(BitmapUtils.readBitMap(context, R.mipmap.wt_6_b_y_b));

            holder.imageView_playImage = (ImageView) convertView.findViewById(R.id.RankImageUrl);// 节目图片
            holder.textView_playName = (TextView) convertView.findViewById(R.id.RankTitle);// 节目名称
            holder.textRankContent = (TextView) convertView.findViewById(R.id.RankContent);// 来源

            holder.imageLast = (ImageView) convertView.findViewById(R.id.image_last);// 上次播放时间图标

            holder.textView_PlayIntroduce = (TextView) convertView.findViewById(R.id.tv_last);
            holder.imageCheck = convertView.findViewById(R.id.lin_check);// 是否选中 清除
            holder.layoutCheck = convertView.findViewById(R.id.layout_check);
            holder.check = (ImageView) convertView.findViewById(R.id.img_check);
            holder.textNumber = (TextView) convertView.findViewById(R.id.text_number);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        PlayerHistory lists = list.get(position);
        SimpleDateFormat format = new SimpleDateFormat("mm:ss", Locale.CHINA);
        format.setTimeZone(TimeZone.getTimeZone("GMT"));

        // 封面图片
        String playImage = lists.getPlayerImage();
        if (playImage == null || playImage.equals("null") || playImage.trim().equals("")) {
            holder.imageView_playImage.setImageBitmap(BitmapUtils.readBitMap(context, R.mipmap.wt_image_playertx));
        } else {
            String url = AssembleImageUrlUtils.assembleImageUrl150(playImage);
            Picasso.with(context).load(url.replace("\\/", "/")).resize(100, 100).centerCrop().into(holder.imageView_playImage);
        }

        // 节目名
        String playName = lists.getPlayerName();
        if (playName != null && !playName.equals("")) {
            holder.textView_playName.setText(playName);
        }

        // 来源
        String contentPub = lists.getContentPub();
        if (contentPub != null && !contentPub.equals("")) {
            holder.textRankContent.setText(contentPub);
        }

        // 收听次数
        String playNum = lists.getPlayerNum();
        if (playNum != null && !playNum.equals("")) {
            holder.textNumber.setText(playNum);
        }

        // 上次播放时间
        String mediaType = lists.getPlayerMediaType();
        if (mediaType.equals("RADIO")) {
            holder.imageLast.setVisibility(View.GONE);
            holder.textView_PlayIntroduce.setVisibility(View.GONE);
        } else {
            holder.imageLast.setVisibility(View.VISIBLE);
            holder.textView_PlayIntroduce.setVisibility(View.VISIBLE);

            String playInTime = lists.getPlayerInTime();
            if (playInTime != null && !playInTime.equals("")) {
                playInTime = "上次播放至" + format.format(Integer.valueOf(playInTime));
                holder.textView_PlayIntroduce.setText(playInTime);
            }
        }

        if (lists.isCheck()) {
            holder.imageCheck.setVisibility(View.VISIBLE);
            if (lists.getStatus() == 0) {// 未点击状态
                holder.check.setImageBitmap(BitmapUtils.readBitMap(context, R.mipmap.wt_group_nochecked));
            } else if (lists.getStatus() == 1) {// 点击状态
                holder.check.setImageBitmap(BitmapUtils.readBitMap(context, R.mipmap.wt_group_checked));
            }
        } else {
            holder.imageCheck.setVisibility(View.GONE);
        }
        holder.imageCheck.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                playCheck.checkPosition(position);
            }
        });
        return convertView;
    }

    public interface PlayHistoryCheck {
        void checkPosition(int position);
    }

    class ViewHolder {
        public ImageView imageMask;// 六边形遮罩
        public ImageView imageView_playImage;// 封面图片
        public TextView textView_playName;// 节目名
        public TextView textRankContent;// 来源
        public TextView textNumber;// 收听次数

        public ImageView imageLast;// 上次播放时间图标

        public TextView textView_PlayIntroduce;
        public ImageView check;
        public View imageCheck;
        public View layoutCheck;
    }
}
