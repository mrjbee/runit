package org.monroe.team.runit.app.uc;

import org.monroe.team.android.box.db.DAOSupport;
import org.monroe.team.android.box.services.AndroidServiceRegistry;
import org.monroe.team.android.box.db.TransactionUserCase;
import org.monroe.team.corebox.services.ServiceRegistry;
import org.monroe.team.runit.app.db.Dao;
import org.monroe.team.runit.app.service.ApplicationRegistry;
import org.monroe.team.runit.app.uc.entity.ApplicationData;

import java.util.ArrayList;
import java.util.List;

public class FindMostUsedApplications extends TransactionUserCase<Void, List<ApplicationData>, Dao> {

    public FindMostUsedApplications(ServiceRegistry serviceRegistry) {
        super(serviceRegistry);
    }

    @Override
    protected List<ApplicationData> transactionalExecute(Void request, Dao dao) {
        List<DAOSupport.Result> resultList = dao.getMostUsedApplications();
        List<ApplicationData> answer = new ArrayList<ApplicationData>(resultList.size());
        for (DAOSupport.Result result : resultList) {
            answer.add(new ApplicationData(result.get(1,String.class), result.get(2,String.class)));
        }
        List<ApplicationData> notExistsList = using(ApplicationRegistry.class).filterOutNotExists(answer);
        return answer;
    }

}
