package org.monroe.team.runit.app.views;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ScrollView;

public class MyScrollView extends android.widget.ScrollView{

    public MyScrollView(Context context) {
        super(context);
    }

    public MyScrollView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public MyScrollView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public MyScrollView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    @Override
    protected void onScrollChanged(int l, int t, int oldl, int oldt) {
        super.onScrollChanged(l, t, oldl, oldt);
        notifyViewChildren(this);
    }

    protected void notifyViewChildren(ViewGroup view) {
        for (int i=0; i< view.getChildCount();i++){
            View child = view.getChildAt(i);
            if (child instanceof AbstractChangeSensitiveRelativeLayout.PositionSensitiveChild){
                ((AbstractChangeSensitiveRelativeLayout.PositionSensitiveChild) child).onPositionChanged();
            }
            if (child instanceof ViewGroup){
                notifyViewChildren((ViewGroup) child);
            }
        }
    }
}
