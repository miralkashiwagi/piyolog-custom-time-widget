package com.example.custom1widget

import android.app.Activity
import android.appwidget.AppWidgetManager
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast

class WidgetConfigureLargeActivity : Activity() {

    private var appWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setResult(RESULT_CANCELED)
        setContentView(R.layout.activity_configure_large)

        appWidgetId = intent?.extras?.getInt(
            AppWidgetManager.EXTRA_APPWIDGET_ID,
            AppWidgetManager.INVALID_APPWIDGET_ID
        ) ?: AppWidgetManager.INVALID_APPWIDGET_ID

        if (appWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID) {
            finish()
            return
        }

        val urlInput = findViewById<EditText>(R.id.url_input)
        val typeInput1 = findViewById<EditText>(R.id.type_input_1)
        val typeInput2 = findViewById<EditText>(R.id.type_input_2)
        val typeInput3 = findViewById<EditText>(R.id.type_input_3)
        val saveButton = findViewById<Button>(R.id.save_button)

        WidgetPrefs.loadUrl(this, appWidgetId)?.let { urlInput.setText(it) }
        WidgetPrefs.loadType(this, appWidgetId, 1, "").let { if (it.isNotEmpty()) typeInput1.setText(it) }
        WidgetPrefs.loadType(this, appWidgetId, 2, "").let { if (it.isNotEmpty()) typeInput2.setText(it) }
        WidgetPrefs.loadType(this, appWidgetId, 3, "").let { if (it.isNotEmpty()) typeInput3.setText(it) }

        saveButton.setOnClickListener {
            val url = urlInput.text.toString().trim()
            val type1 = typeInput1.text.toString().trim()
            val type2 = typeInput2.text.toString().trim()
            val type3 = typeInput3.text.toString().trim()

            if (url.isEmpty() || !(url.startsWith("http://") || url.startsWith("https://"))) {
                Toast.makeText(this, "正しいURLを入力してください", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (type1.isEmpty() && type2.isEmpty() && type3.isEmpty()) {
                Toast.makeText(this, "typeを1つ以上入力してください", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            WidgetPrefs.saveUrl(this, appWidgetId, url)
            WidgetPrefs.saveType(this, appWidgetId, 1, type1)
            WidgetPrefs.saveType(this, appWidgetId, 2, type2)
            WidgetPrefs.saveType(this, appWidgetId, 3, type3)

            val appWidgetManager = AppWidgetManager.getInstance(this)
            WidgetProviderLarge.updateWidget(this, appWidgetManager, appWidgetId)

            val resultValue = Intent().putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
            setResult(RESULT_OK, resultValue)
            finish()
        }
    }
}
