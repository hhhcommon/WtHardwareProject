package com.wotingfm.activity.music.program.diantai.adapter;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.wotingfm.R;
import com.wotingfm.activity.music.program.diantai.model.RadioPlay;
import com.wotingfm.activity.music.program.fmlist.activity.FMListActivity;
import com.wotingfm.activity.music.program.fmlist.model.RankInfo;
import com.wotingfm.common.config.GlobalConfig;
import com.wotingfm.helper.ImageLoader;
import com.wotingfm.util.L;

import java.util.List;

/**
 * expandableListView适配器
 */
public class onlineAdapter extends BaseExpandableListAdapter {
    private Context context;
    private List<RadioPlay> group;
    private ImageLoader imageLoader;

    public onlineAdapter(Context context, List<RadioPlay> group) {
        this.context = context;
        this.group = group;
        imageLoader = new ImageLoader(context);
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
            holder.linearMore = (LinearLayout) convertView.findViewById(R.id.lin_head_more);
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

        // 判断回调对象决定是哪个fragment的对象调用的词adapter 从而实现多种布局
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
            convertView = LayoutInflater.from(context).inflate(R.layout.adapter_rankinfo, null);
            holder.textRankTitle = (TextView) convertView.findViewById(R.id.RankTitle);     // 台名
            holder.textRankPlaying = (TextView) convertView.findViewById(R.id.RankPlaying); // 正在播放的节目
            holder.imageRankImage = (ImageView) convertView.findViewById(R.id.RankImageUrl);// 电台图标
            holder.textNumber = (TextView) convertView.findViewById(R.id.tv_num);
            holder.linearCurrentPlay = (LinearLayout) convertView.findViewById(R.id.lin_currentplay);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        RankInfo lists = group.get(groupPosition).getList().get(childPosition);
        if (lists != null) {
            if (lists.getMediaType() != null && !lists.getMediaType().equals("")) {
                if (lists.getContentName() != null && !lists.getContentName().equals("")) {
                    holder.textRankTitle.setText(lists.getContentName());
                }
                if (lists.getContentPub() != null && !lists.getContentPub().equals("")) {
                    holder.textRankPlaying.setText("正在直播：" + lists.getContentPub());
                }
                if (lists.getContentImg() != null
                        && !lists.getContentImg().equals("null") && !lists.getContentImg().trim().equals("")) {

                    String url;
                    if (lists.getContentImg().startsWith("http")) {
                        url = lists.getContentImg();
                    } else {
                        url = GlobalConfig.imageurl + lists.getContentImg();
                    }
                    imageLoader.DisplayImage(url.replace("\\/", "/"), holder.imageRankImage, false, false, null, null);
                }
                if (lists.getWatchPlayerNum() != null
                        && !lists.getWatchPlayerNum().equals("") && !lists.getWatchPlayerNum().equals("null")) {

                    holder.textNumber.setText(lists.getWatchPlayerNum());
                }
                if (!lists.getMediaType().equals("RADIO")) {// 判断 mediaType == AUDIO 的情况
                    holder.linearCurrentPlay.setVisibility(View.INVISIBLE);
                }
            } else {
                L.w("服务器返回数据MediaType为空");
            }
        }
        return convertView;

    }

    class ViewHolder {
        public ImageView imageRankImage;
        public TextView textRankPlaying;
        public TextView textRankTitle;
        public TextView textName;
        public LinearLayout linearMore;
        public TextView textNumber;
        public LinearLayout linearCurrentPlay;
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return true;
    }
}
