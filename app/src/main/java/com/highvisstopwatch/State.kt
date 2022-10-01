package com.highvisstopwatch

sealed class State {
    companion object {
        private const val DEFAULT_INIT_TIME = 40
    }

    data class Initial(val initialTime: Int = DEFAULT_INIT_TIME) : State()
    data class Running(val timeRemaining: Int, val initialTime: Int) : State()
    data class Paused(val timeRemaining: Int, val initialTime: Int) : State()
}
