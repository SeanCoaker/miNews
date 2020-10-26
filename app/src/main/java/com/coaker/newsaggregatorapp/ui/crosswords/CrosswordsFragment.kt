package com.coaker.newsaggregatorapp.ui.crosswords

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.coaker.newsaggregatorapp.R

class CrosswordsFragment : Fragment() {

    private lateinit var crosswordsViewModel: CrosswordsViewModel

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        crosswordsViewModel =
                ViewModelProvider(this).get(CrosswordsViewModel::class.java)
        val root = inflater.inflate(R.layout.fragment_slideshow, container, false)
        val textView: TextView = root.findViewById(R.id.text_slideshow)
        crosswordsViewModel.text.observe(viewLifecycleOwner, {
            textView.text = it
        })
        return root
    }
}