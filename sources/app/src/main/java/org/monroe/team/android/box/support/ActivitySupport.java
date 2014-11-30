package org.monroe.team.android.box.support;

import android.app.Application;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.Window;

import org.monroe.team.runit.app.R;

import java.util.LinkedList;
import java.util.List;

public abstract class ActivitySupport <AppType extends Application> extends android.app.Activity{


    public <ViewType extends View> ViewType view(int resourceId, Class<ViewType> viewType){
        return (ViewType) findViewById(resourceId);
    }

    public View view(int resourceId){
        return findViewById(resourceId);
    }

    public AppType application(){
        return (AppType) getApplication();
    }
}
