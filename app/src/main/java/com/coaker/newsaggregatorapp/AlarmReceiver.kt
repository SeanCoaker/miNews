package com.coaker.newsaggregatorapp

import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.coaker.newsaggregatorapp.ui.keywords.Keyword
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import okhttp3.*
import org.json.JSONObject
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

/**
 * A class that inherits BroadcastReciever in order to send notifications to the user, even when the
 * app is closed.
 *
 * @author Sean Coaker (986529)
 * @since 1.0
 */
class AlarmReceiver : BroadcastReceiver() {

    private var context: Context? = null
    private val client = OkHttpClient()
    private var iso = ""
    private var keywords = ArrayList<Keyword>()
    private var hasUpdatedKeywords = ArrayList<Keyword>()
    private var count = 0


    /**
     * A method that fetches the keywords stored for the user in firestore and calls the start
     * notification method only if the current time is between 9AM and 9PM.
     *
     * @param[context] The context the receiver is called from
     * @param[intent] The intent the receiver is called from.
     */
    @SuppressLint("SimpleDateFormat")
    override fun onReceive(context: Context?, intent: Intent?) {

        val currentHour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)

        if (currentHour in 9..21) {
            this.context = context

            val timezone = TimeZone.getTimeZone("UTC")
            val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX")
            sdf.timeZone = timezone
            iso = sdf.format(Date(System.currentTimeMillis() - (1000 * 60 * 60)))

            val user = Firebase.auth.currentUser
            val db = FirebaseFirestore.getInstance()

            val docRef = db.collection("users").document(user!!.uid)
            docRef.get().addOnSuccessListener { documentSnapshot ->
                if (documentSnapshot.exists()) {
                    val result = documentSnapshot.toObject(UserDocument::class.java)
                    keywords = result!!.keywords!!
                    startNotification()
                }
            }.addOnFailureListener {
                Log.i("Firestore Read: ", "Failed")
            }
        }
    }


    /**
     * A method used to start the notification process. This method goes through each keyword checking
     * if there are new stories for that keyword in the past hour. If there are 2 keywords that have
     * new stories in the past hour, then the loop stops to limit the number of API calls made.
     */
    private fun startNotification() {

        if (count < keywords.size) {
            if (keywords[count].isNotifier == true) {
                val urlPersonal = "https://newsapi.org/v2/everything?q=${keywords[count].word}&language=en&from=$iso&sortBy=relevancy&apiKey=d4fd6b189c7d4ac4afa1f5ac86f9df5d"
                val urlUni =
                    "https://newsapi.org/v2/everything?q=${keywords[count].word}&language=en&from=$iso&sortBy=relevancy&apiKey=b5c1da042e234be5b00bd666e41b160d"
                val request = Request.Builder().url(urlUni).build()

                if (hasUpdatedKeywords.size < 2) {
                    client.newCall(request).enqueue(object : Callback {
                        override fun onFailure(call: Call, e: IOException) {
                            e.printStackTrace()
                        }

                        override fun onResponse(call: Call, response: Response) {
                            response.use {
                                if (response.isSuccessful) {
                                    val jsonResponse = response.body()!!.string()
                                    val jsonObject = JSONObject(jsonResponse)

                                    println(jsonObject.toString(5))

                                    val results = jsonObject.getInt("totalResults")

                                    if (results > 0) hasUpdatedKeywords.add(keywords[count])

                                    count++

                                    startNotification()
                                }
                            }
                        }
                    })
                } else {
                    setupNotification()
                }
            } else {
                count++
                startNotification()
            }
        } else {
            setupNotification()
        }
    }


    /**
     * A method to build the notification to be sent to the user. If there is new news for only one
     * keyword, then a notification is sent notifying the user of new stories for that keyword. If
     * there is more than 1 keyword with new stories, then a generic notification is sent to the
     * user notifying them of new stories that they are interested in. This was to avoid
     * bombarding the user with notifications.
     */
    private fun setupNotification() {

        val channelId = "com.coaker.newsaggregator.miNews"
        val builder = NotificationCompat.Builder(context!!, channelId)

        val notificationChannel = NotificationChannel(channelId, "miNews", NotificationManager.IMPORTANCE_HIGH)
        notificationChannel.enableLights(true)
        notificationChannel.lightColor = Color.BLUE
        notificationChannel.setShowBadge(true)
        notificationChannel.lockscreenVisibility = Notification.VISIBILITY_PUBLIC

        if (hasUpdatedKeywords.size > 1) {
            builder.setSmallIcon(R.drawable.mi_news_logo)
                .setContentTitle("New News Stories")
                .setContentText("New news stories that you are interested in are available.")
                .setSmallIcon(R.drawable.app_icon_png)
                .setAutoCancel(true)
                .priority = NotificationCompat.PRIORITY_DEFAULT

            val notificationManager: NotificationManager =
                context!!.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(notificationChannel)

            val notifyIntent = Intent(context, MainActivity::class.java)
            val pendingIntent = PendingIntent.getActivity(
                context,
                7,
                notifyIntent,
                PendingIntent.FLAG_UPDATE_CURRENT
            )
            builder.setContentIntent(pendingIntent)

            with(NotificationManagerCompat.from(context!!)) {
                notify(3, builder.build())
            }

        } else if (hasUpdatedKeywords.size == 1) {
            val keyword = hasUpdatedKeywords[0].word

            builder.setSmallIcon(R.drawable.mi_news_logo)
                .setContentTitle("New $keyword Stories")
                .setContentText("New stories for $keyword are available.")
                .setSmallIcon(R.drawable.app_icon_png)
                .setAutoCancel(true)
                .priority = NotificationCompat.PRIORITY_DEFAULT

            val notificationManager: NotificationManager =
                context!!.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(notificationChannel)

            val notifyIntent = Intent(context, MainActivity::class.java)
            val pendingIntent = PendingIntent.getActivity(
                context,
                7,
                notifyIntent,
                PendingIntent.FLAG_UPDATE_CURRENT
            )
            builder.setContentIntent(pendingIntent)

            with(NotificationManagerCompat.from(context!!)) {
                notify(3, builder.build())
            }
        }

    }
}