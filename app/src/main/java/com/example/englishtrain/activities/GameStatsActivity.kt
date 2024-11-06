package com.example.englishtrain.activities

import android.os.Bundle
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.englishtrain.DictionaryDbHelper
import com.example.englishtrain.GameRecord
import com.example.englishtrain.R
import com.example.englishtrain.adapters.GameHistoryAdapter
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class GameStatsActivity : AppCompatActivity() {

    private lateinit var dbHelper: DictionaryDbHelper
    private lateinit var gameHistoryRecyclerView: RecyclerView
    private lateinit var gameDetailsTextView: TextView
    private lateinit var circularProgressBar: ProgressBar
    private lateinit var backButton: Button // Кнопка возврата
    private lateinit var gameHistoryAdapter: GameHistoryAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_game_stats)

        dbHelper = DictionaryDbHelper(this)

        // Инициализация виджетов
        gameHistoryRecyclerView = findViewById(R.id.rv_game_history)
        gameDetailsTextView = findViewById(R.id.tv_game_details)
        backButton = findViewById(R.id.btn_back_to_main)

        // Получаем историю игр
        val gameHistory = dbHelper.getGameHistory()

        if (gameHistory.isEmpty()) {
            Toast.makeText(this, "Нет записей о играх", Toast.LENGTH_SHORT).show()
        } else {
            // Инициализация адаптера
            gameHistoryAdapter = GameHistoryAdapter(this, gameHistory) { selectedRecord ->
                displayGameDetails(selectedRecord)
            }
            gameHistoryRecyclerView.layoutManager = LinearLayoutManager(this)
            gameHistoryRecyclerView.adapter = gameHistoryAdapter
        }

        // Обработка нажатия кнопки "Назад"
        backButton.setOnClickListener {
            finish()  // Закрыть активность и вернуться на предыдущий экран
        }
    }

    private fun displayGameDetails(gameRecord: GameRecord) {
        // Форматируем дату из Unix-времени
        val dateFormat = SimpleDateFormat("dd.MM.yyyy HH:mm:ss", Locale.getDefault())
        val date = Date(gameRecord.date.toLong()) // Переводим Unix-время в миллисекунды
        val formattedDate = dateFormat.format(date)

        // Создаем строку с подробностями игры
        val details = """
            Дата: $formattedDate
            Сложность: ${gameRecord.difficulty}
            Вопросов: ${gameRecord.totalQuestions}
            Правильных ответов: ${gameRecord.correctAnswers}
        """.trimIndent()

        // Отображаем подробности игры
        gameDetailsTextView.text = details
    }
}
