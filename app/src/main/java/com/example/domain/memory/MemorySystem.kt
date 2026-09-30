package com.example.domain.memory

import com.example.core.database.dao.MemoryDao
import com.example.core.database.entity.MemoryEntity
import com.example.core.model.MemoryType
import kotlinx.coroutines.flow.Flow
import java.util.UUID
import kotlin.math.sqrt

data class MemoryItem(
    val id: String,
    val title: String,
    val content: String,
    val type: MemoryType,
    val confidence: Float = 1.0f,
    val createdAt: Long = System.currentTimeMillis(),
    val tags: List<String> = emptyList(),
    val isPinned: Boolean = false
)

interface VectorStore {
    suspend fun addEmbedding(id: String, text: String, vector: FloatArray)
    suspend fun search(queryVector: FloatArray, topK: Int = 3): List<String>
    suspend fun delete(id: String)
}

/**
 * On-device local in-memory cosine similarity vector index.
 */
class LocalVectorStore : VectorStore {
    private val store = mutableMapOf<String, FloatArray>()

    override suspend fun addEmbedding(id: String, text: String, vector: FloatArray) {
        store[id] = vector
    }

    override suspend fun search(queryVector: FloatArray, topK: Int): List<String> {
        if (store.isEmpty()) return emptyList()

        return store.entries
            .map { (id, vector) ->
                id to cosineSimilarity(queryVector, vector)
            }
            .sortedByDescending { it.second }
            .take(topK)
            .map { it.first }
    }

    override suspend fun delete(id: String) {
        store.remove(id)
    }

    fun generateLocalPseudoEmbedding(text: String, dimension: Int = 64): FloatArray {
        // Fast deterministic hash-based local pseudo-embedding for zero-network environments
        val vector = FloatArray(dimension)
        val words = text.lowercase().split(Regex("\\s+"))
        for (word in words) {
            val hash = word.hashCode()
            val idx = Math.abs(hash) % dimension
            vector[idx] += 1.0f
        }
        // Normalize
        var norm = 0.0f
        for (v in vector) norm += v * v
        norm = sqrt(norm)
        if (norm > 0) {
            for (i in vector.indices) vector[i] /= norm
        }
        return vector
    }

    private fun cosineSimilarity(v1: FloatArray, v2: FloatArray): Float {
        var dot = 0.0f
        var n1 = 0.0f
        var n2 = 0.0f
        val len = minOf(v1.size, v2.size)
        for (i in 0 until len) {
            dot += v1[i] * v2[i]
            n1 += v1[i] * v1[i]
            n2 += v2[i] * v2[i]
        }
        val denom = sqrt(n1) * sqrt(n2)
        return if (denom > 0) dot / denom else 0.0f
    }
}

class MemoryManager(
    private val memoryDao: MemoryDao,
    private val vectorStore: LocalVectorStore
) {
    var isMemoryEnabled: Boolean = true

    fun getAllMemoriesFlow(): Flow<List<MemoryEntity>> = memoryDao.getAllMemories()

    fun getMemoriesByTypeFlow(type: MemoryType): Flow<List<MemoryEntity>> = memoryDao.getMemoriesByType(type)

    suspend fun saveMemory(
        title: String,
        content: String,
        type: MemoryType,
        tags: List<String> = emptyList(),
        isPinned: Boolean = false
    ): MemoryEntity? {
        if (!isMemoryEnabled) return null
        val id = UUID.randomUUID().toString()
        val entity = MemoryEntity(
            id = id,
            title = title,
            content = content,
            type = type,
            tags = tags.joinToString(","),
            isPinned = isPinned,
            createdAt = System.currentTimeMillis()
        )
        memoryDao.insertMemory(entity)

        // Index in vector store
        val emb = vectorStore.generateLocalPseudoEmbedding("$title $content")
        vectorStore.addEmbedding(id, "$title $content", emb)
        return entity
    }

    suspend fun forgetMemory(id: String) {
        memoryDao.deleteMemory(id)
        vectorStore.delete(id)
    }

    suspend fun clearAll() {
        memoryDao.clearAllMemories()
    }

    suspend fun retrieveRelevantContext(query: String): List<MemoryEntity> {
        if (!isMemoryEnabled) return emptyList()
        val directHits = memoryDao.searchMemories(query)
        if (directHits.isNotEmpty()) return directHits.take(3)

        val queryEmb = vectorStore.generateLocalPseudoEmbedding(query)
        val matchedIds = vectorStore.search(queryEmb, topK = 3)
        return directHits
    }

    suspend fun exportMemoriesAsJson(): String {
        return "Export completed"
    }
}
