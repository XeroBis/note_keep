package com.example.myapplication

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

// MainActivity.kt
class MainActivity : AppCompatActivity() {
    private lateinit var noteRecyclerView: RecyclerView
    private lateinit var addNoteButton: Button

    private var noteList = mutableListOf<String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        noteRecyclerView = findViewById(R.id.note_recycler_view)
        addNoteButton = findViewById(R.id.add_note_button)

        // Load the previously saved notes
        noteList = getNoteListFromSharedPreferences()

        // Set up the RecyclerView
        val adapter = NoteAdapter(noteList)
        noteRecyclerView.adapter = adapter
        noteRecyclerView.layoutManager = LinearLayoutManager(this)

        // Add a new note when the add note button is clicked
        addNoteButton.setOnClickListener {
            addNewNote()
        }
    }

    private fun addNewNote() {
        val note = "New note"
        noteList.add(note)
        saveNoteListToSharedPreferences(noteList)
        val adapter = noteRecyclerView.adapter as NoteAdapter
        adapter.notifyItemInserted(noteList.size - 1)
    }

    private fun getNoteListFromSharedPreferences(): MutableList<String> {
        val sharedPreferences = getSharedPreferences("MyNoteApp", Context.MODE_PRIVATE)
        val json = sharedPreferences.getString("noteList", "")
        return if (json != "") {
            Gson().fromJson(json, object : TypeToken<MutableList<String>>() {}.type)
        } else {
            mutableListOf()
        }
    }

    private fun saveNoteListToSharedPreferences(noteList: MutableList<String>) {
        val sharedPreferences = getSharedPreferences("MyNoteApp", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        val json = Gson().toJson(noteList)
        editor.putString("noteList", json)
        editor.apply()
    }

    inner class NoteAdapter(private val noteList: MutableList<String>) :
        RecyclerView.Adapter<NoteAdapter.NoteViewHolder>() {

        inner class NoteViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            val noteTextView: TextView = itemView.findViewById(R.id.note_text_view)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NoteViewHolder {
            val itemView = LayoutInflater.from(parent.context)
                .inflate(R.layout.note_item, parent, false)
            return NoteViewHolder(itemView)
        }

        override fun onBindViewHolder(holder: NoteViewHolder, position: Int) {
            val note = noteList[position]
            holder.noteTextView.text = note
            holder.itemView.setOnClickListener {
                // Edit the note when it's clicked
                editNoteAtPosition(position)
            }
            holder.itemView.setOnLongClickListener {
                // Delete the note when it's long pressed
                deleteNoteAtPosition(position)
                true
            }
        }

        override fun getItemCount(): Int {
            return noteList.size
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun editNoteAtPosition(position: Int) {
        val note = noteList[position]
        val alertDialogBuilder = AlertDialog.Builder(this)
        alertDialogBuilder.setTitle("Edit Note")
        val inputEditText = EditText(this)
        inputEditText.setText(note)
        alertDialogBuilder.setView(inputEditText)
        alertDialogBuilder.setPositiveButton("OK") { _, _ ->
            val editedNote = inputEditText.text.toString()
            noteList[position] = editedNote
            saveNoteListToSharedPreferences(noteList)
            noteRecyclerView.adapter?.notifyDataSetChanged()
            Toast.makeText(this, "Note updated", Toast.LENGTH_SHORT).show()
        }
        alertDialogBuilder.setNegativeButton("Cancel") { dialog, _ ->
            dialog.cancel()
        }
        alertDialogBuilder.show()
    }

    private fun deleteNoteAtPosition(position: Int) {
        val note = noteList[position]
        val alertDialogBuilder = AlertDialog.Builder(this)
        alertDialogBuilder.setTitle("Delete Note")
        alertDialogBuilder.setMessage("Are you sure you want to delete the note \"$note\"?")
        alertDialogBuilder.setPositiveButton("Yes") { _, _ ->
            noteList.removeAt(position)
            saveNoteListToSharedPreferences(noteList)
            noteRecyclerView.adapter?.notifyItemRemoved(position)
            Toast.makeText(this, "Note deleted", Toast.LENGTH_SHORT).show()
        }
        alertDialogBuilder.setNegativeButton("No") { dialog, _ ->
            dialog.cancel()
        }
        alertDialogBuilder.show()
    }

    fun editNote(view: View) {
        val position = noteRecyclerView.getChildLayoutPosition(view.parent as View)
        editNoteAtPosition(position)
    }

    fun deleteNote(view: View) {
        val position = noteRecyclerView.getChildLayoutPosition(view.parent as View)
        deleteNoteAtPosition(position)
    }


}
