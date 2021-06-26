package com.hhp227.application.ui.scrollable;

import android.animation.ObjectAnimator;
import android.view.animation.Interpolator;

public class InterpolatorCloseUpAnimatorConfigurator implements CloseUpAnimatorConfigurator {

    private final Interpolator mInterpolator;

    public InterpolatorCloseUpAnimatorConfigurator(Interpolator interpolator) {
        this.mInterpolator = interpolator;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void configure(ObjectAnimator animator) {
        animator.setInterpolator(mInterpolator);
    }
}
