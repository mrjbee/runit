package org.monroe.team.runit.app.views;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.RectF;
import android.os.Build;
import android.util.AttributeSet;
import android.view.View;

import org.monroe.team.android.box.utils.DisplayUtils;

public class PushActionView extends View {


    public PushActionView(Context context) {
        super(context);
        init();
    }

    public PushActionView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public PushActionView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public PushActionView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init();
    }

    private float x = 200;
    private float y = 200;
    private float fraction = 0f;
    private String textPush = "";
    private String textAction = "";

    private float textPadding =0;
    private float circeSize = 0;

    private void init() {
          textPadding = DisplayUtils.dpToPx(10,getResources());
          circeSize = DisplayUtils.dpToPx(50, getResources());
    }

    @Override
    protected void onDraw(Canvas canvas) {
        Paint paint = new Paint();
        paint.setAntiAlias(true);
        int color = Color.rgb(245,245,245);
        String text = "";
        if (fraction != 1){
            text = textPush;
            color = Color.argb(200,245,245,245);
        }else {
            text = textAction;
            paint.setShadowLayer(3,0,2,Color.argb(50,0,0,0));
        }
        paint.setColor(color);
        paint.setStyle(Paint.Style.FILL);

        setLayerType(View.LAYER_TYPE_SOFTWARE, paint);

        Paint textPaint = new Paint();
        textPaint.setAntiAlias(true);
        textPaint.setColor(Color.rgb(50,50,50));
        textPaint.setStyle(Paint.Style.FILL);
        textPaint.setTextSize(30);


        int[] loc = new int[2];
        getLocationOnScreen(loc);
        canvas.translate(x - loc[0], y - loc[1]);
        canvas.scale(fraction, fraction);
        canvas.rotate(180 - 180*fraction);

        canvas.drawCircle(0, 0, circeSize, paint);

        Rect textBounds = new Rect();
        textPaint.getTextBounds(text,0, text.length(), textBounds);

        canvas.drawRoundRect(
                new RectF(
                - textBounds.width() / 2 - textPadding,
                -textBounds.height() - textPadding *2 - circeSize * 1.2f,
                textBounds.width() / 2 + textPadding,
                -circeSize * 1.2f),
                5, 5,
                paint
        );
        canvas.drawText(text,- textBounds.width() / 2, - circeSize * 1.2f - textPadding ,textPaint);

    }

    public void startPush(float x, float y, String textPush, String textAction){
        this.x = x;
        this.y = y;
        this.textPush = textPush;
        this.textAction = textAction;
        fraction = 0;
        //TODO more here
        setVisibility(VISIBLE);
    }

    public void pushing(float x, float y, float fraction) {
        this.x = x;
        this.y = y;
        this.fraction =fraction;
        invalidate();
    }


    public void stopPush(){
        setVisibility(INVISIBLE);
    }


}
