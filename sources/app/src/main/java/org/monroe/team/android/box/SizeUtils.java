package org.monroe.team.android.box;

import android.content.res.Resources;
import android.util.TypedValue;

final public class SizeUtils {
    private SizeUtils() {}

    public static float dpToPx(float dp, Resources resources){
       return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, resources.getDisplayMetrics());
    }

}
