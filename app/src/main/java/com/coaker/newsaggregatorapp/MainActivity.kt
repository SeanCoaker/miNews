package com.coaker.newsaggregatorapp

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

class MainActivity : AppCompatActivity(), MenuItem.OnMenuItemClickListener {
    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var navController: NavController
    private lateinit var navHostFragment: NavHostFragment
    private lateinit var progressBar: ProgressBar
    private lateinit var tabLayout: TabLayout

    private var articleList = ArrayList<NewsData>()
    private var keywordsList = ArrayList<String>()

    private var name: String? = null
    private var email: String? = null
    private var image: Uri? = null
    private var isCustom: Boolean? = null

    private val client = OkHttpClient()
    private val db = FirebaseFirestore.getInstance()

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
                keywordsList = documentSnapshot.get("keywords") as ArrayList<String>
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
                R.id.nav_key_terms
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
                resetArticles(tabLayout.getTabAt(0)!!.text.toString(), "2020-11-02")
            }
        }

        val drawerImage = navView.getHeaderView(0).findViewById<ImageView>(R.id.drawerImage)

        if (FirebaseAuth.getInstance().currentUser!!.providerData[1].providerId == EmailAuthProvider.PROVIDER_ID) {
            drawerImage.setImageResource(R.drawable.ic_baseline_person_24)
        } else {
            Picasso.get().load(image).resize(128, 128).into(drawerImage)
        }

        Log.i("Image: ", image.toString())

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
        val urlUni = "https://newsapi.org/v2/everything?q=$keyword&language=en&from=$date&sortBy=relevancy&apiKey=b5c1da042e234be5b00bd666e41b160d"
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
                getArticles(keyword, "2020-11-02")
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

    private fun setupTabLayout() {
        tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) {
                articleList.clear()
                val keyword = tab.text.toString()
                resetArticles(keyword, "2020-11-02")
            }

            override fun onTabUnselected(tab: TabLayout.Tab) {}
            override fun onTabReselected(tab: TabLayout.Tab) {}
        })

        keywordsList.forEach {
            tabLayout.addTab(tabLayout.newTab().setText(it))
        }

        val addTopicsTextView = findViewById<TextView>(R.id.addTopicsTextView)
        val fab = findViewById<FloatingActionButton>(R.id.addTopicsFab)

        if (tabLayout.getTabAt(0) == null) {
            tabLayout.visibility = View.GONE
            progressBar.visibility = View.GONE
            addTopicsTextView.visibility = View.VISIBLE
            fab.visibility = View.VISIBLE

            fab.setOnClickListener {
                val intent = Intent(this, KeywordSelectionActivity::class.java)
                startActivityForResult(intent, 7375)
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
                keywordsList = data.getStringArrayListExtra("keywordsList")!!
                setupTabLayout()

                val users = db.collection("users")

                users.document(Firebase.auth.uid.toString()).set(mapOf("keywords" to keywordsList))
            }
        }
    }
}

