package com.wotingfm.activity.mine.qrcode;

import android.content.Intent;
import android.graphics.Bitmap;
import android.widget.ImageView;
import android.widget.TextView;

import com.wotingfm.R;
import com.wotingfm.activity.common.baseactivity.AppBaseActivity;
import com.wotingfm.activity.im.interphone.groupmanage.model.UserInfo;
import com.wotingfm.common.config.GlobalConfig;
import com.wotingfm.helper.CreatQRImageHelper;
import com.wotingfm.helper.ImageLoader;
import com.wotingfm.util.BitmapUtils;

/**
 * 展示二维码
 */
public class EWMShowActivity extends AppBaseActivity {
	private ImageView imageViewEwm;
	private ImageView imageHead;
	private TextView textName;
	private TextView textNews;
	private ImageLoader imageLoader;
	private String url;
	private Bitmap bitmap, bmp;

    @Override
    protected int setViewId() {
        return R.layout.activity_ewmshow;
    }

    @Override
    protected void init() {
        setTitle("二维码");
        imageLoader = new ImageLoader(context);
        Intent data = getIntent();
        if(data != null){
            String news = data.getStringExtra("news");
            String type = data.getStringExtra("type");
            int types;		// 1：个人   2：组
            if(type != null && !type.trim().equals("")){
                types = Integer.parseInt(type);
            }else{
                types = 1;
            }
            imageViewEwm = (ImageView) findViewById(R.id.imageView_ewm);
            imageHead = (ImageView) findViewById(R.id.image);
            textName = (TextView) findViewById(R.id.name);
            textNews = (TextView) findViewById(R.id.news);
            getData(types, news);
        }
    }

    private void getData(int type, String news) {
        UserInfo userInfo = null;
        String imageUrl = null;
        String name = null;
		if(type == 1){
            userInfo = (UserInfo) getIntent().getSerializableExtra("person");
            imageUrl = userInfo.getPortraitMini();
            name = userInfo.getUserName();
		} else if (type == 2){
            userInfo = (UserInfo)getIntent().getSerializableExtra("group");
            imageUrl = userInfo.getPortraitMini();
            name = userInfo.getGroupName();
		}
        if (name != null && !name.equals("")) {
            textName.setText(name);
        }
        if (news != null && !news.equals("")) {
            textNews.setText(news);
        }
        if (imageUrl == null || imageUrl.equals("") || imageUrl.equals("null")|| imageUrl.trim().equals("")) {
            imageHead.setImageResource(R.mipmap.wt_image_tx_qz);
        } else {
            if(imageUrl.startsWith("http:")){
                url=imageUrl;
            }else{
                url = GlobalConfig.imageurl + imageUrl;
            }
            imageLoader.DisplayImage(url.replace("\\/", "/"), imageHead, false, false, null, null);
        }
        bitmap = CreatQRImageHelper.getInstance().createQRImage(type, userInfo, 220, 220);
        if(bitmap != null){
            imageViewEwm.setImageBitmap(bitmap);
        } else {
            bmp = BitmapUtils.readBitMap(context, R.mipmap.ewm);
            imageViewEwm.setImageBitmap(bmp);
        }
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
        imageViewEwm = null;
        imageHead = null;
        textName = null;
        textNews = null;
		imageLoader = null;
		url = null;
		if(bitmap != null){
            bitmap.recycle();
            bitmap = null;
		}
		if(bmp != null){
            bmp.recycle();
            bmp = null;
		}
	}
}