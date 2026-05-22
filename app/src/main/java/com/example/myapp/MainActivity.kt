package com.example.myapp

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.myapp.databinding.ActivityMainBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var adapter: NewsAdapter
    private val apiService = NewsApiService.create()

    private var currentOffset = 0
    private var isLoading = false
    private val limit = 20

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
        val layoutManager = LinearLayoutManager(this)
        binding.recyclerView.layoutManager = layoutManager
        binding.recyclerView.adapter = adapter

        binding.recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                if (dy > 0) {
                    val visibleItemCount = layoutManager.childCount
                    val totalItemCount = layoutManager.itemCount
                    val pastVisibleItems = layoutManager.findFirstVisibleItemPosition()

                    if (!isLoading) {
                        if ((visibleItemCount + pastVisibleItems) >= totalItemCount) {
                            fetchMoreNews()
                        }
                    }
                }
            }
        })
    }

    private fun fetchNews() {
        currentOffset = 0
        isLoading = true
        binding.swipeRefreshLayout.isRefreshing = true
        binding.errorTextView.visibility = View.GONE

        lifecycleScope.launch {
            try {
                val response = withContext(Dispatchers.IO) {
                    apiService.getLatestNews(limit = limit, offset = currentOffset)
                }

                currentOffset += limit
                isLoading = false
                binding.swipeRefreshLayout.isRefreshing = false
                binding.recyclerView.visibility = View.VISIBLE
                adapter.updateData(response.results)

            } catch (e: Exception) {
                isLoading = false
                binding.swipeRefreshLayout.isRefreshing = false
                binding.recyclerView.visibility = View.GONE
                binding.errorTextView.visibility = View.VISIBLE
                binding.errorTextView.text = "Error fetching news: ${e.message}"
            }
        }
    }

    private fun fetchMoreNews() {
        isLoading = true

        lifecycleScope.launch {
            try {
                val response = withContext(Dispatchers.IO) {
                    apiService.getLatestNews(limit = limit, offset = currentOffset)
                }

                currentOffset += limit
                isLoading = false
                adapter.appendData(response.results)

            } catch (e: Exception) {
                isLoading = false
                // Optional: show a snackbar or toast for pagination error
            }
        }
    }
}
