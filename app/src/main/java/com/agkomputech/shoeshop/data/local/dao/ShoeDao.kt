package com.agkomputech.shoeshop.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
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

    @Update
    suspend fun updateShoe(shoe: Shoe)

    // Relies on the ShoePhoto/ShoeSize/Sale foreign keys' onDelete = CASCADE
    // (Room enables SQLite foreign-key enforcement by default), so this alone
    // also removes the shoe's photos, sizes and sale history.
    @Query("DELETE FROM shoes WHERE id = :shoeId")
    suspend fun deleteShoeById(shoeId: Long)

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

    // Editing sizes is done as "wipe and re-insert" from ShoeRepository.updateShoe(),
    // simpler and safer than diffing add/remove for a handful of size rows.
    @Query("DELETE FROM shoe_sizes WHERE shoeId = :shoeId")
    suspend fun deleteSizesForShoe(shoeId: Long)

    // --- Sales / receipts ---
    @Insert
    suspend fun insertSale(sale: Sale): Long

    @Query("SELECT * FROM sales ORDER BY dateEpochMillis DESC")
    fun observeAllSales(): Flow<List<Sale>>
}
