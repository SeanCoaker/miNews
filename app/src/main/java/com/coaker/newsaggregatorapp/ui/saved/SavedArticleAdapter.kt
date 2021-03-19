package com.coaker.newsaggregatorapp.ui.saved

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.coaker.newsaggregatorapp.R
import com.coaker.newsaggregatorapp.ui.keywords.KeywordAdapter.ViewHolder
import java.io.File

/**
 * An adapter class that displays a saved article title in a card view.
 *
 * @author Sean Coaker (986529)
 * @since 1.0
 */
class SavedArticleAdapter(
    private val fragment: SavedFragment,
    private val dataList: ArrayList<File>
) : RecyclerView.Adapter<SavedArticleAdapter.ViewHolder>() {
    private var root: Context? = null


    /**
     * An inner class for configuring the layout of each item in the recycler view.
     *
     * @param[view] The view to be configured
     */
    inner class ViewHolder(view: View) :  RecyclerView.ViewHolder(view), View.OnClickListener {

        val headline = view.findViewById(R.id.headlineText) as TextView

        init {
            view.isClickable = true
            view.setOnClickListener(this)

            val crossButton = view.findViewById<ImageButton>(R.id.crossButton)
            crossButton.setOnClickListener(this)
        }

        override fun onClick(v: View) {
            when (v.id) {
                R.id.crossButton -> {
                    removeItem(adapterPosition)
                }

                R.id.savedCard -> fragment.loadArticle(dataList[adapterPosition])
            }

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
            R.layout.saved_article_list_item,
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
        val savedArticle = dataList[position]

        holder.headline.text = savedArticle.name.dropLast(4)
    }


    /**
     * A method that returns how many items are in the data list.
     *
     * @return[Int]
     */
    override fun getItemCount(): Int {
        return dataList.size
    }


    /**
     * A method to remove a saved article from the dataList and savedArticle list. Also deletes
     * the saved article from internal storage.
     *
     * @param[position] The position in the array lists to remove the saved article from.
     */
    private fun removeItem(position: Int) {
        dataList[position].delete()
        dataList.removeAt(position)
        notifyItemRemoved(position)
        fragment.updateArticleList(dataList)
    }

}