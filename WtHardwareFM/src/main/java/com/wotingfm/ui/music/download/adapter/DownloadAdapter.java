package com.wotingfm.ui.music.download.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.squareup.picasso.Picasso;
import com.wotingfm.R;
import com.wotingfm.ui.music.download.model.FileInfo;
import com.wotingfm.util.AssembleImageUrlUtils;
import com.wotingfm.util.BitmapUtils;
import com.wotingfm.widget.CircleProgress;

import java.text.DecimalFormat;
import java.util.List;

/**
 * 下载中数据展示
 */
public class DownloadAdapter extends BaseAdapter {
    private List<FileInfo> list;
    private Context context;
    private DecimalFormat df;

    public DownloadAdapter(Context context, List<FileInfo> list) {
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

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            holder = new ViewHolder();
            convertView = LayoutInflater.from(context).inflate(R.layout.adapter_uncompelete, null);

            // 六边形封面图片遮罩
            Bitmap maskBitmap = BitmapUtils.readBitMap(context, R.mipmap.wt_6_b_y_b);
            holder.imageMask = (ImageView) convertView.findViewById(R.id.img_liu);
            holder.imageMask.setImageBitmap(maskBitmap);

            holder.imageCover = (ImageView) convertView.findViewById(R.id.img_touxiang);// 封面图片
            holder.textRankTitle = (TextView) convertView.findViewById(R.id.RankTitle);// 节目标题
            holder.textAuthor = (TextView) convertView.findViewById(R.id.tv_author);// 来源

            holder.imageWaitDownload = (ImageView) convertView.findViewById(R.id.img_play);// 图标 等待下载
            holder.viewDownload = (RelativeLayout) convertView.findViewById(R.id.rv_download);// 下载中视图
            holder.imageCircle = (CircleProgress) convertView.findViewById(R.id.roundBar2);// 下载中显示进度圆形图片
            holder.viewBoard = (LinearLayout) convertView.findViewById(R.id.lin_downloadboard);// 下载大小视图

            holder.textStart = (TextView) convertView.findViewById(R.id.download_start);// 已下载文件大小
            holder.textEnd = (TextView) convertView.findViewById(R.id.download_end);// 文件总大小

            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        FileInfo lists = list.get(position);

        // 封面图片
        String imageUrl = lists.getImageurl();
        if (imageUrl == null || imageUrl.equals("null") || imageUrl.trim().equals("")) {
            holder.imageCover.setImageBitmap(BitmapUtils.readBitMap(context, R.mipmap.wt_image_playertx));
        } else {
            imageUrl = AssembleImageUrlUtils.assembleImageUrl150(imageUrl);
            Picasso.with(context).load(imageUrl.replace("\\/", "/")).resize(100, 100).centerCrop().into(holder.imageCover);
        }

        // 节目标题
        String fileName = lists.getFileName();
        if (fileName == null || fileName.equals("")) {
            fileName = "未知";
        }
        holder.textRankTitle.setText(fileName);

        // 来源
        String playFrom = lists.getPlayFrom();
        if (playFrom == null || playFrom.equals("null") || playFrom.trim().equals("") || playFrom.trim().equals("author")) {
            playFrom = "未知";
        }
        holder.textAuthor.setText(playFrom);

        // 下载状态  下载中 OR  等待下载
        int downLoadType = lists.getDownloadtype();
        if (downLoadType == 0) {// 未下载
            holder.imageWaitDownload.setImageResource(R.mipmap.wt_img_download_waiting);
            holder.imageWaitDownload.setVisibility(View.VISIBLE);

            holder.viewBoard.setVisibility(View.GONE);
            holder.viewDownload.setVisibility(View.GONE);
        } else {// 暂停
            holder.imageWaitDownload.setVisibility(View.GONE);

            holder.viewBoard.setVisibility(View.VISIBLE);
            holder.viewDownload.setVisibility(View.VISIBLE);

            // 文件总大小
            String endString;
            int end = lists.getEnd();
            if (end >= 0) {
                endString = df.format(end / 1000.0 / 1000.0) + "MB";
            } else {
                endString = df.format(0 / 1000.0 / 1000.0) + "MB";
            }
            holder.textEnd.setText(endString);

            // 已下载文件大小
            int start = lists.getStart();
            if (start >= 0) {
                float a = (float) start;
                float b = (float) end;
                String c = df.format(a / b);
                int d = (int) (Float.parseFloat(c) * 100);
                holder.imageCircle.setMainProgress(d);
                holder.textStart.setText(df.format(start / 1000.0 / 1000.0) + "MB/");
            } else {
                holder.imageCircle.setMainProgress(0);
                holder.textStart.setText(df.format(0 / 1000.0 / 1000.0) + "MB/");
            }
        }
        return convertView;
    }

    public void updateProgress(String url, int start, int end) {
        int id = 0;
        for (int i = 0; i < list.size(); i++) {
            if (list.get(i).getUrl().trim().equals(url)) {
                id = i;
                break;
            }
        }
        if (list != null && list.size() != 0) {
            FileInfo fileInfo = list.get(id);
            fileInfo.setFinished(start / end);
            fileInfo.setStart(start);
            fileInfo.setEnd(end);
            notifyDataSetChanged();
        }
    }

    private class ViewHolder {
        public ImageView imageMask;// 六边形封面图片遮罩
        public ImageView imageCover;// 封面图片
        public TextView textRankTitle;// 节目标题
        public TextView textAuthor;// 来源

        public ImageView imageWaitDownload;// 图标 等待下载
        public RelativeLayout viewDownload;// 下载中视图

        public CircleProgress imageCircle;// 下载中显示进度圆形图片
        public LinearLayout viewBoard;// 下载大小视图

        public TextView textStart;// 已下载的文件大小
        public TextView textEnd;// 文件总大小
    }
}
