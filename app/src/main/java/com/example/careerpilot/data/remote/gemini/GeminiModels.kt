package com.example.careerpilot.data.remote.gemini

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject

@Serializable
data class GeminiGenerateRequest(
    val contents: List<GeminiContent>,
    val tools: List<GeminiTool>? = null,
    val generationConfig: GeminiGenerationConfig? = null,
    val systemInstruction: GeminiContent? = null
)

@Serializable
data class GeminiContent(
    val role: String? = null,
    val parts: List<GeminiPart>
)

@Serializable
data class GeminiPart(
    val text: String? = null
)

@Serializable
data class GeminiTool(
    @SerialName("googleSearch")
    val googleSearch: JsonObject? = null,
    @SerialName("google_search")
    val google_search: JsonObject? = null
)

@Serializable
data class GeminiGenerationConfig(
    val temperature: Float? = 0.4f,
    val topP: Float? = 0.95f,
    val topK: Int? = 40,
    val maxOutputTokens: Int? = 2048
)

@Serializable
data class GeminiGenerateResponse(
    val candidates: List<GeminiCandidate>? = null,
    val promptFeedback: JsonObject? = null
)

@Serializable
data class GeminiCandidate(
    val content: GeminiContent? = null,
    val finishReason: String? = null,
    val groundingMetadata: GroundingMetadata? = null
)

@Serializable
data class GroundingMetadata(
    val webSearchQueries: List<String>? = null,
    val searchEntryPoint: SearchEntryPoint? = null,
    val groundingChunks: List<GroundingChunk>? = null,
    val groundingSupports: List<GroundingSupport>? = null
)

@Serializable
data class SearchEntryPoint(
    val renderedContent: String? = null
)

@Serializable
data class GroundingChunk(
    val web: WebChunk? = null
)

@Serializable
data class WebChunk(
    val uri: String? = null,
    val title: String? = null
)

@Serializable
data class GroundingSupport(
    val segment: TextSegment? = null,
    val groundingChunkIndices: List<Int>? = null,
    val confidenceScores: List<Float>? = null
)

@Serializable
data class TextSegment(
    val startIndex: Int? = null,
    val endIndex: Int? = null,
    val text: String? = null
)
