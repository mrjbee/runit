package org.monroe.team.runit.app.android;

import android.animation.Animator;
import android.animation.TimeInterpolator;
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
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import org.monroe.team.android.box.SizeUtils;
import org.monroe.team.android.box.manager.BackgroundTaskManager;
import org.monroe.team.android.box.support.ActivitySupport;
import org.monroe.team.android.box.ui.AppearanceControllerOld;
import org.monroe.team.android.box.ui.animation.apperrance.AppearanceController;
import org.monroe.team.runit.app.R;
import org.monroe.team.runit.app.android.preneter.RefreshableListAdapter;
import org.monroe.team.runit.app.uc.entity.ApplicationData;

import static org.monroe.team.android.box.ui.animation.apperrance.AppearanceControllerBuilder.*;

import java.util.List;
import java.util.Set;


public class DashboardActivity extends ActivitySupport <RunitApp> {

    private ArrayAdapter<RunitApp.AppSearchResult> searchResultAdapter;

    private AppearanceController recentAppsPanelAppearanceController;
    private AppearanceController mostUsedAppsPanelAppearanceController;
    private AppearanceController searchButtonAppearanceController;
    private AppearanceController searchPanelAppearanceController;
    private AppearanceController backgroundAppearanceController;

    private RefreshableListAdapter recentAppsRefreshableListAdapter;
    private RefreshableListAdapter mostUsedAppsRefreshableListAdapter;
    private boolean requestKeyboardFlag;

