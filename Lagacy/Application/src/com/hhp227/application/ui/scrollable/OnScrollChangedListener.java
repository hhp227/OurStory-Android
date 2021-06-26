package com.hhp227.application.ui.scrollable;

public interface OnScrollChangedListener {

    /**
     * This method will be invoked when scroll state
     * of {@link ru.noties.scrollable.ScrollableLayout} has changed.
     * @see ru.noties.scrollable.ScrollableLayout#setOnScrollChangedListener(OnScrollChangedListener)
     * @param y current scroll y
     * @param oldY previous scroll y
     * @param maxY maximum scroll y (helpful for calculating scroll ratio for e.g. for alpha to be applied)
     */
    void onScrollChanged(int y, int oldY, int maxY);
}
