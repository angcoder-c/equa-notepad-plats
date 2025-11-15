package com.example.equa_notepad_plats.network

import com.example.equa_notepad_plats.BuildConfig
import io.ktor.client.*
import io.ktor.client.engine.okhttp.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import org.json.JSONObject

object OpenRouterClient {

    private const val OPENROUTER_URL = "https://openrouter.ai/api/v1/chat/completions"
    private const val MODEL = "openai/gpt-4o-mini"

    private val client = HttpClient(OkHttp)

    suspend fun ask(prompt: String, maxtokens: Int = 200): String {
        val bodyJson = """
            {
              "model": "$MODEL",
              "messages": [
                {"role": "system", "content": "Eres un generador de ejercicios de matemáticas."},
                {"role": "user", "content": "$prompt"}
              ],
              "max_tokens": ${maxtokens}, 
              "temperature": 0.7,
              "top_p": 1.0
            }
        """.trimIndent()

        val response = client.post(OPENROUTER_URL) {
            header("Authorization", "Bearer ${BuildConfig.OPEN_ROUTER_KEY}")
            header("Content-Type", "application/json")
            setBody(bodyJson)
        }

        val responseText = response.bodyAsText()

        val json = JSONObject(responseText)

        return json
            .getJSONArray("choices")
            .getJSONObject(0)
            .getJSONObject("message")
            .getString("content")
    }
}
