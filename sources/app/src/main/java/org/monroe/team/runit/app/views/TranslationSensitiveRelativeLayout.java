package org.monroe.team.runit.app.views;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

public class TranslationSensitiveRelativeLayout extends RelativeLayout{

    public TranslationSensitiveRelativeLayout(Context context) {
        super(context);
    }

    public TranslationSensitiveRelativeLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public TranslationSensitiveRelativeLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public TranslationSensitiveRelativeLayout(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    @Override
    public void setTranslationX(float translationX) {
        super.setTranslationX(translationX);
        notifyViewChildren(this);
    }


    @Override
    public void setTranslationY(float translationY) {
        super.setTranslationY(translationY);
        notifyViewChildren(this);
    }

    private void notifyViewChildren(ViewGroup view) {
        for (int i=0; i< view.getChildCount();i++){
            View child = view.getChildAt(i);
            if (child instanceof ParentTranslationAware){
                ((ParentTranslationAware) child).onParentTranslationChanged();
            }
            if (child instanceof ViewGroup){
                notifyViewChildren((ViewGroup) child);
            }
        }
    }

    public static interface ParentTranslationAware {
        public void onParentTranslationChanged();
    }
}
