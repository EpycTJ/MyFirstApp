package com.example.myapp

import android.content.Intent
import android.net.Uri
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.browser.customtabs.CustomTabsIntent
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.example.myapp.databinding.ItemNewsBinding

class NewsAdapter(private var articles: List<NewsArticle> = emptyList()) :
    RecyclerView.Adapter<NewsAdapter.NewsViewHolder>() {

    class NewsViewHolder(val binding: ItemNewsBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NewsViewHolder {
        val binding = ItemNewsBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return NewsViewHolder(binding)
    }

    override fun onBindViewHolder(holder: NewsViewHolder, position: Int) {
        val article = articles[position]
        with(holder.binding) {
            newsTitleTextView.text = article.title
            newsSummaryTextView.text = article.summary
            newsSiteTextView.text = article.newsSite

            newsImageView.load(article.imageUrl) {
                crossfade(true)
                // You can add placeholder or error images here if needed
                // placeholder(R.drawable.placeholder)
                // error(R.drawable.error)
            }

            root.setOnClickListener {
                val customTabsIntent = CustomTabsIntent.Builder().build()
                customTabsIntent.launchUrl(holder.itemView.context, Uri.parse(article.url))
            }
        }
    }

    override fun getItemCount(): Int = articles.size

    fun updateData(newArticles: List<NewsArticle>) {
        articles = newArticles
        notifyDataSetChanged()
    }
}
