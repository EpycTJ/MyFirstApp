package com.example.myapp

import android.os.Bundle
import android.view.View
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.appcompat.app.AppCompatActivity
import coil.load
import com.example.myapp.databinding.ActivityArticleDetailBinding

class ArticleDetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityArticleDetailBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityArticleDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val articleUrl = intent.getStringExtra("ARTICLE_URL") ?: ""
        val imageUrl = intent.getStringExtra("IMAGE_URL") ?: ""
        val title = intent.getStringExtra("ARTICLE_TITLE") ?: ""

        setupToolbar(title)
        loadImage(imageUrl)
        setupWebView(articleUrl)
    }

    private fun setupToolbar(title: String) {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)
        binding.toolbar.setNavigationOnClickListener {
            finish()
        }
        binding.collapsingToolbar.title = title
        binding.collapsingToolbar.setExpandedTitleColor(resources.getColor(android.R.color.white, theme))
        binding.collapsingToolbar.setCollapsedTitleTextColor(resources.getColor(R.color.text_primary, theme))
    }

    private fun loadImage(url: String) {
        if (url.isNotEmpty()) {
            binding.detailImageView.load(url) {
                crossfade(true)
            }
        }
    }

    private fun setupWebView(url: String) {
        if (url.isEmpty()) {
            binding.webViewProgress.visibility = View.GONE
            return
        }

        binding.webView.settings.apply {
            javaScriptEnabled = true
            domStorageEnabled = true
            useWideViewPort = true
            loadWithOverviewMode = true
        }

        binding.webView.webViewClient = object : WebViewClient() {
            override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
                view?.loadUrl(request?.url.toString())
                return true
            }

            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)
                binding.webViewProgress.visibility = View.GONE
            }
        }

        binding.webView.webChromeClient = object : WebChromeClient() {
            override fun onProgressChanged(view: WebView?, newProgress: Int) {
                if (newProgress == 100) {
                    binding.webViewProgress.visibility = View.GONE
                }
            }
        }

        binding.webView.loadUrl(url)
    }

    override fun onBackPressed() {
        if (binding.webView.canGoBack()) {
            binding.webView.goBack()
        } else {
            super.onBackPressed()
        }
    }
}
