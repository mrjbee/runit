package org.monroe.team.runit.app.android.preneter;

import android.content.Context;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;

import org.monroe.team.android.box.manager.BackgroundTaskManager;
import org.monroe.team.android.box.support.ActivitySupport;
import org.monroe.team.runit.app.R;
import org.monroe.team.runit.app.android.RunitApp;
import org.monroe.team.runit.app.uc.entity.ApplicationData;

import java.util.List;

public abstract class SearchResultListDelegate {

    private final ListView listView;
    private ArrayAdapter<RunitApp.AppSearchResult> adapter;

    public SearchResultListDelegate(final ActivitySupport<RunitApp> activity, ListView listView, final int layoutId) {
        this.listView = listView;
        adapter = new ArrayAdapter<RunitApp.AppSearchResult>(activity, layoutId){
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                final SearchItemDetails holder;
                if (convertView == null){
                    LayoutInflater inflater = (LayoutInflater) getContext()
                            .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                    convertView = inflater.inflate(layoutId, null);
                    holder = new SearchItemDetails(
                            convertView.findViewById(R.id.search_first_item_stub),
                            (TextView) convertView.findViewById(R.id.search_item_text),
                            (ImageView) convertView.findViewById(R.id.search_item_image));
                    convertView.setTag(holder);
                } else {
                    holder = (SearchItemDetails) convertView.getTag();
                }
                if (position == 0){
                    holder.firstItemMarginView.setVisibility(View.VISIBLE);
                } else {
                    holder.firstItemMarginView.setVisibility(View.GONE);
                }
                if (holder.drawableLoadingTask != null){
                    holder.drawableLoadingTask.cancel();
                }

                holder.foundApplicationItem = this.getItem(position);
                final ApplicationData applicationData = holder.foundApplicationItem.applicationData;
                SpannableString spannableString = new SpannableString(applicationData.name);
                if (holder.foundApplicationItem.selectionStartIndex != null){
                    spannableString.setSpan(new ForegroundColorSpan(activity.getResources().getColor(R.color.blue_themed)),
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

                BackgroundTaskManager.BackgroundTask<Drawable> loadTask = activity.application().loadApplicationIcon(applicationData, new RunitApp.OnLoadApplicationIconCallback() {
                    SearchItemDetails forHolder = holder;

                    @Override
                    public void load(ApplicationData applicationData, Drawable drawable) {
                        if (forHolder.foundApplicationItem.applicationData == applicationData) {
                            forHolder.imageView.setImageDrawable(drawable);
                            forHolder.imageView.setVisibility(View.VISIBLE);
                        }
                    }
                });

                holder.drawableLoadingTask = loadTask;
                return convertView;
            }

            class SearchItemDetails {

                final View firstItemMarginView;
                final TextView textView;
                final ImageView imageView;

                RunitApp.AppSearchResult foundApplicationItem;

                BackgroundTaskManager.BackgroundTask<Drawable> drawableLoadingTask;

                SearchItemDetails(View firstItemMarginView, TextView textView, ImageView imageView) {
                    this.firstItemMarginView = firstItemMarginView;
                    this.textView = textView;
                    this.imageView = imageView;
                }
            }
        };
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                SearchResultListDelegate.this.onItemClick(parent, view, position, id);
            }
        });
    }

    protected abstract void onItemClick(AdapterView<?> parent, View view, int position, long id);

    public void add(boolean clearPrevious, List<RunitApp.AppSearchResult> resultList){
        if (clearPrevious) adapter.clear();
        if (resultList != null) {
            adapter.addAll(resultList);
        }
        adapter.notifyDataSetChanged();
    }

    public RunitApp.AppSearchResult get(int position){
      return  adapter.getItem(position);
    }

}
