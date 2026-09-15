package com.example.piyologtimewidget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import androidx.work.Constraints
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf

class WidgetProviderSmall : AppWidgetProvider() {

    companion object {
        const val ACTION_REFRESH = "com.example.piyologtimewidget.ACTION_REFRESH_SMALL"
        fun updateWidget(context: Context, appWidgetManager: AppWidgetManager, appWidgetId: Int) {
            val views = RemoteViews(context.packageName, R.layout.widget_small)
            applyTextSizes(appWidgetManager, appWidgetId, views)

            val refreshIntent = Intent(context, WidgetProviderSmall::class.java).apply {
                action = ACTION_REFRESH
                putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
            }
            val flags = PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            val pendingIntent = PendingIntent.getBroadcast(
                context, appWidgetId, refreshIntent, flags
            )
            views.setOnClickPendingIntent(R.id.widget_root, pendingIntent)

            val configureIntent = Intent(context, WidgetConfigureSmallActivity::class.java).apply {
                action = AppWidgetManager.ACTION_APPWIDGET_CONFIGURE
                putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
            }
            val configurePendingIntent = PendingIntent.getActivity(
                context, appWidgetId, configureIntent, flags
            )
            views.setOnClickPendingIntent(R.id.settings_button, configurePendingIntent)

            val url = WidgetPrefs.loadUrl(context, appWidgetId)
            val type = WidgetPrefs.loadType(context, appWidgetId, slot = 1, defaultValue = "")
            views.setTextViewText(R.id.label_text, type.ifBlank { "ぴよログAPI" })

            if (url.isNullOrEmpty()) {
                views.setTextViewText(R.id.time_ago_text, "URL未設定")
                views.setTextViewText(R.id.updated_text, "タップして設定")
                appWidgetManager.updateAppWidget(appWidgetId, views)
                return
            }

            views.setTextViewText(R.id.time_ago_text, "更新待機中…")
            appWidgetManager.updateAppWidget(appWidgetId, views)

            val request = OneTimeWorkRequestBuilder<WidgetRefreshWorker>()
                .setConstraints(
                    Constraints.Builder()
                        .setRequiredNetworkType(NetworkType.CONNECTED)
                        .build()
                )
                .setInputData(workDataOf(WidgetRefreshWorker.KEY_APP_WIDGET_ID to appWidgetId))
                .build()
            WorkManager.getInstance(context.applicationContext).enqueueUniqueWork(
                "widget-refresh-$appWidgetId",
                ExistingWorkPolicy.REPLACE,
                request
            )
        }

        /** Scale the three labels from the smallest side of the resized widget. */
        private fun applyTextSizes(
            appWidgetManager: AppWidgetManager,
            appWidgetId: Int,
            views: RemoteViews
        ) {
            val options = appWidgetManager.getAppWidgetOptions(appWidgetId)
            val smallestSide = minOf(
                options.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_WIDTH, 40),
                options.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_HEIGHT, 40)
            )
            val scale = (smallestSide / 40f).coerceIn(1f, 2f)

            views.setTextViewTextSize(
                R.id.label_text, android.util.TypedValue.COMPLEX_UNIT_SP, 8f * scale
            )
            views.setTextViewTextSize(
                R.id.time_ago_text, android.util.TypedValue.COMPLEX_UNIT_SP, 12f * scale
            )
            views.setTextViewTextSize(
                R.id.updated_text, android.util.TypedValue.COMPLEX_UNIT_SP, 6f * scale
            )
        }
    }

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        for (appWidgetId in appWidgetIds) {
            updateWidget(context, appWidgetManager, appWidgetId)
        }
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        if (intent.action == ACTION_REFRESH) {
            val appWidgetId = intent.getIntExtra(
                AppWidgetManager.EXTRA_APPWIDGET_ID,
                AppWidgetManager.INVALID_APPWIDGET_ID
            )
            if (appWidgetId != AppWidgetManager.INVALID_APPWIDGET_ID) {
                updateWidget(context, AppWidgetManager.getInstance(context), appWidgetId)
            }
        }
    }

    override fun onAppWidgetOptionsChanged(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetId: Int,
        newOptions: android.os.Bundle
    ) {
        updateWidget(context, appWidgetManager, appWidgetId)
    }

    override fun onDeleted(context: Context, appWidgetIds: IntArray) {
        for (appWidgetId in appWidgetIds) {
            WidgetPrefs.deleteAll(context, appWidgetId)
        }
    }
}
