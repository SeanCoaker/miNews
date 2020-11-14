package com.coaker.newsaggregatorapp.ui.keywords

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.ItemTouchHelper.*
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.coaker.newsaggregatorapp.MainActivity
import com.coaker.newsaggregatorapp.R

class KeywordsFragment : Fragment() {
    private lateinit var root: View
    private lateinit var parent: MainActivity
    private lateinit var keywordsList: ArrayList<Keyword>
    private lateinit var adapter: KeywordAdapter

    private var isAddingKeywords = false


    private val itemTouchHelper by lazy {
        // 1. Note that I am specifying all 4 directions.
        //    Specifying START and END also allows
        //    more organic dragging than just specifying UP and DOWN.
        val simpleItemTouchCallback =
            object : ItemTouchHelper.SimpleCallback(UP or
                    DOWN or
                    START or
                    END, 0) {

                override fun onMove(recyclerView: RecyclerView,
                                    viewHolder: RecyclerView.ViewHolder,
                                    target: RecyclerView.ViewHolder): Boolean {

                    val adapter = recyclerView.adapter as KeywordAdapter
                    val from = viewHolder.adapterPosition
                    val to = target.adapterPosition
                    // 2. Update the backing model. Custom implementation in
                    //    MainRecyclerViewAdapter. You need to implement
                    //    reordering of the backing model inside the method.
                    adapter.moveItem(from, to)
                    // 3. Tell adapter to render the model update.
                    adapter.notifyItemMoved(from, to)

                    return true
                }

                override fun onSwiped(viewHolder: RecyclerView.ViewHolder,
                                      direction: Int) {
                    // 4. Code block for horizontal swipe.
                    //    ItemTouchHelper handles horizontal swipe as well, but
                    //    it is not relevant with reordering. Ignoring here.
                }

                override fun onSelectedChanged(
                    viewHolder: RecyclerView.ViewHolder?,
                    actionState: Int
                ) {
                    super.onSelectedChanged(viewHolder, actionState)

                    if (actionState == ACTION_STATE_DRAG) {
                        viewHolder!!.itemView.alpha = 0.5F
                    }
                }

                override fun clearView(
                    recyclerView: RecyclerView,
                    viewHolder: RecyclerView.ViewHolder
                ) {
                    super.clearView(recyclerView, viewHolder)
                    viewHolder.itemView.alpha = 1.0F
                }
            }
        ItemTouchHelper(simpleItemTouchCallback)
    }


    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        root = inflater.inflate(R.layout.fragment_keyword_list, container, false)

        parent = activity as MainActivity
        keywordsList = parent.keywordsList
        parent.tabLayout.visibility = View.GONE

        val recyclerView = root.findViewById<RecyclerView>(R.id.keywordRecyclerView)
        recyclerView.layoutManager = LinearLayoutManager(root.context)

        adapter = KeywordAdapter(this, keywordsList)
        recyclerView.adapter = adapter

        itemTouchHelper.attachToRecyclerView(recyclerView)

        val textViewAddKeywords = root.findViewById<TextView>(R.id.textViewAddKeyword)
        textViewAddKeywords.setOnClickListener {
            isAddingKeywords = true
            parent.startKeywordSelection(7376)
        }

        return root
    }

    fun updateKeywordsList(dataList: ArrayList<Keyword>) {
        keywordsList = dataList
    }

    override fun onPause() {

        if (!isAddingKeywords) {
            parent.setupTabLayout()
            parent.updateFirebase()
        }

        super.onPause()
    }
}
