package com.example.mvctask

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class UserController(private val userDao: UserDao) {

    // Function to register a new user
    suspend fun registerUser(name: String, email: String, number: String, password: String): Boolean {
        return withContext(Dispatchers.IO) {
            val user = User(name = name, email = email, number = number, password = password)
            userDao.insert(user)
            true
        }
    }

    // Function to login a user
    suspend fun loginUser(emailPhone: String, password: String): User? {
        return withContext(Dispatchers.IO) {
            val loggedInUser = userDao.login(emailPhone, password)
            loggedInUser?.let {
                it.isLoggedIn = true
                userDao.update(it)
            }
            loggedInUser
        }
    }

    //User LogOut Function
    suspend fun logoutUser() {
        withContext(Dispatchers.IO) {
            val loggedInUser = userDao.getLoggedInUser()
            loggedInUser?.let {
                it.isLoggedIn = false
                userDao.update(it)
            }
        }
    }

    suspend fun getLoggedInUser(): User? {
        return userDao.getLoggedInUser()
    }


//    // Function to get users from the API
//    suspend fun getApiUsersFromApi(): List<ApiUser> {
//        return try {
//            RetrofitInstance.apiUserService.getApiUsers()
//        } catch (e: Exception) {
//            // Handle API call exception
//            emptyList()
//        }
//    }
//
//    // Function to update a user on the API without updating local database
//    suspend fun updateApiUser(apiUser: ApiUser): Boolean {
//        return try {
//            // Call the API to update the user
//            RetrofitInstance.apiUserService.updateApiUser(
//                apiUser.id,
//                ApiUser(id = apiUser.id, name = apiUser.name, email = apiUser.email)
//            )
//
//            true
//        } catch (e: Exception) {
//            // Handle API call exception
//            false
//        }
//    }
//
//    // Function to delete a user on the API without updating local database
//    suspend fun deleteApiUser(userId: Int): Boolean {
//        return try {
//            // Call the API to delete the user
//            RetrofitInstance.apiUserService.deleteApiUser(userId)
//            Log.d("--delete", "userId")
//
//            true
//        } catch (e: Exception) {
//            // Handle API call exception
//            false
//        }
//    }

}
