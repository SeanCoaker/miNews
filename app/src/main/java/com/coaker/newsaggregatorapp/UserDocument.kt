package com.coaker.newsaggregatorapp

import com.coaker.newsaggregatorapp.ui.keywords.Keyword

/**
 * A data class used to fetch user documents from firestore.
 *
 * @author Sean Coaker (986529)
 * @since 1.0
 */
data class UserDocument(val keywords: ArrayList<Keyword>? = null)