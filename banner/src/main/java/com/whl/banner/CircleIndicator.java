package com.whl.banner;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

/**
 * 圆形指示器
 */
public class CircleIndicator extends View implements IIndicator {
    private int mNormalRadius;
    private int mSelectedRadius;
    private int maxRadius;
    private int mIndicatorSpace;//指示器之间的距离
    private int mItemCount;
    private int mCurrentPosition;
    private Paint mPaint;

    public CircleIndicator(Context context) {
        this(context, null);
    }

    public CircleIndicator(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CircleIndicator(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mNormalRadius = DensityUtil.dpToPx(4);
        mSelectedRadius = DensityUtil.dpToPx(4);
        mIndicatorSpace = DensityUtil.dpToPx(10);
        mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        if (mItemCount <= 1) {
            return;
        }

        maxRadius = Math.max(mSelectedRadius, mNormalRadius);
        //间距 *（总数-1）+ itemCount * 直径
        int width = (mItemCount - 1) * mIndicatorSpace + maxRadius * 2 * mItemCount;
        setMeasuredDimension(width, maxRadius * 2);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (mItemCount <= 1) {
            return;
        }
        float left = 0;
        for (int i = 0; i < mItemCount; ++i) {
            mPaint.setColor(mCurrentPosition == i ? Color.parseColor("#5CB85C") : Color.parseColor("#BDBDBD"));
            int radius = mCurrentPosition == i ? mSelectedRadius : mNormalRadius;
            canvas.drawCircle(left + radius, maxRadius, radius, mPaint);
            left += mIndicatorSpace + radius * 2;
        }
    }

    @Override
    public void setItemCount(int itemCount) {
        mItemCount = itemCount;
    }

    @Override
    public void setCurrentPosition(int currentPosition) {
        mCurrentPosition = currentPosition;
        invalidate();
    }
}
