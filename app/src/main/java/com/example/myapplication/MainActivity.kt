package com.example.myapplication


import android.content.Context
import android.content.Intent
import android.graphics.Rect
import android.os.Build
import android.os.Bundle
import android.os.Parcel
import android.os.Parcelable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.CheckBox
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken


data class Position(val id:Int) : Parcelable {
    constructor(parcel : Parcel) : this(
        parcel.readInt()
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeInt(id)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<Position> {
        override fun createFromParcel(parcel: Parcel): Position {
            return Position(parcel)
        }

        override fun newArray(size: Int): Array<Position?> {
            return arrayOfNulls(size)
        }
    }
}
// Define the Note data class
data class Note(val title: String?, val content: String?, var selected:Boolean = false) : Parcelable {
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


    private val startForResult = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            result : ActivityResult ->
        if (result.resultCode == RESULT_OK) {
            val note = if (Build.VERSION.SDK_INT >= 33) {
                result.data?.getBundleExtra("extra")?.getParcelable("note", Note::class.java)!!

            } else {
                result.data?.getBundleExtra("extra")?.getParcelable("note")!!
            }

            val id = if (Build.VERSION.SDK_INT >= 33) {
                result.data?.getBundleExtra("extra")?.getParcelable("id", Position::class.java)!!
            } else {
                result.data?.getBundleExtra("extra")?.getParcelable("id")!!
            }
            // Use the returned note object here
            noteList[id.id] = note
            saveNoteListToSharedPreferences(noteList)
            val adapter = noteRecyclerView.adapter as NoteAdapter
            adapter.notifyItemChanged(id.id)
            Toast.makeText(this@MainActivity, "Note: ${note.title}, ${note.content}", Toast.LENGTH_SHORT).show()
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
            addNoteDetailActivity(note)
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


    private fun addNoteDetailActivity(note: Note){
        if (noteList.isNotEmpty()) {
            val lastnote = noteList.last()
            if (lastnote.title !="Title" && lastnote.content != "Sample Content") {
                startForResult.launch(Intent(this, NoteDetailActivity::class.java).apply {
                    noteList.add(note)
                    val position = noteList.indexOf(note)
                    val extras = Bundle()
                    extras.putParcelable("note", note)
                    extras.putParcelable("id", Position(position))
                    putExtra("extra", extras)
                })
            } else {
                Toast.makeText(this@MainActivity, "Dernières Notes non modifiés", Toast.LENGTH_SHORT).show()
            }
        } else {
            startForResult.launch(Intent(this, NoteDetailActivity::class.java).apply {
                noteList.add(note)
                val position = noteList.indexOf(note)
                val extras = Bundle()
                extras.putParcelable("note", note)
                extras.putParcelable("id", Position(position))
                putExtra("extra", extras)
            })
        }

    }
    private fun openNoteDetailActivity(note: Note) {
        startForResult.launch(Intent(this, NoteDetailActivity::class.java).apply {
            val position = noteList.indexOf(note)
            val extras = Bundle()
            extras.putParcelable("note", note)
            extras.putParcelable("id", Position(position))
            putExtra("extra", extras)
        })
    }
    private fun deleteSelectedNotes() {
        val selectedPositions = mutableListOf<Int>()
        for ((index, note) in noteList.withIndex()) {
            if (note.selected) {
                selectedPositions.add(index)
            }
        }
        // Remove the selected notes from the list
        for (position in selectedPositions.reversed()) {
            noteList.removeAt(position)
            adapter.notifyItemRemoved(position)
            adapter.notifyItemRangeChanged(position, noteList.size)
        }
        // Clear the selection
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
        }

        override fun getItemCount(): Int {
            return noteList.size
        }

        inner class NoteViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            val titleTextView: TextView = itemView.findViewById(R.id.title_text_view)
            val contentTextView: TextView = itemView.findViewById(R.id.content_text_view)
            val selectedCheckBox: CheckBox = itemView.findViewById(R.id.selected_checkbox)

            init {
                itemView.setOnClickListener {
                    val position = adapterPosition
                    if (position != RecyclerView.NO_POSITION) {
                        val note = noteList[position]
                        onItemClick(note)
                    }
                }
                itemView.setOnLongClickListener {
                    val position = adapterPosition
                    if (position != RecyclerView.NO_POSITION) {
                        val note = noteList[position]
                        note.selected = !note.selected
                        if (note.selected) {
                            selectedNotes.add(position)
                        } else {
                            selectedNotes.remove(position)
                        }
                        selectedCheckBox.isChecked = note.selected
                        notifyItemChanged(position)
                        true
                    } else {
                        false
                    }
                }
            }

            fun bind(note: Note) {
                titleTextView.text = note.title
                contentTextView.text = note.content
                selectedCheckBox.isChecked = note.selected
                selectedCheckBox.visibility = if (note.selected) View.VISIBLE else View.GONE
            }
        }
    }
    class SpaceItemDecoration(private val verticalSpace: Int, private val horizontalSpace: Int) : RecyclerView.ItemDecoration() {
        override fun getItemOffsets(
            outRect: Rect,
            view: View,
            parent: RecyclerView,
            state: RecyclerView.State) {
            outRect.top = verticalSpace
            outRect.left = horizontalSpace
            outRect.right = horizontalSpace
        }
    }
}
