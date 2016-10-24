package com.wotingfm.activity.im.interphone.find.result.adapter;

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
import com.wotingfm.activity.im.interphone.find.result.model.FindGroupNews;
import com.wotingfm.common.config.GlobalConfig;
import com.wotingfm.util.BitmapUtils;

import java.util.List;

public class FindGroupResultAdapter extends BaseAdapter {
    private List<FindGroupNews> list;
    private Context context;

    public FindGroupResultAdapter(Context context, List<FindGroupNews> list) {
        super();
        this.list = list;
        this.context = context;
    }

    public void ChangeData(List<FindGroupNews> list) {
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
        String url;
        ViewHolder holder;
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.adapter_contactquery, parent, false);
            holder = new ViewHolder();
            holder.textInviteName = (TextView) convertView.findViewById(R.id.rank_title);        // 人名
            holder.textInviteMessage = (TextView) convertView.findViewById(R.id.rank_content);    // 介绍
            holder.imageInviteImage = (ImageView) convertView.findViewById(R.id.rank_image_url);    // 该人头像
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        FindGroupNews invite = list.get(position);
        if (invite.getGroupName() == null || invite.getGroupName().equals("")) {
            holder.textInviteName.setText("未知");
        } else {
            holder.textInviteName.setText(invite.getGroupName());
        }
        if (invite.getGroupOriDesc() == null || invite.getGroupOriDesc().equals("")) {
            holder.textInviteMessage.setVisibility(View.GONE);
        } else {
            holder.textInviteMessage.setVisibility(View.VISIBLE);
            holder.textInviteMessage.setText(invite.getGroupOriDesc());
        }
        if (invite.getGroupImg() == null || invite.getGroupImg().equals("")
                || invite.getGroupImg().equals("null") || invite.getGroupImg().trim().equals("")) {
            Bitmap bmp = BitmapUtils.readBitMap(context, R.mipmap.wt_image_tx_qz);
            holder.imageInviteImage.setImageBitmap(bmp);
        } else {
            if (invite.getGroupImg().startsWith("http:")) {
                url = invite.getGroupImg();
            } else {
                url = GlobalConfig.imageurl + invite.getGroupImg();
            }
            Picasso.with(context).load(url.replace("\\/", "/")).resize(100, 100).centerCrop().into(holder.imageInviteImage);
        }
        return convertView;
    }

    class ViewHolder {
        public TextView textInviteName;
        public TextView textInviteMessage;
        public ImageView imageInviteImage;
    }
}
