package com.wotingfm.activity.music.favorite.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;
import com.wotingfm.R;
import com.wotingfm.activity.music.program.fmlist.model.RankInfo;
import com.wotingfm.common.config.GlobalConfig;
import com.wotingfm.util.BitmapUtils;

import java.util.List;

public class FavorListAdapter extends BaseAdapter {
    private List<RankInfo> list;
    private Context context;
    private FavoriteCheck favoriteCheck;
    private String url;

    public FavorListAdapter(Context context, List<RankInfo> list) {
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

    public void setOnListener(FavoriteCheck favoriteCheck) {
        this.favoriteCheck = favoriteCheck;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            holder = new ViewHolder();
            convertView = LayoutInflater.from(context).inflate(R.layout.adapter_favoritelist, parent, false);
            holder.textview_ranktitle = (TextView) convertView.findViewById(R.id.RankTitle);// 台名
            holder.imageview_rankimage = (ImageView) convertView.findViewById(R.id.RankImageUrl);// 电台图标
            holder.tv_RankContent = (TextView) convertView.findViewById(R.id.RankContent);
            holder.img_check = (ImageView) convertView.findViewById(R.id.img_check);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        RankInfo lists = list.get(position);
        if (lists.getContentName() == null || lists.getContentName().equals("")) {
            holder.textview_ranktitle.setText("未知");
        } else {
            holder.textview_ranktitle.setText(lists.getContentName());
        }
        if (lists.getContentImg() == null || lists.getContentImg().equals("")
                || lists.getContentImg().equals("null")
                || lists.getContentImg().trim().equals("")) {
            Bitmap bmp = BitmapUtils.readBitMap(context, R.mipmap.wt_bg_noimage);
            holder.imageview_rankimage.setImageBitmap(bmp);
        } else {
            if (lists.getContentImg().startsWith("http:")) {
                url = lists.getContentImg();
            } else {
                url = GlobalConfig.imageurl + lists.getContentImg();
            }
            Picasso.with(context).load(url.replace("\\/", "/")).resize(100, 100).centerCrop().into(holder.imageview_rankimage);
        }
        if (lists.getContentDesc() == null || lists.getContentDesc().equals("")) {
            holder.tv_RankContent.setText("未知");
        } else {
            holder.tv_RankContent.setText(lists.getContentDesc());
        }
        if (lists.getViewtype() == 0) {
            // 0 状态时 为点选框隐藏状态设置当前的选择状态为 0
            holder.img_check.setVisibility(View.GONE);
        } else {
            // 1状态 此时设置 choiceType 生效
            holder.img_check.setVisibility(View.VISIBLE);
            if (lists.getChecktype() == 0) {
                Bitmap bmp = BitmapUtils.readBitMap(context, R.mipmap.wt_group_nochecked);// 未点击状态
                holder.img_check.setImageBitmap(bmp);
            } else {
                Bitmap bmp = BitmapUtils.readBitMap(context, R.mipmap.wt_group_checked); // 点击状态
                holder.img_check.setImageBitmap(bmp);
            }
        }
        holder.img_check.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                favoriteCheck.checkPosition(position);
            }
        });
        return convertView;
    }

    public interface FavoriteCheck {
        void checkPosition(int position);
    }

    private class ViewHolder {
        public ImageView imageview_rankimage;
        public TextView textview_ranktitle;
        public TextView tv_RankContent;
        public ImageView img_check;
    }
}
