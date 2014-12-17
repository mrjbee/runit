package org.monroe.team.runit.app.uc;


import android.util.Log;

import org.monroe.team.android.box.db.DAOSupport;
import org.monroe.team.android.box.db.TransactionManager;
import org.monroe.team.android.box.manager.NetworkManager;
import org.monroe.team.android.box.manager.ServiceRegistry;
import org.monroe.team.android.box.uc.TransactionUserCase;
import org.monroe.team.android.box.uc.UserCase;
import org.monroe.team.android.box.uc.UserCaseSupport;
import org.monroe.team.runit.app.db.Dao;
import org.monroe.team.runit.app.service.ApplicationRegistry;
import org.monroe.team.runit.app.service.PlayMarketDetailsProvider;
import org.monroe.team.runit.app.uc.entity.ApplicationData;

import java.io.IOException;

public class RefreshApplicationCategory extends UserCaseSupport<ApplicationData, RefreshApplicationCategory.RefreshStatus> {

    public RefreshApplicationCategory(ServiceRegistry serviceRegistry) {
        super(serviceRegistry);
    }


    @Override
    public RefreshStatus execute(final ApplicationData request) {
        RefreshStatus answer = using(TransactionManager.class).execute(new TransactionManager.TransactionAction<RefreshStatus>() {
            @Override
            public RefreshStatus execute(DAOSupport dao) {
                dao=dao;
                DAOSupport.Result result = ((Dao)dao).getAppByName(request.packageName, request.name);
                if (result != null && result.get(5, Long.class) != null){
                    return RefreshStatus.NO_ACTION_REQUIRED;
                }
                return null;
            }
        });

        if (answer != null){
            return answer;
        }

        try {
            PlayMarketDetailsProvider.PlayMarketCategory category = using(PlayMarketDetailsProvider.class).getCategory(request.packageName);
            Log.i("CATEGORY_RESOLVER",request.getUniqueName()+"="+category.name());
            final int index = using(ApplicationRegistry.class).getCategoryIndex(category);
            using(TransactionManager.class).execute(new TransactionManager.TransactionAction<Object>() {
                @Override
                public Object execute(DAOSupport dao) {
                    ((Dao)dao).updateApplicationCategory(request.packageName, request.name, (long) index, true);
                    return null;
                }
            });
        } catch (IOException e) {
            return RefreshStatus.FAILED_NO_CONNECTION;
        } catch (PlayMarketDetailsProvider.BlockException e) {
            return RefreshStatus.FAILED_BLOCKED;
        }
        return RefreshStatus.UPDATED;
    }

    public enum RefreshStatus {
        NO_ACTION_REQUIRED,
        UPDATED,
        FAILED_NO_CONNECTION,
        FAILED_BLOCKED
    }

}
