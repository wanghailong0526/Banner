package com.whl.banner;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Rect;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.view.ViewCompat;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleEventObserver;
import androidx.lifecycle.LifecycleOwner;


import java.lang.ref.WeakReference;
import java.util.ArrayList;

/**
 * @author : wanghailong
 * @date :
 * @description: 当前显示的 page 在最前面; 左边一张; 右边一张; 共三张
 * 大于三张 复用之前的 view
 * 排列方式   (getItemCount-1)    (0)   (1)
 */
public class Banner extends FrameLayout {
    public static final String CURRENT_DATA_INDEX = "current_data_index";
    private int dx;
    private int mx;
    private int deltax;
    private boolean isDragged = false;
    private ArrayList<ViewHolder> mHolders;
    private int currentPageIdx = 0;//第一张 view 的下标为0
    private int currentPageWidth = 0;//当前在最前面显示的 View 的宽度
    private int backViewWidth = 0;//在后面显示的 View 的宽度
    private int totalScroll = 0;//当前显示的 View 总的滑动距离

    private int switchPageDis = 0;//当前显示的 View 滑动到这个距离就切换 View
    private int leftPageIdx = 0;
    private int rightPageIdx = 0;
    private int currentDataIdx = 0;
    private int leftDataIdx;
    private int rightDataIdx;
    private int flag = -1;
    private int leftPageScrollDis = 0;
    private int rightPageScrollDis = 0;

    private Adapter mAdapter;
    private IIndicator mIndicatorView;
    private boolean mIsAutoLoop = true;//是否自动轮播
    private LoopHandler mLoopHandler;
    private long mLoopTime = 3000;//轮播间隔时间 3 秒
    private OnPageChnagedListener mOnPageChangedListener;
    private onPageClickListener mOnpageClickListener;
    private PageAnimator mPageAnimator;


    public Banner(Context context) {
        this(context, null);
    }

    public Banner(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public Banner(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        mLoopHandler = new LoopHandler(Looper.getMainLooper(), this);
        mHolders = new ArrayList();
        mPageAnimator = new com.whl.banner.PageAnimator();
    }

    public void notifiedDataSetChanged() {
        stopLoop();
        if (mAdapter == null) return;
        if (getChildCount() > 0) {
            removeAllViews();
        }

        if (mHolders.size() == 0) {
            for (int i = 0; i < 3; ++i) {
                final ViewHolder holder = mAdapter.onCreateViewHolder(Banner.this);
                if (holder.itemView.getParent() != null) {
                    throw new IllegalStateException("ViewHolder views must not be attached when" + " created. Ensure that you are not passing 'true' to the attachToRoot" + " parameter of LayoutInflater.inflate(..., boolean attachToRoot)");
                }
                mHolders.add(i, holder);
                LayoutParams layoutParams = ((LayoutParams) holder.itemView.getLayoutParams());
                addView(holder.itemView, layoutParams);
            }
        }
        mHolders.get(2).itemView.setZ(0);
        mHolders.get(1).itemView.setZ(0);
        mHolders.get(0).itemView.setZ(1);

        mAdapter.onBindViewHolder(mHolders.get(2), getRightDataIdx());
        mAdapter.onBindViewHolder(mHolders.get(1), getLeftDataIdx());
        mAdapter.onBindViewHolder(mHolders.get(0), currentDataIdx);

        /**
         * 添加 IndicatorView
         */
        if (mIndicatorView != null) {
            mIndicatorView.setItemCount(mAdapter.getItemCount());
            mIndicatorView.setCurrentPosition(currentDataIdx);
        }

        startLoop();
    }

    @SuppressLint("ObjectAnimatorBinding")
    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        MotionEvent ev = event;
        switch (ev.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                stopLoop();
                isDragged = false;
                dx = (int) ev.getRawX();
                break;
            case MotionEvent.ACTION_MOVE:
                mx = (int) ev.getRawX();
                deltax = mx - dx;
                if (deltax < 0) {//向左滑动
                    flag = -1;
                } else {
                    flag = 1;
                }
                if (Math.abs(deltax) > 3) {//移动
                    if (getParent() != null) {
                        getParent().requestDisallowInterceptTouchEvent(true);
                    }
                    isDragged = true;
                    dx = mx;
                    ViewCompat.offsetLeftAndRight(mHolders.get(currentPageIdx).itemView, deltax);
                    totalScroll += deltax;
                }
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_OUTSIDE:
                changePage(totalScroll);
                totalScroll = 0;
                //如果是点击事件 并且 点击位置在 itemview 内
                if (!isDragged && pointInView(ev.getRawX(), ev.getRawY(), mHolders.get(currentPageIdx).itemView) && mOnpageClickListener != null) {
                    mOnpageClickListener.onPageClick(currentDataIdx);
                }
                startLoop();
                if (getParent() != null) {
                    getParent().requestDisallowInterceptTouchEvent(false);
                }
                break;
        }
        //如果是滑动  return true 认为事件被消费了,后继的 move , up 事件都由此 view 处理
        return true;
    }

