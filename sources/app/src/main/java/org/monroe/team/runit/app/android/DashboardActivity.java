package org.monroe.team.runit.app.android;

import android.animation.Animator;
import android.annotation.TargetApi;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.os.Build;
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
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import org.monroe.team.android.box.app.ui.PushToActionAdapter;
import org.monroe.team.android.box.app.ui.PushToListView;
import org.monroe.team.android.box.utils.DisplayUtils;
import org.monroe.team.corebox.services.BackgroundTaskManager;
import org.monroe.team.android.box.app.ActivitySupport;
import org.monroe.team.android.box.app.ui.AppearanceControllerOld;
import org.monroe.team.android.box.app.ui.animation.apperrance.AppearanceController;
import org.monroe.team.runit.app.ApplicationDrawerActivity;
import org.monroe.team.runit.app.R;
import org.monroe.team.runit.app.android.preneter.RefreshableListAdapter;
import org.monroe.team.runit.app.uc.entity.ApplicationData;
import org.monroe.team.runit.app.views.PushActionView;

import static org.monroe.team.android.box.app.ui.animation.apperrance.AppearanceControllerBuilder.*;

import java.util.List;


public class DashboardActivity extends ActivitySupport <RunitApp> {

    private ArrayAdapter<RunitApp.AppSearchResult> searchResultAdapter;

    private AppearanceController hotAppsPanelAppearanceController;
    private AppearanceController searchButtonAppearanceController;
    private AppearanceController searchPanelAppearanceController;
    private AppearanceController searchResultPanelAppearanceController;

    private RefreshableListAdapter mostUsedAppsRefreshableListAdapter;
    private RefreshableListAdapter recentAppsRefreshableListAdapter;
    private boolean requestKeyboardFlag;

    @Override
    protected void onCreate (final Bundle savedInstanceState) {
        application().requestRefreshApps();
        setContentView(R.layout.activity_dash);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            updateTrayColor();
        }
        setupSearchResultListAdapter();
        setupAnimations();
        setupApplicationPanels();
        Boolean searchActivated = null;
        if (savedInstanceState != null){
            searchActivated = savedInstanceState.getBoolean("search_activated");
        }

        if (getIntent() != null && getIntent().getBooleanExtra("go_search_request",false)){
            searchActivated = true;
            requestKeyboardFlag = true;
            getIntent().removeExtra("go_search_request");
        }


        if (searchActivated == null){
            //initial run
            hotAppsPanelAppearanceController.showWithoutAnimation();
            searchButtonAppearanceController.showWithoutAnimation();
            searchPanelAppearanceController.hideWithoutAnimation();
            searchResultPanelAppearanceController.hideWithoutAnimation();
        }else if (searchActivated){
            hotAppsPanelAppearanceController.hideWithoutAnimation();
            searchButtonAppearanceController.hideWithoutAnimation();
            searchPanelAppearanceController.showWithoutAnimation();
            searchResultPanelAppearanceController.showWithoutAnimation();
        } else {
            hotAppsPanelAppearanceController.showWithoutAnimation();
            searchButtonAppearanceController.showWithoutAnimation();
            searchPanelAppearanceController.hideWithoutAnimation();
            searchResultPanelAppearanceController.hideWithoutAnimation();
            searchResultAdapter.clear();
            searchResultAdapter.notifyDataSetChanged();
        }

