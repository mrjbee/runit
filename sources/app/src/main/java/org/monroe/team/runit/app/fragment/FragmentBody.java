package org.monroe.team.runit.app.fragment;

import android.app.Fragment;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.view.View;

import org.monroe.team.corebox.log.L;
import org.monroe.team.runit.app.ContractBackButton;
import org.monroe.team.runit.app.R;
import org.monroe.team.runit.app.views.ViewPagerPageRelativeLayout;

public class FragmentBody extends FragmentAbstractMain implements ContractBackButton{

    private ViewPager mViewPager;
    private FragmentPagerAdapter mFragmentPagerAdapter;

    @Override
    protected int getLayoutId() {
        return R.layout.fragment_body;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mViewPager = view(R.id.viewPager,ViewPager.class);
        mFragmentPagerAdapter = new FragmentPagerAdapter(activity().getFragmentManager()) {

            @Override
            public Fragment getItem(int position) {
                switch (position){
                    case 0: return new FragmentPageHome();
                    case 1: return new FragmentPageHome();
                    default:
                        throw new IllegalStateException("Position not exists = "+position);
                }
            }

            @Override
            public int getCount() {
                return 2;
            }
        };
        mViewPager.setAdapter(mFragmentPagerAdapter);

        mFragmentPagerAdapter.notifyDataSetChanged();
        /*if (getArguments().getBoolean("first_run", false)) {
            mViewPager.setCurrentItem(1);
            getArguments().putBoolean("first_run", false);
        }*/
        mViewPager.setPageTransformer(false, new ViewPager.PageTransformer() {
            @Override
            public void transformPage(View page, float position) {
                if (page instanceof ViewPagerPageRelativeLayout){
                    ((ViewPagerPageRelativeLayout) page).setSlideVisibilityFraction(position);
                }
            }
        });
        mViewPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                getPage(position).setOnPageScroll();
                if (position < mFragmentPagerAdapter.getCount() -1){
                    getPage(position + 1).setOnPageScroll();
                }
            }

            @Override
            public void onPageSelected(int position) {
                activityMain().onBodyPageChanged(position);
                if (getPage(position) ==null){
                    return;
                }
                getPage(position).onSelect();
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mFragmentPagerAdapter != null) {
            mFragmentPagerAdapter = null;
        }
    }

    @Override
    public boolean onBackPressed() {
        FragmentAbstractBodyPage dashboardSlide = getCurrentSlide();
        if (dashboardSlide instanceof ContractBackButton){
            if (((ContractBackButton) dashboardSlide).onBackPressed()){
                return true;
            }
        }

        //if (mViewPager.getCurrentItem() == 1) return false;
       // mViewPager.setCurrentItem(1, true);
        return true;
    }

    public void updateScreen(int screenPosition) {
        mViewPager.setCurrentItem(screenPosition, true);
    }

    private FragmentAbstractBodyPage getPage(int pageIndex) {
        String pageTag = "android:switcher:" + mViewPager.getId() + ":" + pageIndex;
        return (FragmentAbstractBodyPage) getFragmentManager().findFragmentByTag(pageTag);
    }

    public FragmentAbstractBodyPage getCurrentSlide() {
        int curItem = mViewPager.getCurrentItem();
        Fragment fragment = getPage(curItem);
        return (FragmentAbstractBodyPage) fragment;
    }

    public void viewPagerGesture(boolean enabled) {
        mViewPager.requestDisallowInterceptTouchEvent(!enabled);
    }
}
