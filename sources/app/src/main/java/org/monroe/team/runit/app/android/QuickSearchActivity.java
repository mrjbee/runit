package org.monroe.team.runit.app.android;

import android.animation.Animator;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextWatcher;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import org.monroe.team.android.box.GenericListViewAdapter;
import org.monroe.team.android.box.manager.BackgroundTaskManager;
import org.monroe.team.android.box.support.ActivitySupport;
import org.monroe.team.android.box.ui.AppearanceControllerOld;
import org.monroe.team.android.box.ui.animation.apperrance.AppearanceController;
import static org.monroe.team.android.box.ui.animation.apperrance.AppearanceControllerBuilder.*;

import org.monroe.team.android.box.utils.DisplayUtils;
import org.monroe.team.runit.app.R;
import org.monroe.team.runit.app.uc.entity.ApplicationData;

import java.util.List;


public class QuickSearchActivity extends ActivitySupport<RunitApp> {

    private AppearanceController searchHeaderAppearanceController;
    private AppearanceController searchContentAppearanceController;
    private ArrayAdapter<RunitApp.AppSearchResult> adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        application().requestRefreshApps();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quick_search);
        /*view(R.id.qs_root).addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
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
        });*/

        adapter = new GenericListViewAdapter<>(this,R.layout.item_search_application_qs, new GenericListViewAdapter.GenericViewHolderFactory<RunitApp.AppSearchResult>() {
            @Override
            public GenericListViewAdapter.GenericViewHolder<RunitApp.AppSearchResult> construct() {
                return new GenericListViewAdapter.GenericViewHolder<RunitApp.AppSearchResult>() {

                    View space;
                    TextView textView;
                    TextView textCategoryView;
                    ImageView imageView;

                    RunitApp.AppSearchResult foundApplicationItem;

                    BackgroundTaskManager.BackgroundTask<?> drawableLoadingTask;
                    BackgroundTaskManager.BackgroundTask<?> categoryLoadingTask;

                    @Override
                    public void discoverUI() {
                        space = _view(R.id.item_space,View.class);
                        textView = _view(R.id.search_item_text,TextView.class);
                        textCategoryView = _view(R.id.search_item_sub_text,TextView.class);
                        imageView = _view(R.id.search_item_image, ImageView.class);
                    }

                    @Override
                    public void update(final RunitApp.AppSearchResult appSearchResult, int position) {
                        space.setVisibility(position != 0 ? View.GONE : View.VISIBLE);
                        textCategoryView.setText("");
                        foundApplicationItem = appSearchResult;
                        final ApplicationData applicationData = foundApplicationItem.applicationData;
                        SpannableString spannableString = new SpannableString(applicationData.name);
                        if (foundApplicationItem.selectionStartIndex != null){
                            spannableString.setSpan(new ForegroundColorSpan(getResources().getColor(R.color.blue_themed)),
                                    foundApplicationItem.selectionStartIndex,
                                    foundApplicationItem.selectionEndIndex,
                                    Spanned.SPAN_COMPOSING);
                            spannableString.setSpan(new StyleSpan(Typeface.BOLD),
                                    foundApplicationItem.selectionStartIndex,
                                    foundApplicationItem.selectionEndIndex,
                                    Spanned.SPAN_COMPOSING);
                        }

                        textView.setText(spannableString);
                        imageView.setVisibility(View.INVISIBLE);

                        drawableLoadingTask =  application().loadApplicationIcon(applicationData, new RunitApp.OnLoadApplicationIconCallback() {
                            @Override
                            public void load(ApplicationData applicationData, Drawable drawable) {
                                if (appSearchResult.applicationData == applicationData) {
                                    imageView.setImageDrawable(drawable);
                                    imageView.setVisibility(View.VISIBLE);
                                }
                            }
                        });

                        categoryLoadingTask =  application().loadApplicationCategory(applicationData, new RunitApp.OnLoadCategoryCallback() {
                            @Override
                            public void load(ApplicationData applicationData, RunitApp.Category category) {
                                if (appSearchResult.applicationData == applicationData) {
                                    textCategoryView.setText(category.name);
                                }
                            }
                        });
                    }

                    @Override
                    public void cleanup() {

                        if (drawableLoadingTask != null){
                            drawableLoadingTask.cancel();
                        }

                        if (categoryLoadingTask != null){
                            categoryLoadingTask.cancel();
                        }
                    }
                };
            }
        });
        view(R.id.qs_search_result_list,ListView.class).setAdapter(adapter);
        view(R.id.qs_search_result_list,ListView.class).setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                application().launchApplication(adapter.getItem(position).applicationData);
            }
        });

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


            searchContentAppearanceController = animateAppearance(view(R.id.qs_content_panel),
                     xSlide(0f,DisplayUtils.screenWidth(getResources())))
                    .showAnimation(duration_constant(400), interpreter_accelerate(0.5f))
                    .build();


            searchHeaderAppearanceController = animateAppearance(view(R.id.qs_search_panel),
                    ySlide(-DisplayUtils.dpToPx(10, getResources()), -DisplayUtils.dpToPx(90, getResources())))
                    .showAnimation(duration_constant(500), interpreter_overshot())
                    .build();

            searchHeaderAppearanceController.hideWithoutAnimation();
            searchContentAppearanceController.hideWithoutAnimation();
            application().fetchMostRecentApplication(new RunitApp.OnApplicationFetchedCallback() {
                @Override
                public void fetched(List<ApplicationData> applicationDataList) {
                    adapter.clear();
                    for (ApplicationData data : applicationDataList) {
                        adapter.add(new RunitApp.AppSearchResult(data,0,0));
                    }
                    adapter.notifyDataSetChanged();
                }
            });
        }

        view_list(R.id.qs_search_result_list).setOnScrollListener(new AbsListView.OnScrollListener() {

            private int lastVisibleFirstItem = -1;

            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {
            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {

                if (totalItemCount == 0) return;

                if (firstVisibleItem == 0 || lastVisibleFirstItem == -1) {
                    searchHeaderAppearanceController.show();
                    lastVisibleFirstItem = firstVisibleItem;
                } else if (firstVisibleItem - lastVisibleFirstItem > 1) {
                    searchHeaderAppearanceController.hide();
                    lastVisibleFirstItem = firstVisibleItem;
                } else if (firstVisibleItem - lastVisibleFirstItem < -1) {
                    searchHeaderAppearanceController.show();
                    lastVisibleFirstItem = firstVisibleItem;
                }
            }
        });

    }


    @Override
    protected void onResume() {
        super.onResume();
        if (searchHeaderAppearanceController != null) {
            searchHeaderAppearanceController.showAndCustomize(new AppearanceController.AnimatorCustomization() {
                @Override
                public void customize(Animator animator) {
                    animator.setStartDelay(100);
                    animator.addListener(new AppearanceControllerOld.AnimatorListenerAdapter(){
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            focusOnEdit();
                        }
                    });
                }
            });
            searchContentAppearanceController.show();
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
                adapter.clear();
                adapter.addAll(searchResultList);
                adapter.notifyDataSetChanged();
            }
        });
    }


    public static PendingIntent open(Context context) {
        Intent intent = new Intent(context, QuickSearchActivity.class)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                .addFlags(Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED)
                .addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
        return PendingIntent.getActivity(context, 0, intent,
                PendingIntent.FLAG_UPDATE_CURRENT);
    }

    private void checkAndFinish() {
        if (view(R.id.qs_search_edit,EditText.class).getText().toString().isEmpty()){
            finishActivity();
        }
    }

    @Override
    public void onBackPressed() {
        finishActivity();
    }

    private void finishActivity() {
        view_list(R.id.qs_search_result_list).setOnScrollListener(null);
        searchContentAppearanceController.hideAndCustomize(new AppearanceController.AnimatorCustomization(){
            @Override
            public void customize(Animator animator) {
                animator.addListener(new AppearanceControllerOld.AnimatorListenerAdapter(){
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        finish();
                    }
                });
            }
        });
        searchHeaderAppearanceController.hide();
    }

}
