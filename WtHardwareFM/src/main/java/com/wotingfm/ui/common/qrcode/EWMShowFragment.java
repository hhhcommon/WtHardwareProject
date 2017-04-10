package com.wotingfm.ui.common.qrcode;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;
import com.wotingfm.R;
import com.wotingfm.common.config.GlobalConfig;
import com.wotingfm.common.constant.StringConstant;
import com.wotingfm.common.helper.CreateQRImageHelper;
import com.wotingfm.ui.common.model.GroupInfo;
import com.wotingfm.ui.interphone.model.UserInviteMeInside;
import com.wotingfm.ui.mine.main.MineActivity;
import com.wotingfm.ui.music.main.PlayerActivity;
import com.wotingfm.ui.music.main.ProgramActivity;
import com.wotingfm.util.AssembleImageUrlUtils;
import com.wotingfm.util.BitmapUtils;

/**
 * 展示二维码
 * 作者：xinlong on 2016/4/28 21:18
 * 邮箱：645700751@qq.com
 */
public class EWMShowFragment extends Fragment implements OnClickListener {
    private ImageView imageEwm;
    private ImageView imageHead;
    private TextView textName;
    private TextView textNews;
    private TextView textTip;
    private Bitmap bmp;
    private FragmentActivity context;
    private View rootView;
    private String fromType;

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.head_left_btn:// 返回
                if (fromType == null || fromType.equals("")) return ;
                switch (fromType) {
                    case StringConstant.TAG_MINE:
                        MineActivity.close();
                        break;
                    case StringConstant.TAG_PROGRAM:
                        ProgramActivity.close();
                        break;
                    case StringConstant.TAG_PLAY:
                        PlayerActivity.close();
                        break;
                }
                break;
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (rootView == null) {
            rootView = inflater.inflate(R.layout.activity_ewmshow, container, false);
            context = getActivity();

            initView();
        }
        return rootView;
    }

    // 初始化视图
    private void initView() {
        rootView.findViewById(R.id.head_left_btn).setOnClickListener(this);// 返回

        ImageView imageBackground = (ImageView) rootView.findViewById(R.id.id_image_background);
        imageBackground.setImageBitmap(BitmapUtils.readBitMap(context, R.mipmap.wt_image_qrcode_background));

        textTip = (TextView) rootView.findViewById(R.id.id_text_tip);

        imageEwm = (ImageView) rootView.findViewById(R.id.imageView_ewm);
        imageHead = (ImageView) rootView.findViewById(R.id.image);
        textName = (TextView) rootView.findViewById(R.id.name);
        textNews = (TextView) rootView.findViewById(R.id.news);

        if (getArguments() != null) {
            fromType = getArguments().getString(StringConstant.FROM_TYPE);
            int type = getArguments().getInt("type", 1);// 0：单体节目分享  1：个人   2：组  3：专辑分享
            if (type == 0) {
                shapeContent();
            } else if (type == 3) {

            } else {
                String image = getArguments().getString("image");
                String news = getArguments().getString("news");
                String name = getArguments().getString("name");
                setData(type, image, news, name);
            }
        }
    }

    // 初始化数据
    private void setData(int type, String imageUrl, String news, String name) {
        /**
         * type == 1 的界面有两个
         * 1、TalkPersonNewsActivity
         * 2、MineActivity
         * 两个界面跳转过来携带的对象不一样
         * TalkPersonNewsActivity - > UserInviteMeInside  --> "person"
         * MineActivity -> UserInfo     --> "person"
         */
        if (type == 1) {
            UserInviteMeInside meInside = (UserInviteMeInside) getArguments().getSerializable("person");
            bmp = CreateQRImageHelper.getInstance().createQRImage(type, null, meInside, 220, 220);
        } else if (type == 2) {
            GroupInfo groupNews = (GroupInfo) getArguments().getSerializable("group");
            bmp = CreateQRImageHelper.getInstance().createQRImage(type, groupNews, null, 220, 220);

            textTip.setText("扫面上面的二维码图案，加入群组");
        }
        if (name != null && !name.equals("")) {
            textName.setText(name);
        }
        if (news != null && !news.equals("")) {
            textNews.setText(news);
        }
        if (imageUrl != null && !imageUrl.equals("null") && !imageUrl.trim().equals("")) {
            if (!imageUrl.startsWith("http:")) {
                imageUrl = AssembleImageUrlUtils.assembleImageUrl150(GlobalConfig.imageurl + imageUrl);
            } else {
                imageUrl = AssembleImageUrlUtils.assembleImageUrl150(imageUrl);
            }
            Picasso.with(context).load(imageUrl.replace("\\/", "/")).resize(100, 100).centerCrop().into(imageHead);
        } else {
            imageHead.setImageBitmap(BitmapUtils.readBitMap(context, R.mipmap.wt_image_tx_hy));
        }
        if (bmp == null) {
            bmp = BitmapUtils.readBitMap(context, R.mipmap.ewm);
        }
        imageEwm.setImageBitmap(bmp);
    }

    // 单体节目分享
    private void shapeContent() {
        if (GlobalConfig.playerObject.getContentImg() == null) {
            imageHead.setImageBitmap(BitmapUtils.readBitMap(context, R.mipmap.wt_image_playertx));
        } else {
            String contentImage = GlobalConfig.playerObject.getContentImg();
            if (!contentImage.startsWith("http")) {
                contentImage = GlobalConfig.imageurl + contentImage;
            }
            contentImage = AssembleImageUrlUtils.assembleImageUrl180(contentImage);
            Picasso.with(context).load(contentImage.replace("\\/", "/")).into(imageHead);
        }

        String contentTile = GlobalConfig.playerObject.getContentName();
        if (contentTile == null || contentTile.equals("")) {
            contentTile = "未知";
        }
        textName.setText(contentTile);

        String contentDescn = GlobalConfig.playerObject.getContentDescn();
        if (contentDescn == null || contentDescn.equals("")) {
            contentDescn = "暂无介绍";
        }
        textNews.setText(contentDescn);

        textTip.setText("扫面上面的二维码图案");
    }

    // 专辑分享
    private void shapeAlbum() {

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        imageEwm = null;
        imageHead = null;
        textName = null;
        textNews = null;
        if (bmp != null) {
            bmp.recycle();
            bmp = null;
        }
    }
}
