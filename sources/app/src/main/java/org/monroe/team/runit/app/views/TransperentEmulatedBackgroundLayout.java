package org.monroe.team.runit.app.views;

import android.content.Context;
import android.graphics.Point;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewParent;

import org.monroe.team.android.box.utils.DisplayUtils;

public abstract class TransperentEmulatedBackgroundLayout extends BackgroundLayout {
    public TransperentEmulatedBackgroundLayout(Context context) {
        super(context);
    }

    public TransperentEmulatedBackgroundLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public TransperentEmulatedBackgroundLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public TransperentEmulatedBackgroundLayout(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    @Override
    final protected Point getBackgroundDrawPosition() {
        Point answer = new Point(0,0);
        updateViewPosition(answer, this);
        return answer;
    }

    private static void updateViewPosition(Point point, View view) {
        point.offset((int)view.getX(), (int)view.getY());
        updateViewParentPosition(point, view.getParent());
    }

    private static void updateViewParentPosition(Point point, ViewParent view) {

        if (view == null) return;
        if (view instanceof StaticBackgroundLayout) return;

        if (view instanceof View){
            updateViewPosition(point, (View) view);
        }else {
            updateViewParentPosition(point, view.getParent());
        }
    }
}
