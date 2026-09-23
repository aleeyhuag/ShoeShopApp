package com.agkomputech.shoeshop.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * One row per shoe STYLE (not per size — price is the same across sizes).
 * tagId is a human-friendly label ("#0142") printed for your own reference;
 * it is not required for lookup since scanning finds the shoe by photo.
 */
@Entity(tableName = "shoes")
data class Shoe(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val tagId: String,
    val name: String,           // free text, e.g. "Puma Slide — Navy" — for your own reference
    val category: String,       // Sneakers / Slides / Flip-flops / Crocs / Formal / Boots
    val costPrice: Int,         // in Naira, stored as whole numbers (kobo not tracked)
    val sellingPrice: Int,
    val lowestPrice: Int,       // the floor you'll accept when a customer bargains
    val dateAddedEpochMillis: Long
)