    @Override
    protected void onCreate (final Bundle savedInstanceState) {
        setContentView(R.layout.activity_dashboard);
        view(R.id.dashboard_bkg_image_scroll_crunch).setEnabled(false);
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
            recentAppsPanelAppearanceController.hideWithoutAnimation();
            recentAppsPanelAppearanceController.showAndCustomize(new AppearanceController.AnimatorCustomization() {
                @Override
                public void customize(Animator animator) {
                    animator.setStartDelay(600);
                }
            });
            mostUsedAppsPanelAppearanceController.hideWithoutAnimation();
            mostUsedAppsPanelAppearanceController.showAndCustomize(new AppearanceController.AnimatorCustomization() {
                @Override
                public void customize(Animator animator) {
                    animator.setStartDelay(400);
                }
            });
            searchButtonAppearanceController.showWithoutAnimation();
            searchPanelAppearanceController.hideWithoutAnimation();
        }else if (searchActivated){
            recentAppsPanelAppearanceController.hideWithoutAnimation();
            mostUsedAppsPanelAppearanceController.hideWithoutAnimation();
            searchButtonAppearanceController.hideWithoutAnimation();
            searchPanelAppearanceController.showWithoutAnimation();
            backgroundAppearanceController.hideWithoutAnimation();
        } else {
            recentAppsPanelAppearanceController.showWithoutAnimation();
            mostUsedAppsPanelAppearanceController.showWithoutAnimation();
            searchButtonAppearanceController.showWithoutAnimation();
            searchPanelAppearanceController.hideWithoutAnimation();
            backgroundAppearanceController.showWithoutAnimation();
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
                mostUsedAppsPanelAppearanceController.hide();
                recentAppsPanelAppearanceController.hide();
                searchPanelAppearanceController.showAndCustomize(new AppearanceController.AnimatorCustomization() {
                    @Override
                    public void customize(Animator animator) {
                        animator.setStartDelay(400);
                        animator.addListener(new AppearanceControllerOld.AnimatorListenerAdapter() {
                            @Override
                            public void onAnimationEnd(Animator animation) {
                                focusOnEdit();
                            }
                        });
                    }
                });
                backgroundAppearanceController.hide();
            }
        });
        view(R.id.dashboard_search_result_list, ListView.class).setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                view(R.id.dashboard_search_edit, EditText.class).setText("");
                searchPanelAppearanceController.hideWithoutAnimation();
                recentAppsPanelAppearanceController.show();
                mostUsedAppsPanelAppearanceController.show();
                searchButtonAppearanceController.showWithoutAnimation();
                backgroundAppearanceController.showWithoutAnimation();
                application().launchApplication(searchResultAdapter.getItem(position).applicationData);
                searchResultAdapter.clear();
                searchResultAdapter.notifyDataSetChanged();
            }
        });

        view(R.id.dashboard_search_edit,EditText.class).addTextChangedListener(new TextWatcher() {
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
        recentAppsRefreshableListAdapter = new RefreshableListAdapter(application(),
                view(R.id.dashboard_recent_container, ViewGroup.class),
                R.layout.launcher_view,
                5);
        mostUsedAppsRefreshableListAdapter = new RefreshableListAdapter(application(),
                view(R.id.dashboard_most_used_container, ViewGroup.class),
                R.layout.launcher_view,
                5);
    }

    private void setupAnimations() {
        backgroundAppearanceController = animateAppearance(
                view(R.id.dashboard_bkg_image_scroll_crunch),
                alpha(1f, 0.1f))
                .showAnimation(duration_constant(1500), interpreter_accelerate())
                .hideAnimation(duration_constant(500), interpreter_decelerate())
                .build();

        //Involve alpha too
        recentAppsPanelAppearanceController = animateAppearance(
                view(R.id.dashboard_recent_apps_panel),
                ySlide(0f, getResources().getDisplayMetrics().heightPixels))
                .showAnimation(duration_auto_fint(), interpreter_overshot())
                .hideAnimation(duration_auto_fint(), interpreter_overshot())
                .hideAndGone()
                .build();

        mostUsedAppsPanelAppearanceController = animateAppearance(
                view(R.id.dashboard_most_used_apps_panel),
                ySlide(0f, getResources().getDisplayMetrics().heightPixels))
                .showAnimation(duration_auto_fint(), interpreter_overshot())
                .hideAnimation(duration_auto_fint())
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
                ySlide(0f, -SizeUtils.dpToPx(70, getResources())))
                .showAnimation(duration_auto_fint(), interpreter_overshot())
                .hideAnimation(duration_auto_fint(), interpreter_overshot())
                .build();
    }

    private TimeInterpreterBuilder interpreter_accelerate() {
        return new TimeInterpreterBuilder() {
            @Override
            public TimeInterpolator build() {
                return new AccelerateInterpolator();
            }
        };
    }

    private TimeInterpreterBuilder interpreter_decelerate() {
        return new TimeInterpreterBuilder() {
            @Override
            public TimeInterpolator build() {
                return new DecelerateInterpolator();
            }
        };
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

    private void fetchMostRecentApplications() {
        application().fetchMostRecentApplication(new RunitApp.OnApplicationFetchedCallback() {
            @Override
            public void fetched(List<ApplicationData> applicationDataList) {
                if (applicationDataList.isEmpty()){
                    view(R.id.dashboard_recent_no_data_text).setVisibility(View.VISIBLE);
                } else {
                    view(R.id.dashboard_recent_no_data_text).setVisibility(View.GONE);
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
                Set<ApplicationData> visibleApplicationsSet = recentAppsRefreshableListAdapter.getVisibleApplications();
                for (ApplicationData visibleData : visibleApplicationsSet) {
                    applicationDataList.remove(visibleData);
                }
                if (applicationDataList.isEmpty()){
                    view(R.id.dashboard_most_used_no_data_text).setVisibility(View.VISIBLE);
                } else {
                    view(R.id.dashboard_most_used_no_data_text).setVisibility(View.GONE);
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

                holder.foundApplicationItem = this.getItem(position);
                final ApplicationData applicationData = holder.foundApplicationItem.applicationData;
                SpannableString spannableString = new SpannableString(applicationData.name);
                if (holder.foundApplicationItem.selectionStartIndex != null){
                    spannableString.setSpan(new ForegroundColorSpan(
                            getResources().getColor(R.color.blue_themed)),
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

                BackgroundTaskManager.BackgroundTask<Drawable> loadTask = application().loadApplicationIcon(applicationData, new RunitApp.OnLoadApplicationIconCallback() {
                    SearchItemDetails forHolder = holder;

                    @Override
                    public void load(ApplicationData applicationData, Drawable drawable) {
                        if (forHolder.foundApplicationItem.applicationData == applicationData) {
                            forHolder.imageView.setImageDrawable(drawable);
                            forHolder.imageView.setVisibility(View.VISIBLE);
                        }
                    }
                });

                holder.drawableLoadingTask = loadTask;
                return convertView;
            }

            class SearchItemDetails {

                final View firstItemMarginView;
                final TextView textView;
                final ImageView imageView;

                RunitApp.AppSearchResult foundApplicationItem;

                BackgroundTaskManager.BackgroundTask<Drawable> drawableLoadingTask;

                SearchItemDetails(View firstItemMarginView, TextView textView, ImageView imageView) {
                    this.firstItemMarginView = firstItemMarginView;
                    this.textView = textView;
                    this.imageView = imageView;
                }
            }
        };
        view(R.id.dashboard_search_result_list, ListView.class).setAdapter(searchResultAdapter);
    }

    @Override
    public void onBackPressed() {
        if (view(R.id.dashboard_search_button).getVisibility() != View.VISIBLE){
            searchButtonAppearanceController.show();
            backgroundAppearanceController.show();
            view(R.id.dashboard_search_edit, EditText.class).setText("");
            searchPanelAppearanceController.hide();
            mostUsedAppsPanelAppearanceController.show();
            recentAppsPanelAppearanceController.show();
        } else {
            super.onBackPressed();
        }

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


}
