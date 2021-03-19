package com.coaker.newsaggregatorapp.ui.keywords

import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.ItemTouchHelper.*
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.coaker.newsaggregatorapp.MainActivity
import com.coaker.newsaggregatorapp.R
import com.coaker.newsaggregatorapp.Variables
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase


/**
 * An fragment class that controls the keywords fragment.
 *
 * @author Sean Coaker (986529)
 * @since 1.0
 */
class KeywordsFragment : Fragment(), MenuItem.OnMenuItemClickListener {
    private lateinit var root: View
    private lateinit var parent: MainActivity
    private lateinit var adapter: KeywordAdapter
    private lateinit var recyclerView: RecyclerView

    private var isAddingKeywords = false
    private var isLoggingOut = false
    private var originalSize = 0


    // Controls what happens with different actions to the recycler view.
    private val itemTouchHelper by lazy {

        val simpleItemTouchCallback =
            object : ItemTouchHelper.SimpleCallback(
                UP or
                        DOWN or
                        START or
                        END, 0
            ) {

                // Configures the reordering keywords feature.
                override fun onMove(
                    recyclerView: RecyclerView,
                    viewHolder: RecyclerView.ViewHolder,
                    target: RecyclerView.ViewHolder
                ): Boolean {

                    val adapter = recyclerView.adapter as KeywordAdapter
                    val from = viewHolder.adapterPosition
                    val to = target.adapterPosition

                    adapter.moveItem(from, to)
                    adapter.notifyItemMoved(from, to)

                    return true
                }

                override fun onSwiped(
                    viewHolder: RecyclerView.ViewHolder,
                    direction: Int
                ) {

                }

                // Sets the item selected as slightly transparent when the user holds the item ready to reorder.
                override fun onSelectedChanged(
                    viewHolder: RecyclerView.ViewHolder?,
                    actionState: Int
                ) {
                    super.onSelectedChanged(viewHolder, actionState)

                    if (actionState == ACTION_STATE_DRAG) {
                        viewHolder!!.itemView.alpha = 0.5F
                    }
                }

                // Sets the recycler view item back to its original state.
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


    /**
     * A method to configure the fragment for when the view is created.
     *
     * @param[inflater] Used to inflate our layout in the fragment.
     * @param[container] Contains our inflated layout.
     * @param[savedInstanceState] Used to restore the fragment after leaving it.
     *
     * @return[View] Returns our fragment view.
     */
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        root = inflater.inflate(R.layout.fragment_keyword_list, container, false)

        parent = activity as MainActivity

        recyclerView = root.findViewById(R.id.keywordRecyclerView)
        recyclerView.layoutManager = LinearLayoutManager(context)

        adapter = KeywordAdapter(this, Variables.keywordsList)
        recyclerView.adapter = adapter


        itemTouchHelper.attachToRecyclerView(recyclerView)


        val textViewAddKeywords = root.findViewById<TextView>(R.id.textViewAddKeyword)
        textViewAddKeywords.setOnClickListener {
            isAddingKeywords = true
            parent.startKeywordSelection(7376)
        }

        // Shows helper instructions for the user.
        if (Variables.keywordsList.isNotEmpty()) {
            Toast.makeText(
                context,
                "Turn the switches on or off to turn notifications on or off for each keyword.",
                Toast.LENGTH_LONG
            ).show()
            adapter = KeywordAdapter(this, Variables.keywordsList)
            recyclerView.adapter = adapter
        }


        val logoutItem = parent.navView.menu.findItem(R.id.nav_logout)
        logoutItem.setOnMenuItemClickListener(this)

        return root
    }


    /**
     * A method to update the keywords list after changes are made in the recycler view.
     *
     * @param[dataList] The list used in the recycler view adapter.
     */
    fun updateKeywordsList(dataList: ArrayList<Keyword>) {
        Variables.keywordsList = dataList
        if (Variables.keywordsList.isEmpty()) {
            Variables.articleList.clear()
        }
    }


    /**
     * A method called when the fragment resumes. Used to display any changes to the recycler view.
     */
    override fun onResume() {
        isAddingKeywords = false

        val newSize = Variables.keywordsList.size

        adapter.dataList = Variables.keywordsList
        adapter.notifyItemRangeInserted(originalSize, newSize - originalSize)

        super.onResume()
    }


    /**
     * A method called when the fragment is paused. Used to save the previous size of the recycler
     * view and updates any changes to the firestore data.
     */
    override fun onPause() {
        originalSize = Variables.keywordsList.size

        updateFirebase()

        super.onPause()
    }


    /**
     * A method used to ensure the logout button works correctly in this fragment.
     *
     * @param[item] The menu item being clicked.
     */
    override fun onMenuItemClick(item: MenuItem?): Boolean {
        when (item!!.itemId) {
            R.id.nav_logout -> {

                isLoggingOut = true
                parent.logout()
                return true
            }
        }
        return false
    }


    /**
     * A method used to update the firebase stored data.
     */
    fun updateFirebase() {
        parent.updateFirebase()
    }
}
