package com.wotingfm.ui.interphone.notify.adapter;

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
import com.wotingfm.ui.interphone.linkman.model.DBNotifyHistory;
import com.wotingfm.util.AssembleImageUrlUtils;
import com.wotingfm.util.BitmapUtils;

import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.List;

;

public class NotifyNewsAdapter extends BaseAdapter {
    private List<DBNotifyHistory> list;
    private Context context;
    private DBNotifyHistory lists;
    private SimpleDateFormat format;

    public NotifyNewsAdapter(Context context, List<DBNotifyHistory> list) {
        super();
        this.list = list;
        this.context = context;
        format = new SimpleDateFormat("yy-MM-dd HH:mm");
    }

    public void ChangeDate(List<DBNotifyHistory> list) {
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

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            holder = new ViewHolder();
            convertView = LayoutInflater.from(context).inflate(R.layout.adapter_notifynews, null);
            holder.time = (TextView) convertView.findViewById(R.id.time);
            holder.tile = (TextView) convertView.findViewById(R.id.title);
            holder.content = (TextView) convertView.findViewById(R.id.content);
            holder.Image = (ImageView) convertView.findViewById(R.id.Image);
            holder.img_zhezhao = (ImageView) convertView.findViewById(R.id.img_zhezhao);
            Bitmap bmp_zhezhao = BitmapUtils.readBitMap(context, R.mipmap.wt_6_b_y_b);
            holder.img_zhezhao.setImageBitmap(bmp_zhezhao);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        lists = list.get(position);
        if (lists != null) {
            if (lists.getTitle() == null || lists.getTitle().equals("")) {
                holder.tile.setText("新信息");
            } else {
                holder.tile.setText(lists.getTitle());
            }
            if (lists.getContent() == null || lists.getContent().equals("")) {
                holder.content.setText("未知消息");
            } else {
                holder.content.setText(lists.getContent());
            }
            if (lists.getDealTime() == null || lists.getDealTime().equals("") || lists.getDealTime().equals("null")) {
                holder.time.setText(format.format(new Date(System.currentTimeMillis())));
            } else {
                holder.time.setText(format.format(new Date(Long.parseLong(lists.getDealTime()))));
            }
            if (lists.getImageUrl() == null || lists.getImageUrl().equals("")
                    || lists.getImageUrl().equals("null") || lists.getImageUrl().trim().equals("")) {
                Bitmap bmp = BitmapUtils.readBitMap(context, R.mipmap.wt_linkman_news);
                holder.Image.setImageBitmap(bmp);
            } else {
                String url;
                if(lists.getImageUrl().startsWith("http:")){
                    url=lists.getImageUrl();
                }else{
                    url = GlobalConfig.imageurl+lists.getImageUrl();
                }
                url= AssembleImageUrlUtils.assembleImageUrl150(url);
                Picasso.with(context).load(url.replace("\\/", "/")).into(holder.Image);
            }
        }
        return convertView;
    }

    class ViewHolder {
        public ImageView Image;
        public TextView content;
        public TextView tile;
        public TextView time;
        public ImageView img_zhezhao;
    }
}
