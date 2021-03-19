package com.coaker.newsaggregatorapp.ui.home

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.coaker.newsaggregatorapp.MainActivity
import com.coaker.newsaggregatorapp.NewsData
import com.coaker.newsaggregatorapp.R
import com.coaker.newsaggregatorapp.Variables
import com.coaker.newsaggregatorapp.ui.article.ArticleAdapter
import com.coaker.newsaggregatorapp.UserDocument
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.tabs.TabLayout
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

/**
 * A fragment class that controls the main news article feed fragment.
 *
 * @author Sean Coaker (986529)
 * @since 1.0
 */
class HomeFragment : Fragment() {
    private lateinit var root: View
    private lateinit var parent: MainActivity
    private lateinit var refreshButton: MenuItem
    private lateinit var spinner: Spinner
    private lateinit var networkErrorText: TextView
    private lateinit var recyclerView: RecyclerView
    private lateinit var progressBar: ProgressBar
    private lateinit var fab: FloatingActionButton
    private lateinit var hintText: TextView
    private lateinit var tabLayout: TabLayout

    private var sort = "relevancy"

    private val client = OkHttpClient()
    private val db = FirebaseFirestore.getInstance()


    /**
     * A method used when the fragment is created to ensure that an options menu is shown to the user.
     *
     * @param[savedInstanceState] Any previous saved instance of the fragment.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setHasOptionsMenu(true)
    }


    /**
     * A method to display the refresh button in the options menu
     *
     * @param[menu] The menu to edit.
     */
    override fun onPrepareOptionsMenu(menu: Menu) {
        super.onPrepareOptionsMenu(menu)
        menu.findItem(R.id.action_refresh).isVisible = true
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
        root = inflater.inflate(R.layout.fragment_home_recycler, container, false)
        parent = activity as MainActivity

        recyclerView = root.findViewById(R.id.RecyclerView)
        recyclerView.visibility = View.GONE


        // Sets up the sort articles drop down list
        spinner = requireActivity().findViewById(R.id.sortSpinner)

        val sortChoicesList = ArrayList<String>()

        sortChoicesList.add("Relevancy")
        sortChoicesList.add("Popularity")
        sortChoicesList.add("Published At")

        val adapter = ArrayAdapter(requireContext(), R.layout.spinner_item, sortChoicesList)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner.adapter = adapter

        spinner.setSelection(0, false)
        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                sort = parent!!.getItemAtPosition(position).toString().toLowerCase(Locale.ROOT)

                if (sort == "published at") {
                    sort = "publishedAt"
                }

                refreshArticles()
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }


        networkErrorText = requireActivity().findViewById(R.id.networkErrorTextView)

        progressBar = requireActivity().findViewById(R.id.progressBar)


        fab = requireActivity().findViewById(R.id.addTopicsFab)
        fab.setOnClickListener{
            parent.startKeywordSelection(7375)
        }


        hintText = requireActivity().findViewById(R.id.addTopicsTextView)

        tabLayout = requireActivity().findViewById(R.id.tabLayout)


        val user = Firebase.auth.currentUser
        user!!.reload()

        // Fetches the list of keywords that user has previously stored on firestore.
        val docRef = db.collection("users").document(user.uid)
        docRef.get().addOnSuccessListener { documentSnapshot ->
            if (documentSnapshot.exists()) {
                val result = documentSnapshot.toObject(UserDocument::class.java)
                Variables.keywordsList = result!!.keywords!!

                if (Variables.isConnected) {

                    if (Variables.keywordsList.isNotEmpty()) {
                        setupTabLayout()
                        spinner.visibility = View.VISIBLE
                        tabLayout.visibility = View.VISIBLE
                    } else {
                        spinner.visibility = View.GONE
                        tabLayout.visibility = View.GONE
                        hintText.visibility = View.VISIBLE
                        fab.visibility = View.VISIBLE
                    }


                } else {
                    spinner.visibility = View.GONE
                    networkErrorText.visibility = View.VISIBLE
                    tabLayout.visibility = View.GONE
                }
            } else {
                spinner.visibility = View.GONE
                hintText.visibility = View.VISIBLE
                fab.visibility = View.VISIBLE
                tabLayout.visibility = View.GONE
                refreshButton.isVisible = false
            }

            progressBar.visibility = View.GONE
        }.addOnFailureListener {
            Log.i("Firestore Read: ", "Failed")
        }

