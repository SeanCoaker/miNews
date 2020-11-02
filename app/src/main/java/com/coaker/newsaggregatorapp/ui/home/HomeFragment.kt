package com.coaker.newsaggregatorapp.ui.home

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.coaker.newsaggregatorapp.ArticleAdapter
import com.coaker.newsaggregatorapp.NewsData
import com.coaker.newsaggregatorapp.R
import kotlinx.coroutines.*
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject

class HomeFragment : Fragment() {
    var articleList = ArrayList<NewsData>()

    private val client = OkHttpClient()

    val thisFragment = this

    private lateinit var homeViewModel: HomeViewModel

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        homeViewModel =
                ViewModelProvider(this).get(HomeViewModel::class.java)
        articleList.clear()
        val root = inflater.inflate(R.layout.fragment_home_recycler, container, false)

        lifecycleScope.launch {
            val operation = async(Dispatchers.IO) {
                getArticles("gaming", "2020-11-02")
            }
            operation.await()

            val recyclerView = root.findViewById<RecyclerView>(R.id.RecyclerView)
            recyclerView.layoutManager = LinearLayoutManager(root.context)

            val adapter = ArticleAdapter(thisFragment, articleList)
            recyclerView.adapter = adapter

        }


        return root
    }

    fun showArticle(articleToShow: Int) {
        val directions = HomeFragmentDirections.actionNavHomeToNavArticleWebview(articleList[articleToShow].url.toString())
        view?.findNavController()?.navigate(directions)
    }

    private fun getArticles(keyTerm: String, date: String) {
        val url = "https://newsapi.org/v2/everything?q=$keyTerm&language=en&from=$date&sortBy=publishedAt&apiKey=d4fd6b189c7d4ac4afa1f5ac86f9df5d"
        Log.i("url: ", url)
        val request = Request.Builder().url(url).build()

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

}
