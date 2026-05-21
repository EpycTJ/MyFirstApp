package com.example.myapp

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.myapp.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val newsList = generateDummyNews()
        val adapter = NewsAdapter(newsList)

        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        binding.recyclerView.adapter = adapter
    }

    private fun generateDummyNews(): List<News> {
        return listOf(
            News("Breaking: Local Kotlin developer builds amazing app", "In an incredible turn of events, a developer built an amazing app in minutes using Jetpack and Material Design."),
            News("Tech Stocks Surge", "Major tech companies see record highs following strong earnings reports and AI announcements."),
            News("New Restaurant Downtown", "The much-anticipated fusion restaurant finally opened its doors to long lines and rave reviews."),
            News("Weather Update: Weekend Forecast", "Expect sunny skies and warm temperatures this weekend. Perfect time for outdoor activities!"),
            News("Sports Highlights", "The home team secured a thrilling victory in the final seconds of yesterday's championship match.")
        )
    }
}
