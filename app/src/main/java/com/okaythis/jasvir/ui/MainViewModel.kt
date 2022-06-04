package com.okaythis.jasvir.ui

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class MainViewModel:ViewModel() {

    private val _transactions = MutableLiveData<ArrayList<String>>()
    val transactions:MutableLiveData<ArrayList<String>> = _transactions


    init {
        val list = ArrayList<String>()
        list.add("123")
        transactions.value = list
    }

}