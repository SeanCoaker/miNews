package com.coaker.newsaggregatorapp

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.util.Log
import android.view.Menu
import android.view.View
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import java.io.File
import java.io.PrintWriter
import java.util.*

/**
 * A class used to display an article to a user in a webview.
 *
 * @author Sean Coaker (986529)
 * @since 1.0
 */
class ArticleWebViewActivity : AppCompatActivity() {
    private var url = ""
    private var content = ""
    private var headline = ""
    private var source = ""
    private var date = ""
    private var tts: TextToSpeech? = null


    /**
     * A method called when the activity is being created. This method gets data needed from the intent
     * and also configures the webview.
     *
     * @param[savedInstanceState] Any previous saved instance of the activity.
     */
    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.content_article_webview)

        val intent = intent
        url = intent.getStringExtra("url")!!
        content = intent.getStringExtra("content")!!
        headline = intent.getStringExtra("headline")!!
        source = intent.getStringExtra("source")!!
        date = intent.getStringExtra("date")!!

        val webView = findViewById<WebView>(R.id.WebView)

        // Used to fix a slow scrolling webview in older android versions.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            webView.setLayerType(View.LAYER_TYPE_HARDWARE, null)
        } else {
            webView.setLayerType(View.LAYER_TYPE_SOFTWARE, null)
        }

        webView.apply {
            settings.apply {
                // Allows media to be displayed in the webview.
                javaScriptEnabled = true
                cacheMode = WebSettings.LOAD_NO_CACHE

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
        }

        webView.loadUrl(url)

        webView.settings.mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW;

        supportActionBar!!.title = ""
    }


    /**
     * A method to setup the options menu. When the options menu is created, we setup the onclick
     * listener for the audio button, share button and save button.
     *
     * @param[menu] The menu to be edited.
     * @param[inflater] Used to inflate the menu.
     */
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.article, menu)

        val audioButton = menu.findItem(R.id.action_audio)
        val shareButton = menu.findItem(R.id.action_share)
        val saveButton = menu.findItem(R.id.action_save)

        // Configures the text to speech feature.
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

        /*
        Configures the audio button so that when it's clicked and text to speech isn't playing, text
        to speech will start reading the content of the article. If the text to speech is playing when
        the audio button is clicked, then text to speech is stopped.
         */
        audioButton.setOnMenuItemClickListener {

            if (tts!!.isSpeaking) {
                tts!!.stop()
                audioButton.icon = ContextCompat.getDrawable(this, R.drawable.ic_baseline_volume_up_24)
            } else {
                audioButton.icon = ContextCompat.getDrawable(this, R.drawable.ic_baseline_volume_off_24)

                val strings = content.chunked(4000)

                strings.forEach {
                    tts!!.speak(it, TextToSpeech.QUEUE_ADD, null, "")
                }


            }

            true
        }

        /*
        Configures the share button so that when clicked, the user will be shown a screen which will
        allow them to share the url of the article in another app.
         */
        shareButton.setOnMenuItemClickListener {
            val shareIntent = Intent(Intent.ACTION_SEND)
            shareIntent.type = "text/plain"
            shareIntent.putExtra(Intent.EXTRA_TEXT, url)
            startActivity(Intent.createChooser(shareIntent, "Share Link"))

            true
        }

        /*
        Configures the save button so that the user can save the article to view it again later.
         */
        saveButton.setOnMenuItemClickListener {
            saveArticle()

            true
        }

        return true
    }


    /**
     * Displays a back button in the activity's action bar so that the user can navigate to where
     * they were previously.
     */
    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }


    /**
     * A method used to save an article to the device's internal storage. The file is saved in the
     * user's own directory so that only they can access their saved articles.
     */
    private fun saveArticle() {
        val userId = Firebase.auth.currentUser!!.uid

        val dir = File(filesDir, "saved articles - $userId")
        var writer: PrintWriter? = null

        if (!dir.exists()) {
            dir.mkdir()
        }

        try {
            val file = File(dir, "$headline.txt")
            writer = file.printWriter()
            writer.use { out ->
                out.println(url)
                out.println(source)
                out.println(headline)
                out.println(date)
                out.println(content)
            }

            Toast.makeText(this, "Article Saved", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }


    /**
     * A method called when the activity is paused. This method will stop the text to speech.
     */
    override fun onPause() {
        super.onPause()

        tts!!.stop()
    }

}