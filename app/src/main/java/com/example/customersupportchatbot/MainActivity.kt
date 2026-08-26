package com.example.customersupportchatbot

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import java.util.concurrent.Executors

class MainActivity : AppCompatActivity() {

    private lateinit var chatRecyclerView: RecyclerView
    private lateinit var messageInput: EditText
    private lateinit var sendButton: Button
    private lateinit var progressBar: ProgressBar

    private val messages = mutableListOf<Message>()

    private lateinit var adapter: ChatAdapter

    private val executor = Executors.newSingleThreadExecutor()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_main)

        // Connect XML components
        chatRecyclerView = findViewById(R.id.chatRecyclerView)
        messageInput = findViewById(R.id.messageInput)
        sendButton = findViewById(R.id.sendButton)
        progressBar = findViewById(R.id.progressBar)

        // Setup RecyclerView
        adapter = ChatAdapter(messages)

        chatRecyclerView.layoutManager =
            LinearLayoutManager(this)

        chatRecyclerView.adapter = adapter

        // Initial chatbot message
        addBotMessage(
            "Hello! 👋\n\n" +
                    "I am your Customer Support Assistant.\n\n" +
                    "You can ask me about:\n" +
                    "• Order status\n" +
                    "• Refunds\n" +
                    "• Payments\n" +
                    "• Delivery\n" +
                    "• Account problems\n" +
                    "• Customer support"
        )

        // Send button
        sendButton.setOnClickListener {
            sendMessage()
        }
    }

    private fun sendMessage() {

        val text = messageInput.text
            .toString()
            .trim()

        // Don't send empty messages
        if (text.isEmpty()) {

            Toast.makeText(
                this,
                "Please enter a message",
                Toast.LENGTH_SHORT
            ).show()

            return
        }

        // Add user message
        addUserMessage(text)

        // Clear input
        messageInput.text.clear()

        // Show loading
        setLoading(true)

        // Send message to Python backend
        executor.execute {

            val result = ApiService.sendMessage(text)

            runOnUiThread {

                setLoading(false)

                result.onSuccess { botResponse ->

                    // Successful response
                    addBotMessage(
                        botResponse.response
                    )

                }.onFailure { error ->

                    // Show the REAL error
                    val errorMessage =
                        error.message ?: "Unknown error"

                    addBotMessage(
                        "Backend connection failed.\n\n" +
                                "Error: $errorMessage"
                    )

                    Toast.makeText(
                        this,
                        "Error: $errorMessage",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }
    }

    private fun addUserMessage(text: String) {

        messages.add(
            Message(
                text = text,
                isUser = true
            )
        )

        adapter.notifyItemInserted(
            messages.lastIndex
        )

        scrollToBottom()
    }

    private fun addBotMessage(text: String) {

        messages.add(
            Message(
                text = text,
                isUser = false
            )
        )

        adapter.notifyItemInserted(
            messages.lastIndex
        )

        scrollToBottom()
    }

    private fun scrollToBottom() {

        if (messages.isNotEmpty()) {

            chatRecyclerView.scrollToPosition(
                messages.lastIndex
            )
        }
    }

    private fun setLoading(loading: Boolean) {

        progressBar.visibility =
            if (loading) {
                View.VISIBLE
            } else {
                View.GONE
            }

        sendButton.isEnabled = !loading
    }

    override fun onDestroy() {

        executor.shutdownNow()

        super.onDestroy()
    }
}