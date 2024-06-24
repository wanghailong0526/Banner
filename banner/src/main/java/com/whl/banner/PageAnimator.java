package com.whl.banner;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;

public class PageAnimator implements Banner.PageAnimator {
    AnimatorSet set;

    public PageAnimator() {
        set = new AnimatorSet();
    }

    @Override
    public void pageInAnim(View view) {
        //设置缩放中心为 view 的中心点
        view.setPivotX((float) view.getMeasuredWidth() / 2);
        view.setPivotY((float) view.getMeasuredHeight() / 2);

        ObjectAnimator scaleX = ObjectAnimator.ofFloat(view, View.SCALE_X, 1.0f, 1.3f);
        ObjectAnimator scaleY = ObjectAnimator.ofFloat(view, View.SCALE_Y, 1.0f, 1.3f);

        set.setInterpolator(new AccelerateDecelerateInterpolator());
        set.playTogether(scaleX, scaleY);
        set.setDuration(1000);
        set.start();
    }

    @Override
    public void pageOutAnim(View view) {
        view.setPivotX((float) view.getMeasuredWidth() / 2);
        view.setPivotY((float) view.getMeasuredHeight() / 2);

        ObjectAnimator scaleX = ObjectAnimator.ofFloat(view, View.SCALE_X, 1.3f, 1.0f);
        ObjectAnimator scaleY = ObjectAnimator.ofFloat(view, View.SCALE_Y, 1.3f, 1.0f);

        set.setInterpolator(new AccelerateDecelerateInterpolator());
        set.playTogether(scaleX, scaleY);
        set.setDuration(1000);
        set.start();
    }

    @Override
    public void pageTranslation(View view, int dis) {

    }
}
