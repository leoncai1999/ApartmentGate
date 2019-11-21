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
            // account validation needed here

            var fbAuth = FirebaseAuth.getInstance()

            fbAuth.signInWithEmailAndPassword(emailET.text.toString(), passwordET.text.toString()).addOnCompleteListener(this, OnCompleteListener<AuthResult> { task ->
                if (task.isSuccessful) {
                    val mainActivityIntent = Intent(this, MainActivity::class.java)
                    startActivity(mainActivityIntent)

                } else{
                    Toast.makeText(this, "Login failed.", Toast.LENGTH_LONG).show()
                }
            })
        }


        createAccountText.setOnClickListener {
            val createAccountIntent = Intent(this, CreateAccountActivity::class.java)
            startActivity(createAccountIntent)
        }
    }

}
