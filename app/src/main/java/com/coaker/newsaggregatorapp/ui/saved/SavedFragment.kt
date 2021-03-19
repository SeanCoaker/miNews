package com.coaker.newsaggregatorapp.ui.saved

import android.content.Intent
import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.coaker.newsaggregatorapp.MainActivity
import com.coaker.newsaggregatorapp.OfflineSavedArticleActivity
import com.coaker.newsaggregatorapp.R
import com.coaker.newsaggregatorapp.Variables
import com.coaker.newsaggregatorapp.ArticleWebViewActivity
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import java.io.File

/**
 * A fragment class that controls the saved article list fragment.
 *
 * @author Sean Coaker (986529)
 * @since 1.0
 */
class SavedFragment : Fragment() {

    private lateinit var filesList: ArrayList<File>


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
    ): View? {
        val root = inflater.inflate(R.layout.fragment_saved, container, false)

        val parent = activity as MainActivity
        parent.tabLayout.visibility = View.GONE

        val userId = Firebase.auth.currentUser!!.uid

        val dir = File(context?.filesDir, "saved articles - $userId")
        val files = dir.listFiles()

        val recyclerView = root.findViewById<RecyclerView>(R.id.savedRecycler)
        recyclerView.layoutManager = LinearLayoutManager(context)

        // Creates a list of saved article files from internal storage.
        if (files != null) {
            filesList = files.toCollection(ArrayList())
            val adapter = SavedArticleAdapter(this, filesList)
            recyclerView.adapter = adapter
        }

        return root
    }


    /**
     * A method to setup the options menu for the article reading layout.
     *
     * @param[menu] The menu to be edited.
     * @param[inflater] Used to inflate the menu.
     */
    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        // Inflate the menu; this adds items to the action bar if it is present.
        val context = activity as MainActivity
        context.menuInflater.inflate(R.menu.article, menu)
    }


    /**
     * A method to load the selected article for the user to read. If the user is online, the
     * article will be loaded using the url in a webview. If they are offline then the article
     * will be shown in a default layout with article content read from the saved file.
     *
     * @param[file] The file to be read from.
     */
    fun loadArticle(file: File) {

        val fileLines = ArrayList<String>()
        file.useLines { lines -> fileLines.addAll(lines) }

        if (fileLines.isNotEmpty()) {
            val url = fileLines[0]
            val source = fileLines[1]
            val headline = fileLines[2]
            val date = fileLines[3]

            var content = ""

            for (i in 4 until fileLines.size) {
                content += fileLines[i] + " "
            }

            val intent = if (Variables.isConnected) {
                Intent(context, ArticleWebViewActivity::class.java)
            } else {
                Intent(context, OfflineSavedArticleActivity::class.java)
            }

            intent.putExtra("url", url)
            intent.putExtra("headline", headline)
            intent.putExtra("source", source)
            intent.putExtra("date", date)
            intent.putExtra("content", content)
            startActivity(intent)
        }
    }


    /**
     * A method to update the list of saved articles.
     *
     * @param[dataList] The list of saved articles in the adapter.
     */
    fun updateArticleList(dataList: ArrayList<File>) {
        filesList = dataList
    }

}