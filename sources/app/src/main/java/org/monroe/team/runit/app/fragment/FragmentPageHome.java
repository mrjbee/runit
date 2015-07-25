package org.monroe.team.runit.app.fragment;

import android.os.Bundle;

import org.monroe.team.runit.app.R;
import org.monroe.team.runit.app.views.TransperentDataBitmapBackgroundLayout;

public class FragmentPageHome extends FragmentAbstractBodyPage {

    private TransperentDataBitmapBackgroundLayout mPanelPageContent;

    @Override
    public void onSelect() {

    }

    @Override
    protected int getLayoutId() {
        return R.layout.fragment_page_home;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mPanelPageContent = view(R.id.panel_page_content, TransperentDataBitmapBackgroundLayout.class);
        mPanelPageContent.setBlurredBackground(application().data_blurredBackground);
    }
}
