package com.utp.finalproject.data

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class TaskDatabaseHelper(context: Context) : SQLiteOpenHelper(
    context,
    DATABASE_NAME,
    null,
    DATABASE_VERSION
) {

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE $TABLE_TASKS (
                $COL_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COL_PET_NAME TEXT NOT NULL,
                $COL_TASK_TYPE TEXT NOT NULL,
                $COL_DUE_DATE TEXT NOT NULL,
                $COL_PRIORITY TEXT NOT NULL,
                $COL_NOTES TEXT NOT NULL,
                $COL_COMPLETED INTEGER NOT NULL DEFAULT 0
            )
            """.trimIndent()
        )
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS $TABLE_TASKS")
        onCreate(db)
    }

    companion object {
        private const val DATABASE_NAME = "homepet.db"
        private const val DATABASE_VERSION = 1

        const val TABLE_TASKS = "pet_tasks"
        const val COL_ID = "id"
        const val COL_PET_NAME = "pet_name"
        const val COL_TASK_TYPE = "task_type"
        const val COL_DUE_DATE = "due_date"
        const val COL_PRIORITY = "priority"
        const val COL_NOTES = "notes"
        const val COL_COMPLETED = "completed"
    }
}
