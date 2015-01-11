package org.monroe.team.runit.app.uc;
import org.monroe.team.android.box.db.DAOSupport;
import org.monroe.team.android.box.services.AndroidServiceRegistry;
import org.monroe.team.android.box.db.TransactionUserCase;
import org.monroe.team.corebox.services.ServiceRegistry;
import org.monroe.team.runit.app.db.Dao;

import java.util.ArrayList;
import java.util.List;

public class GetApplicationCategories extends TransactionUserCase<Void,List<GetApplicationCategories.ApplicationCategory>, Dao> {


    public GetApplicationCategories(ServiceRegistry serviceRegistry) {
        super(serviceRegistry);
    }

    @Override
    protected List<ApplicationCategory> transactionalExecute(Void request, Dao dao) {
        List<DAOSupport.Result> results = dao.appsCountPerCategory();
        List<ApplicationCategory> answer = new ArrayList<>();
        for (DAOSupport.Result result : results) {
            Long category = result.get(0, Long.class);
            long appsCount = result.get(1, Long.class);
            answer.add(new ApplicationCategory(category, appsCount));
        }
        return answer;
    }

    public final class ApplicationCategory {

        public final Long categoryId;
        public final long appsCount;

        public ApplicationCategory(Long categoryId, long appsCount) {
            this.categoryId = categoryId;
            this.appsCount = appsCount;
        }
    }
}
