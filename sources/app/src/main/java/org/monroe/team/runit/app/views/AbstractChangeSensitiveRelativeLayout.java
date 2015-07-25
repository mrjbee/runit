package org.monroe.team.runit.app.views;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

public class AbstractChangeSensitiveRelativeLayout extends RelativeLayout{

    public AbstractChangeSensitiveRelativeLayout(Context context) {
        super(context);
    }

    public AbstractChangeSensitiveRelativeLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public AbstractChangeSensitiveRelativeLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public AbstractChangeSensitiveRelativeLayout(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    protected void notifyViewChildren(ViewGroup view) {
        for (int i=0; i< view.getChildCount();i++){
            View child = view.getChildAt(i);
            if (child instanceof PositionSensitiveChild){
                ((PositionSensitiveChild) child).onPositionChanged();
            }
            if (child instanceof ViewGroup){
                notifyViewChildren((ViewGroup) child);
            }
        }
    }

    public static interface PositionSensitiveChild {
        public void onPositionChanged();
    }
}
