package com.coaker.newsaggregatorapp

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.widget.VideoView
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth

class SplashAnimatedActivity: AppCompatActivity() {
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