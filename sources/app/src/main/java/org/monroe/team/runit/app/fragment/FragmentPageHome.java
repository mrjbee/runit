package org.monroe.team.runit.app.fragment;

import android.os.Bundle;
import android.widget.ScrollView;

import org.monroe.team.android.box.utils.DisplayUtils;
import org.monroe.team.runit.app.R;
import org.monroe.team.runit.app.views.MyScrollView;
import org.monroe.team.runit.app.views.TransperentDataBitmapBackgroundLayout;

public class FragmentPageHome extends FragmentAbstractBodyPage implements MyScrollView.OnScrollListener {

    private TransperentDataBitmapBackgroundLayout mPanelPageContent;
    private MyScrollView mScrollView;
    private boolean mScrollOnPageRequested = false;

    @Override
    public void onSelect() {

    }

    @Override
    public void setOnPageScroll() {
        if (mScrollOnPageRequested) return;
        mScrollOnPageRequested = true;
        mScrollView.smoothScrollTo(0,0);
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
        mScrollView = view(R.id.scroll_view, MyScrollView.class);
        mScrollView.setScrollListener(this);
        mScrollView.setSmoothScrollingEnabled(true);
    }

    @Override
    public void onScrollChanged(int left, int top) {
        mScrollOnPageRequested = false;
        if (top > DisplayUtils.dpToPx(50, getResources())){
            activityMain().visibility_Header(false);
        }else {
            activityMain().visibility_Header(true);
        }
    }
}
