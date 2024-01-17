package com.example.mvctask

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.room.Room
import com.google.firebase.FirebaseException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit

class MainActivity : AppCompatActivity() {

    //lateinit var appDatabase: AppDatabase
    //private lateinit var userController: UserController

    //private lateinit var auth: FirebaseAuth
    //private lateinit var verificationId: String




    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

//        // Initialize Room Database
//        appDatabase = Room.databaseBuilder(
//            applicationContext,
//            AppDatabase::class.java,
//            "app-database"
//        ).build()

//        userController = UserController(appDatabase.userDao())
        //auth = FirebaseAuth.getInstance()



        val etName = findViewById<EditText>(R.id.name_et)
        val etEmail = findViewById<EditText>(R.id.email_et)
        val etNumber = findViewById<EditText>(R.id.number_et)
        val etPassword = findViewById<EditText>(R.id.password_et)
        val cbTerms = findViewById<CheckBox>(R.id.terms_cb)
        val btnSignup = findViewById<Button>(R.id.signup_btn)

        val tvErrorName = findViewById<TextView>(R.id.name_error_tv)
        val tvErrorEmail = findViewById<TextView>(R.id.email_error_tv)
        val tvErrorNumber = findViewById<TextView>(R.id.number_error_tv)
        val tvErrorPassword = findViewById<TextView>(R.id.password_error_tv)


        var btn_signin = findViewById<TextView>(R.id.sign_in_btn)

        btn_signin.setOnClickListener {
            val intent = Intent(this, LoginScreen::class.java)
            startActivity(intent)
        }


        btnSignup.setOnClickListener {

            val name = etName.text.toString()
            val email = etEmail.text.toString()
            val number = etNumber.text.toString()
            val password = etPassword.text.toString()

            if (name.isEmpty()) {
                // Show error message for missing fields
                Toast.makeText(applicationContext, "Some fields are empty", Toast.LENGTH_SHORT)
                    .show()
                tvErrorName.visibility = View.VISIBLE
                tvErrorName.setText("Name is empty")
            } else {
                tvErrorName.setText("")
                tvErrorName.visibility = View.GONE

            }

            if (email.isEmpty()){
                tvErrorEmail.visibility = View.VISIBLE
                tvErrorEmail.setText("Email is empty")
            } else if(email.contains('@') && email.endsWith(".com")){
                tvErrorEmail.setText("Your email is invalid")
            } else {
                tvErrorEmail.setText("")
                tvErrorEmail.visibility = View.GONE
            }

            if (number.isEmpty()){
                tvErrorNumber.visibility = View.VISIBLE
                tvErrorNumber.setText("Number is empty")
            } else if(number.length != 13 && !(number.startsWith("+92"))){
                tvErrorNumber.visibility = View.VISIBLE
                tvErrorNumber.setText("Please enter number with in this format: +92__________")
            } else {
                tvErrorNumber.setText("")
                tvErrorNumber.visibility = View.GONE
            }

            if (password.isEmpty()){
            tvErrorPassword.visibility = View.VISIBLE
            tvErrorPassword.setText("Password is empty")
            } else {
                tvErrorPassword.setText("")
                tvErrorPassword.visibility = View.GONE

            }
            if (!(cbTerms.isChecked)) {
                Toast.makeText(applicationContext, "Please agree with the Terms of Service and Privacy Policy",
                    Toast.LENGTH_SHORT)
                    .show()

            }


            if (
                (tvErrorName.visibility == View.GONE) &&
                (tvErrorEmail.visibility == View.GONE) &&
                (tvErrorNumber.visibility == View.GONE) &&
                (tvErrorPassword.visibility == View.GONE) &&
                cbTerms.isChecked
                ) {


                val otp_verify = Intent(this, PhoneVerification::class.java)
                otp_verify.putExtra("userName", name)
                otp_verify.putExtra("num", number)
                otp_verify.putExtra("email", email)
                otp_verify.putExtra("password", password)
                startActivity(otp_verify)

//                    //Proceed with signup process
//                CoroutineScope(Dispatchers.Main).launch {
//                    val registrationSuccessful =
//                        userController.registerUser(name, email, password)
//
//                    if (registrationSuccessful) {
//                        Toast.makeText(applicationContext, "Signup succeeded", Toast.LENGTH_SHORT)
//                            .show()
//
//                        etName.setText("")
//                        etEmail.setText("")
//                        etPassword.setText("")
//
//                        // Handle navigation or any other action after successful registration
//                        val home = Intent(applicationContext, MainActivity2::class.java)
//                        home.putExtra("userName", name)
//                        startActivity(home)
//                    } else {
//                        Toast.makeText(applicationContext, "Registration failed", Toast.LENGTH_SHORT)
//                            .show()
//                    }
//                }
            }


        }
    }




}

