package com.cailihuang.apartmentgate

import android.os.Bundle
import com.google.android.material.snackbar.Snackbar
import androidx.appcompat.app.AppCompatActivity
import android.content.Intent
import kotlinx.android.synthetic.main.activity_welcome.*
import android.view.View
import android.widget.Button
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import android.widget.Toast


class WelcomeActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_welcome)

        signInButton.setOnClickListener {
            if (emailET.text.isEmpty() || passwordET.text.isEmpty()) {
                Toast.makeText(this, "You must enter your email and password.", Toast.LENGTH_LONG).show()
            } else {
                // TODO make password hidden
                // in activity_welcome, passwordET android:password="true"

                var fbAuth = FirebaseAuth.getInstance()
                fbAuth.signInWithEmailAndPassword(emailET.text.toString(), passwordET.text.toString()).addOnCompleteListener(this, OnCompleteListener<AuthResult> { task ->
                    if (task.isSuccessful) {
                        Toast.makeText(this, "Please be patient as we calculate your scores.", Toast.LENGTH_LONG).show()
                        val mainActivityIntent = Intent(this, MainActivity::class.java)
                        startActivity(mainActivityIntent)
                    } else {
                        Toast.makeText(this, "Login failed.", Toast.LENGTH_LONG).show()
                    }
                })
            }
        }

        createAccountText.setOnClickListener {
            val createAccountIntent = Intent(this, CreateAccountActivity::class.java)
            startActivity(createAccountIntent)
        }
    }

}
