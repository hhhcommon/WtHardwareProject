package com.wotingfm.activity.music.download.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.squareup.picasso.Picasso;
import com.wotingfm.R;
import com.wotingfm.activity.music.download.model.FileInfo;
import com.wotingfm.util.AssembleImageUrlUtils;
import com.wotingfm.util.BitmapUtils;

import java.text.DecimalFormat;
import java.util.List;

public class DownLoadSequAdapter extends BaseAdapter {
    private List<FileInfo> list;
    private Context context;
    private downloadSequCheck downloadCheck;
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
    public void setOnListener(downloadSequCheck downloadCheck) {
        this.downloadCheck = downloadCheck;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            holder = new ViewHolder();
            convertView = LayoutInflater.from(context).inflate(R.layout.adapter_download_complete, null);
            holder.textview_ranktitle = (TextView) convertView.findViewById(R.id.RankTitle);// 台名
            holder.imageview_rankimage = (ImageView) convertView.findViewById(R.id.RankImageUrl);// 电台图标
            holder.tv_RankContent = (TextView) convertView.findViewById(R.id.RankContent);
            holder.img_check= (ImageView) convertView.findViewById(R.id.img_check);
            holder.lin_check=(LinearLayout)convertView.findViewById(R.id.lin_check);
            holder.tv_count=(TextView)convertView.findViewById(R.id.tv_count);
            holder.tv_sum=(TextView)convertView.findViewById(R.id.tv_sum);
            holder.img_liu = (ImageView)convertView.findViewById(R.id.img_liu);
            Bitmap bmp = BitmapUtils.readBitMap(context, R.mipmap.wt_6_b_y_b);
            holder.img_liu.setImageBitmap(bmp);
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
            Bitmap bmp = BitmapUtils.readBitMap(context, R.mipmap.wt_image_playertx);
            holder.imageview_rankimage.setImageBitmap(bmp);
        } else {
            String url = AssembleImageUrlUtils.assembleImageUrl150(lists.getSequimgurl());
            Picasso.with(context).load(url.replace("\\/", "/")).resize(100, 100).centerCrop().into(holder.imageview_rankimage);
        }
        if (lists.getPlayFrom() == null || lists.getPlayFrom() .equals("")) {
            holder.tv_RankContent.setText("我听科技");
        } else {
            holder.tv_RankContent.setText(lists.getPlayFrom());
        }
        if(lists.getViewtype()==0){
			/*0状态时 为点选框隐藏状态
			 *设置当前的选择状态为0
			 */
            holder.lin_check.setVisibility(View.GONE);

        }else{
            //1状态 此时设置choicetype生效
            holder.lin_check.setVisibility(View.VISIBLE);
            if(lists.getChecktype()==0){
                //未点击状态
                holder.img_check.setImageResource(R.mipmap.wt_group_nochecked);
            }else{
                //点击状态
                holder.img_check.setImageResource(R.mipmap.wt_group_checked);
            }
        }
        if(lists.getCount()!=-1){
            holder.tv_count.setText(lists.getCount()+"集");
        }

        if(lists.getSum()!=-1){
            holder.tv_sum.setText(df.format(lists.getSum() / 1000.0 / 1000.0) + "MB");
        }
        holder.lin_check.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                downloadCheck.checkposition(position);
            }
        });
        return convertView;
    }

    public interface downloadSequCheck {
        void checkposition(int position);
    }

    private class ViewHolder {
        public ImageView imageview_rankimage;
        public TextView textview_ranktitle;
        public TextView tv_RankContent;
        public ImageView img_check;
        public LinearLayout lin_check;
        public TextView tv_count;
        public TextView tv_sum;
        public ImageView img_liu;
    }
}
