package com.okaythis.jasvir.di

import android.content.Context
import com.okaythis.jasvir.data.repository.PreferenceRepo
import com.okaythis.jasvir.data.repository.TransactionRepo
import com.okaythis.jasvir.retrofit.RetrofitWrapper
import com.okaythis.jasvir.retrofit.TransactionEndpoints
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
object ScheduleModule {

    @Provides
    fun providesChannelRepository(@ApplicationContext ctx: Context): TransactionRepo {
        return TransactionRepo(getTransationHandler(), getPrefsRepo(ctx))
    }


    @Provides
    fun getRetrofitWrapper():RetrofitWrapper{
        return RetrofitWrapper()
    }

    @Provides
    fun getPrefsRepo(@ApplicationContext appContext: Context): PreferenceRepo {
        return PreferenceRepo(appContext)
    }

    @Provides
    @Singleton
    fun getTransationHandler(): TransactionEndpoints {
        return getRetrofitWrapper().handleTransactionEndpoints()

    }


}