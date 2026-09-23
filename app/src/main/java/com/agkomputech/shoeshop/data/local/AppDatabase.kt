package com.agkomputech.shoeshop.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.agkomputech.shoeshop.data.local.dao.ShoeDao
import com.agkomputech.shoeshop.data.local.entity.EmbeddingConverter
import com.agkomputech.shoeshop.data.local.entity.Sale
import com.agkomputech.shoeshop.data.local.entity.Shoe
import com.agkomputech.shoeshop.data.local.entity.ShoePhoto
import com.agkomputech.shoeshop.data.local.entity.ShoeSize

@Database(
    entities = [Shoe::class, ShoePhoto::class, ShoeSize::class, Sale::class],
    version = 1,
    exportSchema = false
)
@TypeConverters(EmbeddingConverter::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun shoeDao(): ShoeDao

    companion object {
        @Volatile private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "shoeshop.db"
                ).build().also { INSTANCE = it }
            }
    }
}
