package com.example.piyologtimewidget

import android.app.Activity
import android.appwidget.AppWidgetManager
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast

class WidgetConfigureSmallActivity : Activity() {

    private var appWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setResult(RESULT_CANCELED)
        setContentView(R.layout.activity_configure_small)

        appWidgetId = intent?.extras?.getInt(
            AppWidgetManager.EXTRA_APPWIDGET_ID,
            AppWidgetManager.INVALID_APPWIDGET_ID
        ) ?: AppWidgetManager.INVALID_APPWIDGET_ID

        if (appWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID) {
            finish()
            return
        }

        val urlInput = findViewById<EditText>(R.id.url_input)
        val typeInput = findViewById<EditText>(R.id.type_input)
        val saveButton = findViewById<Button>(R.id.save_button)

        WidgetPrefs.loadUrl(this, appWidgetId)?.let { urlInput.setText(it) }
        val savedType = WidgetPrefs.loadType(this, appWidgetId, slot = 1, defaultValue = "")
        if (savedType.isNotEmpty()) typeInput.setText(savedType)

        saveButton.setOnClickListener {
            val url = urlInput.text.toString().trim()
            val type = typeInput.text.toString().trim()

            if (url.isEmpty() || !(url.startsWith("http://") || url.startsWith("https://"))) {
                Toast.makeText(this, "正しいURLを入力してください", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (type.isEmpty()) {
                Toast.makeText(this, "typeを入力してください", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            WidgetPrefs.saveUrl(this, appWidgetId, url)
            WidgetPrefs.saveType(this, appWidgetId, slot = 1, type = type)

            val appWidgetManager = AppWidgetManager.getInstance(this)
            WidgetProviderSmall.updateWidget(this, appWidgetManager, appWidgetId)

            val resultValue = Intent().putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
            setResult(RESULT_OK, resultValue)
            finish()
        }
    }
}
