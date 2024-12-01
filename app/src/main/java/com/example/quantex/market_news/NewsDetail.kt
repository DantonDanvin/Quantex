package com.example.quantex.market_news

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.view.MenuItem
import android.view.View
import android.view.WindowManager
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.appcompat.widget.Toolbar
import com.example.quantex.R
import com.example.quantex.databinding.ActivityNewsDetailBinding
import com.example.quantex.databinding.ActivitySettingsBinding
import com.google.android.material.progressindicator.LinearProgressIndicator

class NewsDetail : AppCompatActivity() {

    private lateinit var binding: ActivityNewsDetailBinding
    private lateinit var webView: WebView
    private lateinit var progressIndicator: LinearProgressIndicator

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_news_detail)
        binding = ActivityNewsDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        progressIndicator = binding.progressBar

        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
        window.statusBarColor = resources.getColor(R.color.black)

        // Toolbar
        val toolbar: Toolbar = findViewById(R.id.toolbarnewsdetail)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true) // Enable the back button
        // Get the custom toolbar layout
        val customToolbar: View = layoutInflater.inflate(R.layout.custom_toolbar_newsdetail, toolbar, false)
        // Set the custom toolbar layout as the toolbar's layout
        toolbar.addView(customToolbar)


        val url = intent.getStringExtra("url")
        webView = findViewById(R.id.web_view)
        val webSettings = webView.settings
        webSettings.javaScriptEnabled = true
        webView.webViewClient = WebViewClient()
        if (url != null) {
            webView.loadUrl(url)
        }

        Handler().postDelayed({
            progressIndicator.visibility = View.INVISIBLE
        }, 7000) // 7 seconds in milliseconds




    }

    override fun onBackPressed() {
        super.onBackPressed()
        if(webView.canGoBack()){
            webView.goBack();
        }
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