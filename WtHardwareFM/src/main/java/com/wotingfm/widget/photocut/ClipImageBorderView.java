package com.wotingfm.widget.photocut;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;

public class ClipImageBorderView extends View {
    private int mHorizontalPadding;         // 水平方向与View的边距
    private int mVerticalPadding;           // 垂直方向与View的边距
    private int mWidth;                     // 绘制的矩形的宽度
    private int mBorderColor = Color.parseColor("#FFa684");     // 边框的颜色，默认为白色
    private int mBorderWidth = 1;           // 边框的宽度 单位dp
    private Paint mPaint;

    public ClipImageBorderView(Context context) {
        this(context, null);
    }

    public ClipImageBorderView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ClipImageBorderView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);

        mBorderWidth = (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, mBorderWidth, getResources().getDisplayMetrics());
        mPaint = new Paint();
        mPaint.setAntiAlias(true);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        mWidth = getWidth() - 2 * mHorizontalPadding;                               // 计算矩形区域的宽度
        mVerticalPadding = (getHeight() - mWidth) / 2;                              // 计算距离屏幕垂直边界 的边距
        mPaint.setColor(Color.parseColor("#aa000000"));
        mPaint.setStyle(Style.FILL);
        canvas.drawRect(0, 0, mHorizontalPadding, getHeight(), mPaint);             // 绘制左边1
        canvas.drawRect(getWidth() - mHorizontalPadding, 0, getWidth(),             // 绘制右边2
                getHeight(), mPaint);
        canvas.drawRect(mHorizontalPadding, 0, getWidth() - mHorizontalPadding,     // 绘制上边3
                mVerticalPadding, mPaint);
        canvas.drawRect(mHorizontalPadding, getHeight() - mVerticalPadding,         // 绘制下边4
                getWidth() - mHorizontalPadding, getHeight(), mPaint);
        mPaint.setColor(mBorderColor);                                              // 绘制外边框
        mPaint.setStrokeWidth(mBorderWidth);
        mPaint.setStyle(Style.STROKE);
        canvas.drawRect(mHorizontalPadding, mVerticalPadding, getWidth()
                - mHorizontalPadding, getHeight() - mVerticalPadding, mPaint);

    }

    public void setHorizontalPadding(int mHorizontalPadding) {
        this.mHorizontalPadding = mHorizontalPadding;
    }
}
