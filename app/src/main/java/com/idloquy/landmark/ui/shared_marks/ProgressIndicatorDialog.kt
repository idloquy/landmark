package com.idloquy.landmark.ui.shared_marks

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.idloquy.landmark.ui.theme.LandmarkTheme

@Composable
fun ProgressIndicatorDialog(
    onDismiss: () -> Unit,
    text: String,
) {
    Dialog(
        onDismissRequest = onDismiss,
    ) {
        Surface {
            Row(
                modifier = Modifier.padding(25.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                CircularProgressIndicator()

                Spacer(Modifier.width(15.dp))

                Text(text, style = MaterialTheme.typography.bodyLarge)
            }
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun ProgressIndicatorDialogPreview() {
    LandmarkTheme {
        Surface(Modifier.fillMaxSize()) {
            ProgressIndicatorDialog(
                onDismiss = {},
                text = "Updating mark...",
            )
        }
    }
}