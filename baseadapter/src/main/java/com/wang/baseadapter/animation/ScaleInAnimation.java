package com.wang.baseadapter.animation;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.view.View;


/**
 * scale animation
 */
public class ScaleInAnimation implements BaseAnimation {

    private static final float DEFAULT_SCALE_FROM = .5f;
    private final float mFrom;

    public ScaleInAnimation() {
        this(DEFAULT_SCALE_FROM);
    }

    public ScaleInAnimation(float from) {
        mFrom = from;
    }

    @Override
    public AnimatorSet getAnimators(View view) {
        AnimatorSet set = new AnimatorSet();
        set.playTogether(
                ObjectAnimator.ofFloat(view, "scaleX", mFrom, 1f),
                ObjectAnimator.ofFloat(view, "scaleY", mFrom, 1f)
        );
        return set;
    }
}
