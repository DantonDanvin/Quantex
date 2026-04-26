package com.example.quantex.activitys

import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import com.example.quantex.R
import com.example.quantex.databinding.ActivityContactUsBinding

class ContactUs : AppCompatActivity() {

    private lateinit var binding: ActivityContactUsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityContactUsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        window.statusBarColor = ContextCompat.getColor(this, R.color.splash_bg)
        window.navigationBarColor = ContextCompat.getColor(this, R.color.bottom_nav_bg)

        val toolbar: Toolbar = binding.toolbarcontactus
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = ""

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
                val intent = Intent(Intent.ACTION_SENDTO).apply { data = Uri.parse(mail) }
                try {
                    startActivity(Intent.createChooser(intent, "Send Email.."))
                    Toast.makeText(this, "Email sent", Toast.LENGTH_SHORT).show()
                } catch (e: Exception) {
                    Toast.makeText(this, e.message, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    @Suppress("DEPRECATION")
    override fun onBackPressed() {
        super.onBackPressed()
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return if (item.itemId == android.R.id.home) {
            @Suppress("DEPRECATION")
            onBackPressed()
            true
        } else super.onOptionsItemSelected(item)
    }
}