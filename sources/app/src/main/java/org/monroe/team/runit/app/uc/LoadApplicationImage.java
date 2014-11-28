package org.monroe.team.runit.app.uc;

import android.graphics.drawable.Drawable;

import org.monroe.team.android.box.manager.ServiceRegistry;
import org.monroe.team.android.box.uc.UserCaseSupport;
import org.monroe.team.runit.app.service.ApplicationRegistry;
import org.monroe.team.runit.app.uc.entity.ApplicationData;

public class LoadApplicationImage extends UserCaseSupport<ApplicationData,Drawable> {

    public LoadApplicationImage(ServiceRegistry serviceRegistry) {
        super(serviceRegistry);
    }

    @Override
    public Drawable execute(ApplicationData request) {
        Drawable answer = using(ApplicationRegistry.class).loadIconFor(request);
        return answer;
    }
}
