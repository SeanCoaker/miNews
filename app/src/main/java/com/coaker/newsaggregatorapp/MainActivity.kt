package com.coaker.newsaggregatorapp

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.coaker.newsaggregatorapp.ui.keywords.Keyword
import com.coaker.newsaggregatorapp.ui.keywords.UserDocument
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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class MainActivity : AppCompatActivity(), MenuItem.OnMenuItemClickListener {
    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var navController: NavController
    private lateinit var navHostFragment: NavHostFragment
    private lateinit var progressBar: ProgressBar
    lateinit var tabLayout: TabLayout

    private var articleList = ArrayList<NewsData>()
    var keywordsList = ArrayList<Keyword>()

    private var name: String? = null
    private var email: String? = null
    private var image: Uri? = null
    private var isCustom: Boolean? = null

    private val client = OkHttpClient()
    private val db = FirebaseFirestore.getInstance()

    @SuppressLint("SimpleDateFormat")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)

        isCustom = intent.getBooleanExtra("custom", false)

        progressBar = findViewById(R.id.progressBar)
        tabLayout = findViewById(R.id.tabLayout)

        val user = Firebase.auth.currentUser
        user!!.reload()

        val docRef = db.collection("users").document(user.uid)
        docRef.get().addOnSuccessListener { documentSnapshot ->
            if (documentSnapshot.exists()) {
                val result = documentSnapshot.toObject(UserDocument::class.java)
                keywordsList = result!!.keywords!!
            }
            setupTabLayout()
        }.addOnFailureListener {
            Log.i("Firestore Read: ", "Failed")
        }

        name = user.displayName.toString()
        email = user.email
        image = user.photoUrl

        val drawerLayout: DrawerLayout = findViewById(R.id.drawer_layout)
        val navView: NavigationView = findViewById(R.id.nav_view)

        navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        navController = navHostFragment.navController

        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
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

        if (savedInstanceState != null) {
            articleList = savedInstanceState.getParcelableArrayList("articleList")!!
            tabLayout.getTabAt(savedInstanceState.getInt("tab"))!!.select()
            restoreArticles()
        } else {
            if (tabLayout.getTabAt(0) != null) {
                val sdf = SimpleDateFormat("yyyy-MM-dd")
                val yesterday = Calendar.getInstance()
                yesterday.add(Calendar.DATE, -1)
                sdf.format(yesterday)
                resetArticles(tabLayout.getTabAt(0)!!.text.toString(), sdf.format(yesterday.time))
            }
        }

        val drawerImage = navView.getHeaderView(0).findViewById<ImageView>(R.id.drawerImage)

        if (FirebaseAuth.getInstance().currentUser!!.providerData[1].providerId == EmailAuthProvider.PROVIDER_ID) {
            drawerImage.setImageResource(R.drawable.ic_baseline_person_24)
        } else {
            Picasso.get().load(image).resize(128, 128).into(drawerImage)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.main, menu)
        findViewById<TextView>(R.id.nameText).text = name
        findViewById<TextView>(R.id.emailText).text = email

        return true
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment)
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }

    private fun getArticles(keyword: String, date: String) {
        //val urlPersonal = "https://newsapi.org/v2/everything?q=$keyword&language=en&from=$date&sortBy=relevancy&apiKey=d4fd6b189c7d4ac4afa1f5ac86f9df5d"
        val urlUni =
            "https://newsapi.org/v2/everything?q=$keyword&language=en&from=$date&sortBy=relevancy&apiKey=b5c1da042e234be5b00bd666e41b160d"
        val request = Request.Builder().url(urlUni).build()

        val response = client.newCall(request).execute().body()!!.string()

        val jsonObject = JSONObject(response)
        val jsonArray = jsonObject.getJSONArray("articles")

        for (i in 0 until jsonArray.length()) {
            val jsonArticle = jsonArray.getJSONObject(i)

            val newsData = NewsData()

            val sourceObject = jsonArticle.getJSONObject("source")
            newsData.source = sourceObject.getString("name")

            newsData.author = jsonArticle.getString("author")
            newsData.title = jsonArticle.getString("title")
            newsData.description = jsonArticle.getString("description")
            newsData.url = jsonArticle.getString("url")
            newsData.urlToImage = jsonArticle.getString("urlToImage")
            newsData.publishedAt = jsonArticle.getString("publishedAt")
            newsData.content = jsonArticle.getString("content")

            articleList.add(newsData)
        }
    }

    fun resetArticles(keyword: String, date: String) {
        lifecycleScope.launch {
            val operation = async(Dispatchers.IO) {
                println(date)
                getArticles(keyword, date)
            }
            operation.await()

            val recyclerView = findViewById<RecyclerView>(R.id.RecyclerView)

            recyclerView.layoutManager = LinearLayoutManager(this@MainActivity)

            val adapter = ArticleAdapter(this@MainActivity, articleList)
            recyclerView.adapter = adapter

            progressBar.visibility = View.GONE
            recyclerView.visibility = View.VISIBLE
        }
    }

    fun showArticle(articleToShow: Int) {
        val newIntent = Intent(this, ArticleWebViewActivity::class.java)

//        lifecycleScope.launch {
//            val operation = async(Dispatchers.IO) {
//                val doc = Jsoup.connect(url).get()
//                content = doc.text()
//            }
//            operation.await()
//
//            newIntent.putExtra("url", url)
//            newIntent.putExtra("content", content)
//            startActivity(newIntent)
//        }

        newIntent.putExtra("url", articleList[articleToShow].url.toString())
        newIntent.putExtra("content", articleList[articleToShow].content)
        startActivity(newIntent)
    }

    private fun restoreArticles() {
        val recyclerView = findViewById<RecyclerView>(R.id.RecyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this@MainActivity)

        val adapter = ArticleAdapter(this@MainActivity, articleList)
        recyclerView.adapter = adapter

        progressBar.visibility = View.GONE
        recyclerView.visibility = View.VISIBLE
    }

    override fun onSaveInstanceState(savedState: Bundle) {
        savedState.putParcelableArrayList("articleList", articleList)
        savedState.putInt("tab", findViewById<TabLayout>(R.id.tabLayout).selectedTabPosition)
        super.onSaveInstanceState(savedState)
    }

    override fun onMenuItemClick(item: MenuItem?): Boolean {
        when (item!!.itemId) {
            R.id.nav_logout -> {

                FirebaseAuth.getInstance().signOut()
                LoginManager.getInstance().logOut()
                val logoutIntent = Intent(this, LoginActivity::class.java)
                startActivity(logoutIntent)
                return true
            }
        }
        return false
    }

    fun setupTabLayout() {

        tabLayout.removeAllTabs()

        tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            @SuppressLint("SimpleDateFormat")
            override fun onTabSelected(tab: TabLayout.Tab) {
                articleList.clear()
                val keyword = tab.text.toString()
                val sdf = SimpleDateFormat("yyyy-MM-dd")
                val yesterday = Calendar.getInstance()
                yesterday.add(Calendar.DATE, -1)

                resetArticles(keyword, sdf.format(yesterday.time).toString())
            }

            override fun onTabUnselected(tab: TabLayout.Tab) {}
            override fun onTabReselected(tab: TabLayout.Tab) {}
        })

        keywordsList.forEach {
            tabLayout.addTab(tabLayout.newTab().setText(it.word))
        }

        val addTopicsTextView = findViewById<TextView>(R.id.addTopicsTextView)
        val fab = findViewById<FloatingActionButton>(R.id.addTopicsFab)

        if (tabLayout.getTabAt(0) == null) {
            tabLayout.visibility = View.GONE
            progressBar.visibility = View.GONE
            addTopicsTextView.visibility = View.VISIBLE
            fab.visibility = View.VISIBLE

            fab.setOnClickListener {
                startKeywordSelection(7375)
            }
        } else {
            tabLayout.visibility = View.VISIBLE
            addTopicsTextView.visibility = View.GONE
            fab.visibility = View.GONE
        }

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == 7375 && resultCode == Activity.RESULT_OK) {
            if (data!!.hasExtra("keywordsList")) {
                keywordsList =
                    data.getParcelableArrayListExtra<Keyword>("keywordsList") as ArrayList<Keyword>
                setupTabLayout()

                updateFirebase()
            }
        } else if (requestCode == 7376 && resultCode == Activity.RESULT_OK) {
            if (data!!.hasExtra("keywordsList")) {
                keywordsList =
                    data.getParcelableArrayListExtra<Keyword>("keywordsList") as ArrayList<Keyword>

                updateFirebase()
            }
        }
    }

    fun updateFirebase() {
        val users = db.collection("users")

        users.document(Firebase.auth.uid.toString()).set(mapOf("keywords" to keywordsList))
    }

    fun startKeywordSelection(requestCode: Int) {
        val intent = Intent(this, KeywordSelectionActivity::class.java)
        intent.putParcelableArrayListExtra("keywordsList", keywordsList)
        startActivityForResult(intent, requestCode)
    }
}

