package com.wotingfm.ui.music.player.fragment.more;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;
import com.wotingfm.R;
import com.wotingfm.common.config.GlobalConfig;
import com.wotingfm.ui.baseactivity.AppBaseActivity;
import com.wotingfm.util.AssembleImageUrlUtils;
import com.wotingfm.util.BitmapUtils;
import com.wotingfm.widget.RoundImageView;

/**
 * 播放节目详情
 */
public class PlayDetailsActivity extends AppBaseActivity implements View.OnClickListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_play_details);

        initView();
        initEvent();
    }

    // 初始化视图
    private void initView() {
        if(GlobalConfig.playerObject == null || GlobalConfig.playerObject.getMediaType() == null) return ;
        String mediaType = GlobalConfig.playerObject.getMediaType();// 播放节目类型

        // 六边形封面图片遮罩
        ImageView imageMask = (ImageView) findViewById(R.id.image_mask);
        imageMask.setImageBitmap(BitmapUtils.readBitMap(context, R.mipmap.wt_6_b_y_b));

        // 封面图片
        ImageView imageCover = (ImageView) findViewById(R.id.image_cover);
        String playImage = GlobalConfig.playerObject.getContentImg();
        if (playImage == null || playImage.equals("null") || playImage.trim().equals("")) {
            imageCover.setImageBitmap(BitmapUtils.readBitMap(context, R.mipmap.wt_image_playertx));
        } else {
            if(!playImage.startsWith("http")) {
                playImage = GlobalConfig.imageurl + playImage;
            }
            playImage = AssembleImageUrlUtils.assembleImageUrl150(playImage);
            Picasso.with(context).load(playImage.replace("\\/", "/")).resize(100, 100).centerCrop().into(imageCover);
        }

        // 节目标题
        TextView textTitle = (TextView) findViewById(R.id.text_title);
        String playName = GlobalConfig.playerObject.getContentName();
        if (playName == null || playName.equals("")) {
            playName = "未知";
        }
        textTitle.setText(playName);

        // 播放节目来源
        TextView textContent = (TextView) findViewById(R.id.rank_content);
        String contentPub = GlobalConfig.playerObject.getContentPub();
        if(contentPub == null || contentPub.equals("")) {
            contentPub = "未知";
        }
        textContent.setText(contentPub);

        // 播放次数
        TextView textNumber = (TextView) findViewById(R.id.text_number);
        String playCount = GlobalConfig.playerObject.getPlayCount();
        if(playCount == null || playCount.equals("")) {
            playCount = "1234";
        }
        textNumber.setText(playCount);

        // 节目时长
        ImageView imageLast = (ImageView) findViewById(R.id.image_last);// 节目时长小图标
        TextView textLast = (TextView) findViewById(R.id.tv_last);
        if(mediaType.equals("AUDIO")) {
            String contentTime = GlobalConfig.playerObject.getContentTimes();
            if (contentTime == null|| contentTime.equals("") || contentTime.equals("null")) {
                contentTime = context.getString(R.string.play_time);
            } else {
                int minute = Integer.valueOf(contentTime) / (1000 * 60);
                int second = (Integer.valueOf(contentTime) / 1000) % 60;
                if(second < 10){
                    contentTime = minute + "\'" + " " + "0" + second + "\"";
                }else{
                    contentTime = minute + "\'" + " " + second + "\"";
                }
            }
            textLast.setText(contentTime);
        } else {
            imageLast.setVisibility(View.GONE);
            textLast.setVisibility(View.GONE);
        }

        // 主播  暂无主播字段
        View linearAnchor = findViewById(R.id.linear_anchor);
        linearAnchor.setVisibility(View.GONE);

        RoundImageView roundImageHead = (RoundImageView) findViewById(R.id.round_image_head);// 主播头像
        roundImageHead.setVisibility(View.GONE);

        TextView textAnchorName = (TextView) findViewById(R.id.text_anchor_name);// 主播名字
        textAnchorName.setVisibility(View.GONE);

        // 标签
        View linearLabel = findViewById(R.id.linear_label);
        linearLabel.setVisibility(View.GONE);

        TextView textLabel = (TextView) findViewById(R.id.text_label);// 标签
        textLabel.setVisibility(View.GONE);

        // 内容介绍
        View linearContent = findViewById(R.id.linear_content);
        TextView textDescn = (TextView) findViewById(R.id.text_content);// 介绍内容
        String contentDescn = GlobalConfig.playerObject.getContentDescn();
        if(contentDescn == null || contentDescn.equals("")) {
            linearContent.setVisibility(View.GONE);
        } else {
            linearContent.setVisibility(View.VISIBLE);
            textDescn.setText(contentDescn);
        }
    }

    // 初始化点击事件
    private void initEvent() {
        findViewById(R.id.head_left_btn).setOnClickListener(this);// 返回
        findViewById(R.id.linear_concern).setOnClickListener(this);// 关注主播
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.head_left_btn:// 返回
                finish();
                break;
            case R.id.linear_concern:// 关注主播

                break;
        }
    }
}