        view(R.id.dashboard_search_button, Button.class).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                searchButtonAppearanceController.hideAndCustomize(new AppearanceController.AnimatorCustomization() {
                    @Override
                    public void customize(Animator animator) {
                        animator.setStartDelay(100);
                    }
                });
                hotAppsPanelAppearanceController.hide();
                searchPanelAppearanceController.showAndCustomize(new AppearanceController.AnimatorCustomization() {
                    @Override
                    public void customize(Animator animator) {
                        animator.addListener(new AppearanceControllerOld.AnimatorListenerAdapter() {
                            @Override
                            public void onAnimationEnd(Animator animation) {
                                focusOnEdit();
                            }
                        });
                    }
                });
                searchResultPanelAppearanceController.show();
            }
        });
        view(R.id.dashboard_search_result_list, ListView.class).setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                view(R.id.dashboard_search_edit, EditText.class).setText("");
                searchPanelAppearanceController.hideWithoutAnimation();
                hotAppsPanelAppearanceController.show();
                hotAppsPanelAppearanceController.show();
                searchButtonAppearanceController.showWithoutAnimation();
                searchResultPanelAppearanceController.hide();
                application().launchApplication(searchResultAdapter.getItem(position).applicationData);
                searchResultAdapter.clear();
                searchResultAdapter.notifyDataSetChanged();
            }
        });
        view(R.id.dashboard_search_result_list, PushToListView.class).setPushThreshold(150);
        view(R.id.dashboard_search_result_list, PushToListView.class).setPushListener(new PushToActionAdapter(350) {

            @Override
            protected void cancelPushAction(float pushCoefficient, float x, float y) {
                view(R.id.dashboard_push_action_view, PushActionView.class)
                        .stopPush();
            }

            @Override
            protected void applyPushAction(float x, float y) {
                view(R.id.dashboard_push_action_view, PushActionView.class)
                        .stopPush();
                InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
                inputMethodManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
                closeSearch();
            }


            @Override
            protected void beforePush(float x, float y) {
                view(R.id.dashboard_push_action_view, PushActionView.class)
                        .startPush(x,y,"Push it ...", "Close Search");
            }

            @Override
            protected void pushInProgress(float pushCoefficient, float x, float y) {
                view(R.id.dashboard_push_action_view, PushActionView.class)
                        .pushing(x,y,pushCoefficient);
                if (pushCoefficient > 0.5f){
                    InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
                    inputMethodManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
                }
            }
        });

        view(R.id.dashboard_search_edit, EditText.class).addTextChangedListener(new TextWatcher() {
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

        view(R.id.dashboard_app_categories_panel).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(DashboardActivity.this, ApplicationDrawerActivity.class);
                int[] location = new int[2];
                v.getLocationInWindow(location);
                intent.putExtra("button_bounds", new float[]{
                        location[0],
                        location[1],
                        v.getWidth(),
                        v.getHeight()
                });
                startActivity(intent);
            }
        });

        super.onCreate(savedInstanceState);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void updateTrayColor() {
        getWindow().setStatusBarColor(getResources().getColor(R.color.blue_themed_dark));
    }

    private void focusOnEdit() {
        EditText yourEditText = view(R.id.dashboard_search_edit, EditText.class);
        yourEditText.requestFocus();
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.showSoftInput(yourEditText, InputMethodManager.SHOW_IMPLICIT);
    }


    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean("search_activated",
                view(R.id.dashboard_search_button).getVisibility() != View.VISIBLE);
    }

    private void setupApplicationPanels() {
        mostUsedAppsRefreshableListAdapter = new RefreshableListAdapter(application(),
                view(R.id.dashboard_resent_used_container, ViewGroup.class),
                15,
                new ImageWithCaptionViewRenderer());
        recentAppsRefreshableListAdapter = new RefreshableListAdapter(application(),
                view(R.id.dashboard_most_used_container, ViewGroup.class),
                5, new ImageOnlyViewRenderer());
    }

    private void setupAnimations() {

        searchResultPanelAppearanceController = animateAppearance(
                view(R.id.dashboard_search_result_container),
                ySlide(0f, -getResources().getDisplayMetrics().heightPixels))
                .showAnimation(duration_constant(200))
                .hideAnimation(duration_constant(300),interpreter_overshot())
                .hideAndGone()
                .build();

        hotAppsPanelAppearanceController = animateAppearance(
                view(R.id.dashboard_hot_app_container),
                ySlide(0f, getResources().getDisplayMetrics().heightPixels))
                .showAnimation(duration_constant(200))
                .hideAnimation(duration_constant(300),interpreter_overshot())
                .hideAndGone()
                .build();

        searchButtonAppearanceController= animateAppearance(
                view(R.id.dashboard_search_button),
                xSlide(0f, 200f))
                .showAnimation(duration_constant(300), interpreter_overshot())
                .hideAnimation(duration_constant(300))
                .hideAndGone()
                .build();


        searchPanelAppearanceController = animateAppearance(
                view(R.id.dashboard_search_panel),
                xSlide(0f, DisplayUtils.dpToPx(300, getResources())))
                .hideAndGone()
                .showAnimation(duration_auto_fint(), interpreter_overshot())
                .hideAnimation(duration_auto_fint(), interpreter_overshot())
                .hideAndInvisible()
                .build();

    }

    @Override
    protected void onResume() {
        super.onResume();
        fetchMostRecentApplications();
        if (requestKeyboardFlag){
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        Thread.sleep(500);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                   runOnUiThread(new Runnable() {
                       @Override
                       public void run() {
                           focusOnEdit();
                       }
                   });
                }
            }).start();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    private void fetchMostRecentApplications() {
        application().fetchMostRecentApplication(new RunitApp.OnApplicationFetchedCallback() {
            @Override
            public void fetched(List<ApplicationData> applicationDataList) {
                if (applicationDataList.isEmpty()){
                    view(R.id.dashboard_most_used_no_data_text).setVisibility(View.VISIBLE);
                } else {
                    view(R.id.dashboard_most_used_no_data_text).setVisibility(View.GONE);
                }
                recentAppsRefreshableListAdapter.refreshList(applicationDataList);
                fetchMostUsedApplications();
            }
        });
    }

    private void fetchMostUsedApplications() {
        application().fetchMostUsedApplication(new RunitApp.OnApplicationFetchedCallback() {
            @Override
            public void fetched(List<ApplicationData> applicationDataList) {
                if (applicationDataList.isEmpty()){
                    view(R.id.dashboard_resent_used_no_data_text).setVisibility(View.VISIBLE);
                } else {
                    view(R.id.dashboard_resent_used_no_data_text).setVisibility(View.GONE);
                }
                mostUsedAppsRefreshableListAdapter.refreshList(applicationDataList);
            }
        });
    }
    private void setupSearchResultListAdapter() {
        searchResultAdapter = new ArrayAdapter<RunitApp.AppSearchResult>(getApplicationContext(), R.layout.item_search_application){
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                final SearchItemDetails holder;
                if (convertView == null){
                    LayoutInflater inflater = (LayoutInflater) getContext()
                            .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                    convertView = inflater.inflate(R.layout.item_search_application, null);
                    holder = new SearchItemDetails(
                            convertView.findViewById(R.id.search_first_item_stub),
                            (TextView) convertView.findViewById(R.id.search_item_text),
                            (TextView) convertView.findViewById(R.id.search_item_sub_text),
                            (ImageView) convertView.findViewById(R.id.search_item_image));
                    convertView.setTag(holder);
                } else {
                    holder = (SearchItemDetails) convertView.getTag();
                }
                if (position == 0){
                    holder.firstItemMarginView.setVisibility(View.VISIBLE);
                } else {
                    holder.firstItemMarginView.setVisibility(View.GONE);
                }
                if (holder.drawableLoadingTask != null){
                    holder.drawableLoadingTask.cancel();
                }
                if (holder.categoryLoadingTask != null){
                    holder.categoryLoadingTask.cancel();
                }
                holder.textCategoryView.setText("");

                holder.foundApplicationItem = this.getItem(position);
                final ApplicationData applicationData = holder.foundApplicationItem.applicationData;
                SpannableString spannableString = new SpannableString(applicationData.name);
                if (holder.foundApplicationItem.selectionStartIndex != null){
                    spannableString.setSpan(new ForegroundColorSpan(getResources().getColor(R.color.blue_themed)),
                            holder.foundApplicationItem.selectionStartIndex,
                            holder.foundApplicationItem.selectionEndIndex,
                            Spanned.SPAN_COMPOSING);
                    spannableString.setSpan(new StyleSpan(Typeface.BOLD),
                            holder.foundApplicationItem.selectionStartIndex,
                            holder.foundApplicationItem.selectionEndIndex,
                            Spanned.SPAN_COMPOSING);
                }
                holder.textView.setText(spannableString);
                holder.imageView.setVisibility(View.INVISIBLE);

                holder.drawableLoadingTask =  application().loadApplicationIcon(applicationData, new RunitApp.OnLoadApplicationIconCallback() {

                    SearchItemDetails forHolder = holder;

                    @Override
                    public void load(ApplicationData applicationData, Drawable drawable) {
                        if (forHolder.foundApplicationItem.applicationData == applicationData) {
                            forHolder.imageView.setImageDrawable(drawable);
                            forHolder.imageView.setVisibility(View.VISIBLE);
                        }
                    }
                });

                holder.categoryLoadingTask =  application().loadApplicationCategory(applicationData, new RunitApp.OnLoadCategoryCallback() {

                    SearchItemDetails forHolder = holder;

                    @Override
                    public void load(ApplicationData applicationData, RunitApp.Category category) {
                        if (forHolder.foundApplicationItem.applicationData == applicationData) {
                            forHolder.textCategoryView.setText(category.name);
                        }
                    }
                });
                return convertView;
            }

            class SearchItemDetails {

                final View firstItemMarginView;
                final TextView textView;
                final TextView textCategoryView;
                final ImageView imageView;

                RunitApp.AppSearchResult foundApplicationItem;

                BackgroundTaskManager.BackgroundTask<?> drawableLoadingTask;
                BackgroundTaskManager.BackgroundTask<?> categoryLoadingTask;


                SearchItemDetails(View firstItemMarginView, TextView textView, TextView textCategoryView, ImageView imageView) {
                    this.firstItemMarginView = firstItemMarginView;
                    this.textView = textView;
                    this.textCategoryView = textCategoryView;
                    this.imageView = imageView;
                }
            }
        };
        view(R.id.dashboard_search_result_list, ListView.class).setAdapter(searchResultAdapter);
        view(R.id.dashboard_app_categories_panel).setVisibility(View.GONE);
        application().fetchApplicationCategories(new RunitApp.OnAppCategoriesCallback() {
            @Override
            public void fetched(List<RunitApp.Category> fetchData, boolean inProgress) {
                view(R.id.dashboard_app_categories_panel).setVisibility(View.VISIBLE);
            }

            @Override
            public void noData() {

            }
        });
    }

    @Override
    public void onBackPressed() {
        if (view(R.id.dashboard_search_button).getVisibility() != View.VISIBLE){
            closeSearch();
        } else {
            super.onBackPressed();
        }

    }

    private void closeSearch() {
        searchButtonAppearanceController.show();
        view(R.id.dashboard_search_edit, EditText.class).setText("");
        searchPanelAppearanceController.hide();
        searchResultPanelAppearanceController.hide();
        hotAppsPanelAppearanceController.show();
    }

    private void initiateAppsSearch() {
        final String searchQuery = view(R.id.dashboard_search_edit,EditText.class).getText().toString().trim();
        application().searchApplicationByName(searchQuery.trim(), new RunitApp.OnAppSearchCallback(){
            @Override
            public void found(String searchQuery, List<RunitApp.AppSearchResult> searchResultList) {
                searchResultAdapter.clear();
                searchResultAdapter.addAll(searchResultList);
                searchResultAdapter.notifyDataSetChanged();
            }
        });
    }

    @Override
    protected void onDestroy() {
        application().cancelSearchApplicationByName();
        application().cancelMostResentApplicationFetch();
        super.onDestroy();
    }

    public static PendingIntent openDashboardForSearch(Context context) {
        Intent intent = new Intent(context, DashboardActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_DOCUMENT | Intent.FLAG_ACTIVITY_NO_HISTORY);
        intent.putExtra("go_search_request", true);
        return PendingIntent.getActivity(context, 0, intent,
                PendingIntent.FLAG_UPDATE_CURRENT);
    }

    private class ImageOnlyViewRenderer implements RefreshableListAdapter.ViewRender{

        @Override
        public View renderData(final ApplicationData data, Drawable icon, ViewGroup viewGroup, LayoutInflater inflater) {
            ImageView imageView = (ImageView) inflater.inflate(R.layout.launcher_view,viewGroup,false);
            imageView.setImageDrawable(icon);
            imageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    DashboardActivity.this.application().launchApplication(data);
                }
            });
            return imageView;
        }
    }

    private class ImageWithCaptionViewRenderer implements RefreshableListAdapter.ViewRender{

        @Override
        public View renderData(final ApplicationData data, Drawable icon, ViewGroup viewGroup, LayoutInflater inflater) {
            View answer = inflater.inflate(R.layout.launcher_view_with_caption,viewGroup,false);
            ImageView imageView = (ImageView) answer.findViewById(R.id.icon);
            imageView.setImageDrawable(icon);
            answer.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    DashboardActivity.this.application().launchApplication(data);
                }
            });
            ((TextView)answer.findViewById(R.id.name)).setText(data.name);
            return answer;
        }
    }
}
