package org.monroe.team.runit.app.android;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.util.LruCache;
import android.util.Pair;
import android.view.View;
import android.widget.ListView;

import org.monroe.team.android.box.manager.BackgroundTaskManager;
import org.monroe.team.android.box.manager.Model;
import org.monroe.team.android.box.manager.SettingManager;
import org.monroe.team.android.box.support.ApplicationSupport;
import org.monroe.team.runit.app.ApplicationRefreshService;
import org.monroe.team.runit.app.RunItModel;
import org.monroe.team.runit.app.service.ApplicationRegistry;
import org.monroe.team.runit.app.service.CategoryNameResolver;
import org.monroe.team.runit.app.service.PlayMarketDetailsProvider;
import org.monroe.team.runit.app.uc.FindAppsByCategory;
import org.monroe.team.runit.app.uc.FindAppsByText;
import org.monroe.team.runit.app.uc.FindMostUsedApplications;
import org.monroe.team.runit.app.uc.FindRecentApplications;
import org.monroe.team.runit.app.uc.GetApplicationCategories;
import org.monroe.team.runit.app.uc.LaunchApplication;
import org.monroe.team.runit.app.uc.LoadApplicationCategory;
import org.monroe.team.runit.app.uc.LoadApplicationImage;
import org.monroe.team.runit.app.uc.UpdateApplicationCategory;
import org.monroe.team.runit.app.uc.entity.ApplicationData;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.Callable;

public class RunitApp extends ApplicationSupport<RunItModel> {

    private BackgroundTaskManager.BackgroundTask<FindAppsByText.SearchResult> searchAppBackgroundTask;
    private BackgroundTaskManager.BackgroundTask<FindAppsByText.SearchResult> appCategoryBackgroundTask;
    private LruCache<String, Drawable> launcherIconCache = new LruCache<String, Drawable>(20);
    private BackgroundTaskManager.BackgroundTask<List<ApplicationData>> mostResentAppFetchTask;
    private BackgroundTaskManager.BackgroundTask<List<ApplicationData>> mostUsedAppFetchTask;

    @Override
    public void onCreate() {
        super.onCreate();
        requestRefreshApps();
        startService(new Intent(this, ApplicationRefreshService.class));
    }

    @Override
    protected RunItModel createModel() {
        return new RunItModel("RunIt",getApplicationContext());
    }

    public synchronized void searchApplicationByName(final String searchQuery, final OnAppSearchCallback callback){
        cancelSearchApplicationByName();
        searchAppBackgroundTask = model().execute(FindAppsByText.class,new FindAppsByText.SearchRequest(searchQuery),new Model.BackgroundResultCallback<FindAppsByText.SearchResult>() {
            @Override
            public void onResult(FindAppsByText.SearchResult response) {
                List<AppSearchResult> appSearchResultList = new ArrayList<AppSearchResult>(response.applicationDataList.size());
                for (ApplicationData applicationData : response.applicationDataList) {
                        Integer highlightStartIndex = null,highlightEndIndex = null;
                        if (!searchQuery.isEmpty()){
                            highlightStartIndex = applicationData.name.toLowerCase().indexOf(searchQuery.toLowerCase());
                            highlightEndIndex = highlightStartIndex + searchQuery.length();
                        }
                        appSearchResultList.add(new AppSearchResult(applicationData,highlightStartIndex,highlightEndIndex));
                }
                callback.found(searchQuery, appSearchResultList);
            }

            @Override
            public void onDone() {
                callback.done();
            }
        });
    }

    public void fetchApplicationByCategory(Category item, final OnAppSearchCallback callback) {
        if (appCategoryBackgroundTask != null) appCategoryBackgroundTask.cancel();
        appCategoryBackgroundTask = model().execute(FindAppsByCategory.class, item.categoryId, new Model.BackgroundResultCallback<FindAppsByText.SearchResult>() {
            @Override
            public void onResult(FindAppsByText.SearchResult response) {
                List<AppSearchResult> appSearchResultList = new ArrayList<AppSearchResult>(response.applicationDataList.size());
                for (ApplicationData applicationData : response.applicationDataList) {
                    appSearchResultList.add(new AppSearchResult(applicationData,0,0));
                }
                callback.found(null, appSearchResultList);
            }

            @Override
            public void onDone() {
                callback.done();
            }
        });
    }

    public BackgroundTaskManager.BackgroundTask<?> loadApplicationIcon(final ApplicationData data, final OnLoadApplicationIconCallback callback){
        Drawable drawable = launcherIconCache.get(data.getUniqueName());
        if (drawable != null){
            callback.load(data,drawable);
            return null;
        }
        BackgroundTaskManager.BackgroundTask<Drawable> loadTask = model().execute(LoadApplicationImage.class, data, new Model.BackgroundResultCallback<Drawable>() {
            @Override
            public void onResult(Drawable response) {
                launcherIconCache.put(data.getUniqueName(),response);
                callback.load(data, response);
            }
        });
        return loadTask;
    }


    public BackgroundTaskManager.BackgroundTask<?> loadApplicationCategory(final ApplicationData data, final OnLoadCategoryCallback callback) {

        BackgroundTaskManager.BackgroundTask<Long> loadTask = model().execute(LoadApplicationCategory.class, data, new Model.BackgroundResultCallback<Long>() {
            @Override
            public void onResult(Long response) {
                callback.load(data,new Category(
                        model().usingService(CategoryNameResolver.class)
                                .categoryNameById(response),
                                0,response));
            }
        });
        return loadTask;
    }




