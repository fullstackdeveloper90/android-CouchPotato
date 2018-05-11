package com.thorangs.couchpotato.backend

import com.google.gson.GsonBuilder
import com.thorangs.couchpotato.backend.syncing.PotatoUserApiService
import com.thorangs.couchpotato.backend.syncing.StepsSyncService
import com.thorangs.couchpotato.backend.syncing.UserDataApiService
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit


/**
 * Created by Laxman Bhattarai on 11/18/17.
 * erluxman@gmail.com
 * https://github.com/erluxman
 * https://twitter.com/erluxman
 */
class RestClient private constructor() {
    val stepSyncService: StepsSyncService = retrofit.create(StepsSyncService::class.java)
    val potatoUserApiService: PotatoUserApiService = retrofit.create(PotatoUserApiService::class.java)
    val userDataApiService: UserDataApiService = retrofit.create(UserDataApiService::class.java)

    companion object {
        private val gson = GsonBuilder()
                .setLenient()
                .create()!!

        private val httpClient = OkHttpClient.Builder()
                .connectTimeout(75, TimeUnit.SECONDS)
                .readTimeout(60, TimeUnit.SECONDS)
                .writeTimeout(60, TimeUnit.SECONDS)
                .build()

        private val retrofit = Retrofit.Builder()
                .baseUrl(baseUrl)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .client(httpClient)
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .build()

        private val client = RestClient()

        fun instance(): RestClient = client
    }

}