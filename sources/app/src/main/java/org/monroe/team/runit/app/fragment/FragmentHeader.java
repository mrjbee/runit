package org.monroe.team.runit.app.fragment;

import android.os.Bundle;

import org.monroe.team.runit.app.R;
import org.monroe.team.runit.app.views.TransperentDataBitmapBackgroundLayout;

public class FragmentHeader extends FragmentAbstractMain{

    private TransperentDataBitmapBackgroundLayout mPanelHeaderContent;

    @Override
    protected int getLayoutId() {
        return R.layout.fragment_header;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mPanelHeaderContent = view(R.id.panel_header_content, TransperentDataBitmapBackgroundLayout.class);
        mPanelHeaderContent.setBlurredBackground(application().data_blurredBackground);
    }
}
