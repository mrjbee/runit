package org.monroe.team.runit.app.service;


import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.drawable.Drawable;

import org.monroe.team.android.box.Closure;
import org.monroe.team.android.box.Lists;
import org.monroe.team.runit.app.RunItModel;
import org.monroe.team.runit.app.uc.entity.ApplicationData;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class ApplicationRegistry {

    private final PackageManager packageManager;
    private List<ResolveInfo> applicationPackageInfoList;

    public ApplicationRegistry(PackageManager packageManager) {
        this.packageManager = packageManager;
    }

    public synchronized List<ApplicationData> getApplicationsWithLauncherActivity() {
        return getApplicationsWithLauncherActivityImpl();
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
            if (!RunItModel.class.getPackage().getName().equals(resolveInfo.activityInfo.packageName)){
                String name = resolveInfo.loadLabel(packageManager).toString();
                answer.add(new ApplicationData(name, resolveInfo.activityInfo.packageName));
            }
        }
        return answer;
    }

    private synchronized List<ResolveInfo> getLauncherApplicationPackageInfoList() {
        if (applicationPackageInfoList == null) {
            refreshApplicationsWithLauncherActivityList();
        }
        return applicationPackageInfoList;
    }

    public void refreshApplicationsWithLauncherActivityList() {
        final Intent mainIntent = new Intent(Intent.ACTION_MAIN, null);
        mainIntent.addCategory(Intent.CATEGORY_LAUNCHER);
        applicationPackageInfoList = packageManager.queryIntentActivities(mainIntent, 0);
    }

    public Drawable loadIconFor(ApplicationData request) {
        return getLauncherActivityResolverInfoByPackageName(request).loadIcon(packageManager);
    }

    public List<ApplicationData> filterOutNotExists(List<ApplicationData> testList) {
        final List<ApplicationData> answer =new ArrayList<ApplicationData>();
        Lists.iterateAndRemove(testList, new Closure<Iterator<ApplicationData>, Boolean>() {
            @Override
            public Boolean execute(Iterator<ApplicationData> iterator) {
                ApplicationData data = iterator.next();
                if (getLauncherActivityResolverInfoByPackageName(data) == null){
                    answer.add(data);
                    iterator.remove();
                }
                return false;
            }
        });
        return answer;
    }

    public int getCategoryIndex(PlayMarketDetailsProvider.PlayMarketCategory category) {
        return category.ordinal();
    }
}
