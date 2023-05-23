package com.example.myapplication

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity

class NoteDetailActivity : AppCompatActivity() {

    private lateinit var titleEditText: EditText
    private lateinit var contentEditText: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_note_detail)

        titleEditText = findViewById(R.id.note_title_textview)
        contentEditText = findViewById(R.id.note_content_textview)

        // Retrieve the Note object from the intent
        val note = if (Build.VERSION.SDK_INT >= 33) {
            intent.getParcelableExtra("note", Note::class.java)!!
        } else {
            intent.getParcelableExtra<Note>("note")!!
        }
        // Populate the views with the data from the Note object
        note.let {
            titleEditText.setText(it.title)
            contentEditText.setText(it.content)
        }
        val saveButton = findViewById<Button>(R.id.save_note_button)
        saveButton.setOnClickListener {

            saveNote()
        }
    }
    private fun saveNote() {
        val title = titleEditText.text.toString()
        val content = contentEditText.text.toString()

        val note = Note(title, content)
        //val intent = Intent(this, MainActivity::class.java).apply {
        //    putExtra("note", note)
        //}
        //Toast.makeText(this, "Save button clicked${note.content}", Toast.LENGTH_SHORT).show()
        val intent = Intent()
        intent.putExtra("note", note)

        setResult(RESULT_OK, intent)
        finish()
    }
}