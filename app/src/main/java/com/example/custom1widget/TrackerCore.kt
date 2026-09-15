package com.example.custom1widget

import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

/**
 * 複数のウィジェット(小/大)から共有される、JSON取得〜表示文字列組み立てのロジック。
 */
object TrackerCore {

    data class RecordResult(val text: String, val isError: Boolean)

    private val clockFormatter: DateTimeFormatter =
        DateTimeFormatter.ofPattern("HH:mm").withZone(ZoneId.systemDefault())

    fun fetchJson(urlString: String): JSONObject {
        val connection = URL(urlString).openConnection() as HttpURLConnection
        connection.requestMethod = "GET"
        connection.connectTimeout = 10000
        connection.readTimeout = 10000
        connection.setRequestProperty("Accept", "application/json")
        try {
            val code = connection.responseCode
            if (code !in 200..299) {
                throw RuntimeException("HTTP $code")
            }
            val body = connection.inputStream.bufferedReader().use { it.readText() }
            return JSONObject(body)
        } finally {
            connection.disconnect()
        }
    }

    /** records配列の中から、指定typeで最も新しいdatetimeを返す */
    fun findLatestByType(json: JSONObject, type: String): Instant? {
        val records = json.optJSONArray("records") ?: return null
        var latest: Instant? = null
        for (i in 0 until records.length()) {
            val record = records.optJSONObject(i) ?: continue
            if (record.optString("type") != type) continue
            val dtString = record.optString("datetime", "")
            if (dtString.isEmpty()) continue
            val instant = try {
                Instant.parse(dtString) // 例: 2026-09-14T04:35:00.000Z
            } catch (e: Exception) {
                continue
            }
            if (latest == null || instant.isAfter(latest)) {
                latest = instant
            }
        }
        return latest
    }

    /** "3時間15分前 (14:20)" のような表示文字列を作る */
    fun formatResult(latest: Instant?): String {
        if (latest == null) return "記録なし"
        val agoSeconds = Instant.now().epochSecond - latest.epochSecond
        val hours = if (agoSeconds < 0) 0 else agoSeconds / 3600
        val minutes = if (agoSeconds < 0) 0 else (agoSeconds % 3600) / 60
        val agoText = if (hours <= 0) "${minutes}分前" else "${hours}時間${minutes}分前"
        val clockText = clockFormatter.format(latest)
        return "$agoText ($clockText)"
    }

    fun nowClockLabel(): String = clockFormatter.format(Instant.now())
}
