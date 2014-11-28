package org.monroe.team.runit.app.service;


import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.drawable.Drawable;

import org.monroe.team.runit.app.uc.entity.ApplicationData;

import java.util.ArrayList;
import java.util.List;

public class ApplicationRegistry {

    private List<ApplicationData> launchApplicationDatas;
    private final PackageManager packageManager;
    private List<ResolveInfo> applicationPackageInfoList;

    public ApplicationRegistry(PackageManager packageManager) {
        this.packageManager = packageManager;
    }

    public synchronized List<ApplicationData> getApplicationsWithLauncherActivity() {
        if (launchApplicationDatas == null) {
            launchApplicationDatas = getApplicationsWithLauncherActivityImpl();
        }
        return launchApplicationDatas;
    }

    public synchronized ResolveInfo getLauncherActivityResolverInfoByPackageName(ApplicationData data) {
        for (ResolveInfo resolveInfo : getLauncherApplicationPackageInfoList()) {
            if (data.packageName.equals(resolveInfo.activityInfo.packageName) &&
                    data.name.equals(resolveInfo.loadLabel(packageManager))){
                return resolveInfo;
            }
        }
        return null;
    }

    private List<ApplicationData> getApplicationsWithLauncherActivityImpl() {
        List<ApplicationData> answer = new ArrayList<ApplicationData>();
        for (ResolveInfo resolveInfo : getLauncherApplicationPackageInfoList()) {
            String name = resolveInfo.loadLabel(packageManager).toString();
            answer.add(new ApplicationData(name, resolveInfo.activityInfo.packageName));
        }
        return answer;
    }

    private synchronized List<ResolveInfo> getLauncherApplicationPackageInfoList() {
        if (applicationPackageInfoList == null) {
            refreshApplicationsWithLauncherActivityList();
        }
        return applicationPackageInfoList;
    }

    public synchronized void refreshApplicationsWithLauncherActivityList() {
        final Intent mainIntent = new Intent(Intent.ACTION_MAIN, null);
        mainIntent.addCategory(Intent.CATEGORY_LAUNCHER);
        applicationPackageInfoList = packageManager.queryIntentActivities(mainIntent, 0);
    }

    public Drawable loadIconFor(ApplicationData request) {
        return getLauncherActivityResolverInfoByPackageName(request).loadIcon(packageManager);
    }

}
