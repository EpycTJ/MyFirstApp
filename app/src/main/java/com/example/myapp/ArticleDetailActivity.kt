package com.example.myapp

import android.os.Bundle
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import coil.load

class ArticleDetailActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_article_detail)

        val url = intent.getStringExtra("EXTRA_URL") ?: ""
        val imageUrl = intent.getStringExtra("EXTRA_IMAGE_URL") ?: ""
        val title = intent.getStringExtra("EXTRA_TITLE") ?: ""

        val backButton: ImageButton = findViewById(R.id.backButton)
        val articleImageView: ImageView = findViewById(R.id.articleImageView)
        val articleTitleTextView: TextView = findViewById(R.id.articleTitleTextView)
        val webView: WebView = findViewById(R.id.webView)

        backButton.setOnClickListener { finish() }

        if (imageUrl.isNotEmpty()) {
            articleImageView.load(imageUrl) {
                crossfade(true)
            }
        }

        articleTitleTextView.text = title

        webView.settings.javaScriptEnabled = true
        webView.webViewClient = WebViewClient()
        webView.webChromeClient = WebChromeClient()
        if (url.isNotEmpty()) {
            webView.loadUrl(url)
        }
    }
}
