package org.monroe.team.runit.app.android;

import android.animation.Animator;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;

import org.monroe.team.android.box.support.ActivitySupport;
import org.monroe.team.android.box.ui.AppearanceControllerOld;
import org.monroe.team.android.box.ui.AppearanceControllers;
import org.monroe.team.android.box.ui.animation.apperrance.AppearanceController;
import static org.monroe.team.android.box.ui.animation.apperrance.AppearanceControllerBuilder.*;
import org.monroe.team.runit.app.R;
import org.monroe.team.runit.app.android.preneter.SearchResultListDelegate;

import java.util.List;


public class QuickSearchActivity extends ActivitySupport<RunitApp> {

    private SearchResultListDelegate searchListDelegate;
    private AppearanceController searchPanelAppearanceController;
    private AppearanceController searchResultPanelAppearanceController;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quick_search);
        view(R.id.qs_root).addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
            @Override
            public void onLayoutChange(View v, int left, int top, int right, int bottom,
                                       int oldLeft, int oldTop, int oldRight, int oldBottom) {
                if (oldBottom < bottom
                        && left==oldLeft
                        && right==oldRight
                        && top == oldTop){
                    checkAndFinish();
                }
            }
        });
        searchListDelegate = new SearchResultListDelegate(this,view(R.id.qs_search_result_list, ListView.class), R.layout.item_search_application_qs) {
            @Override
            protected void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                application().launchApplication(searchListDelegate.get(position).applicationData);
            }
        };

        view(R.id.qs_search_edit, EditText.class).addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                initiateAppsSearch();
            }
        });


        if (savedInstanceState == null) {

            float searchPanelAppearanceStartFrom = 0;
            if (getIntent() != null && getIntent().getSourceBounds() != null) {
                searchPanelAppearanceStartFrom = getIntent().getSourceBounds().top;
            } else {
                searchPanelAppearanceStartFrom = getResources().getDisplayMetrics().heightPixels * 0.8f;
            }


            searchResultPanelAppearanceController = animateAppearance(view(R.id.qs_search_result_panel),
                     alpha(1f,0f))
                    .showAnimation(duration_constant(600), interpreter_decelerate(0.2f))
                    .build();


            searchPanelAppearanceController = animateAppearance(view(R.id.qs_search_panel), ySlide(0, searchPanelAppearanceStartFrom))
                    .showAnimation(duration_constant(400), interpreter_accelerate(0.8f))
                    .build();

            searchPanelAppearanceController.hideWithoutAnimation();
            searchResultPanelAppearanceController.hideWithoutAnimation();
        }


    }


    @Override
    protected void onResume() {
        super.onResume();
        if (searchPanelAppearanceController != null) {
            searchPanelAppearanceController.showAndCustomize(new AppearanceController.AnimatorCustomization() {
                @Override
                public void customize(Animator animator) {
                    animator.addListener(new AppearanceControllerOld.AnimatorListenerAdapter(){
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            focusOnEdit();
                        }
                    });
                }
            });
            searchResultPanelAppearanceController.show();
        }
    }


    private void focusOnEdit() {
        EditText yourEditText = view(R.id.qs_search_edit, EditText.class);
        yourEditText.requestFocus();
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.showSoftInput(yourEditText, InputMethodManager.SHOW_IMPLICIT);
    }

    private void initiateAppsSearch() {
        final String searchQuery = view(R.id.qs_search_edit,EditText.class).getText().toString().trim();
        application().searchApplicationByName(searchQuery.trim(), new RunitApp.OnAppSearchCallback(){
            @Override
            public void found(String searchQuery, List<RunitApp.AppSearchResult> searchResultList) {
                searchListDelegate.add(true, searchResultList);
            }
        });
    }


    public static PendingIntent open(Context context) {
        Intent intent = new Intent(context, QuickSearchActivity.class)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                .addFlags(Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
        return PendingIntent.getActivity(context, 0, intent,
                PendingIntent.FLAG_UPDATE_CURRENT);
    }

    private void checkAndFinish() {
        if (view(R.id.qs_search_edit,EditText.class).getText().toString().isEmpty()){
            finish();
        }
    }

}
