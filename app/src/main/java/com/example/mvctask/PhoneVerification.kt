package com.example.mvctask

import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.os.CountDownTimer
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.room.Room
import com.google.android.gms.auth.api.phone.SmsRetriever
import com.google.firebase.FirebaseException
import com.google.firebase.FirebaseTooManyRequestsException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthMissingActivityForRecaptchaException
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit
import java.util.regex.Pattern

class PhoneVerification : AppCompatActivity() {

    private val REQ_USER_CONSENT = 200
    var smsBroadcastReceiver: SmsBroadcastReceiver? = null

    private lateinit var auth: FirebaseAuth
    private lateinit var userController: UserController
    lateinit var appDatabase: AppDatabase

    private lateinit var digit1_et: EditText
    private lateinit var digit2_et: EditText
    private lateinit var digit3_et: EditText
    private lateinit var digit4_et: EditText
    private lateinit var digit5_et: EditText
    private lateinit var digit6_et: EditText

    private lateinit var codeBar: ProgressBar
    private lateinit var timer: CountDownTimer
    private var verificationId: String? = null
    private lateinit var timerTv: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_phone_verification)
        Log.d("onCreate", "OTP activity is creating")

        // Initialize Room Database
        appDatabase = Room.databaseBuilder(
            applicationContext,
            AppDatabase::class.java,
            "app-database"
        ).build()

        auth = FirebaseAuth.getInstance()
        userController = UserController(appDatabase.userDao())

        digit1_et = findViewById(R.id.etDigit1)
        digit2_et = findViewById(R.id.etDigit2)
        digit3_et = findViewById(R.id.etDigit3)
        digit4_et = findViewById(R.id.etDigit4)
        digit5_et = findViewById(R.id.etDigit5)
        digit6_et = findViewById(R.id.etDigit6)

        startSmartUserConsent()
        registerBroadcastReceiver()

        codeBar = findViewById(R.id.code_pb)

        var verifyBtn = findViewById<Button>(R.id.verify_btn)
        timerTv = findViewById(R.id.timer_tv)

        var phoneNumber = intent.getStringExtra("num")!!



        if (phoneNumber != null) {
            sendVerificationCode(phoneNumber)
            Log.d("SendingOTP", "Sending number for getting otp")

            // Set up listeners to move focus between OTP input blocks
            setUpOtpInputFocus()
        }



        verifyBtn.setOnClickListener(){
            //collect otp from all the edit texts
            val typedOTP =
                (digit1_et.text.toString() + digit2_et.text.toString() + digit3_et.text.toString()
                        + digit4_et.text.toString() + digit5_et.text.toString() + digit6_et.text.toString())

            if (typedOTP.isNotEmpty()) {
                if (typedOTP.length == 6) {
                    val credential: PhoneAuthCredential =
                        PhoneAuthProvider.getCredential(verificationId!!, typedOTP)

                    signInWithCredential(credential)
                } else {
                    Toast.makeText(this, "Please Enter Correct OTP", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(this, "Please Enter OTP", Toast.LENGTH_SHORT).show()
            }
        }


    }

    private fun startSmartUserConsent() {
        val client = SmsRetriever.getClient(this)
        client.startSmsUserConsent(null)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == REQ_USER_CONSENT) {
            if (resultCode == RESULT_OK && data != null){
                val message = data.getStringExtra(SmsRetriever.EXTRA_SMS_MESSAGE)
                getOtpFromMessage(message)
            }
        }
    }

    private fun getOtpFromMessage(message: String?) {

        val otpPatter = Pattern.compile("(|^)\\d{6}")
        val matcher = otpPatter.matcher(message)
        if (matcher.find()){
            val otp = matcher.group(0)
            if (otp.length == 6) {
                digit1_et.setText(otp[0].toString())
                digit2_et.setText(otp[1].toString())
                digit3_et.setText(otp[2].toString())
                digit4_et.setText(otp[3].toString())
                digit5_et.setText(otp[4].toString())
                digit6_et.setText(otp[5].toString())
            }

        }

    }

    private fun registerBroadcastReceiver() {
        smsBroadcastReceiver = SmsBroadcastReceiver()
        smsBroadcastReceiver!!.smsBroadcastReceiverListener = object : SmsBroadcastReceiver.SmsBroadcastReceiverListener{
            override fun onSuccess(intent: Intent?) {
                if (intent != null) {
                    startActivityForResult(intent, REQ_USER_CONSENT)
                }
            }

            override fun onFailure() {

            }
        }
        val intentFilter = IntentFilter(SmsRetriever.SMS_RETRIEVED_ACTION)
        registerReceiver(smsBroadcastReceiver, intentFilter)
    }

    private fun initTimer() {
        timer = object : CountDownTimer(60000, 1000) { // 60 seconds countdown
            override fun onTick(millisUntilFinished: Long) {
                // Update UI with the remaining time
                val secondsRemaining = millisUntilFinished / 1000
                // Example: Show remaining time in a TextView
                timerTv.text = "$secondsRemaining seconds"
            }

            override fun onFinish() {
                // Handle timer finish (e.g., enable resend button)
                // Example: Enable a "Resend Code" button
                // buttonResendCode.isEnabled = true
            }
        }.start()
    }

    private fun sendVerificationCode(phoneNumber: String) {
        val options = PhoneAuthOptions.newBuilder(auth)
            .setPhoneNumber(phoneNumber)
            .setTimeout(80L, TimeUnit.SECONDS)
            .setActivity(this)
            .setCallbacks(callbacks)
            .build()

        PhoneAuthProvider.verifyPhoneNumber(options)
    }

    private val callbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

        override fun onVerificationCompleted(credential: PhoneAuthCredential) {
            signInWithCredential(credential)
            Toast.makeText(applicationContext, "Verification Succeeded", Toast.LENGTH_SHORT).show()
        }

        override fun onCodeAutoRetrievalTimeOut(p0: String) {
            super.onCodeAutoRetrievalTimeOut(p0)
        }

        override fun onCodeSent(verificationId: String, token: PhoneAuthProvider.ForceResendingToken) {
            super.onCodeSent(verificationId, token)
            this@PhoneVerification.verificationId = verificationId
            codeBar.visibility = View.GONE
            Toast.makeText(applicationContext, "Code sent", Toast.LENGTH_SHORT).show()

            // Start the timer when the code is sent
            initTimer()
            timer.start()
        }

        override fun onVerificationFailed(e: FirebaseException) {
            Log.w("onFail", "onVerificationFailed", e)

            if (e is FirebaseAuthInvalidCredentialsException) {
                Toast.makeText(applicationContext, "Invalid request", Toast.LENGTH_SHORT).show()
            } else if (e is FirebaseTooManyRequestsException) {
                Toast.makeText(applicationContext, "The SMS quota for the project has been exceeded", Toast.LENGTH_SHORT).show()
            } else if (e is FirebaseAuthMissingActivityForRecaptchaException) {
                Toast.makeText(applicationContext, "reCAPTCHA verification attempted with null Activity", Toast.LENGTH_SHORT).show()
            }

            // Stop the timer in case of verification failure
            timer.cancel()
        }
    }

    private fun signInWithCredential(credential: PhoneAuthCredential) {
        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    val name = intent.getStringExtra("userName")
                    val email = intent.getStringExtra("email")
                    val password = intent.getStringExtra("password")
                    var phoneNumber = intent.getStringExtra("num")!!

                    if (phoneNumber != null && name != null && email != null && password != null) {
                        CoroutineScope(Dispatchers.Main).launch {
                            val registrationSuccessful =
                                userController.registerUser(name, email, phoneNumber!!, password)

                            if (registrationSuccessful) {
                                Toast.makeText(applicationContext, "Signup succeeded", Toast.LENGTH_SHORT)
                                    .show()

                                val home = Intent(applicationContext, MainActivity2::class.java)
                                home.putExtra("userName", name)
                                startActivity(home)
                                finish()
                            } else {
                                Toast.makeText(
                                    applicationContext,
                                    "Registration failed",
                                    Toast.LENGTH_SHORT
                                )
                                    .show()
                            }
                        }
                    }
                } else {
                    // Sign in failed, handle errors
                }
            }
    }

    fun signInWithVerificationCode(verificationCode: String) {
        if (verificationId != null) {
            val credential: PhoneAuthCredential =
                PhoneAuthProvider.getCredential(verificationId!!, verificationCode)

            signInWithCredential(credential)
        } else {
            // Handle the case when verificationId is null
            Toast.makeText(this, "Verification failed. Please try again.", Toast.LENGTH_SHORT).show()
        }
    }

    private fun setUpOtpInputFocus() {
        digit1_et.addTextChangedListener(FocusTextWatcher(digit1_et, digit2_et))
        digit2_et.addTextChangedListener(FocusTextWatcher(digit2_et, digit3_et))
        digit3_et.addTextChangedListener(FocusTextWatcher(digit3_et, digit4_et))
        digit4_et.addTextChangedListener(FocusTextWatcher(digit4_et, digit5_et))
        digit5_et.addTextChangedListener(FocusTextWatcher(digit5_et, digit6_et))
        digit6_et.addTextChangedListener(FocusTextWatcher(digit6_et, null))
    }

    private inner class FocusTextWatcher(private val currentEditText: EditText, private val nextEditText: EditText?) :
        SimpleTextWatcher() {
        override fun onTextChanged(charSequence: CharSequence?, start: Int, before: Int, count: Int) {
            if (count > 0) {
                if (nextEditText == null) {
                    // If it's the 6th block, call signInWithCredential
                    val typedOTP = StringBuilder()
                        .append(digit1_et.text)
                        .append(digit2_et.text)
                        .append(digit3_et.text)
                        .append(digit4_et.text)
                        .append(digit5_et.text)
                        .append(charSequence)
                        .toString()

                    if (typedOTP.length == 6) {
                        signInWithVerificationCode(typedOTP)
                    }
                } else {
                    // Move focus to the next EditText
                    nextEditText.requestFocus()
                }
            }
        }
    }


    override fun onDestroy() {
        timer.cancel()
        super.onDestroy()
    }

    override fun onStart() {
        super.onStart()
        registerBroadcastReceiver()

    }

    override fun onStop() {
        super.onStop()
        unregisterReceiver(smsBroadcastReceiver)
    }


}

