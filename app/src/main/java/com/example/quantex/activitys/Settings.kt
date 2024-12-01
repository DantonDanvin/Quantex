package com.example.quantex.activitys

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.view.WindowManager
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.widget.SwitchCompat
import androidx.appcompat.widget.Toolbar
import com.example.quantex.R
import com.example.quantex.ThemeUtils
import com.example.quantex.databinding.ActivitySettingsBinding

class Settings : AppCompatActivity() {

    private lateinit var binding: ActivitySettingsBinding
    private lateinit var themeSwitch: SwitchCompat

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)
        binding = ActivitySettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
        window.statusBarColor = resources.getColor(R.color.black)

        // Toolbar
        val toolbar: Toolbar = findViewById(R.id.toolbarsettings)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true) // Enable the back button
        // Get the custom toolbar layout
        val customToolbar: View = layoutInflater.inflate(R.layout.custom_toolbar_setting, toolbar, false)
        // Set the custom toolbar layout as the toolbar's layout
        toolbar.addView(customToolbar)

        // For theme.
        themeSwitch = findViewById(R.id.themeSwitch)
        // Load current mode preference and update switch text
        val isNightMode = ThemeUtils.loadNightModePref(this)
        themeSwitch.isChecked = isNightMode

        themeSwitch.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                AlertDialog.Builder(this)
                    .setTitle("Theme change")
                    .setMessage("OK --> Apply and Restart Now\nCancel --> Apply on next launch")
                    .setPositiveButton(android.R.string.yes) { dialog, which ->
                        // Close the app
                        finishAffinity()
                    }
                    .setNegativeButton(android.R.string.no, null)
                    .setIcon(android.R.drawable.ic_dialog_info)
                    .show()
//                Toast.makeText(this@Settings,"Night Mode NO", Toast.LENGTH_SHORT).show()
                ThemeUtils.saveNightModePref(this,true)
            } else {
                AlertDialog.Builder(this)
                    .setTitle("Theme change")
                    .setMessage("OK --> Apply and Restart Now\nCancel --> Apply on next launch")
                    .setPositiveButton(android.R.string.yes) { dialog, which ->
                        // Close the app
                        finishAffinity()
                    }
                    .setNegativeButton(android.R.string.no, null)
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .show()
//                Toast.makeText(this@Settings,"Night Mode Off",Toast.LENGTH_SHORT).show()
                ThemeUtils.saveNightModePref(this,false)
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