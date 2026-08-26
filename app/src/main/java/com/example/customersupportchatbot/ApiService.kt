package com.example.customersupportchatbot

import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL

object ApiService {

    // Python Flask backend running on your computer
    private const val BASE_URL = "http://10.164.187.189:5000"

    fun sendMessage(message: String): Result<BotResponse> {

        return try {

            val url = URL("$BASE_URL/chat")

            val connection =
                url.openConnection() as HttpURLConnection

            connection.requestMethod = "POST"
            connection.connectTimeout = 5000
            connection.readTimeout = 10000
            connection.doOutput = true

            connection.setRequestProperty(
                "Content-Type",
                "application/json"
            )

            val jsonBody = JSONObject()
                .put("message", message)
                .toString()

            OutputStreamWriter(
                connection.outputStream
            ).use {
                it.write(jsonBody)
            }

            val responseCode = connection.responseCode

            val inputStream =
                if (responseCode in 200..299) {
                    connection.inputStream
                } else {
                    connection.errorStream
                }

            val responseText =
                BufferedReader(
                    InputStreamReader(inputStream)
                ).use {
                    it.readText()
                }

            connection.disconnect()

            if (responseCode !in 200..299) {

                Result.failure(
                    Exception(
                        "Server returned HTTP $responseCode"
                    )
                )

            } else {

                val json = JSONObject(responseText)

                val botResponse = BotResponse(
                    intent = json.optString(
                        "intent",
                        "fallback"
                    ),
                    response = json.optString(
                        "response",
                        "Sorry, I could not understand that."
                    ),
                    confidence = json.optDouble(
                        "confidence",
                        0.0
                    )
                )

                Result.success(botResponse)
            }

        } catch (e: Exception) {

            Result.failure(e)
        }
    }
}

data class BotResponse(
    val intent: String,
    val response: String,
    val confidence: Double
)