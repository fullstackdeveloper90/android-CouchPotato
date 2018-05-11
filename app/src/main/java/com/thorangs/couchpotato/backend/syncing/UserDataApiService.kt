package com.thorangs.couchpotato.backend.syncing

import android.support.annotation.NonNull
import com.thorangs.couchpotato.models.user.*
import io.reactivex.Observable
import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST
import retrofit2.http.Path

interface UserDataApiService {

    @Headers("Content-Type: application/json")
    @POST("user/signup")
    fun signUp(@NonNull @Body user: SignUpUser): Observable<String>

    @Headers("Content-Type: application/json")
    @POST("user/buddyemail/update")
    fun updateBuddyEmail(@NonNull @Body buddy: BuddyEmailSignInUser): Observable<String>

    @Headers("Content-Type: application/json")
    @POST("user/steptarget/update")
    fun updateStepTarget(@NonNull @Body user: TargetStepsSignInUser): Observable<String>

    @Headers("Content-Type: application/json")
    @POST("user/signin")
    fun signIn(@NonNull @Body user: SignInUser): Observable<UserDTO>

    @Headers("Content-Type: application/json")
    @POST("user/forgotpassword")
    fun forgotPasswordRequest(@NonNull @Body userEmail: String): Observable<String>

    @Headers("Content-Type: application/json")
    @POST("user/changepassword")
    fun changePassword(@NonNull @Body user: PasswordChangeUser): Observable<String>

    @Headers("Content-Type: application/json")
    @POST("user/update")
    fun updateUser(@Body user: UserDTO): Observable<String>

    @Headers("Content-Type: application/json")
    @POST("user/validatetoken/{key}")
    fun verifyKey(@Path("key") key: String, @NonNull @Body email: String): Observable<String>

    @Headers("Content-Type: application/json")
    @POST("user/isactive")
    fun isActive( @NonNull @Body email: String): Observable<Boolean>

    @Headers("Content-Type: application/json")
    @POST("user/setactive/{isActive}")
    fun setActive(@Path("isActive") isActive: Boolean, @NonNull @Body email: String): Observable<String>

    @Headers("Content-Type: application/json")
    @POST("user/setpassword")
    fun setPasswordWithToken(@NonNull @Body user: PasswordSetSignInUser): Observable<UserDTO>
}