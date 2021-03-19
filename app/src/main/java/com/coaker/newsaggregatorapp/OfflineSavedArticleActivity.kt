package com.coaker.newsaggregatorapp

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.util.Log
import android.view.Menu
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import java.text.SimpleDateFormat
import java.util.*

/**
 * A class used to display an article to a user in a generic view when they're offline.
 *
 * @author Sean Coaker (986529)
 * @since 1.0
 */
class OfflineSavedArticleActivity: AppCompatActivity() {

    private var content: String? = null
    private var url: String? = null
    private var tts: TextToSpeech? = null


    /**
     * A method called when the activity is being created. This method gets data needed from the intent
     * and displays the values in the generic offline article layout.
     *
     * @param[savedInstanceState] Any previous saved instance of the activity.
     */
    @SuppressLint("SimpleDateFormat")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.saved_article_offline_layout)

        val intent = intent

        url = intent.getStringExtra("url")
        content = intent.getStringExtra("content")
        val headline = intent.getStringExtra("headline")
        val source = intent.getStringExtra("source")
        val date = intent.getStringExtra("date")

        val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'")
        val stringDate = sdf.parse(date!!)

        val textViewHeadline: TextView = findViewById(R.id.textViewHeadline)
        val textViewSource: TextView = findViewById(R.id.textViewSource)
        val textViewDate: TextView = findViewById(R.id.textViewDate)
        val textViewContent: TextView = findViewById(R.id.textViewContent)

        textViewHeadline.text = headline
        textViewContent.text = content
        textViewSource.text = source
        textViewDate.text = stringDate!!.toString()
    }


    /**
     * A method to setup the options menu. When the options menu is created, we setup the onclick
     * listener for the audio button and share button.
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
        saveButton.isVisible = false

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

                val strings = content!!.chunked(4000)

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
     * A method called when the activity is paused. This method will stop the text to speech.
     */
    override fun onPause() {
        super.onPause()

        tts!!.stop()
    }
}