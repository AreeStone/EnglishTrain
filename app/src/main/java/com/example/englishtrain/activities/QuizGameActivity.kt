package com.example.englishtrain.activities

import android.os.Bundle
import android.os.Handler
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.englishtrain.DictionaryDbHelper
import com.example.englishtrain.R
import com.example.englishtrain.Word
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class QuizGameActivity : AppCompatActivity() {

    private lateinit var dbHelper: DictionaryDbHelper
    private lateinit var tvRussianWord: TextView
    private lateinit var ivResultImage: ImageView // ImageView для результатов
    private var wordList: List<Word> = emptyList()
    private var currentQuestionIndex = 0
    private var correctAnswers = 0
    private var totalQuestions = 0
    private var difficulty: Int = 1
    private var questionCount: Int = 5
    private var questionsAsked = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_quiz_game)

        dbHelper = DictionaryDbHelper(this)

        // Получаем параметры из Intent
        difficulty = intent.getIntExtra("DIFFICULTY", 1)
        questionCount = intent.getIntExtra("QUESTION_COUNT", 5)

        // Инициализация виджетов
        tvRussianWord = findViewById(R.id.tv_russian_word)
        ivResultImage = findViewById(R.id.iv_result_image)

        startGame()
    }

    private fun startGame() {
        val words = dbHelper.filterByDifficulty(difficulty)
        wordList = words.shuffled().take(questionCount * 4)
        currentQuestionIndex = 0
        correctAnswers = 0
        questionsAsked = 0

        showNextQuestion()
    }

    private fun showNextQuestion() {
        if (questionsAsked >= questionCount || currentQuestionIndex >= wordList.size) {
            // Игра окончена
            val percentage = (correctAnswers.toFloat() / questionCount) * 100
            showResultImage(percentage)

            Toast.makeText(this, "Игра окончена! Правильных ответов: $correctAnswers", Toast.LENGTH_LONG).show()
            saveGameHistory()
            Handler().postDelayed({
                finish()
            }, 2000)
            return
        }

        val correctWord = wordList[currentQuestionIndex]
        val incorrectWords = wordList.filter { it.russian != correctWord.russian }.shuffled().take(3)
        val options = (incorrectWords + correctWord).shuffled()

        tvRussianWord.text = correctWord.russian

        val buttons = listOf<Button>(
            findViewById(R.id.btn_option_1),
            findViewById(R.id.btn_option_2),
            findViewById(R.id.btn_option_3),
            findViewById(R.id.btn_option_4)
        )

        // Отключаем все кнопки до выбора
        buttons.forEach { it.isEnabled = true }

        buttons.forEachIndexed { index, button ->
            button.text = options[index].english
            button.setBackgroundColor(ContextCompat.getColor(this, android.R.color.darker_gray))
            button.setOnClickListener {
                val isCorrect = options[index] == correctWord
                // Отключаем все кнопки после первого выбора
                buttons.forEach { it.isEnabled = false }

                if (isCorrect) {
                    correctAnswers++
                    button.setBackgroundColor(ContextCompat.getColor(this, android.R.color.holo_green_light))
                } else {
                    button.setBackgroundColor(ContextCompat.getColor(this, android.R.color.holo_red_light))
                }

                // Ожидание 1 секунду перед следующим вопросом
                Handler().postDelayed({
                    // Сбрасываем цвет кнопок
                    buttons.forEach { it.setBackgroundColor(ContextCompat.getColor(this, android.R.color.darker_gray)) }
                    questionsAsked++
                    currentQuestionIndex++
                    // Переходим к следующему вопросу
                    showNextQuestion()
                }, 1000)
            }
        }
    }

    private fun showResultImage(percentage: Float) {
        ivResultImage.visibility = ImageView.VISIBLE

        // Показать изображение в зависимости от процента правильных ответов
        when {
            percentage == 100f -> ivResultImage.setImageResource(R.drawable.image_100)  // 100%
            percentage >= 50f -> ivResultImage.setImageResource(R.drawable.image_50_100)  // От 50 до 100%
            else -> ivResultImage.setImageResource(R.drawable.image_under_50)  // Меньше 50%
        }

        // Скрыть изображение через 1 секунду
        Handler().postDelayed({
            ivResultImage.visibility = ImageView.GONE
        }, 3000)
    }

    private fun saveGameHistory() {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        val date = dateFormat.format(Date())
        dbHelper.insertGameStats(
            difficulty = difficulty,
            questionsAsked = questionCount,
            correctAnswers = correctAnswers
        )
    }
}
