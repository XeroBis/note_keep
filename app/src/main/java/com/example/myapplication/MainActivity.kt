package com.example.myapplication


import android.app.AlertDialog
import android.content.Context
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

    private var noteList = mutableListOf<Note>()

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
                deleteNoteAtPosition(position)
                true
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

        alertDialogBuilder.setPositiveButton("OK") { dialog, which ->
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

    private fun deleteNoteAtPosition(position: Int) {
        val note = noteList[position]
        val alertDialogBuilder = AlertDialog.Builder(this)
        alertDialogBuilder.setTitle("Delete Note")
        alertDialogBuilder.setMessage("Are you sure you want to delete the note \"${note.title}\"?")
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
}
