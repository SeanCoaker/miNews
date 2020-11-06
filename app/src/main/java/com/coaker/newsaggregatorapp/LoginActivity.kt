package com.coaker.newsaggregatorapp

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import com.facebook.AccessToken
import com.facebook.CallbackManager
import com.facebook.FacebookCallback
import com.facebook.FacebookException
import com.facebook.internal.CallbackManagerImpl
import com.facebook.login.LoginManager
import com.facebook.login.LoginResult
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.*
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class LoginActivity : AppCompatActivity(), View.OnClickListener {

    companion object {
        private const val GOOGLE_CODE = 9001
    }

    private lateinit var auth: FirebaseAuth
    private lateinit var mGoogleSignInClient: GoogleSignInClient
    private lateinit var callbackManager: CallbackManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.login_layout)

        val googleCard = findViewById<CardView>(R.id.googleCard)
        val emailCard = findViewById<CardView>(R.id.emailCard)
        val facebookCard = findViewById<CardView>(R.id.facebookCard)

        googleCard.setOnClickListener(this)
        emailCard.setOnClickListener(this)
        facebookCard.setOnClickListener(this)

        callbackManager = CallbackManager.Factory.create()


        auth = Firebase.auth
    }

    override fun onStart() {
        val currentUser = auth.currentUser

        if (currentUser != null) {
            updateUI(currentUser)
        } else {
            updateUI(null)
        }

        super.onStart()
    }

    private fun updateUI(user: FirebaseUser?) {
        if (user != null) {
            val intent = Intent(this, MainActivity::class.java)
            intent.putExtra("custom", false)
            startActivity(intent)
        }
    }

    override fun onClick(v: View) {
        when(v.id) {
            R.id.googleCard -> {
                googleSignIn()
            }

            R.id.facebookCard -> {
                facebookSignIn()
            }

            R.id.emailCard -> {
                val emailIntent = Intent(this, EmailLoginActivity::class.java)
                startActivity(emailIntent)
            }
        }
    }

    private fun googleSignIn() {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build()
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso)
        val signInIntent = mGoogleSignInClient.signInIntent
        startActivityForResult(signInIntent, GOOGLE_CODE)
    }

    private fun facebookSignIn() {
        LoginManager.getInstance().logInWithReadPermissions(this, listOf("email", "public_profile"))
        LoginManager.getInstance().registerCallback(callbackManager, object : FacebookCallback<LoginResult> {
            override fun onSuccess(result: LoginResult) {
                facebookAuth(result.accessToken)
            }

            override fun onCancel() {
                updateUI(null)
            }

            override fun onError(error: FacebookException?) {
                updateUI(null)
            }
        })
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == GOOGLE_CODE) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)

            try {
                val account = task.getResult(ApiException::class.java)!!
                googleAuth(account.idToken!!)
            } catch (e: ApiException) {
                updateUI(null)
            }
        } else if (requestCode == CallbackManagerImpl.RequestCodeOffset.Login.toRequestCode()) {
            callbackManager.onActivityResult(requestCode, resultCode, data)
        }
    }

    private fun googleAuth(idToken: String) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(credential)
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        updateUI(auth.currentUser)
                    } else {
                        Snackbar.make(findViewById(android.R.id.content), "Authentication Failed", Snackbar.LENGTH_SHORT).show()
                        updateUI(null)
                    }
                }
    }

    private fun facebookAuth(token: AccessToken) {
        val credential = FacebookAuthProvider.getCredential(token.token)
        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    updateUI(auth.currentUser)
                } else {
                    updateUI(null)
                }
            }
    }
}