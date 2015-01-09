package org.monroe.team.runit.app;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.Space;
import android.widget.Spinner;
import android.widget.TextView;

import org.monroe.team.android.box.Closure;
import org.monroe.team.android.box.GenericListViewAdapter;
import org.monroe.team.android.box.Lists;
import org.monroe.team.android.box.Views;
import org.monroe.team.android.box.support.ActivitySupport;
import org.monroe.team.android.box.ui.AppearanceControllerOld;
import org.monroe.team.android.box.ui.PushToActionAdapter;
import org.monroe.team.android.box.ui.PushToListView;
import org.monroe.team.android.box.ui.animation.apperrance.AppearanceController;
import org.monroe.team.android.box.utils.DisplayUtils;
import org.monroe.team.runit.app.android.RunitApp;
import org.monroe.team.runit.app.uc.entity.ApplicationData;
import org.monroe.team.runit.app.views.PushActionView;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import static org.monroe.team.android.box.ui.animation.apperrance.AppearanceControllerBuilder.animateAppearance;
import static org.monroe.team.android.box.ui.animation.apperrance.AppearanceControllerBuilder.duration_constant;
import static org.monroe.team.android.box.ui.animation.apperrance.AppearanceControllerBuilder.interpreter_decelerate;
import static org.monroe.team.android.box.ui.animation.apperrance.AppearanceControllerBuilder.*;


public class ApplicationDrawerActivity extends ActivitySupport<RunitApp> {

    private List<CategoryData> categoryDataList = new ArrayList<>();
    private ArrayAdapter<CategoryData> categoriesAdapter;
    private ArrayAdapter<CategoryData> categoriesQuickAdapter;

    private AppearanceController headerAppearanceController;
    private AppearanceController rootAppearanceController;
    private AppearanceController shadowAppearanceController;
    private AppearanceController gridAppearanceController;
    private AppearanceController gridShowBtnAppearanceController;
    private AppearanceController listShowBtnAppearanceController;
    private AppearanceController appModPanelController;