    private void changePage(int totalScroll) {
        if (Math.abs(totalScroll) >= switchPageDis) {//切换 page
            //达到滑动边界，开始切换 view
            //1. 如果当前显示的 View 向左滑动， 当前显示 View 的左边一张向右滑动，当前显示 View 的右边向左滑动
            int currentScrollPos = backViewWidth - currentPageWidth / 2 + ((isDragged ? totalScroll : 0) * -flag);//当前 View 的滑动距离
            //当前 View 滑动
            ViewCompat.offsetLeftAndRight(mHolders.get(currentPageIdx).itemView, currentScrollPos * flag);
            if (mPageAnimator != null) {
                mPageAnimator.pageOutAnim(mHolders.get(currentPageIdx).itemView);
            }

            //向左滑动 计算滑动距离
            if (flag == -1) {
                leftPageScrollDis = backViewWidth;
                rightPageScrollDis = -currentPageWidth / 2;
            }
            //向右滑动计算滑动距离
            if (flag == 1) {
                leftPageScrollDis = currentPageWidth / 2;
                rightPageScrollDis = -backViewWidth;
            }

            if (mAdapter.getItemCount() > 3) {
                //如果数据源>3 向左滑动计算当前显示的 page 的数据源位置
                if (flag == -1) {
                    ++currentDataIdx;
                    if (currentDataIdx > mAdapter.getItemCount() - 1) {
                        currentDataIdx = 0;
                    }
                }
                //如果数据源>3 向右滑动计算当前显示的 page 的数据源位置
                if (flag == 1) {
                    --currentDataIdx;
                    if (currentDataIdx < 0) {
                        currentDataIdx = mAdapter.getItemCount() - 1;
                    }
                }
            }

            leftPageIdx = currentPageIdx - 1;
            if (leftPageIdx < 0) {
                leftPageIdx = mHolders.size() - 1;
            }
            //左边一张滑动
            ViewCompat.offsetLeftAndRight(mHolders.get(leftPageIdx).itemView, leftPageScrollDis);

            if (flag == -1 && mAdapter.getItemCount() > 3) {//使用 viewholder 绑数据
                //currentDataIdx + 1 如果当前是向左滑动，最左边的 page 向右滑动，此时最左 page 显示的是最右边的 page 的下一个数据源的数据
                mAdapter.onBindViewHolder(mHolders.get(leftPageIdx), getLeftDataIdx());
            }

            rightPageIdx = currentPageIdx + 1;
            if (rightPageIdx >= mHolders.size()) {
                rightPageIdx = 0;
            }
            //右边一张滑动
            ViewCompat.offsetLeftAndRight(mHolders.get(rightPageIdx).itemView, rightPageScrollDis);

            if (flag == 1 && mAdapter.getItemCount() > 3) {//使用 viewholder 绑数据
                //currentDataIdx - 1 如果是向右滑动，当前显示 page 的右边显示的是getItemCount-2的数据源的数据
                mAdapter.onBindViewHolder(mHolders.get(rightPageIdx), getRightDataIdx());
            }

            mHolders.get(currentPageIdx).itemView.setZ(0);
            if (flag == -1) {//向左滑动
                mHolders.get(rightPageIdx).itemView.setZ(1);
                mHolders.get(leftPageIdx).itemView.setZ(0);
            }
            if (flag == 1) {//向右滑动
                mHolders.get(rightPageIdx).itemView.setZ(0);
                mHolders.get(leftPageIdx).itemView.setZ(1);
            }

            if ((currentPageIdx -= flag) < 0) {
                currentPageIdx = mHolders.size() - 1;
            }
            currentPageIdx %= 3;
            if (mPageAnimator != null) {
                mPageAnimator.pageInAnim(mHolders.get(currentPageIdx).itemView);
            }
            if (mIndicatorView != null) {
                mIndicatorView.setCurrentPosition(currentDataIdx);
            }
        } else {//复位 page
            ViewCompat.offsetLeftAndRight(mHolders.get(currentPageIdx).itemView, -totalScroll);
        }
    }

