package com.example.piyologtimewidget

import android.appwidget.AppWidgetManager
import android.content.Context
import android.widget.RemoteViews
import androidx.work.Worker
import androidx.work.WorkerParameters
import java.io.IOException

/**
 * ネットワーク接続が利用できる時だけ実行される、ウィジェットの取得処理。
 *
 * AppWidgetProvider の onUpdate/onReceive は短時間で終了する必要があるため、そこで
 * 通信を継続するとスリープ復帰時にプロセスが終了して取得失敗になることがある。
 */
class WidgetRefreshWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : Worker(appContext, workerParams) {

    companion object {
        const val KEY_APP_WIDGET_ID = "app_widget_id"
    }

    override fun doWork(): Result {
        val appWidgetId = inputData.getInt(
            KEY_APP_WIDGET_ID,
            AppWidgetManager.INVALID_APPWIDGET_ID
        )
        if (appWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID) return Result.failure()

        val appWidgetManager = AppWidgetManager.getInstance(applicationContext)
        val url = WidgetPrefs.loadUrl(applicationContext, appWidgetId)
        val type = WidgetPrefs.loadType(applicationContext, appWidgetId, slot = 1)
        if (url.isNullOrEmpty()) return Result.success()

        val views = RemoteViews(applicationContext.packageName, R.layout.widget_small)
        views.setTextViewText(R.id.label_text, type.ifBlank { "ぴよログAPI" })

        return try {
            val json = TrackerCore.fetchJson(url)
            val latest = TrackerCore.findLatestByType(json, type)
            views.setTextViewText(R.id.time_ago_text, TrackerCore.formatResult(latest))
            views.setTextViewText(R.id.updated_text, "更新 ${TrackerCore.nowClockLabel()}")
            appWidgetManager.updateAppWidget(appWidgetId, views)
            Result.success()
        } catch (e: IOException) {
            views.setTextViewText(R.id.time_ago_text, "ネットワーク再接続待ち…")
            views.setTextViewText(R.id.updated_text, "再試行します")
            appWidgetManager.updateAppWidget(appWidgetId, views)
            Result.retry()
        } catch (e: Exception) {
            views.setTextViewText(R.id.time_ago_text, "取得失敗")
            views.setTextViewText(R.id.updated_text, e.message ?: "エラー")
            appWidgetManager.updateAppWidget(appWidgetId, views)
            Result.failure()
        }
    }
}
