package org.monroe.team.runit.app.android;

import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.widget.RemoteViews;

import org.monroe.team.runit.app.AppsCategoryActivity;
import org.monroe.team.runit.app.R;
import org.monroe.team.runit.app.android.DashboardActivity;


/**
 * Implementation of App Widget functionality.
 */
public class TransparentPanelWidget extends AppWidgetProvider {

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        // There may be multiple widgets active, so update all of them
        final int N = appWidgetIds.length;
        for (int i = 0; i < N; i++) {
            updateAppWidget(context, appWidgetManager, appWidgetIds[i]);
        }
    }


    @Override
    public void onEnabled(Context context) {
        // Enter relevant functionality for when the first widget is created
    }

    @Override
    public void onDisabled(Context context) {
        // Enter relevant functionality for when the last widget is disabled
    }

    static void updateAppWidget(Context context, AppWidgetManager appWidgetManager,
                                int appWidgetId) {

        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.transperent_panel_widget);
        views.setOnClickPendingIntent(R.id.bw_root_layout, QuickSearchActivity.open(context));
        views.setOnClickPendingIntent(R.id.bw_category_image, AppsCategoryActivity.open(context));
        // Instruct the widget manager to update the widget
        appWidgetManager.updateAppWidget(appWidgetId, views);
    }
}


