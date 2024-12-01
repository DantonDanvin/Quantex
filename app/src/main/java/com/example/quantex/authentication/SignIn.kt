package com.example.quantex.authentication

import android.app.ProgressDialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.WindowManager
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.widget.SwitchCompat
import com.example.quantex.MainActivity
import com.example.quantex.R
import com.example.quantex.activitys.Settings
import com.example.quantex.databinding.ActivitySettingsBinding
import com.example.quantex.databinding.ActivitySignInBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.FirebaseDatabase

class SignIn : AppCompatActivity() {

    private lateinit var binding: ActivitySignInBinding
    private lateinit var bindingSet: ActivitySettingsBinding
    private lateinit var mAuth: FirebaseAuth                // for "bom" authentication. It just take data from user and authenticate.
    private lateinit var database: FirebaseDatabase         // for realtime database.  After authentication we need to save data in realtime database.
    private lateinit var progressDialog: ProgressDialog     // create object when user click on signUp button to show dialog box for reload.

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_in)
        binding = ActivitySignInBinding.inflate(layoutInflater) // use inflate to access all id from xml.
        setContentView(binding.root) // get the root(path) by id.

        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
        window.statusBarColor = resources.getColor(R.color.black)
        window.navigationBarColor = resources.getColor(R.color.black_my)

        mAuth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()

        // create dialog box
        progressDialog = ProgressDialog(this).apply {// 'this' is a object can pass perimeter of this class.
            setTitle("Wait for Login")
            setMessage("LoggingIn")
        }

        binding.btnSignIn.setOnClickListener {
            progressDialog.show()
            val email = binding.etEmail.text.toString()
            val pass = binding.etPassword.text.toString()

            if (email.isEmpty() || pass.isEmpty()) {
                progressDialog.dismiss()
                Toast.makeText(this, "Null value not accept", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            mAuth.signInWithEmailAndPassword(email, pass)
                .addOnCompleteListener { task ->
                    progressDialog.dismiss()
                    if (task.isSuccessful) {
                        Toast.makeText(this, "LogIn successfully", Toast.LENGTH_SHORT).show()
                        val intent = Intent(this, MainActivity::class.java)
                        startActivity(intent)
                        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
                    } else {
                        Toast.makeText(this, task.exception?.message, Toast.LENGTH_SHORT).show()
                    }
                }
        }



        // if user click on textView has (Already have Account) so they go on sign in activity.
        binding.tvClickSignUp.setOnClickListener {
            val intent = Intent(this, SignUp::class.java)
            startActivity(intent)
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
        }

        //  if user is already sign in then they go to main activity.
        val currentUser: FirebaseUser? = mAuth.currentUser
        if (currentUser != null) {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }


    }

    override fun onBackPressed() {
//        super.onBackPressed()
        AlertDialog.Builder(this)
            .setTitle("Confirm Exit")
            .setMessage("Are you sure you want to close the app?")
            .setPositiveButton(android.R.string.yes) { _, _ ->
                finishAffinity()
            }
            .setNegativeButton(android.R.string.no, null)
            .setIcon(android.R.drawable.ic_dialog_alert)
            .show()
    }

}