package com.ugur.ugurpekunsal_comp304lab4_ex1

import android.content.Context
import android.util.Log
import androidx.work.Worker
import androidx.work.WorkerParameters

class LocationSyncWorker(
    context: Context,
    workerParams: WorkerParameters
) : Worker(context, workerParams) {

    override fun doWork(): Result {
        Log.d("LocationSyncWorker", "Starting background sync...")
        // Simulate some work
        Thread.sleep(3000)
        Log.d("LocationSyncWorker", "Background sync completed")
        return Result.success()
    }
} 