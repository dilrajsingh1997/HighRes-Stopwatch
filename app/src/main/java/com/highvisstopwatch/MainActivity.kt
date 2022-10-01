package com.highvisstopwatch

import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.Space
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.TextField
import androidx.compose.material.TextFieldColors
import androidx.compose.material.TextFieldDefaults
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.layoutId
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.ConstraintSet
import androidx.constraintlayout.compose.Dimension
import com.highvisstopwatch.ui.theme.HighVisStopwatchTheme
import kotlin.math.sqrt

class MainActivity : ComponentActivity() {
    private val _viewModel by viewModels<MainViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            HighVisStopwatchTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    Box(modifier = Modifier.fillMaxSize()) {
                        val state by _viewModel.stateFlow.collectAsState()

                        val isTimeChangeAllowed by remember {
                            derivedStateOf {
                                state is State.Initial
                            }
                        }
                        var openDialog by remember { mutableStateOf(false) }
                        var dialogTime by remember {
                            mutableStateOf(TextFieldValue(""))
                        }

                        if (openDialog) {
                            AlertDialog(
                                onDismissRequest = {
                                    openDialog = !openDialog
                                },
                                title = {
                                    Text(text = stringResource(R.string.enter_initial_time))
                                },
                                text = {
                                    Column {
                                        TextField(
                                            value = dialogTime,
                                            onValueChange = {
                                                dialogTime = it
                                            },
                                            colors = TextFieldDefaults.textFieldColors(textColor = MaterialTheme.colorScheme.onSurface)
                                        )
                                    }
                                },
                                confirmButton = {
                                    Button(
                                        onClick = {
                                            _viewModel.changeInitialTime(dialogTime.text.toInt())
                                            openDialog = !openDialog
                                        }
                                    ) {
                                        Text(text = stringResource(R.string.enter))
                                    }
                                }
                            )
                        }

                        Button(
                            modifier = Modifier.align(Alignment.TopCenter),
                            enabled = isTimeChangeAllowed,
                            onClick = {
                                if (isTimeChangeAllowed) {
                                    openDialog = !openDialog
                                }
                            },
                        ) {
                            Text(
                                text = stringResource(R.string.change_time)
                            )
                        }

                        val padding = 10.dp
                        val width = LocalConfiguration.current.screenWidthDp.dp - padding
                        val textWidth = width / sqrt(2.0).toFloat()
                        val textPadding = 0.dp + (width - textWidth) / 2

                        ConstraintLayout(
                            modifier = Modifier
                                .align(Alignment.Center)
                                .fillMaxWidth()
                                .aspectRatio(1f)
                                .padding(padding),
                            constraintSet = ConstraintSet {
                                val text = createRefFor("text")
                                val progress = createRefFor("progress")

                                constrain(progress) {
                                    top.linkTo(parent.top)
                                    bottom.linkTo(parent.bottom)
                                    start.linkTo(parent.start)
                                    end.linkTo(parent.end)

                                    this.height = Dimension.fillToConstraints
                                    this.width = Dimension.fillToConstraints
                                }

                                constrain(text) {
                                    top.linkTo(parent.top, textPadding)
                                    bottom.linkTo(parent.bottom, textPadding + 18.dp)
                                    start.linkTo(parent.start, textPadding)
                                    end.linkTo(parent.end, textPadding)

                                    this.height = Dimension.fillToConstraints
                                    this.width = Dimension.fillToConstraints
                                }
                            }
                        ) {
                            AutoResizeText(
                                modifier = Modifier
                                    .layoutId("text"),
                                text = when(val _state = state) {
                                    is State.Initial -> _state.initialTime
                                    is State.Paused -> _state.timeRemaining
                                    is State.Running -> _state.timeRemaining
                                }.toString()
                            )

                            CircularProgressIndicator(
                                modifier = Modifier
                                    .layoutId("progress"),
                                progress = when(val _state = state) {
                                    is State.Initial -> 0f
                                    is State.Paused -> {
                                        _state.timeRemaining.toFloat() / _state.initialTime
                                    }
                                    is State.Running -> {
                                        _state.timeRemaining.toFloat() / _state.initialTime
                                    }
                                },
                                strokeWidth = 20.dp,
                            )
                        }

                        Row(
                            modifier = Modifier
                                .align(Alignment.BottomCenter)
                                .padding(bottom = 20.dp),
                        ) {
                            Button(
                                onClick = {
                                    _viewModel.stopTimer()
                                }
                            ) {
                                Text(text = stringResource(R.string.stop_timer))
                            }

                            Spacer(modifier = Modifier.width(20.dp))

                            Button(
                                onClick = {
                                    when (state) {
                                        is State.Initial -> {
                                            _viewModel.startTimer()
                                        }
                                        is State.Paused -> {
                                            _viewModel.startTimer()
                                        }
                                        is State.Running -> {
                                            _viewModel.pauseTimer()
                                        }
                                    }
                                },
                            ) {
                                Text(
                                    text = when (state) {
                                        is State.Initial -> stringResource(R.string.start_timer)
                                        is State.Paused -> stringResource(R.string.resume_timer)
                                        is State.Running -> stringResource(R.string.pause_timer)
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
