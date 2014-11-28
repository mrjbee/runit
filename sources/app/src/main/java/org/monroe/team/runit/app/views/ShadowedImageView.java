package org.monroe.team.runit.app.views;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BlurMaskFilter;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.util.AttributeSet;
import android.widget.ImageView;

public class ShadowedImageView extends ImageView{

    private static Paint DEBUG = new Paint();

    public ShadowedImageView(Context context) {
        super(context);
        init();
    }

    private void init() {
        DEBUG.setColor(Color.RED);
        DEBUG.setStyle(Paint.Style.FILL);
    }

    public ShadowedImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public ShadowedImageView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    @Override
    protected void onDraw(Canvas canvas) {

        Bitmap originImageBitmap = Bitmap.createBitmap(getWidth(), getHeight(), Bitmap.Config.ARGB_8888);
        super.onDraw(new Canvas(originImageBitmap));

        Bitmap shadowImageBitmap = Bitmap.createBitmap(getWidth(), getHeight(), Bitmap.Config.ARGB_8888);
        Canvas shadowImageCanvas = new Canvas(shadowImageBitmap);
        shadowImageBitmap.eraseColor(Color.BLACK);

        final Paint shadowPaint = new Paint();
        shadowPaint.setAntiAlias(true);
        shadowPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.DST_IN));
        shadowImageCanvas.drawBitmap(originImageBitmap, 0, 0, shadowPaint);

        Bitmap blurShadowImageBitmap = Bitmap.createBitmap(getWidth(), getHeight(), Bitmap.Config.ARGB_8888);
        Canvas blurShadowImageCanvas = new Canvas(blurShadowImageBitmap);

        shadowPaint.reset();
        shadowPaint.setAntiAlias(true);
        shadowPaint.setColor(Color.argb(150,0,0,0));
        shadowPaint.setMaskFilter(new BlurMaskFilter(4f, BlurMaskFilter.Blur.NORMAL));
        blurShadowImageCanvas.scale(0.9f,0.9f,blurShadowImageBitmap.getWidth()/2,blurShadowImageBitmap.getHeight()/2);
        blurShadowImageCanvas.drawBitmap(shadowImageBitmap.extractAlpha(), 1, 6+6 * 0.9f, shadowPaint);

        canvas.drawBitmap(blurShadowImageBitmap, 0, 0, shadowPaint);
        canvas.drawBitmap(originImageBitmap, 0, 0, null);
    }

    /*
    public Bitmap addShadow(final Bitmap bm, int color, int size, float dx, float dy) {
        final Bitmap mask = Bitmap.createBitmap(bm.getWidth(), bm.getHeight(), Bitmap.Config.ARGB_8888);

        /*
        final Matrix scaleToFit = new Matrix();
        final RectF src = new RectF(0, 0, bm.getWidth(), bm.getHeight());
        final RectF dst = new RectF(0, 0, dstWidth - dx, dstHeight - dy);
        scaleToFit.setRectToRect(src, dst, Matrix.ScaleToFit.CENTER);

        final Matrix dropShadow = new Matrix(scaleToFit);
        dropShadow.postTranslate(dx, dy);
        */
        /*

        final Canvas maskCanvas = new Canvas(mask);
        final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        maskCanvas.drawBitmap(bm, scaleToFit, paint);
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_OUT));
        maskCanvas.drawBitmap(bm, dropShadow, paint);

        final BlurMaskFilter filter = new BlurMaskFilter(size, BlurMaskFilter.Blur.NORMAL);
        paint.reset();
        paint.setAntiAlias(true);
        paint.setColor(color);
        paint.setMaskFilter(filter);
        paint.setFilterBitmap(true);

        final Bitmap ret = Bitmap.createBitmap(dstWidth, dstHeight, Bitmap.Config.ARGB_8888);
        final Canvas retCanvas = new Canvas(ret);
        retCanvas.drawBitmap(mask, 0,  0, paint);
        retCanvas.drawBitmap(bm, scaleToFit, null);
        mask.recycle();
        return ret;
    }
    */
}
