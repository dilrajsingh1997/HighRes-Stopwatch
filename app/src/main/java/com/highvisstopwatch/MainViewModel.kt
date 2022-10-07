package com.highvisstopwatch

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
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
        intent { state ->
            when (state) {
                is State.Initial -> {
                    startTimerInternal(state.initialTime, state.initialTime)
                }
                is State.Paused -> {
                    startTimerInternal(state.timeRemaining, state.initialTime)
                }
                is State.Running -> {
                    Log.d(TAG, "cannot start timer as state is not initial or paused- $state")
                }
            }
        }
    }

    private fun startTimerInternal(timeRemaining: Int, initialTime: Int) {
        _timerJob?.cancel()
        reduce {
            if (it !is State.Running) {
                State.Running(timeRemaining, initialTime)
            } else {
                throw IllegalStateException("cannot start timer while already running- $state")
            }
        }
        _timerJob = intent {
            var time = timeRemaining

            while (time > 0) {
                time -= 1
                delay(1000)
                val _time = time
                reduce {
                    if (it is State.Running) {
                        State.Running(_time, initialTime)
                    } else {
                        it
                    }
                }
            }

            reduce {
                if (it is State.Running) {
                    State.Initial(initialTime)
                } else {
                    it
                }
            }
        }
    }

    fun pauseTimer() {
        reduce { state ->
            (state as? State.Running)?.let { runningState ->
                _timerJob?.cancel()
                State.Paused(runningState.timeRemaining, runningState.initialTime)
            } ?: run {
                Log.d(TAG, "cannot pause timer as timer wasn't running- $state")
                throw IllegalStateException("cannot pause timer as timer wasn't running- $state")
            }
        }
    }

    fun stopTimer() {
        reduce { state ->
            (state as? State.Running)?.let { runningState ->
                _timerJob?.cancel()
                State.Initial(runningState.initialTime)
            } ?: (state as? State.Paused)?.let { pausedState ->
                _timerJob?.cancel()
                State.Initial(pausedState.initialTime)
            } ?: run {
                Log.d(TAG, "cannot pause timer as timer wasn't running/paused- $state")
                throw IllegalStateException("cannot pause timer as timer wasn't running/paused- $state")
            }
        }

    }

    fun changeInitialTime(initialTime: Int) {
        reduce { state ->
            (state as? State.Initial)?.let { initialState ->
                State.Initial(initialTime)
            } ?: run {
                Log.d(TAG, "cannot set initial time- $state")
                throw IllegalStateException("cannot set initial time- $state")
            }
        }
    }

    private fun intent(block: suspend (State) -> Unit): Job {
        return viewModelScope.launch(Dispatchers.Default) {
            block(state)
        }
    }

    private fun reduce(block: suspend (State) -> State) {
        viewModelScope.launch(Dispatchers.Default) {
            _stateFlow.update {
                block(it)
            }
        }
    }
}
