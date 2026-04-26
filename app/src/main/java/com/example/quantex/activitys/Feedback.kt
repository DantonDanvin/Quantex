package com.example.quantex.activitys

import android.media.MediaPlayer
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.view.animation.AnimationUtils
import android.widget.Toast
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import com.example.quantex.R
import com.example.quantex.databinding.ActivityFeedbackBinding

class Feedback : AppCompatActivity() {

    private lateinit var binding: ActivityFeedbackBinding
    private var mediaPlayer: MediaPlayer? = null
    private var rate: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFeedbackBinding.inflate(layoutInflater)
        setContentView(binding.root)

        window.statusBarColor = ContextCompat.getColor(this, R.color.splash_bg)
        window.navigationBarColor = ContextCompat.getColor(this, R.color.bottom_nav_bg)

        val toolbar: Toolbar = binding.toolbarfeedback
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = ""

        binding.rating.setOnRatingBarChangeListener { _, rating, _ ->
            binding.tv.text = "Rating: $rating"
            rate = if (rating >= 4.0f) 5 else 0
        }

        binding.feedback.setOnClickListener {
            if (rate >= 4) {
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