package com.wotingfm.ui.music.search.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.wotingfm.R;
import com.wotingfm.common.config.GlobalConfig;
import com.wotingfm.common.constant.IntegerConstant;
import com.wotingfm.common.constant.StringConstant;
import com.wotingfm.ui.music.program.fmlist.model.RankInfo;
import com.wotingfm.util.AssembleImageUrlUtils;
import com.wotingfm.util.BitmapUtils;

import java.util.List;

public class SearchListAdapter extends BaseAdapter {
    private List<RankInfo> list;
    private Context context;
    private FavoriteCheck favoriteCheck;

    public SearchListAdapter(Context context, List<RankInfo> list) {
        this.context = context;
        this.list = list;
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

    public void setOnListener(FavoriteCheck favoriteCheck) {
        this.favoriteCheck = favoriteCheck;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            holder = new ViewHolder();
            convertView = LayoutInflater.from(context).inflate(R.layout.adapter_favoritelist, parent, false);
            holder.imageMask = (ImageView) convertView.findViewById(R.id.image_mask);// 六边形遮罩
            holder.imageMask.setImageBitmap(BitmapUtils.readBitMap(context, R.mipmap.wt_6_b_y_b));

            holder.textRankTitle = (TextView) convertView.findViewById(R.id.RankTitle);// 节目名
            holder.imageRankImage = (ImageView) convertView.findViewById(R.id.RankImageUrl);// 封面图标
            holder.textRankContent = (TextView) convertView.findViewById(R.id.RankContent);// 来源
            holder.imageCheck = (ImageView) convertView.findViewById(R.id.img_check);// 编辑状态

            holder.textNumber = (TextView) convertView.findViewById(R.id.tv_num);// 收听次数
            holder.textLast = (TextView) convertView.findViewById(R.id.tv_last);// 时长 OR 集数
            holder.imageLast = (ImageView) convertView.findViewById(R.id.image_last);// 图标  时长
            holder.imageNum = (ImageView) convertView.findViewById(R.id.image_num);// 图标  集数
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        RankInfo lists = list.get(position);
        String mediaType = lists.getMediaType();// 类型

        // 封面图片
        String contentImage = lists.getContentImg();
        if (contentImage == null || contentImage.equals("null") || contentImage.trim().equals("")) {
            holder.imageRankImage.setImageBitmap(BitmapUtils.readBitMap(context, R.mipmap.wt_image_playertx_d));
        } else {
            if (!contentImage.startsWith("http:")) {
                contentImage = GlobalConfig.imageurl + contentImage;
            }
            String _url = AssembleImageUrlUtils.assembleImageUrl180(contentImage);

            // 加载图片
            AssembleImageUrlUtils.loadImage(_url, contentImage, holder.imageRankImage, IntegerConstant.TYPE_LIST);
        }

        // 节目名
        String contentName = lists.getContentName();
        if (contentName != null && !contentName.equals("")) {
            holder.textRankTitle.setText(contentName);
        }

        // 来源 -> 专辑
        if (mediaType.equals(StringConstant.TYPE_RADIO)) {
            String currentContent = lists.getCurrentContent();
            if (currentContent == null || currentContent.equals("")) {
                currentContent = "未知";
            }
            currentContent = "正在直播：" + currentContent;
            holder.textRankContent.setText(currentContent);
        } else {
            String contentSequ = lists.getSequName();
            if (contentSequ == null || contentSequ.equals("")) {
                contentSequ = "未知";
            }
            holder.textRankContent.setText(contentSequ);
        }

        // 收听次数
        String watchNum = lists.getPlayCount();
        if (watchNum == null || watchNum.equals("")) {
            watchNum = "1234";
        }
        float playCount = Float.valueOf(watchNum);
        if (playCount > 10000) {
            playCount = playCount / 10000;
            watchNum = playCount + "万";
            if (playCount >= 10000) {
                playCount = playCount / 10000;
                watchNum = playCount + "亿";
            }
        }
        holder.textNumber.setText(watchNum);

        switch (mediaType) {
            case StringConstant.TYPE_RADIO:// 电台
                holder.textLast.setVisibility(View.GONE);
                holder.imageLast.setVisibility(View.GONE);
                holder.imageNum.setVisibility(View.GONE);
                break;
            case StringConstant.TYPE_SEQU:// 专辑
                holder.imageLast.setVisibility(View.GONE);
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
            case StringConstant.TYPE_AUDIO:// 声音
            case StringConstant.TYPE_TTS:// TTS
                holder.imageNum.setVisibility(View.GONE);
                holder.imageLast.setVisibility(View.VISIBLE);
                holder.textLast.setVisibility(View.VISIBLE);

                // 节目时长
                String contentTime = lists.getContentTimes();
                if (contentTime == null || contentTime.equals("") || contentTime.equals("null")) {
                    holder.textLast.setText(context.getString(R.string.play_time));
                } else {
                    int minute = Integer.valueOf(contentTime) / (1000 * 60);
                    int second = (Integer.valueOf(contentTime) / 1000) % 60;
                    if (second < 10) {
                        holder.textLast.setText(minute + "\'" + " " + "0" + second + "\"");
                    } else {
                        holder.textLast.setText(minute + "\'" + " " + second + "\"");
                    }
                }
                break;
        }

        if (lists.getViewtype() == 0) {// 0 状态时 为点选框隐藏状态设置当前的选择状态为 0
            holder.imageCheck.setVisibility(View.GONE);
        } else {// 1 状态 此时设置 choiceType 生效
            holder.imageCheck.setVisibility(View.VISIBLE);
            if (lists.getChecktype() == 0) {
                Bitmap bmp = BitmapUtils.readBitMap(context, R.mipmap.wt_group_nochecked);// 未点击状态
                holder.imageCheck.setImageBitmap(bmp);
            } else {
                Bitmap bmp = BitmapUtils.readBitMap(context, R.mipmap.wt_group_checked); // 点击状态
                holder.imageCheck.setImageBitmap(bmp);
            }
        }
        holder.imageCheck.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                favoriteCheck.checkPosition(position);
            }
        });
        return convertView;
    }

    public interface FavoriteCheck {
        void checkPosition(int position);
    }

    private class ViewHolder {
        public ImageView imageMask;// 六边形封面遮罩
        public ImageView imageRankImage;// 封面
        public TextView textRankTitle;// 标题
        public TextView textRankContent;// 来源
        public ImageView imageCheck;// 编辑状态

        public TextView textNumber;// 收听次数
        public TextView textLast;// 时长 OR 集数
        public ImageView imageLast;// 图标  时长
        public ImageView imageNum;// 图标  集数
    }
}
