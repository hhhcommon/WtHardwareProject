package com.wotingfm.activity.im.interphone.find.result.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.wotingfm.R;
import com.wotingfm.activity.im.interphone.find.result.model.UserInviteMeInside;
import com.wotingfm.common.config.GlobalConfig;
import com.wotingfm.helper.ImageLoader;

import java.util.List;

public class FindFriendResultAdapter extends BaseAdapter {
    private List<UserInviteMeInside> list;
    private Context context;
    private ImageLoader imageLoader;
//    private String url;

    public FindFriendResultAdapter(Context context, List<UserInviteMeInside> list) {
        super();
        this.list = list;
        this.context = context;
        imageLoader = new ImageLoader(context);
    }

    public void ChangeData(List<UserInviteMeInside> list) {
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
            holder.textInviteName = (TextView) convertView.findViewById(R.id.rank_title);//人名
            holder.textInviteMessage = (TextView) convertView.findViewById(R.id.rank_content);//介绍
            holder.imageInviteImage = (ImageView) convertView.findViewById(R.id.rank_image_url);//该人头像
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        UserInviteMeInside Inviter = list.get(position);
        if (Inviter.getUserName() == null || Inviter.getUserName().equals("")) {
            holder.textInviteName.setText("未知");
        } else {
            holder.textInviteName.setText(Inviter.getUserName());
        }
        if (Inviter.getDescn() == null || Inviter.getDescn().equals("")) {
            holder.textInviteMessage.setText("驾车体验生活");
        } else {
            holder.textInviteMessage.setVisibility(View.VISIBLE);
            holder.textInviteMessage.setText("" + Inviter.getDescn());
        }
        if (Inviter.getPortraitMini() == null || Inviter.getPortraitMini().equals("")
                || Inviter.getPortraitMini().equals("null") || Inviter.getPortraitMini().trim().equals("")) {
            holder.imageInviteImage.setImageResource(R.mipmap.wt_image_tx_hy);
        } else {
            if (Inviter.getPortraitMini().startsWith("http:")) {
                url = Inviter.getPortraitMini();
            } else {
                url = GlobalConfig.imageurl + Inviter.getPortraitMini();
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
