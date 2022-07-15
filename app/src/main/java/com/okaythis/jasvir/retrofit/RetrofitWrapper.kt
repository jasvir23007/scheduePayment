package com.okaythis.jasvir.retrofit

import okhttp3.FormBody
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory


class RetrofitWrapper {

    private val BASE_URL = "https://demostand.okaythis.com/"
   // private val BASE_URL = "http://10.0.2.2:4000/"

    private fun createClient(): Retrofit {
        val interceptor = HttpLoggingInterceptor()
        interceptor.setLevel(HttpLoggingInterceptor.Level.BODY)
        val client: OkHttpClient = OkHttpClient().newBuilder().addInterceptor(interceptor).build()


        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    fun handleTransactionEndpoints(): TransactionEndpoints {
        val retrofit: Retrofit = this.createClient()
        return retrofit.create(TransactionEndpoints::class.java)
    }
}