package com.thorangs.couchpotato.backend.syncing

import io.reactivex.Observable
import retrofit2.http.Body
import retrofit2.http.POST

interface ReferApiService {

    @POST("user/referlink")
    fun getReferralLink(@Body userId: Long): Observable<String>
}