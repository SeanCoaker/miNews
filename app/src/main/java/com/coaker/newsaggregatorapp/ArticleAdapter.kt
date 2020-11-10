package com.coaker.newsaggregatorapp

import android.content.Context
import android.icu.text.SimpleDateFormat
import android.icu.util.TimeZone
import android.net.ParseException
import android.text.SpannableStringBuilder
import android.text.format.DateUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.text.bold
import androidx.recyclerview.widget.RecyclerView
import com.squareup.picasso.Picasso
import java.util.*
import kotlin.collections.ArrayList


class ArticleAdapter(
    private val mainActivity: MainActivity,
    private val dataList: ArrayList<NewsData>
) : RecyclerView.Adapter<ArticleAdapter.ViewHolder>() {
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

            mainActivity.showArticle(adapterPosition)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(
            R.layout.article_preview_layout,
            parent,
            false
        )
        root = parent.context

        return ViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val articlePreview = dataList[position]

        holder.headline.text = articlePreview.title

        if (articlePreview.urlToImage ==  "null") {
            Picasso.get().load(R.drawable.emptyimage).resize(1340, 820).into(holder.image)
        } else {
            Picasso.get().load(articlePreview.urlToImage).resize(1340, 820).into(holder.image)
        }

        holder.preview.text = articlePreview.description

        val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.UK)
        sdf.timeZone = TimeZone.getTimeZone("GMT")
        try {
            val time: Long = sdf.parse(articlePreview.publishedAt).time
            val now = System.currentTimeMillis()
            val ago = DateUtils.getRelativeTimeSpanString(time, now, DateUtils.MINUTE_IN_MILLIS)

            val string = SpannableStringBuilder().bold { append(articlePreview.source) }.append(" - $ago")
            holder.publisherDate.text = string
        } catch (e: ParseException) {
            e.printStackTrace()
        }
    }

    override fun getItemCount(): Int {
        return dataList.size
    }

}