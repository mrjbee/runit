package org.monroe.team.runit.app.views;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Point;
import android.os.Build;
import android.util.AttributeSet;

public class ViewPagerPageRelativeLayout extends AbstractChangeSensitiveRelativeLayout{

    public ViewPagerPageRelativeLayout(Context context) {
        super(context);
    }

    private float mSlideVisibilityFraction = -1;

    public ViewPagerPageRelativeLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public ViewPagerPageRelativeLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public ViewPagerPageRelativeLayout(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    public float getSlideVisibilityFraction() {
        return mSlideVisibilityFraction;
    }

    public void setSlideVisibilityFraction(float mSlideVisibilityFraction) {
        this.mSlideVisibilityFraction = mSlideVisibilityFraction;
        notifyViewChildren(this);
    }

    public void updatePosition(Point position){
        position.offset((int) (getWidth()*mSlideVisibilityFraction), 0);
    }
}
