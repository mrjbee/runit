package org.monroe.team.android.box.ui;

public abstract class PushToActionAdapter implements PushTouchDelegate.PushListener{

    private final float pushToActionValue;

    protected PushToActionAdapter(float pushToActionValue) {
        this.pushToActionValue = pushToActionValue;
    }

    @Override
    final public void onOverPushStart(float x, float y) {
        beforePush(x, y);
    }

    @Override
    final public void onOverPush(float x, float y, float overPushValue) {
        float pushCoefficient = calculatePushCoefficient(overPushValue);
        pushInProgress(pushCoefficient,x,y);
    }

    private float calculatePushCoefficient(float overPushValue) {
        if (overPushValue > pushToActionValue){
            overPushValue = pushToActionValue;
        }
        if (overPushValue < 0){
            overPushValue = 0;
        }

        return overPushValue/pushToActionValue;
    }

    @Override
    final public void onOverPushStop(float x, float y, float overPushValue) {
        float pushCoefficient = calculatePushCoefficient(overPushValue);
        if (pushCoefficient == 1f){
            applyPushAction(x,y);
        } else {
            cancelPushAction(pushCoefficient, x, y);
        }
    }

    protected void beforePush(float x, float y){}
    protected abstract void pushInProgress(float pushCoefficient, float x, float y);
    protected abstract void applyPushAction(float x, float y);
    protected abstract void cancelPushAction(float pushCoefficient, float x, float y);

}
