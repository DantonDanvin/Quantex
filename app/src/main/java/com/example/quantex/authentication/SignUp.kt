package com.example.quantex.authentication

import android.app.ProgressDialog
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.quantex.firebaseUser.Users
import com.example.quantex.MainActivity
import com.example.quantex.R
import com.example.quantex.databinding.ActivitySignUpBinding
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class SignUp : AppCompatActivity() {

    private lateinit var binding: ActivitySignUpBinding
    private lateinit var mAuth: FirebaseAuth    // for "bom" authentication. It just take data from user and authenticate.
    private lateinit var database: FirebaseDatabase // for realtime database.  After authentication we need to save data in realtime database.
    private lateinit var progressDialog: ProgressDialog // create object when user click on signUp button to show dialog box for reload.

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_up)
        binding = ActivitySignUpBinding.inflate(layoutInflater)
        setContentView(binding.root)

        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
        window.statusBarColor = resources.getColor(R.color.black)
        window.navigationBarColor = resources.getColor(R.color.black_my)

        mAuth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()

        // create dialog box
        progressDialog = ProgressDialog(this)   // this is a object can pass perimeter of this class.
        progressDialog.setTitle("Wait for Sign Up")
        progressDialog.setMessage("Creating your Account")

        binding.btnSignUp.setOnClickListener {
            progressDialog.show()   // start seeing dialog box.

            val name = binding.etUserName.text.toString()
            val email = binding.etEmail.text.toString()
            val pass = binding.etPassword.text.toString()

            if (name.isEmpty() || email.isEmpty() || pass.isEmpty()) {
                progressDialog.dismiss()
                Toast.makeText(this, "Null value not accept", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            mAuth.createUserWithEmailAndPassword(email, pass).addOnCompleteListener { task ->
                progressDialog.dismiss()     // close to seeing dialog box.
                if (task.isSuccessful) {

                    // it's used to save data on realtime database.
                    val user = Users(name, email, pass) // create object has user class to call constructor for SignUp.
                    val funds = Users("100000")
                    val portfolio = Users("0", true)
                    val transactions = Users("0", "0", "0", true)
                    val id = task.result?.user?.uid     // it can get user id from authentication and store into id(string) variable.
                    database.reference.child("Users").child(id ?: "").setValue(user)    // It can store data(name, email, password) in real time data base in Users child.
                    database.reference.child("Users").child(id ?: "").child("Funds").setValue(funds)
                    database.reference.child("Users").child(id ?: "").child("Transactions").setValue(transactions)
                    database.reference.child("Users").child(id ?: "").child("Portfolio").setValue(portfolio)
                    // given toast if account is created.
                    Toast.makeText(this, "Account Created Successfully", Toast.LENGTH_SHORT).show()

                    val intent = Intent(this, MainActivity::class.java)
                    startActivity(intent)
                    overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
                } else {
                    Toast.makeText(this, task.exception?.message, Toast.LENGTH_SHORT).show()
                }
            }
        }

        // if user click on textView has (Already have Account) so they go on sign in activity.
        binding.tvAlreadyAccount.setOnClickListener {
            val intent = Intent(this, SignIn::class.java)
            startActivity(intent)
            overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right)
        }
    }

    override fun onBackPressed() {
//        super.onBackPressed()
        AlertDialog.Builder(this)
            .setTitle("Confirm Exit")
            .setMessage("Are you sure you want to close the app?")
            .setPositiveButton(android.R.string.yes) { dialog, which ->
                finishAffinity()
            }
            .setNegativeButton(android.R.string.no, null)
            .setIcon(android.R.drawable.ic_dialog_alert)
            .show()
    }

}
