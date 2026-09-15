package com.example.piyologtimewidget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.os.Build
import android.widget.RemoteViews
import java.util.concurrent.Executors

class WidgetProviderSmall : AppWidgetProvider() {

    companion object {
        const val ACTION_REFRESH = "com.example.piyologtimewidget.ACTION_REFRESH_SMALL"
        private val executor = Executors.newCachedThreadPool()

        fun updateWidget(context: Context, appWidgetManager: AppWidgetManager, appWidgetId: Int) {
            val views = RemoteViews(context.packageName, R.layout.widget_small)

            val refreshIntent = Intent(context, WidgetProviderSmall::class.java).apply {
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

            executor.execute {
                var shouldUpdateWidget = true
                try {
                    val json = TrackerCore.fetchJson(url)
                    val generatedAt = json.optString("generated_at", "")
                    if (
                        generatedAt.isNotEmpty() &&
                        generatedAt == WidgetPrefs.loadGeneratedAt(context, appWidgetId)
                    ) {
                        shouldUpdateWidget = false
                        return@execute
                    }

                    val latest = TrackerCore.findLatestByType(json, type)
                    views.setTextViewText(R.id.time_ago_text, TrackerCore.formatResult(latest))
                    views.setTextViewText(R.id.updated_text, "取得 ${TrackerCore.nowClockLabel()}")
                    if (generatedAt.isNotEmpty()) {
                        WidgetPrefs.saveGeneratedAt(context, appWidgetId, generatedAt)
                    }
                } catch (e: Exception) {
                    views.setTextViewText(R.id.time_ago_text, "取得失敗")
                    views.setTextViewText(R.id.updated_text, e.message ?: "エラー")
                } finally {
                    if (shouldUpdateWidget) {
                        appWidgetManager.updateAppWidget(appWidgetId, views)
                    }
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
