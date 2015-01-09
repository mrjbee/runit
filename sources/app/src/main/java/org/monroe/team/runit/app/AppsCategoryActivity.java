package org.monroe.team.runit.app;

import android.animation.Animator;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Spinner;
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
import org.monroe.team.runit.app.views.PushActionView;

import java.util.List;

import static org.monroe.team.android.box.ui.animation.apperrance.AppearanceControllerBuilder.alpha;
import static org.monroe.team.android.box.ui.animation.apperrance.AppearanceControllerBuilder.animateAppearance;
import static org.monroe.team.android.box.ui.animation.apperrance.AppearanceControllerBuilder.combine;
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
    private AppearanceController appModPanelController;
    private AppearanceController appsGridController;
    private ApplicationData appUnderMod;
    private AsyncTask<Void, Void, Void> reFetchLaterAsyncTask;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_apps_category);
        float[] buttonBounds = null;
        if (getIntent() != null) {
            buttonBounds = getIntent().getFloatArrayExtra("button_bounds");
            if (buttonBounds == null || buttonBounds.length == 0){
                Rect rect = getIntent().getSourceBounds();
                if (rect != null) {
                    buttonBounds = new float[]{rect.left, rect.top, rect.width(), rect.height()};
                } else {
                    buttonBounds = new float[]{0,0,0,0};
                }
            }
            AppearanceController headerAppearanceController = combine(
                    animateAppearance(
                            view(R.id.ac_header_panel),
                            ySlide(0f,
                                    !DisplayUtils.isLandscape(getResources(), R.bool.class) ?
                                            buttonBounds[1] - DisplayUtils.dpToPx(40, getResources()) :
                                            buttonBounds[1] - DisplayUtils.dpToPx(20, getResources())))
                            .showAnimation(duration_constant(400), interpreter_accelerate_decelerate()),
                    animateAppearance(
                            view(R.id.ac_header_panel),
                            xSlide(0f, buttonBounds[0] - DisplayUtils.dpToPx(10, getResources())))
                            .showAnimation(duration_constant(400), interpreter_accelerate_decelerate()));


            AppearanceController backgroundAppearanceController = animateAppearance(
                    view(R.id.ac_background_panel),
                    alpha(1f, 0.2f))
                    .showAnimation(duration_constant(700), interpreter_decelerate(null))
                    .build();


            if (savedInstanceState != null && savedInstanceState.getBoolean("animation_off")){
                headerAppearanceController.showWithoutAnimation();
                backgroundAppearanceController.showWithoutAnimation();
            } else {
                headerAppearanceController.hideWithoutAnimation();
                backgroundAppearanceController.hideWithoutAnimation();
                headerAppearanceController.show();
                backgroundAppearanceController.show();
            }
        }

        DisplayUtils.landscape_portrait(getResources(), R.bool.class, new Closure<Void, Void>() {
            @Override
            public Void execute(Void arg) {
                appModPanelController = animateAppearance(
                        view(R.id.ac_app_mod_panel),
                        ySlide(0, DisplayUtils.screenHeight(getResources())))
                        .showAnimation(duration_constant(400), interpreter_overshot())
                        .hideAnimation(duration_constant(200), interpreter_accelerate_decelerate())
                        .hideAndGone()
                        .build();

                appsPanelController = animateAppearance(
                        view(R.id.ac_apps_panel),
                        widthSlide((int) DisplayUtils.dpToPx(450, getResources()), 0))
                        .showAnimation(duration_constant(200), interpreter_decelerate(null))
                        .hideAnimation(duration_constant(200), interpreter_accelerate_decelerate())
                        .hideAndGone()
                        .build();
                return null;
            }
        }, new Closure<Void, Void>() {
            @Override
            public Void execute(Void arg) {

                appModPanelController = animateAppearance(
                        view(R.id.ac_app_mod_panel),
                        ySlide(0, DisplayUtils.screenHeight(getResources())))
                        .showAnimation(duration_constant(400), interpreter_overshot())
                        .hideAnimation(duration_constant(200), interpreter_accelerate_decelerate())
                        .hideAndGone()
                        .build();

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

        view(R.id.ac_apps_grid, PushToGridView.class).setPushThreshold(100);
        view(R.id.ac_apps_grid, PushToGridView.class).setPushListener(new PushToActionAdapter(150) {

            @Override
            protected void cancelPushAction(float pushCoefficient, float x, float y) {
                view(R.id.ac_push_action_view, PushActionView.class)
                        .stopPush();
            }

            @Override
            protected void applyPushAction(float x, float y) {
                view(R.id.ac_push_action_view, PushActionView.class)
                        .stopPush();
                appsPanelController.hide();
            }


            @Override
            protected void beforePush(float x, float y) {
                view(R.id.ac_push_action_view, PushActionView.class)
                        .startPush(x,y,"Push it ...", "Close Apps Panel");
            }

            @Override
            protected void pushInProgress(float pushCoefficient, float x, float y) {
                view(R.id.ac_push_action_view, PushActionView.class)
                        .pushing(x,y,pushCoefficient);
            }
        });

        appsPanelController.hideWithoutAnimation();
        appModPanelController.hideWithoutAnimation();

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
                //TODO: Replace with onItemClick
                convertView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        RunitApp.Category category = categoryAdapter.getItem(position);
                        Spinner spinner = view(R.id.ac_app_mod_category_spinner, Spinner.class);
                        ArrayAdapter<RunitApp.Category> spinAdapter = (ArrayAdapter<RunitApp.Category>) spinner.getAdapter();
                        int position = spinAdapter.getPosition(category);
                        if (position < 0){
                            position = 0;
                        }
                        appUnderMod = null;
                        spinner.setSelection(position, false);

                        view(R.id.ac_category_label, TextView.class).setText(category.name);
                        categoryAppsAdapter.clear();
                        categoryAppsAdapter.notifyDataSetChanged();
                        application().fetchApplicationByCategory(category, new RunitApp.OnAppSearchCallback() {
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
                        appModPanelController.hide();
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
                            convertView.findViewById(R.id.header_space),
                            (TextView) convertView.findViewById(R.id.search_item_text),
                            (ImageView) convertView.findViewById(R.id.search_item_image));
                    convertView.setTag(holder);
                } else {
                    holder = (SearchItemDetails) convertView.getTag();
                }
                if (holder.drawableLoadingTask != null){
                    holder.drawableLoadingTask.cancel();
                }
                if(position < 3){
                    holder.firstItemMarginView.setVisibility(View.VISIBLE);
                } else {
                    holder.firstItemMarginView.setVisibility(View.GONE);
                }

                holder.foundApplicationItem = this.getItem(position);
                final ApplicationData applicationData = holder.foundApplicationItem.applicationData;
                holder.textView.setText(applicationData.name);
                holder.imageView.setVisibility(View.INVISIBLE);

                BackgroundTaskManager.BackgroundTask<?> loadTask = application().loadApplicationIcon(applicationData, new RunitApp.OnLoadApplicationIconCallback() {
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

                BackgroundTaskManager.BackgroundTask<?> drawableLoadingTask;

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

        final GridView gridView =  view(R.id.ac_apps_grid, GridView.class);
        final float headerSize = DisplayUtils.dpToPx(40*3,getResources());
        final View header= view(R.id.ac_category_panel);
        header.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return true;
            }
        });
        view(R.id.ac_apps_grid, GridView.class).setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {

            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                View c = gridView.getChildAt(0);
                if (c == null) return;
                float scrollYPx = -c.getTop() + gridView.getFirstVisiblePosition() * c.getHeight();
                if (scrollYPx > headerSize){
                    header.setAlpha(0);
                } else {
                    float alpha = 1 - scrollYPx/headerSize;
                    header.setAlpha(alpha);
                }
                header.invalidate();
            }
        });

        view(R.id.ac_apps_grid, GridView.class).setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, final View view, int position, long id) {
                ApplicationData data = categoryAppsAdapter.getItem(position).applicationData;
                view(R.id.ac_app_mod_name,TextView.class).setText(data.name);
                appUnderMod = data;
                application().loadApplicationIcon(data, new RunitApp.OnLoadApplicationIconCallback() {
                    @Override
                    public void load(ApplicationData applicationData, Drawable drawable) {
                        if (applicationData == appUnderMod){
                            view(R.id.ac_app_mod_icon, ImageView.class).setImageDrawable(drawable);
                        }
                    }
                });
                appsPanelController.hideAndCustomize(new AppearanceController.AnimatorCustomization() {
                    @Override
                    public void customize(Animator animator) {
                        animator.addListener(new AppearanceControllerOld.AnimatorListenerAdapter(){
                            @Override
                            public void onAnimationEnd(Animator animation) {
                               appModPanelController.show();
                            }
                        });
                    }
                });

                return true;
            }
        });

        view(R.id.ac_apps_grid, GridView.class).setAdapter(categoryAppsAdapter);
        view(R.id.ac_synch_panel).setVisibility(View.GONE);
        view(R.id.ac_category_list, PushToListView.class).setPushListener(new PushToActionAdapter(150) {
            @Override
            protected void cancelPushAction(float pushCoefficient, float x, float y) {
                view(R.id.ac_root_layout).animate().alpha(1f).setInterpolator(interpreter_decelerate(null).build());
                view(R.id.ac_push_action_view, PushActionView.class).stopPush();
            }

            @Override
            protected void applyPushAction(float x, float y) {
                AppsCategoryActivity.this.finish();
                view(R.id.ac_push_action_view, PushActionView.class).stopPush();
            }

            @Override
            protected void beforePush(float x, float y) {
                view(R.id.ac_push_action_view, PushActionView.class)
                        .startPush(x, y, "Keep pushing ...", "Close Categories");
            }

            @Override
            protected void pushInProgress(float pushCoefficient, float x, float y) {
                float alpha = (1 - pushCoefficient * 0.5f);
                view(R.id.ac_root_layout).setAlpha(alpha);
                view(R.id.ac_push_action_view, PushActionView.class).pushing(x, y, pushCoefficient);
            }
        });
        ArrayAdapter<RunitApp.Category> categoriesAdapter = new ArrayAdapter<RunitApp.Category>(this,R.layout.item_category_drop);
        categoriesAdapter.addAll(application().supportedCategories());
        view(R.id.ac_app_mod_category_spinner, Spinner.class).setAdapter(categoriesAdapter);
        view(R.id.ac_app_mod_category_spinner, Spinner.class).setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
               if (appUnderMod == null) return;
               RunitApp.Category category = (RunitApp.Category) view(R.id.ac_app_mod_category_spinner, Spinner.class).getAdapter().getItem(position);
               application().updateAppCategory(appUnderMod, category);
               appUnderMod = null;
               appModPanelController.hide();
               reFetchCategories();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                appModPanelController.hide();
            }
        });


        view(R.id.ac_category_close).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                appsGridController.hide();
                appsPanelController.hide();
            }
        });


        view(R.id.ac_app_mod_close).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                appModPanelController.hide();
            }
        });
        view(R.id.ac_synch_panel).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(AppsCategoryActivity.this);
                builder.setMessage("Apps cataloging consist of getting category from Google Play per each installed application. " +
                        "This process may take some time which depends from your internet connection and count of installed applications." +
                        "\n\n Please also note that in order to safe your mobile traffic, we design it to run only during WI-FI connection.")
                        .setTitle("About Apps Cataloging")
                        .setPositiveButton("Got It", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                // FIRE ZE MISSILES!
                            }
                        });
                AlertDialog dialog = builder.create();
                dialog.show();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        reFetchCategories();
    }

    private void reFetchCategories() {
        application().fetchApplicationCategories(new RunitApp.OnAppCategoriesCallback() {
            @Override
            public void fetched(List<RunitApp.Category> fetchData, boolean syncInProgress) {
                view(R.id.ac_synch_panel).setVisibility(syncInProgress? View.VISIBLE:View.GONE);
                categoryAdapter.clear();
                categoryAdapter.addAll(fetchData);
                categoryAdapter.notifyDataSetChanged();
                if (syncInProgress && (reFetchLaterAsyncTask == null || reFetchLaterAsyncTask.getStatus() == AsyncTask.Status.FINISHED)){
                    reFetchLaterAsyncTask = new AsyncTask<Void, Void, Void>() {
                        @Override
                        protected Void doInBackground(Void... params) {
                            try {
                                Thread.sleep(1000);
                            } catch (InterruptedException e) {
                            }
                            reFetchLaterAsyncTask = null;
                            reFetchCategories();
                             return null;
                        }
                    };
                    reFetchLaterAsyncTask.execute();
                }
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
        if (view(R.id.ac_apps_panel).getVisibility() != View.GONE ||
            view(R.id.ac_app_mod_panel).getVisibility() != View.GONE ){
            appsPanelController.hide();
            appModPanelController.hide();
        } else {
            super.onBackPressed();
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean("animation_off",true);
    }

    public static PendingIntent open(Context context) {
            Intent intent = new Intent(context, ApplicationDrawerActivity.class)
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    .addFlags(Intent.FLAG_ACTIVITY_MULTIPLE_TASK)
                    .addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY)
                    .addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
            return PendingIntent.getActivity(context, 0, intent,
                    PendingIntent.FLAG_UPDATE_CURRENT);
    }
}
