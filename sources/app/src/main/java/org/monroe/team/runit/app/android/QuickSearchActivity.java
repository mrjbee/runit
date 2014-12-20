package org.monroe.team.runit.app.android;

import android.animation.Animator;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextWatcher;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import org.monroe.team.android.box.manager.BackgroundTaskManager;
import org.monroe.team.android.box.support.ActivitySupport;
import org.monroe.team.android.box.ui.AppearanceControllerOld;
import org.monroe.team.android.box.ui.animation.apperrance.AppearanceController;
import static org.monroe.team.android.box.ui.animation.apperrance.AppearanceControllerBuilder.*;
import org.monroe.team.runit.app.R;
import org.monroe.team.runit.app.uc.entity.ApplicationData;

import java.util.List;


public class QuickSearchActivity extends ActivitySupport<RunitApp> {

    private AppearanceController searchPanelAppearanceController;
    private AppearanceController searchResultPanelAppearanceController;
    private ArrayAdapter<RunitApp.AppSearchResult> adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quick_search);
        view(R.id.qs_root).addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
            @Override
            public void onLayoutChange(View v, int left, int top, int right, int bottom,
                                       int oldLeft, int oldTop, int oldRight, int oldBottom) {
                if (oldBottom < bottom
                        && left==oldLeft
                        && right==oldRight
                        && top == oldTop){
                    checkAndFinish();
                }
            }
        });

        final int layout = R.layout.item_search_application_qs;
        adapter = new ArrayAdapter<RunitApp.AppSearchResult>(this, layout){
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                final SearchItemDetails holder;
                if (convertView == null){
                    LayoutInflater inflater = (LayoutInflater) getContext()
                            .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                    convertView = inflater.inflate(layout, null);
                    holder = new SearchItemDetails(
                            convertView.findViewById(R.id.search_first_item_stub),
                            (TextView) convertView.findViewById(R.id.search_item_text),
                            (TextView) convertView.findViewById(R.id.search_item_sub_text),
                            (ImageView) convertView.findViewById(R.id.search_item_image));
                    convertView.setTag(holder);
                } else {
                    holder = (SearchItemDetails) convertView.getTag();
                }
                if (holder.drawableLoadingTask != null){
                    holder.drawableLoadingTask.cancel();
                }
                if (holder.categoryLoadingTask != null){
                    holder.categoryLoadingTask.cancel();
                }
                holder.textCategoryView.setText("");

                holder.foundApplicationItem = this.getItem(position);
                final ApplicationData applicationData = holder.foundApplicationItem.applicationData;
                SpannableString spannableString = new SpannableString(applicationData.name);
                if (holder.foundApplicationItem.selectionStartIndex != null){
                    spannableString.setSpan(new ForegroundColorSpan(getResources().getColor(R.color.blue_themed)),
                            holder.foundApplicationItem.selectionStartIndex,
                            holder.foundApplicationItem.selectionEndIndex,
                            Spanned.SPAN_COMPOSING);
                    spannableString.setSpan(new StyleSpan(Typeface.BOLD),
                            holder.foundApplicationItem.selectionStartIndex,
                            holder.foundApplicationItem.selectionEndIndex,
                            Spanned.SPAN_COMPOSING);
                }
                holder.textView.setText(spannableString);
                holder.imageView.setVisibility(View.INVISIBLE);

                holder.drawableLoadingTask =  application().loadApplicationIcon(applicationData, new RunitApp.OnLoadApplicationIconCallback() {

                    SearchItemDetails forHolder = holder;

                    @Override
                    public void load(ApplicationData applicationData, Drawable drawable) {
                        if (forHolder.foundApplicationItem.applicationData == applicationData) {
                            forHolder.imageView.setImageDrawable(drawable);
                            forHolder.imageView.setVisibility(View.VISIBLE);
                        }
                    }
                });

                holder.categoryLoadingTask =  application().loadApplicationCategory(applicationData, new RunitApp.OnLoadCategoryCallback() {

                    SearchItemDetails forHolder = holder;

                    @Override
                    public void load(ApplicationData applicationData, RunitApp.Category category) {
                        if (forHolder.foundApplicationItem.applicationData == applicationData) {
                            forHolder.textCategoryView.setText(category.name);
                        }
                    }
                });
                return convertView;
            }

            class SearchItemDetails {

                final View firstItemMarginView;
                final TextView textView;
                final TextView textCategoryView;
                final ImageView imageView;

                RunitApp.AppSearchResult foundApplicationItem;

                BackgroundTaskManager.BackgroundTask<?> drawableLoadingTask;
                BackgroundTaskManager.BackgroundTask<?> categoryLoadingTask;

                SearchItemDetails(View firstItemMarginView, TextView textView, TextView textCategoryView, ImageView imageView) {
                    this.firstItemMarginView = firstItemMarginView;
                    this.textView = textView;
                    this.textCategoryView = textCategoryView;
                    this.imageView = imageView;
                }
            }
        };
        view(R.id.qs_search_result_list,ListView.class).setAdapter(adapter);
        view(R.id.qs_search_result_list,ListView.class).setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                application().launchApplication(adapter.getItem(position).applicationData);
            }
        });

        view(R.id.qs_search_edit, EditText.class).addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                initiateAppsSearch();
            }
        });


        if (savedInstanceState == null) {

            float searchPanelAppearanceStartFrom = 0;
            if (getIntent() != null && getIntent().getSourceBounds() != null) {
                searchPanelAppearanceStartFrom = getIntent().getSourceBounds().top;
            } else {
                searchPanelAppearanceStartFrom = getResources().getDisplayMetrics().heightPixels * 0.8f;
            }


            searchResultPanelAppearanceController = animateAppearance(view(R.id.qs_content_panel),
                     alpha(1f,0f))
                    .showAnimation(duration_constant(300), interpreter_decelerate(0.3f))
                    .build();


            searchPanelAppearanceController = animateAppearance(view(R.id.qs_search_panel),
                    ySlide(0, searchPanelAppearanceStartFrom))
                    .showAnimation(duration_constant(400), interpreter_accelerate(0.8f))
                    .build();

            searchPanelAppearanceController.hideWithoutAnimation();
            searchResultPanelAppearanceController.hideWithoutAnimation();
            application().fetchMostRecentApplication(new RunitApp.OnApplicationFetchedCallback() {
                @Override
                public void fetched(List<ApplicationData> applicationDataList) {
                    adapter.clear();
                    for (ApplicationData data : applicationDataList) {
                        adapter.add(new RunitApp.AppSearchResult(data,0,0));
                    }
                    adapter.notifyDataSetChanged();
                }
            });
        }


    }


    @Override
    protected void onResume() {
        super.onResume();
        if (searchPanelAppearanceController != null) {
            searchPanelAppearanceController.showAndCustomize(new AppearanceController.AnimatorCustomization() {
                @Override
                public void customize(Animator animator) {
                    animator.setStartDelay(100);
                    animator.addListener(new AppearanceControllerOld.AnimatorListenerAdapter(){
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            focusOnEdit();
                        }
                    });
                }
            });
            searchResultPanelAppearanceController.showAndCustomize(new AppearanceController.AnimatorCustomization() {
                @Override
                public void customize(Animator animator) {
                    animator.setStartDelay(500);
                }
            });
        }
    }


    private void focusOnEdit() {
        EditText yourEditText = view(R.id.qs_search_edit, EditText.class);
        yourEditText.requestFocus();
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.showSoftInput(yourEditText, InputMethodManager.SHOW_IMPLICIT);
    }

    private void initiateAppsSearch() {
        final String searchQuery = view(R.id.qs_search_edit,EditText.class).getText().toString().trim();
        application().searchApplicationByName(searchQuery.trim(), new RunitApp.OnAppSearchCallback(){
            @Override
            public void found(String searchQuery, List<RunitApp.AppSearchResult> searchResultList) {
                adapter.clear();
                adapter.addAll(searchResultList);
                adapter.notifyDataSetChanged();
            }
        });
    }


    public static PendingIntent open(Context context) {
        Intent intent = new Intent(context, QuickSearchActivity.class)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                .addFlags(Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED)
                .addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
        return PendingIntent.getActivity(context, 0, intent,
                PendingIntent.FLAG_UPDATE_CURRENT);
    }

    private void checkAndFinish() {
        if (view(R.id.qs_search_edit,EditText.class).getText().toString().isEmpty()){
            finish();
        }
    }

}
