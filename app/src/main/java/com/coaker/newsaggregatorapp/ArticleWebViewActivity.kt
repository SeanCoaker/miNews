package com.coaker.newsaggregatorapp

import android.annotation.SuppressLint
import android.net.http.SslError
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.util.Log
import android.view.Menu
import android.view.View
import android.webkit.SslErrorHandler
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebView.RENDERER_PRIORITY_BOUND
import android.webkit.WebViewClient
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.webkit.WebViewCompat
import androidx.webkit.WebViewFeature
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import org.jsoup.Jsoup
import java.util.*

class ArticleWebViewActivity : AppCompatActivity() {
    private lateinit var url: String
    var content: String? = null

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.content_article_webview)

        val intent = intent
        url = intent.getStringExtra("url")!!
        content = intent.getStringExtra("content")!!

        val webView = findViewById<WebView>(R.id.WebView)

        webView.apply {
            settings.apply {
                javaScriptEnabled = true

                setSupportZoom(true)
                builtInZoomControls = true
                displayZoomControls = false
            }
        }

        webView.webViewClient = object : WebViewClient() {
            override fun shouldOverrideUrlLoading(view: WebView, url: String): Boolean {
                view.loadUrl(url)
                return true
            }

//            override fun onReceivedSslError(
//                view: WebView,
//                handler: SslErrorHandler,
//                error: SslError
//            ) {
//                handler.proceed()
//            }
        }

        webView.loadUrl(url)

        webView.settings.mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW;

        supportActionBar!!.title = "Article"
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.article, menu)

        var tts: TextToSpeech? = null

        val audioButton = menu.findItem(R.id.action_audio)

        tts = TextToSpeech(this, TextToSpeech.OnInitListener {
            if (it == TextToSpeech.SUCCESS) {

                val result = tts!!.setLanguage(Locale.UK)
                if (result == TextToSpeech.LANG_MISSING_DATA
                    || result == TextToSpeech.LANG_NOT_SUPPORTED
                ) {
                    Log.e("TTS", "Language Not Supported")
                } else {
                    audioButton!!.isEnabled = true
                }

            } else {
                Log.e("TTS", "Initialization Failed")
            }
        })

        audioButton.setOnMenuItemClickListener {
            Log.i("HTML", content!!.length.toString())

            val strings = content!!.chunked(4000)

            strings.forEach {
                tts.speak(it, TextToSpeech.QUEUE_ADD, null, "")
            }

            true
        }

        return true
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }

}