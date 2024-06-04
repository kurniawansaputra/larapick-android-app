package id.go.kebumenkab.larapick.data.retrofit

import id.go.kebumenkab.larapick.data.response.DefaultResponse
import id.go.kebumenkab.larapick.data.response.PickupLogsResponse
import id.go.kebumenkab.larapick.data.response.UserResponse
import retrofit2.Call
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface ApiService {
    @FormUrlEncoded
    @POST("login")
    fun login(
        @Field("email") email: String,
        @Field("password") password: String
    ): Call<UserResponse>

    @POST("logout")
    fun logout(
        @Header("Authorization") token: String
    ): Call<UserResponse>

    @FormUrlEncoded
    @POST("update-password")
    fun updatePassword(
        @Header("Authorization") token: String,
        @Field("old_password") oldPassword: String,
        @Field("new_password") newPassword: String
    ): Call<DefaultResponse>

    @GET("pickup-logs")
    fun getPickupLogs(
        @Header("Authorization") token: String,
        @Query("year") year: Int,
        @Query("month") month: Int
    ): Call<PickupLogsResponse>

    @GET("user/{qr-code}")
    fun getGuardianByQrCode(
        @Header("Authorization") token: String,
        @Path("qr-code") qrCode: String,
    ): Call<UserResponse>

    @FormUrlEncoded
    @POST("pickup")
    fun pickup(
        @Header("Authorization") token: String,
        @Field("student_id") studentId: Int,
        @Field("guardian_id") guardianId: Int,
        @Field("admin_id") adminId: Int
    ): Call<DefaultResponse>
}