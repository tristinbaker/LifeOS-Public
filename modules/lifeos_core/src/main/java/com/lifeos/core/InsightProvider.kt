package com.lifeos.core

interface InsightProvider {
    val insightId: String
    val cardTitle: String
    val cardQuestion: String
    suspend fun buildDataContext(): String
}
