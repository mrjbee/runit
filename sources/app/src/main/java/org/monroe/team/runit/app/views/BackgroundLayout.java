package org.monroe.team.runit.app.views;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Point;
import android.os.Build;
import android.util.AttributeSet;
import android.widget.RelativeLayout;

import org.monroe.team.android.box.utils.DisplayUtils;

public abstract class BackgroundLayout extends RelativeLayout{

    public BackgroundLayout(Context context) {
        super(context);
    }

    public BackgroundLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public BackgroundLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public BackgroundLayout(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        Bitmap mBlurredBitmap = getBackgroundBitmap();
        if (mBlurredBitmap != null){
            Point position = getBackgroundDrawPosition();
            canvas.drawBitmap(mBlurredBitmap, -position.x, -position.y, null);
        }
        super.onDraw(canvas);
    }

    protected abstract Point getBackgroundDrawPosition();
    protected abstract Bitmap getBackgroundBitmap();
}
