package com.example.quantex.authentication

import android.app.ProgressDialog
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.quantex.firebaseUser.Users
import com.example.quantex.MainActivity
import com.example.quantex.R
import com.example.quantex.databinding.ActivitySignUpBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class SignUp : AppCompatActivity() {

    private lateinit var binding: ActivitySignUpBinding
    private lateinit var mAuth: FirebaseAuth
    private lateinit var database: FirebaseDatabase
    private lateinit var progressDialog: ProgressDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignUpBinding.inflate(layoutInflater)
        setContentView(binding.root)

        window.statusBarColor = ContextCompat.getColor(this, R.color.auth_gradient_start)
        window.navigationBarColor = ContextCompat.getColor(this, R.color.auth_gradient_end)

        mAuth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()

        progressDialog = ProgressDialog(this).apply {
            setTitle("Creating Account")
            setMessage("Please wait...")
        }

        binding.btnSignUp.setOnClickListener {
            progressDialog.show()
            val name = binding.etUserName.text.toString()
            val email = binding.etEmail.text.toString()
            val pass = binding.etPassword.text.toString()

            if (name.isEmpty() || email.isEmpty() || pass.isEmpty()) {
                progressDialog.dismiss()
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            mAuth.createUserWithEmailAndPassword(email, pass).addOnCompleteListener { task ->
                progressDialog.dismiss()
                if (task.isSuccessful) {
                    val user = Users(name, email, pass)
                    val funds = Users("100000")
                    val portfolio = Users("0", true)
                    val transactions = Users("0", "0", "0", true)
                    val id = task.result?.user?.uid
                    database.reference.child("Users").child(id ?: "").setValue(user)
                    database.reference.child("Users").child(id ?: "").child("Funds").setValue(funds)
                    database.reference.child("Users").child(id ?: "").child("Transactions").setValue(transactions)
                    database.reference.child("Users").child(id ?: "").child("Portfolio").setValue(portfolio)

                    Toast.makeText(this, "Account Created Successfully", Toast.LENGTH_SHORT).show()
                    startActivity(Intent(this, MainActivity::class.java))
                    overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
                } else {
                    Toast.makeText(this, task.exception?.message, Toast.LENGTH_SHORT).show()
                }
            }
        }

        binding.tvAlreadyAccount.setOnClickListener {
            startActivity(Intent(this, SignIn::class.java))
            overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right)
        }
    }

    @Suppress("DEPRECATION")
    override fun onBackPressed() {
        AlertDialog.Builder(this, com.google.android.material.R.style.ThemeOverlay_Material3_MaterialAlertDialog)
            .setTitle("Confirm Exit")
            .setMessage("Are you sure you want to close the app?")
            .setPositiveButton("Yes") { _, _ -> finishAffinity() }
            .setNegativeButton("No", null)
            .show()
    }
}
