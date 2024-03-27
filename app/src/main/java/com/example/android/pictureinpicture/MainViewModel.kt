/*
 * Copyright (C) 2021 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.android.pictureinpicture

import android.content.SharedPreferences
import android.os.SystemClock
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.map
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.android.awaitFrame
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class MainViewModel(private val prefs: SharedPreferences) : ViewModel() {

    private var job: Job? = null

    private var startUptimeMillis = SystemClock.uptimeMillis()

    private val timeMillis = MutableLiveData(0L)
    private val _started = MutableLiveData(false)

    val started: LiveData<Boolean> = _started

    val time = timeMillis.map { millis ->
        val minutes = millis / 1000 / 60
        val m = minutes.toString().padStart(2, '0')
        val seconds = (millis / 1000) % 60
        val s = seconds.toString().padStart(2, '0')
        val hundredths = (millis % 1000) / 10
        val h = hundredths.toString().padStart(2, '0')
        "$m:$s:$h"
    }

    /**
     * Starts the stopwatch if it is not yet started, or pauses it if it is already started.
     */
    fun startOrPause() {
        if (started.value == true) {
            pause()
        } else {
            start()
        }
    }

    private fun start() {
        _started.value = true
        job = viewModelScope.launch { start() }
    }

    private fun pause() {
        _started.value = false
        job?.cancel()
    }

    private suspend fun CoroutineScope.start() {
        startUptimeMillis = SystemClock.uptimeMillis() - (timeMillis.value ?: 0L)
        while (isActive) {
            timeMillis.value = (SystemClock.uptimeMillis() - startUptimeMillis)
            // Updates on every render frame.
            awaitFrame()
        }
    }

    fun saveCurrentState() {
        prefs.edit().putLong("start_up_time", startUptimeMillis).apply()
        prefs.edit().putLong("time", timeMillis.value ?: 0L).apply()
        prefs.edit().putBoolean("started", started.value ?: false).apply()
    }

    fun restoreState() {
        startUptimeMillis = prefs.getLong("start_up_time", SystemClock.uptimeMillis())
        _started.value = prefs.getBoolean("started", false)
        if (started.value == true) {
            timeMillis.value = SystemClock.uptimeMillis() - startUptimeMillis
            start()
        } else {
            timeMillis.value = prefs.getLong("time", 0L)
        }
    }


    /**
     * Clears the stopwatch to 00:00:00.
     */
    fun clear() {
        startUptimeMillis = SystemClock.uptimeMillis()
        timeMillis.value = 0L
        prefs.edit().clear().apply()
    }

}

class MainViewModelFactory(
    private val prefs: SharedPreferences,
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MainViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return MainViewModel(
                prefs = prefs,
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
