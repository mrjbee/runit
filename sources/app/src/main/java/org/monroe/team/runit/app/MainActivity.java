package org.monroe.team.runit.app;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.ImageView;

import org.monroe.team.android.box.app.ActivitySupport;
import static org.monroe.team.android.box.app.ui.animation.apperrance.AppearanceControllerBuilder.*;

import org.monroe.team.android.box.app.ui.SlideTouchGesture;
import org.monroe.team.android.box.app.ui.animation.apperrance.AppearanceController;
import org.monroe.team.android.box.app.ui.animation.apperrance.SceneDirector;
import org.monroe.team.android.box.utils.DisplayUtils;
import org.monroe.team.runit.app.android.RunitApp;
import org.monroe.team.runit.app.fragment.FragmentHeader;
import org.monroe.team.runit.app.views.StaticBackgroundLayout;

public class MainActivity extends ActivitySupport<RunitApp>{


    private AppearanceController ac_shadowLayer;
    private AppearanceController ac_mainContentLayer;
    private float mScreenWidth;
    private StaticBackgroundLayout mPanelPageContent;
    private AppearanceController ac_fragHeader;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        crunch_requestNoAnimation();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mPanelPageContent = view(R.id.panel_page_content, StaticBackgroundLayout.class);
        application().function_updateBackgroundSize(0,0,0,0);
        mPanelPageContent.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                mPanelPageContent.getViewTreeObserver().removeGlobalOnLayoutListener(this);
                application().function_updateBackgroundSize(0, 0, mPanelPageContent.getWidth(), mPanelPageContent.getHeight());
            }
        });

        ac_shadowLayer = animateAppearance(view(R.id.layer_shadow),alpha(1f,0f))
                .showAnimation(duration_constant(300), interpreter_decelerate(0.6f))
                .hideAnimation(duration_constant(400), interpreter_accelerate(0.3f))
            .build();

        mScreenWidth = DisplayUtils.screenWidth(getResources());
        ac_mainContentLayer = animateAppearance(view(R.id.layer_main_content),xSlide(0f, mScreenWidth))
                .showAnimation(duration_constant(300), interpreter_decelerate(0.5f))
                .hideAnimation(duration_constant(200), interpreter_accelerate(0.3f))
                .build();


        ac_fragHeader = animateAppearance(view(R.id.frag_header),ySlide(0f, -DisplayUtils.dpToPx(100f, getResources())))
                .showAnimation(duration_constant(200), interpreter_decelerate(0.6f))
                .hideAnimation(duration_constant(400), interpreter_accelerate(0.3f))
                .hideAndGone()
                .build();
        mPanelPageContent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                visibility_Header(!visibility_isHeader());
            }
        });

        if (isFirstRun()){
            getFragmentManager().beginTransaction().add(R.id.frag_header, new FragmentHeader()).commit();
        }

        ac_fragHeader.hideWithoutAnimation();

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

    private void visibility_Header(boolean visible) {
        if (visible){
           ac_fragHeader.show();
        }else {
            ac_fragHeader.hide();
        }
    }

    private boolean visibility_isHeader() {
        return view(R.id.frag_header).getVisibility() != View.GONE;
    }

    @Override
    protected void onStart() {
        super.onStart();
        application().data_configuration.fetch(true, observe_data(new OnValue<RunitApp.Configuration>() {
            @Override
            public void action(final RunitApp.Configuration configuration) {
                mPanelPageContent.setBitmapBackground(configuration.background);
                if (isFirstRun()){
                    ac_shadowLayer.show();
                    ac_mainContentLayer.show();
                }
            }
        }));
        application().data_blurredBackground.fetch(true, observe_data(new OnValue<Bitmap>() {
            @Override
            public void action(Bitmap bitmap) {
                ac_fragHeader.show();
            }
        }));
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
