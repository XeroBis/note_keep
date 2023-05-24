package com.example.myapplication

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.internal.ContextUtils.getActivity


class NoteDetailActivity : AppCompatActivity() {

    private lateinit var titleEditText: EditText
    private lateinit var contentEditText: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_note_detail)
        titleEditText = findViewById(R.id.note_title_textview)
        contentEditText = findViewById(R.id.note_content_textview)

        supportActionBar?.hide()

        // Retrieve the Note object from the intent
        val note = if (Build.VERSION.SDK_INT >= 33) {
            intent.getBundleExtra("extra")?.getParcelable("note",  Note::class.java)!!
        } else {
            intent.getBundleExtra("extra")?.getParcelable("note")!!
        }
        // Retrieve the Note object from the intent
        val position = if (Build.VERSION.SDK_INT >= 33) {
            intent.getBundleExtra("extra")?.getParcelable("id",  Position::class.java)!!
        } else {
            intent.getBundleExtra("extra")?.getParcelable("id")!!
        }

        // Populate the views with the data from the Note object
        note.let {
            titleEditText.setText(it.title)
            contentEditText.setText(it.content)
        }
        val backButton = findViewById<ImageButton>(R.id.back_button)
        backButton.setOnClickListener{
            saveNote(position)
        }

        val saveButton = findViewById<Button>(R.id.save_note_button)
        saveButton.setOnClickListener {
            saveNote(position)
        }
    }
    private fun saveNote(position:Position) {
        val title = titleEditText.text.toString()
        val content = contentEditText.text.toString()

        val note = Note(title, content)
        //val intent = Intent(this, MainActivity::class.java).apply {
        //    putExtra("note", note)
        //}
        //Toast.makeText(this, "Save button clicked${note.content}", Toast.LENGTH_SHORT).show()
        val intent = Intent()
        val extras = Bundle()
        extras.putParcelable("note", note)
        extras.putParcelable("id", position)
        intent.putExtra("extra", extras)

        setResult(RESULT_OK, intent)
        finish()
    }

}