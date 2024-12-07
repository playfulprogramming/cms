package com.playfulprogramming.cms.tasks

import com.playfulprogramming.cms.sql.Database
import com.playfulprogramming.cms.sql.TaskResults
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement

interface TaskService {
    suspend fun getOrCreate(task: String, id: String, data: JsonElement): TaskResults?
}

class TaskServiceImpl(
    private val database: Database,
) : TaskService {
    override suspend fun getOrCreate(task: String, id: String, data: JsonElement) = withContext(Dispatchers.IO) {
        database.transactionWithResult {
            val result = database.tasksQueries
                .getResultById(task, id)
                .executeAsOneOrNull()

            if (result == null) {
                database.tasksQueries.insertRequest(task, id, Json.encodeToString(data))
            }

            result
        }
    }
}
