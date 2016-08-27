package com.wotingfm.common.base;

import android.annotation.TargetApi;
import android.app.Activity;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import com.wotingfm.R;

/**
 * 基类 包含公共标题栏以及一些公共方法供子类调用或重写
 * 继承此类需注意 xml中必须include  base_activity_title.xml
 * Created by Administrator on 2016/8/27 0027.
 */
public abstract class BaseActivity extends Activity {
    private TextView leftBack;          // 左上角  返回
    private TextView rightMore;         // 右上角  更多
    private TextView textTitle;         // 标题
    private ImageView leftImage;        // 左上角  图标
    private ImageView rightImage;       // 右上角  图标

    @TargetApi(Build.VERSION_CODES.KITKAT)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(setViewId());
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);        // 透明状态栏
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);    // 透明导航栏

        initView();
    }

    /*
     * 初始化
     */
    private void initView(){
        leftBack = findView(R.id.left_back);
        leftBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        rightMore = findView(R.id.right_more);
        textTitle = findView(R.id.text_title);
        leftImage = findView(R.id.left_image);
        rightImage = findView(R.id.right_image);

        init();
    }

    // 子类重写  用于加载布局文件
    protected abstract int setViewId();

    // 子类重写  用于界面和数据的初始化
    protected abstract void init();

    /**
     * 查找视图
     */
    protected <T extends View> T findView(int id){
        return (T)findViewById(id);
    }

    /**
     * 设置标题
     */
    protected void setTitle(String title){
        textTitle.setText(title);
    }

    /**
     * 设置标题
     */
    protected void setTitleInt(int title){
        textTitle.setText(title);
    }

    /**
     * 设置左上角返回隐藏
     */
    protected void setLeftTextGone(){
        leftBack.setVisibility(View.GONE);
    }

    /**
     * 设置左上角文字
     * @param leftText
     */
    protected void setLeftText(String leftText){
        leftBack.setText(leftText);
    }

    /**
     * 设置左上角图标返回显示
     */
    protected void setLeftImageVisible(){
        leftImage.setVisibility(View.VISIBLE);
    }

    /**
     * 设置左上角图标并显示
     * @param imageId
     */
    protected void setLeftImage(int imageId, View.OnClickListener onClickListener){
        leftImage.setImageDrawable(getResources().getDrawable(imageId));
        leftImage.setVisibility(View.VISIBLE);
        if(onClickListener == null){
            leftImage.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    finish();
                }
            });
        } else {
            leftImage.setOnClickListener(onClickListener);
        }
    }

    /**
     * 设置右上角更多显示
     */
    protected void setRightTextVisible(View.OnClickListener onClickListener){
        rightMore.setVisibility(View.VISIBLE);
        rightMore.setOnClickListener(onClickListener);
    }

    /**
     * 设置右上角文字并显示
     * @param rightText
     */
    protected void setRightText(String rightText, View.OnClickListener onClickListener){
        rightMore.setText(rightText);
        rightMore.setVisibility(View.VISIBLE);
        rightMore.setOnClickListener(onClickListener);
    }

    /**
     * 设置右上角图标显示
     */
    protected void setRightImageVisible(View.OnClickListener onClickListener){
        rightImage.setVisibility(View.VISIBLE);
        rightImage.setOnClickListener(onClickListener);
    }

    /**
     * 设置右上角图标并显示
     * @param imageId
     */
    protected void setRightImage(int imageId, View.OnClickListener onClickListener){
        rightImage.setImageDrawable(getResources().getDrawable(imageId));
        rightImage.setVisibility(View.VISIBLE);
        rightImage.setOnClickListener(onClickListener);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        leftBack = null;
        rightMore = null;
        textTitle = null;
        leftImage = null;
        rightImage = null;
        setContentView(R.layout.activity_null_view);
    }
}
