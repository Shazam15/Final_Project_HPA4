package com.utp.finalproject.data.local.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.utp.finalproject.data.local.dao.HistoryDao
import com.utp.finalproject.data.local.dao.PetDao
import com.utp.finalproject.data.local.dao.RewardDao
import com.utp.finalproject.data.local.dao.TaskDao
import com.utp.finalproject.data.local.entity.ActivityHistoryEntity
import com.utp.finalproject.data.local.entity.PetEntity
import com.utp.finalproject.data.local.entity.RewardEntity
import com.utp.finalproject.data.local.entity.TaskEntity

@Database(
    entities = [
        TaskEntity::class,
        PetEntity::class,
        RewardEntity::class,
        ActivityHistoryEntity::class
    ],
    version = 3,
    exportSchema = true
)
abstract class HomePetDatabase : RoomDatabase() {
    abstract fun taskDao(): TaskDao
    abstract fun petDao(): PetDao
    abstract fun rewardDao(): RewardDao
    abstract fun historyDao(): HistoryDao

    companion object {
        @Volatile
        private var INSTANCE: HomePetDatabase? = null

        fun getInstance(context: Context): HomePetDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    HomePetDatabase::class.java,
                    "homepet_room.db"
                ).addMigrations(MIGRATION_1_2, MIGRATION_2_3)
                    .build()
                    .also { INSTANCE = it }
            }
        }

        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE tasks ADD COLUMN locationName TEXT")
                db.execSQL("ALTER TABLE tasks ADD COLUMN latitude REAL")
                db.execSQL("ALTER TABLE tasks ADD COLUMN longitude REAL")
                db.execSQL("ALTER TABLE tasks ADD COLUMN placeId TEXT")
                db.execSQL("ALTER TABLE pet ADD COLUMN hunger INTEGER NOT NULL DEFAULT 100")
                db.execSQL("ALTER TABLE pet ADD COLUMN lastInteractionAt INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE pet ADD COLUMN lastStatsUpdateAt INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE pet ADD COLUMN lastAppOpenedAt INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE pet ADD COLUMN lastDecayNotificationAt INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE pet ADD COLUMN lastNotificationLevel TEXT NOT NULL DEFAULT ''")
                db.execSQL(
                    "UPDATE pet SET lastInteractionAt = lastUpdatedAt, " +
                        "lastStatsUpdateAt = lastUpdatedAt, lastAppOpenedAt = lastUpdatedAt " +
                        "WHERE lastStatsUpdateAt = 0"
                )
            }
        }

        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE pet ADD COLUMN equippedHat TEXT NOT NULL DEFAULT ''")
                db.execSQL("ALTER TABLE pet ADD COLUMN equippedClothing TEXT NOT NULL DEFAULT ''")
                db.execSQL("ALTER TABLE pet ADD COLUMN equippedBackground TEXT NOT NULL DEFAULT ''")
            }
        }
    }
}
