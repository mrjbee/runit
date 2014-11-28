package org.monroe.team.runit.app.android;

import android.graphics.drawable.Drawable;
import android.util.LruCache;

import org.monroe.team.android.box.manager.BackgroundTaskManager;
import org.monroe.team.android.box.manager.Model;
import org.monroe.team.android.box.support.ApplicationSupport;
import org.monroe.team.runit.app.RunItModel;
import org.monroe.team.runit.app.service.ApplicationRegistry;
import org.monroe.team.runit.app.uc.FindAppsByText;
import org.monroe.team.runit.app.uc.FindMostUsedApplications;
import org.monroe.team.runit.app.uc.FindRecentApplications;
import org.monroe.team.runit.app.uc.LaunchApplication;
import org.monroe.team.runit.app.uc.LoadApplicationImage;
import org.monroe.team.runit.app.uc.entity.ApplicationData;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

public class RunitApp extends ApplicationSupport<RunItModel> {

    private BackgroundTaskManager.BackgroundTask<FindAppsByText.SearchResult> searchAppBackgroundTask;
    private LruCache<String, Drawable> launcherIconCache = new LruCache<String, Drawable>(20);
    private BackgroundTaskManager.BackgroundTask<List<ApplicationData>> mostResentAppFetchTask;
    private BackgroundTaskManager.BackgroundTask<List<ApplicationData>> mostUsedAppFetchTask;

    @Override
    public void onCreate() {
        super.onCreate();
        model().usingService(BackgroundTaskManager.class).execute(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                RunitApp.this.model().usingService(ApplicationRegistry.class).refreshApplicationsWithLauncherActivityList();
                return null;
            }
        });
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

    public BackgroundTaskManager.BackgroundTask<Drawable> loadApplicationIcon(final ApplicationData data, final OnLoadApplicationIconCallback callback){
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
