package com.coaker.newsaggregatorapp

import android.content.Intent
import android.os.Bundle
import android.os.Parcelable
import android.util.Log
import android.view.Menu
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
import com.google.android.material.navigation.NavigationView
import com.google.android.material.tabs.TabLayout
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import org.jsoup.Jsoup

class MainActivity : AppCompatActivity() {
    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var navController: NavController
    private lateinit var navHostFragment: NavHostFragment

    var articleList = ArrayList<NewsData>()

    private val client = OkHttpClient()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)

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
                R.id.nav_key_terms,
                R.id.nav_logout
            ), drawerLayout
        )
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)

        val tabLayout = findViewById<TabLayout>(R.id.tabLayout)

        if (savedInstanceState != null) {
            articleList = savedInstanceState.getParcelableArrayList("articleList")!!
            tabLayout.getTabAt(savedInstanceState.getInt("tab"))!!.select()
            restoreArticles()
        } else {
            resetArticles(tabLayout.getTabAt(0)!!.text.toString(), "2020-11-02")
        }


        tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) {
                articleList.clear()
                val keyword = tab.text.toString()
                resetArticles(keyword, "2020-11-02")
            }

            override fun onTabUnselected(tab: TabLayout.Tab) {}
            override fun onTabReselected(tab: TabLayout.Tab) {}
        })

    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.main, menu)
        return true
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment)
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }

    private fun getArticles(keyword: String, date: String) {
        //val urlPersonal = "https://newsapi.org/v2/everything?q=$keyword&language=en&from=$date&sortBy=relevancy&apiKey=d4fd6b189c7d4ac4afa1f5ac86f9df5d"
        val urlUni = "https://newsapi.org/v2/everything?q=$keyword&language=en&from=$date&sortBy=relevancy&apiKey=b5c1da042e234be5b00bd666e41b160d"
        Log.i("url: ", urlUni)
        val request = Request.Builder().url(urlUni).build()

        val response = client.newCall(request).execute().body()!!.string()

        val jsonObject = JSONObject(response)
        val jsonArray = jsonObject.getJSONArray("articles")

        Log.i("Result: ", jsonArray.toString(10))

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

        }
    }

    fun showArticle(articleToShow: Int) {
        val newIntent = Intent(this, ArticleWebViewActivity::class.java)
        val url = articleList[articleToShow].url.toString()
        var content: String? = null

        lifecycleScope.launch {
            val operation = async(Dispatchers.IO) {
                val doc = Jsoup.connect(url).get()
                content = doc.text()
            }
            operation.await()

            newIntent.putExtra("url", url)
            newIntent.putExtra("content", content)
            startActivity(newIntent)
        }
    }

    fun restoreArticles() {
        val recyclerView = findViewById<RecyclerView>(R.id.RecyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this@MainActivity)

        val adapter = ArticleAdapter(this@MainActivity, articleList)
        recyclerView.adapter = adapter
    }

    override fun onSaveInstanceState(savedState: Bundle) {
        savedState.putParcelableArrayList("articleList", articleList)
        savedState.putInt("tab", findViewById<TabLayout>(R.id.tabLayout).selectedTabPosition)
        super.onSaveInstanceState(savedState)
    }
}

