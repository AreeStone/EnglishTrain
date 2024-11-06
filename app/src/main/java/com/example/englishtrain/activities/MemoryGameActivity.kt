package com.example.englishtrain.activities

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.Spinner
import androidx.appcompat.app.AppCompatActivity
import com.example.englishtrain.MainActivity
import com.example.englishtrain.R

class MemoryGameActivity : AppCompatActivity() {

    private lateinit var spinnerDifficulty: Spinner
    private lateinit var spinnerQuestionCount: Spinner
    private lateinit var btnStartGame: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_memory_game)

        // Инициализация виджетов
        spinnerDifficulty = findViewById(R.id.spinner_difficulty)
        spinnerQuestionCount = findViewById(R.id.spinner_question_count)
        btnStartGame = findViewById(R.id.btn_start_game)

        btnStartGame.setOnClickListener {
            val difficulty = spinnerDifficulty.selectedItemPosition + 1 // Сложность: 1 - Легкий, 2 - Средний, 3 - Сложный
            val questionCount = spinnerQuestionCount.selectedItem.toString().toInt()


            // Переход в активность квиза и передача настроек
            val intent = Intent(this, QuizGameActivity::class.java).apply {
                putExtra("DIFFICULTY", difficulty)
                putExtra("QUESTION_COUNT", questionCount)
            }
            startActivity(intent)
        }

        val btnMainMenu: Button = findViewById(R.id.btn_main_menu)
        btnMainMenu.setOnClickListener {
            startActivity(Intent(this, MainActivity::class.java))
        }
    }
}
