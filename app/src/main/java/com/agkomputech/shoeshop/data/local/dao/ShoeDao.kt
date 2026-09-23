package com.agkomputech.shoeshop.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.agkomputech.shoeshop.data.local.entity.Sale
import com.agkomputech.shoeshop.data.local.entity.Shoe
import com.agkomputech.shoeshop.data.local.entity.ShoePhoto
import com.agkomputech.shoeshop.data.local.entity.ShoeSize
import kotlinx.coroutines.flow.Flow

@Dao
interface ShoeDao {

    // --- Shoes ---
    @Insert
    suspend fun insertShoe(shoe: Shoe): Long

    @Query("SELECT * FROM shoes ORDER BY dateAddedEpochMillis DESC")
    fun observeAllShoes(): Flow<List<Shoe>>

    @Query("SELECT * FROM shoes WHERE id = :shoeId")
    suspend fun getShoe(shoeId: Long): Shoe?

    @Query("SELECT * FROM shoes WHERE category = :category ORDER BY dateAddedEpochMillis DESC")
    fun observeShoesByCategory(category: String): Flow<List<Shoe>>

    // --- Photos / embeddings ---
    @Insert
    suspend fun insertPhoto(photo: ShoePhoto): Long

    // Loaded once at app start / after any add-shoe, then matched in memory —
    // see ShoeMatcher. Fine at a few-hundred-shoe scale; no vector DB needed.
    @Query("SELECT * FROM shoe_photos")
    suspend fun getAllPhotos(): List<ShoePhoto>

    @Query("SELECT * FROM shoe_photos WHERE shoeId = :shoeId")
    suspend fun getPhotosForShoe(shoeId: Long): List<ShoePhoto>

    // --- Sizes / stock ---
    @Insert
    suspend fun insertSize(size: ShoeSize): Long

    @Query("SELECT * FROM shoe_sizes WHERE shoeId = :shoeId")
    suspend fun getSizesForShoe(shoeId: Long): List<ShoeSize>

    // --- Sales / receipts ---
    @Insert
    suspend fun insertSale(sale: Sale): Long

    @Query("SELECT * FROM sales ORDER BY dateEpochMillis DESC")
    fun observeAllSales(): Flow<List<Sale>>
}
