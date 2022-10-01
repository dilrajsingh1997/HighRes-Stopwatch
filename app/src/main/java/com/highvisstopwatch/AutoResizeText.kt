package com.highvisstopwatch

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun AutoResizeText(modifier: Modifier = Modifier, initialFontSize: TextUnit = 1000.sp, text: String) {
    val textStyleBody1 = MaterialTheme.typography.bodyLarge
    var textStyle by remember { mutableStateOf(textStyleBody1.copy(fontSize = initialFontSize)) }
    var readyToDraw by remember { mutableStateOf(false) }
    var isTextLayout by remember {
        mutableStateOf(false)
    }

    Text(
        text = text,
        style = textStyle,
        maxLines = 1,
        softWrap = false,
        textAlign = TextAlign.Center,
        modifier = modifier.drawWithContent {
            if (readyToDraw) drawContent()
        },
        onTextLayout = { textLayoutResult ->
            if (!isTextLayout) {
                textStyle = textStyle.copy(fontSize = initialFontSize)
                isTextLayout = true
            } else if (textLayoutResult.didOverflowWidth || textLayoutResult.didOverflowHeight) {
                textStyle = textStyle.copy(fontSize = textStyle.fontSize * 0.9)
            } else {
                isTextLayout = false
                readyToDraw = true
            }
        }
    )
}
