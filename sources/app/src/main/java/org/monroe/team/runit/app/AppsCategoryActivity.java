package org.monroe.team.runit.app;

import android.animation.Animator;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import org.monroe.team.android.box.Closure;
import org.monroe.team.android.box.ui.AppearanceControllerOld;
import org.monroe.team.android.box.ui.PushToActionAdapter;
import org.monroe.team.android.box.ui.PushToGridView;
import org.monroe.team.android.box.utils.DisplayUtils;
import org.monroe.team.android.box.manager.BackgroundTaskManager;
import org.monroe.team.android.box.support.ActivitySupport;
import org.monroe.team.android.box.ui.animation.apperrance.AppearanceController;
import org.monroe.team.runit.app.android.RunitApp;
import org.monroe.team.runit.app.uc.entity.ApplicationData;
import org.monroe.team.android.box.ui.PushToListView;

import java.util.List;

import static org.monroe.team.android.box.ui.animation.apperrance.AppearanceControllerBuilder.alpha;
import static org.monroe.team.android.box.ui.animation.apperrance.AppearanceControllerBuilder.animateAppearance;
import static org.monroe.team.android.box.ui.animation.apperrance.AppearanceControllerBuilder.duration_constant;
import static org.monroe.team.android.box.ui.animation.apperrance.AppearanceControllerBuilder.heightSlide;
import static org.monroe.team.android.box.ui.animation.apperrance.AppearanceControllerBuilder.interpreter_accelerate_decelerate;
import static org.monroe.team.android.box.ui.animation.apperrance.AppearanceControllerBuilder.interpreter_decelerate;
import static org.monroe.team.android.box.ui.animation.apperrance.AppearanceControllerBuilder.interpreter_overshot;
import static org.monroe.team.android.box.ui.animation.apperrance.AppearanceControllerBuilder.widthSlide;
import static org.monroe.team.android.box.ui.animation.apperrance.AppearanceControllerBuilder.xSlide;
import static org.monroe.team.android.box.ui.animation.apperrance.AppearanceControllerBuilder.ySlide;


public class AppsCategoryActivity extends ActivitySupport<RunitApp> {

    private ArrayAdapter<RunitApp.Category> categoryAdapter;
    private ArrayAdapter<RunitApp.AppSearchResult> categoryAppsAdapter;

