package com.example.englishtrain.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.englishtrain.GameRecord
import com.example.englishtrain.R
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class GameHistoryAdapter(
    private val context: Context,
    private val gameHistory: List<GameRecord>,
    private val onItemClick: (GameRecord) -> Unit // Интерфейс для обработки кликов
) : RecyclerView.Adapter<GameHistoryAdapter.GameHistoryViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GameHistoryViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.item_game_record, parent, false)
        return GameHistoryViewHolder(view)
    }

    override fun onBindViewHolder(holder: GameHistoryViewHolder, position: Int) {
        val gameRecord = gameHistory[position]
        holder.bind(gameRecord)
    }

    override fun getItemCount(): Int {
        return gameHistory.size
    }

    inner class GameHistoryViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val dateTextView: TextView = itemView.findViewById(R.id.tv_game_record_date)
        private val correctAnswersTextView: TextView = itemView.findViewById(R.id.tv_game_record_correct_answers)
        private val difficultyTextView: TextView = itemView.findViewById(R.id.tv_game_record_difficulty)

        fun bind(gameRecord: GameRecord) {
            val dateFormat = SimpleDateFormat("dd.MM.yyyy HH:mm:ss", Locale.getDefault())
            val date = Date(gameRecord.date.toLong())
            dateTextView.text = dateFormat.format(date)
            correctAnswersTextView.text = "Правильных ответов: ${gameRecord.correctAnswers} из ${gameRecord.totalQuestions}"
            difficultyTextView.text = "Сложность: ${gameRecord.difficulty}"

            itemView.setOnClickListener {
                onItemClick(gameRecord)
            }
        }
    }
}
