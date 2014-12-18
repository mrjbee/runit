package org.monroe.team.android.box.ui;

import android.view.MotionEvent;

public class PushTouchDelegate {

    private final float PUSH_VECTOR_THRESHOLD = 200;

    private final PushDelegateOwner owner;
    private float startY = 0;
    private boolean overPushAllowed = false;
    private boolean overPushStarted = false;
    private PushListener pushListener;

    public PushTouchDelegate(PushDelegateOwner owner) {
        this.owner = owner;
    }

    PushListener getPushListener() {
        return pushListener;
    }

    void setPushListener(PushListener pushListener) {
        this.pushListener = pushListener;
    }

    boolean pushProcessing(MotionEvent event) {
        float y = event.getY();
        float overPush = y - startY;

        switch (event.getAction()) {
            case MotionEvent.ACTION_MOVE: {
                if (overPushAllowed) {

                    if (!overPushStarted && PUSH_VECTOR_THRESHOLD < overPush) {
                        overPushStarted = true;
                        pushListener.onOverPushStart(event.getX(), event.getY());
                    }

                    if (overPushStarted){
                        pushListener.onOverPush(event.getX(), event.getY(), overPush - PUSH_VECTOR_THRESHOLD);
                    }
                    if (overPushStarted){
                        return true;
                    }
                }
            }
            break;
            case MotionEvent.ACTION_DOWN: {
                startY = y;
                overPushStarted = false;
                overPushAllowed = owner.isScrollTopReached();
                break;
            }
            case MotionEvent.ACTION_UP: {
                if (overPushStarted) {
                    pushListener.onOverPushStop(event.getX(), event.getY(), overPush - PUSH_VECTOR_THRESHOLD);
                }
                overPushAllowed = false;
                overPushStarted = false;
                break;
            }

        }
        return false;
    }

    public static interface PushListener{
        void onOverPushStart(float x, float y);
        void onOverPush(float x, float y, float overPushValue);
        void onOverPushStop(float x, float y, float overPushValue);
    }

    static interface PushDelegateOwner {
        boolean isScrollTopReached();
    }
}
