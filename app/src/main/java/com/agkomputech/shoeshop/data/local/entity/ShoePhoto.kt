package com.agkomputech.shoeshop.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.TypeConverter
import androidx.room.TypeConverters

enum class PhotoAngle { TOP, SIDE, DETAIL }

/**
 * One row per reference photo of a shoe (recommended: 3 per shoe — TOP, SIDE, DETAIL).
 * `embedding` is the numeric fingerprint produced by the TFLite model at save time;
 * it's what ShoeMatcher compares against a live camera frame — never edited by hand.
 */
@Entity(
    tableName = "shoe_photos",
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
@TypeConverters(EmbeddingConverter::class)
data class ShoePhoto(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val shoeId: Long,
    val angle: PhotoAngle,
    val imagePath: String,       // file path in app-private internal storage
    val embedding: FloatArray    // fixed-length vector from EmbeddingExtractor, e.g. size 1280
) {
    // FloatArray needs manual equals/hashCode for Room's data class generation
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is ShoePhoto) return false
        return id == other.id && shoeId == other.shoeId && angle == other.angle &&
            imagePath == other.imagePath && embedding.contentEquals(other.embedding)
    }
    override fun hashCode(): Int {
        var result = id.hashCode()
        result = 31 * result + shoeId.hashCode()
        result = 31 * result + angle.hashCode()
        result = 31 * result + imagePath.hashCode()
        result = 31 * result + embedding.contentHashCode()
        return result
    }
}

/** Stores a FloatArray embedding as a compact byte blob in SQLite. */
class EmbeddingConverter {
    @TypeConverter
    fun fromFloatArray(value: FloatArray): ByteArray {
        val buffer = java.nio.ByteBuffer.allocate(value.size * 4)
        value.forEach { buffer.putFloat(it) }
        return buffer.array()
    }

    @TypeConverter
    fun toFloatArray(bytes: ByteArray): FloatArray {
        val buffer = java.nio.ByteBuffer.wrap(bytes)
        val floats = FloatArray(bytes.size / 4)
        for (i in floats.indices) floats[i] = buffer.getFloat(i * 4)
        return floats
    }
}
