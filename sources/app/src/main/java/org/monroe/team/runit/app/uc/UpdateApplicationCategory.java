package org.monroe.team.runit.app.uc;

import org.monroe.team.android.box.services.AndroidServiceRegistry;
import org.monroe.team.android.box.db.TransactionUserCase;
import org.monroe.team.corebox.services.ServiceRegistry;
import org.monroe.team.runit.app.db.Dao;
import org.monroe.team.runit.app.uc.entity.ApplicationData;

public class UpdateApplicationCategory extends TransactionUserCase<UpdateApplicationCategory.Request, Void, Dao>{


    public UpdateApplicationCategory(ServiceRegistry serviceRegistry) {
        super(serviceRegistry);
    }

    @Override
    protected Void transactionalExecute(Request request, Dao dao) {
        if (request.applicationData == null) return null;
        boolean answer = dao.updateApplicationCategory(request.applicationData.packageName,
                request.applicationData.name,
                request.categoryId,
                true);
        return null;
    }

    public static class Request{

        private final long categoryId;
        private final ApplicationData applicationData;

        public Request(long categoryId, ApplicationData applicationData) {
            this.categoryId = categoryId;
            this.applicationData = applicationData;
        }
    }
}
