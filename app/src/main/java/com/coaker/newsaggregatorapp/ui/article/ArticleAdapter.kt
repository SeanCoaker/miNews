package com.coaker.newsaggregatorapp.ui.article

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
import com.coaker.newsaggregatorapp.MainActivity
import com.coaker.newsaggregatorapp.NewsData
import com.coaker.newsaggregatorapp.R
import com.squareup.picasso.Picasso
import java.util.*
import kotlin.collections.ArrayList

/**
 * An adapter class that displays important news article info as a preview in a card view.
 *
 * @author Sean Coaker (986529)
 * @since 1.0
 */
class ArticleAdapter(
    private val mainActivity: MainActivity,
    private val dataList: ArrayList<NewsData>
) : RecyclerView.Adapter<ArticleAdapter.ViewHolder>() {
    private var root: Context? = null


    /**
     * An inner class for configuring the layout of each item in the recycler view.
     *
     * @param[view] The view to be configured
     */
    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view), View.OnClickListener {

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


    /**
     * A method that inflates the article preview layout as the view of each recycler view item.
     *
     * @param[parent] The parent of the view as a ViewGroup
     * @param[viewType] The view type of the view as an int
     *
     * @return[ViewHolder] The inflated view as a ViewHolder
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(
            R.layout.article_preview_layout,
            parent,
            false
        )
        root = parent.context

        return ViewHolder(itemView)
    }


    /**
     * A method that sets the values to be displayed in each view included in the recycler view.
     *
     * @param[holder] The view holder to be configured.
     * @param[position] The position of the view holder in the recycler view.
     */
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val articlePreview = dataList[position]

        holder.headline.text = articlePreview.title

        // Sets the article preview image, or sets a default image if the article doesn't include a title image.
        if (articlePreview.urlToImage == "null") {
            Picasso.get().load(R.drawable.emptyimage).resize(1340, 820).into(holder.image)
        } else {
            Picasso.get().load(articlePreview.urlToImage).resize(1340, 820).into(holder.image)
        }

        holder.preview.text = articlePreview.description

        // The next block of code displays how long ago the article was published in a user friendly way.
        val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.UK)
        sdf.timeZone = TimeZone.getTimeZone("UK")

        try {
            val time: Long = sdf.parse(articlePreview.publishedAt).time
            val now = System.currentTimeMillis()
            val ago = DateUtils.getRelativeTimeSpanString(time, now, DateUtils.MINUTE_IN_MILLIS)

            val string =
                SpannableStringBuilder().bold { append(articlePreview.source) }.append(" - $ago")
            holder.publisherDate.text = string
        } catch (e: ParseException) {
            e.printStackTrace()
        }
    }


    /**
     * A method that returns how many items are in the data list.
     *
     * @return[Int]
     */
    override fun getItemCount(): Int {
        return dataList.size
    }

}