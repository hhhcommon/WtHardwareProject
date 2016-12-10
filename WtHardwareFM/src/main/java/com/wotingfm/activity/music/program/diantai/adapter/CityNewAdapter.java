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
import com.wotingfm.common.config.GlobalConfig;
import com.wotingfm.util.AssembleImageUrlUtils;
import com.wotingfm.util.BitmapUtils;

import java.util.List;


public class CityNewAdapter extends BaseAdapter {
    private List<RankInfo> list;
    private Context context;

    public CityNewAdapter(Context context, List<RankInfo> list) {
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
            convertView = LayoutInflater.from(context).inflate(R.layout.adapter_rankinfo, parent, false);

            // 六边形封面遮罩
            holder.imageMask = (ImageView) convertView.findViewById(R.id.img_zhezhao);
            holder.imageMask.setImageBitmap(BitmapUtils.readBitMap(context, R.mipmap.wt_6_b_y_b));

            holder.textRankTitle = (TextView) convertView.findViewById(R.id.RankTitle);// 台名
            holder.textRankPlaying = (TextView) convertView.findViewById(R.id.RankPlaying);// 正在播放的节目
            holder.imageRankImage = (ImageView) convertView.findViewById(R.id.RankImageUrl);// 电台图标
            holder.textNumber = (TextView) convertView.findViewById(R.id.tv_num);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        RankInfo lists = list.get(position);
        String mediaType = lists.getMediaType();

        if (mediaType != null && !mediaType.equals("")) {

            // 标题
            if (lists.getContentName() != null && !lists.getContentName().equals("")) {
                holder.textRankTitle.setText(lists.getContentName());
            }

            // 封面
            String contentImage = lists.getContentImg();
            if (contentImage == null || contentImage.equals("null") || contentImage.trim().equals("")) {
                Bitmap bmp = BitmapUtils.readBitMap(context, R.mipmap.wt_image_playertx_d);
                holder.imageRankImage.setImageBitmap(bmp);
            } else {
                String url;
                if (contentImage.startsWith("http")) {
                    url = contentImage;
                } else {
                    url = GlobalConfig.imageurl + contentImage;
                }
                url = AssembleImageUrlUtils.assembleImageUrl150(url);
                Picasso.with(context).load(url.replace("\\/", "/")).resize(100, 100).centerCrop().into(holder.imageRankImage);
            }

            // 收听人数
            String playCount = lists.getPlayCount();
            if (playCount != null && !playCount.equals("") && !playCount.equals("null")) {
                holder.textNumber.setText(lists.getPlayCount());
            }

            if (mediaType.equals("RADIO")) {
//				if (lists.getContentPub() != null && !lists.getContentPub().equals("")) {
//                    holder.textRankPlaying.setText("正在直播：" + lists.getContentPub());
//				}
//                holder.textRankPlaying.setText("正在直播：测试-无节目单数据");
                holder.textRankPlaying.setText("测试-无节目单数据");
            } else {
                if (lists.getContentPub() != null && !lists.getContentPub().equals("")) {
                    holder.textRankPlaying.setText(lists.getContentPub());
				}
            }
        }
        return convertView;
    }

    private class ViewHolder {
        public TextView textRankTitle, textNumber, textRankPlaying;// 标题  收听人数  正在直播的节目
        public ImageView imageRankImage;// 封面
        public ImageView imageMask;// 六边形封面遮罩
    }
}
