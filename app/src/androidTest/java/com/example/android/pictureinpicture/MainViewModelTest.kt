package com.example.android.pictureinpicture

import android.content.SharedPreferences
import android.os.SystemClock
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.Observer
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.AdditionalMatchers.not
import org.mockito.ArgumentMatchers.anyBoolean
import org.mockito.ArgumentMatchers.anyLong
import org.mockito.ArgumentMatchers.anyString
import org.mockito.ArgumentMatchers.eq
import org.mockito.Mock
import org.mockito.Mockito.doNothing
import org.mockito.Mockito.mock
import org.mockito.Mockito.times
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import org.mockito.junit.MockitoJUnitRunner

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(MockitoJUnitRunner::class)
class MainViewModelTest {

    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    @Mock
    lateinit var mockPrefs: SharedPreferences

    @Mock
    lateinit var mockEditor: SharedPreferences.Editor

    @Before
    fun setUp() {
        `when`(mockPrefs.edit()).thenReturn(
            mockEditor
        )
        `when`(mockEditor.putLong(anyString(), anyLong())).thenReturn(mockEditor)
        `when`(mockEditor.putBoolean(anyString(), anyBoolean())).thenReturn(mockEditor)
        `when`(mockEditor.clear()).thenReturn(mockEditor)
        doNothing().`when`(mockEditor).apply()

    }

    @After
    fun tearDown() {
    }

    @Test
    fun testTimerDefaultState() = runTest {
        //Given
        val expectedTime = "00:00:00"
        val expectedIsStarted = false

        val startedObserver = mock(Observer::class.java) as Observer<Boolean>

        //When
        val viewModel = MainViewModel(mockPrefs)
        viewModel.started.observeForever(startedObserver)

        //Then
        assertEquals(expectedTime, viewModel.time.getValueForTest())
        verify(startedObserver).onChanged(eq(expectedIsStarted))

        viewModel.started.removeObserver(startedObserver)
    }

    @Test
    fun testStartOrPauseTimer() = runTest {
        //Given
        val expectedIsStarted = true

        val startedObserver = mock(Observer::class.java) as Observer<Boolean>

        val viewModel = MainViewModel(mockPrefs)
        viewModel.started.observeForever(startedObserver)

        //When start timer
        viewModel.startOrPause()

        //Then
        verify(startedObserver).onChanged(eq(expectedIsStarted))

        //When pause timer
        viewModel.startOrPause()

        //Then
        verify(startedObserver, times(2)).onChanged(not(eq(expectedIsStarted)))

        viewModel.started.removeObserver(startedObserver)
    }

    @Test
    fun testRestTimer() = runTest {
        //Given
        val expectedTime = "00:00:00"
        val givenTime = SystemClock.uptimeMillis()

        `when`(mockPrefs.getLong(eq("time"), anyLong())).thenReturn(givenTime)
        `when`(mockPrefs.getBoolean(eq("started"), eq(false))).thenReturn(false)
        val viewModel = MainViewModel(mockPrefs)

        //When
        viewModel.restoreState()

        //Then
        assertNotEquals(expectedTime, viewModel.time.getValueForTest())

        //When
        viewModel.clear()
        val actualTime = viewModel.time.getValueForTest()

        //Then
        assertEquals(expectedTime, actualTime)
        verify(mockEditor, times(1)).clear()
    }

    @Test
    fun testRestoreStartState() = runTest {
        //Given
        val notExpectedTime = "00:00:00"
        val expectedIsStarted = true

        val restoreTime = SystemClock.uptimeMillis() - 1000L

        `when`(mockPrefs.getLong(eq("start_up_time"), anyLong())).thenReturn(restoreTime)
        `when`(mockPrefs.getBoolean(eq("started"), eq(false))).thenReturn(true)

        val viewModel = MainViewModel(mockPrefs)

        //When
        viewModel.restoreState()
        val actualTime = viewModel.time.getValueForTest()
        val actualIsStarted = viewModel.started.getValueForTest()

        //Then
        assertNotEquals(notExpectedTime, actualTime)
        assertEquals(expectedIsStarted, actualIsStarted)
        verify(mockPrefs, times(1)).getLong(anyString(), anyLong())
        verify(mockPrefs, times(1)).getBoolean(anyString(), anyBoolean())
    }

    @Test
    fun testRestorePauseState() = runTest {
        //Given
        val notExpectedTime = "00:00:00"
        val expectedIsStarted = false

        val restoreTime = SystemClock.uptimeMillis()

        `when`(mockPrefs.getLong(eq("time"), anyLong())).thenReturn(restoreTime)
        `when`(mockPrefs.getBoolean(eq("started"), eq(false))).thenReturn(false)

        val viewModel = MainViewModel(mockPrefs)

        //When
        viewModel.restoreState()
        val actualTime = viewModel.time.getValueForTest()
        val actualIsStarted = viewModel.started.getValueForTest()

        //Then
        assertNotEquals(notExpectedTime, actualTime)
        assertEquals(expectedIsStarted, actualIsStarted)
        verify(mockPrefs, times(2)).getLong(anyString(), anyLong())
        verify(mockPrefs, times(1)).getBoolean(anyString(), anyBoolean())
    }

    @Test
    fun testSaveStartState() = runTest {
        //Given

        `when`(mockEditor.putLong(eq("start_up_time"), anyLong())).thenReturn(mockEditor)
        `when`(mockEditor.putLong(eq("time"), anyLong())).thenReturn(mockEditor)
        `when`(mockEditor.putBoolean(eq("started"), eq(false))).thenReturn(mockEditor)

        val viewModel = MainViewModel(mockPrefs)

        //When
        viewModel.saveCurrentState()

        //Then
        verify(mockEditor, times(2)).putLong(anyString(), anyLong())
        verify(mockEditor, times(1)).putBoolean(anyString(), anyBoolean())
        verify(mockEditor, times(3)).apply()
    }

    @Test
    fun testSavePauseState() = runTest {
        //Given

        `when`(mockEditor.putLong(eq("start_up_time"), anyLong())).thenReturn(mockEditor)
        `when`(mockEditor.putLong(eq("time"), anyLong())).thenReturn(mockEditor)
        `when`(mockEditor.putBoolean(eq("started"), eq(true))).thenReturn(mockEditor)

        val viewModel = MainViewModel(mockPrefs)

        //When
        viewModel.startOrPause()
        viewModel.saveCurrentState()

        //Then
        verify(mockEditor, times(2)).putLong(anyString(), anyLong())
        verify(mockEditor, times(1)).putBoolean(anyString(), anyBoolean())
        verify(mockEditor, times(3)).apply()
    }

}