package com.prasannakumar.strangerswebrtc.ui

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.database.FirebaseDatabase
import com.prasannakumar.strangerswebrtc.R
import com.prasannakumar.strangerswebrtc.databinding.ActivityLoginBinding
import com.prasannakumar.strangerswebrtc.model.User

class Login : AppCompatActivity() {
    private lateinit var binding: ActivityLoginBinding
    private lateinit var mGoogleSignInClient: GoogleSignInClient
    private val RC_SIGN_IN = 11
    private lateinit var mAuth: FirebaseAuth
    private lateinit var db: FirebaseDatabase
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }

    override fun onResume() {
        super.onResume()

        mAuth = FirebaseAuth.getInstance()
        db = FirebaseDatabase.getInstance()

        if (mAuth.currentUser != null) {
            goToNextActivity()
        }

        val googleOptions = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail().build()

        mGoogleSignInClient = GoogleSignIn.getClient(this, googleOptions)

        binding.loginUsingGoogle.setOnClickListener {
            val intent = mGoogleSignInClient.signInIntent
            startActivityForResult(intent, RC_SIGN_IN)
        }
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == RC_SIGN_IN) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            val account = task.result
            authWithGoogle(account.idToken!!)
        }
    }

    private fun authWithGoogle(idToken: String) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        mAuth.signInWithCredential(credential)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val user = mAuth.currentUser
                    db.reference.child("profiles").child(user!!.uid)
                        .setValue(User(user.uid,
                            user.displayName.toString(),
                            user.photoUrl.toString(),
                            "No Data"))
                        .addOnCompleteListener { task1 ->
                            if (task1.isSuccessful) {
                                goToNextActivity()
                            } else {
                                Toast.makeText(this, task1.exception?.message, Toast.LENGTH_SHORT)
                                    .show()
                            }
                        }
                    Log.e("Profile", user.photoUrl.toString())
                }
            }
    }

    private fun goToNextActivity() {
        startActivity(Intent(this, MainActivity::class.java))
        finishAffinity()
    }
}