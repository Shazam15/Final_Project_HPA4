package com.utp.finalproject.data

import android.content.ContentValues
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class TaskRepository(
    private val databaseHelper: TaskDatabaseHelper
) {

    suspend fun getTasks(orderBy: String): List<PetTask> = withContext(Dispatchers.IO) {
        val db = databaseHelper.readableDatabase
        val sortColumn = when (orderBy) {
            ORDER_BY_PRIORITY -> TaskDatabaseHelper.COL_PRIORITY
            ORDER_BY_PET -> TaskDatabaseHelper.COL_PET_NAME
            else -> TaskDatabaseHelper.COL_DUE_DATE
        }

        db.query(
            TaskDatabaseHelper.TABLE_TASKS,
            null,
            null,
            null,
            null,
            null,
            "$sortColumn COLLATE NOCASE ASC"
        ).use { cursor ->
            buildList {
                val idIndex = cursor.getColumnIndexOrThrow(TaskDatabaseHelper.COL_ID)
                val petNameIndex = cursor.getColumnIndexOrThrow(TaskDatabaseHelper.COL_PET_NAME)
                val taskTypeIndex = cursor.getColumnIndexOrThrow(TaskDatabaseHelper.COL_TASK_TYPE)
                val dueDateIndex = cursor.getColumnIndexOrThrow(TaskDatabaseHelper.COL_DUE_DATE)
                val priorityIndex = cursor.getColumnIndexOrThrow(TaskDatabaseHelper.COL_PRIORITY)
                val notesIndex = cursor.getColumnIndexOrThrow(TaskDatabaseHelper.COL_NOTES)
                val completedIndex = cursor.getColumnIndexOrThrow(TaskDatabaseHelper.COL_COMPLETED)

                while (cursor.moveToNext()) {
                    add(
                        PetTask(
                            id = cursor.getLong(idIndex),
                            petName = cursor.getString(petNameIndex),
                            taskType = cursor.getString(taskTypeIndex),
                            dueDate = cursor.getString(dueDateIndex),
                            priority = cursor.getString(priorityIndex),
                            notes = cursor.getString(notesIndex),
                            isCompleted = cursor.getInt(completedIndex) == 1
                        )
                    )
                }
            }
        }
    }

    suspend fun getTask(id: Long): PetTask? = withContext(Dispatchers.IO) {
        val db = databaseHelper.readableDatabase

        db.query(
            TaskDatabaseHelper.TABLE_TASKS,
            null,
            "${TaskDatabaseHelper.COL_ID} = ?",
            arrayOf(id.toString()),
            null,
            null,
            null
        ).use { cursor ->
            if (!cursor.moveToFirst()) {
                return@withContext null
            }

            PetTask(
                id = cursor.getLong(cursor.getColumnIndexOrThrow(TaskDatabaseHelper.COL_ID)),
                petName = cursor.getString(cursor.getColumnIndexOrThrow(TaskDatabaseHelper.COL_PET_NAME)),
                taskType = cursor.getString(cursor.getColumnIndexOrThrow(TaskDatabaseHelper.COL_TASK_TYPE)),
                dueDate = cursor.getString(cursor.getColumnIndexOrThrow(TaskDatabaseHelper.COL_DUE_DATE)),
                priority = cursor.getString(cursor.getColumnIndexOrThrow(TaskDatabaseHelper.COL_PRIORITY)),
                notes = cursor.getString(cursor.getColumnIndexOrThrow(TaskDatabaseHelper.COL_NOTES)),
                isCompleted = cursor.getInt(cursor.getColumnIndexOrThrow(TaskDatabaseHelper.COL_COMPLETED)) == 1
            )
        }
    }

    suspend fun saveTask(task: PetTask): Long = withContext(Dispatchers.IO) {
        val values = task.toContentValues()
        val db = databaseHelper.writableDatabase

        if (task.id == 0L) {
            db.insert(TaskDatabaseHelper.TABLE_TASKS, null, values)
        } else {
            db.update(
                TaskDatabaseHelper.TABLE_TASKS,
                values,
                "${TaskDatabaseHelper.COL_ID} = ?",
                arrayOf(task.id.toString())
            )
            task.id
        }
    }

    suspend fun updateCompletion(taskId: Long, isCompleted: Boolean) = withContext(Dispatchers.IO) {
        val values = ContentValues().apply {
            put(TaskDatabaseHelper.COL_COMPLETED, if (isCompleted) 1 else 0)
        }

        databaseHelper.writableDatabase.update(
            TaskDatabaseHelper.TABLE_TASKS,
            values,
            "${TaskDatabaseHelper.COL_ID} = ?",
            arrayOf(taskId.toString())
        )
    }

    suspend fun deleteTask(taskId: Long) = withContext(Dispatchers.IO) {
        databaseHelper.writableDatabase.delete(
            TaskDatabaseHelper.TABLE_TASKS,
            "${TaskDatabaseHelper.COL_ID} = ?",
            arrayOf(taskId.toString())
        )
    }

    private fun PetTask.toContentValues(): ContentValues {
        return ContentValues().apply {
            put(TaskDatabaseHelper.COL_PET_NAME, petName)
            put(TaskDatabaseHelper.COL_TASK_TYPE, taskType)
            put(TaskDatabaseHelper.COL_DUE_DATE, dueDate)
            put(TaskDatabaseHelper.COL_PRIORITY, priority)
            put(TaskDatabaseHelper.COL_NOTES, notes)
            put(TaskDatabaseHelper.COL_COMPLETED, if (isCompleted) 1 else 0)
        }
    }

    companion object {
        const val ORDER_BY_DATE = "Fecha"
        const val ORDER_BY_PRIORITY = "Prioridad"
        const val ORDER_BY_PET = "Mascota"
    }
}
