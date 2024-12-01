package com.example.quantex.activitys

import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.view.WindowManager
import android.widget.Toast
import androidx.appcompat.widget.Toolbar
import com.example.quantex.R
import com.example.quantex.databinding.ActivityContactUsBinding

class ContactUs : AppCompatActivity() {

    private lateinit var binding: ActivityContactUsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_contact_us)
        binding = ActivityContactUsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
        window.statusBarColor = resources.getColor(R.color.black)

        // Toolbar
        val toolbar: Toolbar = findViewById(R.id.toolbarcontactus)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true) // Enable the back button
        // Get the custom toolbar layout
        val customToolbar: View = layoutInflater.inflate(R.layout.custom_toolbar_contactus, toolbar, false)
        // Set the custom toolbar layout as the toolbar's layout
        toolbar.addView(customToolbar)

        binding.send.setOnClickListener {
            val subject = binding.subject.text.toString().trim()
            val message = binding.message.text.toString().trim()
            val email = "allaccept101@gmail.com"

            if (subject.isEmpty()) {
                Toast.makeText(this, "Please add Subject", Toast.LENGTH_SHORT).show()
            } else if (message.isEmpty()) {
                Toast.makeText(this, "Please add some Message", Toast.LENGTH_SHORT).show()
            } else {
                val mail = "mailto:$email?&subject=${Uri.encode(subject)}&body=${Uri.encode(message)}"
                val intent = Intent(Intent.ACTION_SENDTO).apply {
                    data = Uri.parse(mail)
                }
                try {
                    startActivity(Intent.createChooser(intent, "Send Email.."))
                    Toast.makeText(this, "Email sent", Toast.LENGTH_SHORT).show()
                } catch (e: Exception) {
                    Toast.makeText(this, e.message, Toast.LENGTH_SHORT).show()
                }
            }
        }


    }

    override fun onBackPressed() {
        super.onBackPressed()
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right)
    }


    // go back
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return if (item.itemId == android.R.id.home) {
            onBackPressed() // Go back to the previous activity
            overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right)
            true
        } else {
            super.onOptionsItemSelected(item)
        }
    }

}