    private Closure<Integer,DisplayData> adapterDisplayDataToRealIndexConverted;
    private Closure<Integer,Integer> adapterRealIndexToDisplayIndexConverted;
    private boolean dynamicHeaderEnabled = true;
    private Timer dynamicHeaderEnableTimer = null;
    private ApplicationData appUnderMod;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_application_drawer);

        constructor_appearance();
        constructor_adapterCategoryList();
        construct_drawerCategoryList();
        construct_categoryQuickList();

        if (isFirstRun(savedInstanceState)){
            headerAppearanceController.hideWithoutAnimation();
            rootAppearanceController.hideWithoutAnimation();
            gridShowBtnAppearanceController.hideWithoutAnimation();
        }

        if (isFirstRun(savedInstanceState)) {
            rootAppearanceController.showAndCustomize(new AppearanceController.AnimatorCustomization() {
                @Override
                public void customize(Animator animator) {
                    animator.addListener(new AppearanceControllerOld.AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            reFetchCategories();
                        }
                    });
                }
            });
        } else {
            reFetchCategories();
        }

        headerAppearanceController.show();

        if (isLandscape(R.bool.class)){
            listShowBtnAppearanceController.showWithoutAnimation();
            listShowBtnAppearanceController.hideAndCustomize(new AppearanceController.AnimatorCustomization() {
                @Override
                public void customize(Animator animator) {
                    animator.setStartDelay(800);
                }
            });
        }else{
            construct_modeSwitchBtns();
            if (!isFirstRun(savedInstanceState)){
                listShowBtnAppearanceController.hideWithoutAnimation();
                listShowBtnAppearanceController.showAndCustomize(new AppearanceController.AnimatorCustomization() {
                    @Override
                    public void customize(Animator animator) {
                        animator.setStartDelay(800);
                    }
                });
            }else{
                listShowBtnAppearanceController.showWithoutAnimation();
            }
        }
        appModPanelController.hideWithoutAnimation();
        view(R.id.ac_app_mod_close).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                appModPanelController.hide();
            }
        });
        final ArrayAdapter<RunitApp.Category> spinnerCategoryAddapter = new ArrayAdapter<RunitApp.Category>(this,R.layout.item_category_drop);
        spinnerCategoryAddapter.addAll(application().supportedCategories());
        view(R.id.ac_app_mod_category_spinner, Spinner.class).setAdapter(spinnerCategoryAddapter);
        view(R.id.ac_app_mod_category_spinner, Spinner.class).setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (appUnderMod == null) {
                    return;
                }
                RunitApp.Category category = (RunitApp.Category) view(R.id.ac_app_mod_category_spinner, Spinner.class).getAdapter().getItem(position);
                application().updateAppCategory(appUnderMod, category);
                appUnderMod = null;
                appModPanelController.hideAndCustomize(new AppearanceController.AnimatorCustomization() {
                    @Override
                    public void customize(Animator animator) {
                        animator.addListener(new AppearanceControllerOld.AnimatorListenerAdapter() {
                            @Override
                            public void onAnimationEnd(Animator animation) {
                                categoriesAdapter.clear();
                                categoriesQuickAdapter.clear();
                                categoryDataList.clear();
                                categoriesQuickAdapter.notifyDataSetChanged();
                                categoriesAdapter.notifyDataSetChanged();
                                reFetchCategories();
                            }
                        });
                    }
                });
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                appModPanelController.hide();
            }
        });

        view(R.id.drawer_touch_banner).setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                boolean captureRequired = view(R.id.ac_app_mod_panel).getVisibility() == View.VISIBLE;
                if (captureRequired) onBackPressed();
                return captureRequired;
            }
        });
    }

    private void constructor_appearance() {
        appModPanelController = animateAppearance(
                view(R.id.ac_app_mod_panel),
                ySlide(0, DisplayUtils.screenHeight(getResources())))
                .showAnimation(duration_constant(400), interpreter_overshot())
                .hideAnimation(duration_constant(200), interpreter_accelerate_decelerate())
                .hideAndGone()
                .build();

        headerAppearanceController =
                animateAppearance(
                        view(R.id.drawer_header_layout),
                        ySlide(-DisplayUtils.dpToPx(10, getResources()), -DisplayUtils.dpToPx(90, getResources())))
                        .showAnimation(duration_constant(600), interpreter_overshot())
                        .hideAnimation(duration_constant(800), interpreter_overshot())
                        .build();


        shadowAppearanceController =
                animateAppearance(
                        view(R.id.drawer_header_shadow),
                        alpha(0.8f, 0.3f))
                        .showAnimation(duration_constant(800), interpreter_overshot())
                        .hideAnimation(duration_constant(500), interpreter_overshot())
                        .build();

        gridAppearanceController =
                animateAppearance(
                        view(R.id.drawer_category_list_panel),
                        xSlide(0, DisplayUtils.screenWidth(getResources()) - DisplayUtils.dpToPx(9, getResources())))
                        .showAnimation(duration_constant(400), interpreter_decelerate(null))
                        .hideAnimation(duration_constant(300), interpreter_accelerate_decelerate())
                        .build();

        gridShowBtnAppearanceController =
                animateAppearance(
                        view(R.id.drawer_category_list_check),
                        scale(1f,0f))
                        .showAnimation(duration_constant(400), interpreter_overshot())
                        .hideAnimation(duration_constant(300), interpreter_accelerate(null))
                        .hideAndInvisible()
                        .build();

        listShowBtnAppearanceController =
                animateAppearance(
                        view(R.id.drawer_category_quick_list_check),
                        scale(1f,0f))
                        .showAnimation(duration_constant(400), interpreter_overshot())
                        .hideAnimation(duration_constant(300), interpreter_accelerate(null))
                        .hideAndInvisible()
                        .build();



        rootAppearanceController =
                animateAppearance(
                        view(R.id.drawer_root_layout),
                        xSlide(0f, DisplayUtils.screenWidth(getResources())))
                        .showAnimation(duration_constant(300))
                        .hideAnimation(duration_constant(500))
                        .build();
    }

    private void construct_categoryQuickList() {
        categoriesQuickAdapter = new GenericListViewAdapter<>(this, R.layout.item_drawer_category_quick,new GenericListViewAdapter.GenericViewHolderFactory<CategoryData>(){
            @Override
            public GenericListViewAdapter.GenericViewHolder<CategoryData> construct() {
                return new GenericListViewAdapter.GenericViewHolder<CategoryData>() {

                    View space;
                    TextView nameView;

                    @Override
                    public void discoverUI() {
                        space = _view(R.id.item_space, Space.class);
                        nameView = _view(R.id.item_name, TextView.class);
                    }

                    @Override
                    public void update(CategoryData data, int position) {
                        space.setVisibility(position == 0?View.VISIBLE:View.GONE);
                        nameView.setText(data.category.name);
                    }
                };
            }
        });

        view_list(R.id.drawer_category_quick_list).setAdapter(categoriesQuickAdapter);
        view_list(R.id.drawer_category_quick_list).setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                CategoryData category = categoriesQuickAdapter.getItem(position);
                int dataPosition = categoriesAdapter.getPosition(category);
                if (dataPosition != -1) {
                    int positionToSelect = adapterRealIndexToDisplayIndexConverted.execute(dataPosition);
                    view_list(R.id.drawer_category_list).smoothScrollToPositionFromTop(
                            positionToSelect,
                            (int) DisplayUtils.dpToPx(70,getResources()),
                            300);
                    if (dataPosition != 0){
                        disableDynamicHeaderForAWhile();
                    }
                    closeQuickListMode(false);
                }

            }
        });
    }

    private void disableDynamicHeaderForAWhile() {
        if (dynamicHeaderEnableTimer != null){
            dynamicHeaderEnableTimer.cancel();
        }
        dynamicHeaderEnabled = false;
        dynamicHeaderEnableTimer = new Timer(true);
        dynamicHeaderEnableTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                dynamicHeaderEnabled = true;
            }
        },1500);
    }


    private void construct_modeSwitchBtns() {
        view(R.id.drawer_category_quick_list_check).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                disableDynamicHeaderForAWhile();
                gridAppearanceController.hideAndCustomize(new AppearanceController.AnimatorCustomization() {
                    @Override
                    public void customize(Animator animator) {
                        animator.addListener(new AnimatorListenerAdapter() {
                            @Override
                            public void onAnimationStart(final Animator animation) {
                                view(R.id.drawer_category_list_shadow).setVisibility(View.VISIBLE);
                                runLastOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        headerAppearanceController.hideAndCustomize(new AppearanceController.AnimatorCustomization() {
                                            @Override
                                            public void customize(Animator animator) {
                                                animation.addListener(new AppearanceControllerOld.AnimatorListenerAdapter(){
                                                    @Override
                                                    public void onAnimationEnd(Animator animation) {
                                                        gridShowBtnAppearanceController.show();
                                                    }
                                                });
                                            }
                                        });
                                    }
                                });
                            }
                        });

                    }
                });
            }
        });

        view(R.id.drawer_category_list_check).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                closeQuickListMode(true);
            }
        });
    }

    private void closeQuickListMode(boolean showHeader) {
        if (showHeader) {
            headerAppearanceController.show();
        }else{
            headerAppearanceController.hide();
        }
        gridShowBtnAppearanceController.hide();
        gridAppearanceController.showAndCustomize(new AppearanceController.AnimatorCustomization() {
            @Override
            public void customize(Animator animator) {
                animator.addListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationStart(Animator animation) {
                        view(R.id.drawer_category_list_shadow).setVisibility(View.GONE);
                    }
                });
            }
        });
    }

    private void construct_drawerCategoryList() {
        final PushToListView categoryList = view(R.id.drawer_category_list, PushToListView.class);
        categoryList.setOnScrollListener(new AbsListView.OnScrollListener() {

            private int lastVisibleFirstItem = -1;

            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {}

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {

                if (!dynamicHeaderEnabled) return;
                if (totalItemCount == 0) return;

                if (firstVisibleItem == 0 || lastVisibleFirstItem == -1){
                    headerAppearanceController.show();
                    shadowAppearanceController.hide();
                    lastVisibleFirstItem = firstVisibleItem;
                } else if (firstVisibleItem-lastVisibleFirstItem > 1) {
                    headerAppearanceController.hide();
                    shadowAppearanceController.hide();
                    lastVisibleFirstItem = firstVisibleItem;
                } else if (firstVisibleItem - lastVisibleFirstItem < -1){
                    headerAppearanceController.show();
                    shadowAppearanceController.show();
                    lastVisibleFirstItem = firstVisibleItem;
                }

                DisplayData data = adapterDisplayDataToRealIndexConverted.execute(firstVisibleItem);
                view(R.id.drawer_header_text, TextView.class).setText(data.data.category.name);


            }
        });

        categoryList.setAdapter(categoriesAdapter);
        categoryList.setPushListener(new PushToActionAdapter(250) {
            @Override
            protected void cancelPushAction(float pushCoefficient, float x, float y) {
                view(R.id.drawer_root_layout).animate().translationX(0).setInterpolator(interpreter_decelerate(null).build());
                view(R.id.drawer_push_action_view, PushActionView.class).stopPush();
                headerAppearanceController.show();
            }

            @Override
            protected void applyPushAction(float x, float y) {
                ApplicationDrawerActivity.this.finish();
                view(R.id.drawer_push_action_view, PushActionView.class).stopPush();
            }

            @Override
            protected void beforePush(float x, float y) {
                view(R.id.drawer_push_action_view, PushActionView.class)
                        .startPush(x, y, "Keep pushing ...", "Close Categories");
            }

            @Override
            protected void pushInProgress(float pushCoefficient, float x, float y) {
                view(R.id.drawer_header_layout).setTranslationY(
                        -DisplayUtils.dpToPx(10, getResources())
                                - pushCoefficient * view(R.id.drawer_header_layout).getLayoutParams().height);
                view(R.id.drawer_root_layout).setTranslationX(pushCoefficient * DisplayUtils.screenWidth(getResources()));
                view(R.id.drawer_push_action_view, PushActionView.class).pushing(x, y, pushCoefficient);
            }
        });
    }

    private void constructor_adapterCategoryList() {
        categoriesAdapter = new ArrayAdapter<CategoryData>(this, R.layout.item_drawer_category){

            final int itemInRow = 3;

            private int getRealCount(){
                return super.getCount();
            }
            @Override
            public int getCount() {

                if (adapterDisplayDataToRealIndexConverted ==null){
                    adapterDisplayDataToRealIndexConverted = new Closure<Integer, DisplayData>() {
                        @Override
                        public DisplayData execute(Integer arg) {
                            return getDataToDisplay(arg);
                        }
                    };

                     adapterRealIndexToDisplayIndexConverted = new Closure<Integer, Integer>() {
                         @Override
                         public Integer execute(Integer requestedRealIndex) {

                             int realCount = getRealCount();
                             if (requestedRealIndex >= realCount) throw new IndexOutOfBoundsException("Shouldn`t be more then = "+realCount+", but was = "+requestedRealIndex);

                             int answer = 0;
                             //TODO: move to resources
                             for (int i = 0; i <= requestedRealIndex; i++){
                                 //for header
                                 answer ++;
                                 CategoryData data = getItem(i);
                                 if (i!=requestedRealIndex)
                                    answer += data.apps.size()/itemInRow + ((data.apps.size() % itemInRow == 0) ? 0 : 1);
                             }
                             return answer;
                         }
                    };
                }
                int realCount = getRealCount();
                int answer = 0;
                //TODO: move to resources
                for (int i = 0; i<realCount; i++){
                    //for header
                    answer ++;
                    CategoryData data = getItem(i);
                    answer += data.apps.size()/itemInRow + ((data.apps.size() % itemInRow == 0) ? 0 : 1);
                }
                return answer;
            }

            private DisplayData getDataToDisplay(int requestedPosition) {
                int realCount = super.getCount();
                int elemIndex = -1;
                for (int i = 0; i<realCount; i++) {
                    //for header
                    elemIndex++;

                    CategoryData data = getItem(i);
                    if (elemIndex == requestedPosition){
                        return new DisplayData(data, -1);
                    }

                    //over rows
                    for (int j=0; j < data.apps.size(); j+=itemInRow){
                        elemIndex+=1;
                        if (elemIndex == requestedPosition){
                            return new DisplayData(data, j);
                        }
                    }
                }
                throw new IndexOutOfBoundsException("Requested elem = "+requestedPosition+" but last elem is "+elemIndex);
            }

            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                final CategoryItemHolder holder;
                if (convertView == null){
                    LayoutInflater inflater = (LayoutInflater) getContext()
                            .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                    convertView = inflater.inflate(R.layout.item_drawer_category, null);
                    holder = new CategoryItemHolder(convertView, itemInRow);
                    for (int i = 0; i< itemInRow; i++){
                        View viewToUse = inflater.inflate(R.layout.item_category_app, holder.categoryAppsGrid, false);
                        viewToUse.setVisibility(View.INVISIBLE);
                        holder.categoryAppsGrid.addView(viewToUse);
                    }
                    convertView.setTag(holder);
                } else {
                    holder = (CategoryItemHolder) convertView.getTag();
                }
                DisplayData displayData = getDataToDisplay(position);
                holder.updateData(position, displayData);
                return convertView;
            }


        };
        categoriesAdapter.getCount();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    private void closeActivity(final boolean completelly) {
        disableDynamicHeaderForAWhile();
        headerAppearanceController.hide();
        rootAppearanceController.hideAndCustomize(new AppearanceController.AnimatorCustomization() {
            @Override
            public void customize(Animator animator) {
                animator.addListener(new AppearanceControllerOld.AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        if (completelly) super_onBackPress();
                    }
                });

            }
        });
    }

    @Override
    public void onBackPressed() {
        if (view(R.id.ac_app_mod_panel).getVisibility() == View.VISIBLE){
            appModPanelController.hide();
            return;
        }
        if (view(R.id.drawer_category_list_check).getVisibility()==View.VISIBLE){
            closeQuickListMode(true);
        }else {
            closeActivity(true);
        }
    }

    private void super_onBackPress(){
        super.onBackPressed();
    }

    private void reFetchCategories() {
        application().fetchApplicationCategories(new RunitApp.OnAppCategoriesCallback() {
            @Override
            public void fetched(final List<RunitApp.Category> categories, boolean syncInProgress) {
                //view(R.id.drawer_synch_panel).setVisibility(syncInProgress ? View.VISIBLE : View.GONE);
                mergeCategoriesWithExisting(categories);
                fetchApplicationsPerCategory();
            }

            private void mergeCategoriesWithExisting(final List<RunitApp.Category> categories) {
                synchronized (categoryDataList) {
                    //Remove all which not exists any more
                    Lists.iterateAndRemove(categoryDataList, new Closure<Iterator<CategoryData>, Boolean>() {
                        @Override
                        public Boolean execute(Iterator<CategoryData> it) {
                            if (!categories.contains(it.next().category)) {
                                it.remove();
                            }
                            return false;
                        }
                    });

                    //Add all that not present
                    for (RunitApp.Category category : categories) {
                        if (!Lists.in(categoryDataList, category, new Closure<Pair<CategoryData, RunitApp.Category>, Boolean>() {
                            @Override
                            public Boolean execute(Pair<CategoryData, RunitApp.Category> arg) {
                                return arg.first.category.equals(arg.second);
                            }
                        })) {
                            categoryDataList.add(new CategoryData(category));
                        }
                    }
                }
            }

            @Override
            public void noData() {
                view(R.id.ac_synch_panel).setVisibility(View.VISIBLE);
                categoryDataList.clear();
            }
        });
    }

    private void fetchApplicationsPerCategory() {
        synchronized (categoryDataList) {
            for (final CategoryData categoryData : categoryDataList) {
                if (categoryData.isFetchRequired()) {
                    application().fetchApplicationByCategory(categoryData.category,new RunitApp.OnAppSearchCallback() {
                        @Override
                        public void found(String searchQuery, List<RunitApp.AppSearchResult> searchResultList) {
                            categoryData.setApplications(searchResultList);
                            showCategoryIfNotAlready(categoryData);
                            fetchApplicationsPerCategory();
                        }
                    });
                    return;
                }
            }
        }
    }

    private void showCategoryIfNotAlready(CategoryData categoryData) {
        if (1 == categoriesAdapter.getPosition(categoryData)){
            return;
        }
        //TODO: add in alphabetic order
        categoriesAdapter.add(categoryData);
        categoriesAdapter.notifyDataSetChanged();

        if (1 == categoriesQuickAdapter.getPosition(categoryData)){
            return;
        }
        //TODO: add in alphabetic order
        categoriesQuickAdapter.add(categoryData);
        categoriesQuickAdapter.notifyDataSetChanged();


    }

    private static class CategoryData{

        private final RunitApp.Category category;
        private final List<RunitApp.AppSearchResult> apps = new ArrayList<>();
        private boolean fetchRequired = true;

        private CategoryData(RunitApp.Category category) {
            this.category = category;
        }

        public boolean isFetchRequired() {
            return fetchRequired;
        }

        public void setApplications(List<RunitApp.AppSearchResult> applications) {
            apps.clear();
            apps.addAll(applications);
            fetchRequired = false;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            CategoryData data = (CategoryData) o;

            if (!category.equals(data.category)) return false;

            return true;
        }

        @Override
        public int hashCode() {
            return category.hashCode();
        }
    }

    class DisplayData{

        private final CategoryData data;
        private final int appsStartIndex;

        DisplayData(CategoryData data, int appsStartIndex) {
            this.data = data;
            this.appsStartIndex = appsStartIndex;
        }
    }

    class CategoryItemHolder {

        final TextView categoryCaptionLabel;
        final ViewGroup categoryAppsGrid;
        final View header;
        private final int itemInRow;

        @Deprecated
        CategoryData data;
        DisplayData displayData;


        CategoryItemHolder(View convertView, int itemInRow) {
            this.itemInRow = itemInRow;
            categoryCaptionLabel = (TextView) convertView.findViewById(R.id.item_name);
            categoryAppsGrid = (ViewGroup) convertView.findViewById(R.id.item_container);
            header = convertView.findViewById(R.id.item_header);
        }

        public void updateData(int position, DisplayData displayData){
            if (this.displayData == displayData) return;
            this.data = displayData.data;
            this.displayData = displayData;


            if (displayData.appsStartIndex == -1){
                //label only required
                categoryCaptionLabel.setText(displayData.data.category.name);
                categoryAppsGrid.setVisibility(View.GONE);
                header.setVisibility(position==0?View.INVISIBLE:View.VISIBLE);
            } else {
                //request application row
                Views.findChild(categoryAppsGrid,new Closure<Pair<Integer, View>, Boolean>() {
                    @Override
                    public Boolean execute(Pair<Integer, View> arg) {
                        arg.second.setVisibility(View.INVISIBLE);
                        return false;
                    }
                });
                fetchIconAndAddItem(displayData, displayData.appsStartIndex, displayData.appsStartIndex + itemInRow - 1);
                categoryAppsGrid.setVisibility(View.VISIBLE);
                header.setVisibility(View.GONE);
            }
        }

        private void fetchIconAndAddItem(final DisplayData controlData, final int curAppIndex, final int lastItemIndex) {
            if ((curAppIndex >= controlData.data.apps.size()) ||
                    (curAppIndex > lastItemIndex))
                return;

            application().loadApplicationIcon(controlData.data.apps.get(curAppIndex).applicationData,new RunitApp.OnLoadApplicationIconCallback() {
                @Override
                public void load(ApplicationData applicationData, Drawable drawable) {
                    if (controlData != CategoryItemHolder.this.displayData) return;
                    addAppView(applicationData, drawable);
                    fetchIconAndAddItem(controlData, (curAppIndex+1), lastItemIndex);
                }

            });

        }

        private void addAppView(final ApplicationData applicationData, Drawable drawable) {
            View viewToUse = Views.findChild(categoryAppsGrid,new Closure<Pair<Integer, View>, Boolean>() {
                @Override
                public Boolean execute(Pair<Integer, View> arg) {
                    return arg.second.getVisibility() == View.INVISIBLE;
                }
            });
            if (viewToUse == null){
                throw new IllegalStateException("It`s now eager ignited");
                        /*
                        LayoutInflater inflater = (LayoutInflater) getContext()
                                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                        viewToUse = inflater.inflate(R.layout.item_category_app, categoryAppsGrid, false);
                        categoryAppsGrid.addView(viewToUse);
                        */
            }
            ((TextView) viewToUse.findViewById(R.id.search_item_text)).setText(applicationData.name);
            ((ImageView) viewToUse.findViewById(R.id.search_item_image)).setImageDrawable(drawable);
            viewToUse.setVisibility(View.VISIBLE);
            viewToUse.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    application().launchApplication(applicationData);
                }
            });
            viewToUse.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    view(R.id.ac_app_mod_name,TextView.class).setText(applicationData.name);
                    appUnderMod = null;
                    Spinner spinner = view(R.id.ac_app_mod_category_spinner, Spinner.class);
                    int position = ((ArrayAdapter<RunitApp.Category>)spinner.getAdapter()).getPosition(displayData.data.category);
                    if (position < 0){
                        position = 0;
                    }
                    spinner.setSelection(position,false);
                    application().loadApplicationIcon(applicationData, new RunitApp.OnLoadApplicationIconCallback() {
                        @Override
                        public void load(ApplicationData applicationData, Drawable drawable) {
                            view(R.id.ac_app_mod_icon, ImageView.class).setImageDrawable(drawable);
                        }
                    });
                    appModPanelController.showAndCustomize(new AppearanceController.AnimatorCustomization() {
                        @Override
                        public void customize(Animator animator) {
                            animator.addListener(new AppearanceControllerOld.AnimatorListenerAdapter(){
                                @Override
                                public void onAnimationEnd(Animator animation) {
                                    appUnderMod = applicationData;
                                }
                            });
                        }
                    });
                    return true;
                }
            });
        }
    }
}
