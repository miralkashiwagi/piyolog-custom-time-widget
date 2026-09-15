package com.example.custom1widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.os.Build
import android.view.View
import android.widget.RemoteViews
import java.util.concurrent.Executors

class WidgetProviderLarge : AppWidgetProvider() {

    companion object {
        const val ACTION_REFRESH = "com.example.custom1widget.ACTION_REFRESH_LARGE"
        private val executor = Executors.newCachedThreadPool()

        private val labelIds = intArrayOf(R.id.label_text_1, R.id.label_text_2, R.id.label_text_3)
        private val valueIds = intArrayOf(R.id.time_ago_text_1, R.id.time_ago_text_2, R.id.time_ago_text_3)

        fun updateWidget(context: Context, appWidgetManager: AppWidgetManager, appWidgetId: Int) {
            val views = RemoteViews(context.packageName, R.layout.widget_large)

            val refreshIntent = Intent(context, WidgetProviderLarge::class.java).apply {
                action = ACTION_REFRESH
                putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
            }
            val flags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            } else {
                PendingIntent.FLAG_UPDATE_CURRENT
            }
            val pendingIntent = PendingIntent.getBroadcast(
                context, appWidgetId, refreshIntent, flags
            )
            views.setOnClickPendingIntent(R.id.widget_root, pendingIntent)

            val url = WidgetPrefs.loadUrl(context, appWidgetId)
            val types = (1..3).map { slot -> WidgetPrefs.loadType(context, appWidgetId, slot) }

            // typeが空欄の行は隠す
            for (i in 0..2) {
                val visible = types[i].isNotBlank()
                views.setViewVisibility(labelIds[i], if (visible) View.VISIBLE else View.GONE)
                views.setViewVisibility(valueIds[i], if (visible) View.VISIBLE else View.GONE)
                if (visible) {
                    views.setTextViewText(labelIds[i], types[i])
                }
            }

            if (url.isNullOrEmpty()) {
                views.setTextViewText(R.id.updated_text, "タップして設定してください")
                for (i in 0..2) {
                    if (types[i].isNotBlank()) views.setTextViewText(valueIds[i], "URL未設定")
                }
                appWidgetManager.updateAppWidget(appWidgetId, views)
                return
            }

            for (i in 0..2) {
                if (types[i].isNotBlank()) views.setTextViewText(valueIds[i], "更新中…")
            }
            appWidgetManager.updateAppWidget(appWidgetId, views)

            executor.execute {
                try {
                    val json = TrackerCore.fetchJson(url)
                    for (i in 0..2) {
                        if (types[i].isBlank()) continue
                        val latest = TrackerCore.findLatestByType(json, types[i])
                        views.setTextViewText(valueIds[i], TrackerCore.formatResult(latest))
                    }
                    views.setTextViewText(R.id.updated_text, "更新 ${TrackerCore.nowClockLabel()}")
                } catch (e: Exception) {
                    for (i in 0..2) {
                        if (types[i].isNotBlank()) views.setTextViewText(valueIds[i], "取得失敗")
                    }
                    views.setTextViewText(R.id.updated_text, e.message ?: "エラー")
                } finally {
                    appWidgetManager.updateAppWidget(appWidgetId, views)
                }
            }
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

    override fun onDeleted(context: Context, appWidgetIds: IntArray) {
        for (appWidgetId in appWidgetIds) {
            WidgetPrefs.deleteAll(context, appWidgetId)
        }
    }
}
