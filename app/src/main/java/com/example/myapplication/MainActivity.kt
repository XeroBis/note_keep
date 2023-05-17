package com.example.myapplication


import android.app.AlertDialog
import android.content.Context
import android.graphics.Color
import android.graphics.Rect
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

// alan
// Define the Note data class
data class Note(val title: String, val content: String)


// MainActivity.kt
class MainActivity : AppCompatActivity() {
    private lateinit var noteRecyclerView: RecyclerView
    private lateinit var addNoteButton: Button
    private lateinit var deleteButton: Button

    private var noteList = mutableListOf<Note>()
    private val selectedNotes = mutableSetOf<Int>()
    private lateinit var adapter : NoteAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        noteRecyclerView = findViewById(R.id.note_recycler_view)
        addNoteButton = findViewById(R.id.add_note_button)
        deleteButton = findViewById(R.id.delete_button)

        // Load the previously saved notes
        noteList = getNoteListFromSharedPreferences()

        // Set up the RecyclerView
        adapter = NoteAdapter(noteList)
        noteRecyclerView.adapter = adapter
        noteRecyclerView.layoutManager = LinearLayoutManager(this)

        // Add a new note when the add note button is clicked
        addNoteButton.setOnClickListener {
            addNewNote()
        }

        deleteButton.setOnClickListener {
            deleteSelectedNotes()
        }

        // Add space between notes
        val verticalSpaceBetweenItems = resources.getDimensionPixelSize(R.dimen.vertical_space_between_items)
        val horizontalSpaceBetweenItems = resources.getDimensionPixelSize(R.dimen.horizontal_space_between_items)
        noteRecyclerView.addItemDecoration(
            SpaceItemDecoration(verticalSpaceBetweenItems, horizontalSpaceBetweenItems)
        )
    }

    private fun addNewNote() {
        if (noteList.isNotEmpty()){
            val lastnote = noteList.last()
            if (lastnote.title != "New Title" && lastnote.content != "New Content") {
                val note = Note("New Title", "New Content")
                noteList.add(note)
                saveNoteListToSharedPreferences(noteList)
                val adapter = noteRecyclerView.adapter as NoteAdapter
                adapter.notifyItemInserted(noteList.size - 1)
            }
        } else {
            val note = Note("New Title", "New Content")
            noteList.add(note)
            saveNoteListToSharedPreferences(noteList)
            val adapter = noteRecyclerView.adapter as NoteAdapter
            adapter.notifyItemInserted(noteList.size - 1)
        }
    }
    private fun deleteSelectedNotes() {
        val sortedPositions = selectedNotes.toList().sortedDescending()
        for (position in sortedPositions) {
            noteList.removeAt(position)
            adapter.notifyItemRemoved(position)
        }
        selectedNotes.clear()
        saveNoteListToSharedPreferences(noteList)
        Toast.makeText(this@MainActivity, "Notes deleted", Toast.LENGTH_SHORT).show()
    }


    private fun getNoteListFromSharedPreferences(): MutableList<Note> {
        val sharedPreferences = getSharedPreferences("MyNoteApp", Context.MODE_PRIVATE)
        val json = sharedPreferences.getString("noteList", "")
        return if (json != "") {
            Gson().fromJson(json, object : TypeToken<MutableList<Note>>() {}.type)
        } else {
            mutableListOf()
        }
    }

    private fun saveNoteListToSharedPreferences(noteList: MutableList<Note>) {
        val sharedPreferences = getSharedPreferences("MyNoteApp", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        val jsonArray = Gson().toJsonTree(noteList).asJsonArray
        editor.putString("noteList", jsonArray.toString())
        editor.apply()
    }


    inner class NoteAdapter(private val noteList: MutableList<Note>) :
        RecyclerView.Adapter<NoteAdapter.NoteViewHolder>() {

        private lateinit var adapter: NoteAdapter

        inner class NoteViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            val titleTextView: TextView = itemView.findViewById(R.id.title_text_view)
            val contentTextView: TextView = itemView.findViewById(R.id.content_text_view)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NoteViewHolder {
            val itemView = LayoutInflater.from(parent.context)
                .inflate(R.layout.note_item, parent, false)
            return NoteViewHolder(itemView)
        }

        override fun onBindViewHolder(holder: NoteViewHolder, position: Int) {
            val note = noteList[position]
            holder.titleTextView.text = note.title
            holder.contentTextView.text = note.content

            holder.itemView.setOnClickListener {
                editNoteAtPosition(position)
            }

            holder.itemView.setOnLongClickListener {
                // Delete the note when it's long pressed
                toggleNoteSelection(holder, position)
                true
            }

            if (selectedNotes.contains(position)) {
                holder.itemView.setBackgroundColor(Color.LTGRAY)
            } else {
                holder.itemView.setBackgroundColor(Color.WHITE)
            }

        }

        private fun toggleNoteSelection(holder: NoteViewHolder, position: Int) {
            if (selectedNotes.contains(position)) {
                selectedNotes.remove(position)
                // Deselect the note
                holder.itemView.setBackgroundColor(Color.WHITE)
            } else {
                selectedNotes.add(position)
                // Select the note
                holder.itemView.setBackgroundColor(Color.LTGRAY)
            }
        }

        override fun getItemCount(): Int {
            return noteList.size
        }
    }

    class SpaceItemDecoration(
        private val verticalSpace: Int,
        private val horizontalSpace: Int
    ) : RecyclerView.ItemDecoration() {

        override fun getItemOffsets(
            outRect: Rect,
            view: View,
            parent: RecyclerView,
            state: RecyclerView.State
        ) {
            outRect.top = verticalSpace
            outRect.left = horizontalSpace
            outRect.right = horizontalSpace
        }
    }

    private fun editNoteAtPosition(position: Int) {
        val note = noteList[position]
        val alertDialogBuilder = AlertDialog.Builder(this)
        alertDialogBuilder.setTitle("Edit Note")

        // Inflate the custom dialog view
        val dialogView = LayoutInflater.from(this).inflate(R.layout.edit_note_dialog, null)

        // Get references to the EditText views
        val titleEditText = dialogView.findViewById<EditText>(R.id.title_edit_text)
        val contentEditText = dialogView.findViewById<EditText>(R.id.content_edit_text)

        // Set the text of the EditText views to the values of the note being edited
        titleEditText.setText(note.title)
        contentEditText.setText(note.content)

        alertDialogBuilder.setView(dialogView)

        alertDialogBuilder.setPositiveButton("OK") { _, _ ->
            val editedTitle = titleEditText.text.toString()
            val editedContent = contentEditText.text.toString()
            noteList[position] = Note(editedTitle, editedContent)
            saveNoteListToSharedPreferences(noteList)
            //noteRecyclerView.adapter?.notifyDataSetChanged()
            noteRecyclerView.adapter?.notifyItemChanged(position)
            Toast.makeText(this, "Note updated", Toast.LENGTH_SHORT).show()
        }
        alertDialogBuilder.setNegativeButton("Cancel") { dialog, _ ->
            dialog.cancel()
        }
        alertDialogBuilder.show()
    }

}
