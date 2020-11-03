package com.coaker.newsaggregatorapp.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.coaker.newsaggregatorapp.R

class HomeFragment : Fragment() {
    private lateinit var homeViewModel: HomeViewModel

    private lateinit var root: View

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        homeViewModel =
                ViewModelProvider(this).get(HomeViewModel::class.java)

        root = inflater.inflate(R.layout.fragment_home_recycler, container, false)

        return root
    }

}
