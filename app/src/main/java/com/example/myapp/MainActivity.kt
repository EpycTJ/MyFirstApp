package com.example.myapp

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.KeyEvent
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import androidx.appcompat.app.AppCompatActivity
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

        setupRecyclerView()
        setupChips()
        setupRefreshColors()
        setupSearch()

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

    private fun setupRefreshColors() {
        binding.swipeRefreshLayout.setColorSchemeResources(R.color.accent_blue)
        binding.swipeRefreshLayout.setProgressBackgroundColorSchemeResource(R.color.bg_light)
    }

    private fun setupSearch() {
        binding.btnSearch.setOnClickListener {
            if (binding.searchEditText.visibility == View.VISIBLE) {
                binding.searchEditText.visibility = View.GONE
                // Hide keyboard
                val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
                imm.hideSoftInputFromWindow(binding.searchEditText.windowToken, 0)
                // Clear search if empty
                if (binding.searchEditText.text.isNullOrBlank() && searchQuery != null) {
                    searchQuery = null
                    refreshNews()
                }
            } else {
                binding.searchEditText.visibility = View.VISIBLE
                binding.searchEditText.requestFocus()
                // Show keyboard
                val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
                imm.showSoftInput(binding.searchEditText, InputMethodManager.SHOW_IMPLICIT)
            }
        }

        binding.searchEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                searchJob?.cancel()
                searchJob = lifecycleScope.launch {
                    delay(500)
                    val newQuery = if (s.isNullOrBlank()) null else s.toString()
                    if (searchQuery != newQuery) {
                        searchQuery = newQuery
                        refreshNews()
                    }
                }
            }
        })

        binding.searchEditText.setOnEditorActionListener { v, actionId, event ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH ||
                actionId == EditorInfo.IME_ACTION_DONE ||
                event?.action == KeyEvent.ACTION_DOWN && event.keyCode == KeyEvent.KEYCODE_ENTER) {

                // Hide keyboard
                val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
                imm.hideSoftInputFromWindow(v.windowToken, 0)

                searchQuery = if (v.text.isNullOrBlank()) null else v.text.toString()
                refreshNews()
                true
            } else {
                false
            }
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
            binding.mainProgress.visibility = View.VISIBLE
        }
        binding.errorTextView.visibility = View.GONE
        binding.emptyStateLayout.visibility = View.GONE

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
                binding.mainProgress.visibility = View.GONE
                binding.recyclerView.visibility = View.VISIBLE

                if (isLoadMore) {
                    allArticles.addAll(response.results)
                } else {
                    allArticles = response.results.toMutableList()
                }

                adapter.updateData(allArticles)
                currentOffset += pageSize
                isLoading = false

                if (allArticles.isEmpty()) {
                    binding.recyclerView.visibility = View.GONE
                    binding.emptyStateLayout.visibility = View.VISIBLE
                }

            } catch (e: Exception) {
                isLoading = false
                binding.swipeRefreshLayout.isRefreshing = false
                binding.mainProgress.visibility = View.GONE
                if (!isLoadMore) {
                    binding.recyclerView.visibility = View.GONE
                    binding.errorTextView.visibility = View.VISIBLE
                    binding.errorTextView.text = "Error fetching news: ${e.message}"
                }
            }
        }
    }
}
