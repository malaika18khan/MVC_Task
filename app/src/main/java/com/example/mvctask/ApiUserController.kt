package com.example.mvctask

import RetrofitInstance
import android.util.Log

class ApiUserController(private val apiUserService: ApiUserService) {

    // Function to get users from the API
    suspend fun getApiUsersFromApi(): List<ApiUser> {
        return try {
            RetrofitInstance.apiUserService.getApiUsers()
        } catch (e: Exception) {
            // Handle API call exception
            emptyList()
        }
    }

    // Function to update a user on the API
    suspend fun updateApiUser(apiUser: ApiUser): Boolean {
        return try {
            // Call the API to update the user
            RetrofitInstance.apiUserService.updateApiUser(
                apiUser.id,
                ApiUser(id = apiUser.id, name = apiUser.name, email = apiUser.email)
            )
            true
        } catch (e: Exception) {
            // Handle API call exception
            false
        }
    }

    // Function to delete a user on the API without updating local database
    suspend fun deleteApiUser(userId: Int): Boolean {
        return try {
            // Call the API to delete the user
            RetrofitInstance.apiUserService.deleteApiUser(userId)
            Log.d("--delete", "userId: $userId")

            true
        } catch (e: Exception) {
            // Handle API call exception
            false
        }
    }

    //Function to add user on the API
    suspend fun createApiUser(apiUser: ApiUser): ApiUser? {
        return try {
            RetrofitInstance.apiUserService.createApiUser(apiUser)
        } catch (e: Exception) {
            // Handle API call exception
            null
        }
    }
}