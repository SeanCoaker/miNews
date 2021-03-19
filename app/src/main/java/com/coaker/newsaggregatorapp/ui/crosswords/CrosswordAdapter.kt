package com.coaker.newsaggregatorapp.ui.crosswords

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.coaker.newsaggregatorapp.CrosswordFile
import com.coaker.newsaggregatorapp.R
import java.util.*

/**
 * An adapter class that displays crossword file names in a card view.
 *
 * @author Sean Coaker (986529)
 * @since 1.0
 */
class CrosswordAdapter(
    private val fragment: CrosswordsFragment,
    private val dataList: ArrayList<CrosswordFile>
) : RecyclerView.Adapter<CrosswordAdapter.ViewHolder>() {
    private var root: Context? = null


    /**
     * An inner class for configuring the layout of each item in the recycler view.
     *
     * @param[view] The view to be configured
     */
    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view), View.OnClickListener {

        val crosswordNameText = view.findViewById(R.id.crosswordNameText) as TextView

        init {
            view.isClickable = true
            view.setOnClickListener(this)
        }

        override fun onClick(v: View?) {
            fragment.showCrossword(adapterPosition)
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
            R.layout.puzzle_list_item,
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
        val crossword = dataList[position]

        holder.crosswordNameText.text = crossword.id
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