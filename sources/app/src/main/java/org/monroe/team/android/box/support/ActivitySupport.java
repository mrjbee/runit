package org.monroe.team.android.box.support;

import android.app.Application;
import android.os.Bundle;
import android.view.View;

import java.util.LinkedList;
import java.util.List;

public abstract class ActivitySupport <AppType extends Application> extends android.app.Activity{

    private List<Cancellable> destroyAwareList = new LinkedList<Cancellable>();

    public <ViewType extends View> ViewType view(int resourceId, Class<ViewType> viewType){
        return (ViewType) findViewById(resourceId);
    }

    public View view(int resourceId){
        return findViewById(resourceId);
    }

    public AppType application(){
        return (AppType) getApplication();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        for (Cancellable cancellable : destroyAwareList) {
            cancellable.start();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        for (Cancellable cancellable : destroyAwareList) {
            cancellable.cancel();
        }
        destroyAwareList.clear();
    }

    public void registerCallableForCreateDestroy(Cancellable cancellable){
        destroyAwareList.add(cancellable);
    };
}
