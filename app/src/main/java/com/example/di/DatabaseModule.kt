package com.example.di

import android.content.Context
import com.example.core.database.HarmesDatabase
import com.example.core.database.dao.ConversationDao
import com.example.core.database.dao.DocumentDao
import com.example.core.database.dao.MemoryDao
import com.example.core.database.dao.MessageDao
import com.example.core.database.dao.ModelDao
import com.example.core.database.dao.TaskDao
import com.example.core.database.dao.ToolExecutionDao
import com.example.data.repository.ConversationRepository
import com.example.data.repository.ConversationRepositoryImpl
import com.example.data.repository.SettingsRepository
import com.example.data.repository.SettingsRepositoryImpl
import com.example.data.repository.TaskRepository
import com.example.data.repository.TaskRepositoryImpl
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideApplicationScope(): CoroutineScope {
        return CoroutineScope(SupervisorJob() + Dispatchers.Default)
    }

    @Provides
    @Singleton
    fun provideHarmesDatabase(
        @ApplicationContext context: Context,
        scope: CoroutineScope
    ): HarmesDatabase {
        return HarmesDatabase.getDatabase(context, scope)
    }

    @Provides
    fun provideConversationDao(database: HarmesDatabase): ConversationDao {
        return database.conversationDao()
    }

    @Provides
    fun provideMessageDao(database: HarmesDatabase): MessageDao {
        return database.messageDao()
    }

    @Provides
    fun provideTaskDao(database: HarmesDatabase): TaskDao {
        return database.taskDao()
    }

    @Provides
    fun provideMemoryDao(database: HarmesDatabase): MemoryDao {
        return database.memoryDao()
    }

    @Provides
    fun provideDocumentDao(database: HarmesDatabase): DocumentDao {
        return database.documentDao()
    }

    @Provides
    fun provideToolExecutionDao(database: HarmesDatabase): ToolExecutionDao {
        return database.toolExecutionDao()
    }

    @Provides
    fun provideModelDao(database: HarmesDatabase): ModelDao {
        return database.modelDao()
    }

    @Provides
    @Singleton
    fun provideConversationRepository(database: HarmesDatabase): ConversationRepository {
        return ConversationRepositoryImpl(database)
    }

    @Provides
    @Singleton
    fun provideTaskRepository(database: HarmesDatabase): TaskRepository {
        return TaskRepositoryImpl(database)
    }

    @Provides
    @Singleton
    fun provideSettingsRepository(@ApplicationContext context: Context): SettingsRepository {
        return SettingsRepositoryImpl(context)
    }
}
