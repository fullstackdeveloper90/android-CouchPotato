package com.thorangs.couchpotato.backend.syncing

import com.thorangs.couchpotato.database.StepsLog
import com.thorangs.couchpotato.database.StepsLogDTO
import io.reactivex.Observable
import io.reactivex.Single
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.Field
import retrofit2.http.Headers
import retrofit2.http.POST

/**
 * Created by Laxman Bhattarai on 11/18/17.
 * erluxman@gmail.com
 * https://github.com/erluxman
 * https://twitter.com/erluxman
 */

interface StepsSyncService {
    @POST("user/step")
    fun getAllSteps(@Body userId: Long): Observable<List<StepsLogDTO>>

    @POST("user/steps/all")
    fun getStepsOfDay(@Field("user_id") userId: Long,
                      @Field("date") dateString: String): Single<StepsLog>

    @Headers("Content-Type: application/json")
    @POST("step/update")
    fun saveSteps(
            @Body step: Step): Call<String?>

}