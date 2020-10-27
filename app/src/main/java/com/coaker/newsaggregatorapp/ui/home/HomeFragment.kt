package com.coaker.newsaggregatorapp.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.cardview.widget.CardView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import com.coaker.newsaggregatorapp.MainActivity
import com.coaker.newsaggregatorapp.R
import com.coaker.newsaggregatorapp.ui.article.ArticleFragment


class HomeFragment : Fragment() {

    private lateinit var homeViewModel: HomeViewModel

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        homeViewModel =
                ViewModelProvider(this).get(HomeViewModel::class.java)
        val root = inflater.inflate(R.layout.fragment_home, container, false)
        val articleButton = root.findViewById<CardView>(R.id.cardView1)

        articleButton.setOnClickListener {
            view?.findNavController()?.navigate(R.id.nav_article)
        }
        return root
    }
}
