package com.hhp227.application.ui.scrollable;

import android.content.Context;
import android.content.res.Resources;

public class DipUtils {

    private DipUtils() {}

    static int dipToPx(Context context, int dip) {
        final Resources r = context.getResources();
        final float scale = r.getDisplayMetrics().density;
        return (int) (dip * scale + .5F);
    }
}
