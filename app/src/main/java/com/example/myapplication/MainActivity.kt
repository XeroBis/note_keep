package com.example.myapplication

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast

class MainActivity : AppCompatActivity() {
    private val KEY_NOTE = "note"

    private lateinit var noteEditText: EditText
    private lateinit var saveButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        noteEditText = findViewById(R.id.note_edit_text)
        saveButton = findViewById(R.id.save_button)

        // Load the previously saved note
        val savedNote = getNoteFromSharedPreferences()
        noteEditText.setText(savedNote)

        // Save the note when the save button is clicked
        saveButton.setOnClickListener {
            val note = noteEditText.text.toString()
            saveNoteToSharedPreferences(note)
            Toast.makeText(this, "Note saved", Toast.LENGTH_SHORT).show()
        }
    }

    private fun getNoteFromSharedPreferences(): String {
        val sharedPreferences = getSharedPreferences("MyNoteApp", Context.MODE_PRIVATE)
        return sharedPreferences.getString(KEY_NOTE, "") ?: ""
    }

    private fun saveNoteToSharedPreferences(note: String) {
        val sharedPreferences = getSharedPreferences("MyNoteApp", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putString(KEY_NOTE, note)
        editor.apply()
    }
}
