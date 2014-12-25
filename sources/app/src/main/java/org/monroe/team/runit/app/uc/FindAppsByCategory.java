package org.monroe.team.runit.app.uc;

import org.monroe.team.android.box.db.DAOSupport;
import org.monroe.team.android.box.manager.ServiceRegistry;
import org.monroe.team.android.box.uc.TransactionUserCase;
import org.monroe.team.android.box.uc.UserCaseSupport;
import org.monroe.team.runit.app.db.Dao;
import org.monroe.team.runit.app.service.ApplicationRegistry;
import org.monroe.team.runit.app.uc.entity.ApplicationData;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class FindAppsByCategory extends TransactionUserCase<Long, FindAppsByText.SearchResult, Dao> {


    public FindAppsByCategory(ServiceRegistry serviceRegistry) {
        super(serviceRegistry);
    }

    @Override
    protected FindAppsByText.SearchResult transactionalExecute(Long request, Dao dao) {
        FindAppsByText.SearchResult answer = new FindAppsByText.SearchResult();
        List<DAOSupport.Result> results = dao.getAppsByCategory(request);
        for (DAOSupport.Result result : results) {
            answer.applicationDataList.add(new ApplicationData(result.get(1,String.class), result.get(2,String.class)));
        }
        Collections.sort(answer.applicationDataList,new Comparator<ApplicationData>() {
            @Override
            public int compare(ApplicationData lhs, ApplicationData rhs) {
                if(lhs == null) return -1;
                if(rhs == null) return 1;
                return lhs.name.compareTo(rhs.name);
            }
        });
        return answer;
    }

}
