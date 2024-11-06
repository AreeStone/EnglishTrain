package com.example.englishtrain

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import android.widget.Button
import com.example.englishtrain.activities.DictionaryActivity
import com.example.englishtrain.activities.GameStatsActivity
import com.example.englishtrain.activities.MemoryGameActivity

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val btnDictionary: Button = findViewById(R.id.btn_dictionary)
        btnDictionary.setOnClickListener {
            startActivity(Intent(this, DictionaryActivity::class.java))
        }

        val btnMemory: Button = findViewById(R.id.btn_memory)
        btnMemory.setOnClickListener {
            startActivity(Intent(this, MemoryGameActivity::class.java))
        }

        val btnStats: Button = findViewById(R.id.btn_stats)
        btnStats.setOnClickListener {
            startActivity(Intent(this, GameStatsActivity::class.java))
        }
    }
}
