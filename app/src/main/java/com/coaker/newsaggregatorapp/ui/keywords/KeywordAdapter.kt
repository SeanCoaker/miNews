package com.coaker.newsaggregatorapp.ui.keywords

import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.CompoundButton
import android.widget.ImageButton
import android.widget.Switch
import androidx.appcompat.widget.SwitchCompat
import androidx.recyclerview.widget.RecyclerView
import com.coaker.newsaggregatorapp.R
import com.coaker.newsaggregatorapp.ui.article.ArticleAdapter.ViewHolder
import java.util.*
import kotlin.collections.ArrayList

/**
 * An adapter class that displays a keyword in a card view.
 *
 * @author Sean Coaker (986529)
 * @since 1.0
 */
class KeywordAdapter(
    private val fragment: KeywordsFragment,
    var dataList: ArrayList<Keyword>
) : RecyclerView.Adapter<KeywordAdapter.ViewHolder>() {


    /**
     * An inner class for configuring the layout of each item in the recycler view.
     *
     * @param[view] The view to be configured
     */
    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view), View.OnClickListener, CompoundButton.OnCheckedChangeListener {

        val keywordSwitch = view.findViewById(R.id.notifSwitch) as SwitchCompat

        init {
            view.isClickable = true

            val crossButton = view.findViewById<ImageButton>(R.id.crossButton)
            crossButton.setOnClickListener(this)

            val notifSwitch = view.findViewById<SwitchCompat>(R.id.notifSwitch)
            notifSwitch.setOnCheckedChangeListener(this)
        }

        // Sets up the onclick for the cross button.
        override fun onClick(v: View) {
            when (v.id) {
                R.id.crossButton -> {
                    removeItem(adapterPosition)
                }
            }
        }

        // Sets up code for when the notification switch for each keyword is changed.
        override fun onCheckedChanged(buttonView: CompoundButton?, isChecked: Boolean) {
            when (buttonView!!.id) {
                R.id.notifSwitch -> {
                    toggleNotifier(adapterPosition, isChecked)
                }
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
            R.layout.keyword_list_item,
            parent,
            false
        )

        return ViewHolder(itemView)
    }


    /**
     * A method that sets the values to be displayed in each view included in the recycler view.
     *
     * @param[holder] The view holder to be configured.
     * @param[position] The position of the view holder in the recycler view.
     */
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val keyword = dataList[position]

        holder.keywordSwitch.text = keyword.word
        holder.keywordSwitch.isChecked = keyword.isNotifier!!
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
     * A method to move items in the dataList when the user reorders keywords.
     *
     * @param[from] The position to move the keyword from.
     * @param[to] The position to move the keyword to.
     */
    fun moveItem(from: Int, to: Int) {
        Collections.swap(dataList, from, to)

        fragment.updateKeywordsList(dataList)
    }


    /**
     * A method to remove a keyword from the dataList and keywordsList.
     *
     * @param[position] The position in the array lists to remove the keyword from.
     */
    fun removeItem(position: Int) {
        dataList.removeAt(position)
        notifyItemRemoved(position)
        fragment.updateKeywordsList(dataList)
        fragment.updateFirebase()
    }


    /**
     * A method to toggle the notifier boolean for the selected keyword.
     *
     * @param[position] The position of the keyword in the recycler view and array lists.
     * @param[isChecked] The isChecked value of the switch.
     */
    fun toggleNotifier(position: Int, isChecked: Boolean) {
        dataList[position].isNotifier = isChecked
        fragment.updateKeywordsList(dataList)
        fragment.updateFirebase()
    }
}