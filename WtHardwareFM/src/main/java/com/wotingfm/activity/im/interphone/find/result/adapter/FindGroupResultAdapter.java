package com.wotingfm.activity.im.interphone.find.result.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.wotingfm.R;
import com.wotingfm.activity.im.interphone.find.result.model.FindGroupNews;
import com.wotingfm.common.config.GlobalConfig;
import com.wotingfm.helper.ImageLoader;

import java.util.List;

public class FindGroupResultAdapter extends BaseAdapter {
    private List<FindGroupNews> list;
    private Context context;
    private ImageLoader imageLoader;
//    private String url;

    public FindGroupResultAdapter(Context context, List<FindGroupNews> list) {
        super();
        this.list = list;
        this.context = context;
        imageLoader = new ImageLoader(context);
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
            holder.imageInviteImage.setImageResource(R.mipmap.wt_image_tx_qz);
        } else {
            if (invite.getGroupImg().startsWith("http:")) {
                url = invite.getGroupImg();
            } else {
                url = GlobalConfig.imageurl + invite.getGroupImg();
            }
            imageLoader.DisplayImage(url.replace("\\/", "/"), holder.imageInviteImage, false, false, null, null);
        }
        return convertView;
    }

    class ViewHolder {
        public TextView textInviteName;
        public TextView textInviteMessage;
        public ImageView imageInviteImage;
    }
}
