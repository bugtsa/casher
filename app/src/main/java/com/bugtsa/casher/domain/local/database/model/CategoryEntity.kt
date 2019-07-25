package com.bugtsa.casher.domain.local.database.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "category")
data class CategoryEntity(
        val remoteId: Long,
        val name: String
) {
    @PrimaryKey(autoGenerate = true)
    var id: Long = 0L
}