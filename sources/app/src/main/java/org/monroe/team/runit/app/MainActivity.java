package org.monroe.team.runit.app;

import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;

import org.monroe.team.android.box.app.ActivitySupport;
import static org.monroe.team.android.box.app.ui.animation.apperrance.AppearanceControllerBuilder.*;

import org.monroe.team.android.box.app.ui.SlideTouchGesture;
import org.monroe.team.android.box.app.ui.animation.apperrance.AppearanceController;
import org.monroe.team.android.box.app.ui.animation.apperrance.SceneDirector;
import org.monroe.team.android.box.utils.DisplayUtils;
import org.monroe.team.runit.app.android.RunitApp;

public class MainActivity extends ActivitySupport<RunitApp>{


    private AppearanceController ac_shadowLayer;
    private AppearanceController ac_mainContentLayer;
    private float mScreenWidth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        crunch_requestNoAnimation();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ac_shadowLayer = animateAppearance(view(R.id.layer_shadow),alpha(1f,0f))
                .showAnimation(duration_constant(300), interpreter_decelerate(0.6f))
                .hideAnimation(duration_constant(400), interpreter_accelerate(0.3f))
            .build();

        mScreenWidth = DisplayUtils.screenWidth(getResources());
        ac_mainContentLayer = animateAppearance(view(R.id.layer_main_content),xSlide(0f, mScreenWidth))
                .showAnimation(duration_constant(300), interpreter_decelerate(0.5f))
                .hideAnimation(duration_constant(200), interpreter_accelerate(0.3f))
                .build();

        if (isFirstRun()) {
            ac_shadowLayer.hideWithoutAnimation();
            ac_mainContentLayer.hideWithoutAnimation();
        }else {
            ac_shadowLayer.showWithoutAnimation();
            ac_mainContentLayer.showWithoutAnimation();
        }

        view(R.id.layer_shadow_left).setOnTouchListener(new SlideTouchGesture(mScreenWidth, SlideTouchGesture.Axis.X_RIGHT) {
            @Override
            protected void onProgress(float x, float y, float slideValue, float fraction) {
                view(R.id.layer_shadow).setAlpha(1 - fraction);
                view(R.id.layer_main_content).setTranslationX(mScreenWidth * fraction);
            }

            @Override
            protected void onApply(float x, float y, float slideValue, float fraction) {
                MainActivity.this.onBackPressed();
            }

            @Override
            protected void onCancel(float x, float y, float slideValue, float fraction) {
                ac_shadowLayer.show();
                ac_mainContentLayer.show();
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (isFirstRun()){
            ac_shadowLayer.show();
            ac_mainContentLayer.show();
        }
    }

    @Override
    public void onBackPressed() {
        SceneDirector.scenario()
                    .hide(ac_shadowLayer, ac_mainContentLayer)
                .then().action(new Runnable() {
            @Override
            public void run() {
                MainActivity.super.onBackPressed();
            }
        }).play();
    }
}
