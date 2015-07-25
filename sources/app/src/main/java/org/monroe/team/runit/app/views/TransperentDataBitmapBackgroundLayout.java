package org.monroe.team.runit.app.views;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.Build;
import android.util.AttributeSet;

import org.monroe.team.android.box.data.Data;

public class TransperentDataBitmapBackgroundLayout extends TransperentEmulatedBackgroundLayout implements AbstractChangeSensitiveRelativeLayout.PositionSensitiveChild {

    private Data<Bitmap> mBlurredBackgroundProvider;
    private Bitmap mBlurredBitmap;
    private Data.DataChangeObserver<Bitmap> mBitmapDataChangeObserver;

    public TransperentDataBitmapBackgroundLayout(Context context) {
        super(context);
    }

    public TransperentDataBitmapBackgroundLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public TransperentDataBitmapBackgroundLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public TransperentDataBitmapBackgroundLayout(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    @Override
    protected Bitmap getBackgroundBitmap() {
        return mBlurredBitmap;
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);
        //TODO: check if (changed)
        requestBackgroundImage();
    }

    private void requestBackgroundImage() {
        if (mBlurredBackgroundProvider == null) return;
        mBlurredBackgroundProvider.fetch(true, new Data.FetchObserver<Bitmap>() {
            @Override
            public void onFetch(Bitmap bitmap) {
                mBlurredBitmap = bitmap;
                invalidate();
            }

            @Override
            public void onError(Data.FetchError fetchError) {
                throw new IllegalStateException();
            }
        });
    }

    public void setBlurredBackground(Data<Bitmap> mBlurredBackgroundProvider) {
        if (this.mBlurredBackgroundProvider != null || mBlurredBackgroundProvider == null) throw new IllegalStateException();
        this.mBlurredBackgroundProvider = mBlurredBackgroundProvider;
        mBitmapDataChangeObserver = new Data.DataChangeObserver<Bitmap>() {
            @Override
            public void onDataInvalid() {
                requestBackgroundImage();
                mBlurredBitmap = null;
                invalidate();
            }

            @Override
            public void onData(Bitmap bitmap) {
            }
        };
        if (mBlurredBackgroundProvider == null) return;
        this.mBlurredBackgroundProvider.addDataChangeObserver(mBitmapDataChangeObserver);
        requestBackgroundImage();
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (mBlurredBackgroundProvider == null) return;
        this.mBlurredBackgroundProvider.removeDataChangeObserver(mBitmapDataChangeObserver);
    }

    @Override
    public void onPositionChanged() {
        invalidate();
    }
}
