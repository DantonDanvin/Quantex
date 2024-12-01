package com.example.quantex

interface ExceptionListener {
    fun uncaughtException(thread: Thread, throwable: Throwable)
}