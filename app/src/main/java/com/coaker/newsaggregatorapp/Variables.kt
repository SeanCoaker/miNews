package com.coaker.newsaggregatorapp

import com.coaker.newsaggregatorapp.ui.keywords.Keyword

/**
 * Static variables that are associated with the whole app.
 */
object Variables {
    // Stores if the device is connected to a network.
    var isConnected: Boolean = false
    // Stores the current article list.
    var articleList = ArrayList<NewsData>()
    // Stores the user's list of keywords to view news stories for.
    var keywordsList = ArrayList<Keyword>()
}