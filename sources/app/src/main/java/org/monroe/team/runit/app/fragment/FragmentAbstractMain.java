package org.monroe.team.runit.app.fragment;

import org.monroe.team.android.box.app.FragmentSupport;
import org.monroe.team.runit.app.MainActivity;
import org.monroe.team.runit.app.android.RunitApp;

public abstract class FragmentAbstractMain extends FragmentSupport<RunitApp> {

    final public MainActivity activityMain(){
        return (MainActivity) activity();
    }

}
