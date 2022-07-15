package com.okaythis.jasvir.ui

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.okaythis.jasvir.data.repository.TransactionRepo
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(val transactionRepo: TransactionRepo) : ViewModel() {
    private val _transactions = MutableLiveData<ArrayList<String>>()
    val transactions: MutableLiveData<ArrayList<String>> = _transactions


    private val _linkingCode = MutableLiveData<String>()
    val linkingCode: MutableLiveData<String> = _linkingCode


    init {
        transactions.value = transactionRepo.preferenceRepo.getArrayList()
    }

     fun startServerLinking(userExternalId: String?) {
       viewModelScope.launch {
           linkingCode.value =  transactionRepo.startServerLinking(userExternalId)
       }

    }

  fun updateListTransaction(entry:String){
      transactions.value = transactionRepo.insertList(entry)
  }


}