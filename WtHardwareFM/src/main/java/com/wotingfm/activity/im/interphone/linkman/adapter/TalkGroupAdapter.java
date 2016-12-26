package com.wotingfm.activity.im.interphone.linkman.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.squareup.picasso.Picasso;
import com.wotingfm.R;
import com.wotingfm.activity.im.interphone.linkman.model.TalkGroupInside;
import com.wotingfm.common.config.GlobalConfig;
import com.wotingfm.util.BitmapUtils;

import java.util.List;

/**
 * 群组适配器
 *
 * @author 辛龙
 *         2016年3月25日
 */
public class TalkGroupAdapter extends BaseAdapter {
    private List<TalkGroupInside> list;
    private Context context;
    private OnListener onListener;
    private TalkGroupInside lists;
    private String url;

    public TalkGroupAdapter(Context context, List<TalkGroupInside> list) {
        super();
        this.list = list;
        this.context = context;
    }

    public void ChangeDate(List<TalkGroupInside> list) {
        this.list = list;
        this.notifyDataSetChanged();
    }

    public void setOnListener(OnListener onListener) {
        this.onListener = onListener;
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
            convertView = LayoutInflater.from(context).inflate(R.layout.adapter_talk_person, null);
            holder.tv_name = (TextView) convertView.findViewById(R.id.tv_name);//名
            holder.tv_b_name = (TextView) convertView.findViewById(R.id.tv_b_name);//名
            holder.imageView_touxiang = (ImageView) convertView.findViewById(R.id.image);
            holder.lin_add = (LinearLayout) convertView.findViewById(R.id.lin_add);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        lists = list.get(position);
        if (lists.getGroupName() == null || lists.getGroupName().equals("")) {
            holder.tv_name.setText("未知");//名
        } else {
            holder.tv_name.setText(lists.getGroupName());//名
        }
        if (lists.getGroupSignature()== null || lists.getGroupSignature().equals("")) {
            holder.tv_b_name.setVisibility(View.GONE);
        } else {
            holder.tv_b_name.setVisibility(View.VISIBLE);
            holder.tv_b_name.setText(lists.getGroupSignature());//名
        }
        if (lists.getGroupImg() == null || lists.getGroupImg().equals("") || lists.getGroupImg().equals("null") || lists.getGroupImg().trim().equals("")) {
            Bitmap bmp = BitmapUtils.readBitMap(context, R.mipmap.wt_image_tx_qz);
            holder.imageView_touxiang.setImageBitmap(bmp);
        } else {
//			Bitmap bmp = BitmapUtils.readBitMap(context, R.mipmap.wt_image_tx_qz);
//			holder.imageView_touxiang.setImageBitmap(bmp);
            if (lists.getGroupImg().startsWith("http:")) {
                url = lists.getGroupImg();
            } else {
                url = GlobalConfig.imageurl + lists.getGroupImg();
            }
            Picasso.with(context).load(url.replace("\\/", "/")).resize(100, 100).centerCrop().into(holder.imageView_touxiang);
        }

        holder.lin_add.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                onListener.add(position);
            }
        });
        return convertView;
    }

    public interface OnListener {
        void add(int position);
    }

    class ViewHolder {
        public TextView tv_b_name;
        public LinearLayout lin_add;
        public ImageView imageView_touxiang;
        public TextView tv_name;

    }
}
