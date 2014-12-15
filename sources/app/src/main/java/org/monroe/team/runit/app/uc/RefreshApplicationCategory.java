package org.monroe.team.runit.app.uc;


import android.util.Log;

import org.monroe.team.android.box.db.DAOSupport;
import org.monroe.team.android.box.manager.NetworkManager;
import org.monroe.team.android.box.manager.ServiceRegistry;
import org.monroe.team.android.box.uc.TransactionUserCase;
import org.monroe.team.runit.app.db.Dao;
import org.monroe.team.runit.app.service.ApplicationRegistry;
import org.monroe.team.runit.app.service.PlayMarketDetailsProvider;
import org.monroe.team.runit.app.uc.entity.ApplicationData;

import java.io.IOException;

public class RefreshApplicationCategory extends TransactionUserCase<ApplicationData, RefreshApplicationCategory.RefreshStatus, Dao> {

    public RefreshApplicationCategory(ServiceRegistry serviceRegistry) {
        super(serviceRegistry);
    }

    @Override
    protected RefreshStatus transactionalExecute(ApplicationData request, Dao dao) {
        DAOSupport.Result result = dao.getAppByName(request.packageName, request.name);
        if (result != null && result.get(5, Long.class) != null){
            return RefreshStatus.NO_ACTION_REQUIRED;
        }
        try {
            PlayMarketDetailsProvider.PlayMarketCategory category = using(PlayMarketDetailsProvider.class).getCategory(request.packageName);
            Log.i("CATEGORY_RESOLVER",request.getUniqueName()+"="+category.name());
            int index = using(ApplicationRegistry.class).getCategoryIndex(category);
            dao.insertApplicationWithCategory(request.packageName,request.name, index);
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
