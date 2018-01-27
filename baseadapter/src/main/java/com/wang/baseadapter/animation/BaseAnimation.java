package com.wang.baseadapter.animation;

import android.animation.AnimatorSet;
import android.view.View;

/**
 * base animation
 */
public interface BaseAnimation {

    AnimatorSet getAnimators(View view);

}