    /**
     * 计算坐标是否在 view 内
     *
     * @param x    x 坐标
     * @param y    y 坐标
     * @param view 是否在这个 view 内
     * @return true (x,y)在 view 内
     */
    public boolean pointInView(float x, float y, View view) {
        Rect rect = new Rect();
        view.getGlobalVisibleRect(rect);
        rect.bottom = (int) (rect.bottom + ((view.getHeight() * view.getScaleY()) / 2));
        rect.right = (int) (rect.right + ((view.getWidth() * view.getScaleX()) / 2));
        boolean isInViewRect = rect.contains((int) x, (int) y);
        return isInViewRect;
    }

    private int getRightDataIdx() {
        int rightDataIdx = currentDataIdx - 1;
        if (rightDataIdx < 0) {
            rightDataIdx = mAdapter.getItemCount() - 1;
        }
        return rightDataIdx;
    }

    private int getLeftDataIdx() {
        int leftDataIdx = currentDataIdx + 1;
        leftDataIdx %= mAdapter.getItemCount();
        return leftDataIdx;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        //没有内容时的大小
        if (mAdapter == null || getChildCount() <= 0) {
            int width = chooseSize(widthMeasureSpec, ViewCompat.getMinimumWidth(this) + getPaddingLeft() + getPaddingRight(), getPaddingLeft() + getPaddingRight());
            int height = chooseSize(heightMeasureSpec, ViewCompat.getMinimumHeight(this) + getPaddingTop() + getPaddingBottom(), getPaddingLeft() + getPaddingRight());
            setMeasuredDimension(width, height);
            return;
        }
        int childCount = getChildCount();
        for (int i = childCount - 1; i >= 0; --i) {
            View child = getChildAt(i);
            measureChild(child, widthMeasureSpec, heightMeasureSpec);
        }
        int childWidth = getChildAt(0).getMeasuredWidth();
        int childHeight = getChildAt(0).getMeasuredHeight();

        final int width = chooseSize(widthMeasureSpec, childWidth * 2 + getPaddingLeft() + getPaddingRight(), ViewCompat.getMinimumWidth(this) + getPaddingLeft() + getPaddingRight());
        final int height = chooseSize(heightMeasureSpec, childHeight + getPaddingTop() + getPaddingBottom(), ViewCompat.getMinimumHeight(this) + getPaddingTop() + getPaddingBottom());
        setMeasuredDimension(width, height);
    }

