package com.agkomputech.shoeshop

import android.app.Application
import com.agkomputech.shoeshop.data.local.AppDatabase
import com.agkomputech.shoeshop.data.repository.ShoeRepository
import com.agkomputech.shoeshop.ml.EmbeddingExtractor

class ShoeShopApplication : Application() {

    lateinit var repository: ShoeRepository
        private set

    private lateinit var embeddingExtractor: EmbeddingExtractor

    override fun onCreate() {
        super.onCreate()
        val database = AppDatabase.getInstance(this)
        embeddingExtractor = EmbeddingExtractor(this)
        repository = ShoeRepository(
            dao = database.shoeDao(),
            embeddingExtractor = embeddingExtractor
        )
    }

    override fun onTerminate() {
        super.onTerminate()
        embeddingExtractor.close()
    }
}
