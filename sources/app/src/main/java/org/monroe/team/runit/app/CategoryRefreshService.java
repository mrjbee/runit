package org.monroe.team.runit.app;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import org.monroe.team.android.box.db.DAOSupport;
import org.monroe.team.android.box.db.TransactionManager;
import org.monroe.team.android.box.manager.NetworkManager;
import org.monroe.team.runit.app.android.RunitApp;
import org.monroe.team.runit.app.service.ApplicationRegistry;
import org.monroe.team.runit.app.uc.RefreshApplicationCategory;
import org.monroe.team.runit.app.uc.entity.ApplicationData;

import java.util.List;

public class CategoryRefreshService extends Service {

    public Thread refreshThread;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
       if (refreshThread == null){
           refreshThread = new Thread(){
               @Override
               public void run() {
                   try {
                       CategoryRefreshService.this.refresh();
                   }finally {
                       CategoryRefreshService.this.stopSelf();
                   }
               }
           };
           refreshThread.start();
       }
       super.onStartCommand(intent, flags, startId);
       return Service.START_NOT_STICKY;
    }

    private void refresh() {
        RunitApp app = (RunitApp) getApplication();
        if (!app.model().usingService(NetworkManager.class).isUsingWifi()) return;
        List<ApplicationData> appsList = app.model().usingService(ApplicationRegistry.class).getApplicationsWithLauncherActivity();
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
                   try {
                       Thread.sleep(1000);
                       continue;
                   } catch (InterruptedException e) {
                       return;
                   }
               case FAILED_BLOCKED:
                   //enough fot today :)
                   return;
           }


        }
    }

    @Override
    public IBinder onBind(Intent intent) {return null;}
}
