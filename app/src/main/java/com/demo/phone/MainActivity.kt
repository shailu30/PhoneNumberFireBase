package com.demo.phone

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.View
import android.widget.Toast
import com.google.firebase.FirebaseException
import com.google.firebase.FirebaseTooManyRequestsException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthProvider
import kotlinx.android.synthetic.main.activity_main.*
import java.util.concurrent.TimeUnit


class MainActivity : AppCompatActivity() {

    private val TAG = "MainActivity"

    private var mAuth = FirebaseAuth.getInstance()

    private var mVerificationId: String? = null

    private var mResendToken: PhoneAuthProvider.ForceResendingToken? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        btnSend.setOnClickListener {
            val phone = edtPhone.text.toString()
            when {
                phone.length == 10 -> {
                    PhoneAuthProvider.getInstance().verifyPhoneNumber(
                            "+91" + phone,
                            60,
                            TimeUnit.SECONDS,
                            this@MainActivity,
                            mCallbacks)
                    edtPhoneOtp.visibility = View.VISIBLE
                    btnOtpCheck.visibility = View.VISIBLE
                }
                phone.isEmpty() -> message("Mobile number is empty")
                phone.length < 10 -> message("Invalid mobile number")
            }
        }

        btnOtpCheck.setOnClickListener {
            val otp = edtPhoneOtp.text.toString()
            if (mVerificationId != null && otp.isNotEmpty()) {
                val credential = PhoneAuthProvider.getCredential(mVerificationId ?: "", otp)
                if (otp == credential.smsCode) {
                    message("Successfully Login")
                }
            }
        }
    }

    private fun message(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    private fun signInWithPhoneAuthCredential(credential: PhoneAuthCredential) {
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, { task ->
                    if (task.isSuccessful) {
                        val user = task.result.user
                        Log.e(TAG, "onVerificationCompleted:" + credential)
                    } else {
                        if (task.exception is FirebaseAuthInvalidCredentialsException) {
                            Log.e(TAG, "The verification code entered was invalid")
                        }
                    }
                })
    }

    override fun onDestroy() {
        super.onDestroy()
        mAuth.signOut()
    }

    private val mCallbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

        override fun onVerificationCompleted(credential: PhoneAuthCredential) {
            signInWithPhoneAuthCredential(credential)
            message("onVerificationCompleted.")
            Log.e(TAG, "onVerificationCompleted:" + credential)
        }

        override fun onVerificationFailed(e: FirebaseException) {
            when (e) {
                is FirebaseAuthInvalidCredentialsException -> {
                    message("Invalid request")
                    Log.e(TAG, "Invalid request ${e.printStackTrace()}")
                }
                is FirebaseTooManyRequestsException -> {
                    message("The SMS quota for the project has been exceeded")
                    Log.e(TAG, "The SMS quota for the project has been exceeded ${e.printStackTrace()}")
                }
                else -> Log.e(TAG, "Others ${e.printStackTrace()}")
            }
        }

        override fun onCodeSent(verificationId: String?,
                                token: PhoneAuthProvider.ForceResendingToken?) {
            mVerificationId = verificationId
            mResendToken = token
            message("Code sent")
            Log.e(TAG, "onCodeSent: $mVerificationId")
        }
    }
}