    private static int chooseSize(int spec, int desired, int min) {
        final int mode = MeasureSpec.getMode(spec);
        final int size = MeasureSpec.getSize(spec);
        switch (mode) {
            case MeasureSpec.EXACTLY:
                return size;
            case MeasureSpec.AT_MOST:
                return Math.min(size, Math.max(desired, min));
            case MeasureSpec.UNSPECIFIED:
            default:
                return Math.max(desired, min);
        }
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        if (mAdapter == null || getChildCount() <= 0) return;

        View child0 = getChildAt(0);
        int childWidth = child0.getMeasuredWidth();
        currentPageWidth = childWidth;
        backViewWidth = childWidth;
        switchPageDis = currentPageWidth / 3;

        if (mPageAnimator != null && mHolders.size() > 0) {
            mPageAnimator.pageInAnim(mHolders.get(0).itemView);
        }
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);
        if (changed) {
            int childCount = getChildCount();
            if (childCount <= 0) return;

            View child0 = getChildAt(0);
            int childWidth = child0.getMeasuredWidth();
            int childHeight = child0.getMeasuredHeight();

            int measuredWidth = getMeasuredWidth();
            int measuredHeight = getMeasuredHeight();

            int left = measuredWidth / 2 - childWidth / 2;
            int top = measuredHeight / 2 - childHeight / 2 + getPaddingTop();
            int right = left + childWidth;
            int bottom = childHeight + getPaddingTop();

            child0.layout(left, top, right, bottom);

            left = measuredWidth / 2;
            right = left + childWidth;
            View child1 = getChildAt(1);
            child1.layout(left, top, right, bottom);

            left = measuredWidth / 2 - 2 * childWidth / 2;
            right = left + childWidth;
            View child2 = getChildAt(2);
            child2.layout(left, top, right, bottom);
        }
    }

    private static class LoopHandler extends Handler {
        private WeakReference<Banner> reference;

        public LoopHandler(@NonNull Looper looper, Banner banner) {
            super(looper);
            this.reference = new WeakReference<>(banner);
        }

        @Override
        public void handleMessage(@NonNull Message msg) {
            Banner banner = reference.get();
            if (banner != null && banner.canLoop()) {
                banner.isDragged = false;
                banner.changePage(banner.switchPageDis);
                sendEmptyMessageDelayed(1, banner.mLoopTime);
            }
        }
    }

    private boolean canLoop() {
        return mIsAutoLoop && mAdapter != null && mAdapter.getItemCount() >= 3;
    }

    /**
     * 开始轮播
     */
    public Banner startLoop() {
        if (canLoop()) {
            stopLoop();
            mLoopHandler.sendEmptyMessageDelayed(1, mLoopTime);
        }
        return this;
    }

    /**
     * 停止轮播
     */
    public Banner stopLoop() {
        mLoopHandler.removeCallbacksAndMessages(null);
        return this;
    }

    @Override
    public void onWindowFocusChanged(boolean hasWindowFocus) {
        super.onWindowFocusChanged(hasWindowFocus);
        if (hasWindowFocus) {
            startLoop();
        } else {
            stopLoop();
        }
    }

    public Banner addLifecycleObserver(LifecycleOwner owner) {
        if (owner != null) {
            owner.getLifecycle().addObserver(new LifecycleEventObserver() {
                @Override
                public void onStateChanged(@NonNull LifecycleOwner lifecycleOwner, @NonNull Lifecycle.Event event) {
                    switch (event) {
                        case ON_RESUME:
                            startLoop();
                            break;
                        case ON_DESTROY:
                            stopLoop();
                            break;
                    }
                }
            });
        }
        return this;
    }

    public Banner setIndicatorView(IIndicator indicatorView) {
        if (indicatorView != null) {
            mIndicatorView = indicatorView;
        }
        return this;
    }

    public Banner setIsAutoLoop(boolean isAutoLoop) {
        mIsAutoLoop = isAutoLoop;
        return this;
    }

    public Banner setLoopTime(long loopTime) {
        mLoopTime = loopTime;
        return this;
    }

    private interface OnPageChnagedListener {
        void onPageChanged(int leftPageIndex, int currentPagIndex, int rightPageIndex);
    }

    public Banner setOnPageChangedListener(OnPageChnagedListener onPageChangedListener) {
        mOnPageChangedListener = onPageChangedListener;
        return this;
    }

    private interface onPageClickListener {
        void onPageClick(int currentIndex);
    }

    public Banner setOnPageClickListener(onPageClickListener onPageClickListener) {
        mOnpageClickListener = onPageClickListener;
        return this;
    }

    public interface PageAnimator {
        public void pageInAnim(View view);

        public void pageOutAnim(View view);

        public void pageTranslation(View view, int dis);
    }

    public Banner setPageAnimator(PageAnimator pageAnimator) {
        mPageAnimator = pageAnimator;
        return this;
    }

    public Banner setAdapter(Adapter adapter) {
        mAdapter = adapter;
        return this;
    }

    public abstract static class Adapter<VH extends ViewHolder> {
        public Adapter() {
        }

        public abstract VH onCreateViewHolder(ViewGroup parent);

        public abstract void onBindViewHolder(VH viewHolder, int position);

        public abstract int getItemCount();
    }

    public abstract static class ViewHolder {
        public final View itemView;

        public ViewHolder(View itemView) {
            this.itemView = itemView;
        }
    }

    @Nullable
    @Override
    protected Parcelable onSaveInstanceState() {
        Parcelable state = super.onSaveInstanceState();
        BannerSavedState ss = new BannerSavedState(state);
        ss.current_data_idx = currentDataIdx;
        return ss;
    }

    @Override
    protected void onRestoreInstanceState(Parcelable state) {
        if (!(state instanceof BannerSavedState)) {
            super.onRestoreInstanceState(state);
            return;
        }
        BannerSavedState ss = (BannerSavedState) state;
        super.onRestoreInstanceState(ss.getSuperState());
        currentDataIdx = ss.current_data_idx;
    }

    private static class BannerSavedState extends BaseSavedState {
        int current_data_idx;

        protected BannerSavedState(Parcel in) {
            super(in);
            this.current_data_idx = in.readInt();
        }

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeInt(this.current_data_idx);
        }

        public void readFromParcel(Parcel source) {
            this.current_data_idx = source.readInt();
        }

        public BannerSavedState(Parcelable state) {
            super(state);
        }

        public static final Creator<BannerSavedState> CREATOR = new Creator<BannerSavedState>() {
            @Override
            public BannerSavedState createFromParcel(Parcel source) {
                return new BannerSavedState(source);
            }

            @Override
            public BannerSavedState[] newArray(int size) {
                return new BannerSavedState[size];
            }
        };
    }
}

