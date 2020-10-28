package com.coaker.newsaggregatorapp.ui.article

import android.os.Bundle
import android.view.*
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.coaker.newsaggregatorapp.MainActivity
import com.coaker.newsaggregatorapp.R


class ArticleFragment : Fragment() {

    private lateinit var articleViewModel: ArticleViewModel

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        articleViewModel =
                ViewModelProvider(this).get(ArticleViewModel::class.java)
        val root = inflater.inflate(R.layout.fragment_article_webview, container, false)

        val webView = root.findViewById<WebView>(R.id.WebView)

        webView.webViewClient = object : WebViewClient() {
            override fun shouldOverrideUrlLoading(view: WebView, url: String): Boolean {
                view.loadUrl(url)
                return true
            }
        }

        webView.loadUrl("https://www.bbc.co.uk/sport/football/54180724")

        setHasOptionsMenu(true)

        return root
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        // Inflate the menu; this adds items to the action bar if it is present.
        val context = activity as MainActivity
        context.menuInflater.inflate(R.menu.article, menu)
    }
}