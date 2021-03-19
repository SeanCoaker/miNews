package com.coaker.newsaggregatorapp.ui.article

import android.graphics.drawable.Drawable

/**
 * A class that stores data about each news article.
 *
 * @author Sean Coaker (986529)
 * @since 1.0
 */
class ArticleView {
    var headline: String? = null
    var image: Drawable? = null
    var preview: String? = null
    var publisher: String? = null
    var url: String? = null
    var longAgo: String? = null
}
