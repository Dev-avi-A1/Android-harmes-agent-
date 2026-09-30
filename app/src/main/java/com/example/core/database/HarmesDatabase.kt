package com.example.core.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.core.database.dao.ConversationDao
import com.example.core.database.dao.DocumentDao
import com.example.core.database.dao.MemoryDao
import com.example.core.database.dao.MessageDao
import com.example.core.database.dao.ModelDao
import com.example.core.database.dao.TaskDao
import com.example.core.database.dao.ToolExecutionDao
import com.example.core.database.entity.ConversationEntity
import com.example.core.database.entity.DocumentEntity
import com.example.core.database.entity.MemoryEntity
import com.example.core.database.entity.MessageEntity
import com.example.core.database.entity.ModelEntity
import com.example.core.database.entity.TaskEntity
import com.example.core.database.entity.TaskStepEntity
import com.example.core.database.entity.ToolExecutionEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        ConversationEntity::class,
        MessageEntity::class,
        TaskEntity::class,
        TaskStepEntity::class,
        MemoryEntity::class,
        DocumentEntity::class,
        ToolExecutionEntity::class,
        ModelEntity::class
    ],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class HarmesDatabase : RoomDatabase() {
    abstract fun conversationDao(): ConversationDao
    abstract fun messageDao(): MessageDao
    abstract fun taskDao(): TaskDao
    abstract fun memoryDao(): MemoryDao
    abstract fun documentDao(): DocumentDao
    abstract fun toolExecutionDao(): ToolExecutionDao
    abstract fun modelDao(): ModelDao

    companion object {
        @Volatile
        private var INSTANCE: HarmesDatabase? = null

        fun getDatabase(context: Context, scope: CoroutineScope): HarmesDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    HarmesDatabase::class.java,
                    "harmes_agent.db"
                ).fallbackToDestructiveMigration(true)
                    .addCallback(HarmesDatabaseCallback(scope))
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }

    private class HarmesDatabaseCallback(
        private val scope: CoroutineScope
    ) : RoomDatabase.Callback() {
        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            INSTANCE?.let { database ->
                scope.launch(Dispatchers.IO) {
                    populateInitialData(database)
                }
            }
        }

        private suspend fun populateInitialData(db: HarmesDatabase) {
            // Pre-seed available local and cloud models
            val defaultModels = listOf(
                ModelEntity(
                    id = "harmes-local-lite",
                    name = "Harmes Neural-Lite 1.2B",
                    provider = "On-Device NPU/CPU",
                    sizeMb = 650,
                    quantization = "Q4_K_M (4-bit)",
                    isLocal = true,
                    isActive = true,
                    supportedHardware = "CPU / NPU / DSP",
                    contextLength = 4096
                ),
                ModelEntity(
                    id = "harmes-local-compact",
                    name = "Harmes Mobile-Agent 3.8B",
                    provider = "On-Device GPU",
                    sizeMb = 2100,
                    quantization = "Q4_0 (INT4)",
                    isLocal = true,
                    isActive = false,
                    supportedHardware = "Qualcomm Adreno / Mali GPU",
                    contextLength = 8192
                ),
                ModelEntity(
                    id = "gemini-3.5-flash",
                    name = "Gemini 3.5 Flash",
                    provider = "Google Cloud AI",
                    sizeMb = 0,
                    quantization = "Cloud FP16",
                    isLocal = false,
                    isActive = false,
                    supportedHardware = "Google Cloud TPUv5e",
                    contextLength = 1048576
                ),
                ModelEntity(
                    id = "gemini-3.1-pro-preview",
                    name = "Gemini 3.1 Pro (Deep Reasoner)",
                    provider = "Google Cloud AI",
                    sizeMb = 0,
                    quantization = "Cloud BF16",
                    isLocal = false,
                    isActive = false,
                    supportedHardware = "Google Cloud TPU Cluster",
                    contextLength = 2097152
                )
            )
            db.modelDao().insertModels(defaultModels)
        }
    }
}
