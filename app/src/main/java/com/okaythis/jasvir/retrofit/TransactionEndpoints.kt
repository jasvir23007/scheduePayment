package com.okaythis.jasvir.retrofit

import com.okaythis.jasvir.data.model.AuthorizationResponse
import com.okaythis.jasvir.data.model.OkayLinking
import retrofit2.Call
import retrofit2.Response
import retrofit2.http.POST
import retrofit2.http.Query


interface TransactionEndpoints {

    @POST("/link")
    suspend fun linkUser(@Query("userExternalId") userExternalId: String?): Response<OkayLinking>

    @POST("/auth")
    fun authorizeTransaction(@Query("userExternalId") userExternalId: String?): Call<AuthorizationResponse>

    @POST("/auth/pin")
    fun authorizePinTransaction(@Query("userExternalId") userExternalId: String?): Call<AuthorizationResponse>
}