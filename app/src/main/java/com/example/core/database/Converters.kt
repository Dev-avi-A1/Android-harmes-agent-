package com.example.core.database

import androidx.room.TypeConverter
import com.example.core.model.MemoryType
import com.example.core.model.ProcessingEngine
import com.example.core.model.RiskLevel
import com.example.core.model.SenderRole
import com.example.core.model.TaskStatus

class Converters {
    @TypeConverter
    fun fromSenderRole(value: SenderRole): String = value.name

    @TypeConverter
    fun toSenderRole(value: String): SenderRole = try {
        SenderRole.valueOf(value)
    } catch (_: Exception) {
        SenderRole.USER
    }

    @TypeConverter
    fun fromProcessingEngine(value: ProcessingEngine): String = value.name

    @TypeConverter
    fun toProcessingEngine(value: String): ProcessingEngine = try {
        ProcessingEngine.valueOf(value)
    } catch (_: Exception) {
        ProcessingEngine.LOCAL_AI
    }

    @TypeConverter
    fun fromMemoryType(value: MemoryType): String = value.name

    @TypeConverter
    fun toMemoryType(value: String): MemoryType = try {
        MemoryType.valueOf(value)
    } catch (_: Exception) {
        MemoryType.SEMANTIC
    }

    @TypeConverter
    fun fromTaskStatus(value: TaskStatus): String = value.name

    @TypeConverter
    fun toTaskStatus(value: String): TaskStatus = try {
        TaskStatus.valueOf(value)
    } catch (_: Exception) {
        TaskStatus.PLANNED
    }

    @TypeConverter
    fun fromRiskLevel(value: RiskLevel): String = value.name

    @TypeConverter
    fun toRiskLevel(value: String): RiskLevel = try {
        RiskLevel.valueOf(value)
    } catch (_: Exception) {
        RiskLevel.LOW
    }
}
