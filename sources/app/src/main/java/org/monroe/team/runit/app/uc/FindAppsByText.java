package org.monroe.team.runit.app.uc;

import org.monroe.team.android.box.services.AndroidServiceRegistry;
import org.monroe.team.corebox.services.ServiceRegistry;
import org.monroe.team.corebox.uc.UserCaseSupport;
import org.monroe.team.runit.app.service.ApplicationRegistry;
import org.monroe.team.runit.app.uc.entity.ApplicationData;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class FindAppsByText extends UserCaseSupport<FindAppsByText.SearchRequest, FindAppsByText.SearchResult> {


    public FindAppsByText(ServiceRegistry serviceRegistry) {
        super(serviceRegistry);
    }

    @Override
    public SearchResult execute(SearchRequest request) {
        SearchResult result = new SearchResult();
        if (!request.searchString.isEmpty()){
            List<ApplicationData> descriptions = using(ApplicationRegistry.class).getApplicationsWithLauncherActivity();
            for (ApplicationData description : descriptions) {
                if (description.name.toLowerCase().contains(request.searchString.toLowerCase())){
                   result.applicationDataList.add(description);
                }
            }
        }
        Collections.sort(result.applicationDataList, new Comparator<ApplicationData>() {
            @Override
            public int compare(ApplicationData lhs, ApplicationData rhs) {
                if (lhs == rhs && lhs == null){
                    return 0;
                }
                if (lhs == null){
                    return -1;
                }
                return lhs.name.compareTo(rhs.name.toString());
            }
        });
        return result;
    }

    public static class SearchRequest{

        private final String searchString;

        public SearchRequest(String searchString) {
            this.searchString = searchString;
        }
    }

    public static class SearchResult {
        public final List<ApplicationData> applicationDataList = new ArrayList<ApplicationData>();
    }
}
