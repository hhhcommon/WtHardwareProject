package com.wotingfm.ui.music.search.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;
import com.wotingfm.R;
import com.wotingfm.ui.music.program.fmlist.model.RankInfo;
import com.wotingfm.ui.music.search.model.SuperRankInfo;
import com.wotingfm.common.config.GlobalConfig;
import com.wotingfm.util.AssembleImageUrlUtils;
import com.wotingfm.util.BitmapUtils;

import java.util.List;

public class SearchContentAdapter extends BaseExpandableListAdapter {
    private Context context;
    private List<SuperRankInfo> mSuperRankInfo;

    public SearchContentAdapter(Context context, List<SuperRankInfo> mSuperRankInfo) {
        this.context = context;
        this.mSuperRankInfo = mSuperRankInfo;
    }

    @Override
    public int getGroupCount() {
        return mSuperRankInfo.size();
    }

    @Override
    public int getChildrenCount(int groupPosition) {
        return mSuperRankInfo.get(groupPosition).getList().size();
    }

    @Override
    public Object getGroup(int groupPosition) {
        return mSuperRankInfo.get(groupPosition);
    }

    @Override
    public Object getChild(int groupPosition, int childPosition) {
        return mSuperRankInfo.get(groupPosition).getList().get(childPosition);
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

    @Override
    public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.adapter_fragment_radio_list, parent, false);
            holder = new ViewHolder();
            holder.textName = (TextView) convertView.findViewById(R.id.tv_name);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        String key = mSuperRankInfo.get(groupPosition).getKey();
        if (key != null && !key.equals("")) {
            if (key.equals("AUDIO")) {
                holder.textName.setText("声音");
            } else if (key.equals("RADIO")) {
                holder.textName.setText("电台");
            } else if (key.equals("SEQU")) {
                holder.textName.setText("专辑");
            } else if (key.equals("TTS")) {
                holder.textName.setText("TTS");
            }
        } else {
            holder.textName.setText("我听");
        }
        return convertView;
    }

    @Override
    public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
        final ViewHolder holder;
        if (convertView == null) {
            holder = new ViewHolder();
            convertView = LayoutInflater.from(context).inflate(R.layout.adapter_search_content, parent, false);
            holder.imageMask = (ImageView) convertView.findViewById(R.id.img_zhezhao);// 六边形遮罩
            holder.imageMask.setImageBitmap(BitmapUtils.readBitMap(context, R.mipmap.wt_6_b_y_b));

            holder.textRankTitle = (TextView) convertView.findViewById(R.id.RankTitle);// 台名
            holder.imagePlaying = (ImageView) convertView.findViewById(R.id.image_playing);// 来源小图标
            holder.textPlaying = (TextView) convertView.findViewById(R.id.text_playing);// "正在直播"
            holder.textRankPlaying = (TextView) convertView.findViewById(R.id.RankPlaying);// 正在播放的节目
            holder.imageRankImage = (ImageView) convertView.findViewById(R.id.RankImageUrl);// 电台图标
            holder.textNumber = (TextView) convertView.findViewById(R.id.tv_num);// 收听次数
            holder.textLast = (TextView) convertView.findViewById(R.id.tv_last);// 时长 OR 集数
            holder.imageLast = (ImageView) convertView.findViewById(R.id.image_last);// 图标  时长
            holder.imageNum = (ImageView) convertView.findViewById(R.id.image_num);// 图标  集数
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        RankInfo lists = mSuperRankInfo.get(groupPosition).getList().get(childPosition);
        String mediaType = lists.getMediaType();// 类型

        // 标题
        String contentName = lists.getContentName();
        if (contentName == null || contentName.equals("")) {
            contentName = "未知";
        }
        holder.textRankTitle.setText(contentName);

        // 收听次数
        String playCount = lists.getPlayCount();
        if (playCount == null || playCount.equals("")) {
            playCount = "1234";
        }
        float count = Float.valueOf(playCount);
        if(count >= 10000) {
            count = count / 10000;
            playCount = count + "万";
            if(count >= 10000) {
                count = count / 10000;
                playCount = count + "亿";
            }
        }
        holder.textNumber.setText(playCount);

        // 封面
        String contentImage = lists.getContentImg();
        if (contentImage == null || lists.getContentImg().equals("null") || lists.getContentImg().trim().equals("")) {
            holder.imageRankImage.setImageBitmap(BitmapUtils.readBitMap(context, R.mipmap.wt_bg_noimage));
        } else {
            String url1;
            if (lists.getContentImg().startsWith("http")) {
                url1 = lists.getContentImg();
            } else {
                url1 = GlobalConfig.imageurl + lists.getContentImg();
            }
            url1 = AssembleImageUrlUtils.assembleImageUrl150(url1);
            Picasso.with(context).load(url1.replace("\\/", "/")).resize(100, 100).centerCrop().into(holder.imageRankImage);
        }

        // 来源
        if(mediaType.equals("RADIO")) {
            if (lists.getCurrentContent() != null && !lists.getCurrentContent().equals("")) {
//                holder.textRankPlaying.setText(lists.getCurrentContent());
                holder.textRankPlaying.setText(lists.getCurrentContent());
            }
        } else {
            if (lists.getContentPub() != null && !lists.getContentPub().equals("")) {
                holder.textRankPlaying.setText(lists.getContentPub());
            }
        }

        switch (mediaType) {
            case "RADIO":// 电台
                holder.textLast.setVisibility(View.GONE);
                holder.imageLast.setVisibility(View.GONE);
                holder.imageNum.setVisibility(View.GONE);
                holder.imagePlaying.setVisibility(View.GONE);
                holder.textPlaying.setVisibility(View.VISIBLE);
                break;
            case "SEQU":// 专辑
                holder.textPlaying.setVisibility(View.GONE);
                holder.imageLast.setVisibility(View.GONE);
                holder.imagePlaying.setVisibility(View.VISIBLE);
                holder.imageNum.setVisibility(View.VISIBLE);
                holder.textLast.setVisibility(View.VISIBLE);

                // 集数
                String contentSubCount = lists.getContentSubCount();
                if (contentSubCount == null || contentSubCount.equals("") || contentSubCount.equals("null")) {
                    holder.textLast.setText("0" + "集");
                } else {
                    holder.textLast.setText(contentSubCount + "集");
                }
                break;
            case "AUDIO":// 声音
            case "TTS":// TTS
                holder.textPlaying.setVisibility(View.GONE);
                holder.imageNum.setVisibility(View.GONE);
                holder.imagePlaying.setVisibility(View.VISIBLE);
                holder.imageLast.setVisibility(View.VISIBLE);
                holder.textLast.setVisibility(View.VISIBLE);

                // 节目时长
                String contentTime = lists.getContentTimes();
                if (contentTime != null&& !contentTime.equals("")) {
                    int minute = Integer.valueOf(contentTime) / (1000 * 60);
                    int second = (Integer.valueOf(contentTime) / 1000) % 60;
                    if(second < 10){
                        contentTime = minute + "\'" + " " + "0" + second + "\"";
                    }else{
                        contentTime = minute + "\'" + " " + second + "\"";
                    }
                    holder.textLast.setText(contentTime);
                }
                break;
        }
        return convertView;
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return true;
    }

    class ViewHolder {
        public ImageView imageMask;// 遮罩
        public ImageView imageRankImage;// 图标
        public ImageView imagePlaying;// 来源小图标
        public TextView textPlaying;// "正在直播"
        public TextView textRankPlaying;// 正在播放的节目
        public TextView textRankTitle;// 台名
        public TextView textName;
        public TextView textNumber;// 收听次数
        public TextView textLast;// 时长 OR 集数
        public ImageView imageLast;// 图标  时长
        public ImageView imageNum;// 图标  集数
    }
}
