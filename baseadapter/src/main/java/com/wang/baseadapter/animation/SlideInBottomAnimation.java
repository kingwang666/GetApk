package com.wang.baseadapter.animation;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.view.View;


/**
 * slide bottom animation
 */
public class SlideInBottomAnimation implements BaseAnimation {

    @Override
    public AnimatorSet getAnimators(View view) {
        AnimatorSet set = new AnimatorSet();
        set.play(ObjectAnimator.ofFloat(view, "translationY", view.getMeasuredHeight(), 0));
        return set;
    }
}
