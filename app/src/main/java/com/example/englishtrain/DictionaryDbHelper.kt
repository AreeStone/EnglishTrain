package com.example.englishtrain

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class DictionaryDbHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_NAME = "dictionary.db"
        private const val DATABASE_VERSION = 4

        const val TABLE_WORDS = "words"
        const val COLUMN_ID = "_id"
        const val COLUMN_ENGLISH = "english"
        const val COLUMN_RUSSIAN = "russian"
        const val COLUMN_IS_LEARNED = "is_learned"
        const val COLUMN_DIFFICULTY = "difficulty"

        const val TABLE_GAME_STATS = "game_stats"
        const val COLUMN_GAME_ID = "_id"
        const val COLUMN_GAME_DIFFICULTY = "difficulty"
        const val COLUMN_GAME_QUESTIONS_ASKED = "questions_asked"
        const val COLUMN_GAME_CORRECT_ANSWERS = "correct_answers"
        const val COLUMN_GAME_DATE = "game_date"
    }

    override fun onCreate(db: SQLiteDatabase) {
        val createTable = """
            CREATE TABLE $TABLE_WORDS (
                $COLUMN_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COLUMN_ENGLISH TEXT,
                $COLUMN_RUSSIAN TEXT,
                $COLUMN_IS_LEARNED INTEGER DEFAULT 0,
                $COLUMN_DIFFICULTY INTEGER DEFAULT 1
            )
        """
        db.execSQL(createTable)

        val createGameStatsTable = """
            CREATE TABLE $TABLE_GAME_STATS (
                $COLUMN_GAME_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COLUMN_GAME_DIFFICULTY INTEGER,
                $COLUMN_GAME_QUESTIONS_ASKED INTEGER,
                $COLUMN_GAME_CORRECT_ANSWERS INTEGER,
                $COLUMN_GAME_DATE TEXT
            )
        """
        db.execSQL(createGameStatsTable)

        val cursor = db.query(TABLE_WORDS, null, null, null, null, null, null)
        if (cursor.count == 0) {
            insertDefaultWords(db)
        }
        cursor.close()
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        if (oldVersion < 2) {
            db.execSQL("ALTER TABLE $TABLE_WORDS ADD COLUMN $COLUMN_IS_LEARNED INTEGER DEFAULT 0")
            db.execSQL("ALTER TABLE $TABLE_WORDS ADD COLUMN $COLUMN_DIFFICULTY INTEGER DEFAULT 1")
        }
        if (oldVersion < 4) {
            // Обновляем базу данных и создаем таблицу для статистики
            db.execSQL("""
                CREATE TABLE IF NOT EXISTS $TABLE_GAME_STATS (
                    $COLUMN_GAME_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                    $COLUMN_GAME_DIFFICULTY INTEGER,
                    $COLUMN_GAME_QUESTIONS_ASKED INTEGER,
                    $COLUMN_GAME_CORRECT_ANSWERS INTEGER,
                    $COLUMN_GAME_DATE TEXT
                )
            """)
        }
    }

    fun insertGameStats(difficulty: Int, questionsAsked: Int, correctAnswers: Int) {
        val db = writableDatabase
        val contentValues = ContentValues().apply {
            put(COLUMN_GAME_DIFFICULTY, difficulty)
            put(COLUMN_GAME_QUESTIONS_ASKED, questionsAsked)
            put(COLUMN_GAME_CORRECT_ANSWERS, correctAnswers)
            put(COLUMN_GAME_DATE, System.currentTimeMillis().toString()) // Дата в миллисекундах
        }
        db.insert(TABLE_GAME_STATS, null, contentValues)
        db.close()
    }

    fun getGameHistory(): List<GameRecord> {
        val records = mutableListOf<GameRecord>()
        val db = readableDatabase
        val cursor = db.query(TABLE_GAME_STATS, null, null, null, null, null, "$COLUMN_GAME_DATE DESC")

        with(cursor) {
            while (moveToNext()) {
                val date = getString(getColumnIndexOrThrow(COLUMN_GAME_DATE))
                val correctAnswers = getInt(getColumnIndexOrThrow(COLUMN_GAME_CORRECT_ANSWERS))
                val questionsAsked = getInt(getColumnIndexOrThrow(COLUMN_GAME_QUESTIONS_ASKED))
                val difficulty = getString(getColumnIndexOrThrow(COLUMN_GAME_DIFFICULTY))

                records.add(GameRecord(date, correctAnswers, questionsAsked, difficulty))
            }
        }
        cursor.close()
        db.close()

        return records
    }

    fun addWord(english: String, russian: String, difficulty: Int = 1) {
        val db = writableDatabase
        val contentValues = ContentValues().apply {
            put(COLUMN_ENGLISH, english)
            put(COLUMN_RUSSIAN, russian)
            put(COLUMN_DIFFICULTY, difficulty)
            put(COLUMN_IS_LEARNED, 0) // по умолчанию слово не изучено
        }
        db.insert(TABLE_WORDS, null, contentValues)
        db.close()
    }

    fun getAllWords(): List<Word> {
        val wordsList = mutableListOf<Word>()
        val db = readableDatabase
        val cursor = db.query(TABLE_WORDS, null, null, null, null, null, null)

        with(cursor) {
            while (moveToNext()) {
                val id = getInt(getColumnIndexOrThrow(COLUMN_ID))
                val english = getString(getColumnIndexOrThrow(COLUMN_ENGLISH))
                val russian = getString(getColumnIndexOrThrow(COLUMN_RUSSIAN))
                val isLearned = getInt(getColumnIndexOrThrow(COLUMN_IS_LEARNED)) == 1
                val difficulty = getInt(getColumnIndexOrThrow(COLUMN_DIFFICULTY))
                wordsList.add(Word(id, english, russian, isLearned, difficulty))
            }
        }
        cursor.close()
        db.close()
        return wordsList
    }

    fun updateWordLearnedStatus(wordId: Int, isLearned: Boolean) {
        val db = writableDatabase
        val contentValues = ContentValues().apply {
            put(COLUMN_IS_LEARNED, if (isLearned) 1 else 0)
        }
        db.update(TABLE_WORDS, contentValues, "$COLUMN_ID = ?", arrayOf(wordId.toString()))
        db.close()
    }

    fun updateWordById(id: Int, newEnglish: String, newRussian: String, newIsLearned: Boolean, newDifficulty: Int) {
        val db = writableDatabase
        val contentValues = ContentValues().apply {
            put(COLUMN_ENGLISH, newEnglish)
            put(COLUMN_RUSSIAN, newRussian)
            put(COLUMN_IS_LEARNED, if (newIsLearned) 1 else 0)
            put(COLUMN_DIFFICULTY, newDifficulty)
        }
        db.update(TABLE_WORDS, contentValues, "$COLUMN_ID = ?", arrayOf(id.toString()))
        db.close()
    }


    fun deleteWordById(id: Int) {
        val db = writableDatabase
        db.delete(TABLE_WORDS, "$COLUMN_ID = ?", arrayOf(id.toString()))
        db.close()
    }

    fun filterByDifficulty(difficulty: Int): List<Word> {
        val filteredList = mutableListOf<Word>()
        val db = readableDatabase
        val cursor = db.query(TABLE_WORDS, null, "$COLUMN_DIFFICULTY = ?", arrayOf(difficulty.toString()), null, null, null)

        with(cursor) {
            while (moveToNext()) {
                val id = getInt(getColumnIndexOrThrow(COLUMN_ID))
                val english = getString(getColumnIndexOrThrow(COLUMN_ENGLISH))
                val russian = getString(getColumnIndexOrThrow(COLUMN_RUSSIAN))
                val isLearned = getInt(getColumnIndexOrThrow(COLUMN_IS_LEARNED)) == 1
                val difficultyLevel = getInt(getColumnIndexOrThrow(COLUMN_DIFFICULTY))
                filteredList.add(Word(id, english, russian, isLearned, difficultyLevel))
            }
        }
        cursor.close()
        db.close()
        return filteredList
    }

    private fun insertDefaultWords(db: SQLiteDatabase) {
        val words = listOf(
            // Сложность 1
            Triple("cat", "кот", 1),
            Triple("dog", "собака", 1),
            Triple("apple", "яблоко", 1),
            Triple("orange", "апельсин", 1),
            Triple("car", "машина", 1),
            Triple("bus", "автобус", 1),
            Triple("book", "книга", 1),
            Triple("pen", "ручка", 1),
            Triple("house", "дом", 1),
            Triple("tree", "дерево", 1),
            Triple("school", "школа", 1),
            Triple("friend", "друг", 1),
            Triple("pen", "перо", 1),
            Triple("water", "вода", 1),
            Triple("keyboard", "клавиатура", 1),
            Triple("window", "окно", 1),
            Triple("sun", "солнце", 1),
            Triple("moon", "луна", 1),
            Triple("star", "звезда", 1),
            Triple("cloud", "облако", 1),
            Triple("night", "ночь", 1),
            Triple("day", "день", 1),
            Triple("light", "свет", 1),
            Triple("dark", "темный", 1),
            Triple("sky", "небо", 1),
            Triple("rain", "дождь", 1),
            Triple("snow", "снег", 1),
            Triple("cold", "холодный", 1),
            Triple("hot", "горячий", 1),
            Triple("cat", "кошка", 1),
            Triple("dog", "пес", 1),
            Triple("bird", "птица", 1),
            Triple("fish", "рыба", 1),
            Triple("tree", "дерево", 1),
            Triple("mountain", "гора", 1),

            // Сложность 2
            Triple("computer", "компьютер", 2),
            Triple("telephone", "телефон", 2),
            Triple("bicycle", "велосипед", 2),
            Triple("friend", "друг", 2),
            Triple("school", "школа", 2),
            Triple("student", "студент", 2),
            Triple("teacher", "учитель", 2),
            Triple("restaurant", "ресторан", 2),
            Triple("hotel", "отель", 2),
            Triple("university", "университет", 2),
            Triple("library", "библиотека", 2),
            Triple("city", "город", 2),
            Triple("street", "улица", 2),
            Triple("work", "работа", 2),
            Triple("office", "офис", 2),
            Triple("shop", "магазин", 2),
            Triple("market", "рынок", 2),
            Triple("park", "парк", 2),
            Triple("music", "музыка", 2),
            Triple("movie", "фильм", 2),
            Triple("book", "книга", 2),
            Triple("language", "язык", 2),
            Triple("question", "вопрос", 2),
            Triple("answer", "ответ", 2),
            Triple("idea", "идея", 2),
            Triple("history", "история", 2),
            Triple("dream", "мечта", 2),
            Triple("art", "искусство", 2),
            Triple("government", "правительство", 2),

            // Сложность 3
            Triple("universe", "вселенная", 3),
            Triple("technology", "технология", 3),
            Triple("philosophy", "философия", 3),
            Triple("literature", "литература", 3),
            Triple("mathematics", "математика", 3),
            Triple("architecture", "архитектура", 3),
            Triple("biology", "биология", 3),
            Triple("chemistry", "химия", 3),
            Triple("physics", "физика", 3),
            Triple("psychology", "психология", 3),
            Triple("economics", "экономика", 3),
            Triple("politics", "политика", 3),
            Triple("pharmaceutical", "фармацевтический", 3),
            Triple("astronomy", "астрономия", 3),
            Triple("engineering", "инженерия", 3),
            Triple("medical", "медицинский", 3),
            Triple("scientific", "научный", 3),
            Triple("innovation", "инновация", 3),
            Triple("internet", "интернет", 3),
            Triple("artificial", "искусственный", 3),
            Triple("intelligence", "интеллект", 3),
            Triple("robot", "робот", 3),
            Triple("machine", "машина", 3),
            Triple("algorithm", "алгоритм", 3),
            Triple("software", "программное обеспечение", 3),
            Triple("network", "сеть", 3),
            Triple("database", "база данных", 3),
            Triple("cloud", "облако", 3),
            Triple("development", "разработка", 3)
        )

        val contentValues = ContentValues()
        for ((english, russian, difficulty) in words) {
            contentValues.clear()
            contentValues.put(COLUMN_ENGLISH, english)
            contentValues.put(COLUMN_RUSSIAN, russian)
            contentValues.put(COLUMN_DIFFICULTY, difficulty)
            contentValues.put(COLUMN_IS_LEARNED, 0) // По умолчанию слово не изучено
            db.insert(TABLE_WORDS, null, contentValues)
        }
    }


}
