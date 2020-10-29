package com.coaker.newsaggregatorapp.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.coaker.newsaggregatorapp.ArticleAdapter
import com.coaker.newsaggregatorapp.ArticleView
import com.coaker.newsaggregatorapp.R


class HomeFragment : Fragment() {
    val dataList = ArrayList<ArticleView>()

    private lateinit var homeViewModel: HomeViewModel

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        homeViewModel =
                ViewModelProvider(this).get(HomeViewModel::class.java)
        dataList.clear()
        val root = inflater.inflate(R.layout.fragment_home_recycler, container, false)

        val recyclerView = root.findViewById<RecyclerView>(R.id.RecyclerView)
        recyclerView.layoutManager = LinearLayoutManager(root.context)

        val articleView1 = ArticleView()
        articleView1.headline = getString(R.string.bale_headline)
        articleView1.image = ContextCompat.getDrawable(root.context, R.drawable.bale)
        articleView1.preview = getString(R.string.bale_preview)
        articleView1.publisher = "BBC Sport"
        articleView1.url = "https://www.bbc.co.uk/sport/football/54180724"
        articleView1.longAgo = "3 hours ago"

        val articleView2 = ArticleView()
        articleView2.headline = getString(R.string.lewa_headline)
        articleView2.image = ContextCompat.getDrawable(root.context, R.drawable.lewa)
        articleView2.preview = getString(R.string.lewa_preview)
        articleView2.publisher = "The Guardian"
        articleView2.url = "https://www.theguardian.com/football/2020/oct/24/european-roundup-lewandowski-hits-hat-trick-as-bayern-thrash-eintracht"
        articleView2.longAgo = "5 hours ago"

        val articleView3 = ArticleView()
        articleView3.headline = getString(R.string.hamilton_headline)
        articleView3.image = ContextCompat.getDrawable(root.context, R.drawable.hamilton)
        articleView3.preview = getString(R.string.hamilton_preview)
        articleView3.publisher = "ESPN"
        articleView3.url = "https://www.espn.co.uk/f1/story/_/id/30187515/hamilton-moves-schumacher-92nd-win"
        articleView3.longAgo = "8 hours ago"

        dataList.add(articleView1)
        dataList.add(articleView2)
        dataList.add(articleView3)

        val adapter = ArticleAdapter(this, dataList)
        recyclerView.adapter = adapter

        return root
    }

    fun showArticle(articleToShow: Int) {
        val directions = HomeFragmentDirections.actionNavHomeToNavArticleWebview(dataList[articleToShow].url.toString())
        view?.findNavController()?.navigate(directions)
    }
}
