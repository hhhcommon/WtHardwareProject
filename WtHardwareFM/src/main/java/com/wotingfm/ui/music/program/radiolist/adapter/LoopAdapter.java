package com.wotingfm.ui.music.program.radiolist.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;
import com.wotingfm.R;
import com.wotingfm.common.config.GlobalConfig;
import com.wotingfm.ui.music.program.radiolist.model.Image;
import com.wotingfm.util.AssembleImageUrlUtils;
import com.wotingfm.util.BitmapUtils;
import com.wotingfm.widget.rollviewpager.RollPagerView;
import com.wotingfm.widget.rollviewpager.adapter.LoopPagerAdapter;

import java.util.List;

/**
 * author：辛龙 (xinLong)
 * 2017/3/8 11:41
 * 邮箱：645700751@qq.com
 */
public class LoopAdapter extends LoopPagerAdapter {

    private final Context context;
    private final List<Image> list;
    private final Bitmap bmp;

    public LoopAdapter(RollPagerView viewPager,Context context, List<Image> imageList) {
        super(viewPager);
        this.context = context;
        this.list = imageList;
        bmp = BitmapUtils.readBitMap(context, R.mipmap.img_person_background);// 默认图片

    }

    @Override
    public View getView(ViewGroup container, int position) {

        ImageView view = new ImageView(container.getContext());
        view.setScaleType(ImageView.ScaleType.FIT_XY);
        view.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

        String contentImg= list.get(position).getLoopImg();

        if (contentImg != null && !contentImg.trim().equals("")) {
            if (!contentImg.startsWith("http")) {
                contentImg = GlobalConfig.imageurl + contentImg;
            }
            contentImg = AssembleImageUrlUtils.assembleImageUrl150(contentImg);
            Picasso.with(context).load(contentImg.replace("\\/", "/")).into(view);
        } else {
            view.setImageBitmap(bmp);
        }
        return view;
    }

    @Override
    public int getRealCount() {
        return list.size();
    }
}
