package com.playfulprogramming.cms.tasks.data

sealed interface TaskResult<T: Any> {
    class Accepted<T: Any>: TaskResult<T>
    class Success<T: Any>(val result: T): TaskResult<T>
    class Failed<T: Any>: TaskResult<T>
}