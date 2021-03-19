package com.coaker.newsaggregatorapp.ui.crosswords

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.coaker.newsaggregatorapp.CrosswordFile
import com.coaker.newsaggregatorapp.MainActivity
import com.coaker.newsaggregatorapp.R
import org.akop.ararat.core.Crossword
import org.akop.ararat.core.buildCrossword
import org.akop.ararat.io.PuzFormatter
import org.akop.ararat.view.CrosswordView

/**
 * An fragment class that displays a list of crosswords to the user and then allows the user to
 * solve their selected crossword. This class was created with the help of https://github.com/0xe1f/ararat
 * and their crossword library.
 *
 * @author Sean Coaker (986529)
 * @since 1.0
 */
class CrosswordsFragment : Fragment(), CrosswordView.OnLongPressListener,
    CrosswordView.OnStateChangeListener, CrosswordView.OnSelectionChangeListener {

    private lateinit var crosswordView: CrosswordView
    private lateinit var hint: TextView
    private lateinit var recyclerView: RecyclerView
    private lateinit var solveWordButton: Button
    private lateinit var backButton: Button

    private val crosswordFiles = ArrayList<CrosswordFile>()

    private var currentWord: Crossword.Word? = null


    /**
     * A method that configures how the fragment is displayed to the user.
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
        val root = inflater.inflate(R.layout.fragment_crosswords, container, false)

        val parent = activity as MainActivity
        parent.tabLayout.visibility = View.GONE

        crosswordView = root.findViewById(R.id.crosswordView)
        crosswordView.visibility = View.GONE

        hint = root.findViewById(R.id.hintText)
        hint.visibility = View.GONE

        loadCrosswordFiles()

        recyclerView = root.findViewById(R.id.crosswordsRecycler)
        recyclerView.layoutManager = LinearLayoutManager(context)

        val adapter = CrosswordAdapter(this, crosswordFiles)
        recyclerView.adapter = adapter

        solveWordButton = root.findViewById(R.id.solveButton)
        solveWordButton.visibility = View.GONE
        solveWordButton.setOnClickListener {
            Toast.makeText(activity, currentWord.toString(), Toast.LENGTH_SHORT).show()
        }

        backButton = root.findViewById(R.id.backButton)
        backButton.visibility = View.GONE
        backButton.setOnClickListener {
            backButton.visibility = View.GONE
            solveWordButton.visibility = View.GONE
            crosswordView.visibility = View.GONE
            hint.visibility = View.GONE

            recyclerView.visibility = View.VISIBLE
        }

        return root
    }


    /**
     * A method to load crossword files from the raw directory into an array list.
     */
    private fun loadCrosswordFiles() {
        val files = R.raw::class.java.declaredFields
        files.forEach {
            if (it.name != "mi_news_video") {
                crosswordFiles.add(CrosswordFile(it.name))
            }
        }
    }


    /**
     * A method to show the selected crossword to the user.
     *
     * @param[position] The position of the crossword in the recycler view and array list.
     */
    fun showCrossword(position: Int) {
        val crosswordFile = crosswordFiles[position].id
        val resourceId = resources.getIdentifier(crosswordFile, "raw", requireContext().packageName)
        val crossword = readPuzzle(resourceId)

        crosswordView.let { cv ->
            cv.crossword = crossword
            cv.onLongPressListener = this
            cv.onStateChangeListener = this
            cv.onSelectionChangeListener = this

            cv.inputValidator = { ch ->
                !ch.first().isISOControl()
            }

            onSelectionChanged(cv, cv.selectedWord, cv.selectedCell)
        }

        recyclerView.visibility = View.GONE

        crosswordView.visibility = View.VISIBLE
        hint.visibility = View.VISIBLE
        solveWordButton.visibility = View.VISIBLE
        backButton.visibility = View.VISIBLE
    }


    /**
     * An empty method that needed to be included.
     */
    override fun onCrosswordChanged(view: CrosswordView) {}


    /**
     * A method to let the user know when the crossword is solved.
     *
     * @param[view] The crossword view.
     */
    override fun onCrosswordSolved(view: CrosswordView) {
        Toast.makeText(activity, "Puzzle Solved",
            Toast.LENGTH_LONG).show()
    }


    /**
     * An empty method that needed to be included.
     */
    override fun onCrosswordUnsolved(view: CrosswordView) {}


    /**
     * A method which reads the crossword puzzle file and converts it to a crossword on screen.
     *
     * @param[resourceId] The crossword file to be read from in the raw directory.
     *
     * @return[Crossword] The crossword puzzle.
     */
    private fun readPuzzle(resourceId: Int): Crossword =
        resources.openRawResource(resourceId).use { s ->
            buildCrossword {
                PuzFormatter().read(this, s)
            }
        }


    /**
     * A method to change the word hint when the user selects a new word to edit.
     *
     * @param[view] The crossword view.
     * @param[word] The selected word.
     * @param[position] The word's position.
     */
    override fun onSelectionChanged(view: CrosswordView,
                                    word: Crossword.Word?, position: Int) {
        var hintText = ""
        currentWord = word

        // Gets the hint depending on if the user is solving an across or down word.
        when (word!!.direction) {
            Crossword.Word.DIR_ACROSS -> hintText = word.number.toString() + ". Across -- " + word.hint

            Crossword.Word.DIR_DOWN -> hintText = word.number.toString() + ". Down -- " + word.hint

        }

        hint.text = hintText
    }


    /**
     * An empty method that needed to be included.
     */
    override fun onCellLongPressed(view: CrosswordView, word: Crossword.Word, cell: Int) {}
}