package com.smartkids.launcher.service

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import javax.inject.Inject
import javax.inject.Singleton
import android.util.Log

@Singleton
class JarvisManager @Inject constructor() {


    private val apiKey = "your api key here"

    private val url =
        "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.0-flash:generateContent?key=\$apiKey"

    // Conversation history keeps context between messages
    private val history = mutableListOf<Pair<String, String>>()

    fun sendMessage(userMessage: String): Flow<String> = flow {
        history.add("user" to userMessage)
        try {
            val reply = withContext(Dispatchers.IO) { callApi() }
            history.add("model" to reply)
            emit(reply)
        }  catch (e: Exception) {
            // This creates a bright red error in Logcat with a specific searchable tag
            Log.e("JarvisError", "The API call completely crashed!", e)
            emit("I am having trouble connecting. Please check your internet and try again.")
        }
    }

    fun resetChat() { history.clear() }

    private fun callApi(): String {
        val conn = (URL(url).openConnection() as HttpURLConnection).apply {
            requestMethod = "POST"
            setRequestProperty("Content-Type", "application/json")
            doOutput = true
            connectTimeout = 15000
            readTimeout = 30000
        }
        conn.outputStream.use {
            it.write(buildBody().toByteArray(Charsets.UTF_8))
        }
        val code = conn.responseCode
        if (code != HttpURLConnection.HTTP_OK) {
            val err = conn.errorStream?.bufferedReader()?.readText()
            throw Exception("HTTP $code: $err")
        }
        return parseReply(conn.inputStream.bufferedReader().readText())
    }

    private fun buildBody(): String {
        val root = JSONObject()

        // Jarvis personality
        root.put(
            "systemInstruction",
            JSONObject().put(
                "parts",
                JSONArray().put(JSONObject().put("text", SYSTEM_PROMPT))
            )
        )

        // Full conversation history
        val contents = JSONArray()
        history.forEach { (role, text) ->
            contents.put(
                JSONObject()
                    .put("role", role)
                    .put("parts",
                        JSONArray().put(JSONObject().put("text", text))
                    )
            )
        }
        root.put("contents", contents)

        // Child safety filters
        root.put("safetySettings", JSONArray().apply {
            listOf(
                "HARM_CATEGORY_HARASSMENT",
                "HARM_CATEGORY_HATE_SPEECH",
                "HARM_CATEGORY_SEXUALLY_EXPLICIT",
                "HARM_CATEGORY_DANGEROUS_CONTENT"
            ).forEach { cat ->
                put(
                    JSONObject()
                        .put("category", cat)
                        .put("threshold", "BLOCK_MEDIUM_AND_ABOVE")
                )
            }
        })

        // Short responses for children
        root.put(
            "generationConfig",
            JSONObject()
                .put("temperature", 0.7)
                .put("maxOutputTokens", 200)
        )

        return root.toString()
    }

    private fun parseReply(json: String): String {
        return try {
            JSONObject(json)
                .getJSONArray("candidates")
                .getJSONObject(0)
                .getJSONObject("content")
                .getJSONArray("parts")
                .getJSONObject(0)
                .getString("text")
        } catch (_: Exception) {
            "I received a response but could not read it. Please try again."
        }
    }

    companion object {
        private const val SYSTEM_PROMPT = """
You are Jarvis, a kind AI helper for children aged 5 to 14.
- Speak simply, warmly, and briefly
- Keep replies to 2 or 3 sentences only
- Never discuss violence, adult content, or unsafe topics
- Help with homework, math, and science questions
- Suggest offline activities when screen time is up
- Always be encouraging and positive
- Never ask the child for personal information
        """
    }
}