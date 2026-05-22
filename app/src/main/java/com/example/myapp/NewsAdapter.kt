package com.example.myapp

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import coil.load
import coil.transform.CircleCropTransformation
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
            newsSiteTextView.text = article.newsSite
            newsDateTextView.text = formatPublishedDate(article.publishedAt)

            newsSummaryTextView.text = article.summary
            newsSummaryTextView.visibility = View.VISIBLE

            newsImageView.load(article.imageUrl) {
                crossfade(true)
            }

            // Generate a simple site icon (or load logo) - using the image for now or clear it
            siteIcon.load(article.imageUrl) {
                crossfade(true)
                transformations(CircleCropTransformation())
            }

            root.setOnClickListener {
                val intent = Intent(holder.itemView.context, ArticleDetailActivity::class.java).apply {
                    putExtra("ARTICLE_URL", article.url)
                    putExtra("IMAGE_URL", article.imageUrl)
                    putExtra("ARTICLE_TITLE", article.title)
                }
                holder.itemView.context.startActivity(intent)
            }

            shareButton.setOnClickListener {
                val sendIntent: Intent = Intent().apply {
                    action = Intent.ACTION_SEND
                    putExtra(Intent.EXTRA_TEXT, "${article.title}\n\n${article.url}")
                    type = "text/plain"
                }
                val shareIntent = Intent.createChooser(sendIntent, null)
                holder.itemView.context.startActivity(shareIntent)
            }
        }
    }

    private fun formatPublishedDate(dateString: String): String {
        return try {
            val instant = java.time.Instant.parse(dateString)
            val now = java.time.Instant.now()
            val diff = java.time.Duration.between(instant, now)

            when {
                diff.toDays() > 0 -> "${diff.toDays()}d ago"
                diff.toHours() > 0 -> "${diff.toHours()}h ago"
                diff.toMinutes() > 0 -> "${diff.toMinutes()}m ago"
                else -> "Just now"
            }
        } catch (e: Exception) {
            dateString.split("T")[0]
        }
    }

    override fun getItemCount(): Int = articles.size

    fun updateData(newArticles: List<NewsArticle>) {
        articles = newArticles
        notifyDataSetChanged()
    }
}
