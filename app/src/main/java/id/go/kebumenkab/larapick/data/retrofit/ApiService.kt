package id.go.kebumenkab.larapick.data.retrofit

import id.go.kebumenkab.larapick.data.response.DefaultResponse
import id.go.kebumenkab.larapick.data.response.GradeResponse
import id.go.kebumenkab.larapick.data.response.PickupLogResponse
import id.go.kebumenkab.larapick.data.response.StudentResponse
import id.go.kebumenkab.larapick.data.response.UserResponse
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
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

    @GET("pickup-logs-by-student")
    fun getPickupLogs(
        @Header("Authorization") token: String,
        @Query("student_id") studentId: Int,
        @Query("year") year: Int,
        @Query("month") month: Int,
        @Query("status") status: String
    ): Call<PickupLogResponse>

    @GET("user/{qr-code}")
    fun getGuardianByQrCode(
        @Header("Authorization") token: String,
        @Path("qr-code") qrCode: String,
    ): Call<UserResponse>

    @Multipart
    @POST("pickup")
    fun pickup(
        @Header("Authorization") token: String,
        @Part("student_id") studentId: RequestBody,
        @Part("guardian_id") guardianId: RequestBody? = null,
        @Part("admin_id") adminId: RequestBody,
        @Part("status") status: RequestBody,
        @Part("note") note: RequestBody? = null,
        @Part file: MultipartBody.Part? = null
    ): Call<DefaultResponse>

    @FormUrlEncoded
    @POST("update-pickup-status")
    fun updatePickupStatus(
        @Header("Authorization") token: String,
        @Field("pickup_id") pickupId: Int,
        @Field("guardian_id") guardianId: Int,
        @Field("status") status: String
    ): Call<DefaultResponse>

    @GET("grades")
    fun getGrades(
        @Header("Authorization") token: String,
    ): Call<GradeResponse>

    @GET("students-by-grade")
    fun getStudentByGrade(
        @Header("Authorization") token: String,
        @Query("grade_id") gradeId: Int,
    ): Call<StudentResponse>
}