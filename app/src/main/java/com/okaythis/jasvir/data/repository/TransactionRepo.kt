package com.okaythis.jasvir.data.repository

import android.widget.Toast
import com.okaythis.jasvir.data.model.AuthorizationResponse
import com.okaythis.jasvir.data.model.OkayLinking
import com.okaythis.jasvir.retrofit.RetrofitWrapper
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class TransactionRepo {

    private val retrofitWrapper = RetrofitWrapper()
    private val transactionHandler =  retrofitWrapper.handleTransactionEndpoints()

    private suspend fun startServerAuthorization(userExternalId: String?)  {
        transactionHandler.authorizeTransaction(userExternalId).enqueue(object:
            Callback<AuthorizationResponse> {
            override fun onFailure(call: Call<AuthorizationResponse>, t: Throwable) {
            }

            override fun onResponse(
                call: Call<AuthorizationResponse>,
                response: Response<AuthorizationResponse>
            ) {


            }

        })
    }

    private suspend fun startServerLinking(userExternalId: String?): String? {
        return transactionHandler.linkUser(userExternalId).body()?.linkingCode
    }

    private fun startPinAuthorization(userExternalId: String?) {
        transactionHandler.authorizePinTransaction(userExternalId).enqueue(object:
            Callback<AuthorizationResponse> {
            override fun onFailure(call: Call<AuthorizationResponse>, t: Throwable) {
               // Toast.makeText(this@MainActivity, "Error making request to Server", Toast.LENGTH_LONG).show()
            }

            override fun onResponse(
                call: Call<AuthorizationResponse>,
                response: Response<AuthorizationResponse>
            ) {
                //Toast.makeText(this@MainActivity, "Request made successfully ", Toast.LENGTH_LONG).show()
            }

        })
    }


}