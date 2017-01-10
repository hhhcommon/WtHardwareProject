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
            holder.textview_ranktitle = (TextView) convertView.findViewById(R.id.RankTitle);// 台名
            holder.imageview_rankimage = (ImageView) convertView.findViewById(R.id.RankImageUrl);// 电台图标
            holder.textRankContent = (TextView) convertView.findViewById(R.id.RankContent);

            holder.img_zhezhao = (ImageView) convertView.findViewById(R.id.img_zhezhao);
            Bitmap bmp_zhezhao = BitmapUtils.readBitMap(context, R.mipmap.wt_6_b_y_b);
            holder.img_zhezhao.setImageBitmap(bmp_zhezhao);

            bmp = BitmapUtils.readBitMap(context, R.mipmap.wt_image_playertx);
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
        holder.textview_ranktitle.setText(contentName);

        // 封面图片
        String contentImg = lists.getContentImg();
        if (contentImg == null || contentImg.equals("null")|| contentImg.trim().equals("")) {
            holder.imageview_rankimage.setImageBitmap(bmp);
        } else {
            if(!contentImg.startsWith("http")){
                contentImg = GlobalConfig.imageurl + contentImg;
            }
            contentImg= AssembleImageUrlUtils.assembleImageUrl150(contentImg);
            Picasso.with(context).load(contentImg.replace("\\/", "/")).resize(100, 100).centerCrop().into(holder.imageview_rankimage);
        }

        // 来源
        String contentPub = lists.getContentPub();
        if (contentPub == null || contentPub.equals("") || contentPub.equals("null")) {
            contentPub = "未知";
        }
        holder.textRankContent.setText(contentPub);

        return convertView;
    }

    class  ViewHolder {
        public ImageView imageview_rankimage;
        public TextView textview_ranktitle;
        public TextView textRankContent;
        public ImageView img_zhezhao;
    }
}
