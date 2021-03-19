package com.coaker.newsaggregatorapp

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.widget.VideoView
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth

/**
 * An activity class that displays the app's logo in a video on app startup.
 *
 * @author Sean Coaker (986529)
 * @since 1.0
 */
class SplashAnimatedActivity: AppCompatActivity() {


    /**
     * This method is called when the activity is being created. The method checks if a user is logged
     * in. If they are then the main activity is shown, if not then the login activity is shown. Whilst
     * these 2 activities begin to load, the app's logo is displayed in a splash screen video.
     *
     * @param[savedInstanceState] Any previous saved instance of the activity.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.splash_layout_animated)

        supportActionBar?.hide()

        Handler().postDelayed({
            if (FirebaseAuth.getInstance().currentUser != null) {
                val splashIntent = Intent(this, MainActivity::class.java)
                startActivity(splashIntent)
            } else {
                val splashIntent = Intent(this, LoginActivity::class.java)
                startActivity(splashIntent)
            }

            finish()
        }, 3000)

        val videoView = findViewById<VideoView>(R.id.videoViewSplash)
        videoView.setVideoURI(Uri.parse("android.resource://$packageName/" + R.raw.mi_news_video))
        videoView.setZOrderOnTop(true)
        videoView.start()


    }
}