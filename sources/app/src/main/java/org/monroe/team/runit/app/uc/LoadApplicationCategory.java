package org.monroe.team.runit.app.uc;

import org.monroe.team.android.box.db.DAOSupport;
import org.monroe.team.android.box.services.AndroidServiceRegistry;
import org.monroe.team.android.box.db.TransactionUserCase;
import org.monroe.team.corebox.services.ServiceRegistry;
import org.monroe.team.runit.app.db.Dao;
import org.monroe.team.runit.app.uc.entity.ApplicationData;

public class LoadApplicationCategory extends TransactionUserCase<ApplicationData,Long,Dao> {

    public LoadApplicationCategory(ServiceRegistry serviceRegistry) {
        super(serviceRegistry);
    }

    @Override
    protected Long transactionalExecute(ApplicationData request, Dao dao) {
        DAOSupport.Result result = dao.getAppByName(request.packageName, request.name);
        if (result == null){
            return  null;
        }
        return result.get(5,Long.class);
    }


}
