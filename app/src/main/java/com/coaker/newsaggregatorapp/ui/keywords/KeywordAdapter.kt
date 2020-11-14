package com.coaker.newsaggregatorapp.ui.keywords

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.SwitchCompat
import androidx.recyclerview.widget.RecyclerView
import com.coaker.newsaggregatorapp.R
import kotlin.collections.ArrayList

class KeywordAdapter(
    private val fragment: KeywordsFragment,
    private val dataList: ArrayList<Keyword>
) : RecyclerView.Adapter<KeywordAdapter.ViewHolder>() {

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view), View.OnClickListener {

        val keywordSwitch = view.findViewById(R.id.notifSwitch) as SwitchCompat

        init {
            view.isClickable = true
            view.setOnClickListener(this)
        }

        override fun onClick(v: View?) {

        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(
            R.layout.keyword_list_item,
            parent,
            false
        )

        return ViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val keyword = dataList[position]

        holder.keywordSwitch.text = keyword.word
        holder.keywordSwitch.isChecked = keyword.isNotifier!!
    }

    override fun getItemCount(): Int {
        return dataList.size
    }

    fun moveItem(from: Int, to: Int) {
        val fromKeyword = dataList[from]
        dataList.removeAt(from)
        if (to < from) {
            dataList.add(to, fromKeyword)
        } else {
            dataList.add(to - 1, fromKeyword)
        }

        fragment.updateKeywordsList(dataList)
    }
}