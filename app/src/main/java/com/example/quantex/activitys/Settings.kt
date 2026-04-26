package com.example.quantex.activitys

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import com.example.quantex.R
import com.example.quantex.ThemeUtils
import com.example.quantex.databinding.ActivitySettingsBinding
import com.google.android.material.materialswitch.MaterialSwitch

class Settings : AppCompatActivity() {

    private lateinit var binding: ActivitySettingsBinding
    private lateinit var themeSwitch: MaterialSwitch

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        window.statusBarColor = ContextCompat.getColor(this, R.color.splash_bg)
        window.navigationBarColor = ContextCompat.getColor(this, R.color.bottom_nav_bg)

        val toolbar: Toolbar = binding.toolbarsettings
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = ""

        themeSwitch = binding.themeSwitch
        val isNightMode = ThemeUtils.loadNightModePref(this)
        themeSwitch.isChecked = isNightMode

        themeSwitch.setOnCheckedChangeListener { _, isChecked ->
            ThemeUtils.saveNightModePref(this, isChecked)
            AlertDialog.Builder(this, com.google.android.material.R.style.ThemeOverlay_Material3_MaterialAlertDialog)
                .setTitle("Theme Changed")
                .setMessage("OK → Apply & Restart Now\nCancel → Apply on next launch")
                .setPositiveButton("Restart") { _, _ -> finishAffinity() }
                .setNegativeButton("Later", null)
                .show()
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