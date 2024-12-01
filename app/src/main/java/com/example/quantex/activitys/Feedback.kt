package com.example.quantex.activitys

import android.media.MediaPlayer
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.view.WindowManager
import android.view.animation.AnimationUtils
import android.widget.Toast
import androidx.appcompat.widget.Toolbar
import com.example.quantex.R
import com.example.quantex.databinding.ActivityFeedbackBinding
import com.example.quantex.databinding.ActivitySettingsBinding

class Feedback : AppCompatActivity() {

    private lateinit var binding: ActivityFeedbackBinding
    private var mediaPlayer: MediaPlayer? = null
    private var rate: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_feedback)
        binding = ActivityFeedbackBinding.inflate(layoutInflater)
        setContentView(binding.root)

        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
        window.statusBarColor = resources.getColor(R.color.black)

        // Toolbar
        val toolbar: Toolbar = findViewById(R.id.toolbarfeedback)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true) // Enable the back button
        // Get the custom toolbar layout
        val customToolbar: View = layoutInflater.inflate(R.layout.custom_toolbar_feedback, toolbar, false)
        // Set the custom toolbar layout as the toolbar's layout
        toolbar.addView(customToolbar)


        binding.rating.setOnRatingBarChangeListener { ratingBar, rating, fromUser ->
            binding.tv.text = "Rating: $rating"
            rate = if (rating == 5.0f || rating == 4.0f) 5 else 0
        }

        binding.feedback.setOnClickListener {
            if (rate == 5 || rate == 4) {
                playWinnerSound()
                startCelebrationAnimation(binding.root)
            }
            Toast.makeText(this, "Feedback Accepted", Toast.LENGTH_SHORT).show()
            binding.feedback.visibility = View.GONE
            binding.rating.setIsIndicator(true)
        }

    }


    private fun playWinnerSound() {
        mediaPlayer = MediaPlayer.create(this, R.raw.goodresult)
        mediaPlayer?.start()
    }

    private fun startCelebrationAnimation(view: View) {
        val animation = AnimationUtils.loadAnimation(this, R.anim.celebration_animation)
        view.startAnimation(animation)
    }

    override fun onDestroy() {
        super.onDestroy()
        mediaPlayer?.release()
        mediaPlayer = null
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