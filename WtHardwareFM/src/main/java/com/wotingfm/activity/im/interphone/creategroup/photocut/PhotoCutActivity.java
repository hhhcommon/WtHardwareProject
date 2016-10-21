package com.wotingfm.activity.im.interphone.creategroup.photocut;

import android.annotation.TargetApi;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.TextView;

import com.wotingfm.R;
import com.wotingfm.activity.common.baseactivity.BaseActivity;
import com.wotingfm.common.constant.StringConstant;
import com.wotingfm.widget.photocut.ClipImageLayout;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;

/**
 * 图片剪裁页面
 */
public class PhotoCutActivity extends BaseActivity {
    private Bitmap bitmap;
    private ClipImageLayout mClipImageLayout;
    private TextView textSave;
    private int type;

    @TargetApi(Build.VERSION_CODES.KITKAT)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_photo_cut);
        initView();
    }

    /**
     * 处理上一个页面传递过来的数据
     */
    private void handleIntent() {
        Intent intent = getIntent();
        if (intent == null) {
            return ;
        }
        String imageUrl = intent.getStringExtra(StringConstant.START_PHOTO_ZOOM_URI);
        type = intent.getIntExtra(StringConstant.START_PHOTO_ZOOM_TYPE, -1);
        if (imageUrl == null || imageUrl.equals("")) {
            return ;
        }
        mClipImageLayout.setImage(Uri.parse(imageUrl));
        textSave.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                bitmap = mClipImageLayout.clip();
                ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.JPEG, 10, byteArrayOutputStream);
                try {
                    if (type == 1) {
                        long a = System.currentTimeMillis();
                        String s = String.valueOf(a);
                        FileOutputStream out = new FileOutputStream(new File(Environment.getExternalStorageDirectory() + "/woting/" + s + ".png"));
                        out.write(byteArrayOutputStream.toByteArray());
                        out.flush();
                        out.close();
                        Intent intent = new Intent();
                        intent.putExtra(StringConstant.PHOTO_CUT_RETURN_IMAGE_PATH, Environment.getExternalStorageDirectory() + "/woting/" + s + ".png");
                        setResult(1, intent);
                        finish();
                    } else {
                        FileOutputStream out = new FileOutputStream(new File(Environment.getExternalStorageDirectory() + "/woting/portaitUser.png"));
                        out.write(byteArrayOutputStream.toByteArray());
                        out.flush();
                        out.close();
                        setResult(1);
                        finish();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    /**
     * 初始化界面
     */
    private void initView() {
        mClipImageLayout = (ClipImageLayout) findViewById(R.id.id_clipImageLayout);
        textSave = (TextView) findViewById(R.id.text_save);
        handleIntent();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (bitmap != null && !bitmap.isRecycled()) {
            bitmap.recycle();
            bitmap = null;
        }
        if (mClipImageLayout != null) {
            mClipImageLayout.closeResource();
            mClipImageLayout = null;
        }
        textSave = null;
        setContentView(R.layout.activity_null);
    }
}
