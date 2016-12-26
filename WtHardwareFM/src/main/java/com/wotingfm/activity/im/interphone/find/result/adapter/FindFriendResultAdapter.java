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
import com.wotingfm.activity.im.interphone.find.result.model.UserInviteMeInside;
import com.wotingfm.common.config.GlobalConfig;
import com.wotingfm.util.AssembleImageUrlUtils;
import com.wotingfm.util.BitmapUtils;

import java.util.List;

public class FindFriendResultAdapter extends BaseAdapter {
    private List<UserInviteMeInside> list;
    private Context context;
    private String url;

    public FindFriendResultAdapter(Context context, List<UserInviteMeInside> list) {
        super();
        this.list = list;
        this.context = context;
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
        ViewHolder holder;
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.adapter_contactquery, null);
            holder = new ViewHolder();
            holder.textview_invitename = (TextView) convertView.findViewById(R.id.RankTitle);        //人名
            holder.textview_invitemessage = (TextView) convertView.findViewById(R.id.RankContent);    //介绍
            holder.tv_b_id = (TextView) convertView.findViewById(R.id.RankId);//id
            holder.imageview_inviteimage = (ImageView) convertView.findViewById(R.id.RankImageUrl);//该人头像
            holder.img_zhezhao = (ImageView) convertView.findViewById(R.id.img_zhezhao);
            Bitmap bmp_zhezhao = BitmapUtils.readBitMap(context, R.mipmap.wt_6_b_y_b);
            holder.img_zhezhao.setImageBitmap(bmp_zhezhao);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        UserInviteMeInside Inviter = list.get(position);
        if (Inviter.getUserName() == null || Inviter.getUserName().equals("")) {
            holder.textview_invitename.setText("未知");
        } else {
            holder.textview_invitename.setText(Inviter.getUserName());
        }

        if (Inviter.getUserNum() == null || Inviter.getUserNum().equals("")) {
            holder.tv_b_id.setVisibility(View.GONE);
        } else {
            holder.tv_b_id.setVisibility(View.VISIBLE);
            holder.tv_b_id.setText("ID: " + Inviter.getUserNum());//id
        }


        if (Inviter.getUserSign() == null || Inviter.getUserSign().equals("")) {
            holder.textview_invitemessage.setVisibility(View.GONE);
//			holder.textview_invitemessage.setText("驾车体验生活");
        } else {
            holder.textview_invitemessage.setVisibility(View.VISIBLE);
            holder.textview_invitemessage.setText("" + Inviter.getUserSign());
        }
        if (Inviter.getPortraitMini() == null || Inviter.getPortraitMini().equals("")
                || Inviter.getPortraitMini().equals("null") || Inviter.getPortraitMini().trim().equals("")) {
            Bitmap bmp = BitmapUtils.readBitMap(context, R.mipmap.wt_image_tx_hy);
            holder.imageview_inviteimage.setImageBitmap(bmp);
        } else {
            if (Inviter.getPortraitMini().startsWith("http:")) {
                url = Inviter.getPortraitMini();
            } else {
                url = GlobalConfig.imageurl + Inviter.getPortraitMini();
            }
            url = AssembleImageUrlUtils.assembleImageUrl150(url);
            Picasso.with(context).load(url.replace("\\/", "/")).resize(100, 100).centerCrop().into(holder.imageview_inviteimage);
        }
        return convertView;
    }

    class ViewHolder {
        public TextView textview_invitename;
        public TextView textview_invitemessage;
        public ImageView imageview_inviteimage;
        public ImageView img_zhezhao;
        public TextView tv_b_id;
    }
}
