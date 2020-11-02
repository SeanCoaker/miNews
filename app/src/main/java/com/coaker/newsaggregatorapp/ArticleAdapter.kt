package com.coaker.newsaggregatorapp

import android.content.Context
import android.text.SpannableStringBuilder
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.text.bold
import androidx.recyclerview.widget.RecyclerView
import com.coaker.newsaggregatorapp.ui.home.HomeFragment
import com.squareup.picasso.Picasso

class ArticleAdapter(private val fragment: HomeFragment, private val dataList: ArrayList<NewsData>) : RecyclerView.Adapter<ArticleAdapter.ViewHolder>() {
    private var root: Context? = null

    inner class ViewHolder(view: View) :  RecyclerView.ViewHolder(view), View.OnClickListener {

        val headline = view.findViewById(R.id.articleHeadline2) as TextView
        val image = view.findViewById(R.id.articleImage2) as ImageView
        val preview = view.findViewById(R.id.articlePreview2) as TextView
        val publisherDate = view.findViewById(R.id.articlePublisherDate) as TextView

        init {
            view.isClickable = true
            view.setOnClickListener(this)
        }

        override fun onClick(v: View?) {

            fragment.showArticle(adapterPosition)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.article_preview_layout, parent, false)
        root = parent.context

        return ViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val articlePreview = dataList[position]

        holder.headline.text = articlePreview.title

        if (articlePreview.urlToImage ==  "null") {
            Picasso.with(root).load(R.drawable.emptyimage).resize(1340, 820).into(holder.image)
        } else {
            Picasso.with(root).load(articlePreview.urlToImage).resize(1340, 820).into(holder.image)
        }

        holder.preview.text = articlePreview.description

        val string = SpannableStringBuilder().bold { append(articlePreview.source) }.append(" - " + articlePreview.publishedAt)
        holder.publisherDate.text = string

    }

    override fun getItemCount(): Int {
        return dataList.size
    }

}