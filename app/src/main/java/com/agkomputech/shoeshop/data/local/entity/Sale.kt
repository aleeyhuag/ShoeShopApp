package com.agkomputech.shoeshop.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * One row per sale, feeding the receipt/PDF generation flow (same pattern as the
 * pharmacy app's sales page). Sales are recorded but do not currently auto-reduce
 * ShoeSize.quantity — matches the "receipts only for now" decision made for the
 * pharmacy project; wire up stock deduction later if wanted.
 */
@Entity(
    tableName = "sales",
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
data class Sale(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val shoeId: Long,
    val size: String,
    val quantitySold: Int,
    val salePrice: Int,
    val customerName: String? = null,
    val dateEpochMillis: Long
)
