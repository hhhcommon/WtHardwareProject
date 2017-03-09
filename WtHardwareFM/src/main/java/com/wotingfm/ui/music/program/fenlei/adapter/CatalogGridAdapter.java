package com.wotingfm.ui.music.program.fenlei.adapter;

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
import com.wotingfm.ui.music.program.fenlei.model.Attributes;
import com.wotingfm.ui.music.program.fenlei.model.FenLeiName;
import com.wotingfm.util.AssembleImageUrlUtils;
import com.wotingfm.util.BitmapUtils;

import java.util.List;
/**
 * 分类页面适配器2
 * author：辛龙 (xinLong)
 * 2017/3/8 13:49
 * 邮箱：645700751@qq.com
 */
public class CatalogGridAdapter extends BaseAdapter {
    private List<FenLeiName> list;
    private Context context;
    private ViewHolder holder;
    private Bitmap bmp;

    public CatalogGridAdapter(Context context, List<FenLeiName> list) {
        super();
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
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.adapter_fenlei_child_grid, null);
            holder = new ViewHolder();
            holder.tv_name = (TextView) convertView.findViewById(R.id.tv_name);
            holder.image_url = (ImageView) convertView.findViewById(R.id.image_url);
            bmp = BitmapUtils.readBitMap(context, R.mipmap.image_fenlei_icon);// 封面图片的默认图片
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        holder.tv_name.setText(list.get(position).getName());

        try {
            Attributes attributes = list.get(position).getAttributes();
            if (attributes != null && attributes.getChannelImg() != null && !attributes.getChannelImg().trim().equals("")) {
                String contentImg = attributes.getChannelImg();
                if (!contentImg.startsWith("http")) {
                    contentImg = GlobalConfig.imageurl + contentImg;
                }
                contentImg = AssembleImageUrlUtils.assembleImageUrl150(contentImg);
                Picasso.with(context).load(contentImg.replace("\\/", "/")).resize(100, 100).centerCrop().into(holder.image_url);
            } else {
                holder.image_url.setImageBitmap(bmp);
            }
        } catch (Exception e) {
            e.printStackTrace();
            holder.image_url.setImageBitmap(bmp);
        }

        return convertView;
    }

    class ViewHolder {
        public TextView tv_name;
        public ImageView image_url;
    }
}
