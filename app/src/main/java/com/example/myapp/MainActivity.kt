package com.example.myapp

import android.os.Bundle
import android.view.Menu
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.myapp.databinding.ActivityMainBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var adapter: NewsAdapter
    private val apiService = NewsApiService.create()

    private var currentTopic = "All"
    private var searchQuery: String? = null
    private var isLoading = false
    private var currentOffset = 0
    private val pageSize = 20
    private var allArticles = mutableListOf<NewsArticle>()
    private var searchJob: Job? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        setupRecyclerView()
        setupChips()

        binding.swipeRefreshLayout.setOnRefreshListener {
            refreshNews()
        }

        fetchNews()
    }

    private fun setupRecyclerView() {
        adapter = NewsAdapter()
        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        binding.recyclerView.adapter = adapter

        binding.recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                val layoutManager = recyclerView.layoutManager as LinearLayoutManager
                val visibleItemCount = layoutManager.childCount
                val totalItemCount = layoutManager.itemCount
                val firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition()

                if (!isLoading) {
                    if ((visibleItemCount + firstVisibleItemPosition) >= totalItemCount
                        && firstVisibleItemPosition >= 0
                    ) {
                        fetchNews(isLoadMore = true)
                    }
                }
            }
        })
    }

    private fun setupChips() {
        binding.chipGroup.setOnCheckedStateChangeListener { group, checkedIds ->
            val checkedId = checkedIds.firstOrNull() ?: return@setOnCheckedStateChangeListener
            currentTopic = when (checkedId) {
                R.id.chipNasa -> "NASA"
                R.id.chipSpaceX -> "SpaceX"
                R.id.chipBlogs -> "Blogs"
                R.id.chipReports -> "Reports"
                else -> "All"
            }
            refreshNews()
        }
    }

    private fun refreshNews() {
        currentOffset = 0
        allArticles.clear()
        fetchNews()
    }

    private fun fetchNews(isLoadMore: Boolean = false) {
        if (isLoading) return
        isLoading = true

        if (!isLoadMore) {
            binding.swipeRefreshLayout.isRefreshing = true
        }
        binding.errorTextView.visibility = View.GONE

        lifecycleScope.launch {
            try {
                val response = withContext(Dispatchers.IO) {
                    when (currentTopic) {
                        "Blogs" -> apiService.getBlogs(limit = pageSize, offset = currentOffset, search = searchQuery)
                        "Reports" -> apiService.getReports(limit = pageSize, offset = currentOffset, search = searchQuery)
                        "NASA" -> apiService.getArticles(limit = pageSize, offset = currentOffset, search = searchQuery, newsSite = "NASA")
                        "SpaceX" -> apiService.getArticles(limit = pageSize, offset = currentOffset, search = searchQuery, newsSite = "SpaceX")
                        else -> apiService.getArticles(limit = pageSize, offset = currentOffset, search = searchQuery)
                    }
                }

                binding.swipeRefreshLayout.isRefreshing = false
                binding.recyclerView.visibility = View.VISIBLE

                if (isLoadMore) {
                    allArticles.addAll(response.results)
                } else {
                    allArticles = response.results.toMutableList()
                }

                adapter.updateData(allArticles)
                currentOffset += pageSize
                isLoading = false

            } catch (e: Exception) {
                isLoading = false
                binding.swipeRefreshLayout.isRefreshing = false
                if (!isLoadMore) {
                    binding.recyclerView.visibility = View.GONE
                    binding.errorTextView.visibility = View.VISIBLE
                    binding.errorTextView.text = "Error fetching news: ${e.message}"
                }
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        val searchItem = menu.findItem(R.id.action_search)
        val searchView = searchItem.actionView as SearchView

        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                searchQuery = query
                refreshNews()
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                searchJob?.cancel()
                searchJob = lifecycleScope.launch {
                    delay(500)
                    searchQuery = if (newText.isNullOrBlank()) null else newText
                    refreshNews()
                }
                return true
            }
        })

        return true
    }
}
