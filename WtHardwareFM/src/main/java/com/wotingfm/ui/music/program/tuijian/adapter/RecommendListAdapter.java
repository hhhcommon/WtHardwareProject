package com.wotingfm.ui.music.program.tuijian.adapter;

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
import com.wotingfm.ui.music.program.fmlist.model.RankInfo;
import com.wotingfm.common.config.GlobalConfig;
import com.wotingfm.util.AssembleImageUrlUtils;
import com.wotingfm.util.BitmapUtils;

import java.util.List;

public class RecommendListAdapter extends BaseAdapter {
    private List<RankInfo> list;
    private Context context;
    private Bitmap bmp;

    public RecommendListAdapter(Context context, List<RankInfo> list) {
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
            convertView = LayoutInflater.from(context).inflate(R.layout.adapter_fragment_recommend, null);
            holder = new ViewHolder();

            // 六边形封面图片遮罩
            Bitmap bitmapMask = BitmapUtils.readBitMap(context, R.mipmap.wt_6_b_y_b);
            holder.imageCoverMask = (ImageView) convertView.findViewById(R.id.img_zhezhao);
            holder.imageCoverMask.setImageBitmap(bitmapMask);

            bmp = BitmapUtils.readBitMap(context, R.mipmap.wt_image_playertx);// 封面图片的默认图片
            holder.imageCover = (ImageView) convertView.findViewById(R.id.RankImageUrl);// 封面图片

            holder.textTitle = (TextView) convertView.findViewById(R.id.RankTitle);// 标题
            holder.textContent = (TextView) convertView.findViewById(R.id.RankContent);// 来源 -> 专辑

            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        RankInfo lists = list.get(position);

        // 标题
        String contentName = lists.getContentName();
        if (contentName == null || contentName.equals("")) {
            contentName = "未知";
        }
        holder.textTitle.setText(contentName);

        // 封面图片
        String contentImg = lists.getContentImg();
        if (contentImg == null || contentImg.equals("null")|| contentImg.trim().equals("")) {
            holder.imageCover.setImageBitmap(bmp);
        } else {
            if(!contentImg.startsWith("http")){
                contentImg = GlobalConfig.imageurl + contentImg;
            }
            contentImg= AssembleImageUrlUtils.assembleImageUrl150(contentImg);
            Picasso.with(context).load(contentImg.replace("\\/", "/")).resize(100, 100).centerCrop().into(holder.imageCover);
        }

        // 来源 -> 专辑、主播
        String media = lists.getMediaType();
        String name;
        if (media != null && media.equals("SEQU")) {
            name = lists.getContentName();
            if (name == null || name.equals("")) {
                name = "未知";
            }
            name = "专辑：" + name;
            holder.textContent.setText(name);
        } else {
            try {
                name = lists.getContentPersons().get(0).getPerName();
                if (name == null || name.equals("")) {
                    name = "未知";
                }
                name = "主播：" + name;
                holder.textContent.setText(name);
            } catch (Exception e) {
                e.printStackTrace();
                name = "主播：未知";
                holder.textContent.setText(name);
            }
        }

        return convertView;
    }

    class  ViewHolder {
        public ImageView imageCoverMask;// 六边形封面图片遮罩
        public ImageView imageCover;// 封面图片
        public TextView textTitle;// 标题
        public TextView textContent;// 来源 -> 专辑
    }
}
