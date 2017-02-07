package com.wotingfm.ui.music.download.downloadlist.adapter;

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
import com.wotingfm.ui.music.download.model.FileInfo;
import com.wotingfm.util.AssembleImageUrlUtils;
import com.wotingfm.util.BitmapUtils;

import java.text.DecimalFormat;
import java.util.List;

/**
 * 下载列表数据展示
 */
public class DownLoadListAdapter extends BaseAdapter {
    private List<FileInfo> list;
    private Context context;
    private DownloadList downloadList;

    public DownLoadListAdapter(Context context, List<FileInfo> list) {
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

    public void setonListener(DownloadList downloadList) {
        this.downloadList = downloadList;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            holder = new ViewHolder();
            convertView = LayoutInflater.from(context).inflate(R.layout.adapter_downloadlist, null);

            // 六边形封面图片遮罩
            Bitmap bmp = BitmapUtils.readBitMap(context, R.mipmap.wt_6_b_y_b);
            holder.imageMask = (ImageView) convertView.findViewById(R.id.img_liu);
            holder.imageMask.setImageBitmap(bmp);

            holder.imageRankImage = (ImageView) convertView.findViewById(R.id.RankImageUrl);// 电台图标
            holder.textRankTitle = (TextView) convertView.findViewById(R.id.RankTitle);// 台名
            holder.textRankContent = (TextView) convertView.findViewById(R.id.RankContent);// 来源
            holder.textSum = (TextView) convertView.findViewById(R.id.tv_sum);// 节目大小
            holder.viewDelete = convertView.findViewById(R.id.lin_clear);// 删除

            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        FileInfo lists = list.get(position);

        // 电台图标
        String imageUrl = lists.getImageurl();
        if (imageUrl == null || imageUrl.equals("null") || imageUrl.trim().equals("")) {
            imageUrl = lists.getSequimgurl();
        }
        if (imageUrl == null || imageUrl.equals("null") || imageUrl.trim().equals("")) {
            Bitmap bmp = BitmapUtils.readBitMap(context, R.mipmap.wt_image_playertx);
            holder.imageRankImage.setImageBitmap(bmp);
        } else {
            imageUrl = AssembleImageUrlUtils.assembleImageUrl150(imageUrl);
            Picasso.with(context).load(imageUrl.replace("\\/", "/")).resize(100, 100).centerCrop().into(holder.imageRankImage);
        }

        // 台名
        String fileName = lists.getFileName();
        if (fileName == null || fileName.equals("")) {
            fileName = "未知";
        }
        holder.textRankTitle.setText(fileName);

        // 来源
        String playFrom = lists.getPlayFrom();
        if (playFrom == null || playFrom.equals("")) {
            playFrom = "未知";
        }
        holder.textRankContent.setText(playFrom);

        // 大小
        try {
            if (lists.getEnd() <= 0) {
                holder.textSum.setText("0MB");
            } else {
                holder.textSum.setText(new DecimalFormat("0.00").format(lists.getEnd() / 1000.0 / 1000.0) + "MB");
            }
        } catch (Exception e) {
            e.printStackTrace();
            holder.textSum.setText("0MB");
        }

        // 删除
        holder.viewDelete.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                downloadList.checkPosition(position);
            }
        });
        return convertView;
    }

    public interface DownloadList {
        void checkPosition(int position);
    }

    private class ViewHolder {
        public ImageView imageMask;// 六边形封面图片遮罩
        public ImageView imageRankImage;// 电台图标
        public TextView textRankTitle;// 台名
        public TextView textRankContent;// 来源
        public TextView textSum;// 节目大小
        public View viewDelete;// 删除
    }
}
