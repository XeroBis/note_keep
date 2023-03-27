package com.example.myapplication

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val inputText = findViewById<EditText>(R.id.input_text)
        val outputText = findViewById<TextView>(R.id.output_text)
        val button = findViewById<Button>(R.id.button)

        button.setOnClickListener {
            val input = inputText.text.toString()
            outputText.text = "You entered: $input"
        }
    }
}