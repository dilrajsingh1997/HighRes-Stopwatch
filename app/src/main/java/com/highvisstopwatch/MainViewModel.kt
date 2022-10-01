package com.highvisstopwatch

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class MainViewModel : ViewModel() {
    companion object {
        private const val TAG = "MainViewModel"
    }

    private val _stateFlow = MutableStateFlow<State>(State.Initial())
    val stateFlow = _stateFlow.asStateFlow()

    private var _timerJob: Job? = null

    private val state: State
        get() = _stateFlow.value

    fun startTimer() {
        (state as? State.Initial)?.let { initialState ->
            startTimerInternal(initialState.initialTime, initialState.initialTime)
        } ?: (state as? State.Paused)?.let { pausedState ->
            startTimerInternal(pausedState.timeRemaining, pausedState.initialTime)
        } ?: run {
            Log.d(TAG, "cannot start timer as state is not initial or paused- $state")
        }
    }

    private fun startTimerInternal(timeRemaining: Int, initialTime: Int) {
        _timerJob?.cancel()
        _timerJob = viewModelScope.launch {
            var time = timeRemaining
            _stateFlow.update {
                State.Running(time, initialTime)
            }

            while (time > 0) {
                time -= 1
                delay(1000)
                _stateFlow.update {
                    if (it is State.Running) {
                        State.Running(time, initialTime)
                    } else {
                        it
                    }
                }
            }

            _stateFlow.update {
                if (it is State.Running) {
                    State.Initial(initialTime)
                } else {
                    it
                }
            }
        }
    }

    fun pauseTimer() {
        (state as? State.Running)?.let { runningState ->
            _timerJob?.cancel()
            _stateFlow.update {
                State.Paused(runningState.timeRemaining, runningState.initialTime)
            }
        } ?: run {
            Log.d(TAG, "cannot pause timer as timer wasn't running- $state")
        }
    }

    fun stopTimer() {
        (state as? State.Running)?.let { runningState ->
            _timerJob?.cancel()
            _stateFlow.update {
                State.Initial(runningState.initialTime)
            }
        } ?: (state as? State.Paused)?.let { pausedState ->
            _timerJob?.cancel()
            _stateFlow.update {
                State.Initial(pausedState.initialTime)
            }
        } ?: run {
            Log.d(TAG, "cannot pause timer as timer wasn't running/paused- $state")
        }
    }

    fun changeInitialTime(initialTime: Int) {
        (state as? State.Initial)?.let { initialState ->
            _stateFlow.update {
                State.Initial(initialTime)
            }
        } ?: run {
            Log.d(TAG, "cannot set initial time- $state")
        }
    }
}
