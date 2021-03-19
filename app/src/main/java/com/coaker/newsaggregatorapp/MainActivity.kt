package com.coaker.newsaggregatorapp

import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.drawerlayout.widget.DrawerLayout
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.coaker.newsaggregatorapp.ui.keywords.Keyword
import com.facebook.login.LoginManager
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.navigation.NavigationView
import com.google.android.material.tabs.TabLayout
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import com.squareup.picasso.Picasso
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

/**
 * An activity class that handles the main operations of the application.
 *
 * @author Sean Coaker (986529)
 * @since 1.0
 */
class MainActivity : AppCompatActivity(), MenuItem.OnMenuItemClickListener {
    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var navController: NavController
    private lateinit var navHostFragment: NavHostFragment
    private lateinit var progressBar: ProgressBar
    private lateinit var drawerLayout: DrawerLayout

    private var name: String? = null
    private var email: String? = null
    private var image: Uri? = null
    private var isCustom: Boolean? = null
    private val db = FirebaseFirestore.getInstance()

    lateinit var tabLayout: TabLayout
    lateinit var navView: NavigationView


    /**
     * A method called when the activity is being created. This method sets up the navigation controller
     * and the drawer layout.
     *
     * @param[savedInstanceState] Any previous saved instance of the activity.
     */
    @SuppressLint("SimpleDateFormat")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)

        isCustom = intent.getBooleanExtra("custom", false)

        progressBar = findViewById(R.id.progressBar)
        progressBar.visibility = View.VISIBLE

        tabLayout = findViewById(R.id.tabLayout)

        val user = Firebase.auth.currentUser
        user!!.reload()

        name = user.displayName.toString()
        email = user.email
        image = user.photoUrl

        drawerLayout = findViewById(R.id.drawer_layout)
        navView = findViewById(R.id.nav_view)

        navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        navController = navHostFragment.navController

        // Passing each menu ID as a set of Ids
        appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.nav_home,
                R.id.nav_saved,
                R.id.nav_crosswords,
                R.id.nav_keywords
            ), drawerLayout
        )
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)

        val logoutItem = navView.menu.findItem(R.id.nav_logout)
        logoutItem.setOnMenuItemClickListener(this)

        val drawerImage = navView.getHeaderView(0).findViewById<ImageView>(R.id.drawerImage)

        if (FirebaseAuth.getInstance().currentUser!!.providerData[1].providerId == EmailAuthProvider.PROVIDER_ID) {
            drawerImage.setImageResource(R.drawable.ic_baseline_person_24)
        } else {
            Picasso.get().load(image).resize(128, 128).into(drawerImage)
        }

    }


    /**
     * This method will stop our notification timer so that the user doesn't get notifications when
     * using this activity.
     */
    override fun onStart() {
        super.onStart()

        val receiver = ComponentName(this, AlarmReceiver::class.java)
        val packageManager = packageManager

        packageManager.setComponentEnabledSetting(
            receiver,
            PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
            PackageManager.DONT_KILL_APP
        )
    }


    /**
     * Allows the user to navigate back to this activity if they have moved to another activity.
     */
    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment)
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }


    /**
     * Creates an intent that allows the user to view their selected article in the web view activity.
     *
     * @param[articleToShow] The position of the article to show in the list of articles.
     */
    fun showArticle(articleToShow: Int) {

        if (Variables.isConnected) {
            val newIntent = Intent(this, ArticleWebViewActivity::class.java)

            newIntent.putExtra("url",Variables.articleList[articleToShow].url.toString())
            newIntent.putExtra("headline", Variables.articleList[articleToShow].title)
            newIntent.putExtra("source", Variables.articleList[articleToShow].source)
            newIntent.putExtra("date", Variables.articleList[articleToShow].publishedAt)
            newIntent.putExtra("content", Variables.articleList[articleToShow].content)

            startActivity(newIntent)
        } else {
            Toast.makeText(this, "You're not connected to a network.",
                Toast.LENGTH_SHORT).show()
        }

    }


    /**
     * Sets up the logout menu item to allow the user to logout correctly.
     *
     * @param[item] The menu item clicked.
     *
     * @return[Boolean] True if the user logged out and false otherwise.
     */
    override fun onMenuItemClick(item: MenuItem?): Boolean {
        when (item!!.itemId) {
            R.id.nav_logout -> {

                logout()

                return true
            }
        }
        return false
    }


    /**
     * This method is used to make sure that when the user adds keywords, firestore is updated.
     *
     * @param[requestCode] The request code sent with the startActivityForResult
     * @param[resultCode] The code returned from the intent.
     * @param[data] The data returned from the intent.
     */
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if ((requestCode == 7375 || requestCode == 7376) && resultCode == Activity.RESULT_OK) {
            if (data!!.hasExtra("keywordsList")) {
                Variables.keywordsList =
                    data.getParcelableArrayListExtra<Keyword>("keywordsList") as ArrayList<Keyword>

                updateFirebase()
            }

            if (requestCode == 7375 && Variables.keywordsList.isEmpty()) {
                findViewById<FloatingActionButton>(R.id.addTopicsFab).visibility = View.VISIBLE
                findViewById<TextView>(R.id.addTopicsTextView).visibility = View.VISIBLE
            }
        }
    }


    /**
     * This method updates the user's list of keywords stored in firestore.
     */
    fun updateFirebase() {

        val users = db.collection("users")

        users.document(Firebase.auth.uid.toString()).set(mapOf("keywords" to Variables.keywordsList))
    }


    /**
     * This starts the KeywordSelectionActivity which allows the user to add keywords to view news stories about.
     *
     * @param[requestCode] Denotes whether the call was made from the home fragment or keywords fragment.
     */
    fun startKeywordSelection(requestCode: Int) {
        val intent = Intent(this, KeywordSelectionActivity::class.java)
        intent.putParcelableArrayListExtra("keywordsList", Variables.keywordsList)
        startActivityForResult(intent, requestCode)
    }


    /**
     * This method logs the user out of their account and returns them to the login screen.
     */
    fun logout() {
        FirebaseAuth.getInstance().signOut()
        LoginManager.getInstance().logOut()
        Variables.articleList.clear()
        Variables.keywordsList.clear()
        val logoutIntent = Intent(this, LoginActivity::class.java)
        logoutIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(logoutIntent)
        finish()
    }


    /**
     * This method ensures that an alarm manager is setup when the activity is stopped. The alarm
     * manager ensures that a notification is sent to the user every hour.
     */
    override fun onStop() {
        super.onStop()

        val notifyIntent = Intent(this, AlarmReceiver::class.java)
        val pendingIntent =
            PendingIntent.getBroadcast(this, 7, notifyIntent, PendingIntent.FLAG_UPDATE_CURRENT)
        val alarmManager = this.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        alarmManager.setRepeating(
            AlarmManager.RTC_WAKEUP,
            System.currentTimeMillis() + (1000 * 60 * 60),
            1000 * 60 * 60, pendingIntent
        )
    }
}

