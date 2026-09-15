package com.example.custom1widget

import android.content.Context

/**
 * ウィジェットIDごとにURLと表示対象のtype名を保存する。
 * slotはtype欄の番号（小ウィジェットは1つだけなのでslot=1固定、大ウィジェットはslot=1,2,3を使用）。
 */
object WidgetPrefs {
    private const val PREFS_NAME = "com.example.custom1widget.WidgetPrefs"
    private const val KEY_URL = "appwidget_url_"
    private const val KEY_TYPE = "appwidget_type_" // + "${slot}_${appWidgetId}"

    fun saveUrl(context: Context, appWidgetId: Int, url: String) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).edit()
            .putString(KEY_URL + appWidgetId, url)
            .apply()
    }

    fun loadUrl(context: Context, appWidgetId: Int): String? {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getString(KEY_URL + appWidgetId, null)
    }

    fun saveType(context: Context, appWidgetId: Int, slot: Int, type: String) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).edit()
            .putString(KEY_TYPE + slot + "_" + appWidgetId, type)
            .apply()
    }

    /** 未設定の場合はdefaultValueを返す（小ウィジェットのデフォルトはCustom1） */
    fun loadType(context: Context, appWidgetId: Int, slot: Int, defaultValue: String = ""): String {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getString(KEY_TYPE + slot + "_" + appWidgetId, null) ?: defaultValue
    }

    fun deleteAll(context: Context, appWidgetId: Int) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).edit()
        prefs.remove(KEY_URL + appWidgetId)
        for (slot in 1..3) {
            prefs.remove(KEY_TYPE + slot + "_" + appWidgetId)
        }
        prefs.apply()
    }
}