    private AppearanceController appsPanelController;
    private AppearanceController appsGridController;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_apps_category);
        float[] buttonBounds = null;
        if (getIntent() != null) {
            buttonBounds = getIntent().getFloatArrayExtra("button_bounds");
            //40dp
            AppearanceController headerAppearanceController = animateAppearance(
                    view(R.id.ac_header_panel),
                    ySlide(0f, buttonBounds[1] - DisplayUtils.dpToPx(40, getResources())))
                    .showAnimation(duration_constant(400), interpreter_accelerate_decelerate())
                    .build();
            AppearanceController headerXAppearanceController = animateAppearance(
                    view(R.id.ac_header_panel),
                    xSlide(0f, buttonBounds[0] - DisplayUtils.dpToPx(10, getResources())))
                    .showAnimation(duration_constant(400), interpreter_accelerate_decelerate())
                    .build();

            headerAppearanceController.hideWithoutAnimation();
            headerAppearanceController.show();
            headerXAppearanceController.hideWithoutAnimation();
            headerXAppearanceController.show();

            AppearanceController backgroundAppearanceController = animateAppearance(
                    view(R.id.ac_background_panel),
                    alpha(1f, 0.2f))
                    .showAnimation(duration_constant(300), interpreter_decelerate(null))
                    .build();
            backgroundAppearanceController.hideWithoutAnimation();
            backgroundAppearanceController.show();

        }

        DisplayUtils.landscape_portrait(getResources(), R.bool.class, new Closure<Void, Void>() {
            @Override
            public Void execute(Void arg) {
                appsPanelController = animateAppearance(
                        view(R.id.ac_apps_panel),
                        widthSlide((int) DisplayUtils.dpToPx(300, getResources()), 0))
                        .showAnimation(duration_constant(200), interpreter_decelerate(null))
                        .hideAnimation(duration_constant(200), interpreter_accelerate_decelerate())
                        .hideAndGone()
                        .build();
                return null;
            }
        }, new Closure<Void, Void>() {
            @Override
            public Void execute(Void arg) {
                appsPanelController = animateAppearance(
                        view(R.id.ac_apps_panel),
                        heightSlide((int) DisplayUtils.dpToPx(400, getResources()), 0))
                        .showAnimation(duration_constant(200), interpreter_decelerate(null))
                        .hideAnimation(duration_constant(200), interpreter_accelerate_decelerate())
                        .hideAndGone()
                        .build();
                return null;
            }
        });


        appsPanelController.hideWithoutAnimation();

        appsGridController = animateAppearance(
                view(R.id.ac_apps_grid),
                alpha(1f,0f))
                .showAnimation(duration_constant(400), interpreter_decelerate(null))
                .hideAnimation(duration_constant(200), interpreter_accelerate_decelerate())
                .hideAndGone()
                .build();

        appsGridController.hideWithoutAnimation();

        categoryAdapter = new ArrayAdapter<RunitApp.Category>(getApplicationContext(), R.layout.item_category){
            @Override
            public View getView(final int position, View convertView, ViewGroup parent) {
                if (convertView == null){
                    LayoutInflater inflater = (LayoutInflater) getContext()
                            .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                    convertView = inflater.inflate(R.layout.item_category, null);
                }

                RunitApp.Category category = this.getItem(position);
                ((TextView)convertView.findViewById(R.id.name)).setText(category.name);
                ((TextView)convertView.findViewById(R.id.sub_name)).setText("apps "+category.appsCount);
                convertView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        RunitApp.Category category = categoryAdapter.getItem(position);
                        view(R.id.ac_category_label, TextView.class).setText(category.name);
                        categoryAppsAdapter.clear();
                        categoryAppsAdapter.notifyDataSetChanged();
                        application().searchApplicationByCategory(v, category, new RunitApp.OnAppSearchCallback() {
                            @Override
                            public void found(String searchQuery, List<RunitApp.AppSearchResult> searchResultList) {
                                categoryAppsAdapter.clear();
                                categoryAppsAdapter.addAll(searchResultList);
                                categoryAppsAdapter.notifyDataSetChanged();
                            }
                        });
                        appsGridController.hideWithoutAnimation();
                        appsPanelController.showAndCustomize(new AppearanceController.AnimatorCustomization() {
                            @Override
                            public void customize(Animator animator) {
                                animator.addListener(new AppearanceControllerOld.AnimatorListenerAdapter() {
                                    @Override
                                    public void onAnimationEnd(Animator animation) {
                                        appsGridController.show();
                                    }
                                });
                            }
                        });
                    }
                });
                return convertView;
            }
        };
        view(R.id.ac_category_list, ListView.class).setAdapter(categoryAdapter);

        categoryAppsAdapter = new ArrayAdapter<RunitApp.AppSearchResult>(getApplicationContext(), R.layout.item_category_app) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                final SearchItemDetails holder;
                if (convertView == null){
                    LayoutInflater inflater = (LayoutInflater) getContext()
                            .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                    convertView = inflater.inflate(R.layout.item_category_app, null);
                    holder = new SearchItemDetails(
                            convertView.findViewById(R.id.search_first_item_stub),
                            (TextView) convertView.findViewById(R.id.search_item_text),
                            (ImageView) convertView.findViewById(R.id.search_item_image));
                    convertView.setTag(holder);
                } else {
                    holder = (SearchItemDetails) convertView.getTag();
                }
                if (holder.drawableLoadingTask != null){
                    holder.drawableLoadingTask.cancel();
                }
                holder.foundApplicationItem = this.getItem(position);
                final ApplicationData applicationData = holder.foundApplicationItem.applicationData;
                holder.textView.setText(applicationData.name);
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

        view(R.id.ac_apps_grid, GridView.class).setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                appsPanelController.hide();
                application().launchApplication(categoryAppsAdapter.getItem(position).applicationData);
            }
        });

        view(R.id.ac_apps_grid, GridView.class).setAdapter(categoryAppsAdapter);
        view(R.id.ac_synch_panel).setVisibility(View.GONE);
        view(R.id.ac_category_list, PushToListView.class).setPushListener(new PushToActionAdapter(150) {
              @Override
              protected void cancelPushAction(float pushCoefficient, float x, float y) {
                  view(R.id.ac_root_layout).animate().alpha(1f).setInterpolator(interpreter_decelerate(null).build());
              }

              @Override
              protected void applyPushAction(float x, float y) {
                  AppsCategoryActivity.this.finish();
              }

              @Override
              protected void pushInProgress(float pushCoefficient, float x, float y) {
                  float alpha = (1 - pushCoefficient*0.9f);
                  view(R.id.ac_root_layout).setAlpha(alpha);
              }
        });

        view(R.id.ac_apps_grid, PushToGridView.class).setPushListener(new PushToActionAdapter(100) {
            @Override
            protected void cancelPushAction(float pushCoefficient, float x, float y) {
                view(R.id.ac_root_layout).animate().alpha(1f).setInterpolator(interpreter_decelerate(null).build());
            }

            @Override
            protected void applyPushAction(float x, float y) {
                AppsCategoryActivity.this.finish();
            }

            @Override
            protected void pushInProgress(float pushCoefficient, float x, float y) {
                float alpha = (1 - pushCoefficient * 0.9f);
                view(R.id.ac_root_layout).setAlpha(alpha);
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        application().fetchApplicationCategories(new RunitApp.OnAppCategoriesCallback() {
            @Override
            public void fetched(List<RunitApp.Category> fetchData, boolean syncInProgress) {
                view(R.id.ac_synch_panel).setVisibility(syncInProgress?View.VISIBLE:View.GONE);
                categoryAdapter.clear();
                categoryAdapter.addAll(fetchData);
                categoryAdapter.notifyDataSetChanged();
            }

            @Override
            public void noData() {
                //TODO should be not happends as button not visible
                view(R.id.ac_synch_panel).setVisibility(View.VISIBLE);
                categoryAdapter.clear();
                categoryAdapter.notifyDataSetChanged();
            }
        });
    }

    @Override
    public void onBackPressed() {
        if (view(R.id.ac_apps_panel).getVisibility() != View.GONE){
            appsPanelController.hide();
        } else {
            super.onBackPressed();
        }
    }
}