    public void launchApplication(ApplicationData data){
        model().execute(LaunchApplication.class,data,new Model.BackgroundResultCallback<Void>() {
            @Override
            public void onResult(Void response) {

            }
        });
    }

    public void fetchMostRecentApplication(final OnApplicationFetchedCallback callback){
        cancelMostResentApplicationFetch();
        mostResentAppFetchTask = model().execute(FindRecentApplications.class, null, new Model.BackgroundResultCallback<List<ApplicationData>>() {
            @Override
            public void onResult(List<ApplicationData> response) {
                callback.fetched(response);
            }
        });
    }

    public void fetchMostUsedApplication(final OnApplicationFetchedCallback callback){
        cancelMostUsedApplicationFetch();
        mostUsedAppFetchTask = model().execute(FindMostUsedApplications.class, null, new Model.BackgroundResultCallback<List<ApplicationData>>() {
            @Override
            public void onResult(List<ApplicationData> response) {
                callback.fetched(response);
            }
        });
    }


    public void cancelMostUsedApplicationFetch() {
        if (mostUsedAppFetchTask != null){
            mostUsedAppFetchTask.cancel();
        }
    }

    public void cancelMostResentApplicationFetch() {
        if (mostResentAppFetchTask != null){
            mostResentAppFetchTask.cancel();
        }
    }


    public synchronized void cancelSearchApplicationByName(){
        if (searchAppBackgroundTask != null){
            searchAppBackgroundTask.cancel();
        }
    }

    public void fetchApplicationCategories(final OnAppCategoriesCallback categoriesCallback) {
        model().execute(GetApplicationCategories.class,null, new Model.BackgroundResultCallback<List<GetApplicationCategories.ApplicationCategory>>() {
            @Override
            public void onResult(List<GetApplicationCategories.ApplicationCategory> response) {
                if (response.isEmpty() || (response.size() ==1 && response.get(0).categoryId == null)){
                    categoriesCallback.noData();
                }
                List<Category> categoryList = new ArrayList<Category>();
                for (GetApplicationCategories.ApplicationCategory applicationCategory : response) {
                    categoryList.add(new Category(
                            model().usingService(CategoryNameResolver.class)
                                   .categoryNameById(applicationCategory.categoryId),
                            applicationCategory.appsCount, applicationCategory.categoryId));
                }
                categoriesCallback.fetched(categoryList,
                        model().usingService(SettingManager.class).get(RunItModel.SETTING_SYNC_IN_PROGRESS));
            }
        });
    }

    public List<Category> supportedCategories() {
        List<Category> categories = new ArrayList<>();
        for (PlayMarketDetailsProvider.PlayMarketCategory category : PlayMarketDetailsProvider.PlayMarketCategory.values()) {
            if (category != PlayMarketDetailsProvider.PlayMarketCategory.NONE &&
                    category != PlayMarketDetailsProvider.PlayMarketCategory.UNDEFINED){
                categories.add(new Category(
                        model().usingService(CategoryNameResolver.class)
                                .categoryNameById((long) category.ordinal()),
                        0, (long) category.ordinal()));
            }
        }
        return categories;
    }

    public void updateAppCategory(ApplicationData appUnderMod, Category category) {
        model().execute(UpdateApplicationCategory.class, new UpdateApplicationCategory.Request(category.categoryId, appUnderMod));
    }

    public void requestRefreshApps() {
        model().usingService(BackgroundTaskManager.class).execute(new Callable<Object>() {
            @Override
            public Object call() throws Exception {
                model().usingService(ApplicationRegistry.class).refreshApplicationsWithLauncherActivityList();
                return null;
            }
        });
    }


    public interface OnAppCategoriesCallback {
        public void fetched(List<Category> fetchData, boolean syncInProgress);
        public void noData();
    }


    public abstract static class OnApplicationFetchedCallback {
        public abstract void fetched(List<ApplicationData> applicationDataList);
    }

    public abstract static class OnAppSearchCallback {
        public abstract void found(String searchQuery, List<AppSearchResult> searchResultList);
        public void done(){}
    }

    public abstract static class OnLoadApplicationIconCallback {
        public abstract void load(ApplicationData applicationData, Drawable drawable);
    }

    public abstract static class OnLoadApplicationsIconCallback {
        public abstract void load(List<Pair<ApplicationData,Drawable>> iconsPerData);
    }

    public abstract static class OnLoadCategoryCallback {
        public abstract void load(ApplicationData applicationData, Category category);
    }


    public static class Category{

        public final String name;
        public final long appsCount;
        private final Long categoryId;

        public Category(String name, long appsCount, Long categoryId) {
            this.name = name;
            this.appsCount = appsCount;
            this.categoryId = categoryId;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            Category category = (Category) o;

            if (categoryId != null ? !categoryId.equals(category.categoryId) : category.categoryId != null)
                return false;

            return true;
        }

        @Override
        public int hashCode() {
            return categoryId != null ? categoryId.hashCode() : 0;
        }

        @Override
        public String toString() {
            return name;
        }
    }

    public static class AppSearchResult {

        public final ApplicationData applicationData;
        public final Integer selectionStartIndex;
        public final Integer selectionEndIndex;

        public AppSearchResult(ApplicationData applicationData, Integer selectionStartIndex, Integer selectionEndIndex) {
            this.applicationData = applicationData;
            this.selectionStartIndex = selectionStartIndex;
            this.selectionEndIndex = selectionEndIndex;
        }
    }


}
