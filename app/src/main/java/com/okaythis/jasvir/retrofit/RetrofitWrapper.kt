package com.okaythis.jasvir.retrofit

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class RetrofitWrapper {

    private val BASE_URL = "http://okayserversample.herokuapp.com/"
   // private val BASE_URL = "http://10.0.2.2:4000/"

    private fun createClient(): Retrofit {
        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    fun handleTransactionEndpoints(): TransactionEndpoints {
        val retrofit: Retrofit = this.createClient()
        return retrofit.create(TransactionEndpoints::class.java)
    }
}