package com.coaker.newsaggregatorapp.ui.crosswords

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class CrosswordsViewModel : ViewModel() {

    private val _text = MutableLiveData<String>().apply {
        value = "This is a crosswords Fragment"
    }
    val text: LiveData<String> = _text
}