package com.wotingfm.ui.music.program.diantai.adapter;

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
import android.widget.LinearLayout;
import android.widget.TextView;

import com.squareup.picasso.Picasso;
import com.wotingfm.R;
import com.wotingfm.common.config.GlobalConfig;
import com.wotingfm.ui.music.program.diantai.model.RadioPlay;
import com.wotingfm.ui.music.program.fmlist.activity.FMListActivity;
import com.wotingfm.ui.music.program.fmlist.model.RankInfo;
import com.wotingfm.util.AssembleImageUrlUtils;
import com.wotingfm.util.BitmapUtils;

import java.util.List;

/**
 * expandableListView适配器
 */
public class OnlinesAdapter extends BaseExpandableListAdapter {
    private Context context;
    private List<RadioPlay> group;

    public OnlinesAdapter(Context context, List<RadioPlay> group) {
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
            convertView = LayoutInflater.from(context).inflate(R.layout.adapter_fragment_radio_list, null);
            holder = new ViewHolder();
            holder.tv_name = (TextView) convertView.findViewById(R.id.tv_name);
            holder.lin_more = (LinearLayout) convertView.findViewById(R.id.lin_head_more);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        final RadioPlay lists = group.get(groupPosition);
        if (lists.getCatalogName() == null || lists.getCatalogName().equals("")) {
            holder.tv_name.setText("未知");
        } else {
            holder.tv_name.setText(lists.getCatalogName());
        }

        // 判断回调对象决定是哪个 fragment 的对象调用的词 adapter 从而实现多种布局
        holder.lin_more.setOnClickListener(new OnClickListener() {
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
            convertView = LayoutInflater.from(context).inflate(R.layout.adapter_rankinfo, null);
            holder.textview_ranktitle = (TextView) convertView.findViewById(R.id.RankTitle);// 台名
            holder.textview_rankplaying = (TextView) convertView.findViewById(R.id.RankPlaying);// 正在播放的节目
            holder.imageview_rankimage = (ImageView) convertView.findViewById(R.id.RankImageUrl);// 电台图标

            holder.img_zhezhao = (ImageView) convertView.findViewById(R.id.img_zhezhao);
            Bitmap bmp_zhezhao = BitmapUtils.readBitMap(context, R.mipmap.wt_6_b_y_b);
            holder.img_zhezhao.setImageBitmap(bmp_zhezhao);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        RankInfo lists = group.get(groupPosition).getList().get(childPosition);

        if (lists != null) {
            String mediaType = lists.getMediaType();// 类型

            if (mediaType != null && mediaType.equals("RADIO")) {
                // 播放内容标题
                String contentName = lists.getContentName();
                if (contentName == null || contentName.equals("")) {
                    contentName = "未知";
                }
                holder.textview_ranktitle.setText(contentName);

                // 正在直播的节目
                String contentPlaying = lists.getIsPlaying();
                if (contentPlaying == null || contentPlaying.equals("")) {
                    contentPlaying = "暂无节目单";
                }
                holder.textview_rankplaying.setText(contentPlaying);

                // 封面图片
                String contentImg = lists.getContentImg();
                if (contentImg == null || contentImg.equals("null") || contentImg.trim().equals("")) {
                    Bitmap bmp = BitmapUtils.readBitMap(context, R.mipmap.wt_image_playertx_d);
                    holder.imageview_rankimage.setImageBitmap(bmp);
                } else {
                    if (!contentImg.startsWith("http")) {
                        contentImg = GlobalConfig.imageurl + contentImg;
                    }
                    contentImg = AssembleImageUrlUtils.assembleImageUrl150(contentImg);
                    Picasso.with(context).load(contentImg.replace("\\/", "/")).into(holder.imageview_rankimage);
                }
            }
        }
        return convertView;
    }

    class ViewHolder {
        public ImageView imageview_rankimage;
        public TextView textview_rankplaying;
        public TextView textview_ranktitle;
        public TextView tv_name;
        public LinearLayout lin_more;
        public ImageView img_zhezhao;
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return true;
    }
}
