package com.example.myapplication


import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.Rect
import android.os.Bundle
import android.os.Parcel
import android.os.Parcelable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

// alan
// Define the Note data class
data class Note(val title: String?, val content: String? ) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString(),
        parcel.readString()
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(title)
        parcel.writeString(content)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<Note> {
        override fun createFromParcel(parcel: Parcel): Note {
            return Note(parcel)
        }

        override fun newArray(size: Int): Array<Note?> {
            return arrayOfNulls(size)
        }
    }
}


// MainActivity.kt
class MainActivity : AppCompatActivity() {
    private lateinit var noteRecyclerView: RecyclerView
    private lateinit var addNoteButton: Button
    private lateinit var deleteButton: Button

    private var noteList = mutableListOf<Note>()
    private val selectedNotes = mutableSetOf<Int>()
    private lateinit var adapter : NoteAdapter

    private val REQUEST_CODE_NOTE_DETAIL = 1

    private val startForResult = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            result : ActivityResult ->
        if (result.resultCode == RESULT_OK) {
            Toast.makeText(this@MainActivity, "Note: notes", Toast.LENGTH_SHORT).show()
            val note = result.data?.getParcelableExtra<Note>("note")
            if (note != null) {
                // Use the returned note object here
                noteList.add(note)
                saveNoteListToSharedPreferences(noteList)
                val adapter = noteRecyclerView.adapter as NoteAdapter
                adapter.notifyItemInserted(noteList.size - 1)
                Toast.makeText(this@MainActivity, "Note: ${note.title}, ${note.content}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        noteRecyclerView = findViewById(R.id.note_recycler_view)
        addNoteButton = findViewById(R.id.add_note_button)
        deleteButton = findViewById(R.id.delete_button)

        // Load the previously saved notes
        noteList = getNoteListFromSharedPreferences()

        // Set up the RecyclerView
        adapter = NoteAdapter(noteList) { note ->
            openNoteDetailActivity(note)
        }
        noteRecyclerView.layoutManager = LinearLayoutManager(this)
        noteRecyclerView.adapter = adapter
        // Add a new note when the add note button is clicked
        addNoteButton.setOnClickListener {
            //addNewNote()
            val note = Note("Title", "Sample Content")
            openNoteDetailActivity(note)
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
    private fun openNoteDetailActivity(note: Note) {
        startForResult.launch(Intent(this, NoteDetailActivity::class.java).apply {
            putExtra("note", note)
        })
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

    inner class NoteAdapter(private val noteList: List<Note>, private val onItemClick: (Note) -> Unit) :
        RecyclerView.Adapter<NoteAdapter.NoteViewHolder>() {

        private lateinit var adapter: NoteAdapter

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NoteViewHolder {
            val itemView = LayoutInflater.from(parent.context)
                .inflate(R.layout.note_item, parent, false)
            return NoteViewHolder(itemView)
        }

        override fun onBindViewHolder(holder: NoteViewHolder, position: Int) {
            val note = noteList[position]
            holder.bind(note)
            holder.titleTextView.text = note.title
            holder.contentTextView.text = note.content

            holder.itemView.setOnLongClickListener {
                // Delete the note when it's long pressed
                toggleNoteSelection(holder, position)
                true
            }

        }

        private fun toggleNoteSelection(holder: NoteViewHolder, position: Int) {
            if (selectedNotes.contains(position)) {
                selectedNotes.remove(position)
                // Deselect the note R.id.note_recycler_view

                holder.itemView.setBackgroundColor(Color.TRANSPARENT);
            } else {
                selectedNotes.add(position)
                // Select the note
                holder.itemView.setBackgroundColor(Color.LTGRAY)
            }
        }

        override fun getItemCount(): Int {
            return noteList.size
        }

        inner class NoteViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            public val titleTextView: TextView = itemView.findViewById(R.id.title_text_view)
            public val contentTextView: TextView = itemView.findViewById(R.id.content_text_view)

            init {
                itemView.setOnClickListener {
                    val position = adapterPosition
                    if (position != RecyclerView.NO_POSITION) {
                        val note = noteList[position]
                        onItemClick(note)
                    }
                }
            }

            fun bind(note: Note) {
                titleTextView.text = note.title
                contentTextView.text = note.content
            }
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
}
