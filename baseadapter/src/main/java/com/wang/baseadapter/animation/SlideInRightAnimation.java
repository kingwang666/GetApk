package com.wang.baseadapter.animation;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.view.View;


/**
 * slide right animation
 */
public class SlideInRightAnimation implements BaseAnimation {

    @Override
    public AnimatorSet getAnimators(View view) {
        AnimatorSet set = new AnimatorSet();
        set.play(ObjectAnimator.ofFloat(view, "translationX", view.getMeasuredWidth(), 0));
        return set;
    }
}
