package com.wotingfm.ui.music.player.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.AnimationDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;
import com.wotingfm.R;
import com.wotingfm.common.config.GlobalConfig;
import com.wotingfm.common.constant.StringConstant;
import com.wotingfm.ui.music.player.model.ContentPersons;
import com.wotingfm.ui.music.player.model.LanguageSearchInside;
import com.wotingfm.util.AssembleImageUrlUtils;
import com.wotingfm.util.BitmapUtils;

import java.util.List;

public class PlayerListAdapter extends BaseAdapter {
    private List<LanguageSearchInside> list;
    private Context context;
    private Bitmap bmp;

    public PlayerListAdapter(Context context, List<LanguageSearchInside> list) {
        this.context = context;
        this.list = list;
        bmp = BitmapUtils.readBitMap(context, R.mipmap.wt_image_playertx);
    }

    public void setList(List<LanguageSearchInside> list) {
        this.list = list;
        notifyDataSetChanged();
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
            convertView = LayoutInflater.from(context).inflate(R.layout.adapter_fragment_player, null);
            holder = new ViewHolder();

            // 正在播放
            holder.imagePlaying = (ImageView) convertView.findViewById(R.id.imageView_playering);
            holder.imagePlaying.setBackgroundResource(R.drawable.playering_show);
            holder.playingAnim = (AnimationDrawable) holder.imagePlaying.getBackground();

            // 六边形封面图片遮罩
            holder.imageCoverMask = (ImageView) convertView.findViewById(R.id.img_zhezhao);
            Bitmap coverMaskBitmap = BitmapUtils.readBitMap(context, R.mipmap.wt_6_b_y_b);
            holder.imageCoverMask.setImageBitmap(coverMaskBitmap);

            // 节目信息
            holder.imageRankImage = (ImageView) convertView.findViewById(R.id.RankImageUrl);// 电台图标
            holder.textRankTitle = (TextView) convertView.findViewById(R.id.RankTitle);// 台名
            holder.textRankContent = (TextView) convertView.findViewById(R.id.RankContent);// 来源 -> 专辑
            holder.imagePub = (ImageView) convertView.findViewById(R.id.image_pub);// 小图标

            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        LanguageSearchInside searchList = list.get(position);
        if (searchList != null) {
            String mediaType = searchList.getMediaType();// 节目类型

            // 播放状态
            String playType = searchList.getType();
            switch (playType) {
                case "0":// 暂停状态 按钮显示但动画停止
                    holder.playingAnim.stop();
                    holder.playingAnim.selectDrawable(0);
                    holder.imagePlaying.setVisibility(View.VISIBLE);
                    holder.textRankTitle.setTextColor(context.getResources().getColor(R.color.dinglan_orange_z));
                    break;
                case "1":// 没有播放但在列表中的节目 按钮不显示
                    holder.imagePlaying.setVisibility(View.INVISIBLE);
                    holder.textRankTitle.setTextColor(context.getResources().getColor(R.color.dinglan_orange));
                    if (holder.playingAnim.isRunning()) {
                        holder.playingAnim.stop();
                    }
                    break;
                case "2":// 播放状态  按钮显示启动动画
                    holder.imagePlaying.setVisibility(View.VISIBLE);
                    holder.textRankTitle.setTextColor(context.getResources().getColor(R.color.dinglan_orange_z));
                    holder.playingAnim.start();
                    break;
            }

            // 封面图片
            String contentImg = searchList.getContentImg();
            if (contentImg != null && !contentImg.equals("")) {
                if (!contentImg.startsWith("http")) {
                    contentImg = GlobalConfig.imageurl + contentImg;
                }
                contentImg = AssembleImageUrlUtils.assembleImageUrl180(contentImg);
                Picasso.with(context).load(contentImg.replace("\\/", "/")).into(holder.imageRankImage);
            } else {
                holder.imageRankImage.setImageBitmap(bmp);
            }

            // 节目标题
            String contentName = searchList.getContentName();
            if (contentName == null || contentName.equals("")) {
                contentName = "未知";
            }
            holder.textRankTitle.setText(contentName);

            // 节目来源 -> 专辑
            if(mediaType != null && mediaType.equals(StringConstant.TYPE_RADIO)) {
                String contentPub = searchList.getContentPub();
                if (contentPub == null || contentPub.equals("")) {
                    contentPub = "未知";
                }
                contentPub = "正在直播：" + contentPub;
                holder.textRankContent.setText(contentPub);
            } else {
                List<ContentPersons> list = searchList.getSeqInfo().getContentPersons();
                String contentPerName;
                if (list != null && list.size() >= 0) {
                    contentPerName = list.get(0).getPerName();
                } else {
                    contentPerName = searchList.getContentName();
                }
                if(contentPerName == null || contentPerName.equals("")) {
                    contentPerName = "未知";
                }
                contentPerName = "主播：" + contentPerName;
                holder.textRankContent.setText(contentPerName);
            }
        }
        return convertView;
    }

    class ViewHolder {
        public AnimationDrawable playingAnim;// 动画
        public ImageView imagePlaying;// 正在播放 显示动画
        public ImageView imageCoverMask;// 六边形封面图片遮罩
        public ImageView imageRankImage;// 封面图片
        public TextView textRankTitle;// 节目标题
        public TextView textRankContent;// 来源 -> 专辑
        public ImageView imagePub;// 小图标
    }
}
