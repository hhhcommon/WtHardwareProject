package com.wotingfm.activity.music.program.diantai.adapter;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;
import com.wotingfm.R;
import com.wotingfm.activity.music.program.diantai.model.RadioPlay;
import com.wotingfm.activity.music.program.fmlist.activity.FMListActivity;
import com.wotingfm.activity.music.program.fmlist.model.RankInfo;
import com.wotingfm.common.config.GlobalConfig;
import com.wotingfm.util.AssembleImageUrlUtils;
import com.wotingfm.util.BitmapUtils;
import com.wotingfm.util.L;

import java.util.List;

/**
 * expandableListView 适配器
 */
public class onlineAdapter extends BaseExpandableListAdapter {
    private Context context;
    private List<RadioPlay> group;

    public onlineAdapter(Context context, List<RadioPlay> group) {
        this.context = context;
        this.group = group;
    }

    @Override
    public int getGroupCount() {
        return group.size();
    }

    @Override
    public int getChildrenCount(int groupPosition) {
        return group.get(groupPosition).getList().size();
    }

    @Override
    public Object getGroup(int groupPosition) {
        return group.get(groupPosition);
    }

    @Override
    public Object getChild(int groupPosition, int childPosition) {
        return group.get(groupPosition).getList().get(childPosition);
    }

    @Override
    public long getGroupId(int groupPosition) {
        return groupPosition;
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return childPosition;
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }

    /**
     * 显示：group
     */
    @Override
    public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
        final ViewHolder holder;
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.adapter_fragment_radio_list, parent, false);
            holder = new ViewHolder();
            holder.textName = (TextView) convertView.findViewById(R.id.tv_name);
            holder.linearMore = convertView.findViewById(R.id.lin_head_more);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        final RadioPlay lists = group.get(groupPosition);
        if (lists.getCatalogName() == null || lists.getCatalogName().equals("")) {
            holder.textName.setText("未知");
        } else {
            holder.textName.setText(lists.getCatalogName());
        }

        // 判断回调对象决定是哪个 fragment 的对象调用的词 adapter 从而实现多种布局
        holder.linearMore.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, FMListActivity.class);
                Bundle bundle = new Bundle();
                bundle.putString("Position", "GROUP");
                bundle.putSerializable("list", lists);
                intent.putExtras(bundle);
                context.startActivity(intent);
            }
        });
        return convertView;
    }

    /**
     * 显示：child
     */
    @Override
    public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
        final ViewHolder holder;
        if (convertView == null) {
            holder = new ViewHolder();
            convertView = LayoutInflater.from(context).inflate(R.layout.adapter_rankinfo, parent, false);
            holder.imageMask = (ImageView) convertView.findViewById(R.id.img_zhezhao);// 六边形遮罩
            holder.imageMask.setImageBitmap(BitmapUtils.readBitMap(context, R.mipmap.wt_6_b_y_b));

            holder.textRankTitle = (TextView) convertView.findViewById(R.id.RankTitle);     // 台名
            holder.textRankPlaying = (TextView) convertView.findViewById(R.id.RankPlaying); // 正在播放的节目
            holder.imageRankImage = (ImageView) convertView.findViewById(R.id.RankImageUrl);// 电台图标
            holder.textNumber = (TextView) convertView.findViewById(R.id.tv_num);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        RankInfo lists = group.get(groupPosition).getList().get(childPosition);
        if (lists != null && lists.getMediaType() != null) {
            String contentName = lists.getContentName();
            // 电台名
            if (contentName != null && !contentName.equals("")) {
                holder.textRankTitle.setText(lists.getContentName());
            }

            // 正在直播的节目
            String contentPub = lists.getContentPub();
            if (contentPub != null && !contentPub.equals("")) {
                holder.textRankPlaying.setText("正在直播：" + contentPub);
            }

            // 封面图片
            String contentImage = lists.getContentImg();
            if (contentImage != null && !contentImage.equals("null") && !contentImage.trim().equals("")) {
                String url;
                if (contentImage.startsWith("http")) {
                    url = contentImage;
                } else {
                    url = GlobalConfig.imageurl + contentImage;
                }
                url = AssembleImageUrlUtils.assembleImageUrl150(url);
                Picasso.with(context).load(url.replace("\\/", "/")).resize(100, 100).centerCrop().into(holder.imageRankImage);
            }else{
                Bitmap bmp = BitmapUtils.readBitMap(context, R.mipmap.wt_bg_noimage);
                holder.imageRankImage.setImageBitmap(bmp);
            }

            // 收听次数
            String watchNum = lists.getWatchPlayerNum();
            if (watchNum != null && !watchNum.equals("") && !watchNum.equals("null")) {
                holder.textNumber.setText(watchNum);
            }
        } else {
            L.w("服务器返回数据 MediaType 为空");
        }
        return convertView;

    }

    class ViewHolder {
        public ImageView imageRankImage;
        public TextView textRankPlaying;
        public TextView textRankTitle;
        public TextView textName;
        public View linearMore;
        public TextView textNumber;
        public ImageView imageMask;// 遮罩
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return true;
    }
}
