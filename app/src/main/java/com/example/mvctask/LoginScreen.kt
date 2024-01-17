package com.example.mvctask

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.room.Room
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class LoginScreen : AppCompatActivity() {

    lateinit var appDatabase: AppDatabase
    private lateinit var userController: UserController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login_screen)

        // Initialize Room Database
        appDatabase = Room.databaseBuilder(
            applicationContext,
            AppDatabase::class.java,
            "app-database"
        ).build()

        userController = UserController(appDatabase.userDao())

        var btn_signup = findViewById<TextView>(R.id.sign_up_btn)
        var btn_login = findViewById<Button>(R.id.login_btn)

        var email_et_lp = findViewById<EditText>(R.id.lp_email_et)
        var password_et_lp = findViewById<EditText>(R.id.lp_password_et)

        val emailErrorLoginTv = findViewById<TextView>(R.id.tv_error_email_login)
        val passwordErrorLoginTv = findViewById<TextView>(R.id.tv_error_password_login)


        btn_signup.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }

        btn_login.setOnClickListener {
            val email = email_et_lp.text.toString()
            val password = password_et_lp.text.toString()

            if (email.isEmpty()){
                emailErrorLoginTv.visibility = View.VISIBLE
                emailErrorLoginTv.setText("Email can not be empty")
            } else {
                emailErrorLoginTv.setText("")
                emailErrorLoginTv.visibility = View.GONE

            }

            if (password.isEmpty()){
                passwordErrorLoginTv.visibility = View.VISIBLE
                passwordErrorLoginTv.setText("Password can not be empty")
            } else {
                passwordErrorLoginTv.setText("")
                passwordErrorLoginTv.visibility = View.GONE

            }

            if (
                (emailErrorLoginTv.visibility == View.GONE) &&
                (passwordErrorLoginTv.visibility == View.GONE)
            ) {

                CoroutineScope(Dispatchers.Main).launch {
                    val loggedInUser = userController.loginUser(email, password)

                    if (loggedInUser != null) {
                        Toast.makeText(applicationContext, "Login Successful", Toast.LENGTH_SHORT)
                            .show()

                        // Handle navigation or any other action after successful login
                        val home_intent = Intent(applicationContext, MainActivity2::class.java)
                        home_intent.putExtra("userName", loggedInUser.name)
                        startActivity(home_intent)
                        finish()
                    } else {
                        Toast.makeText(
                            applicationContext,
                            "Login failed. You entered wrong email or password",
                            Toast.LENGTH_LONG)
                            .show()
                    }
                }
            }




        }


    }
}