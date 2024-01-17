package com.example.mvctask

import RetrofitInstance.apiUserService
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MainActivity2 : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var apiUserAdapter: ApiUserAdapter
    private lateinit var userController: UserController
    private lateinit var apiUserController: ApiUserController
    private lateinit var progressBar: ProgressBar
    private lateinit var addUserBtn: FloatingActionButton


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main2)
        Log.d("onCreate", "MainActivity2 is created")

        recyclerView = findViewById(R.id.recyclerView)
        progressBar = findViewById(R.id.progressBar)
        addUserBtn = findViewById(R.id.add_fab)


        val btnLogout = findViewById<ImageButton>(R.id.btnLogout)
        val userTv = findViewById<TextView>(R.id.user_tv)

        val userName = intent.getStringExtra("userName")
        userTv.setText("Hello $userName")



        // Initialize API user adapter
        apiUserAdapter = ApiUserAdapter(
            userList = emptyList(),
            onItemClickListener = { apiUser -> /* Handle click */ },
            onDeleteClickListener = { apiUser -> deleteUser(apiUser) },
            onEditClickListener = { apiUser -> showEditDialog(apiUser) }
        )

        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = apiUserAdapter
        userController = UserController(AppDatabase.getInstance(this).userDao())
        apiUserController = ApiUserController(apiUserService)
        //userController = UserController(AppDatabase.getInstance(this).u)

        btnLogout.setOnClickListener(){
            showLogoutConfirmationDialog()
        }

        addUserBtn.setOnClickListener(){
            showAddUserDialog()
        }


        // Fetch data from API and update RecyclerView
        fetchDataAndUpdateUI()
    }

    private fun fetchDataAndUpdateUI() {
        CoroutineScope(Dispatchers.Main).launch {
            try {
                // Showing the progress bar while data is being fetched
                progressBar.visibility = View.VISIBLE

                val apiUsers = apiUserController.getApiUsersFromApi()
                apiUserAdapter.setApiUsers(apiUsers)
            } catch (e: Exception) {

                Toast.makeText(applicationContext, "Failed to fetch API data", Toast.LENGTH_SHORT).show()
            } finally {
                // Hiding the progress bar after fetching data
                progressBar.visibility = View.GONE
            }
        }
    }



    private fun deleteUser(apiUser: ApiUser) {
        AlertDialog.Builder(this)
            .setTitle("Confirm Deletion")
            .setMessage("Are you sure you want to delete this user?")
            .setPositiveButton("Delete") { _, _ ->
                CoroutineScope(Dispatchers.Main).launch {
                    try {
                        val isDeleted = apiUserController.deleteApiUser(apiUser.id)

                        if (isDeleted) {
                            Toast.makeText(applicationContext, "User deleted from API", Toast.LENGTH_SHORT).show()
                            // Optionally, remove the user from the adapter's data and notify
                            apiUserAdapter.removeApiUser(apiUser)
                        } else {
                            Toast.makeText(applicationContext, "Failed to delete user from API", Toast.LENGTH_SHORT).show()
                        }
                    } catch (e: Exception) {
                        // Handle exception
                        Toast.makeText(applicationContext, "Error deleting user: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }


    private fun showEditDialog(apiUser: ApiUser) {

        Log.d("user_details", apiUser.name)
        val dialogBuilder = AlertDialog.Builder(this)
        dialogBuilder.setTitle("Edit User Details")
        //dialogBuilder.setMessage("Edit user details")

        // Inflate your custom layout for editing user details, add EditTexts for name and email
        val dialogView = layoutInflater.inflate(R.layout.dialog_edit_user, null)
        dialogBuilder.setView(dialogView)
        val updatedNameEditText = dialogView.findViewById<EditText>(R.id.editName)
        val updatedEmailEditText = dialogView.findViewById<EditText>(R.id.editEmail)

        updatedNameEditText.setText(apiUser.name)
        updatedEmailEditText.setText((apiUser.email))

        dialogBuilder.setPositiveButton("Update") { _, _ ->
            // Handle the positive button click
            try {
                val updatedNameEditText = dialogView.findViewById<EditText>(R.id.editName)
                val updatedEmailEditText = dialogView.findViewById<EditText>(R.id.editEmail)

//                updatedNameEditText.setText(apiUser.name)
//                updatedEmailEditText.setText((apiUser.email))


                // Launch a coroutine to update user details on the API and locally
                CoroutineScope(Dispatchers.Main).launch {
                    try {

                        val updatedName = updatedNameEditText.text.toString()
                        val updatedEmail = updatedEmailEditText.text.toString()

                        var newUpdatedUser = apiUser.copy(
                            apiUser.id,
                            name = updatedName,
                            email = updatedEmail
                        )

                        val isUpdateSuccessful = apiUserController.updateApiUser(newUpdatedUser)

                        if (isUpdateSuccessful) {

                            apiUserAdapter.updateApiUser(newUpdatedUser)

                            Log.d("EditDialog", "Updated Name: $updatedName, Updated Email: $updatedEmail")

                            // Show a toast indicating success
                            Toast.makeText(applicationContext, "Successfully updated", Toast.LENGTH_SHORT).show()
                        } else {
                            // Handle case where update failed
                            Toast.makeText(applicationContext, "Update failed", Toast.LENGTH_SHORT).show()
                        }
                    } catch (e: Exception) {
                        // Handle exception
                        Log.e("EditDialog", "Error updating user", e)
                        Toast.makeText(applicationContext, "Error updating user", Toast.LENGTH_SHORT).show()
                    }
                }

            } catch (e: Exception) {
                // Handle exception
                Log.e("EditDialog", "Error updating user", e)
                Toast.makeText(applicationContext, "Error updating user", Toast.LENGTH_SHORT).show()
            }
        }
        dialogBuilder.setNegativeButton("Cancel", null)

        val dialog = dialogBuilder.create()
        dialog.show()

        fetchDataAndUpdateUI()
    }

    private fun showLogoutConfirmationDialog() {
            AlertDialog.Builder(this)
                .setTitle("Logout")
                .setMessage("Are you sure you want to logout?")
                .setPositiveButton("Yes") { _, _ ->
                    // Handle logout
                    CoroutineScope(Dispatchers.Main).launch {
                        val loggedInUser = userController.getLoggedInUser()
                        loggedInUser?.let {
                            it.isLoggedIn = false
                            userController.logoutUser()
                        }
                        // After updating local database, navigate to the login screen
                        val login_intent = Intent(applicationContext, LoginScreen::class.java)
                        startActivity(login_intent)
                        finish()
                    }
                }
                .setNegativeButton("No", null)
                .show()

    }

    private fun showAddUserDialog() {
        val dialogBuilder = AlertDialog.Builder(this)
        dialogBuilder.setTitle("Add User")
        dialogBuilder.setMessage("Add User Details")

        // Inflate your custom layout for adding user details, add EditTexts for name and email
        val dialogView = layoutInflater.inflate(R.layout.dialog_add_user, null)
        dialogBuilder.setView(dialogView)

        dialogBuilder.setPositiveButton("Add") { _, _ ->
            CoroutineScope(Dispatchers.Main).launch {
                try {
                    val updatedNameEditText = dialogView.findViewById<EditText>(R.id.addName)
                    val updatedEmailEditText = dialogView.findViewById<EditText>(R.id.addEmail)

                    val updatedName = updatedNameEditText.text.toString()
                    val updatedEmail = updatedEmailEditText.text.toString()

                    val newUser = ApiUser(id = 0, name = updatedName, email = updatedEmail)

                    val createdUser = apiUserController.createApiUser(newUser)

                    if (createdUser != null) {
                        apiUserAdapter.addApiUser(createdUser)
                        Toast.makeText(
                            applicationContext,
                            "User created successfully",
                            Toast.LENGTH_SHORT
                        ).show()
                    } else {
                        Toast.makeText(
                            applicationContext,
                            "Failed to create user",
                            Toast.LENGTH_SHORT
                        ).show()
                    }

                } catch (e: Exception) {
                    // Handle exception
                    Log.e("AddUserDialog", "Error creating user", e)
                    Toast.makeText(applicationContext, "Error creating user", Toast.LENGTH_SHORT)
                        .show()
                }
            }
        }

                dialogBuilder.setNegativeButton("Cancel", null)

                val dialog = dialogBuilder.create()
                dialog.show()

        }




}
