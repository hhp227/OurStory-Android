package com.hhp227.application.ui.scrollable;

import android.animation.ObjectAnimator;

public interface CloseUpAnimatorConfigurator {

    /**
     * Note that {@link android.animation.ObjectAnimator#setDuration(long)} would erase current value set by {@link CloseUpIdleAnimationTime} if any present
     * @param animator current {@link android.animation.ObjectAnimator} object to animate close-up animation of a {@link ScrollableLayout}
     */
    void configure(ObjectAnimator animator);
}
