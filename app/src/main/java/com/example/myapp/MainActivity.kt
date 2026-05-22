package com.example.myapp

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.myapp.databinding.ActivityMainBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var adapter: NewsAdapter
    private val apiService = NewsApiService.create()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupRecyclerView()

        binding.swipeRefreshLayout.setOnRefreshListener {
            fetchNews()
        }

        fetchNews()
    }

    private fun setupRecyclerView() {
        adapter = NewsAdapter()
        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        binding.recyclerView.adapter = adapter
    }

    private fun fetchNews() {
        binding.swipeRefreshLayout.isRefreshing = true
        binding.errorTextView.visibility = View.GONE

        lifecycleScope.launch {
            try {
                val response = withContext(Dispatchers.IO) {
                    apiService.getLatestNews(limit = 20)
                }

                binding.swipeRefreshLayout.isRefreshing = false
                binding.recyclerView.visibility = View.VISIBLE
                adapter.updateData(response.results)

            } catch (e: Exception) {
                binding.swipeRefreshLayout.isRefreshing = false
                binding.recyclerView.visibility = View.GONE
                binding.errorTextView.visibility = View.VISIBLE
                binding.errorTextView.text = "Error fetching news: ${e.message}"
            }
        }
    }
}
