package org.monroe.team.runit.app.views;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.util.AttributeSet;

public class StaticBackgroundLayout extends BackgroundLayout{

    private Bitmap mBitmapBackground = null;
    private final Point  mBackgroundPosition = new Point(0, 0);

    public StaticBackgroundLayout(Context context) {
        super(context);
    }

    public StaticBackgroundLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public StaticBackgroundLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public StaticBackgroundLayout(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    public Bitmap getBitmapBackground() {
        return mBitmapBackground;
    }

    public void setBitmapBackground(Bitmap mBitmapBackground) {
        this.mBitmapBackground = mBitmapBackground;
        invalidate();
    }

    @Override
    protected Point getBackgroundDrawPosition() {
        return mBackgroundPosition;
    }

    @Override
    protected Bitmap getBackgroundBitmap() {
        return mBitmapBackground;
    }
}
