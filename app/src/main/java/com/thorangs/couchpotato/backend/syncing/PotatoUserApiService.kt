package com.thorangs.couchpotato.backend.syncing

import com.thorangs.couchpotato.models.user.PotatoUser
import io.reactivex.Observable
import retrofit2.http.GET

interface PotatoUserApiService {

    @GET("step/leaderboard")
    fun getAllUsers(): Observable<List<PotatoUser>>

}