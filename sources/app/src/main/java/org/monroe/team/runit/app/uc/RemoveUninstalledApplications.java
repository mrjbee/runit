package org.monroe.team.runit.app.uc;

import org.monroe.team.android.box.services.AndroidServiceRegistry;
import org.monroe.team.android.box.db.TransactionUserCase;
import org.monroe.team.corebox.services.ServiceRegistry;
import org.monroe.team.runit.app.db.Dao;
import org.monroe.team.runit.app.service.ApplicationRegistry;
import org.monroe.team.runit.app.uc.entity.ApplicationData;

import java.util.List;

public class RemoveUninstalledApplications extends TransactionUserCase<Void,Void,Dao> {

    public RemoveUninstalledApplications(ServiceRegistry serviceRegistry) {
        super(serviceRegistry);
    }

    @Override
    protected Void transactionalExecute(Void request, Dao dao) {
        using(ApplicationRegistry.class).refreshApplicationsWithLauncherActivityList();
        List<ApplicationData> appsData = using(ApplicationRegistry.class).getApplicationsWithLauncherActivity();
        if (appsData.isEmpty()) return null;
        int result = dao.removeAppsNotInList(appsData);
        return null;
    }
}
