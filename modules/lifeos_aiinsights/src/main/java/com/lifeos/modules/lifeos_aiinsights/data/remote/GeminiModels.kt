package com.lifeos.modules.lifeos_aiinsights.data.remote

import kotlinx.serialization.Serializable

// Groq uses the OpenAI-compatible chat completions API

@Serializable
data class GroqRequest(
    val model: String,
    val messages: List<GroqMessage>
)

@Serializable
data class GroqMessage(
    val role: String,
    val content: String
)

@Serializable
data class GroqResponse(
    val choices: List<GroqChoice> = emptyList()
)

@Serializable
data class GroqChoice(
    val message: GroqMessage
)
