package com.agkomputech.shoeshop.ml

import com.agkomputech.shoeshop.data.local.entity.ShoePhoto

/**
 * A single ranked result shown on the scan screen.
 * confidencePercent is just similarity*100, clamped to 0..100 for display.
 */
data class ShoeMatch(
    val shoeId: Long,
    val bestPhoto: ShoePhoto,
    val confidencePercent: Int
)

/**
 * Brute-force cosine-similarity search over every saved reference photo.
 * Fine at a few-hundred-to-low-thousands scale — no vector database needed.
 * All photos are loaded into memory once (see ShoeRepository) and reused
 * across scans until a new shoe is added.
 */
class ShoeMatcher {

    /**
     * @param liveEmbedding embedding of the frame just captured by the camera
     * @param savedPhotos every reference photo currently in the database (each already L2-normalized)
     * @param topN how many ranked matches to return (3 in the mockup screen)
     */
    fun findMatches(
        liveEmbedding: FloatArray,
        savedPhotos: List<ShoePhoto>,
        topN: Int = 3
    ): List<ShoeMatch> {
        // Compare against every photo, then keep only each shoe's single best-scoring photo
        // — a shoe with 3 reference angles shouldn't crowd out other shoes in the results.
        val bestPerShoe = HashMap<Long, ShoeMatch>()

        for (photo in savedPhotos) {
            val similarity = cosineSimilarity(liveEmbedding, photo.embedding)
            val confidence = (similarity.coerceIn(0f, 1f) * 100).toInt()

            val existing = bestPerShoe[photo.shoeId]
            if (existing == null || confidence > existing.confidencePercent) {
                bestPerShoe[photo.shoeId] = ShoeMatch(photo.shoeId, photo, confidence)
            }
        }

        return bestPerShoe.values
            .sortedByDescending { it.confidencePercent }
            .take(topN)
    }

    /** Both vectors are assumed already L2-normalized (see EmbeddingExtractor), so this is a plain dot product. */
    private fun cosineSimilarity(a: FloatArray, b: FloatArray): Float {
        require(a.size == b.size) { "Embedding size mismatch: ${a.size} vs ${b.size}" }
        var dot = 0f
        for (i in a.indices) dot += a[i] * b[i]
        return dot
    }
}
