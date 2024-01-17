package com.example.mvctask

import com.example.mvctask.ApiUser
import retrofit2.http.*

interface ApiUserService {

    @GET("users")
    suspend fun getApiUsers(): List<ApiUser>

    @POST("users")
    suspend fun createApiUser(@Body apiUser: ApiUser): ApiUser

    @PUT("users/{id}")
    suspend fun updateApiUser(@Path("id") userId: Int, @Body apiUser: ApiUser): ApiUser
//    @PUT("users/{id}")
//    Call<User> updateUser(@Path("id") int userId, @Body User user);


    @DELETE("users/{id}")
    suspend fun deleteApiUser(@Path("id") userId: Int): Unit
}
