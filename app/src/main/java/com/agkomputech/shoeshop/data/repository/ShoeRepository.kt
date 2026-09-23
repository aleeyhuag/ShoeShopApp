package com.agkomputech.shoeshop.data.repository

import android.graphics.Bitmap
import com.agkomputech.shoeshop.data.local.dao.ShoeDao
import com.agkomputech.shoeshop.data.local.entity.PhotoAngle
import com.agkomputech.shoeshop.data.local.entity.Sale
import com.agkomputech.shoeshop.data.local.entity.Shoe
import com.agkomputech.shoeshop.data.local.entity.ShoePhoto
import com.agkomputech.shoeshop.data.local.entity.ShoeSize
import com.agkomputech.shoeshop.ml.EmbeddingExtractor
import com.agkomputech.shoeshop.ml.ShoeMatch
import com.agkomputech.shoeshop.ml.ShoeMatcher

class ShoeRepository(
    private val dao: ShoeDao,
    private val embeddingExtractor: EmbeddingExtractor,
    private val matcher: ShoeMatcher = ShoeMatcher()
) {

    // --- Add shoe flow ---

    /**
     * Saves a new shoe with its price/details, then embeds and stores each reference photo.
     * @param photos pairs of (angle, saved bitmap) — normally 3: TOP, SIDE, DETAIL
     * @param sizes pairs of (size label, quantity)
     */
    suspend fun addShoe(
        tagId: String,
        name: String,
        category: String,
        costPrice: Int,
        sellingPrice: Int,
        lowestPrice: Int,
        photos: List<Pair<PhotoAngle, Bitmap>>,
        imagePaths: List<String>,   // where each bitmap was saved to disk, same order as `photos`
        sizes: List<Pair<String, Int>>
    ): Long {
        val shoeId = dao.insertShoe(
            Shoe(
                tagId = tagId,
                name = name,
                category = category,
                costPrice = costPrice,
                sellingPrice = sellingPrice,
                lowestPrice = lowestPrice,
                dateAddedEpochMillis = System.currentTimeMillis()
            )
        )

        photos.forEachIndexed { index, (angle, bitmap) ->
            val embedding = embeddingExtractor.extract(bitmap)
            dao.insertPhoto(
                ShoePhoto(
                    shoeId = shoeId,
                    angle = angle,
                    imagePath = imagePaths[index],
                    embedding = embedding
                )
            )
        }

        sizes.forEach { (size, quantity) ->
            dao.insertSize(ShoeSize(shoeId = shoeId, size = size, quantity = quantity))
        }

        return shoeId
    }

    // --- Scan flow ---

    /** Embeds the live camera frame and ranks it against every saved shoe. */
    suspend fun findMatchesForFrame(bitmap: Bitmap, topN: Int = 3): List<ShoeMatch> {
        val liveEmbedding = embeddingExtractor.extract(bitmap)
        val allPhotos = dao.getAllPhotos()
        return matcher.findMatches(liveEmbedding, allPhotos, topN)
    }

    suspend fun getShoe(shoeId: Long): Shoe? = dao.getShoe(shoeId)

    suspend fun getSizesForShoe(shoeId: Long): List<ShoeSize> = dao.getSizesForShoe(shoeId)

    // --- Sales / receipts ---

    suspend fun recordSale(
        shoeId: Long,
        size: String,
        quantitySold: Int,
        salePrice: Int,
        customerName: String?
    ): Long = dao.insertSale(
        Sale(
            shoeId = shoeId,
            size = size,
            quantitySold = quantitySold,
            salePrice = salePrice,
            customerName = customerName,
            dateEpochMillis = System.currentTimeMillis()
        )
    )
}