        return root
    }


    /**
     * A method called when the fragment is started. This is used to display the correct layout items
     * or to display news articles depending on if the user has selected their keywords and if they're
     * connected to a network.
     */
    override fun onStart() {
        super.onStart()

        if (Variables.articleList.isNotEmpty()) {
            if (Variables.keywordsList.isNotEmpty()) {
                spinner.visibility = View.VISIBLE
                tabLayout.visibility = View.VISIBLE
            }
        } else {
            if (Variables.keywordsList.isNotEmpty()) {
                setupTabLayout()
            }
        }
    }


    /**
     * A method called when the fragment is paused. This hides the items visible in the fragment.
     */
    override fun onPause() {
        super.onPause()

        networkErrorText.visibility = View.GONE
        spinner.visibility = View.GONE
        fab.visibility = View.GONE
        hintText.visibility = View.GONE
        tabLayout.visibility = View.GONE
    }


    /**
     * A method to setup the options menu. When the options menu is created, we setup the onclick
     * listener for the refresh button and display the user's details in the drawer layout.
     *
     * @param[menu] The menu to be edited.
     * @param[inflater] Used to inflate the menu.
     */
    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)

        val user = Firebase.auth.currentUser

        val name = user!!.displayName
        val email = user.email

        inflater.inflate(R.menu.main, menu)
        parent.findViewById<TextView>(R.id.nameText)!!.text = name
        parent.findViewById<TextView>(R.id.emailText)!!.text = email

        refreshButton = menu.findItem(R.id.action_refresh)
        refreshButton.setOnMenuItemClickListener {
            refreshArticles()
            true
        }
    }


    /**
     * A method to setup the tab layout with the user's selected keywords only if the device is
     * connected to a network. A call is made to setup the article's and display them to the user.
     */
    private fun setupTabLayout() {

        if (Variables.isConnected) {
            tabLayout.removeAllTabs()

            tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
                @SuppressLint("SimpleDateFormat")
                override fun onTabSelected(tab: TabLayout.Tab) {
                    Variables.articleList.clear()
                    val keyword = tab.text.toString()
                    val sdf = SimpleDateFormat("yyyy-MM-dd")
                    val yesterday = Calendar.getInstance()
                    yesterday.add(Calendar.DATE, -1)

                    resetArticles(keyword, sdf.format(yesterday.time).toString())
                }

                override fun onTabUnselected(tab: TabLayout.Tab) {}
                override fun onTabReselected(tab: TabLayout.Tab) {}
            })

            Variables.keywordsList.forEach {
                tabLayout.addTab(tabLayout.newTab().setText(it.word))
            }

            if (tabLayout.getTabAt(0) == null) {
                tabLayout.visibility = View.GONE
                progressBar.visibility = View.GONE
                hintText.visibility = View.VISIBLE
                fab.visibility = View.VISIBLE

                fab.setOnClickListener {
                    parent.startKeywordSelection(7375)
                }
            } else {
                tabLayout.visibility = View.VISIBLE
                hintText.visibility = View.GONE
                fab.visibility = View.GONE
                recyclerView.visibility = View.VISIBLE
                spinner.visibility = View.VISIBLE
            }
        } else {
            tabLayout.visibility = View.GONE
        }

    }


    /**
     * A method to call the get articles method without using the UI thread.
     *
     * @param[keyword] The keyword to fetch news about.
     * @param[date] The date of yesterday used to get news from yesterday onwards.
     */
    fun resetArticles(keyword: String, date: String) {

        if (Variables.isConnected) {
            lifecycleScope.launch {
                val operation = async(Dispatchers.IO) {
                    getArticles(keyword, date)
                }
                operation.await()

                val recyclerView = parent.findViewById<RecyclerView>(R.id.RecyclerView)

                recyclerView.layoutManager = LinearLayoutManager(context)

                val adapter = ArticleAdapter(parent, Variables.articleList)
                recyclerView.adapter = adapter


                recyclerView.visibility = View.VISIBLE
                networkErrorText.visibility = View.GONE
                spinner.visibility = View.VISIBLE
                tabLayout.visibility = View.VISIBLE
            }
        }

        progressBar.visibility = View.GONE
    }


    /**
     * A method to retrieve a Json response of news articles from newsAPI.
     *
     * @param[keyword] The keyword to get news about.
     * @param[date] The date of yesterday used to get news from yesterday onwards.
     */
    private fun getArticles(keyword: String, date: String) {
        val urlPersonal =
            "https://newsapi.org/v2/everything?q=$keyword&language=en&from=$date&sortBy=$sort&apiKey=d4fd6b189c7d4ac4afa1f5ac86f9df5d"
        val urlUni =
            "https://newsapi.org/v2/everything?q=$keyword&language=en&from=$date&sortBy=$sort&apiKey=b5c1da042e234be5b00bd666e41b160d"
        val request = Request.Builder().url(urlUni).build()

        val response = client.newCall(request).execute().body()!!.string()

        val jsonObject = JSONObject(response)

        val jsonArray = jsonObject.getJSONArray("articles")

        for (i in 0 until jsonArray.length()) {
            val jsonArticle = jsonArray.getJSONObject(i)

            val newsData = NewsData()

            val sourceObject = jsonArticle.getJSONObject("source")
            newsData.source = sourceObject.getString("name")

            newsData.author = jsonArticle.getString("author")
            newsData.title = jsonArticle.getString("title")
            newsData.description = jsonArticle.getString("description")
            newsData.url = jsonArticle.getString("url")
            newsData.urlToImage = jsonArticle.getString("urlToImage")
            newsData.publishedAt = jsonArticle.getString("publishedAt")
            newsData.content = jsonArticle.getString("content")

            Variables.articleList.add(newsData)
        }
    }


    /**
     * A method used to refresh the list of articles to get the latest articles to be shown to the
     * user.
     */
    fun refreshArticles() {
        if (Variables.isConnected) {
            if (Variables.keywordsList.isNotEmpty()) {
                Variables.articleList.clear()
                val sdf = SimpleDateFormat("yyyy-MM-dd")
                val yesterday = Calendar.getInstance()
                yesterday.add(Calendar.DATE, -1)

                // Checks if the tabLayout is empty before trying to refresh the articles.
                if (tabLayout.selectedTabPosition != -1) {
                    resetArticles(Variables.keywordsList[tabLayout.selectedTabPosition].word!!, sdf.format(yesterday.time).toString())
                } else {
                    spinner.visibility = View.VISIBLE

                    parent.findViewById<TextView>(R.id.networkErrorTextView).visibility = View.GONE

                    setupTabLayout()
                }
            }
        } else {
            Toast.makeText(context, "You're not connected to a network.",
                Toast.LENGTH_SHORT).show()
        }
    }

}
