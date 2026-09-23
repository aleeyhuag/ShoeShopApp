package com.agkomputech.shoeshop.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Stock is tracked per size even though price is per style.
 * One row per (shoe, size) pair.
 */
@Entity(
    tableName = "shoe_sizes",
    foreignKeys = [
        ForeignKey(
            entity = Shoe::class,
            parentColumns = ["id"],
            childColumns = ["shoeId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("shoeId")]
)
data class ShoeSize(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val shoeId: Long,
    val size: String,      // "42", "9.5", etc — kept as string to support mixed size systems
    val quantity: Int
)
