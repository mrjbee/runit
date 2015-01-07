package org.monroe.team.runit.app;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import org.monroe.team.android.box.Closure;
import org.monroe.team.android.box.Lists;
import org.monroe.team.android.box.Views;
import org.monroe.team.android.box.support.ActivitySupport;
import org.monroe.team.android.box.ui.PushToActionAdapter;
import org.monroe.team.android.box.ui.PushToListView;
import org.monroe.team.runit.app.android.RunitApp;
import org.monroe.team.runit.app.uc.entity.ApplicationData;
import org.monroe.team.runit.app.views.PushActionView;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import static org.monroe.team.android.box.ui.animation.apperrance.AppearanceControllerBuilder.interpreter_decelerate;


public class ApplicationDrawerActivity extends ActivitySupport<RunitApp> {

    private List<CategoryData> categoryDataList = new ArrayList<>();
    private ArrayAdapter<CategoryData> categoriesAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_application_drawer);
        constructor_adapterCategoryList();
        construct_drawerCategoryList();
    }

    private void construct_drawerCategoryList() {
        view(R.id.drawer_category_list, PushToListView.class).setAdapter(categoriesAdapter);
        view(R.id.drawer_category_list, PushToListView.class).setPushListener(new PushToActionAdapter(150) {
            @Override
            protected void cancelPushAction(float pushCoefficient, float x, float y) {
                view(R.id.drawer_root_layout).animate().alpha(1f).setInterpolator(interpreter_decelerate(null).build());
                view(R.id.drawer_push_action_view, PushActionView.class).stopPush();
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
                float alpha = (1 - pushCoefficient * 0.5f);
                view(R.id.drawer_root_layout).setAlpha(alpha);
                view(R.id.drawer_push_action_view, PushActionView.class).pushing(x, y, pushCoefficient);
            }
        });
    }

    private void constructor_adapterCategoryList() {
        categoriesAdapter = new ArrayAdapter<CategoryData>(this, R.layout.item_drawer_category){

            final int itemInRow = 3;

            @Override
            public int getCount() {
                int realCount = super.getCount();
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
                    holder = new CategoryItemHolder(convertView);
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

                @Deprecated
                CategoryData data;
                DisplayData displayData;

                CategoryItemHolder(View convertView) {
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
                        header.setVisibility(View.VISIBLE);
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

                private void addAppView(ApplicationData applicationData, Drawable drawable) {
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
                }
            }
        };

    }

    @Override
    protected void onResume() {
        super.onResume();
        reFetchCategories();
    }

    private void reFetchCategories() {
        application().fetchApplicationCategories(new RunitApp.OnAppCategoriesCallback() {
            @Override
            public void fetched(final List<RunitApp.Category> categories, boolean syncInProgress) {
                view(R.id.drawer_synch_panel).setVisibility(syncInProgress ? View.VISIBLE : View.GONE);
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

}
