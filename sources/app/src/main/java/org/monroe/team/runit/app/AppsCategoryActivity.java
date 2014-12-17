package org.monroe.team.runit.app;

import android.content.Context;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import org.monroe.team.android.box.SizeUtils;
import org.monroe.team.android.box.manager.BackgroundTaskManager;
import org.monroe.team.android.box.support.ActivitySupport;
import org.monroe.team.android.box.ui.animation.apperrance.AppearanceController;
import org.monroe.team.runit.app.android.RunitApp;
import org.monroe.team.runit.app.uc.entity.ApplicationData;

import java.util.List;

import static org.monroe.team.android.box.ui.animation.apperrance.AppearanceControllerBuilder.alpha;
import static org.monroe.team.android.box.ui.animation.apperrance.AppearanceControllerBuilder.animateAppearance;
import static org.monroe.team.android.box.ui.animation.apperrance.AppearanceControllerBuilder.duration_constant;
import static org.monroe.team.android.box.ui.animation.apperrance.AppearanceControllerBuilder.interpreter_accelerate_decelerate;
import static org.monroe.team.android.box.ui.animation.apperrance.AppearanceControllerBuilder.interpreter_decelerate;
import static org.monroe.team.android.box.ui.animation.apperrance.AppearanceControllerBuilder.xSlide;
import static org.monroe.team.android.box.ui.animation.apperrance.AppearanceControllerBuilder.ySlide;


public class AppsCategoryActivity extends ActivitySupport<RunitApp> {

    private ArrayAdapter<RunitApp.Category> categoryAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_apps_category);
        float[] buttonBounds = null;
        if (getIntent() != null) {
            buttonBounds = getIntent().getFloatArrayExtra("button_bounds");
            //40dp
            AppearanceController headerAppearanceController = animateAppearance(
                    view(R.id.ac_header_panel),
                    ySlide(0f, buttonBounds[1] - SizeUtils.dpToPx(40, getResources())))
                    .showAnimation(duration_constant(400), interpreter_accelerate_decelerate())
                    .build();
            AppearanceController headerXAppearanceController = animateAppearance(
                    view(R.id.ac_header_panel),
                    xSlide(0f, buttonBounds[0] - SizeUtils.dpToPx(10, getResources())))
                    .showAnimation(duration_constant(400), interpreter_accelerate_decelerate())
                    .build();

            headerAppearanceController.hideWithoutAnimation();
            headerAppearanceController.show();
            headerXAppearanceController.hideWithoutAnimation();
            headerXAppearanceController.show();

            AppearanceController backgroundAppearanceController = animateAppearance(
                    view(R.id.ac_background_panel),
                    alpha(1f, 0.2f))
                    .showAnimation(duration_constant(300), interpreter_decelerate(null))
                    .build();
            backgroundAppearanceController.hideWithoutAnimation();
            backgroundAppearanceController.show();
        }

        categoryAdapter = new ArrayAdapter<RunitApp.Category>(getApplicationContext(), R.layout.item_category){
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                if (convertView == null){
                    LayoutInflater inflater = (LayoutInflater) getContext()
                            .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                    convertView = inflater.inflate(R.layout.item_category, null);
                }

                RunitApp.Category category = this.getItem(position);
                ((TextView)convertView.findViewById(R.id.name)).setText(category.name);
                ((TextView)convertView.findViewById(R.id.sub_name)).setText("apps "+category.appsCount);

                return convertView;
            }
        };
        view(R.id.ac_category_list, ListView.class).setAdapter(categoryAdapter);
    }

    @Override
    protected void onResume() {
        super.onResume();
        application().fetchApplicationCategories(new RunitApp.OnAppCategoriesCallback() {
            @Override
            public void fetched(List<RunitApp.Category> fetchData) {
                categoryAdapter.clear();
                categoryAdapter.addAll(fetchData);
                categoryAdapter.notifyDataSetChanged();
            }

            @Override
            public void noData() {
                //TODO some explanantion here
                categoryAdapter.clear();
                categoryAdapter.notifyDataSetChanged();
            }
        });
    }
}
