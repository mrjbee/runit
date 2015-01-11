package org.monroe.team.runit.app.uc;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.ResolveInfo;

import org.monroe.team.android.box.services.AndroidServiceRegistry;
import org.monroe.team.corebox.services.ServiceRegistry;
import org.monroe.team.corebox.utils.DateUtils;
import org.monroe.team.android.box.db.DAOSupport;
import org.monroe.team.android.box.db.TransactionUserCase;
import org.monroe.team.runit.app.db.Dao;
import org.monroe.team.runit.app.service.ApplicationRegistry;
import org.monroe.team.runit.app.uc.entity.ApplicationData;

import java.util.Date;

public class LaunchApplication extends TransactionUserCase<ApplicationData, Void, Dao> {

    public LaunchApplication(ServiceRegistry serviceRegistry) {
        super(serviceRegistry);
    }

    @Override
    protected Void transactionalExecute(ApplicationData request, Dao dao) {
        String packageName = request.packageName;
        String title = request.name;

        long launchTimes = 1;
        Date date = DateUtils.now();
        DAOSupport.Result result = dao.getAppByName(packageName, title);
        if (result != null){
            launchTimes += result.get(4, Long.class);
        }

        //Do nothing if not updated
        boolean done = dao.updateApplicationLaunchStatistic(packageName,title, date, launchTimes, true);

        ///Launch details
        ResolveInfo resolveInfo= using(ApplicationRegistry.class).getLauncherActivityResolverInfoByPackageName(request);
        ActivityInfo activity=resolveInfo.activityInfo;
        ComponentName name=new ComponentName(activity.applicationInfo.packageName,
                activity.name);
        Intent i=new Intent(Intent.ACTION_MAIN);

        i.addCategory(Intent.CATEGORY_LAUNCHER);
        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK |
                Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
        i.setComponent(name);
        using(Context.class).startActivity(i);
        return null;
    }
}
