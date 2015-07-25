package org.monroe.team.runit.app.views;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Canvas;
import android.os.Build;
import android.util.AttributeSet;
import android.widget.ImageView;
import android.widget.RelativeLayout;

public class BlurredBackgroundRelativeLayout extends RelativeLayout {

    public BlurredBackgroundRelativeLayout(Context context) {
        super(context);
    }

    public BlurredBackgroundRelativeLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public BlurredBackgroundRelativeLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public BlurredBackgroundRelativeLayout(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
    }
}
