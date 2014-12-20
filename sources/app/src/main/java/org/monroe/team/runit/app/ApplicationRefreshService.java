package org.monroe.team.runit.app;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import org.monroe.team.android.box.manager.Model;
import org.monroe.team.android.box.manager.NetworkManager;
import org.monroe.team.android.box.manager.SettingManager;
import org.monroe.team.runit.app.android.RunitApp;
import org.monroe.team.runit.app.service.ApplicationRegistry;
import org.monroe.team.runit.app.uc.RefreshApplicationCategory;
import org.monroe.team.runit.app.uc.RemoveUninstalledApplications;
import org.monroe.team.runit.app.uc.entity.ApplicationData;

import java.util.List;

public class ApplicationRefreshService extends Service {

    public Thread categoryRefreshThread;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        startCategoryRefreshThread();
        RunitApp app = (RunitApp) getApplication();
        app.model().execute(RemoveUninstalledApplications.class, null, new Model.BackgroundResultCallback<Void>() {
            @Override
            public void onResult(Void response) {}
        });
        super.onStartCommand(intent, flags, startId);
        return Service.START_NOT_STICKY;
    }

    private synchronized void startCategoryRefreshThread() {
        if (categoryRefreshThread == null){
            categoryRefreshThread = new Thread(){
                @Override
                public void run() {
                    try {
                        ApplicationRefreshService.this.refresh();
                    }finally {
                        ApplicationRefreshService.this.stopCategoryRefreshThread();
                    }
                }
            };
            categoryRefreshThread.start();
        }
    }

    private synchronized void stopCategoryRefreshThread() {
        categoryRefreshThread = null;
    }

    private void refresh() {
        RunitApp app = (RunitApp) getApplication();
        List<ApplicationData> appsList = app.model().usingService(ApplicationRegistry.class).getApplicationsWithLauncherActivity();
        if (!app.model().usingService(NetworkManager.class).isUsingWifi()) return;
        boolean syncDone = true;
        int successTimes = 0;
        for (ApplicationData applicationData : appsList) {
           if (!app.model().usingService(NetworkManager.class).isUsingWifi()) continue;
           RefreshApplicationCategory.RefreshStatus status = app.model().execute(RefreshApplicationCategory.class,applicationData);
           switch (status){
               case NO_ACTION_REQUIRED:
                   continue;
               case UPDATED:
                   successTimes++;
                   if (successTimes < 5){
                        continue;
                   }
               case FAILED_NO_CONNECTION:
                   syncDone = false;
                   ((RunitApp)getApplication()).model()
                           .usingService(SettingManager.class).set(RunItModel.SETTING_SYNC_IN_PROGRESS, true);
                   try {
                       Thread.sleep(1000);
                       continue;
                   } catch (InterruptedException e) {
                       return;
                   }
               case FAILED_BLOCKED:
                   //enough fot this time :)
                   syncDone = false;
                   ((RunitApp)getApplication()).model()
                           .usingService(SettingManager.class).set(RunItModel.SETTING_SYNC_IN_PROGRESS, true);
                   return;
           }
        }
        ((RunitApp)getApplication()).model()
                .usingService(SettingManager.class).set(RunItModel.SETTING_SYNC_IN_PROGRESS, !syncDone);
    }

    @Override
    public IBinder onBind(Intent intent) {return null;}
}
