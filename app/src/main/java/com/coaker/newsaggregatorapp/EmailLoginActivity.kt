package com.coaker.newsaggregatorapp

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Patterns
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.addTextChangedListener
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.auth.ktx.userProfileChangeRequest
import com.google.firebase.ktx.Firebase
import java.util.regex.Matcher
import java.util.regex.Pattern


class EmailLoginActivity : AppCompatActivity(), View.OnClickListener {
    private lateinit var auth: FirebaseAuth
    private lateinit var loginButton: Button
    private lateinit var registerButton: Button
    private lateinit var registerTextView: TextView
    private lateinit var nameTextInputLayout: TextInputLayout
    private lateinit var emailTextInputLayout: TextInputLayout
    private lateinit var passwordTextInputLayout: TextInputLayout
    private lateinit var confirmPasswordTextInputLayout: TextInputLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.email_password_login)

        supportActionBar?.hide()

        auth = Firebase.auth

        loginButton = findViewById(R.id.loginButton)
        registerTextView = findViewById(R.id.registerTextView)
        registerButton = findViewById(R.id.registerButton)
        nameTextInputLayout = findViewById(R.id.nameLayout)
        emailTextInputLayout = findViewById(R.id.emailLayout)
        passwordTextInputLayout = findViewById(R.id.passwordLayout)
        confirmPasswordTextInputLayout = findViewById(R.id.confirmPasswordLayout)

        loginButton.setOnClickListener(this)
        registerTextView.setOnClickListener(this)
        registerButton.setOnClickListener(this)



        registerButton.visibility = View.GONE
        nameTextInputLayout.visibility = View.GONE
        confirmPasswordTextInputLayout.visibility = View.GONE
    }

    override fun onClick(v: View?) {

        val editTextName = findViewById<TextInputEditText>(R.id.editTextName)
        val editTextEmail = findViewById<TextInputEditText>(R.id.editTextEmail)
        val editTextPassword = findViewById<TextInputEditText>(R.id.editTextPassword)
        val nameLayout = findViewById<TextInputLayout>(R.id.nameLayout)
        val confirmPasswordLayout = findViewById<TextInputLayout>(R.id.confirmPasswordLayout)

        when (v!!.id) {

            R.id.loginButton -> {
                val email = editTextEmail.text.toString()
                val password = editTextPassword.text.toString()

                emailSignIn(email, password)
            }

            R.id.registerTextView -> {
                findViewById<Button>(R.id.loginButton).visibility = View.GONE
                findViewById<TextView>(R.id.registerTextView).visibility = View.GONE

                findViewById<Button>(R.id.registerButton).visibility = View.VISIBLE
                nameLayout.visibility = View.VISIBLE
                confirmPasswordLayout.visibility = View.VISIBLE
            }

            R.id.registerButton -> {
                val name = editTextName.text.toString()
                val email = editTextEmail.text.toString()
                val password = editTextPassword.text.toString()

                var valid = true

                if (editTextName.length() < 1) {
                    nameLayout.error =
                        "Name cannot be empty."
                    valid = false
                } else {
                    nameLayout.error = null
                }

                if (!emailValidation(email)) {
                    findViewById<TextInputLayout>(R.id.emailLayout).error =
                        "Please enter a valid email address."
                    valid = false
                } else {
                    findViewById<TextInputLayout>(R.id.emailLayout).error = null
                }

                if (!passwordValidation(password)) {
                    passwordTextInputLayout.error =
                        "Please enter a valid password. Must contain 1 number and 1 uppercase letter."
                    valid = false
                } else {
                    passwordTextInputLayout.error = null
                    val editTextConfirmPassword = findViewById<TextInputEditText>(R.id.editTextConfirmPassword)
                    if (password != editTextConfirmPassword.text.toString()) {
                        confirmPasswordLayout.error =
                            "Password and Confirm Password fields do not match."
                        valid = false
                    }
                }

                if (valid) {
                    emailCreateAccount(email, password, name)
                }
            }
        }
    }

    private fun emailSignIn(email: String, password: String) {
        auth.signInWithEmailAndPassword(email, password).addOnCompleteListener(this) { task ->
            if (task.isSuccessful) {
                updateUI(auth.currentUser)
            } else {
                updateUI(null)
            }
        }
    }

    private fun emailCreateAccount(email: String, password: String, name: String) {
        auth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(this) { task ->
            if (task.isSuccessful) {

                val user = Firebase.auth.currentUser
                val profileUpdates = userProfileChangeRequest {
                    displayName = name
                    photoUri = Uri.parse("android.resource://com.coaker.newsaggregatorapp/mipmap/ic_launcher_round")
                }

                user!!.updateProfile(profileUpdates).addOnCompleteListener {
                    user.reload()
                    updateUI(auth.currentUser)
                }

            } else {
                val e = task.exception as FirebaseAuthException?
                Toast.makeText(
                    this,
                    "Failed Registration: " + e!!.message,
                    Toast.LENGTH_SHORT
                ).show()
                updateUI(null)
            }
        }
    }

    private fun updateUI(user: FirebaseUser?) {
        if (user != null) {
            val intent = Intent(this, MainActivity::class.java)
            intent.putExtra("custom", true)
            startActivity(intent)
        }
    }

    private fun passwordValidation(password: String): Boolean {
        var valid = true
        var regex: String?
        var pattern: Pattern?
        var matcher: Matcher?

        if (password.length < 8) {
            valid = false
        }

        regex = ".*[A-Z].*"
        pattern = Pattern.compile(regex)
        matcher = pattern.matcher(password)
        if (!matcher.matches()) {
            valid = false
        }

        regex = ".*[0-9].*"
        pattern = Pattern.compile(regex)
        matcher = pattern.matcher(password)
        if (!matcher.matches()) {
            valid = false
        }

        return valid
    }

    private fun emailValidation(email: String): Boolean {
        return Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }
}