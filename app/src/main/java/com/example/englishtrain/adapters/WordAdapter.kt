package com.example.englishtrain.adapters

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.englishtrain.R
import com.example.englishtrain.Word

class WordAdapter(
    private var words: List<Word>,
    private val isDeleteMode: Boolean,
    private val onDeleteClick: (Int) -> Unit,
    private val onPlayClick: (String, String) -> Unit,
    private val onLearnedStatusChanged: (Int, Boolean) -> Unit,
    private val onEditClick: (Word) -> Unit  // Новый коллбэк для редактирования
) : RecyclerView.Adapter<WordAdapter.WordViewHolder>() {

    class WordViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvWord: TextView = itemView.findViewById(R.id.tv_english)
        val tvTranslation: TextView = itemView.findViewById(R.id.tv_russian)
        val actionIcon: ImageView = itemView.findViewById(R.id.iv_delete)
        val checkBoxLearned: CheckBox = itemView.findViewById(R.id.checkbox_learned)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WordViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_word, parent, false)
        return WordViewHolder(view)
    }

    override fun onBindViewHolder(holder: WordViewHolder, position: Int) {
        val word = words[position]
        holder.tvWord.text = word.english
        holder.tvTranslation.text = word.russian
        holder.checkBoxLearned.isChecked = word.isLearned

        when (word.difficulty) {
            1 -> holder.tvWord.setTextColor(Color.parseColor("#006400"))
            2 -> holder.tvWord.setTextColor(Color.parseColor("#8B008B"))
            3 -> holder.tvWord.setTextColor(Color.parseColor("#8B0000"))
        }

        if (isDeleteMode) {
            holder.actionIcon.setImageResource(R.drawable.ic_delete)
            holder.actionIcon.setOnClickListener { onDeleteClick(word.id) }
        } else {
            holder.actionIcon.setImageResource(R.drawable.ic_speaker)
            holder.actionIcon.setOnClickListener { onPlayClick(word.english, word.russian) }
        }

        holder.checkBoxLearned.setOnCheckedChangeListener { _, isChecked ->
            onLearnedStatusChanged(word.id, isChecked)
        }

        // Обработка долгого нажатия для вызова редактирования
        holder.itemView.setOnLongClickListener {
            onEditClick(word)  // Вызываем коллбэк редактирования
            true
        }
    }

    override fun getItemCount(): Int = words.size

    fun updateWords(newWords: List<Word>) {
        words = newWords
        notifyDataSetChanged()
    }
}
