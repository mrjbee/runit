package org.monroe.team.runit.app;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.TextView;

import org.monroe.team.android.box.Closure;
import org.monroe.team.android.box.Lists;
import org.monroe.team.android.box.support.ActivitySupport;
import org.monroe.team.android.box.ui.PushToActionAdapter;
import org.monroe.team.android.box.ui.PushToListView;
import org.monroe.team.android.box.utils.DisplayUtils;
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

            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                final CategoryItemHolder holder;
                if (convertView == null){
                    LayoutInflater inflater = (LayoutInflater) getContext()
                            .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                    convertView = inflater.inflate(R.layout.item_drawer_category, null);
                    holder = new CategoryItemHolder(
                            (TextView) convertView.findViewById(R.id.name),
                            (GridLayout) convertView.findViewById(R.id.grid));
                    convertView.setTag(holder);
                } else {
                    holder = (CategoryItemHolder) convertView.getTag();
                }
                CategoryData data = categoriesAdapter.getItem(position);
                holder.updateData(data);

                return convertView;
            }

            class CategoryItemHolder {

                final TextView categoryCaptionLabel;
                final GridLayout categoryAppsGrid;
                CategoryData data;

                CategoryItemHolder(TextView categoryCaptionLabel, GridLayout categoryAppsGrid) {
                    this.categoryCaptionLabel = categoryCaptionLabel;
                    this.categoryAppsGrid = categoryAppsGrid;
                }

                public void updateData(CategoryData data){
                    if (this.data == data) return;

                    this.data = data;
                    categoryAppsGrid.removeAllViews();
                    categoryCaptionLabel.setText(data.category.name);
                    int rowCount = data.apps.size()/3 + ((data.apps.size() % 3 == 0) ? 0 : 1);
                    categoryAppsGrid.getLayoutParams().height = (int) (rowCount * DisplayUtils.dpToPx(100,getResources()));
                    categoryAppsGrid.requestLayout();
                    Iterator<RunitApp.AppSearchResult> appsIterator = data.apps.iterator();
                    fetchIconAndAddItem(appsIterator, data);

                }

                private void fetchIconAndAddItem(final Iterator<RunitApp.AppSearchResult> appsIterator, final CategoryData controlData) {
                    application().loadApplicationIcon(appsIterator.next().applicationData,new RunitApp.OnLoadApplicationIconCallback() {
                        @Override
                        public void load(ApplicationData applicationData, Drawable drawable) {

                            if (controlData != CategoryItemHolder.this.data) return;

                            LayoutInflater inflater = (LayoutInflater) getContext()
                                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                            View view = inflater.inflate(R.layout.item_category_app,categoryAppsGrid, false);
                            ((TextView) view.findViewById(R.id.search_item_text)).setText(applicationData.name);
                            ((ImageView) view.findViewById(R.id.search_item_image)).setImageDrawable(drawable);
                            categoryAppsGrid.addView(view);

                            if(appsIterator.hasNext()){
                                fetchIconAndAddItem(appsIterator,controlData);
                            }
                        }
                    });
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
                            if (!categories.contains(it.next())) {
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
    }

}
