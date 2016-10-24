package com.wotingfm.activity.music.download.adapter;

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
import com.wotingfm.activity.music.download.model.FileInfo;
import com.wotingfm.util.BitmapUtils;

import java.text.DecimalFormat;
import java.util.List;

public class DownLoadSequAdapter extends BaseAdapter {
    private List<FileInfo> list;
    private Context context;
    private DownLoadDelete downLoadDelete;
    private DecimalFormat df;

    public DownLoadSequAdapter(Context context, List<FileInfo> list) {
        this.context = context;
        this.list = list;
        df = new DecimalFormat("0.00");
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

    public void setOnListener(DownLoadDelete downLoadDelete) {
        this.downLoadDelete = downLoadDelete;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            holder = new ViewHolder();
            convertView = LayoutInflater.from(context).inflate(R.layout.adapter_download_complete, parent, false);
            holder.textview_ranktitle = (TextView) convertView.findViewById(R.id.RankTitle);// 台名
            holder.imageview_rankimage = (ImageView) convertView.findViewById(R.id.RankImageUrl);// 电台图标
            holder.tv_RankContent = (TextView) convertView.findViewById(R.id.RankContent);
            holder.tv_count = (TextView) convertView.findViewById(R.id.tv_count);
            holder.tv_sum = (TextView) convertView.findViewById(R.id.tv_sum);
            holder.textDelete = (TextView) convertView.findViewById(R.id.text_delete);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        FileInfo lists = list.get(position);
        if (lists.getSequname() == null || lists.getSequname().equals("")) {
            holder.textview_ranktitle.setText("未知");
        } else {
            holder.textview_ranktitle.setText(lists.getSequname());
        }
        if (lists.getSequimgurl() == null || lists.getSequimgurl().equals("")
                || lists.getSequimgurl().equals("null")
                || lists.getSequimgurl().trim().equals("")) {
            Bitmap bmp = BitmapUtils.readBitMap(context, R.mipmap.wt_bg_noimage);
            holder.imageview_rankimage.setImageBitmap(bmp);
        } else {
            String url = lists.getSequimgurl();
            Picasso.with(context).load(url.replace("\\/", "/")).resize(100, 100).centerCrop().into(holder.imageview_rankimage);
        }
        if (lists.getAuthor() == null || lists.getAuthor().equals("")) {
            holder.tv_RankContent.setText("我听科技");
        } else {
            holder.tv_RankContent.setText(lists.getAuthor());
        }
        if (lists.getCount() != -1) {
            holder.tv_count.setText(lists.getCount() + "集");
        }
        if (lists.getSum() != -1) {
            holder.tv_sum.setText(df.format(lists.getSum() / 1000.0 / 1000.0) + "MB");
        }
        holder.textDelete.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                downLoadDelete.deletePosition(position);
            }
        });
        return convertView;
    }

    public interface DownLoadDelete {
        void deletePosition(int position);
    }

    private class ViewHolder {
        public ImageView imageview_rankimage;
        public TextView textview_ranktitle;
        public TextView tv_RankContent;
        public TextView tv_count;
        public TextView tv_sum;
        public TextView textDelete;
    }
}
