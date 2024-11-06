package com.example.englishtrain.activities

import android.content.Intent
import android.os.Bundle
import android.speech.tts.TextToSpeech
import androidx.activity.ComponentActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import android.widget.Button
import android.widget.EditText
import android.text.Editable
import android.text.TextWatcher
import android.widget.Spinner
import com.example.englishtrain.DictionaryDbHelper
import com.example.englishtrain.MainActivity
import com.example.englishtrain.R
import com.example.englishtrain.adapters.WordAdapter
import java.util.*

class DictionaryActivity : ComponentActivity() {
    private lateinit var dbHelper: DictionaryDbHelper
    private lateinit var wordAdapter: WordAdapter
    private lateinit var recyclerView: RecyclerView
    private lateinit var etWord: EditText
    private lateinit var etTranslation: EditText
    private lateinit var etSearch: EditText
    private lateinit var btnDeleteSelective: Button
    private lateinit var tts: TextToSpeech
    private lateinit var spinnerDifficulty: Spinner
    private var isDeleteMode = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dictionary)

        dbHelper = DictionaryDbHelper(this)

        recyclerView = findViewById(R.id.recycler_view)
        etWord = findViewById(R.id.et_word)
        etTranslation = findViewById(R.id.et_translation)
        etSearch = findViewById(R.id.et_search)  // Инициализация поля для поиска
        val btnAddWord: Button = findViewById(R.id.btn_add_word)
        val buttonBackToMain: Button = findViewById(R.id.button_back_to_main)
        btnDeleteSelective = findViewById(R.id.btn_delete_word)
        spinnerDifficulty = findViewById(R.id.spinner_difficulty)

        tts = TextToSpeech(this) { status ->
            if (status == TextToSpeech.SUCCESS) {
                tts.language = Locale.US
            }
        }

        recyclerView.layoutManager = LinearLayoutManager(this)
        updateWordList()

        btnAddWord.setOnClickListener {
            val englishWord = etWord.text.toString()
            val russianTranslation = etTranslation.text.toString()
            val difficulty = spinnerDifficulty.selectedItemPosition + 1 // 1 - Легкий, 2 - Средний, 3 - Сложный

            if (englishWord.isNotBlank() && russianTranslation.isNotBlank()) {
                dbHelper.addWord(englishWord, russianTranslation, difficulty)
                etWord.text.clear()
                etTranslation.text.clear()
                updateWordList()
            }
        }

        buttonBackToMain.setOnClickListener {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }

        btnDeleteSelective.setOnClickListener {
            toggleDeleteMode()
        }

        // Добавление TextWatcher для поля поиска
        etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                filterWordList(s.toString())
            }

            override fun afterTextChanged(s: Editable?) {}
        })
    }

    private fun updateWordList() {
        // Сохраняем текущую позицию прокрутки
        val layoutManager = recyclerView.layoutManager as LinearLayoutManager
        val firstVisiblePosition = layoutManager.findFirstVisibleItemPosition()

        val words = dbHelper.getAllWords()
        wordAdapter = WordAdapter(
            words,
            isDeleteMode,
            onDeleteClick = { wordId ->
                dbHelper.deleteWordById(wordId)
                updateWordList()
            },
            onPlayClick = { english, russian ->
                playWordAndTranslation(english, russian)
            },
            onLearnedStatusChanged = { wordId, isLearned ->
                dbHelper.updateWordLearnedStatus(wordId, isLearned) // Обновляем статус изучения в базе
                updateWordList()
            }
        )
        recyclerView.adapter = wordAdapter

        // Восстанавливаем позицию прокрутки
        recyclerView.post {
            layoutManager.scrollToPositionWithOffset(firstVisiblePosition, 0)
        }
    }


    private fun filterWordList(query: String) {
        val words = dbHelper.getAllWords()
        val filteredWords = words.filter { (_, english, russian) ->
            english.contains(query, ignoreCase = true) || russian.contains(query, ignoreCase = true)
        }
        wordAdapter.updateWords(filteredWords)
    }

    private fun toggleDeleteMode() {
        isDeleteMode = !isDeleteMode
        btnDeleteSelective.text = if (isDeleteMode) "Вернуться" else "Удалить слово"
        updateWordList()
    }

    private fun playWordAndTranslation(english: String, russian: String) {
        tts.speak("$english", TextToSpeech.QUEUE_FLUSH, null, null)
    }

    override fun onDestroy() {
        super.onDestroy()
        tts.stop()
        tts.shutdown()
    }
}
